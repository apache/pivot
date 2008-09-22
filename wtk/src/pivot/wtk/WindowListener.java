/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk;

import pivot.wtk.media.Image;

/**
 * <p>Window listener interface.</p>
 *
 * @author gbrown
 */
public interface WindowListener {
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
     * Called when a window's active state has changed.
     *
     * @param window
     */
    public void activeChanged(Window window);

    /**
     * Called when a window's maximized state has changed.
     *
     * @param window
     */
    public void maximizedChanged(Window window);
}
