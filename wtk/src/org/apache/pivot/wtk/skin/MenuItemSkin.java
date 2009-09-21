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
package org.apache.pivot.wtk.skin;

import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuPopup;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;

/**
 * Abstract base class for menu item skins.
 */
public abstract class MenuItemSkin extends ButtonSkin implements Menu.ItemListener {
    protected MenuPopup menuPopup = new MenuPopup();

    private WindowStateListener menuPopupWindowStateListener = new WindowStateListener.Adapter() {
        @Override
        public void windowOpened(Window window) {
            Display display = window.getDisplay();
            display.getContainerMouseListeners().add(displayMouseListener);
        }

        @Override
        public void windowClosed(Window window, Display display, Window owner) {
            display.getContainerMouseListeners().remove(displayMouseListener);
        }
    };

    private ContainerMouseListener displayMouseListener = new ContainerMouseListener.Adapter() {
        @Override
        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            Display display = (Display)container;
            Component descendant = display.getDescendantAt(x, y);

            if (!menuPopup.isAncestor(descendant)
                && descendant != MenuItemSkin.this.getComponent()) {
                menuPopup.close();
            }

            return false;
        }
    };

    protected int buttonPressInterval = 200;
    protected ApplicationContext.ScheduledCallback buttonPressCallback = null;

    public MenuItemSkin() {
        menuPopup.getWindowStateListeners().add(menuPopupWindowStateListener);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Menu.Item menuItem = (Menu.Item)component;
        menuItem.getItemListeners().add(this);

        menuPopup.setMenu(menuItem.getMenu());
    }

    @Override
    public void uninstall() {
        Menu.Item menuItem = (Menu.Item)getComponent();
        menuItem.getItemListeners().remove(this);

        menuPopup.close(true);
        menuPopup.setMenu(null);

        super.uninstall();
    }

    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public void mouseOver(Component component) {
        super.mouseOver(component);

        final Menu.Item menuItem = (Menu.Item)getComponent();
        menuItem.setActive(true);

        if (buttonPressCallback != null) {
            buttonPressCallback.cancel();
            buttonPressCallback = null;
        }

        if (menuItem.getMenu() != null) {
            buttonPressCallback = ApplicationContext.scheduleCallback(new Runnable() {
                public void run() {
                    menuItem.press();
                }
            }, buttonPressInterval);
        }
    }

    @Override
    public void mouseOut(Component component) {
        super.mouseOut(component);

        if (buttonPressCallback != null) {
            buttonPressCallback.cancel();
            buttonPressCallback = null;
        }

        if (!menuPopup.isOpen()) {
            Menu.Item menuItem = (Menu.Item)getComponent();
            menuItem.setActive(false);
        }
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        if (buttonPressCallback != null) {
            buttonPressCallback.cancel();
            buttonPressCallback = null;
        }

        return consumed;
    }

    @Override
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        boolean consumed = super.mouseClick(component, button, x, y, count);

        Menu.Item menuItem = (Menu.Item)getComponent();
        menuItem.press();

        return consumed;
    }

    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        menuPopup.close(true);
    }

    @Override
    public void buttonPressed(Button button) {
        Menu.Item menuItem = (Menu.Item)getComponent();
        Menu menu = menuItem.getMenu();

        if (menu != null
            && !menuPopup.isOpen()) {
            Display display = menuItem.getDisplay();
            Point location = menuItem.mapPointToAncestor(display, getWidth(), 0);

            // TODO Ensure that the popup remains within the bounds of the display

            menuPopup.setLocation(location.x, location.y);
            menuPopup.open(menuItem.getWindow());
            menuPopup.requestFocus();
        }
    }

    @Override
    public void nameChanged(Menu.Item menuItem, String previousName) {
        // No-op
    }

    @Override
    public void menuChanged(Menu.Item menuItem, Menu previousMenu) {
        menuPopup.setMenu(menuItem.getMenu());
        repaintComponent();
    }

    @Override
    public void activeChanged(Menu.Item menuItem) {
        if (!menuItem.isActive()) {
            menuPopup.close(true);
        }

        repaintComponent();
    }
}
