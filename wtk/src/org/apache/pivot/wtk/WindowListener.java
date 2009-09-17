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

import org.apache.pivot.wtk.media.Image;

/**
 * Window listener interface.
 */
public interface WindowListener {
    /**
     * Window listener adapter.
     */
    public static class Adapter implements WindowListener {
        @Override
        public void titleChanged(Window window, String previousTitle) {
        }

        @Override
        public void iconChanged(Window window, Image previousIcon) {
        }

        @Override
        public void contentChanged(Window window, Component previousContent) {
        }

        @Override
        public void ownerChanged(Window window, Window previousOwner) {
        }

        @Override
        public void activeChanged(Window window, Window obverseWindow) {
        }

        @Override
        public void maximizedChanged(Window window) {
        }
    }

    /**
     * Called when a window's title has changed.
     *
     * @param window
     * @param previousTitle
     */
    public void titleChanged(Window window, String previousTitle);

    /**
     * Called when a window's icon has changed.
     *
     * @param window
     * @param previousIcon
     */
    public void iconChanged(Window window, Image previousIcon);

    /**
     * Called when a window's content component has changed.
     *
     * @param window
     * @param previousContent
     */
    public void contentChanged(Window window, Component previousContent);

    /**
     * Called when a window's owner has changed.
     *
     * @param window
     * @param previousOwner
     */
    public void ownerChanged(Window window, Window previousOwner);

    /**
     * Called when a window's active state has changed.
     *
     * @param window
     * @param obverseWindow
     */
    public void activeChanged(Window window, Window obverseWindow);

    /**
     * Called when a window's maximized state has changed.
     *
     * @param window
     */
    public void maximizedChanged(Window window);
}
