package pivot.wtk.skin.terra;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.ButtonStateListener;
import pivot.wtk.Component;
import pivot.wtk.Cursor;
import pivot.wtk.Dimensions;
import pivot.wtk.Menu;
import pivot.wtk.Mouse;
import pivot.wtk.Menu.Item;
import pivot.wtk.media.Image;
import pivot.wtk.skin.ButtonSkin;

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

    private Image checkmarkImage = new CheckmarkImage();

    // TODO Define expander image

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

        // TODO Add expander button width

        return dataRenderer.getPreferredWidth(height);
    }

    public int getPreferredHeight(int width) {
        Menu.Item menuItem = (Menu.Item)getComponent();

        Button.DataRenderer dataRenderer = menuItem.getDataRenderer();
        dataRenderer.render(menuItem.getButtonData(), menuItem, false);

        // TODO Include expander button height

        return dataRenderer.getPreferredHeight(width);
    }

    public Dimensions getPreferredSize() {
        Menu.Item menuItem = (Menu.Item)getComponent();

        Button.DataRenderer dataRenderer = menuItem.getDataRenderer();
        dataRenderer.render(menuItem.getButtonData(), menuItem, false);

        // TODO Include expander button width and height

        return dataRenderer.getPreferredSize();
    }

    public void layout() {
        // No-op
    }

    public void paint(Graphics2D graphics) {
        Menu.Item menuItem = (Menu.Item)getComponent();
        Menu menu = menuItem.getSection().getMenu();

        int width = getWidth();
        int height = getHeight();

        // Paint highlight state
        if (menuItem.isFocused()) {
            Color highlightBackgroundColor = (Color)menu.getStyles().get("highlightBackgroundColor");
            graphics.setColor(highlightBackgroundColor);
            graphics.fillRect(0, 0, width, height);
        }

        // TODO Include expander button

        Button.DataRenderer dataRenderer = menuItem.getDataRenderer();
        dataRenderer.render(menuItem.getButtonData(), menuItem, menuItem.isFocused());
        dataRenderer.setSize(width, height);

        dataRenderer.paint(graphics);
    }

    public Image getCheckmarkImage() {
        return checkmarkImage;
    }

    @Override
    public void mouseOver() {
        super.mouseOver();

        // TODO Start expand timer?

        Menu.Item menuItem = (Menu.Item)getComponent();
        menuItem.requestFocus();
    }

    @Override
    public void mouseClick(Mouse.Button button, int x, int y, int count) {
        Menu.Item menuItem = (Menu.Item)getComponent();
        menuItem.press();
    }

    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        // TODO Hide popup if disabled

        repaintComponent();
    }

    @Override
    public void focusedChanged(Component component, boolean temporary) {
        super.focusedChanged(component, temporary);

        // TODO Hide popup if focus was transferred to a component whose
        // window is not the popup

        repaintComponent();
    }

    public void buttonPressed(Button button) {
        // TODO Show/hide the popup
    }

    public boolean previewStateChange(Button button, Button.State state) {
        return true;
    }

    public void stateChanged(Button button, Button.State previousState) {
        repaintComponent();
    }

    public void menuChanged(Item item, Menu previousMenu) {
        repaintComponent();
    }
}
