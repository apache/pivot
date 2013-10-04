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

import org.apache.pivot.util.Vote;

/**
 * Window state listener interface.
 */
public interface WindowStateListener {
    /**
     * Window state listener adapter.
     */
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
     * @param window
     */
    public void windowOpened(Window window);

    /**
     * Called to preview a window close event.
     *
     * @param window
     */
    public Vote previewWindowClose(Window window);

    /**
     * Called to preview a window open event.
     *
     * @param window
     */
    public Vote previewWindowOpen(Window window);

    /**
     * Called when a window close event has been vetoed.
     *
     * @param window
     * @param reason
     */
    public void windowCloseVetoed(Window window, Vote reason);

    /**
     * Called when a window open event has been vetoed.
     *
     * @param window
     * @param reason
     */
    public void windowOpenVetoed(Window window, Vote reason);

    /**
     * Called when a window has closed.
     *
     * @param window
     * @param display
     * @param owner
     */
    public void windowClosed(Window window, Display display, Window owner);
}
