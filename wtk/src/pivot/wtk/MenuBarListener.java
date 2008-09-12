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

import pivot.collections.Sequence;

/**
 * <p>Menu bar listener interface.</p>
 *
 * @author gbrown
 */
public interface MenuBarListener {
    /**
     * Called when a menu bar's item data renderer has changed.
     *
     * @param menuBar
     * @param previousItemDataRenderer
     */
    public void itemDataRendererChanged(MenuBar menuBar, MenuBar.ItemDataRenderer previousItemDataRenderer);

    /**
     * Called when a menu item group has been inserted into a menu bar's item
     * group sequence.
     *
     * @param menuBar
     * @param index
     */
    public void menuItemGroupInserted(MenuBar menuBar, int index);

    /**
     * Called when a menu item group has been removed from a menu bar's item
     * group sequence.
     *
     * @param menuBar
     * @param index
     * @param menuItemGroups
     */
    public void menuItemGroupsRemoved(MenuBar menuBar, int index, Sequence<Menu.ItemGroup> menuItemGroups);
}
