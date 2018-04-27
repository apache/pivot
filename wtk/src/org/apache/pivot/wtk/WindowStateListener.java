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

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Vote;
import org.apache.pivot.util.VoteResult;

/**
 * Window state listener interface.
 */
public interface WindowStateListener {
    /**
     * Window state listeners.
     */
    public static class Listeners extends ListenerList<WindowStateListener>
        implements WindowStateListener {
        @Override
        public void windowOpened(Window window) {
            forEach(listener -> listener.windowOpened(window));
        }

        @Override
        public Vote previewWindowClose(Window window) {
            VoteResult result = new VoteResult();

            forEach(listener -> result.tally(listener.previewWindowClose(window)));

            return result.get();
        }

        @Override
        public void windowCloseVetoed(Window window, Vote reason) {
            forEach(listener -> listener.windowCloseVetoed(window, reason));
        }

        @Override
        public Vote previewWindowOpen(Window window) {
            VoteResult result = new VoteResult();

            forEach(listener -> result.tally(listener.previewWindowOpen(window)));

            return result.get();
        }

        @Override
        public void windowOpenVetoed(Window window, Vote reason) {
            forEach(listener -> listener.windowOpenVetoed(window, reason));
        }

        @Override
        public void windowClosed(Window window, Display display, Window owner) {
            forEach(listener -> listener.windowClosed(window, display, owner));
        }
    }

    /**
     * Window state listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements WindowStateListener {
        @Override
        public void windowOpened(Window window) {
            // empty block
        }

        @Override
        public Vote previewWindowClose(Window window) {
            return Vote.APPROVE;
        }

        @Override
        public Vote previewWindowOpen(Window window) {
            return Vote.APPROVE;
        }

        @Override
        public void windowCloseVetoed(Window window, Vote reason) {
            // empty block
        }

        @Override
        public void windowOpenVetoed(Window window, Vote reason) {
            // empty block
        }

        @Override
        public void windowClosed(Window window, Display display, Window owner) {
            // empty block
        }
    }

    /**
     * Called when a window has opened.
     *
     * @param window The newly opened window.
     */
    default void windowOpened(Window window) {
    }

    /**
     * Called to preview a window close event.
     *
     * @param window The window that wants to close.
     * @return The vote from each listener as to whether to allow the close.
     */
    default Vote previewWindowClose(Window window) {
        return Vote.APPROVE;
    }

    /**
     * Called to preview a window open event.
     *
     * @param window The window that wants to open.
     * @return The vote from the listener as to whether to allow the open.
     */
    default Vote previewWindowOpen(Window window) {
        return Vote.APPROVE;
    }

    /**
     * Called when a window close event has been vetoed.
     *
     * @param window The window that was to close, but now will not.
     * @param reason The accumulated vote from all the listeners that
     *               vetoed this event.
     */
    default void windowCloseVetoed(Window window, Vote reason) {
    }

    /**
     * Called when a window open event has been vetoed.
     *
     * @param window The window that was to open, but now will not.
     * @param reason The accumulated vote from all the listeners that
     *               vetoed this event.
     */
    default void windowOpenVetoed(Window window, Vote reason) {
    }

    /**
     * Called when a window has closed.
     *
     * @param window  The window that is now closed.
     * @param display The display in which the window was shown.
     * @param owner   The owner of this window (which could be {@code null}).
     */
    default void windowClosed(Window window, Display display, Window owner) {
    }
}
