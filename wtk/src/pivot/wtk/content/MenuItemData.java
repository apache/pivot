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

import pivot.wtk.media.Image;

/**
 * <p>Default menu item data implementation.</p>
 *
 * @author gbrown
 */
public class MenuItemData {
    private Image icon = null;
    private String label = null;

    /**
     * Constructor.
     *
     * @param icon
     * The icon to display in the menu item.
     *
     * @param label
     * The label to display in the menu item.
     */
    public MenuItemData(Image icon, String label) {
        this.icon = icon;
        this.label = label;
    }

    /**
     * Returns this item's icon.
     *
     * @return
     * The item's icon, or <tt>null</tt> if the item does not include an icon.
     */
    public Image getIcon() {
        return icon;
    }

    /**
     * Returns this item's label.
     *
     * @return
     * The item's label, or <tt>null</tt> if the item does not include a label.
     */
    public String getLabel() {
        return label;
    }
}
