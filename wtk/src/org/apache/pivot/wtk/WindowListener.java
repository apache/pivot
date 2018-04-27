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

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.media.Image;

/**
 * Window listener interface.
 */
public interface WindowListener {
    /**
     * Window listeners.
     */
    public static class Listeners extends ListenerList<WindowListener> implements
        WindowListener {
        @Override
        public void titleChanged(Window window, String previousTitle) {
            forEach(listener -> listener.titleChanged(window, previousTitle));
        }

        @Override
        public void iconAdded(Window window, Image addedIcon) {
            forEach(listener -> listener.iconAdded(window, addedIcon));
        }

        @Override
        public void iconInserted(Window window, Image addedIcon, int index) {
            forEach(listener -> listener.iconInserted(window, addedIcon, index));
        }

        @Override
        public void iconsRemoved(Window window, int index, Sequence<Image> removed) {
            forEach(listener -> listener.iconsRemoved(window, index, removed));
        }

        @Override
        public void contentChanged(Window window, Component previousContent) {
            forEach(listener -> listener.contentChanged(window, previousContent));
        }

        @Override
        public void activeChanged(Window window, Window obverseWindow) {
            forEach(listener -> listener.activeChanged(window, obverseWindow));
        }

        @Override
        public void maximizedChanged(Window window) {
            forEach(listener -> listener.maximizedChanged(window));
        }
    }

    /**
     * Window listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements WindowListener {
        @Override
        public void titleChanged(Window window, String previousTitle) {
            // empty block
        }

        @Override
        public void iconAdded(Window window, Image addedIcon) {
            // empty block
        }

        @Override
        public void iconInserted(Window window, Image addedIcon, int index) {
            // empty block
        }

        @Override
        public void iconsRemoved(Window window, int index, Sequence<Image> removed) {
            // empty block
        }

        @Override
        public void contentChanged(Window window, Component previousContent) {
            // empty block
        }

        @Override
        public void activeChanged(Window window, Window obverseWindow) {
            // empty block
        }

        @Override
        public void maximizedChanged(Window window) {
            // empty block
        }
    }

    /**
     * Called when a window's title has changed.
     *
     * @param window        The window whose title has changed.
     * @param previousTitle What the title was previously (can be {@code null}).
     */
    default void titleChanged(Window window, String previousTitle) {
    }

    /**
     * Called when an icon has been added to a window.
     *
     * @param window    The window that has changed.
     * @param addedIcon The icon that was added.
     */
    default void iconAdded(Window window, Image addedIcon) {
    }

    /**
     * Called when a window has had an icon inserted.
     *
     * @param window    The window that has changed.
     * @param addedIcon The newly added icon.
     * @param index     The index where this icon was inserted in the
     *                  window's icon sequence..
     */
    default void iconInserted(Window window, Image addedIcon, int index) {
    }

    /**
     * Called when one or more of the window's icons were removed.
     *
     * @param window  The window that has changed.
     * @param index   Starting index of the icons that were removed in
     *                the window's icon sequence.
     * @param removed The sequence of icons that were actually removed.
     */
    default void iconsRemoved(Window window, int index, Sequence<Image> removed) {
    }

    /**
     * Called when a window's content component has changed.
     *
     * @param window          The window whose content has changed.
     * @param previousContent What the window's content was previously.
     */
    default void contentChanged(Window window, Component previousContent) {
    }

    /**
     * Called when a window's active state has changed.
     *
     * @param window        The window that has changed its active state.
     * @param obverseWindow The "other" window that is affected.
     */
    default void activeChanged(Window window, Window obverseWindow) {
    }

    /**
     * Called when a window's maximized state has changed.
     *
     * @param window The window whose state has changed.
     */
    default void maximizedChanged(Window window) {
    }
}
