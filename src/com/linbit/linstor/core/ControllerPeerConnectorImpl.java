package com.linbit.linstor.core;

import com.linbit.ImplementationError;
import com.linbit.InvalidNameException;
import com.linbit.SatelliteTransactionMgr;
import com.linbit.linstor.Node;
import com.linbit.linstor.NodeData;
import com.linbit.linstor.NodeName;
import com.linbit.linstor.StorPoolDefinitionData;
import com.linbit.linstor.StorPoolName;
import com.linbit.linstor.annotation.SystemContext;
import com.linbit.linstor.logging.ErrorReporter;
import com.linbit.linstor.netcom.Peer;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.security.Privilege;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;

@Singleton
public class ControllerPeerConnectorImpl implements ControllerPeerConnector
{
    private final CoreModule.NodesMap nodesMap;
    private final CoreModule.ResourceDefinitionMap rscDfnMap;
    private final CoreModule.StorPoolDefinitionMap storPoolDfnMap;

    private final ReadWriteLock reconfigurationLock;
    private final ReadWriteLock nodesMapLock;
    private final ReadWriteLock rscDfnMapLock;
    private final ReadWriteLock storPoolDfnMapLock;

    private final ErrorReporter errorReporter;

    private final AccessContext sysCtx;

    // Local NodeName received from the currently active controller
    private NodeName localNodeName;

    // The currently connected controller peer (can be null)
    private Peer controllerPeer;

    @Inject
    public ControllerPeerConnectorImpl(
        CoreModule.NodesMap nodesMapRef,
        CoreModule.ResourceDefinitionMap rscDfnMapRef,
        CoreModule.StorPoolDefinitionMap storPoolDfnMapRef,
        @Named(CoreModule.RECONFIGURATION_LOCK)  ReadWriteLock reconfigurationLockRef,
        @Named(CoreModule.NODES_MAP_LOCK) ReadWriteLock nodesMapLockRef,
        @Named(CoreModule.RSC_DFN_MAP_LOCK) ReadWriteLock rscDfnMapLockRef,
        @Named(CoreModule.STOR_POOL_DFN_MAP_LOCK) ReadWriteLock storPoolDfnMapLockRef,
        ErrorReporter errorReporterRef,
        @SystemContext AccessContext sysCtxRef
    )
    {
        nodesMap = nodesMapRef;
        rscDfnMap = rscDfnMapRef;
        storPoolDfnMap = storPoolDfnMapRef;
        reconfigurationLock = reconfigurationLockRef;
        nodesMapLock = nodesMapLockRef;
        rscDfnMapLock = rscDfnMapLockRef;
        storPoolDfnMapLock = storPoolDfnMapLockRef;
        errorReporter = errorReporterRef;
        sysCtx = sysCtxRef;
    }

    @Override
    public NodeData getLocalNode()
    {
        return (NodeData) nodesMap.get(localNodeName);
    }

    @Override
    public Peer getControllerPeer()
    {
        return controllerPeer;
    }

    @Override
    public void setControllerPeer(
        Peer controllerPeerRef,
        UUID nodeUuid,
        String nodeName,
        UUID disklessStorPoolDfnUuid,
        UUID disklessStorPoolUuid
    )
    {
        try
        {
            reconfigurationLock.writeLock().lock();
            nodesMapLock.writeLock().lock();
            rscDfnMapLock.writeLock().lock();
            storPoolDfnMapLock.writeLock().lock();

            controllerPeer = controllerPeerRef;

            AccessContext tmpCtx = sysCtx.clone();
            tmpCtx.getEffectivePrivs().enablePrivileges(Privilege.PRIV_SYS_ALL);

            SatelliteTransactionMgr transMgr = new SatelliteTransactionMgr();
            NodeData localNode;
            try
            {
                LinStor.disklessStorPoolDfn = StorPoolDefinitionData.getInstanceSatellite(
                    tmpCtx,
                    disklessStorPoolDfnUuid,
                    new StorPoolName(LinStor.DISKLESS_STOR_POOL_NAME),
                    transMgr
                );

                localNodeName = new NodeName(nodeName);

                localNode = NodeData.getInstanceSatellite(
                    sysCtx,
                    nodeUuid,
                    localNodeName,
                    Node.NodeType.SATELLITE,
                    new Node.NodeFlag[] {},
                    disklessStorPoolUuid,
                    transMgr
                );
                transMgr.commit();

                nodesMap.clear();
                rscDfnMap.clear();
                storPoolDfnMap.clear();
                // TODO: make sure everything is cleared

                nodesMap.put(localNode.getName(), localNode);
                storPoolDfnMap.put(LinStor.disklessStorPoolDfn.getName(), LinStor.disklessStorPoolDfn);
                setControllerPeerToCurrentLocalNode();
            }
            catch (ImplementationError | SQLException | InvalidNameException exc)
            {
                errorReporter.reportError(exc);
            }
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw new ImplementationError(
                "sysCtx does not have enough privileges to call node.setPeer",
                accDeniedExc
            );
        }
        finally
        {
            storPoolDfnMapLock.writeLock().unlock();
            rscDfnMapLock.writeLock().unlock();
            nodesMapLock.writeLock().unlock();
            reconfigurationLock.writeLock().unlock();
        }
    }

    @Override
    public void setControllerPeerToCurrentLocalNode()
    {
        reconfigurationLock.readLock().lock();
        nodesMapLock.readLock().lock();
        try
        {
            nodesMap.get(localNodeName).setPeer(sysCtx, controllerPeer);
        }
        catch (AccessDeniedException exc)
        {
            errorReporter.reportError(exc);
        }
        finally
        {
            nodesMapLock.readLock().unlock();
            reconfigurationLock.readLock().unlock();
        }
    }
}