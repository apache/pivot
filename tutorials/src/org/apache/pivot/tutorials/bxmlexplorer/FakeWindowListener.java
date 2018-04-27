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
package org.apache.pivot.tutorials.bxmlexplorer;

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.media.Image;

public interface FakeWindowListener {
    /**
     * Fake window listeners.
     */
    public static class Listeners extends ListenerList<FakeWindowListener>
        implements FakeWindowListener {
        @Override
        public void titleChanged(FakeWindow window, String previousTitle) {
            forEach(listener -> listener.titleChanged(window, previousTitle));
        }

        @Override
        public void iconAdded(FakeWindow window, Image addedIcon) {
            forEach(listener -> listener.iconAdded(window, addedIcon));
        }

        @Override
        public void iconInserted(FakeWindow window, Image addedIcon, int index) {
            forEach(listener -> listener.iconInserted(window, addedIcon, index));
        }

        @Override
        public void iconsRemoved(FakeWindow window, int index, Sequence<Image> removed) {
            forEach(listener -> listener.iconsRemoved(window, index, removed));
        }

        @Override
        public void contentChanged(FakeWindow window, Component previousContent) {
            forEach(listener -> listener.contentChanged(window, previousContent));
        }

    }

    /**
     * Called when a window's title has changed.
     *
     * @param window
     * @param previousTitle
     */
    default void titleChanged(FakeWindow window, String previousTitle) {
    }

    /**
     * Called when a window's icon has changed.
     *
     * @param window
     * @param addedIcon
     */
    default void iconAdded(FakeWindow window, Image addedIcon) {
    }

    /**
     * Called when a window's icon has changed.
     *
     * @param window
     * @param addedIcon
     */
    default void iconInserted(FakeWindow window, Image addedIcon, int index) {
    }

    /**
     * Called when a window's icon has changed.
     *
     * @param window
     * @param index
     * @param removed
     */
    default void iconsRemoved(FakeWindow window, int index, Sequence<Image> removed) {
    }

    /**
     * Called when a window's content component has changed.
     *
     * @param window
     * @param previousContent
     */
    default void contentChanged(FakeWindow window, Component previousContent) {
    }

}
