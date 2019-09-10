package com.linbit.linstor.tasks;

import com.linbit.ImplementationError;
import com.linbit.linstor.InternalApiConsts;
import com.linbit.linstor.annotation.SystemContext;
import com.linbit.linstor.api.ApiCallRc;
import com.linbit.linstor.api.ApiModule;
import com.linbit.linstor.api.interfaces.serializer.CtrlStltSerializer;
import com.linbit.linstor.core.apicallhandler.controller.CtrlRscDeleteApiHelper;
import com.linbit.linstor.core.apicallhandler.controller.internal.CtrlSatelliteUpdateCaller;
import com.linbit.linstor.core.apicallhandler.response.ApiRcException;
import com.linbit.linstor.core.identifier.NodeName;
import com.linbit.linstor.core.objects.Node;
import com.linbit.linstor.core.objects.Resource;
import com.linbit.linstor.core.objects.Resource.RscFlags;
import com.linbit.linstor.logging.ErrorReporter;
import com.linbit.linstor.netcom.Peer;
import com.linbit.linstor.netcom.PeerNotConnectedException;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.tasks.TaskScheduleService.Task;
import com.linbit.utils.Pair;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import reactor.core.publisher.Flux;

@Singleton
public class RetryResourcesTask implements Task
{
    private static final long[] RETRY_DELAYS =
    {
        15_000,
        30_000,
        1 * 60_000,
        2 * 60_000,
        5 * 60_000,
        10 * 60_000,
        30 * 60_000,
        1 * 60 * 60_000,
        4 * 60 * 60_000,
        24 * 60 * 60_000,
    };
    private static final long TASK_TIMEOUT = RETRY_DELAYS[0];
    private static final String RSC_RETRY_API_NAME = "RetryResource";

    private final Object syncObj = new Object();
    private final HashMap<Resource, Pair<Integer, Long>> failedResources = new HashMap<>();

    private final AccessContext sysCtx;
    private final CtrlStltSerializer serializer;
    private final CtrlRscDeleteApiHelper rscDelHelper;
    private final ErrorReporter errorReporter;

    @Inject
    public RetryResourcesTask(
        @SystemContext AccessContext sysCtxRef,
        CtrlStltSerializer serializerRef,
        CtrlRscDeleteApiHelper rscDelHelperRef,
        ErrorReporter errorReporterRef
    )
    {
        sysCtx = sysCtxRef;
        serializer = serializerRef;
        rscDelHelper = rscDelHelperRef;
        errorReporter = errorReporterRef;
    }

    public boolean add(Resource rsc)
    {
        boolean added = false;
        synchronized (syncObj)
        {
            if (!failedResources.containsKey(rsc))
            {
                added = true;
                failedResources.put(rsc, new Pair<>(0, System.currentTimeMillis()));
            }
        }
        if (added)
        {
            errorReporter.logWarning(
                "RetryTask: Failed resource '%s' of node '%s' added for retry.",
                rsc.getDefinition().getName().displayValue,
                rsc.getAssignedNode().getName().displayValue
            );
        }
        return added;
    }

    public void remove(Resource rsc)
    {
        Object removed = null;
        synchronized (syncObj)
        {
            removed = failedResources.remove(rsc);
        }

        if (removed != null)
        {
            errorReporter.logInfo(
                "RetryTask: Failed resource '%s' of node '%s' removed from retry.",
                rsc.getDefinition().getName().displayValue,
                rsc.getAssignedNode().getName().displayValue
            );
        }
    }

    @Override
    public long run()
    {
        List<Resource> rscsToRetry;
        synchronized (syncObj)
        {
            rscsToRetry = getResourcesToRetry();
        }

        for (Resource rsc : rscsToRetry)
        {
            if (!rsc.isDeleted())
            {
                try
                {
                    Node node = rsc.getAssignedNode();
                    if (!node.isDeleted())
                    {
                        Peer peer = node.getPeer(sysCtx);
                        NodeName nodeName = node.getName();

                        errorReporter.logDebug(
                            "RetryTask: Contact satellite '%s' to retry resource '%s'.",
                            nodeName.displayValue,
                            rsc.getDefinition().getName().displayValue
                        );
                        // only update the one satellite, not every involved satellites
                        Flux<ApiCallRc> flux = peer.apiCall(
                            InternalApiConsts.API_CHANGED_RSC,
                            serializer
                                .headerlessBuilder()
                                .changedResource(
                                    rsc.getUuid(),
                                    rsc.getDefinition().getName().displayValue
                                )
                                .build()
                        ).map(
                            inputStream -> CtrlSatelliteUpdateCaller.deserializeApiCallRc(
                                nodeName,
                                inputStream
                            )
                        );

                        if (rsc.getStateFlags().isSet(sysCtx, RscFlags.DELETE))
                        {
                            flux = flux.concatWith(
                                rscDelHelper.deleteData(
                                    nodeName,
                                    rsc.getDefinition().getName()
                                )
                            )
                                .onErrorResume(ApiRcException.class, ignore -> Flux.empty())
                                .onErrorResume(PeerNotConnectedException.class, ignore -> Flux.empty())
                                .subscriberContext(
                                reactor.util.context.Context.of(
                                    ApiModule.API_CALL_NAME, RSC_RETRY_API_NAME,
                                    AccessContext.class, peer.getAccessContext(),
                                    Peer.class, peer
                                )
                            );
                        }
                        flux.subscribe();
                    }
                    else
                    {
                        remove(rsc);
                    }
                }
                catch (AccessDeniedException accDeniedExc)
                {
                    errorReporter.reportError(new ImplementationError(accDeniedExc));
                }
            }
        }
        return TASK_TIMEOUT;
    }

    private List<Resource> getResourcesToRetry()
    {
        List<Resource> ret = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (Entry<Resource, Pair<Integer, Long>> entry : failedResources.entrySet())
        {
            Pair<Integer, Long> pair = entry.getValue();
            int retryIdx;
            int times = 1;

            if (pair.objA >= RETRY_DELAYS.length)
            {
                retryIdx = RETRY_DELAYS.length - 1;
                times = pair.objA - RETRY_DELAYS.length + 1;
            }
            else
            {
                retryIdx = pair.objA;
            }

            long retryAt = pair.objB + RETRY_DELAYS[retryIdx] * times;
            retryAt = (retryAt / TASK_TIMEOUT) * TASK_TIMEOUT;

            if (now >= retryAt)
            {
                ret.add(entry.getKey());
                pair.objA += 1;
                pair.objB = now;
            }
        }

        return ret;
    }
}
