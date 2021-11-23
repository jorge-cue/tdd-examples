package com.example.tddexamples.scheduler;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
public abstract class Action implements Delayed, Runnable {

    protected static Clock clock = Clock.systemUTC();

    private final Instant runInstant;

    /**
     *
     * @param duration Time duration the action should wait in the queue before execute.
     */
    protected Action(Duration duration) {
        checkNotNull(duration, "required argument 'duration' is null");
        this.runInstant = clock.instant().plusMillis(duration.toMillis());
    }

    /**
     * Gets remaining time to run the Action expressed in number of units.
     * 
     * @param units Units for compute the remaining delay.
     * @return remaining delay in terms of unit.
     * @throws NullPointerException is units is null.
     */
    @Override
    public long getDelay(TimeUnit units) {
        checkNotNull(units, "required argument 'units' is null");
        return units.convert(runInstant.toEpochMilli() - clock.millis(), TimeUnit.MILLISECONDS);
    }

    /**
     *
     * @param other Must be an Action superclass.
     * @return
     */
    @Override
    public int compareTo(Delayed other) {
        checkNotNull(other, "required argument 'other' is null");
        checkArgument(other instanceof Action, "argument 'other' is not an Action");
        return this.runInstant.compareTo(((Action) other).runInstant);
    }
}
