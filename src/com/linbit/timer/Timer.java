package com.linbit.timer;

import com.linbit.NegativeTimeException;
import com.linbit.ValueOutOfRangeException;

/**
 * Timer for scheduling Action objects
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public interface Timer<K extends Comparable<K>, V extends Action<K>>
{
    public void addDelayedAction(Long delay, V actionObj)
        throws NegativeTimeException, ValueOutOfRangeException;

    public void addScheduledAction(Long scheduledTime, V actionObj);

    public void cancelAction(K actionId);
}
