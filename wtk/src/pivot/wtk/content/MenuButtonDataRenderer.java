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

public class MenuButtonDataRenderer extends ButtonDataRenderer {
    @Override
    public void render(Object data, Button button, boolean highlight) {
        if (data instanceof MenuItemData) {
            // Translate menu item data to button data
            MenuItemData menuItemData = (MenuItemData)data;
            data = new ButtonData(menuItemData.getIcon(), menuItemData.getLabel());
        }

        super.render(data, button, highlight);
    }
}
