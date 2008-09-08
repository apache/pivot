package pivot.wtk.effects;

import pivot.wtk.ApplicationContext;

public abstract class Transition {
    private int duration;
    private int rate;
    private boolean repeat;

    private long startTime = 0;
    private long currentTime = 0;
    private int intervalID = -1;

    public static final int DEFAULT_DURATION = 0;
    public static final int DEFAULT_RATE = 15;

    private final Runnable updateCallback = new Runnable() {
        public void run() {
            currentTime = System.currentTimeMillis();

            long endTime = startTime + duration;
            if (currentTime >= endTime) {
                if (repeat) {
                    startTime = endTime;
                } else {
                    currentTime = endTime;
                    stop();
                }
            }

            update();
        }
    };

    /**
     * Creates a new, non-repeating transition with the default duration
     * and rate.
     */
    public Transition() {
        this(DEFAULT_DURATION, DEFAULT_RATE, false);
    }

    /**
     * Creates a new transition with the given duration, rate, and repeat.
     *
     * @param duration
     * Transition duration, in milliseconds.
     *
     * @param rate
     * Transition rate, in frames per second.
     */
    public Transition(int duration, int rate, boolean repeat) {
        this.duration = duration;
        this.rate = rate;
        this.repeat = repeat;
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

        if (intervalID != -1) {
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

        if (intervalID != -1) {
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
        return (int)((1f / (float)rate) * 1000);
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
     * Returns the percentage of the transition that has completed.
     *
     * @return
     * A value between 0 and 1, inclusive, representing the transition's
     * percent complete.
     */
    public float getPercentComplete() {
        return (float)(currentTime - startTime) / (float)(startTime + duration);
    }

    /**
     * Starts the transition. Calls {@link #update()} to establish the initial
     * state and starts a timer that will repeatedly call {@link #update()} at
     * the current rate.
     */
    public void start() {
        if (intervalID != -1) {
            throw new IllegalStateException("Transition is currently running.");
        }

        startTime = System.currentTimeMillis();
        intervalID = ApplicationContext.setInterval(updateCallback, getInterval());

        update();
    }

    /**
     * Stops the transition.
     */
    public void stop() {
        if (intervalID != -1) {
            ApplicationContext.clearInterval(intervalID);
            intervalID = -1;
        }
    }

    /**
     * Called repeatedly while the transition is running to update the
     * transition's state.
     */
    protected abstract void update();
}
