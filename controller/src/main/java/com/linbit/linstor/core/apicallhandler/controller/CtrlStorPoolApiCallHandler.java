package com.linbit.linstor.core.apicallhandler.controller;

import com.linbit.ImplementationError;
import com.linbit.InvalidNameException;
import com.linbit.linstor.InternalApiConsts;
import com.linbit.linstor.LinStorDataAlreadyExistsException;
import com.linbit.linstor.LinstorParsingUtils;
import com.linbit.linstor.NodeData;
import com.linbit.linstor.StorPool;
import com.linbit.linstor.StorPoolData;
import com.linbit.linstor.StorPoolDataFactory;
import com.linbit.linstor.StorPoolDefinitionData;
import com.linbit.linstor.StorPoolDefinitionDataControllerFactory;
import com.linbit.linstor.StorPoolName;
import com.linbit.linstor.Volume;
import com.linbit.linstor.annotation.ApiContext;
import com.linbit.linstor.annotation.PeerContext;
import com.linbit.linstor.api.ApiCallRc;
import com.linbit.linstor.api.ApiCallRcImpl;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.api.interfaces.serializer.CtrlClientSerializer;
import com.linbit.linstor.api.interfaces.serializer.CtrlStltSerializer;
import com.linbit.linstor.api.pojo.FreeSpacePojo;
import com.linbit.linstor.api.prop.LinStorObject;
import com.linbit.linstor.core.CoreModule;
import com.linbit.linstor.core.LinStor;
import com.linbit.linstor.core.apicallhandler.response.ApiAccessDeniedException;
import com.linbit.linstor.core.apicallhandler.response.ApiException;
import com.linbit.linstor.core.apicallhandler.response.ApiOperation;
import com.linbit.linstor.core.apicallhandler.response.ApiRcException;
import com.linbit.linstor.core.apicallhandler.response.ApiSQLException;
import com.linbit.linstor.core.apicallhandler.response.ApiSuccessUtils;
import com.linbit.linstor.core.apicallhandler.response.ResponseContext;
import com.linbit.linstor.core.apicallhandler.response.ResponseConverter;
import com.linbit.linstor.logging.ErrorReporter;
import com.linbit.linstor.netcom.Peer;
import com.linbit.linstor.propscon.Props;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.security.AccessType;
import com.linbit.linstor.security.ControllerSecurityModule;
import com.linbit.linstor.security.ObjectProtection;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import static com.linbit.utils.StringUtils.firstLetterCaps;
import static java.util.stream.Collectors.toList;

@Singleton
public class CtrlStorPoolApiCallHandler
{
    private final ErrorReporter errorReporter;
    private final AccessContext apiCtx;
    private final CtrlTransactionHelper ctrlTransactionHelper;
    private final CtrlPropsHelper ctrlPropsHelper;
    private final CtrlApiDataLoader ctrlApiDataLoader;
    private final StorPoolDefinitionDataControllerFactory storPoolDefinitionDataFactory;
    private final StorPoolDataFactory storPoolDataFactory;
    private final ObjectProtection nodesMapProt;
    private final ObjectProtection storPoolDfnMapProt;
    private final CoreModule.StorPoolDefinitionMap storPoolDfnMap;
    private final CtrlClientSerializer clientComSerializer;
    private final CtrlStltSerializer ctrlStltSerializer;
    private final CtrlSatelliteUpdater ctrlSatelliteUpdater;
    private final ResponseConverter responseConverter;
    private final Provider<Peer> peer;
    private final Provider<AccessContext> peerAccCtx;

