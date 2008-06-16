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

import pivot.util.ListenerList;

/**
 * TODO The repeatable flag is used to trigger "split button" behavior.
 * When true, the button reflects the selected value and allows a user to
 * repeatedly press the left half of the button, firing additional menu
 * selection events for the selected item. Pressing the right half of the
 * button continues to fire button press events and display the menu.
 *
 * TODO When a menu item is selected, sets menu item data as button data;
 * sets menu item action key as button action key.
 *
 * @author gbrown
 */
public class MenuButton extends Button {
    @Override
    public void setToggleButton(boolean toggleButton) {
        throw new UnsupportedOperationException("Menu buttons cannot be toggle buttons.");
    }

    public Menu.ItemGroup getMenuData() {
        // TODO
        return null;
    }

    public void setMenuData(Menu.ItemGroup menuData) {
        // TODO
    }

    public boolean isRepeatable() {
        // TODO
        return false;
    }

    public void setRepeatable(boolean repeatable) {
        // TODO
    }

    public void selectMenuItem(Menu.Item menuItem) {
        // TODO Fire event
    }

    public ListenerList<MenuButtonListener> getMenuButtonListeners() {
        // TODO
        return null;
    }

    public ListenerList<MenuSelectionListener> getMenuSelectionListeners() {
        // TODO
        return null;
    }
}
