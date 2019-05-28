package com.linbit.linstor;

import com.linbit.ImplementationError;
import com.linbit.linstor.dbdrivers.interfaces.VolumeDataDatabaseDriver;
import com.linbit.linstor.layer.CtrlLayerDataHelper;
import com.linbit.linstor.layer.LayerPayload;
import com.linbit.linstor.propscon.PropsContainerFactory;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.security.AccessType;
import com.linbit.linstor.stateflags.StateFlagsBits;
import com.linbit.linstor.transaction.TransactionMgr;
import com.linbit.linstor.transaction.TransactionObjectFactory;

import javax.inject.Inject;
import javax.inject.Provider;

import java.sql.SQLException;
import java.util.TreeMap;
import java.util.UUID;

public class VolumeDataControllerFactory
{
    private final VolumeDataDatabaseDriver driver;
    private final PropsContainerFactory propsContainerFactory;
    private final TransactionObjectFactory transObjFactory;
    private final Provider<TransactionMgr> transMgrProvider;
    private final CtrlLayerDataHelper layerStackHelper;

    @Inject
    public VolumeDataControllerFactory(
        VolumeDataDatabaseDriver driverRef,
        PropsContainerFactory propsContainerFactoryRef,
        TransactionObjectFactory transObjFactoryRef,
        Provider<TransactionMgr> transMgrProviderRef,
        CtrlLayerDataHelper layerStackHelperRef
    )
    {
        driver = driverRef;
        propsContainerFactory = propsContainerFactoryRef;
        transObjFactory = transObjFactoryRef;
        transMgrProvider = transMgrProviderRef;
        layerStackHelper = layerStackHelperRef;
    }

    public VolumeData create(
        AccessContext accCtx,
        Resource rsc,
        VolumeDefinition vlmDfn,
        StorPool storPool,
        Volume.VlmFlags[] flags
    )
        throws SQLException, AccessDeniedException, LinStorDataAlreadyExistsException
    {
        rsc.getObjProt().requireAccess(accCtx, AccessType.USE);
        VolumeData volData = null;

        volData = (VolumeData) rsc.getVolume(vlmDfn.getVolumeNumber());

        if (volData != null)
        {
            throw new LinStorDataAlreadyExistsException("The Volume already exists");
        }

        volData = new VolumeData(
            UUID.randomUUID(),
            rsc,
            vlmDfn,
            storPool,
            StateFlagsBits.getMask(flags),
            driver,
            propsContainerFactory,
            transObjFactory,
            transMgrProvider,
            new TreeMap<>()
        );

        driver.create(volData);
        ((ResourceData) rsc).putVolume(accCtx, volData);
        storPool.putVolume(accCtx, volData);
        ((VolumeDefinitionData) vlmDfn).putVolume(accCtx, volData);

        // TODO: might be a good idea to create this object earlier
        layerStackHelper.ensureStackDataExists((ResourceData) rsc, null, new LayerPayload());

        return volData;
    }

    public VolumeData getInstanceSatellite(
        AccessContext accCtx,
        UUID vlmUuid,
        Resource rsc,
        VolumeDefinition vlmDfn,
        StorPool storPoolRef,
        Volume.VlmFlags[] flags
    )
    {
        VolumeData vlmData;
        try
        {
            vlmData = (VolumeData) rsc.getVolume(vlmDfn.getVolumeNumber());
            if (vlmData == null)
            {
                vlmData = new VolumeData(
                    vlmUuid,
                    rsc,
                    vlmDfn,
                    storPoolRef,
                    StateFlagsBits.getMask(flags),
                    driver,
                    propsContainerFactory,
                    transObjFactory,
                    transMgrProvider,
                    new TreeMap<>()
                );
                ((ResourceData) rsc).putVolume(accCtx, vlmData);
                storPoolRef.putVolume(accCtx, vlmData);
                ((VolumeDefinitionData) vlmDfn).putVolume(accCtx, vlmData);

                vlmData.setAllocatedSize(accCtx, vlmDfn.getVolumeSize(accCtx));
                // usable size depends on deviceLayer
            }
        }
        catch (Exception exc)
        {
            throw new ImplementationError(
                "This method should only be called with a satellite db in background!",
                exc
            );
        }

        return vlmData;
    }
}
