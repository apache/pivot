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
package org.apache.pivot.wtk.media;

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Visual;

/**
 * Abstract base class for movies. A movie is either a bitmapped "video" or a
 * vector "animation".
 */
public abstract class Movie implements Visual {
    private int currentFrame = -1;
    private int frameRate = 26;
    private boolean looping = false;

    private ApplicationContext.ScheduledCallback scheduledCallback = null;

    protected MovieListener.Listeners movieListeners = new MovieListener.Listeners();

    private final Runnable nextFrameCallback = new Runnable() {
        @Override
        public void run() {
            if (currentFrame == getTotalFrames() - 1) {
                if (looping) {
                    setCurrentFrame(0);
                } else {
                    stop();
                }
            } else {
                setCurrentFrame(currentFrame + 1);
            }
        }
    };

    @Override
    public int getBaseline() {
        return -1;
    }

    public Dimensions getSize() {
        return new Dimensions(getWidth(), getHeight());
    }

    public abstract int getTotalFrames();

    public int getCurrentFrame() {
        return currentFrame;
    }

    public void setCurrentFrame(int currentFrame) {
        int previousFrame = this.currentFrame;

        if (previousFrame != currentFrame) {
            this.currentFrame = currentFrame;
            movieListeners.currentFrameChanged(this, previousFrame);
        }
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public boolean isLooping() {
        return looping;
    }

    public void setLooping(boolean looping) {
        if (this.looping != looping) {
            this.looping = looping;
            movieListeners.loopingChanged(this);
        }
    }

    public void play() {
        if (scheduledCallback != null) {
            throw new IllegalStateException("Movie is already playing.");
        }

        scheduledCallback = ApplicationContext.scheduleRecurringCallback(nextFrameCallback,
            (int) ((1 / (double) frameRate) * 1000));

        movieListeners.movieStarted(this);
    }

    public void stop() {
        if (scheduledCallback != null) {
            scheduledCallback.cancel();
        }

        scheduledCallback = null;

        movieListeners.movieStopped(this);
    }

    public boolean isPlaying() {
        return (scheduledCallback != null);
    }

    public ListenerList<MovieListener> getMovieListeners() {
        return movieListeners;
    }
}
