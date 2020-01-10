package com.zman.thread.eventloop.impl;

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class ScheduledFutureTask<V>
        extends FutureTask<V> implements RunnableFuture<V>, ScheduledFuture<V> {

    private static long SEQUENCER;

    /** The time the task is enabled to execute in nanoTime units */
    private long time;

    /** Sequence number to break ties FIFO */
    private long sequenceNumber;


    /**
     * Creates a one-shot action with given nanoTime-based trigger time.
     */
    ScheduledFutureTask(Callable<V> callable, long time, TimeUnit timeUnit) {
        super(callable);
        this.time = System.nanoTime() + timeUnit.toNanos(time);
        sequenceNumber = SEQUENCER++;
    }


    public long getDelay(TimeUnit unit) {
        return unit.convert(time - System.nanoTime(), NANOSECONDS);
    }


    public int compareTo(Delayed other) {
        if (other == this) // compare zero if same object
            return 0;
        if (other instanceof ScheduledFutureTask) {
            ScheduledFutureTask<?> x = (ScheduledFutureTask<?>)other;
            long diff = time - x.time;
            if (diff < 0)
                return -1;
            else if (diff > 0)
                return 1;
            else if (sequenceNumber < x.sequenceNumber)
                return -1;
            else
                return 1;
        }else{
            throw new IllegalArgumentException("The parameter is not ScheduledFutureTask");
        }
    }

}