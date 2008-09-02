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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import pivot.collections.Dictionary;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Cursor;
import pivot.wtk.Dimensions;
import pivot.wtk.Display;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.ImageView;
import pivot.wtk.Insets;
import pivot.wtk.Label;
import pivot.wtk.Mouse;
import pivot.wtk.Point;
import pivot.wtk.PushButton;
import pivot.wtk.Bounds;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.Window;
import pivot.wtk.effects.DropShadowDecorator;
import pivot.wtk.media.Image;
import pivot.wtk.skin.WindowSkin;

/**
 * Abstract base class for Frame and Dialog skins.
 *
 * @author gbrown
 */
public abstract class AbstractFrameSkin extends WindowSkin {
    public static class FrameButton extends PushButton {
        public FrameButton(Object buttonData) {
            super(buttonData);

            installSkin(FrameButton.class);
        }
    }

    public static class FrameButtonSkin extends PushButtonSkin {
        public void install(Component component) {
            validateComponentType(component, FrameButton.class);

            super.install(component);
        }

        @Override
        public boolean isFocusable() {
            return false;
        }

        @Override
        public boolean mouseDown(Mouse.Button button, int x, int y) {
            super.mouseDown(button, x, y);
            return true;
        }

        @Override
        public boolean mouseUp(Mouse.Button button, int x, int y) {
            super.mouseUp(button, x, y);
            return true;
        }
    }

    protected abstract class ButtonImage extends ImageAsset {
        public int getPreferredWidth(int height) {
            return 8;
        }

        public int getPreferredHeight(int width) {
            return 8;
        }

        public Dimensions getPreferredSize() {
            return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
        }
    }