    @Inject
    public CtrlStorPoolApiCallHandler(
        ErrorReporter errorReporterRef,
        @ApiContext AccessContext apiCtxRef,
        CtrlTransactionHelper ctrlTransactionHelperRef,
        CtrlPropsHelper ctrlPropsHelperRef,
        CtrlApiDataLoader ctrlApiDataLoaderRef,
        StorPoolDefinitionDataControllerFactory storPoolDefinitionDataFactoryRef,
        StorPoolDataFactory storPoolDataFactoryRef,
        @Named(ControllerSecurityModule.NODES_MAP_PROT) ObjectProtection nodesMapProtRef,
        @Named(ControllerSecurityModule.STOR_POOL_DFN_MAP_PROT) ObjectProtection storPoolDfnMapProtRef,
        CoreModule.StorPoolDefinitionMap storPoolDfnMapRef,
        CtrlClientSerializer clientComSerializerRef,
        CtrlStltSerializer ctrlStltSerializerRef,
        CtrlSatelliteUpdater ctrlSatelliteUpdaterRef,
        ResponseConverter responseConverterRef,
        Provider<Peer> peerRef,
        @PeerContext Provider<AccessContext> peerAccCtxRef
    )
    {
        errorReporter = errorReporterRef;
        apiCtx = apiCtxRef;
        ctrlTransactionHelper = ctrlTransactionHelperRef;
        ctrlPropsHelper = ctrlPropsHelperRef;
        ctrlApiDataLoader = ctrlApiDataLoaderRef;
        storPoolDefinitionDataFactory = storPoolDefinitionDataFactoryRef;
        storPoolDataFactory = storPoolDataFactoryRef;
        nodesMapProt = nodesMapProtRef;
        storPoolDfnMapProt = storPoolDfnMapProtRef;
        storPoolDfnMap = storPoolDfnMapRef;
        clientComSerializer = clientComSerializerRef;
        ctrlStltSerializer = ctrlStltSerializerRef;
        ctrlSatelliteUpdater = ctrlSatelliteUpdaterRef;
        responseConverter = responseConverterRef;
        peer = peerRef;
        peerAccCtx = peerAccCtxRef;
    }

    public ApiCallRc createStorPool(
        String nodeNameStr,
        String storPoolNameStr,
        String driver,
        Map<String, String> storPoolPropsMap
    )
    {
        ApiCallRcImpl responses = new ApiCallRcImpl();
        ResponseContext context = makeStorPoolContext(
            peer.get(),
            ApiOperation.makeRegisterOperation(),
            nodeNameStr,
            storPoolNameStr
        );

        try
        {
            // as the storage pool definition is implicitly created if it doesn't exist
            // we always will update the storPoolDfnMap even if not necessary
            // Therefore we need to be able to modify apiCtrlAccessors.storPoolDfnMap
            requireStorPoolDfnMapChangeAccess();

            StorPoolData storPool = createStorPool(nodeNameStr, storPoolNameStr, driver);
            ctrlPropsHelper.fillProperties(
                LinStorObject.STORAGEPOOL, storPoolPropsMap, getProps(storPool), ApiConsts.FAIL_ACC_DENIED_STOR_POOL);

            ctrlTransactionHelper.commit();

            updateStorPoolDfnMap(storPool);
            responseConverter.addWithDetail(
                responses, context, ctrlSatelliteUpdater.updateSatellite(storPool));

            responseConverter.addWithOp(responses, context,
                ApiSuccessUtils.defaultRegisteredEntry(storPool.getUuid(), getStorPoolDescriptionInline(storPool)));
        }
        catch (Exception | ImplementationError exc)
        {
            responses = responseConverter.reportException(peer.get(), context, exc);
        }

        return responses;
    }

    public ApiCallRc modifyStorPool(
        UUID storPoolUuid,
        String nodeNameStr,
        String storPoolNameStr,
        Map<String, String> overrideProps,
        Set<String> deletePropKeys
    )
    {
        ApiCallRcImpl responses = new ApiCallRcImpl();
        ResponseContext context = makeStorPoolContext(
            peer.get(),
            ApiOperation.makeModifyOperation(),
            nodeNameStr,
            storPoolNameStr
        );

        try
        {
            StorPoolData storPool = loadStorPool(nodeNameStr, storPoolNameStr, true);

            if (storPoolUuid != null && !storPoolUuid.equals(storPool.getUuid()))
            {
                throw new ApiRcException(ApiCallRcImpl.simpleEntry(
                    ApiConsts.FAIL_UUID_STOR_POOL,
                    "UUID-check failed"
                ));
            }

            Props props = getProps(storPool);
            Map<String, String> propsMap = props.map();

            ctrlPropsHelper.fillProperties(
                LinStorObject.STORAGEPOOL, overrideProps, getProps(storPool), ApiConsts.FAIL_ACC_DENIED_STOR_POOL);

            for (String delKey : deletePropKeys)
            {
                propsMap.remove(delKey);
            }

            ctrlTransactionHelper.commit();

            responseConverter.addWithOp(responses, context, ApiSuccessUtils.defaultModifiedEntry(
                storPool.getUuid(), getStorPoolDescriptionInline(storPool)));
            responseConverter.addWithDetail(
                responses, context, ctrlSatelliteUpdater.updateSatellite(storPool));
        }
        catch (Exception | ImplementationError exc)
        {
            responses = responseConverter.reportException(peer.get(), context, exc);
        }

        return responses;
    }

