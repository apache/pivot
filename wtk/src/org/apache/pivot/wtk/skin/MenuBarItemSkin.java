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
import org.apache.pivot.wtk.Direction;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.MenuPopup;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;

/**
 * Abstract base class for menu bar item skins.
 *
 * @author gbrown
 */
public abstract class MenuBarItemSkin extends ButtonSkin implements MenuBar.ItemListener {
    private WindowStateListener menuPopupWindowListener = new WindowStateListener.Adapter() {
        public void windowClosed(Window window, Display display) {
            MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();
            if (menuBarItem.isFocused()) {
                Component.clearFocus();
            } else {
                repaintComponent();
            }

            MenuBar menuBar = menuBarItem.getMenuBar();
            if (!menuBar.containsFocus()) {
                menuBar.setActive(false);
            }
        }
    };

    protected MenuPopup menuPopup = new MenuPopup();

    public MenuBarItemSkin() {
        menuPopup.getWindowStateListeners().add(menuPopupWindowListener);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        MenuBar.Item menuBarItem = (MenuBar.Item)component;
        menuBarItem.getItemListeners().add(this);

        menuPopup.setMenu(menuBarItem.getMenu());
    }

    @Override
    public void uninstall() {
        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();
        menuBarItem.getItemListeners().remove(this);

        menuPopup.close();
        menuPopup.setMenu(null);

        super.uninstall();
    }

    @Override
    public void mouseOver(Component component) {
        super.mouseOver(component);

        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();
        MenuBar menuBar = menuBarItem.getMenuBar();

        if (menuBar.isActive()) {
            menuBarItem.requestFocus();
        }
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();
        menuBarItem.requestFocus();

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
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();

        if (keyCode == Keyboard.KeyCode.UP) {
            menuPopup.requestFocus();
            Component focusedComponent = Component.getFocusedComponent();
            if (focusedComponent != null) {
                focusedComponent.transferFocus(Direction.BACKWARD);
            }

            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.DOWN) {
            menuPopup.requestFocus();
            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.LEFT) {
            menuBarItem.transferFocus(Direction.BACKWARD);
            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.RIGHT) {
            menuBarItem.transferFocus(Direction.FORWARD);
            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.ENTER) {
            menuBarItem.press();
            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.ESCAPE) {
            Component.clearFocus();
            consumed = true;
        } else {
            consumed = super.keyPressed(component, keyCode, keyLocation);
        }

        return consumed;
    }

    @Override
    public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();

        if (keyCode == Keyboard.KeyCode.SPACE) {
            menuBarItem.press();
            consumed = true;
        } else {
            consumed = super.keyReleased(component, keyCode, keyLocation);
        }

        return consumed;
    }

    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        menuPopup.close();
    }

    @Override
    public void focusedChanged(Component component, boolean temporary) {
        super.focusedChanged(component, temporary);

        final MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();

        if (component.isFocused()) {
            if (!menuPopup.isOpen()) {
                Display display = menuBarItem.getDisplay();
                Point menuBarItemLocation = menuBarItem.mapPointToAncestor(display, 0, getHeight());

                // TODO Ensure that the popup remains within the bounds of the display

                menuPopup.setLocation(menuBarItemLocation.x, menuBarItemLocation.y);
                menuPopup.open(menuBarItem);

                // Listen for key events from the popup
                menuPopup.getComponentKeyListeners().add(new ComponentKeyListener() {
                    public boolean keyTyped(Component component, char character) {
                        return false;
                    }

                    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
                        if (keyCode == Keyboard.KeyCode.LEFT
                            || (keyCode == Keyboard.KeyCode.TAB
                                && Keyboard.isPressed(Keyboard.Modifier.SHIFT))) {
                            menuBarItem.transferFocus(Direction.BACKWARD);
                        } else if (keyCode == Keyboard.KeyCode.RIGHT
                            || keyCode == Keyboard.KeyCode.TAB) {
                            menuBarItem.transferFocus(Direction.FORWARD);
                        } else if (keyCode == Keyboard.KeyCode.ESCAPE) {
                            Component.clearFocus();
                        }

                        return false;
                    }

                    public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
                        return false;
                    }
                });
            }
        } else {
            if (!temporary
                && !menuPopup.containsFocus()) {
                menuPopup.close();
            }
        }
    }

    public void menuChanged(MenuBar.Item menuBarItem, Menu previousMenu) {
        menuPopup.setMenu(menuBarItem.getMenu());
        repaintComponent();
    }
}