    protected class MinimizeImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            Window window = (Window)getComponent();
            graphics.setPaint(window.isActive() ? TITLE_BAR_COLOR : INACTIVE_TITLE_BAR_COLOR);
            graphics.fill(new Rectangle2D.Double(0, 6, 8, 2));
        }
    }

    protected class MaximizeImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            Window window = (Window)getComponent();
            graphics.setPaint(window.isActive() ? TITLE_BAR_COLOR : INACTIVE_TITLE_BAR_COLOR);
            graphics.fill(new Rectangle2D.Double(0, 0, 8, 8));

            graphics.setPaint(window.isActive() ? TITLE_BAR_BACKGROUND_COLOR : INACTIVE_TITLE_BAR_BACKGROUND_COLOR);
            graphics.fill(new Rectangle2D.Double(2, 2, 4, 4));
        }
    }

    protected class RestoreImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            Window window = (Window)getComponent();
            graphics.setPaint(window.isActive() ?
                TITLE_BAR_COLOR : INACTIVE_TITLE_BAR_COLOR);
            graphics.fill(new Rectangle2D.Double(1, 1, 6, 6));

            graphics.setPaint(window.isActive() ?
                TITLE_BAR_BACKGROUND_COLOR : INACTIVE_TITLE_BAR_BACKGROUND_COLOR);
            graphics.fill(new Rectangle2D.Double(3, 3, 2, 2));
        }
    }

    protected class CloseImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            Window window = (Window)getComponent();
            graphics.setPaint(window.isActive() ?
                TITLE_BAR_COLOR : INACTIVE_TITLE_BAR_COLOR);
            graphics.setStroke(new BasicStroke(2));

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.draw(new Line2D.Double(0, 0, 7, 7));
            graphics.draw(new Line2D.Double(0, 7, 7, 0));
        }
    }

    protected class ResizeImage extends ImageAsset {
        public int getPreferredWidth(int height) {
            return 5;
        }

        public int getPreferredHeight(int width) {
            return 5;
        }

        public Dimensions getPreferredSize() {
            return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
        }

        public void paint(Graphics2D graphics) {
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));

            graphics.setPaint(Color.BLACK);
            graphics.fillRect(3, 0, 2, 1);
            graphics.fillRect(0, 3, 2, 1);
            graphics.fillRect(3, 3, 2, 1);

            graphics.setPaint(CONTENT_BORDER_COLOR);
            graphics.fillRect(3, 1, 2, 1);
            graphics.fillRect(0, 4, 2, 1);
            graphics.fillRect(3, 4, 2, 1);
        }
    }

    private class MoveMouseHandler implements ComponentMouseListener, ComponentMouseButtonListener {
        public void mouseMove(Component component, int x, int y) {
            Display display = (Display)component;

            // Pretend that the mouse can't move off screen (off the display)
            x = Math.min(Math.max(x, 0), display.getWidth() - 1);
            y = Math.min(Math.max(y, 0), display.getHeight() - 1);

            // Calculate the would-be new window location
            int windowX = x - dragOffset.x;
            int windowY = y - dragOffset.y;

            Window window = (Window)getComponent();
            window.setLocation(windowX, windowY);
        }

        public void mouseOver(Component component) {
        }

        public void mouseOut(Component component) {
        }

        public void mouseDown(Component component, Mouse.Button button, int x, int y) {
        }

        public void mouseUp(Component component, Mouse.Button button, int x, int y) {
            assert (component instanceof Display);
            component.getComponentMouseListeners().remove(this);
            component.getComponentMouseButtonListeners().remove(this);
        }

        public void mouseClick(Component component, Mouse.Button button, int x, int y,
            int count) {
        }
    }

    private class ResizeMouseHandler implements ComponentMouseListener, ComponentMouseButtonListener {
        public void mouseMove(Component component, int x, int y) {
            Display display = (Display)component;

            // Pretend that the mouse can't move off screen (off the display)
            x = Math.min(Math.max(x, 0), display.getWidth() - 1);
            y = Math.min(Math.max(y, 0), display.getHeight() - 1);

            // Calculate the would-be new window size
            Window window = (Window)getComponent();

            int preferredWidth = -1;
            int preferredHeight = -1;

            if (window.isPreferredWidthSet()) {
                preferredWidth = Math.max(x - window.getX() + dragOffset.x,
                    titleBarFlowPane.getPreferredWidth(-1) + 2);
            }

            if (window.isPreferredHeightSet()) {
                preferredHeight = Math.max(y - window.getY() + dragOffset.y,
                    titleBarFlowPane.getHeight() + resizeHandle.getHeight() + 7);
            }

            window.setPreferredSize(preferredWidth, preferredHeight);
        }

        public void mouseOver(Component component) {
        }

        public void mouseOut(Component component) {
        }

        public void mouseDown(Component component, Mouse.Button button, int x, int y) {
        }

        public void mouseUp(Component component, Mouse.Button button, int x, int y) {
            assert (component instanceof Display);
            component.getComponentMouseListeners().remove(this);
            component.getComponentMouseButtonListeners().remove(this);
        }

        public void mouseClick(Component component, Mouse.Button button, int x, int y,
            int count) {
        }
    }

    private Image minimizeImage = new MinimizeImage();
    private Image maximizeImage = new MaximizeImage();
    private Image restoreImage = new RestoreImage();
    private Image closeImage = new CloseImage();
    private Image resizeImage = new ResizeImage();

    private FlowPane titleBarFlowPane = new FlowPane();
    private FlowPane titleFlowPane = new FlowPane();
    private FlowPane frameButtonFlowPane = new FlowPane();

    private ImageView iconImageView = new ImageView();
    private Label titleLabel = new Label();

    private FrameButton minimizeButton = null;
    private FrameButton maximizeButton = null;
    private FrameButton closeButton = null;
    private ImageView resizeHandle = new ImageView(resizeImage);

    private DropShadowDecorator dropShadowDecorator = null;

    private Point dragOffset = null;
    private MoveMouseHandler moveMouseHandler = new MoveMouseHandler();
    private ResizeMouseHandler resizeMouseHandler = new ResizeMouseHandler();

    private Point restoreLocation = null;

    private Insets padding = new Insets(8);
    private boolean resizable = true;

    private static final Font TITLE_BAR_FONT = new Font("Verdana", Font.BOLD, 11);
    private static final Color TITLE_BAR_COLOR = Color.WHITE;
    private static final Color TITLE_BAR_BACKGROUND_COLOR = new Color(0x3c, 0x77, 0xb2);
    private static final Color TITLE_BAR_BEVEL_COLOR = new Color(0x45, 0x89, 0xcc);
    private static final Color TITLE_BAR_PRESSED_BEVEL_COLOR = new Color(0x34, 0x66, 0x99);
    private static final Color TITLE_BAR_BORDER_COLOR = new Color(0x2c, 0x56, 0x80);
    private static final Color INACTIVE_TITLE_BAR_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Color INACTIVE_TITLE_BAR_BACKGROUND_COLOR = new Color(0xcc, 0xca, 0xc2);
    private static final Color INACTIVE_TITLE_BAR_BEVEL_COLOR = new Color(0xe6, 0xe3, 0xda);
    private static final Color INACTIVE_TITLE_BAR_PRESSED_BEVEL_COLOR = new Color(0xCC, 0xCA, 0xC2);
    private static final Color INACTIVE_TITLE_BAR_BORDER_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Color CONTENT_BORDER_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Color CONTENT_BEVEL_COLOR = new Color(0xe6, 0xe3, 0xda);
    private static final float INACTIVE_ICON_OPACITY = 0.5f;

    public AbstractFrameSkin() {
        setBackgroundColor(new Color(0xcc, 0xca, 0xc2));

        // The title bar flow pane contains two nested flow panes: one for
        // the title contents and the other for the buttons
        titleBarFlowPane.add(titleFlowPane);
        titleBarFlowPane.add(frameButtonFlowPane);

        titleBarFlowPane.getStyles().put("horizontalAlignment", HorizontalAlignment.JUSTIFY);
        titleBarFlowPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        titleBarFlowPane.getStyles().put("padding", new Insets(2));

        // Initialize the title flow pane
        titleFlowPane.add(iconImageView);
        titleFlowPane.add(titleLabel);
        titleFlowPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

        titleLabel.getStyles().put("font", TITLE_BAR_FONT);
        iconImageView.getStyles().put("backgroundColor", null);

        // Initialize the button flow pane
        frameButtonFlowPane.getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);
        frameButtonFlowPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
    }

    @Override
    public void install(Component component) {
        validateComponentType(component, Window.class);

        super.install(component);

        Window window = (Window)component;

        // Attach the drop-shadow decorator
        dropShadowDecorator = new DropShadowDecorator();
        window.getDecorators().add(dropShadowDecorator);

        window.add(titleBarFlowPane);

        // Create the frame buttons
        minimizeButton = new FrameButton(minimizeImage);
        maximizeButton = new FrameButton(maximizeImage);
        closeButton = new FrameButton(closeImage);

        frameButtonFlowPane.add(minimizeButton);
        frameButtonFlowPane.add(maximizeButton);
        frameButtonFlowPane.add(closeButton);

        ButtonPressListener buttonPressListener = new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Window window = (Window)getComponent();

                if (button == minimizeButton) {
                    window.setDisplayable(false);
                } else if (button == maximizeButton) {
                    window.setMaximized(!window.isMaximized());
                } else if (button == closeButton) {
                    window.close();
                }
            }
        };

        minimizeButton.getButtonPressListeners().add(buttonPressListener);
        maximizeButton.getButtonPressListeners().add(buttonPressListener);
        closeButton.getButtonPressListeners().add(buttonPressListener);

        resizeHandle.setCursor(Cursor.RESIZE_SOUTH_EAST);
        window.add(resizeHandle);

        iconChanged(window, null);
        titleChanged(window, null);
        activeChanged(window);

        updateMaximizedState();
    }

    @Override
    public void uninstall() {
        Window window = (Window)getComponent();

        // Detach the drop shadow decorator
        window.getDecorators().remove(dropShadowDecorator);
        dropShadowDecorator = null;

        window.remove(titleBarFlowPane);

        frameButtonFlowPane.remove(minimizeButton);
        frameButtonFlowPane.remove(maximizeButton);
        frameButtonFlowPane.remove(closeButton);

        minimizeButton = null;
        maximizeButton = null;
        closeButton = null;

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        Window window = (Window)getComponent();
        Component content = window.getContent();

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

        Window window = (Window)getComponent();
        Component content = window.getContent();

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

        Window window = (Window)getComponent();
        Component content = window.getContent();

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
        Window window = (Window)getComponent();

        int width = getWidth();
        int height = getHeight();

        // Size/position title bar
        titleBarFlowPane.setLocation(1, 1);
        titleBarFlowPane.setSize(Math.max(width - 2, 0),
            Math.max(titleBarFlowPane.getPreferredHeight(width - 2), 0));

        // Size/position resize handle
        resizeHandle.setSize(resizeHandle.getPreferredSize());
        resizeHandle.setLocation(width - resizeHandle.getWidth() - 2,
            height - resizeHandle.getHeight() - 2);

        boolean maximized = window.isMaximized();
        resizeHandle.setVisible(resizable
            && !maximized
            && (window.isPreferredWidthSet()
                || window.isPreferredHeightSet()));

        // Size/position content
        Component content = window.getContent();

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

        Window window = (Window)getComponent();

        int width = getWidth();
        int height = getHeight();
        Dimensions titleBarSize = titleBarFlowPane.getSize();

        // Draw the borders with a 1px solid stroke
        graphics.setStroke(new BasicStroke());

        // Draw the title area
        Bounds titleBarRectangle = new Bounds(0, 0, width - 1, titleBarSize.height + 1);
        graphics.setPaint(window.isActive() ?
            TITLE_BAR_BACKGROUND_COLOR : INACTIVE_TITLE_BAR_BACKGROUND_COLOR);
        graphics.fillRect(titleBarRectangle.x, titleBarRectangle.y,
            titleBarRectangle.width, titleBarRectangle.height);

        graphics.setPaint(window.isActive() ?
            TITLE_BAR_BORDER_COLOR : INACTIVE_TITLE_BAR_BORDER_COLOR);
        graphics.drawRect(titleBarRectangle.x, titleBarRectangle.y,
            titleBarRectangle.width, titleBarRectangle.height);

        graphics.setPaint(window.isActive() ?
            TITLE_BAR_BEVEL_COLOR : INACTIVE_TITLE_BAR_BEVEL_COLOR);
        graphics.drawLine(titleBarRectangle.x + 1, titleBarRectangle.y + 1,
            titleBarRectangle.width - 1, titleBarRectangle.y + 1);

        // Draw the content area
        Bounds contentAreaRectangle = new Bounds(0, titleBarSize.height + 2,
            width - 1, height - (titleBarSize.height + 3));
        graphics.setPaint(CONTENT_BORDER_COLOR);
        graphics.drawRect(contentAreaRectangle.x, contentAreaRectangle.y,
            contentAreaRectangle.width, contentAreaRectangle.height);

        Line2D contentAreaBevelLine = new Line2D.Double(contentAreaRectangle.x + 1, contentAreaRectangle.y + 1,
            contentAreaRectangle.width - 1, contentAreaRectangle.y + 1);
        graphics.setPaint(CONTENT_BEVEL_COLOR);
        graphics.draw(contentAreaBevelLine);
    }

    public boolean getShowMinimizeButton() {
        return minimizeButton.isDisplayable();
    }

    public void setShowMinimizeButton(boolean showMinimizeButton) {
        minimizeButton.setDisplayable(showMinimizeButton);
    }

    public boolean getShowMaximizeButton() {
        return maximizeButton.isDisplayable();
    }

    public void setShowMaximizeButton(boolean showMaximizeButton) {
        maximizeButton.setDisplayable(showMaximizeButton);
    }

    public boolean getShowCloseButton() {
        return closeButton.isDisplayable();
    }

    public void setShowCloseButton(boolean showCloseButton) {
        closeButton.setDisplayable(showCloseButton);
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

    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
        invalidateComponent();
    }

    private void updateMaximizedState() {
        Window window = (Window)getComponent();
        boolean maximized = window.isMaximized();

        if (!maximized) {
            maximizeButton.setButtonData(maximizeImage);

            if (restoreLocation != null) {
                window.setLocation(restoreLocation.x, restoreLocation.y);
            }
        } else {
            maximizeButton.setButtonData(restoreImage);
            restoreLocation = window.getLocation();
        }
    }

    @Override
    public boolean mouseDown(Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(button, x, y);

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
            } else {
                Bounds resizeHandleBounds = resizeHandle.getBounds();

                if (resizeHandleBounds.contains(x, y)) {
                    dragOffset = new Point(getWidth() - x, getHeight() - y);

                    Display display = window.getDisplay();
                    display.getComponentMouseListeners().add(resizeMouseHandler);
                    display.getComponentMouseButtonListeners().add(resizeMouseHandler);
                }
            }
        }

        return consumed;
    }

    @Override
    public void titleChanged(Window window, String previousTitle) {
        String title = window.getTitle();
        titleLabel.setDisplayable(title != null);
        titleLabel.setText(title);
    }

    @Override
    public void iconChanged(Window window, Image previousIcon) {
        Image icon = window.getIcon();
        iconImageView.setDisplayable(icon != null);
        iconImageView.setImage(icon);
    }

    @Override
    public void activeChanged(Window window) {
        boolean active = window.isActive();

        titleLabel.getStyles().put("color", active ?
            TITLE_BAR_COLOR : INACTIVE_TITLE_BAR_COLOR);
        iconImageView.getStyles().put("opacity", active ?
            1.0f : INACTIVE_ICON_OPACITY);

        updateButtonStyles(minimizeButton, active);
        updateButtonStyles(maximizeButton, active);
        updateButtonStyles(closeButton, active);

        repaintComponent();
    }

    private void updateButtonStyles(FrameButton frameButton, boolean active) {
        frameButton.getStyles().put("color", active ?
            TITLE_BAR_COLOR : INACTIVE_TITLE_BAR_COLOR);
        frameButton.getStyles().put("backgroundColor", active ?
            TITLE_BAR_BACKGROUND_COLOR : INACTIVE_TITLE_BAR_BACKGROUND_COLOR);
        frameButton.getStyles().put("bevelColor", active ?
            TITLE_BAR_BEVEL_COLOR : INACTIVE_TITLE_BAR_BEVEL_COLOR);
        frameButton.getStyles().put("pressedBevelColor", active ?
            TITLE_BAR_PRESSED_BEVEL_COLOR : INACTIVE_TITLE_BAR_PRESSED_BEVEL_COLOR);
        frameButton.getStyles().put("borderColor", active ?
            TITLE_BAR_BORDER_COLOR : INACTIVE_TITLE_BAR_BORDER_COLOR);
    }

    @Override
    public void maximizedChanged(Window window) {
        updateMaximizedState();
    }
}
