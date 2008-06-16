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
import pivot.wtk.HorizontalAlignment;

/**
 * TODO Add showIcon property to this class so the size of the button doesn't
 * change when changing selection between items with and without icons.
 *
 * @author gbrown
 */
public class ListButtonDataRenderer extends ButtonDataRenderer {
    public ListButtonDataRenderer() {
        super();

        getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
    }

    @Override
    public void render(Object data, Button button, boolean highlight) {
        if (data == null) {
            data = "";
        } else {
            if (data instanceof ListItem) {
                // Translate list item to button data
                ListItem listItem = (ListItem)data;
                data = new ButtonData(listItem.getIcon(), listItem.getLabel());
            }
        }

        super.render(data, button, highlight);
    }
}
