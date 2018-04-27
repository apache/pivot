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

/**
 * Movie listener interface.
 */
public interface MovieListener {
    /**
     * Movie listener list.
     */
    public static class Listeners extends ListenerList<MovieListener> implements MovieListener {
        @Override
        public void sizeChanged(Movie movie, int previousWidth, int previousHeight) {
            forEach(listener -> listener.sizeChanged(movie, previousWidth, previousHeight));
        }

        @Override
        public void baselineChanged(Movie movie, int previousBaseline) {
            forEach(listener -> listener.baselineChanged(movie, previousBaseline));
        }

        @Override
        public void currentFrameChanged(Movie movie, int previousFrame) {
            forEach(listener -> listener.currentFrameChanged(movie, previousFrame));
        }

        @Override
        public void loopingChanged(Movie movie) {
            forEach(listener -> listener.loopingChanged(movie));
        }

        @Override
        public void movieStarted(Movie movie) {
            forEach(listener -> listener.movieStarted(movie));
        }

        @Override
        public void movieStopped(Movie movie) {
            forEach(listener -> listener.movieStopped(movie));
        }

        @Override
        public void regionUpdated(Movie movie, int x, int y, int width, int height) {
            forEach(listener -> listener.regionUpdated(movie, x, y, width, height));
        }
    }

    /**
     * Movie listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements MovieListener {
        @Override
        public void sizeChanged(Movie movie, int previousWidth, int previousHeight) {
            // empty block
        }

        @Override
        public void baselineChanged(Movie movie, int previousBaseline) {
            // empty block
        }

        @Override
        public void currentFrameChanged(Movie movie, int previousFrame) {
            // empty block
        }

        @Override
        public void loopingChanged(Movie movie) {
            // empty block
        }

        @Override
        public void movieStarted(Movie movie) {
            // empty block
        }

        @Override
        public void movieStopped(Movie movie) {
            // empty block
        }

        @Override
        public void regionUpdated(Movie movie, int x, int y, int width, int height) {
            // empty block
        }
    }

    /**
     * Called when a movie's size has changed.
     *
     * @param movie          The movie that has been resized.
     * @param previousWidth  The previous width of this movie.
     * @param previousHeight The previous value of the height before resizing.
     */
    default void sizeChanged(Movie movie, int previousWidth, int previousHeight) {
    }

    /**
     * Called when a movie's baseline has changed.
     *
     * @param movie            The movie that has changed.
     * @param previousBaseline The previous baseline value of the movie.
     */
    default void baselineChanged(Movie movie, int previousBaseline) {
    }

    /**
     * Called when the movie's current frame changed.
     *
     * @param movie         The movie that has changed.
     * @param previousFrame The previous frame index of the movie.
     */
    default void currentFrameChanged(Movie movie, int previousFrame) {
    }

    /**
     * Called when the movie's looping property changed.
     *
     * @param movie The source of this event.
     */
    default void loopingChanged(Movie movie) {
    }

    /**
     * Called when the movie begins playing. The frame at which the movie is
     * starting can be obtained via <tt>getCurrentFrame()</tt> (it is not
     * guaranteed to be positioned before the first frame when it is started).
     *
     * @param movie The movie that has just started to play.
     */
    default void movieStarted(Movie movie) {
    }

    /**
     * Called when the movie stops playing. The frame at which the movie stopped
     * can be obtained via <tt>getCurrentFrame()</tt> (it is not guaranteed to
     * have completed the last frame when it is stopped).
     *
     * @param movie The movie that has just stopped playing.
     */
    default void movieStopped(Movie movie) {
    }

    /**
     * Called when a region within a movie needs to be repainted.
     *
     * @param movie  The movie that needs repainting.
     * @param x      The upper left X-position of the region to repaint.
     * @param y      The upper left Y-position for the repaint.
     * @param width  The width of the image to repaint.
     * @param height The height to repaint.
     */
    default void regionUpdated(Movie movie, int x, int y, int width, int height) {
    }
}
