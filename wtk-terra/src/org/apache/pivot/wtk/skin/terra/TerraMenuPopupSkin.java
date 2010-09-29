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
package org.apache.pivot.wtk.skin.terra;

import java.awt.Color;

import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuItemSelectionListener;
import org.apache.pivot.wtk.MenuPopup;
import org.apache.pivot.wtk.MenuPopupListener;
import org.apache.pivot.wtk.MenuPopupStateListener;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Panorama;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.effects.DropShadowDecorator;
import org.apache.pivot.wtk.effects.Transition;
import org.apache.pivot.wtk.effects.TransitionListener;
import org.apache.pivot.wtk.skin.WindowSkin;

/**
 * Menu popup skin.
 */
public class TerraMenuPopupSkin extends WindowSkin implements MenuPopupListener,
    MenuPopupStateListener {
    private class RepositionCallback implements Runnable {
        @Override
        public void run() {
            MenuPopup menuPopup = (MenuPopup)getComponent();
            Display display = menuPopup.getDisplay();

            Point location = menuPopup.getLocation();
            Dimensions size = menuPopup.getSize();

            int x = location.x;
            if (x + size.width > display.getWidth()) {
                x -= size.width;
            }

            int y = location.y;
            if (y + size.height > display.getHeight()) {
                y-= size.height;
            }

            menuPopup.setLocation(x, y);
        }
    }

    private Panorama panorama;
    private Border border;

    private DropShadowDecorator dropShadowDecorator = null;
    private Transition closeTransition = null;

    private int closeTransitionDuration = DEFAULT_CLOSE_TRANSITION_DURATION;
    private int closeTransitionRate = DEFAULT_CLOSE_TRANSITION_RATE;

    private ContainerMouseListener displayMouseListener = new ContainerMouseListener.Adapter() {
        @Override
        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            MenuPopup menuPopup = (MenuPopup)getComponent();

            if (menuPopup.isContextMenu()) {
                Display display = (Display)container;
                Component descendant = display.getDescendantAt(x, y);

                if (descendant != display) {
                    Window window = descendant.getWindow();

                    if (!menuPopup.isAncestor(descendant)
                        && (window == null
                            || !menuPopup.isOwner(window))) {
                        menuPopup.close();
                    }
                }
            }

            return false;
        }

        @Override
        public boolean mouseWheel(Container container, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            boolean consumed = false;

            Display display = (Display)container;
            Window window = (Window)display.getComponentAt(x, y);

            MenuPopup menuPopup = (MenuPopup)getComponent();
            if (window != menuPopup
                && (window == null
                    || !menuPopup.isOwner(window))) {
                consumed = true;
            }

            return consumed;
        }
    };

    private MenuItemSelectionListener menuItemSelectionListener = new MenuItemSelectionListener() {
        @Override
        public void itemSelected(Menu.Item item) {
            MenuPopup menuPopup = (MenuPopup)getComponent();
            menuPopup.close();
        }
    };

    private static final int DEFAULT_CLOSE_TRANSITION_DURATION = 250;
    private static final int DEFAULT_CLOSE_TRANSITION_RATE = 30;

    public TerraMenuPopupSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor((Color)null);

        panorama = new Panorama();
        panorama.getStyles().put("buttonBackgroundColor", Color.WHITE);

        border = new Border(panorama);

        border.getStyles().put("color", theme.getColor(7));
        border.getStyles().put("backgroundColor", null);
        border.getStyles().put("padding", 0);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        MenuPopup menuPopup = (MenuPopup)component;
        menuPopup.getMenuPopupListeners().add(this);
        menuPopup.getMenuPopupStateListeners().add(this);

        Menu menu = menuPopup.getMenu();
        if (menu != null) {
            menu.getMenuItemSelectionListeners().add(menuItemSelectionListener);
        }

        panorama.setView(menu);
        menuPopup.setContent(border);

        // Attach the drop-shadow decorator
        dropShadowDecorator = new DropShadowDecorator(3, 3, 3);
        menuPopup.getDecorators().add(dropShadowDecorator);
    }

    public Color getBorderColor() {
        return (Color)border.getStyles().get("color");
    }

    public void setBorderColor(Color borderColor) {
        border.getStyles().put("color", borderColor);
    }

    public void setBorderColor(String borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        border.getStyles().put("color", borderColor);
    }

    public int getCloseTransitionDuration() {
        return closeTransitionDuration;
    }

    public void setCloseTransitionDuration(int closeTransitionDuration) {
        this.closeTransitionDuration = closeTransitionDuration;
    }

    public int getCloseTransitionRate() {
        return closeTransitionRate;
    }

    public void setCloseTransitionRate(int closeTransitionRate) {
        this.closeTransitionRate = closeTransitionRate;
    }

    /**
     * {@link KeyCode#ESCAPE ESCAPE} Close the menu popup.
     */
    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(component, keyCode, keyLocation);

        if (keyCode == Keyboard.KeyCode.ESCAPE) {
            MenuPopup menuPopup = (MenuPopup)getComponent();
            menuPopup.close();
        }

        return consumed;
    }

    @Override
    public void windowOpened(Window window) {
        super.windowOpened(window);

        Display display = window.getDisplay();
        display.getContainerMouseListeners().add(displayMouseListener);

        MenuPopup menuPopup = (MenuPopup)window;
        Menu menu = menuPopup.getMenu();
        if (menu != null) {
            Menu.Item activeItem = menu.getActiveItem();
            if (activeItem != null) {
                activeItem.setActive(false);
            }

            menu.requestFocus();
        }

        panorama.setScrollTop(0);

        if (menuPopup.isContextMenu()) {
            ApplicationContext.queueCallback(new RepositionCallback());
        }
    }

    @Override
    public void windowClosed(Window window, Display display, Window owner) {
        super.windowClosed(window, display, owner);

        display.getContainerMouseListeners().remove(displayMouseListener);

        if (owner != null
            && owner.isOpen()) {
            owner.moveToFront();
        }
    }

    @Override
    public void menuChanged(MenuPopup menuPopup, Menu previousMenu) {
        if (previousMenu != null) {
            previousMenu.getMenuItemSelectionListeners().remove(menuItemSelectionListener);
        }

        Menu menu = menuPopup.getMenu();
        if (menu != null) {
            menu.getMenuItemSelectionListeners().add(menuItemSelectionListener);
        }

        panorama.setView(menu);
    }

    @Override
    public Vote previewMenuPopupClose(final MenuPopup menuPopup, boolean immediate) {
        if (!immediate
            && closeTransition == null) {
            border.setEnabled(false);

            closeTransition = new FadeWindowTransition(menuPopup,
                closeTransitionDuration, closeTransitionRate,
                dropShadowDecorator);

            closeTransition.start(new TransitionListener() {
                @Override
                public void transitionCompleted(Transition transition) {
                    menuPopup.close();
                }
            });
        }

        return (closeTransition != null
            && closeTransition.isRunning()) ? Vote.DEFER : Vote.APPROVE;
    }

    @Override
    public void menuPopupCloseVetoed(MenuPopup menuPopup, Vote reason) {
        if (reason == Vote.DENY
            && closeTransition != null) {
            closeTransition.stop();

            border.setEnabled(true);
            closeTransition = null;
        }
    }

    @Override
    public void menuPopupClosed(MenuPopup menuPopup) {
        border.setEnabled(true);
        closeTransition = null;
    }
}
