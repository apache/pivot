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

/**
 *
 *
 * @author gbrown
 * @author tvolkert
 */
public interface WindowStateListener {
    /**
     *
     *
     * @param window
     *
     * @param display
     *
     * @return
     * <tt>true</tt> to allow the window to open; <tt>false</tt> to disallow it
     */
    public boolean previewWindowOpen(Window window, Display display);

    /**
     *
     *
     * @param window
     */
    public void windowOpened(Window window);

    /**
     *
     *
     * @param window
     *
     * @return
     * <tt>true</tt> to allow the window to close; <tt>false</tt> to disallow it
     */
    public boolean previewWindowClose(Window window);

    /**
     *
     *
     * @param window
     *
     * @param display
     */
    public void windowClosed(Window window, Display display);
}
