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
 * <p>Tab pane attribute listener interface.</p>
 *
 * @author gbrown
 */
public interface TabPaneAttributeListener {
    /**
     * Called when a tab's name attribute has changed.
     *
     * @param tabPane
     * @param component
     * @param previousName
     */
    public void nameChanged(TabPane tabPane, Component component, String previousName);

    /**
     * Called when a tab's icon attribute has changed.
     *
     * @param tabPane
     * @param component
     * @param previousIcon
     */
    public void iconChanged(TabPane tabPane, Component component, Image previousIcon);
}
