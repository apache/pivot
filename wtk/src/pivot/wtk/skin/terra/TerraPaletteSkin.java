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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import pivot.collections.Dictionary;
import pivot.wtk.Bounds;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.Dimensions;
import pivot.wtk.Display;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Insets;
import pivot.wtk.Label;
import pivot.wtk.LinkButton;
import pivot.wtk.Mouse;
import pivot.wtk.Palette;
import pivot.wtk.Point;
import pivot.wtk.Theme;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.Window;
import pivot.wtk.WindowListener;
import pivot.wtk.effects.DropShadowDecorator;
import pivot.wtk.media.Image;
import pivot.wtk.skin.WindowSkin;

/**
 * Palette skin class.
 *
 * @author gbrown
 */
public class TerraPaletteSkin extends WindowSkin {
    /**
     * Close button image.
     *
     * @author gbrown
     */
    protected class CloseImage extends Image {
        public int getWidth() {
            return 6;
        }

        public int getHeight() {
            return 6;
        }

        public void paint(Graphics2D graphics) {
            graphics.setPaint(titleBarColor);
            graphics.setStroke(new BasicStroke(2));

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.drawLine(0, 0, 5, 5);
            graphics.drawLine(0, 5, 5, 0);
        }
    }

    private class MoveMouseHandler implements ComponentMouseListener, ComponentMouseButtonListener {
        public boolean mouseMove(Component component, int x, int y) {
            Display display = (Display)component;

            // Pretend that the mouse can't move off screen (off the display)
            x = Math.min(Math.max(x, 0), display.getWidth() - 1);
            y = Math.min(Math.max(y, 0), display.getHeight() - 1);

            // Calculate the would-be new window location
            int windowX = x - dragOffset.x;
            int windowY = y - dragOffset.y;

            Window window = (Window)getComponent();
            window.setLocation(windowX, windowY);

            return false;
        }

        public void mouseOver(Component component) {
            // No-op
        }

        public void mouseOut(Component component) {
            // No-op
        }

        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            assert (component instanceof Display);
            component.getComponentMouseListeners().remove(this);
            component.getComponentMouseButtonListeners().remove(this);

            return false;
        }

