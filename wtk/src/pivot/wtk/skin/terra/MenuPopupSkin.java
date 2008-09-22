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
package pivot.wtk.skin.terra;

import java.awt.Color;

import pivot.wtk.Border;
import pivot.wtk.Component;
import pivot.wtk.ComponentClassListener;
import pivot.wtk.Display;
import pivot.wtk.Keyboard;
import pivot.wtk.Menu;
import pivot.wtk.MenuItemSelectionListener;
import pivot.wtk.MenuPopup;
import pivot.wtk.MenuPopupListener;
import pivot.wtk.Panorama;
import pivot.wtk.Window;
import pivot.wtk.skin.PopupSkin;

/**
 * <p>Menu popup skin.</p>
 *
 * @author gbrown
 */
public class MenuPopupSkin extends PopupSkin
    implements MenuPopupListener, ComponentClassListener {
    private Panorama panorama;
    private Border border;

    private MenuItemSelectionListener menuItemPressListener = new MenuItemSelectionListener() {
        public void itemSelected(Menu.Item item) {
            MenuPopup menuPopup = (MenuPopup)getComponent();
            menuPopup.close();
        }
    };

    public MenuPopupSkin() {
        panorama = new Panorama();
        border = new Border(panorama);

        // TODO Make border color styleable; inherit from parent popup?
        border.getStyles().put("borderColor", new Color(0x99, 0x99, 0x99));
        border.getStyles().put("padding", 0);
    }

    @Override
    public void install(Component component) {
        validateComponentType(component, MenuPopup.class);

        super.install(component);

        MenuPopup menuPopup = (MenuPopup)component;
        menuPopup.getMenuPopupListeners().add(this);

        if (menuPopup.isOpen()) {
            Component.getComponentClassListeners().add(this);
        }

        Menu menu = menuPopup.getMenu();
        if (menu != null) {
            menu.getMenuItemPressListeners().add(menuItemPressListener);
        }

        border.setContent(menu);
        menuPopup.setContent(border);
    }

    @Override
    public void uninstall() {
        MenuPopup menuPopup = (MenuPopup)getComponent();
        menuPopup.getMenuPopupListeners().remove(this);

        if (menuPopup.isOpen()) {
            Component.getComponentClassListeners().remove(this);
        }

        Menu menu = menuPopup.getMenu();
        if (menu != null) {
            menu.getMenuItemPressListeners().remove(menuItemPressListener);
        }

        border.setContent(null);
        menuPopup.setContent(null);

        super.uninstall();
    }

    @Override
    public boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(keyCode, keyLocation);

        if (keyCode == Keyboard.KeyCode.ESCAPE) {
            MenuPopup menuPopup = (MenuPopup)getComponent();
            Component affiliate = menuPopup.getAffiliate();
            if (affiliate != null) {
                affiliate.requestFocus();
            }

            menuPopup.close();
        }

        return consumed;
    }

    @Override
    public void windowOpened(Window window) {
        super.windowOpened(window);
        Component.getComponentClassListeners().add(this);
    }

    @Override
    public void windowClosed(Window window, Display display) {
        super.windowClosed(window, display);
        Component.getComponentClassListeners().remove(this);
    }

    public void menuChanged(MenuPopup menuPopup, Menu previousMenu) {
        if (previousMenu != null) {
            previousMenu.getMenuItemPressListeners().remove(menuItemPressListener);
        }

        Menu menu = menuPopup.getMenu();
        if (menu != null) {
            menu.getMenuItemPressListeners().add(menuItemPressListener);
        }

        border.setContent(menu);
    }

    public void focusedComponentChanged(Component previousFocusedComponent) {
        MenuPopup menuPopup = (MenuPopup)getComponent();

        if (!menuPopup.containsFocus()) {
            Component affiliate = menuPopup.getAffiliate();
            Component focusedComponent = Component.getFocusedComponent();

            if (focusedComponent != null
                && focusedComponent != affiliate
                && !menuPopup.isOwningAncestorOf(focusedComponent.getWindow())) {
                menuPopup.close();
            }
        }
    }
}
