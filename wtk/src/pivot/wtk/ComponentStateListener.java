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
 * <p>Component state listener interface.</p>
 *
 * @author gbrown
 * @author tvolkert
 */
public interface ComponentStateListener {
    /**
     * Called to preview an enabled change event.
     *
     * @param component
     *
     * @return
     * <tt>true</tt> to allow the enabled state to change; <tt>false</tt> to
     * disallow it
     */
    public boolean previewEnabledChange(Component component);

    /**
     * Called when a component's enabled state has changed.
     *
     * @param component
     */
    public void enabledChanged(Component component);

    /**
     * Called to preview a focused change event.
     *
     * @param component
     * @param temporary
     *
     * @return
     * <tt>true</tt> to allow the focus state to change; <tt>false</tt> to
     * disallow it
     */
    public boolean previewFocusedChange(Component component, boolean temporary);

    /**
     * Called when a component's focused state has changed.
     *
     * @param component
     * @param temporary
     */
    public void focusedChanged(Component component, boolean temporary);
}
