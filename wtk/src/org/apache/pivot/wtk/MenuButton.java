/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.wtk;

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.content.ButtonDataRenderer;


/**
 * Component that allows a user to select one of several menu options. The
 * options are hidden until the user pushes the button.
 * <p>
 * The repeatable flag is used to trigger "split button" behavior.
 * When true, the button reflects the selected value and allows a user to
 * repeatedly press the left half of the button, firing additional menu
 * selection events for the selected item. Pressing the right half of the
 * button continues to fire button press events and display the menu.
 */
@DefaultProperty("menu")
public class MenuButton extends Button {
    private static class MenuButtonListenerList extends WTKListenerList<MenuButtonListener>
        implements MenuButtonListener {
        @Override
        public void menuChanged(MenuButton menuButton, Menu previousMenu) {
            for (MenuButtonListener listener : this) {
                listener.menuChanged(menuButton, previousMenu);
            }
        }
    }

    /**
     * MenuButton skin interface. MenuButton skins must implement
     * this interface to facilitate additional communication between the
     * component and the skin.
     */
    public interface Skin {
        public Window getMenuPopup();
    }

    private Menu menu = null;

    private MenuButtonListenerList menuButtonListeners = new MenuButtonListenerList();

    private static final Button.DataRenderer DEFAULT_DATA_RENDERER = new ButtonDataRenderer();

    public MenuButton() {
        setDataRenderer(DEFAULT_DATA_RENDERER);
        installSkin(MenuButton.class);
    }

    @Override
    protected void setSkin(org.apache.pivot.wtk.Skin skin) {
        if (!(skin instanceof MenuButton.Skin)) {
            throw new IllegalArgumentException("Skin class must implement "
                + MenuButton.Skin.class.getName());
        }

        super.setSkin(skin);
    }

    /**
     * @return the popup window associated with this components skin
     */
    public Window getListPopup() {
        return ((MenuButton.Skin) getSkin()).getMenuPopup();
    }

    @Override
    public void setToggleButton(boolean toggleButton) {
        throw new UnsupportedOperationException("Menu buttons cannot be toggle buttons.");
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        if (menu != null
            && menu.getItem() != null) {
            throw new IllegalArgumentException("menu already belongs to an item.");
        }

        Menu previousMenu = this.menu;

        if (previousMenu != menu) {
            this.menu = menu;
            menuButtonListeners.menuChanged(this, previousMenu);
        }
    }

    public ListenerList<MenuButtonListener> getMenuButtonListeners() {
        return menuButtonListeners;
    }
}
