package pivot.wtk.skin.terra;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import pivot.wtk.ApplicationContext;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.ButtonStateListener;
import pivot.wtk.Component;
import pivot.wtk.Cursor;
import pivot.wtk.Dimensions;
import pivot.wtk.Direction;
import pivot.wtk.Display;
import pivot.wtk.Keyboard;
import pivot.wtk.Menu;
import pivot.wtk.MenuPopup;
import pivot.wtk.Mouse;
import pivot.wtk.Point;
import pivot.wtk.Window;
import pivot.wtk.media.Image;
import pivot.wtk.skin.ButtonSkin;

/**
 * <p>Menu item skin.</p>
 *
 * @author gbrown
 */
public class MenuItemSkin extends ButtonSkin
    implements ButtonPressListener, ButtonStateListener, Menu.ItemListener {
    public final class CheckmarkImage extends Image {
        public static final int SIZE = 14;
        public static final int CHECKMARK_SIZE = 10;

        public int getWidth() {
            return SIZE;
        }

        public int getHeight() {
            return SIZE;
        }

        public void paint(Graphics2D graphics) {
            Menu.Item menuItem = (Menu.Item)getComponent();
            Menu menu = menuItem.getSection().getMenu();

            Color color = (Color)menu.getStyles().get("color");
            graphics.setColor(color);
            graphics.setStroke(new BasicStroke(2.5f));

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw a checkmark
            int n = CHECKMARK_SIZE / 2;
            int m = CHECKMARK_SIZE / 4;
            int offsetX = (SIZE - (n + m)) / 2;
            int offsetY = (SIZE - n) / 2;

            graphics.drawLine(offsetX, (n - m) + offsetY,
                m + offsetX, n + offsetY);
            graphics.drawLine(m + offsetX, n + offsetY,
                (m + n) + offsetX, offsetY);
        }
    }

    private MenuPopup menuPopup = new MenuPopup();

    private int buttonPressTimeoutID = -1;

    private int buttonPressInterval = 200;
    private Image checkmarkImage = new CheckmarkImage();

    public static final int EXPANDER_SIZE = 11;
    public static final int EXPANDER_ICON_SIZE = 5;

    @Override
    public void install(Component component) {
        validateComponentType(component, Menu.Item.class);

        super.install(component);

        Menu.Item menuItem = (Menu.Item)component;
        menuItem.getButtonPressListeners().add(this);
        menuItem.getButtonStateListeners().add(this);
        menuItem.getItemListeners().add(this);

        menuItem.setCursor(Cursor.DEFAULT);
    }

    @Override
    public void uninstall() {
        menuPopup.close();

        Menu.Item menuItem = (Menu.Item)getComponent();
        menuItem.getButtonPressListeners().remove(this);
        menuItem.getButtonStateListeners().remove(this);
        menuItem.getItemListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        Menu.Item menuItem = (Menu.Item)getComponent();

        Button.DataRenderer dataRenderer = menuItem.getDataRenderer();
        dataRenderer.render(menuItem.getButtonData(), menuItem, false);

        return dataRenderer.getPreferredWidth(height) + EXPANDER_SIZE;
    }

    public int getPreferredHeight(int width) {
        Menu.Item menuItem = (Menu.Item)getComponent();

        Button.DataRenderer dataRenderer = menuItem.getDataRenderer();
        dataRenderer.render(menuItem.getButtonData(), menuItem, false);

        return Math.max(dataRenderer.getPreferredHeight(width), EXPANDER_SIZE);
    }

    public Dimensions getPreferredSize() {
        Menu.Item menuItem = (Menu.Item)getComponent();

        Button.DataRenderer dataRenderer = menuItem.getDataRenderer();
        dataRenderer.render(menuItem.getButtonData(), menuItem, false);

        Dimensions preferredSize = dataRenderer.getPreferredSize();

        preferredSize.width += EXPANDER_SIZE;
        preferredSize.height = Math.max(preferredSize.height, EXPANDER_SIZE);

        return preferredSize;
    }

    public void layout() {
        // No-op
    }

    public void paint(Graphics2D graphics) {
        Menu.Item menuItem = (Menu.Item)getComponent();
        Menu menu = menuItem.getSection().getMenu();

        int width = getWidth();
        int height = getHeight();

        boolean highlight = (menuItem.isFocused()
            || menuPopup.isOpen());

        // Paint highlight state
        if (highlight) {
            Color highlightBackgroundColor = (Color)menu.getStyles().get("highlightBackgroundColor");
            graphics.setColor(highlightBackgroundColor);
            graphics.fillRect(0, 0, width, height);
        }

        // Paint the content
        Button.DataRenderer dataRenderer = menuItem.getDataRenderer();
        dataRenderer.render(menuItem.getButtonData(), menuItem, highlight);
        dataRenderer.setSize(Math.max(width - EXPANDER_SIZE, 0), height);

        dataRenderer.paint(graphics);

        // Paint the expander
        if (menuItem.getMenu() != null) {
            Color color = (Color)(highlight ?
                menu.getStyles().get("highlightColor") : menu.getStyles().get("color"));
            graphics.setColor(color);
            graphics.setStroke(new BasicStroke(0));

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.translate(dataRenderer.getWidth() + (EXPANDER_SIZE - EXPANDER_ICON_SIZE) / 2,
                (height - EXPANDER_ICON_SIZE) / 2);

            int[] xPoints = {0, EXPANDER_ICON_SIZE, 0};
            int[] yPoints = {0, EXPANDER_ICON_SIZE / 2, EXPANDER_ICON_SIZE};
            graphics.fillPolygon(xPoints, yPoints, 3);
            graphics.drawPolygon(xPoints, yPoints, 3);
        }
    }

    public Image getCheckmarkImage() {
        return checkmarkImage;
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

        ApplicationContext.clearInterval(buttonPressTimeoutID);

        final Menu.Item menuItem = (Menu.Item)getComponent();
        if (menuItem.getMenu() != null) {
            buttonPressTimeoutID = ApplicationContext.setTimeout(new Runnable() {
                public void run() {
                    menuItem.press();
                }
            }, buttonPressInterval);
        }

        menuItem.requestFocus();
    }

    @Override
    public void mouseOut() {
        super.mouseOut();
        ApplicationContext.clearInterval(buttonPressTimeoutID);
    }

    @Override
    public boolean mouseDown(Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(button, x, y);
        ApplicationContext.clearInterval(buttonPressTimeoutID);

        return consumed;
    }

    @Override
    public void mouseClick(Mouse.Button button, int x, int y, int count) {
        super.mouseClick(button, x, y, count);

        Menu.Item menuItem = (Menu.Item)getComponent();
        menuItem.press();
    }

    @Override
    public boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(keyCode, keyLocation);

        ApplicationContext.clearInterval(buttonPressTimeoutID);

        Menu.Item menuItem = (Menu.Item)getComponent();
        Menu menu = menuItem.getMenu();

        if (keyCode == Keyboard.KeyCode.UP) {
            menuItem.transferFocus(Direction.BACKWARD);
            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.DOWN) {
            menuItem.transferFocus(Direction.FORWARD);
            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.LEFT) {
            Menu parentMenu = menuItem.getSection().getMenu();
            Menu.Item parentMenuItem = parentMenu.getItem();
            if (parentMenuItem != null) {
                parentMenuItem.requestFocus();
                consumed = true;
            }

            menuPopup.close();
        } else if (keyCode == Keyboard.KeyCode.RIGHT) {
            if (menu != null) {
                if (!menuPopup.isOpen()) {
                    menuItem.press();
                }

                menu.requestFocus();
                consumed = true;
            }
        } else if (keyCode == Keyboard.KeyCode.ENTER) {
            menuItem.press();
            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.TAB) {
            // No-op
        } else {
            consumed = super.keyPressed(keyCode, keyLocation);
        }

        return consumed;
    }

    @Override
    public boolean keyReleased(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyReleased(keyCode, keyLocation);

        Menu.Item menuItem = (Menu.Item)getComponent();

        if (keyCode == Keyboard.KeyCode.SPACE) {
            menuItem.press();
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

        repaintComponent();
    }

    public void buttonPressed(Button button) {
        Menu.Item menuItem = (Menu.Item)getComponent();
        Menu menu = menuItem.getMenu();

        if (menu != null
            && !menuPopup.isOpen()) {
            // Determine the popup's location and preferred size, relative
            // to the menu item
            Window window = menuItem.getWindow();

            if (window != null) {
                Display display = window.getDisplay();
                Point menuItemLocation = menuItem.mapPointToAncestor(display, getWidth(), 0);

                // TODO Ensure that the popup remains within the bounds of the display

                menuPopup.setLocation(menuItemLocation.x, menuItemLocation.y);
                menuPopup.open(menuItem);
            }
        }
    }

    public boolean previewStateChange(Button button, Button.State state) {
        return true;
    }

    public void stateChanged(Button button, Button.State previousState) {
        repaintComponent();
    }

    public void menuChanged(Menu.Item item, Menu previousMenu) {
        menuPopup.setMenu(item.getMenu());
        repaintComponent();
    }
}
