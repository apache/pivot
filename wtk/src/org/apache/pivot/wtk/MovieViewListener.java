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
package org.apache.pivot.wtk;

import org.apache.pivot.wtk.media.Movie;
import org.apache.pivot.util.ListenerList;

/**
 * Movie view listener interface.
 */
public interface MovieViewListener {
    /**
     * Movie view listeners.
     */
    public static class Listeners extends ListenerList<MovieViewListener> implements
        MovieViewListener {
        @Override
        public void movieChanged(MovieView movieView, Movie previousMovie) {
            forEach(listener -> listener.movieChanged(movieView, previousMovie));
        }
    }

    /**
     * Called when an movie view's movie has changed.
     *
     * @param movieView The move view that has changed.
     * @param previousMovie The previous movie content.
     */
    public void movieChanged(MovieView movieView, Movie previousMovie);
}
