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
 * <p>Component that allows a user to select one of several menu options. The
 * options are hidden until the user pushes the button.</p>
 *
 * <p>NOTE The repeatable flag is used to trigger "split button" behavior.
 * When true, the button reflects the selected value and allows a user to
 * repeatedly press the left half of the button, firing additional menu
 * selection events for the selected item. Pressing the right half of the
 * button continues to fire button press events and display the menu.</p>
 *
 * @author gbrown
 */
public class MenuButton extends Button {
    private Menu menu = null;

    @Override
    public void setToggleButton(boolean toggleButton) {
        throw new UnsupportedOperationException("Menu buttons cannot be toggle buttons.");
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        // TODO
    }

    public boolean isRepeatable() {
        // TODO
        return false;
    }

    public void setRepeatable(boolean repeatable) {
        // TODO
    }

    public ListenerList<MenuButtonListener> getMenuButtonListeners() {
        // TODO
        return null;
    }
}
