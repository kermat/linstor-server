package com.linbit.linstor.storage2.layer.kinds;

public class DefaultLayerKind implements DeviceLayerKind
{
    @Override
    public boolean isSnapshotSupported()
    {
        return false;
    }

    @Override
    public boolean isResizeSupported()
    {
        return true;
    }

    @Override
    public StartupVerifications[] requiredVerifications()
    {
        return new StartupVerifications[] {
        };
    }
}