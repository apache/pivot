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
 * <p>Window state listener interface.</p>
 *
 * @author gbrown
 * @author tvolkert
 */
public interface WindowStateListener {
    /**
     * Called to preview a window open event.
     *
     * @param window
     * @param display
     *
     * @return
     * <tt>true</tt> to consume the event and prevent the change; <tt>false</tt>
     * to allow it.
     */
    public boolean previewWindowOpen(Window window, Display display);

    /**
     * Called when a window open event has been vetoed.
     *
     * @param window
     */
    public void windowOpenVetoed(Window window);

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
     *
     * @return
     * <tt>true</tt> to consume the event and prevent the change; <tt>false</tt>
     * to allow it.
     */
    public boolean previewWindowClose(Window window);

    /**
     * Called when a window close event has been vetoed.
     *
     * @param window
     */
    public void windowCloseVetoed(Window window);

    /**
     * Called when a window has closed.
     *
     * @param window
     * @param display
     */
    public void windowClosed(Window window, Display display);
}
