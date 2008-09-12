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
 * <p>Popup class representing a context menu.</p>
 *
 * <p>TODO Complete this class and associated skin class.</p>
 *
 * @author gbrown
 */
public class MenuPopup extends Popup {
    public Menu.ItemGroup getMenuData() {
        // TODO
        return null;
    }

    public void setMenuData(Menu.ItemGroup menuData) {
        // TODO
    }

    public void selectMenuItem(Menu.Item menuItem) {
        // TODO Fire event
    }

    public ListenerList<MenuPopupListener> getMenuPopupListeners() {
        // TODO
        return null;
    }

    public ListenerList<MenuSelectionListener> getMenuSelectionListeners() {
        // TODO
        return null;
    }
}
