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
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuButton;
import org.apache.pivot.wtk.MenuButtonListener;
import org.apache.pivot.wtk.MenuPopup;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;

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
        }

        @Override
        public void windowClosed(Window window, Display display, Window owner) {
            display.getContainerMouseListeners().remove(displayMouseListener);

            Window menuButtonWindow = getComponent().getWindow();
            menuButtonWindow.moveToFront();
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
    }

    // MenuButton.Skin methods

    public Window getListPopup() {
        return menuPopup;
    }

    // Component state events
    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        menuPopup.close();
        pressed = false;
    }

    @Override
    public void focusedChanged(Component component, Component obverseComponent) {
        super.focusedChanged(component, obverseComponent);

        // Close the popup if focus was transferred to a component whose
        // window is not the popup
        if (!component.isFocused()
            && !menuPopup.containsFocus()) {
            menuPopup.close();
        }

        pressed = false;
    }

    // Component mouse events
    @Override
    public void mouseOut(Component component) {
        super.mouseOut(component);

        pressed = false;
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        // TODO Consume the event if the menu button is repeatable and the event
        // occurs over the trigger

        pressed = true;
        repaintComponent();

        return consumed;
    }

    @Override
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseUp(component, button, x, y);

        pressed = false;
        repaintComponent();

        return consumed;
    }

    @Override
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        MenuButton menuButton = (MenuButton)getComponent();
        menuButton.press();

        if (menuPopup.isShowing()) {
            menuPopup.requestFocus();
        }

        return true;
    }

    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (keyCode == Keyboard.KeyCode.SPACE) {
            pressed = true;
            repaintComponent();
            consumed = true;
        } else {
            consumed = super.keyPressed(component, keyCode, keyLocation);
        }

        return consumed;
    }

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

    @Override
    public void repeatableChanged(MenuButton menuButton) {
        invalidateComponent();
    }
}
