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

import pivot.util.Vote;
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
import pivot.wtk.effects.DropShadowDecorator;
import pivot.wtk.effects.Transition;
import pivot.wtk.effects.TransitionListener;
import pivot.wtk.skin.PopupSkin;

/**
 * Menu popup skin.
 *
 * @author gbrown
 */
public class MenuPopupSkin extends PopupSkin
    implements MenuPopupListener, ComponentClassListener {
    private Panorama panorama;
    private Border border;

    private DropShadowDecorator dropShadowDecorator = null;
    private Transition closeTransition = null;

    private static final int CLOSE_TRANSITION_DURATION = 100;
    private static final int CLOSE_TRANSITION_RATE = 30;

    private MenuItemSelectionListener menuItemPressListener = new MenuItemSelectionListener() {
        public void itemSelected(Menu.Item item) {
            final MenuPopup menuPopup = (MenuPopup)getComponent();
            closeTransition = new FadeTransition(menuPopup,
                CLOSE_TRANSITION_DURATION, CLOSE_TRANSITION_RATE);
            closeTransition.start(new TransitionListener() {
                public void transitionCompleted(Transition transition) {
                    menuPopup.close();
                }
            });
            menuPopup.close();
        }
    };

    public MenuPopupSkin() {
        setBackgroundColor((Color)null);

        panorama = new Panorama();
        panorama.getStyles().put("buttonBackgroundColor", Color.WHITE);

        border = new Border(panorama);

        border.getStyles().put("color", new Color(0x99, 0x99, 0x99));
        border.getStyles().put("backgroundColor", null);
        border.getStyles().put("padding", 0);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        MenuPopup menuPopup = (MenuPopup)component;
        menuPopup.getMenuPopupListeners().add(this);

        if (menuPopup.isOpen()) {
            Component.getComponentClassListeners().add(this);
        }

        Menu menu = menuPopup.getMenu();
        if (menu != null) {
            menu.getMenuItemSelectionListeners().add(menuItemPressListener);
        }

        panorama.setView(menu);
        menuPopup.setContent(border);

        // Attach the drop-shadow decorator
        dropShadowDecorator = new DropShadowDecorator(3, 3, 3);
        menuPopup.getDecorators().add(dropShadowDecorator);
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
            menu.getMenuItemSelectionListeners().remove(menuItemPressListener);
        }

        panorama.setView(null);
        menuPopup.setContent(null);

        // Detach the drop shadow decorator
        menuPopup.getDecorators().remove(dropShadowDecorator);
        dropShadowDecorator = null;

        super.uninstall();
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

    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(component, keyCode, keyLocation);

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
    public Vote previewWindowClose(Window window) {
        return (closeTransition != null
            && closeTransition.isRunning()) ? Vote.DEFER : Vote.APPROVE;
    }

    @Override
    public void windowCloseVetoed(Window window, Vote reason) {
        if (reason == Vote.DENY
            && closeTransition != null) {
            closeTransition.stop();
        }
    }

    @Override
    public void windowClosed(Window window, Display display) {
        super.windowClosed(window, display);
        Component.getComponentClassListeners().remove(this);

        closeTransition = null;
    }

    public void menuChanged(MenuPopup menuPopup, Menu previousMenu) {
        if (previousMenu != null) {
            previousMenu.getMenuItemSelectionListeners().remove(menuItemPressListener);
        }

        Menu menu = menuPopup.getMenu();
        if (menu != null) {
            menu.getMenuItemSelectionListeners().add(menuItemPressListener);
        }

        panorama.setView(menu);
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
