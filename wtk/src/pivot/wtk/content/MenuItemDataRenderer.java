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
package pivot.wtk.content;

import pivot.wtk.Button;
import pivot.wtk.FlowPane;

/**
 * <p>Default menu item data renderer.</p>
 *
 * @author gbrown
 */
public class MenuItemDataRenderer extends FlowPane implements Button.DataRenderer {
    public void render(Object data, Button button, boolean highlighted) {
        // TODO Query the skin for "gutter" style so we know where to paint
        // the icon and keyboard shortcut

        // TODO
    }
}
