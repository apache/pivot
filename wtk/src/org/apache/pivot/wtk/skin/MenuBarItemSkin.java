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

import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.MenuPopup;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;

/**
 * Abstract base class for menu bar item skins.
 */
public abstract class MenuBarItemSkin extends ButtonSkin implements MenuBar.ItemListener {
    protected MenuPopup menuPopup = new MenuPopup();
    private boolean closeMenuPopup = false;

    private ComponentKeyListener menuPopupComponentKeyListener = new ComponentKeyListener.Adapter() {
        /**
         * {@link KeyCode#LEFT LEFT} or {@link KeyCode#TAB TAB} +
         * {@link Keyboard.Modifier#SHIFT SHIFT} Activate the menu to the left of the
         * current menu.<br>
         * {@link KeyCode#RIGHT RIGHT} or {@link KeyCode#TAB TAB} Activate
         * the menu to the right of the current menu.<br>
         */
        @Override
        public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            boolean consumed = false;

            MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();
            MenuBar menuBar = (MenuBar)menuBarItem.getParent();

            if (keyCode == Keyboard.KeyCode.LEFT
                || (keyCode == Keyboard.KeyCode.TAB
                    && Keyboard.isPressed(Keyboard.Modifier.SHIFT))) {
                menuBar.activatePreviousItem();
                consumed = true;

            } else if (keyCode == Keyboard.KeyCode.RIGHT
                || keyCode == Keyboard.KeyCode.TAB) {
                menuBar.activateNextItem();
                consumed = true;
            }

            return consumed;
        }
    };

    private WindowStateListener menuPopupWindowStateListener = new WindowStateListener.Adapter() {
        @Override
        public void windowOpened(Window window) {
            Display display = window.getDisplay();
            display.getContainerMouseListeners().add(displayMouseListener);
        }

        @Override
        public void windowClosed(Window window, Display display, Window owner) {
            MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();
            menuBarItem.setActive(false);

            // If the menu bar is no longer active, move the window to the
            // front to restore focus
            MenuBar menuBar = (MenuBar)menuBarItem.getParent();
            if (menuBar.getActiveItem() == null) {
                Window menuBarWindow = menuBar.getWindow();

                if (menuBarWindow.isOpen()) {
                    menuBarWindow.moveToFront();
                }
            }

            display.getContainerMouseListeners().remove(displayMouseListener);

            closeMenuPopup = false;
        }
    };

    private ContainerMouseListener displayMouseListener = new ContainerMouseListener.Adapter() {
        @Override
        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            Display display = (Display)container;
            Component descendant = display.getDescendantAt(x, y);

            if (!menuPopup.isAncestor(descendant)
                && descendant != MenuBarItemSkin.this.getComponent()) {
                menuPopup.close(descendant instanceof Display
                    || !menuPopup.isOwner(descendant.getWindow()));
            }

            return false;
        }
    };

    public MenuBarItemSkin() {
        menuPopup.getComponentKeyListeners().add(menuPopupComponentKeyListener);
        menuPopup.getWindowStateListeners().add(menuPopupWindowStateListener);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        MenuBar.Item menuBarItem = (MenuBar.Item)component;
        menuBarItem.getItemListeners().add(this);

        menuPopup.setMenu(menuBarItem.getMenu());
    }

    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public void mouseOver(Component component) {
        super.mouseOver(component);

        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();
        MenuBar menuBar = (MenuBar)menuBarItem.getParent();

        if (menuBar.getActiveItem() != null) {
            menuBarItem.setActive(true);
        }
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();
        closeMenuPopup = menuBarItem.isActive();
        menuBarItem.setActive(true);

        return consumed;
    }

    @Override
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseUp(component, button, x, y);

        if (closeMenuPopup) {
            menuPopup.close();
        }

        return consumed;
    }

    @Override
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        boolean consumed = super.mouseClick(component, button, x, y, count);

        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();
        menuBarItem.press();

        return consumed;
    }

    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        menuPopup.close(true);
    }

    @Override
    public void menuChanged(MenuBar.Item menuBarItem, Menu previousMenu) {
        menuPopup.setMenu(menuBarItem.getMenu());
        repaintComponent();
    }

    @Override
    public void activeChanged(MenuBar.Item menuBarItem) {
        if (menuBarItem.isActive()) {
            Display display = menuBarItem.getDisplay();
            Point menuBarItemLocation = menuBarItem.mapPointToAncestor(display, 0, getHeight());

            // TODO Ensure that the popup remains within the bounds of the display

            menuPopup.setLocation(menuBarItemLocation.x, menuBarItemLocation.y);
            menuPopup.open(menuBarItem.getWindow());
            menuPopup.requestFocus();
        } else {
            menuPopup.close(true);
        }

        repaintComponent();
    }
}
