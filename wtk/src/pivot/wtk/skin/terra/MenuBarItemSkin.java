package pivot.wtk.skin.terra;

import java.awt.Color;
import java.awt.Graphics2D;

import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.ButtonStateListener;
import pivot.wtk.Component;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.Cursor;
import pivot.wtk.Dimensions;
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
import pivot.wtk.skin.ButtonSkin;

/**
 * <p>Menu bar item skin.</p>
 *
 * @author gbrown
 */
public class MenuBarItemSkin extends ButtonSkin
    implements ButtonPressListener, ButtonStateListener, MenuBar.ItemListener {
    private MenuPopup menuPopup = new MenuPopup();

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
    public void install(Component component) {
        validateComponentType(component, MenuBar.Item.class);

        super.install(component);

        MenuBar.Item menuBarItem = (MenuBar.Item)component;
        menuBarItem.getButtonPressListeners().add(this);
        menuBarItem.getButtonStateListeners().add(this);
        menuBarItem.getItemListeners().add(this);

        menuBarItem.setCursor(Cursor.DEFAULT);
    }

    @Override
    public void uninstall() {
        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();
        menuBarItem.getButtonPressListeners().remove(this);
        menuBarItem.getButtonStateListeners().remove(this);
        menuBarItem.getItemListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();

        Button.DataRenderer dataRenderer = menuBarItem.getDataRenderer();
        dataRenderer.render(menuBarItem.getButtonData(), menuBarItem, false);

        return dataRenderer.getPreferredWidth(height);
    }

    public int getPreferredHeight(int width) {
        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();

        Button.DataRenderer dataRenderer = menuBarItem.getDataRenderer();
        dataRenderer.render(menuBarItem.getButtonData(), menuBarItem, false);

        return dataRenderer.getPreferredHeight(width);
    }

    public Dimensions getPreferredSize() {
        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();

        Button.DataRenderer dataRenderer = menuBarItem.getDataRenderer();
        dataRenderer.render(menuBarItem.getButtonData(), menuBarItem, false);

        return dataRenderer.getPreferredSize();
    }

    public void layout() {
        // No-op
    }

    public void paint(Graphics2D graphics) {
        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();
        MenuBar menuBar = menuBarItem.getMenuBar();

        int width = getWidth();
        int height = getHeight();

        boolean highlight = menuPopup.isOpen();

        // Paint highlight state
        if (highlight) {
            Color highlightBackgroundColor = (Color)menuBar.getStyles().get("highlightBackgroundColor");
            graphics.setColor(highlightBackgroundColor);
            graphics.fillRect(0, 0, width, height);
        }

        // Paint the content
        Button.DataRenderer dataRenderer = menuBarItem.getDataRenderer();
        dataRenderer.render(menuBarItem.getButtonData(), menuBarItem, highlight);
        dataRenderer.setSize(width, height);

        dataRenderer.paint(graphics);
    }

    public Color getPopupBorderColor() {
        return (Color)menuPopup.getStyles().get("borderColor");
    }

    public void setPopupBorderColor(Color popupBorderColor) {
        menuPopup.getStyles().put("borderColor", popupBorderColor);
    }

    public void setPopupBorderColor(String popupBorderColor) {
        if (popupBorderColor == null) {
            throw new IllegalArgumentException("popupBorderColor is null.");
        }

        menuPopup.getStyles().put("borderColor", popupBorderColor);
    }

    @Override
    public void mouseOver() {
        super.mouseOver();

        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();
        MenuBar menuBar = menuBarItem.getMenuBar();

        if (menuBar.isActive()) {
            menuBarItem.requestFocus();
        }
    }

    @Override
    public boolean mouseDown(Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(button, x, y);

        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();
        menuBarItem.requestFocus();

        return consumed;
    }

    @Override
    public void mouseClick(Mouse.Button button, int x, int y, int count) {
        super.mouseClick(button, x, y, count);

        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();
        menuBarItem.press();
    }

    @Override
    public boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(keyCode, keyLocation);

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
            consumed = super.keyPressed(keyCode, keyLocation);
        }

        return consumed;
    }

    @Override
    public boolean keyReleased(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyReleased(keyCode, keyLocation);

        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();

        if (keyCode == Keyboard.KeyCode.SPACE) {
            menuBarItem.press();
            consumed = true;
        } else {
            consumed = super.keyReleased(keyCode, keyLocation);
        }

        return consumed;
    }

    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        menuPopup.close();
        repaintComponent();
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

                    public void keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
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
                    }

                    public void keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
                        // No-op
                    }
                });
            }
        } else {
            if (!temporary
                && !menuPopup.containsFocus()) {
                menuPopup.close();
            }
        }

        repaintComponent();
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

    public boolean previewStateChange(Button button, Button.State state) {
        return true;
    }

    public void stateChanged(Button button, Button.State previousState) {
        repaintComponent();
    }

    public void menuChanged(MenuBar.Item item, Menu previousMenu) {
        menuPopup.setMenu(item.getMenu());
        repaintComponent();
    }
}