    public ApiCallRc deleteStorPool(
        String nodeNameStr,
        String storPoolNameStr
    )
    {
        ApiCallRcImpl responses = new ApiCallRcImpl();
        ResponseContext context = makeStorPoolContext(
            peer.get(),
            ApiOperation.makeDeleteOperation(),
            nodeNameStr,
            storPoolNameStr
        );

        try
        {
            StorPoolData storPool = loadStorPool(nodeNameStr, storPoolNameStr, false);

            if (storPool == null)
            {
                throw new ApiRcException(ApiCallRcImpl
                    .entryBuilder(
                        ApiConsts.WARN_NOT_FOUND,
                        "Deletion of " + getStorPoolDescriptionInline(nodeNameStr, storPoolNameStr) + " had no effect."
                    )
                    .setCause(firstLetterCaps(getStorPoolDescriptionInline(nodeNameStr, storPoolNameStr)) +
                        " does not exist.")
                    .build()
                );
            }
            Collection<Volume> volumes = getVolumes(storPool);
            if (!volumes.isEmpty())
            {
                StringBuilder volListSb = new StringBuilder();
                for (Volume vol : volumes)
                {
                    volListSb.append("\n   Node name: '")
                             .append(vol.getResource().getAssignedNode().getName().displayValue)
                             .append("', resource name: '")
                             .append(vol.getResource().getDefinition().getName().displayValue)
                             .append("', volume number: ")
                             .append(vol.getVolumeDefinition().getVolumeNumber().value);
                }

                String correction = volumes.size() == 1 ?
                    "Delete the listed volume first." :
                    "Delete the listed volumes first.";
                throw new ApiRcException(ApiCallRcImpl
                    .entryBuilder(
                        ApiConsts.FAIL_IN_USE,
                        String.format(
                                "The specified storage pool '%s' on node '%s' can not be deleted as " +
                                    "volumes are still using it.",
                                storPoolNameStr,
                                nodeNameStr
                            )
                    )
                    .setDetails("Volumes that are still using the storage pool: " + volListSb.toString())
                    .setCorrection(correction)
                    .build()
                );
            }
            else
            {
                UUID storPoolUuid = storPool.getUuid(); // cache storpool uuid to avoid access deleted storpool
                delete(storPool);
                ctrlTransactionHelper.commit();

                responseConverter.addWithOp(responses, context, ApiSuccessUtils.defaultDeletedEntry(
                    storPoolUuid, getStorPoolDescription(nodeNameStr, storPoolNameStr)));
            }
        }
        catch (Exception | ImplementationError exc)
        {
            responses = responseConverter.reportException(peer.get(), context, exc);
        }

        return responses;
    }

    public void respondStorPool(int msgId, UUID storPoolUuid, String storPoolNameStr)
    {
        try
        {
            StorPoolName storPoolName = new StorPoolName(storPoolNameStr);

            Peer currentPeer = peer.get();
            StorPool storPool = currentPeer.getNode().getStorPool(apiCtx, storPoolName);
            // TODO: check if the storPool has the same uuid as storPoolUuid
            if (storPool != null)
            {
                long fullSyncTimestamp = currentPeer.getFullSyncId();
                long updateId = currentPeer.getNextSerializerId();
                currentPeer.sendMessage(
                    ctrlStltSerializer
                        .builder(InternalApiConsts.API_APPLY_STOR_POOL, msgId)
                        .storPoolData(storPool, fullSyncTimestamp, updateId)
                        .build()
                );
            }
            else
            {
                long fullSyncTimestamp = currentPeer.getFullSyncId();
                long updateId = currentPeer.getNextSerializerId();
                currentPeer.sendMessage(
                    ctrlStltSerializer
                        .builder(InternalApiConsts.API_APPLY_STOR_POOL_DELETED, msgId)
                        .deletedStorPoolData(storPoolNameStr, fullSyncTimestamp, updateId)
                        .build()
                );
            }
        }
        catch (InvalidNameException invalidNameExc)
        {
            errorReporter.reportError(
                new ImplementationError(
                    "Satellite requested data for invalid storpool name '" + storPoolNameStr + "'.",
                    invalidNameExc
                )
            );
        }
        catch (AccessDeniedException accDeniedExc)
        {
            errorReporter.reportError(
                new ImplementationError(
                    "Controller's api context has not enough privileges to gather requested storpool data.",
                    accDeniedExc
                )
            );
        }
    }

