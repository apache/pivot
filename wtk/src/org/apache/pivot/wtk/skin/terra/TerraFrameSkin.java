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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Cursor;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.FrameListener;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.effects.DropShadowDecorator;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.WindowSkin;


/**
 * Frame skin.
 *
 */
public class TerraFrameSkin extends WindowSkin implements FrameListener {
    /**
     * Frame button.
     *
     */
    public static class FrameButton extends PushButton {
        public FrameButton(Object buttonData) {
            super(buttonData);

            installSkin(FrameButton.class);
        }
    }

    /**
     * Frame button skin.
     *
     */
    public static class FrameButtonSkin extends TerraPushButtonSkin {
        public FrameButtonSkin() {
            setPadding(3);
        }

        @Override
        public boolean isFocusable() {
            return false;
        }

        @Override
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            super.mouseDown(component, button, x, y);
            return true;
        }
    }

    /**
     * Abstract base class for frame button images.
     *
     */
    protected abstract class ButtonImage extends Image {
        public int getWidth() {
            return 8;
        }

        public int getHeight() {
            return 8;
        }
    }

    /**
     * Minimize button image.
     *
     */
    protected class MinimizeImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            Frame frame = (Frame)getComponent();
            graphics.setPaint(frame.isActive() ? titleBarColor : inactiveTitleBarColor);
            graphics.fillRect(0, 6, 8, 2);
        }
    }

    /**
     * Maximize button image.
     *
     */
    protected class MaximizeImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            Frame frame = (Frame)getComponent();
            graphics.setPaint(frame.isActive() ? titleBarColor : inactiveTitleBarColor);
            graphics.fillRect(0, 0, 8, 8);

            graphics.setPaint(frame.isActive() ? titleBarBackgroundColor : inactiveTitleBarBackgroundColor);
            graphics.fillRect(2, 2, 4, 4);
        }
    }

    /**
     * Restore button image.
     *
     */
    protected class RestoreImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            Frame frame = (Frame)getComponent();
            graphics.setPaint(frame.isActive() ?
                titleBarColor : inactiveTitleBarColor);
            graphics.fillRect(1, 1, 6, 6);

            graphics.setPaint(frame.isActive() ?
                titleBarBackgroundColor : inactiveTitleBarBackgroundColor);
            graphics.fillRect(3, 3, 2, 2);
        }
    }

    /**
     * Close button image.
     *
     */
    protected class CloseImage extends ButtonImage {
        public void paint(Graphics2D graphics) {
            Frame frame = (Frame)getComponent();
            graphics.setPaint(frame.isActive() ?
                titleBarColor : inactiveTitleBarColor);
            graphics.setStroke(new BasicStroke(2));

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.draw(new Line2D.Double(0.5, 0.5, 7.5, 7.5));
            graphics.draw(new Line2D.Double(0.5, 7.5, 7.5, 0.5));
        }
    }

    /**
     * Resize button image.
     *
     */
    protected class ResizeImage extends Image {
        public static final int ALPHA = 64;

        public int getWidth() {
            return 5;
        }

        public int getHeight() {
            return 5;
        }

        public void paint(Graphics2D graphics) {
            graphics.setPaint(new Color(0, 0, 0, ALPHA));
            graphics.fillRect(3, 0, 2, 1);
            graphics.fillRect(0, 3, 2, 1);
            graphics.fillRect(3, 3, 2, 1);

            graphics.setPaint(new Color(contentBorderColor.getRed(),
                contentBorderColor.getGreen(), contentBorderColor.getBlue(),
                ALPHA));
            graphics.fillRect(3, 1, 2, 1);
            graphics.fillRect(0, 4, 2, 1);
            graphics.fillRect(3, 4, 2, 1);
        }
    }

    private Image minimizeImage = new MinimizeImage();
    private Image maximizeImage = new MaximizeImage();
    private Image restoreImage = new RestoreImage();
    private Image closeImage = new CloseImage();
    private Image resizeImage = new ResizeImage();

    private TablePane titleBarTablePane = new TablePane();
    private BoxPane titleBoxPane = new BoxPane();
    private BoxPane buttonBoxPane = new BoxPane();

    private ImageView iconImageView = new ImageView();
    private Label titleLabel = new Label();

    private FrameButton minimizeButton = null;
    private FrameButton maximizeButton = null;
    private FrameButton closeButton = null;
    private ImageView resizeHandle = new ImageView(resizeImage);

    private DropShadowDecorator dropShadowDecorator = null;

    private Point dragOffset = null;
    private Point resizeOffset = null;

    private Point restoreLocation = null;

    private Color titleBarColor;
    private Color titleBarBackgroundColor;
    private Color titleBarBorderColor;
    private Color inactiveTitleBarColor;
    private Color inactiveTitleBarBackgroundColor;
    private Color inactiveTitleBarBorderColor;
    private Color contentBorderColor;
    private Insets padding;
    private boolean resizable;

    // Derived colors
    private Color titleBarBevelColor;
    private Color inactiveTitleBarBevelColor;
    private Color contentBevelColor;

    private static final float INACTIVE_ICON_OPACITY = 0.5f;

    public TerraFrameSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(10));

        titleBarColor = theme.getColor(4);
        titleBarBackgroundColor = theme.getColor(16);
        titleBarBorderColor = theme.getColor(13);
        inactiveTitleBarColor = theme.getColor(7);
        inactiveTitleBarBackgroundColor = theme.getColor(9);
        inactiveTitleBarBorderColor = theme.getColor(7);
        contentBorderColor = theme.getColor(7);
        padding = new Insets(8);
        resizable = true;

        // Set the derived colors
        titleBarBevelColor = TerraTheme.brighten(titleBarBackgroundColor);
        inactiveTitleBarBevelColor = TerraTheme.brighten(inactiveTitleBarBackgroundColor);

        // The title bar table pane contains two nested box panes: one for
        // the title contents and the other for the buttons
        titleBarTablePane.getColumns().add(new TablePane.Column(1, true));
        titleBarTablePane.getColumns().add(new TablePane.Column(-1));

        TablePane.Row titleRow = new TablePane.Row(-1);
        titleBarTablePane.getRows().add(titleRow);

        titleRow.add(titleBoxPane);
        titleRow.add(buttonBoxPane);

        titleBarTablePane.getStyles().put("padding", new Insets(2));

        // Initialize the title box pane
        titleBoxPane.add(iconImageView);
        titleBoxPane.add(titleLabel);
        titleBoxPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        titleBoxPane.getStyles().put("padding", new Insets(0, 0, 0, 2));

        titleLabel.getStyles().put("fontBold", true);

        iconImageView.setPreferredSize(16, 16);
        iconImageView.getStyles().put("fill", true);
        iconImageView.getStyles().put("backgroundColor", null);

        // Initialize the button box pane
        buttonBoxPane.getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);
        buttonBoxPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Frame frame = (Frame)getComponent();

        // Attach the drop-shadow decorator
        dropShadowDecorator = new DropShadowDecorator();
        frame.getDecorators().add(dropShadowDecorator);

        frame.add(titleBarTablePane);

        // Create the frame buttons
        minimizeButton = new FrameButton(minimizeImage);
        maximizeButton = new FrameButton(maximizeImage);
        closeButton = new FrameButton(closeImage);

        buttonBoxPane.add(minimizeButton);
        buttonBoxPane.add(maximizeButton);
        buttonBoxPane.add(closeButton);

        ButtonPressListener buttonPressListener = new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Frame frame = (Frame)getComponent();

                if (button == minimizeButton) {
                    frame.setVisible(false);
                } else if (button == maximizeButton) {
                    frame.setMaximized(!frame.isMaximized());
                } else if (button == closeButton) {
                    frame.close();
                }
            }
        };

        minimizeButton.getButtonPressListeners().add(buttonPressListener);
        maximizeButton.getButtonPressListeners().add(buttonPressListener);
        closeButton.getButtonPressListeners().add(buttonPressListener);

        frame.add(resizeHandle);

        iconChanged(frame, null);
        titleChanged(frame, null);
        activeChanged(frame, null);

        updateMaximizedState();
    }

    @Override
    public void uninstall() {
        Frame frame = (Frame)getComponent();

        // Detach the drop shadow decorator
        frame.getDecorators().remove(dropShadowDecorator);
        dropShadowDecorator = null;

        frame.remove(titleBarTablePane);

        buttonBoxPane.remove(minimizeButton);
        buttonBoxPane.remove(maximizeButton);
        buttonBoxPane.remove(closeButton);

        minimizeButton = null;
        maximizeButton = null;
        closeButton = null;

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        Dimensions preferredTitleBarSize = titleBarTablePane.getPreferredSize();
        preferredWidth = preferredTitleBarSize.width;

        Frame frame = (Frame)getComponent();

        MenuBar menuBar = frame.getMenuBar();
        if (menuBar != null) {
            preferredWidth = Math.max(preferredWidth, menuBar.getPreferredWidth());
        }

        Component content = frame.getContent();
        if (content != null) {
            if (height != -1) {
                height = Math.max(height - preferredTitleBarSize.height - 5 -
                    padding.top - padding.bottom, 0);
            }

            preferredWidth = Math.max(preferredWidth, content.getPreferredWidth(height));
        }

        preferredWidth += (padding.left + padding.right) + 2;

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        if (width != -1) {
            width = Math.max(width - 2, 0);
        }

        preferredHeight = titleBarTablePane.getPreferredHeight(width);

        Frame frame = (Frame)getComponent();

        MenuBar menuBar = frame.getMenuBar();
        if (menuBar != null) {
            preferredHeight += menuBar.getPreferredHeight();
        }

        Component content = frame.getContent();
        if (content != null) {
            if (width != -1) {
                width = Math.max(width - padding.left - padding.right, 0);
            }

            preferredHeight += content.getPreferredHeight(width);
        }

        preferredHeight += (padding.top + padding.bottom) + 5;

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        Dimensions preferredTitleBarSize = titleBarTablePane.getPreferredSize();

        preferredWidth = preferredTitleBarSize.width;
        preferredHeight = preferredTitleBarSize.height;

        Frame frame = (Frame)getComponent();

        MenuBar menuBar = frame.getMenuBar();
        if (menuBar != null) {
            Dimensions preferredMenuBarSize = menuBar.getPreferredSize();

            preferredWidth = Math.max(preferredWidth, preferredMenuBarSize.width);
            preferredHeight += preferredMenuBarSize.height;
        }

        Component content = frame.getContent();
        if (content != null) {
            Dimensions preferredContentSize = content.getPreferredSize();

            preferredWidth = Math.max(preferredWidth, preferredContentSize.width);
            preferredHeight += preferredContentSize.height;
        }

        preferredWidth += (padding.left + padding.right) + 2;
        preferredHeight += (padding.top + padding.bottom) + 5;

        return new Dimensions(preferredWidth, preferredHeight);
    }

    public void layout() {
        Frame frame = (Frame)getComponent();

        int width = getWidth();
        int height = getHeight();

        // Size/position title bar
        titleBarTablePane.setLocation(1, 1);
        titleBarTablePane.setSize(Math.max(width - 2, 0),
            Math.max(titleBarTablePane.getPreferredHeight(width - 2), 0));

        // Size/position resize handle
        resizeHandle.setSize(resizeHandle.getPreferredSize());
        resizeHandle.setLocation(width - resizeHandle.getWidth() - 2,
            height - resizeHandle.getHeight() - 2);

        boolean maximized = frame.isMaximized();
        resizeHandle.setVisible(resizable
            && !maximized
            && (frame.isPreferredWidthSet()
                || frame.isPreferredHeightSet()));

        // Size/position menu bar
        int contentX = padding.left + 1;
        int contentY = titleBarTablePane.getHeight() + 4;
        int contentWidth = Math.max(width - (padding.left + padding.right + 2), 0);
        int contentHeight = Math.max(height - (titleBarTablePane.getHeight()
            + padding.top + padding.bottom + 5), 0);

        MenuBar menuBar = frame.getMenuBar();
        if (menuBar != null) {
            menuBar.setLocation(1, contentY);

            int menuBarWidth = width - 2;
            if (menuBar.isVisible()) {
                menuBar.setSize(menuBarWidth, menuBar.getPreferredHeight());
            } else {
                menuBar.setSize(menuBarWidth, 0);
            }

            contentY += menuBar.getHeight();
            contentHeight -= menuBar.getHeight();
        }

        contentY += padding.top;

        // Size/position content
        Component content = frame.getContent();
        if (content != null) {
            content.setLocation(contentX, contentY);
            content.setSize(contentWidth, contentHeight);
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        // Call the base class to paint the background
        super.paint(graphics);

        Frame frame = (Frame)getComponent();

        int width = getWidth();
        int height = getHeight();
        int titleBarHeight = titleBarTablePane.getHeight();

        // Draw the title area
        Color titleBarBackgroundColor = frame.isActive() ?
            this.titleBarBackgroundColor : inactiveTitleBarBackgroundColor;
        Color titleBarBorderColor = frame.isActive() ?
            this.titleBarBorderColor : inactiveTitleBarBorderColor;
        Color titleBarBevelColor = frame.isActive() ?
            this.titleBarBevelColor : inactiveTitleBarBevelColor;

        graphics.setPaint(new GradientPaint(width / 2, 0, titleBarBevelColor,
            width / 2, titleBarHeight + 1, titleBarBackgroundColor));
        graphics.fillRect(0, 0, width, titleBarHeight + 1);

        // Draw the border
        graphics.setPaint(titleBarBorderColor);
        GraphicsUtilities.drawRect(graphics, 0, 0, width, titleBarHeight + 2);

        // Draw the content area
        Bounds contentAreaRectangle = new Bounds(0, titleBarHeight + 2,
            width, height - (titleBarHeight + 2));
        graphics.setPaint(contentBorderColor);
        GraphicsUtilities.drawRect(graphics, contentAreaRectangle.x, contentAreaRectangle.y,
            contentAreaRectangle.width, contentAreaRectangle.height);

        graphics.setPaint(contentBevelColor);
        GraphicsUtilities.drawLine(graphics, contentAreaRectangle.x + 1,
            contentAreaRectangle.y + 1, contentAreaRectangle.width - 2, Orientation.HORIZONTAL);
    }

    @Override
    public void setBackgroundColor(Color backgroundColor) {
        super.setBackgroundColor(backgroundColor);
        contentBevelColor = TerraTheme.brighten(backgroundColor);
    }

    public final void setBackgroundColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(color));
    }

    public boolean getShowMinimizeButton() {
        return minimizeButton.isVisible();
    }

    public void setShowMinimizeButton(boolean showMinimizeButton) {
        minimizeButton.setVisible(showMinimizeButton);
    }

    public boolean getShowMaximizeButton() {
        return maximizeButton.isVisible();
    }

    public void setShowMaximizeButton(boolean showMaximizeButton) {
        maximizeButton.setVisible(showMaximizeButton);
    }

    public boolean getShowCloseButton() {
        return closeButton.isVisible();
    }

    public void setShowCloseButton(boolean showCloseButton) {
        closeButton.setVisible(showCloseButton);
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

    public final void setPadding(String padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(Insets.decode(padding));
    }

    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
        invalidateComponent();
    }

    private void updateMaximizedState() {
        Frame frame = (Frame)getComponent();
        boolean maximized = frame.isMaximized();

        if (!maximized) {
            maximizeButton.setButtonData(maximizeImage);

            if (restoreLocation != null) {
                frame.setLocation(restoreLocation.x, restoreLocation.y);
            }
        } else {
            maximizeButton.setButtonData(restoreImage);
            restoreLocation = frame.getLocation();
        }
    }

    @Override
    public boolean mouseMove(Component component, int x, int y) {
        boolean consumed = super.mouseMove(component, x, y);

        if (Mouse.getCapturer() == component) {
            Frame frame = (Frame)getComponent();
            Display display = frame.getDisplay();

            Point location = frame.mapPointToAncestor(display, x, y);

            // Pretend that the mouse can't move off screen (off the display)
            location = new Point(Math.min(Math.max(location.x, 0), display.getWidth() - 1),
                Math.min(Math.max(location.y, 0), display.getHeight() - 1));

            if (dragOffset != null) {
                // Move the frame
                frame.setLocation(location.x - dragOffset.x, location.y - dragOffset.y);
            } else {
                if (resizeOffset != null) {
                    // Resize the frame
                    int preferredWidth = -1;
                    int preferredHeight = -1;

                    if (frame.isPreferredWidthSet()) {
                        preferredWidth = Math.max(location.x - frame.getX() + resizeOffset.x,
                            titleBarTablePane.getPreferredWidth(-1) + 2);
                        preferredWidth = Math.min(preferredWidth, frame.getMaximumPreferredWidth());
                        preferredWidth = Math.max(preferredWidth, frame.getMinimumPreferredWidth());
                    }

                    if (frame.isPreferredHeightSet()) {
                        preferredHeight = Math.max(location.y - frame.getY() + resizeOffset.y,
                            titleBarTablePane.getHeight() + resizeHandle.getHeight() + 7);
                        preferredHeight = Math.min(preferredHeight, frame.getMaximumPreferredHeight());
                        preferredHeight = Math.max(preferredHeight, frame.getMinimumPreferredHeight());
                    }

                    frame.setPreferredSize(preferredWidth, preferredHeight);
                }
            }
        } else {
            Cursor cursor = null;
            if (x > resizeHandle.getX()
                && y > resizeHandle.getY()) {
                boolean preferredWidthSet = component.isPreferredWidthSet();
                boolean preferredHeightSet = component.isPreferredHeightSet();

                if (preferredWidthSet
                    && preferredHeightSet) {
                    cursor = Cursor.RESIZE_SOUTH_EAST;
                } else if (preferredWidthSet) {
                    cursor = Cursor.RESIZE_EAST;
                } else if (preferredHeightSet) {
                    cursor = Cursor.RESIZE_SOUTH;
                } else {
                    cursor = null;
                }
            } else {
                cursor = null;
            }

            component.setCursor(cursor);
        }

        return consumed;
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        Frame frame = (Frame)getComponent();
        boolean maximized = frame.isMaximized();

        if (button == Mouse.Button.LEFT
            && !maximized) {
            Bounds titleBarBounds = titleBarTablePane.getBounds();

            if (titleBarBounds.contains(x, y)) {
                dragOffset = new Point(x, y);
                Mouse.capture(component);
            } else {
                Bounds resizeHandleBounds = resizeHandle.getBounds();

                if (resizeHandleBounds.contains(x, y)) {
                    resizeOffset = new Point(getWidth() - x, getHeight() - y);
                    Mouse.capture(component);
                }
            }
        }

        return consumed;
    }

    @Override
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseUp(component, button, x, y);

        if (Mouse.getCapturer() == component) {
            dragOffset = null;
            resizeOffset = null;
            Mouse.release();
        }

        return consumed;
    }

    @Override
    public void titleChanged(Window window, String previousTitle) {
        String title = window.getTitle();
        titleLabel.setVisible(title != null);
        titleLabel.setText(title);
    }

    @Override
    public void iconChanged(Window window, Image previousIcon) {
        Image icon = window.getIcon();
        iconImageView.setVisible(icon != null);
        iconImageView.setImage(icon);
    }

    @Override
    public void activeChanged(Window window, Window obverseWindow) {
        boolean active = window.isActive();

        titleLabel.getStyles().put("color", active ?
            titleBarColor : inactiveTitleBarColor);
        iconImageView.getStyles().put("opacity", active ?
            1.0f : INACTIVE_ICON_OPACITY);

        updateButtonStyles(minimizeButton, active);
        updateButtonStyles(maximizeButton, active);
        updateButtonStyles(closeButton, active);

        repaintComponent();
    }

    private void updateButtonStyles(FrameButton frameButton, boolean active) {
        frameButton.getStyles().put("color", active ?
            titleBarColor : inactiveTitleBarColor);
        frameButton.getStyles().put("backgroundColor", active ?
            titleBarBackgroundColor : inactiveTitleBarBackgroundColor);
        frameButton.getStyles().put("borderColor", active ?
            titleBarBorderColor : inactiveTitleBarBorderColor);
    }

    @Override
    public void maximizedChanged(Window window) {
        updateMaximizedState();
    }

    public void menuBarChanged(Frame frame, MenuBar previousMenuBar) {
        invalidateComponent();
    }
}
