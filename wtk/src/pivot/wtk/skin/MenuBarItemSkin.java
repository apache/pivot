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
package pivot.wtk.skin;

import pivot.wtk.Button;
import pivot.wtk.Component;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.Direction;
import pivot.wtk.Display;
import pivot.wtk.Keyboard;
import pivot.wtk.Menu;
import pivot.wtk.MenuBar;
import pivot.wtk.MenuPopup;
import pivot.wtk.Mouse;
import pivot.wtk.Point;
import pivot.wtk.Window;
import pivot.wtk.WindowStateListener;

/**
 * Abstract base class for menu bar item skins.
 *
 * @author gbrown
 */
public abstract class MenuBarItemSkin extends ButtonSkin implements MenuBar.ItemListener {
    protected MenuPopup menuPopup = new MenuPopup();

    public MenuBarItemSkin() {
        menuPopup.getWindowStateListeners().add(new WindowStateListener() {
            public boolean previewWindowOpen(Window window, Display display) {
                return true;
            }

            public void windowOpened(Window window) {
                // No-op
            }

            public boolean previewWindowClose(Window window) {
                return true;
            }

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
        });
    }

    @Override
    public void uninstall() {
        menuPopup.close();

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
    public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        super.mouseClick(component, button, x, y, count);

        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();
        menuBarItem.press();
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
                Window window = menuBarItem.getWindow();
                Display display = window.getDisplay();
                Point menuBarItemLocation = menuBarItem.mapPointToAncestor(display, 0, getHeight());

                // TODO Ensure that the popup remains within the bounds of the display

                menuPopup.setLocation(menuBarItemLocation.x, menuBarItemLocation.y);
                menuPopup.open(menuBarItem);

                // Listen for key events from the popup
                menuPopup.getComponentKeyListeners().add(new ComponentKeyListener() {
                    public void keyTyped(Component component, char character) {
                        // No-op
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

    public void buttonPressed(Button button) {
        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();
        MenuBar menuBar = menuBarItem.getMenuBar();

        if (menuPopup.isOpen()) {
            if (menuBar.isActive()) {
                Component.clearFocus();
            } else {
                menuBar.setActive(true);
            }
        }
    }

    public void menuChanged(MenuBar.Item item, Menu previousMenu) {
        menuPopup.setMenu(item.getMenu());
        repaintComponent();
    }
}
