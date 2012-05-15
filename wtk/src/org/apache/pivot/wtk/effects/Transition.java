/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.wtk.effects;

import org.apache.pivot.wtk.ApplicationContext;

/**
 * Abstract base class for "transitions", which are animated application
 * effects.
 */
public abstract class Transition {
    private int duration;
    private int rate;
    private boolean repeating;

    private boolean reversed = false;

    private TransitionListener transitionListener;

    private long startTime = 0;
    private long currentTime = 0;
    private ApplicationContext.ScheduledCallback transitionCallback = null;

    private final Runnable updateCallback = new Runnable() {
        @Override
        public void run() {
            currentTime = System.currentTimeMillis();

            long endTime = startTime + duration;
            if (currentTime >= endTime) {
                if (repeating) {
                    startTime = endTime;
                } else {
                    currentTime = endTime;
                    stop();

                    if (transitionListener != null) {
                        transitionListener.transitionCompleted(Transition.this);
                    }
                }
            }

            update();
        }
    };

    /**
     * Creates a new non-repeating transition with the given duration, rate.
     *
     * @param duration
     * Transition duration, in milliseconds.
     *
     * @param rate
     * Transition rate, in frames per second.
     */
    public Transition(int duration, int rate) {
        this(duration, rate, false);
    }

    /**
     * Creates a new transition with the given duration, rate, and repeat.
     *
     * @param duration
     * Transition duration, in milliseconds.
     *
     * @param rate
     * Transition rate, in frames per second.
     *
     * @param repeating
     * <tt>true</tt> if the transition should repeat; <tt>false</tt>, otherwise.
     */
    public Transition(int duration, int rate, boolean repeating) {
        this(duration, rate, repeating, false);
    }

    /**
     * Creates a new transition with the given duration, rate, and repeat.
     *
     * @param duration
     * Transition duration, in milliseconds.
     *
     * @param rate
     * Transition rate, in frames per second.
     *
     * @param repeating
     * <tt>true</tt> if the transition should repeat; <tt>false</tt>, otherwise.
     *
     * @param reversed
     * <tt>true</tt> if the transition should run in reverse; <tt>false</tt>
     * otherwise.
     */
    public Transition(int duration, int rate, boolean repeating, boolean reversed) {
        if (duration <= 0) {
            throw new IllegalArgumentException("duration must be positive.");
        }

        this.duration = duration;
        this.rate = rate;
        this.repeating = repeating;
        this.reversed = reversed;
    }

    /**
     * Returns the transition duration.
     *
     * @return
     * The duration of the transition, in milliseconds.
     *
     * @see #setDuration(int)
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Sets the transition duration, the length of time the transition is
     * scheduled to run.
     *
     * @param duration
     * The duration of the transition, in milliseconds.
     */
    public void setDuration(int duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("duration is negative.");
        }

        if (transitionCallback != null) {
            throw new IllegalStateException("Transition is currently running.");
        }

        this.duration = duration;
    }

    /**
     * Returns the transition rate.
     *
     * @return
     * The rate of the transition, in frames per second.
     *
     * @see #setRate(int)
     */
    public int getRate() {
        return rate;
    }

    /**
     * Sets the transition rate, the number of times the transition will be
     * updated within the span of one second.
     *
     * @param rate
     * The transition rate, in frames per second.
     */
    public void setRate(int rate) {
        if (rate < 0) {
            throw new IllegalArgumentException("rate is negative.");
        }

        if (transitionCallback != null) {
            throw new IllegalStateException("Transition is currently running.");
        }

        this.rate = rate;
    }

    /**
     * Returns the transition interval, the number of milliseconds between
     * updates.
     *
     * @return
     * The transition interval, in milliseconds.
     */
    public int getInterval() {
        return (int)((1f / rate) * 1000);
    }

    /**
     * Returns the time at which the transition was started.
     *
     * @return
     * The transition's start time.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Returns the last time the transition was updated.
     *
     * @return
     * The most recent update time.
     */
    public long getCurrentTime() {
        return currentTime;
    }

    /**
     * Returns the elapsed time since the transition started.
     *
     * @return
     * Returns the amount of time that has passed since the transition
     * was started. If the transition is reversed, this value reflects the
     * amount of time remaining.
     */
    public int getElapsedTime() {
        long endTime = startTime + duration;

        int elapsedTime;
        if (reversed) {
            elapsedTime = (int)(endTime - currentTime);
        } else {
            elapsedTime = (int)(currentTime - startTime);
        }

        return elapsedTime;
    }

    /**
     * Returns the percentage of the transition that has completed.
     *
     * @return
     * A value between 0 and 1, inclusive, representing the transition's
     * percent complete. If the transition is reversed, this value reflects
     * the percent remaining.
     */
    public float getPercentComplete() {
        float percentComplete = (float)(currentTime - startTime) / (float)(duration);

        if (reversed) {
            percentComplete = 1.0f - percentComplete;
        }

        return percentComplete;
    }

    /**
     * Tells whether or not the transition is currently running.
     *
     * @return
     * <tt>true</tt> if the transition is currently running; <tt>false</tt> if
     * it is not
     */
    public boolean isRunning() {
        return (transitionCallback != null);
    }

    /**
     * Starts the transition with no listener.
     *
     * @see #start(TransitionListener)
     */
    public final void start() {
        start(null);
    }

    /**
     * Starts the transition. Calls {@link #update()} to establish the
     * initial state and starts a timer that will repeatedly call
     * {@link #update()} at the current rate. The specified
     * <tt>TransitionListener</tt> will be notified when the transition
     * completes.
     *
     * @param transitionListenerArgument
     * The listener to get notified when the transition completes, or
     * <tt>null</tt> if no notification is necessary
     */
    public void start(TransitionListener transitionListenerArgument) {
        if (transitionCallback != null) {
            throw new IllegalStateException("Transition is currently running.");
        }

        this.transitionListener = transitionListenerArgument;

        startTime = System.currentTimeMillis();
        currentTime = startTime;

        transitionCallback = ApplicationContext.scheduleRecurringCallback(updateCallback,
            getInterval());

        update();
    }

    /**
     * Stops the transition. Does not fire a
     * {@link TransitionListener#transitionCompleted(Transition)} event.
     */
    public void stop() {
        if (transitionCallback != null) {
            transitionCallback.cancel();
        }

        transitionCallback = null;
    }

    /**
     * "Fast-forwards" to the end of the transition and fires a
     * {@link TransitionListener#transitionCompleted(Transition)} event.
     */
    public void end() {
        if (transitionCallback != null) {
            currentTime = startTime + duration;
            stop();
            update();
            transitionListener.transitionCompleted(this);
        }
    }

    /**
     * Called repeatedly while the transition is running to update the
     * transition's state.
     */
    protected abstract void update();

    public boolean isRepeating() {
        return repeating;
    }

    /**
     * Tests whether the transition is reversed.
     *
     * @return
     * <tt>true</tt> if the transition is reversed; <tt>false</tt>, otherwise.
     */
    public boolean isReversed() {
        return reversed;
    }

    /**
     * Sets the transition's reversed flag.
     *
     * @param reversed
     */
    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    /**
     * Reverses the transition. If the transition is running, updates the start
     * time so the reverse duration is the same as the current elapsed time.
     */
    public void reverse() {
        if (isRunning()) {
            long repeatDuration = currentTime - startTime;
            long endTime = currentTime + repeatDuration;
            startTime = endTime - duration;
        }

        setReversed(!isReversed());
    }
}
