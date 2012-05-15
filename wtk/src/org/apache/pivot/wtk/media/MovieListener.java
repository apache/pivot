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

/**
 * Movie listener interface.
 */
public interface MovieListener {
    /**
     * Movie listener adapter.
     */
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
     * @param movie
     * @param previousWidth
     * @param previousHeight
     */
    public void sizeChanged(Movie movie, int previousWidth, int previousHeight);

    /**
     * Called when a movie's baseline has changed.
     *
     * @param movie
     * @param previousBaseline
     */
    public void baselineChanged(Movie movie, int previousBaseline);

    /**
     * Called when the movie's current frame changed.
     *
     * @param movie
     * @param previousFrame
     */
    public void currentFrameChanged(Movie movie, int previousFrame);

    /**
     * Called when the movie's looping property changed.
     *
     * @param movie
     */
    public void loopingChanged(Movie movie);

    /**
     * Called when the movie begins playing. The frame at which the movie is
     * starting can be obtained via <tt>getCurrentFrame()</tt> (it is not
     * guaranteed to be positioned before the first frame when it is started).
     *
     * @param movie
     */
    public void movieStarted(Movie movie);

    /**
     * Called when the movie stops playing. The frame at which the movie
     * stopped can be obtained via <tt>getCurrentFrame()</tt> (it is not
     * guaranteed to have completed the last frame when it is stopped).
     *
     * @param movie
     */
    public void movieStopped(Movie movie);

    /**
     * Called when a region within a movie needs to be repainted.
     *
     * @param movie
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void regionUpdated(Movie movie, int x, int y, int width, int height);
}
