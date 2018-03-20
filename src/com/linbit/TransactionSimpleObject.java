package com.linbit;

import java.sql.SQLException;
import java.util.Objects;

public class TransactionSimpleObject<PARENT, ELEMENT> implements TransactionObject
{
    private boolean initialized = false;

    private PARENT parent;
    private ELEMENT object;
    private ELEMENT cachedObject;
    private SingleColumnDatabaseDriver<PARENT, ELEMENT> dbDriver;

    private TransactionMgr transMgr;

    public TransactionSimpleObject(
        PARENT parentRef,
        ELEMENT objRef,
        SingleColumnDatabaseDriver<PARENT, ELEMENT> driverRef
    )
    {
        parent = parentRef;
        object = objRef;
        cachedObject = objRef;
        if (driverRef == null)
        {
            dbDriver = new NoOpObjectDatabaseDriver<>();
        }
        else
        {
            dbDriver = driverRef;
        }
    }

    public ELEMENT set(ELEMENT obj) throws SQLException
    {
        if (initialized)
        {
            if (!Objects.equals(obj, cachedObject))
            {
                dbDriver.update(parent, obj, transMgr);
            }
        }
        else
        {
            cachedObject = obj;
        }
        ELEMENT oldObj = object;
        object = obj;
        return oldObj;
    }

    public ELEMENT get()
    {
        return object;
    }

    @Override
    public void initialized()
    {
        initialized = true;
    }

    @Override
    public boolean isInitialized()
    {
        return initialized;
    }

    @Override
    public void setConnection(TransactionMgr transMgrRef) throws ImplementationError
    {
        if (!hasTransMgr() && isDirtyWithoutTransMgr())
        {
            throw new ImplementationError("setConnection was called AFTER data was manipulated", null);
        }
        if (transMgrRef != null)
        {
            if (transMgrRef != transMgr)
            {
                transMgrRef.register(this);
                // forward transaction manager to simple object
                if (object instanceof TransactionObject)
                {
                    ((TransactionObject) object).setConnection(transMgrRef);
                }
            }
        }
        transMgr = transMgrRef;
    }

    @Override
    public void commit()
    {
        assert (TransactionMgr.isCalledFromTransactionMgr("commit"));
        cachedObject = object;
    }

    @Override
    public void rollback()
    {
        assert (TransactionMgr.isCalledFromTransactionMgr("rollback"));
        object = cachedObject;
    }

    @Override
    public boolean isDirty()
    {
        return object != cachedObject;
    }

    @Override
    public boolean isDirtyWithoutTransMgr()
    {
        return !hasTransMgr() && isDirty();
    }

    @Override
    public boolean hasTransMgr()
    {
        return transMgr != null;
    }
}