    byte[] listStorPools(int msgId, List<String> filterNodes, List<String> filterStorPools)
    {
        ArrayList<StorPool.StorPoolApi> storPools = new ArrayList<>();
        try
        {
            nodesMapProt.requireAccess(peerAccCtx.get(), AccessType.VIEW);
            storPoolDfnMapProt.requireAccess(peerAccCtx.get(), AccessType.VIEW);
            final List<String> upperFilterStorPools =
                filterStorPools.stream().map(String::toUpperCase).collect(toList());
            final List<String> upperFilterNodes =
                filterNodes.stream().map(String::toUpperCase).collect(toList());
            storPoolDfnMap.values().stream()
                .filter(storPoolDfn -> upperFilterStorPools.isEmpty() ||
                    upperFilterStorPools.contains(storPoolDfn.getName().value))
                .forEach(storPoolDfn ->
                {
                    try
                    {
                        for (StorPool storPool : storPoolDfn.streamStorPools(peerAccCtx.get())
                                .filter(storPool -> upperFilterNodes.isEmpty() ||
                                    upperFilterNodes.contains(storPool.getNode().getName().value))
                                .collect(toList()))
                        {
                            if (!storPool.getName().getDisplayName().equals(LinStor.DISKLESS_STOR_POOL_NAME))
                            {
                                storPools.add(storPool.getApiData(peerAccCtx.get(), null, null));
                            }
                            // fullSyncId and updateId null, as they are not going to be serialized anyways
                        }
                    }
                    catch (AccessDeniedException accDeniedExc)
                    {
                        // don't add storpooldfn without access
                    }
                }
                );
        }
        catch (AccessDeniedException accDeniedExc)
        {
            // for now return an empty list.
        }

        return clientComSerializer
            .builder(ApiConsts.API_LST_STOR_POOL, msgId)
            .storPoolList(storPools)
            .build();
    }

    void updateRealFreeSpace(FreeSpacePojo[] freeSpacePojos)
    {
        if (!peer.get().getNode().isDeleted())
        {
            String nodeName = peer.get().getNode().getName().displayValue;

            try
            {
                for (FreeSpacePojo freeSpacePojo : freeSpacePojos)
                {
                    ResponseContext context = makeStorPoolContext(
                        peer.get(),
                        ApiOperation.makeModifyOperation(),
                        peer.get().getNode().getName().displayValue,
                        freeSpacePojo.getStorPoolName()
                    );

                    try
                    {
                        StorPoolData storPool = loadStorPool(nodeName, freeSpacePojo.getStorPoolName(), true);
                        if (storPool.getUuid().equals(freeSpacePojo.getStorPoolUuid()))
                        {
                            setRealFreeSpace(storPool, freeSpacePojo.getFreeSpace());
                        }
                        else
                        {
                            throw new ApiRcException(ApiCallRcImpl.simpleEntry(
                                ApiConsts.FAIL_UUID_STOR_POOL,
                                "UUIDs mismatched when updating free space of " + getStorPoolDescriptionInline(storPool)
                            ));
                        }
                    }
                    catch (Exception | ImplementationError exc)
                    {
                        // Add context to exception
                        throw new ApiRcException(responseConverter.exceptionToResponse(exc, context), exc, true);
                    }
                }

                ctrlTransactionHelper.commit();
            }
            catch (ApiRcException exc)
            {
                ApiCallRc.RcEntry entry = exc.getRcEntry();
                errorReporter.reportError(
                    exc instanceof ApiException && exc.getCause() != null ? exc.getCause() : exc,
                    peerAccCtx.get(),
                    peer.get(),
                    entry.getMessage()
                );
            }
        }
        // else: the node is deleted, thus if it still has any storpools left, those will
        // soon be deleted as well.
    }

    private void setRealFreeSpace(StorPoolData storPool, long freeSpace)
    {
        try
        {
            storPool.setRealFreeSpace(peerAccCtx.get(), freeSpace);
        }
        catch (AccessDeniedException | SQLException exc)
        {
            throw new ImplementationError(exc);
        }
    }

    private void requireStorPoolDfnMapChangeAccess()
    {
        try
        {
            storPoolDfnMapProt.requireAccess(
                peerAccCtx.get(),
                AccessType.CHANGE
            );
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw new ApiAccessDeniedException(
                accDeniedExc,
                "change any storage pools",
                ApiConsts.FAIL_ACC_DENIED_STOR_POOL_DFN
            );
        }
    }

