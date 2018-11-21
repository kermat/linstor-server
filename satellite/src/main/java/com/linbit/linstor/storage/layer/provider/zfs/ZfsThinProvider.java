package com.linbit.linstor.storage.layer.provider.zfs;

import com.linbit.ImplementationError;
import com.linbit.extproc.ExtCmdFactory;
import com.linbit.linstor.StorPool;
import com.linbit.linstor.Volume;
import com.linbit.linstor.VolumeDefinition;
import com.linbit.linstor.core.StltConfigAccessor;
import com.linbit.linstor.logging.ErrorReporter;
import com.linbit.linstor.propscon.InvalidKeyException;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.storage.StorageConstants;
import com.linbit.linstor.storage.StorageException;
import com.linbit.linstor.storage.layer.DeviceLayer.NotificationListener;
import com.linbit.linstor.storage.layer.provider.WipeHandler;
import com.linbit.linstor.storage.utils.DeviceLayerUtils;
import com.linbit.linstor.storage.utils.ZfsCommands;
import com.linbit.utils.RemoveAfterDevMgrRework;

import java.sql.SQLException;

public class ZfsThinProvider extends ZfsProvider
{
    public ZfsThinProvider(
        ErrorReporter errorReporter,
        ExtCmdFactory extCmdFactory,
        AccessContext storDriverAccCtx,
        StltConfigAccessor stltConfigAccessor,
        WipeHandler wipeHandler,
        NotificationListener notificationListener
    )
    {
        super(
            errorReporter,
            extCmdFactory,
            storDriverAccCtx,
            stltConfigAccessor,
            wipeHandler,
            notificationListener,
            "ZFS-Thin"
        );
    }

    @Override
    protected void createLvImpl(Volume vlm) throws StorageException, AccessDeniedException, SQLException
    {
        ZfsCommands.create(
            extCmdFactory.create(),
            ((ZfsLayerDataStlt) vlm.getLayerData(storDriverAccCtx)).zpool,
            asLvIdentifier(vlm),
            vlm.getVolumeDefinition().getVolumeSize(storDriverAccCtx),
            true
        );
    }

    @Override
    protected String getZPool(StorPool storPool) throws AccessDeniedException
    {
        String zPool;
        try
        {
            zPool = DeviceLayerUtils.getNamespaceStorDriver(
                storPool.getProps(storDriverAccCtx)
            ).getProp(StorageConstants.CONFIG_ZFS_THIN_POOL_KEY);
        }
        catch (InvalidKeyException exc)
        {
            throw new ImplementationError(exc);
        }
        return zPool;
    }

    @RemoveAfterDevMgrRework // this method should stay protected. Here it is made public
    // only to be accessible from LayeredSnapshotHelper
    @Override
    public String asLvIdentifier(VolumeDefinition vlmDfn)
    {
        return super.asLvIdentifier(vlmDfn);
    }
}