        public boolean mouseClick(Component component, Mouse.Button button, int x, int y,
            int count) {
            return false;
        }
    }

    private Image closeImage = new CloseImage();

    private FlowPane titleBarFlowPane = new FlowPane();
    private FlowPane titleFlowPane = new FlowPane();
    private FlowPane buttonFlowPane = new FlowPane();

    private Label titleLabel = new Label();
    private LinkButton closeButton = new LinkButton(closeImage);

    private DropShadowDecorator dropShadowDecorator = null;

    private Point dragOffset = null;
    private MoveMouseHandler moveMouseHandler = new MoveMouseHandler();

    private Insets padding = new Insets(4);

    private WindowListener ownerListener = new WindowListener() {
        public void titleChanged(Window window, String previousTitle) {
            // No-op
        }

        public void iconChanged(Window window, Image previousIcon) {
            // No-op
        }

        public void contentChanged(Window window, Component previousContent) {
            // No-op
        }

        public void ownerChanged(Window window, Window previousOwner) {
            // No-op
        }

        public void activeChanged(Window window) {
            getComponent().setDisplayable(window.isActive());
        }

        public void maximizedChanged(Window window) {
            // No-op
        }
    };

    private Color titleBarColor;
    private Color titleBarBackgroundColor;
    private Color titleBarBorderColor;
    private Color contentBorderColor;

    // Derived colors
    private Color titleBarBevelColor;
    private Color contentBevelColor;

    public TerraPaletteSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(10));

        titleBarColor = theme.getColor(4);
        titleBarBackgroundColor = theme.getColor(16);
        titleBarBorderColor = theme.getColor(13);
        contentBorderColor = theme.getColor(7);

        // Set the derived colors
        titleBarBevelColor = TerraTheme.brighten(titleBarBackgroundColor);

        // The title bar flow pane contains two nested flow panes: one for
        // the title contents and the other for the buttons
        titleBarFlowPane.add(titleFlowPane);
        titleBarFlowPane.add(buttonFlowPane);

        titleBarFlowPane.getStyles().put("horizontalAlignment", HorizontalAlignment.JUSTIFY);
        titleBarFlowPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        titleBarFlowPane.getStyles().put("padding", new Insets(2, 3, 2, 3));

        // Initialize the title flow pane
        titleFlowPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        titleFlowPane.add(titleLabel);

        Font font = theme.getFont();
        titleLabel.getStyles().put("fontBold", true);
        titleLabel.getStyles().put("fontSize", Math.round(font.getSize2D() * 0.8f));
        titleLabel.getStyles().put("color", titleBarColor);

        // Initialize the button flow pane
        buttonFlowPane.getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);
        buttonFlowPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        buttonFlowPane.add(closeButton);

        closeButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Palette palette = (Palette)getComponent();
                palette.close();
            }
        });
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Palette palette = (Palette)component;
        palette.add(titleBarFlowPane);

        // Attach the drop-shadow decorator
        dropShadowDecorator = new DropShadowDecorator(3, 3, 3);
        palette.getDecorators().add(dropShadowDecorator);

        ownerChanged(palette, null);
        titleChanged(palette, null);
    }

    @Override
    public void uninstall() {
        Palette palette = (Palette)getComponent();
        palette.remove(titleBarFlowPane);

        // Detach the drop shadow decorator
        palette.getDecorators().remove(dropShadowDecorator);
        dropShadowDecorator = null;

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        Palette palette = (Palette)getComponent();
        Component content = palette.getContent();

        Dimensions preferredTitleBarSize = titleBarFlowPane.getPreferredSize();
        preferredWidth = preferredTitleBarSize.width;

        if (content != null
            && content.isDisplayable()) {
            if (height != -1) {
                height = Math.max(height - preferredTitleBarSize.height - 4 -
                    padding.top - padding.bottom, 0);
            }

            preferredWidth = Math.max(preferredWidth,
                content.getPreferredWidth(height));
        }

        preferredWidth += (padding.left + padding.right) + 2;

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        Palette palette = (Palette)getComponent();
        Component content = palette.getContent();

        if (width != -1) {
            width = Math.max(width - 2, 0);
        }

        preferredHeight = titleBarFlowPane.getPreferredHeight(width);

        if (content != null
            && content.isDisplayable()) {
            if (width != -1) {
                width = Math.max(width - padding.left - padding.right, 0);
            }

            preferredHeight += content.getPreferredHeight(width);
        }

        preferredHeight += (padding.top + padding.bottom) + 4;

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        Palette palette = (Palette)getComponent();
        Component content = palette.getContent();

        Dimensions preferredTitleBarSize = titleBarFlowPane.getPreferredSize();

        preferredWidth = preferredTitleBarSize.width;
        preferredHeight = preferredTitleBarSize.height;

        if (content != null
            && content.isDisplayable()) {
            Dimensions preferredContentSize = content.getPreferredSize();

            preferredWidth = Math.max(preferredWidth, preferredContentSize.width);
            preferredHeight += preferredContentSize.height;
        }

        preferredWidth += (padding.left + padding.right) + 2;
        preferredHeight += (padding.top + padding.bottom) + 4;

        return new Dimensions(preferredWidth, preferredHeight);
    }

    public void layout() {
        Palette palette = (Palette)getComponent();

        int width = getWidth();
        int height = getHeight();

        // Size/position title bar
        titleBarFlowPane.setLocation(1, 1);
        titleBarFlowPane.setSize(Math.max(width - 2, 0),
            Math.max(titleBarFlowPane.getPreferredHeight(width - 2), 0));

        // Size/position content
        Component content = palette.getContent();

        if (content != null) {
            if (content.isDisplayable()) {
                content.setVisible(true);

                content.setLocation(padding.left + 1,
                    titleBarFlowPane.getHeight() + padding.top + 3);

                int contentWidth = Math.max(width - (padding.left + padding.right + 2), 0);
                int contentHeight = Math.max(height - (titleBarFlowPane.getHeight()
                    + padding.top + padding.bottom + 4), 0);

                content.setSize(contentWidth, contentHeight);
            } else {
                content.setVisible(false);
            }
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        // Call the base class to paint the background
        super.paint(graphics);

        int width = getWidth();
        int height = getHeight();
        int titleBarHeight = titleBarFlowPane.getHeight();

        graphics.setStroke(new BasicStroke());

        // Draw the title area
        graphics.setPaint(new GradientPaint(width / 2, 0, titleBarBevelColor,
            width / 2, titleBarHeight + 1, titleBarBackgroundColor));
        graphics.fillRect(0, 0, width, titleBarHeight + 1);

        // Draw the border
        graphics.setPaint(titleBarBorderColor);
        graphics.drawRect(0, 0, width - 1, titleBarHeight + 1);
        // Draw the content area
        Bounds contentAreaRectangle = new Bounds(0, titleBarHeight + 2,
            width - 1, height - (titleBarHeight + 3));
        graphics.setPaint(contentBorderColor);
        graphics.drawRect(contentAreaRectangle.x, contentAreaRectangle.y,
            contentAreaRectangle.width, contentAreaRectangle.height);

        graphics.setPaint(contentBevelColor);
        graphics.drawLine(contentAreaRectangle.x + 1, contentAreaRectangle.y + 1,
            contentAreaRectangle.width - 1, contentAreaRectangle.y + 1);
    }

    @Override
    public void setBackgroundColor(Color backgroundColor) {
        super.setBackgroundColor(backgroundColor);
        contentBevelColor = TerraTheme.brighten(backgroundColor);
    }

    public Insets getPadding() {
        return padding;
    }

    public void setPadding(Insets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        this.padding = padding;
        invalidateComponent();
    }

    public final void setPadding(Dictionary<String, ?> padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(new Insets(padding));
    }

    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(padding.intValue());
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        Window window = (Window)getComponent();
        boolean maximized = window.isMaximized();

        if (button == Mouse.Button.LEFT
            && !maximized) {
            Bounds titleBarBounds = titleBarFlowPane.getBounds();

            if (titleBarBounds.contains(x, y)) {
                dragOffset = new Point(x, y);

                Display display = window.getDisplay();
                display.getComponentMouseListeners().add(moveMouseHandler);
                display.getComponentMouseButtonListeners().add(moveMouseHandler);
            }
        }

        return consumed;
    }

    @Override
    public void ownerChanged(Window window, Window previousOwner) {
        super.ownerChanged(window, previousOwner);

        if (previousOwner != null) {
            previousOwner.getWindowListeners().remove(ownerListener);
        }

        Window owner = window.getOwner();
        if (owner != null) {
            owner.getWindowListeners().add(ownerListener);
        }
    }

    @Override
    public void titleChanged(Window window, String previousTitle) {
        super.titleChanged(window, previousTitle);

        String title = window.getTitle();
        titleLabel.setDisplayable(title != null);
        titleLabel.setText(title);
    }
}