    private StorPoolData createStorPool(String nodeNameStr, String storPoolNameStr, String driver)
    {
        NodeData node = ctrlApiDataLoader.loadNode(nodeNameStr, true);
        StorPoolDefinitionData storPoolDef = ctrlApiDataLoader.loadStorPoolDfn(storPoolNameStr, false);

        StorPoolData storPool;
        try
        {
            if (storPoolDef == null)
            {
                // implicitly create storage pool definition if it doesn't exist
                storPoolDef = storPoolDefinitionDataFactory.create(
                    peerAccCtx.get(),
                    LinstorParsingUtils.asStorPoolName(storPoolNameStr)
                );
            }

            storPool = storPoolDataFactory.create(
                peerAccCtx.get(),
                node,
                storPoolDef,
                driver
            );
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw new ApiAccessDeniedException(
                accDeniedExc,
                "register " + getStorPoolDescriptionInline(nodeNameStr, storPoolNameStr),
                ApiConsts.FAIL_ACC_DENIED_STOR_POOL
            );
        }
        catch (LinStorDataAlreadyExistsException alreadyExistsExc)
        {
            throw new ApiRcException(ApiCallRcImpl
                .entryBuilder(
                    ApiConsts.FAIL_EXISTS_STOR_POOL,
                    getStorPoolDescription(nodeNameStr, storPoolNameStr) + " already exists."
                )
                .build(),
                alreadyExistsExc
            );
        }
        catch (SQLException sqlExc)
        {
            throw new ApiSQLException(sqlExc);
        }
        return storPool;
    }

    private void updateStorPoolDfnMap(StorPoolData storPool)
    {
        try
        {
            storPoolDfnMap.put(
                storPool.getName(),
                storPool.getDefinition(apiCtx)
            );
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw new ImplementationError(accDeniedExc);
        }
    }

    private Collection<Volume> getVolumes(StorPoolData storPool)
    {
        Collection<Volume> volumes;
        try
        {
            volumes = storPool.getVolumes(peerAccCtx.get());
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw new ApiAccessDeniedException(
                accDeniedExc,
                "access the volumes of " + getStorPoolDescriptionInline(storPool),
                ApiConsts.FAIL_ACC_DENIED_STOR_POOL
            );
        }
        return volumes;
    }

    private void delete(StorPoolData storPool)
    {
        try
        {
            storPool.delete(peerAccCtx.get());
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw new ApiAccessDeniedException(
                accDeniedExc,
                "delete " + getStorPoolDescriptionInline(storPool),
                ApiConsts.FAIL_ACC_DENIED_STOR_POOL
            );
        }
        catch (SQLException sqlExc)
        {
            throw new ApiSQLException(sqlExc);
        }
    }

    public static String getStorPoolDescription(String nodeNameStr, String storPoolNameStr)
    {
        return "Node: " + nodeNameStr + ", Storage pool name: " + storPoolNameStr;
    }

    public static String getStorPoolDescriptionInline(StorPool storPool)
    {
        return getStorPoolDescriptionInline(
            storPool.getNode().getName().displayValue,
            storPool.getName().displayValue
        );
    }

    public static String getStorPoolDescriptionInline(NodeData node, StorPoolDefinitionData storPoolDfn)
    {
        return getStorPoolDescriptionInline(
            node.getName().displayValue,
            storPoolDfn.getName().displayValue
        );
    }

    public static String getStorPoolDescriptionInline(String nodeNameStr, String storPoolNameStr)
    {
        return "storage pool '" + storPoolNameStr + "' on node '" + nodeNameStr + "'";
    }

    private StorPoolData loadStorPool(String nodeNameStr, String storPoolNameStr, boolean failIfNull)
    {
        return ctrlApiDataLoader.loadStorPool(
            ctrlApiDataLoader.loadStorPoolDfn(storPoolNameStr, true),
            ctrlApiDataLoader.loadNode(nodeNameStr, true),
            failIfNull
        );
    }

    private Props getProps(StorPoolData storPool)
    {
        Props props;
        try
        {
            props = storPool.getProps(peerAccCtx.get());
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw new ApiAccessDeniedException(
                accDeniedExc,
                "access properties of storage pool '" + storPool.getName().displayValue +
                    "' on node '" + storPool.getNode().getName().displayValue + "'",
                ApiConsts.FAIL_ACC_DENIED_STOR_POOL
            );
        }
        return props;
    }

    private static ResponseContext makeStorPoolContext(
        Peer peer,
        ApiOperation operation,
        String nodeNameStr,
        String storPoolNameStr
    )
    {
        Map<String, String> objRefs = new TreeMap<>();
        objRefs.put(ApiConsts.KEY_NODE, nodeNameStr);
        objRefs.put(ApiConsts.KEY_STOR_POOL_DFN, storPoolNameStr);

        return new ResponseContext(
            peer,
            operation,
            getStorPoolDescription(nodeNameStr, storPoolNameStr),
            getStorPoolDescriptionInline(nodeNameStr, storPoolNameStr),
            ApiConsts.MASK_STOR_POOL,
            objRefs
        );
    }
}