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

import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuButton;
import org.apache.pivot.wtk.MenuButtonListener;
import org.apache.pivot.wtk.MenuPopup;
import org.apache.pivot.wtk.MenuPopupStateListener;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;
import org.apache.pivot.wtk.Keyboard.KeyCode;

/**
 * Abstract base class for menu button skins.
 */
public abstract class MenuButtonSkin extends ButtonSkin
    implements MenuButton.Skin, MenuButtonListener {
    protected boolean pressed = false;
    protected MenuPopup menuPopup = new MenuPopup();

    private WindowStateListener menuPopupWindowStateListener = new WindowStateListener.Adapter() {
        @Override
        public void windowOpened(Window window) {
            Display display = window.getDisplay();
            display.getContainerMouseListeners().add(displayMouseListener);

            window.requestFocus();
        }

        @Override
        public void windowClosed(Window window, Display display, Window owner) {
            display.getContainerMouseListeners().remove(displayMouseListener);

            Window componentWindow = getComponent().getWindow();
            if (componentWindow != null
                && componentWindow.isOpen()
                && !componentWindow.isClosing()) {
                componentWindow.moveToFront();
            }

            pressed = false;
            repaintComponent();
        }
    };

    private MenuPopupStateListener menuPopupStateListener = new MenuPopupStateListener.Adapter() {
        @Override
        public Vote previewMenuPopupClose(MenuPopup menuPopupArgument, boolean immediate) {
            if (menuPopupArgument.containsFocus()) {
                getComponent().requestFocus();
            }

            return Vote.APPROVE;
        }

        @Override
        public void menuPopupCloseVetoed(MenuPopup menuPopupArgument, Vote reason) {
            if (reason == Vote.DENY) {
                menuPopupArgument.requestFocus();
            }
        }
    };

    private ContainerMouseListener displayMouseListener = new ContainerMouseListener.Adapter() {
        @Override
        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            Display display = (Display)container;
            Component descendant = display.getDescendantAt(x, y);

            if (!menuPopup.isAncestor(descendant)
                && descendant != MenuButtonSkin.this.getComponent()) {
                menuPopup.close();
            }

            return false;
        }
    };

    @Override
    public void install(Component component) {
        super.install(component);

        MenuButton menuButton = (MenuButton)getComponent();
        menuButton.getMenuButtonListeners().add(this);

        menuPopup.setMenu(menuButton.getMenu());

        menuPopup.getWindowStateListeners().add(menuPopupWindowStateListener);
        menuPopup.getMenuPopupStateListeners().add(menuPopupStateListener);
    }

    // MenuButton.Skin methods

    @Override
    public Window getMenuPopup() {
        return menuPopup;
    }

    // Component state events
    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        if (!component.isEnabled()) {
            pressed = false;
        }

        repaintComponent();

        menuPopup.close();
    }

    // Component mouse events
    @Override
    public void mouseOut(Component component) {
        super.mouseOut(component);

        pressed = false;
        repaintComponent();
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        pressed = true;
        repaintComponent();

        if (menuPopup.isOpen()) {
            menuPopup.close();
        } else {
            menuPopup.open(component.getWindow());
        }

        return consumed;
    }

    @Override
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseUp(component, button, x, y);

        pressed = false;
        repaintComponent();

        return consumed;
    }

    /**
     * {@link KeyCode#SPACE SPACE} Repaints the component to reflect the pressed
     * state.
     *
     * @see #keyReleased(Component, int,
     * org.apache.pivot.wtk.Keyboard.KeyLocation)
     */
    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (keyCode == Keyboard.KeyCode.SPACE) {
            pressed = true;
            repaintComponent();

            if (menuPopup.isOpen()) {
                menuPopup.close();
            } else {
                menuPopup.open(component.getWindow());
            }

            consumed = true;
        } else {
            consumed = super.keyPressed(component, keyCode, keyLocation);
        }

        return consumed;
    }

    /**
     * {@link KeyCode#SPACE SPACE} 'presses' the button.
     */
    @Override
    public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        MenuButton menuButton = (MenuButton)getComponent();

        if (keyCode == Keyboard.KeyCode.SPACE) {
            pressed = false;
            repaintComponent();

            menuButton.press();
        } else {
            consumed = super.keyReleased(component, keyCode, keyLocation);
        }

        return consumed;
    }

    @Override
    public void menuChanged(MenuButton menuButton, Menu previousMenu) {
        menuPopup.setMenu(menuButton.getMenu());
    }
}
