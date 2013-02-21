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
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Cursor;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Palette;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowClassListener;
import org.apache.pivot.wtk.effects.DropShadowDecorator;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.WindowSkin;

/**
 * Palette skin class.
 */
public class TerraPaletteSkin extends WindowSkin {
    /**
     * Close button image.
     */
    protected class CloseImage extends Image {
        @Override
        public int getWidth() {
            return 6;
        }

        @Override
        public int getHeight() {
            return 6;
        }

        @Override
        public void paint(Graphics2D graphics) {
            graphics.setPaint(titleBarColor);
            graphics.setStroke(new BasicStroke(2));

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.draw(new Line2D.Double(0.5, 0.5, 5.5, 5.5));
            graphics.draw(new Line2D.Double(0.5, 5.5, 5.5, 0.5));
        }
    }

    /**
     * Resize button image.
     */
    protected class ResizeImage extends Image {
        public static final int ALPHA = 64;

        @Override
        public int getWidth() {
            return 5;
        }

        @Override
        public int getHeight() {
            return 5;
        }

        @Override
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

    private Image closeImage = new CloseImage();
    private Image resizeImage = new ResizeImage();

    private TablePane titleBarTablePane = new TablePane();
    private BoxPane titleBoxPane = new BoxPane();
    private BoxPane buttonBoxPane = new BoxPane();

    private Label titleLabel = new Label();
    private LinkButton closeButton = new LinkButton(closeImage);
    private ImageView resizeHandle = new ImageView(resizeImage);

    private DropShadowDecorator dropShadowDecorator = null;

    private Point dragOffset = null;
    private Point resizeOffset = null;

    private float titleFontScale = 0.8f;

    private Insets padding = new Insets(1);

    private WindowClassListener windowClassListener = new WindowClassListener() {
        @Override
        public void activeWindowChanged(Window previousActiveWindow) {
            Palette palette = (Palette)getComponent();
            Window owner = palette.getOwner();

            Window activeWindow = Window.getActiveWindow();
            palette.setVisible(activeWindow != null
                && (owner == activeWindow
                    || owner.isOwner(activeWindow)));
            invalidateComponent();
        }
    };

    private Color titleBarColor;
    private Color titleBarBackgroundColor;
    private Color titleBarBorderColor;
    private Color contentBorderColor;
    private Color inactiveTitleBarBackgroundColor;
    private Color inactiveTitleBarBorderColor;

    // Derived colors
    private Color contentBevelColor;

    public TerraPaletteSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(10));

        titleBarColor = theme.getColor(4);
        titleBarBackgroundColor = theme.getColor(14);
        titleBarBorderColor = theme.getColor(12);
        contentBorderColor = theme.getColor(7);

        // Set the derived colors
        inactiveTitleBarBackgroundColor = theme.getColor(9);
        inactiveTitleBarBorderColor = theme.getColor(7);

        // The title bar table pane contains two nested box panes: one for
        // the title contents and the other for the buttons
        titleBarTablePane.getColumns().add(new TablePane.Column(1, true));
        titleBarTablePane.getColumns().add(new TablePane.Column(-1));

        TablePane.Row titleRow = new TablePane.Row(-1);
        titleBarTablePane.getRows().add(titleRow);

        titleRow.add(titleBoxPane);
        titleRow.add(buttonBoxPane);

        titleBarTablePane.getStyles().put("padding", new Insets(2, 3, 2, 3));

        // Initialize the title box pane
        titleBoxPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        titleBoxPane.add(titleLabel);
        titleBoxPane.getStyles().put("padding", new Insets(0, 0, 0, 3));

        Font titleFont = theme.getFont();
        titleFont = titleFont.deriveFont(Font.BOLD, Math.round(titleFont.getSize2D() * titleFontScale));
        titleLabel.getStyles().put("font", titleFont);
        titleLabel.getStyles().put("color", titleBarColor);

        // Initialize the button box pane
        buttonBoxPane.getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);
        buttonBoxPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        buttonBoxPane.add(closeButton);

        closeButton.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter() {
            @Override
            public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
                // Consume the event so we don't process it as a click on
                // the title bar and try to move the window
                return true;
            }
        });

        closeButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
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
        palette.add(titleBarTablePane);

        // Attach the drop-shadow decorator
        dropShadowDecorator = new DropShadowDecorator(3, 3, 3);
        palette.getDecorators().add(dropShadowDecorator);

        palette.add(resizeHandle);

        titleChanged(palette, null);
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        Palette palette = (Palette)getComponent();
        Component content = palette.getContent();

        Dimensions preferredTitleBarSize = titleBarTablePane.getPreferredSize();
        preferredWidth = preferredTitleBarSize.width;

        if (content != null) {
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

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        Palette palette = (Palette)getComponent();
        Component content = palette.getContent();

        if (width != -1) {
            width = Math.max(width - 2, 0);
        }

        preferredHeight = titleBarTablePane.getPreferredHeight(width);

        if (content != null) {
            if (width != -1) {
                width = Math.max(width - padding.left - padding.right, 0);
            }

            preferredHeight += content.getPreferredHeight(width);
        }

        preferredHeight += (padding.top + padding.bottom) + 4;

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        Palette palette = (Palette)getComponent();
        Component content = palette.getContent();

        Dimensions preferredTitleBarSize = titleBarTablePane.getPreferredSize();

        preferredWidth = preferredTitleBarSize.width;
        preferredHeight = preferredTitleBarSize.height;

        if (content != null) {
            Dimensions preferredContentSize = content.getPreferredSize();

            preferredWidth = Math.max(preferredWidth, preferredContentSize.width);
            preferredHeight += preferredContentSize.height;
        }

        preferredWidth += (padding.left + padding.right) + 2;
        preferredHeight += (padding.top + padding.bottom) + 4;

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public void layout() {
        Palette palette = (Palette)getComponent();

        int width = getWidth();
        int height = getHeight();

        int clientX = 1;
        int clientY = 1;
        int clientWidth = Math.max(width - 2, 0);
        int clientHeight = Math.max(height - 2, 0);

        // Size/position title bar
        titleBarTablePane.setLocation(clientX, clientY);
        titleBarTablePane.setSize(clientWidth, titleBarTablePane.getPreferredHeight());

        // Size/position resize handle
        resizeHandle.setSize(resizeHandle.getPreferredSize());
        resizeHandle.setLocation(clientWidth - resizeHandle.getWidth(),
            clientHeight - resizeHandle.getHeight());
        resizeHandle.setVisible(palette.isPreferredWidthSet()
            || palette.isPreferredHeightSet());

        // Size/position content
        Component content = palette.getContent();

        if (content != null) {
            content.setLocation(padding.left + 1,
                titleBarTablePane.getHeight() + padding.top + 3);

            int contentWidth = Math.max(width - (padding.left + padding.right + 2), 0);
            int contentHeight = Math.max(height - (titleBarTablePane.getHeight()
                + padding.top + padding.bottom + 4), 0);

            content.setSize(contentWidth, contentHeight);
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        // Call the base class to paint the background
        super.paint(graphics);

        int width = getWidth();
        int height = getHeight();
        int titleBarHeight = titleBarTablePane.getHeight();

        graphics.setStroke(new BasicStroke());

        Palette palette = (Palette)getComponent();
        boolean active = palette.getOwner().isActive();
        boolean enabled = palette.isEnabled();

        Color currentTitleBarBackgroundColor = (active && enabled) ? titleBarBackgroundColor : inactiveTitleBarBackgroundColor;
        Color currentTitleBarBorderColor = (active && enabled) ? titleBarBorderColor : inactiveTitleBarBorderColor;
        Color titleBarBevelColor = TerraTheme.brighten(currentTitleBarBackgroundColor);

        // Draw the title area
        graphics.setPaint(new GradientPaint(width / 2f, 0, titleBarBevelColor,
            width / 2f, titleBarHeight + 1, currentTitleBarBackgroundColor));
        graphics.fillRect(0, 0, width, titleBarHeight + 1);

        // Draw the border
        graphics.setPaint(currentTitleBarBorderColor);
        GraphicsUtilities.drawRect(graphics, 0, 0, width, titleBarHeight + 1);
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
    public Bounds getClientArea() {
        int width = getWidth();
        int height = getHeight();
        int titleBarHeight = titleBarTablePane.getHeight();

        return new Bounds(0, titleBarHeight + 2, width, height - (titleBarHeight + 2));
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

    public final void setPadding(String padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(Insets.decode(padding));
    }

    /**
     * Sets the font used in rendering the titlebar text
     * @param font A {@link org.apache.pivot.wtk.skin.ComponentSkin#decodeFont(String) font specification}
     */
    public final void setTitleFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        titleLabel.getStyles().put("font", decodeFont(font));
    }

    /**
     * Sets the font used in rendering the titlebar text
     * @param font A dictionary {@link Theme#deriveFont describing a font}
     */
    public final void setTitleFont(Dictionary<String, ?> font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        titleLabel.getStyles().put("font", Theme.deriveFont(font));
    }

    public final float getTitleFontScale() {
        return titleFontScale;
    }

    public final void setTitleFontScale(float scale) {
        this.titleFontScale = scale;

        TerraTheme theme = (TerraTheme)Theme.getTheme();
        Font titleFont = theme.getFont();
        titleFont = titleFont.deriveFont(Font.BOLD, Math.round(titleFont.getSize2D() * scale));
        titleLabel.getStyles().put("font", titleFont);
        invalidateComponent();
    }

    @Override
    public boolean mouseMove(Component component, int x, int y) {
        boolean consumed = super.mouseMove(component, x, y);

        if (Mouse.getCapturer() == component) {
            Palette palette = (Palette)getComponent();
            Display display = palette.getDisplay();

            Point location = palette.mapPointToAncestor(display, x, y);

            // Pretend that the mouse can't move off screen (off the display)
            location = new Point(Math.min(Math.max(location.x, 0), display.getWidth() - 1),
                Math.min(Math.max(location.y, 0), display.getHeight() - 1));

            if (dragOffset != null) {
                // Move the window
                palette.setLocation(location.x - dragOffset.x, location.y - dragOffset.y);
            } else {
                if (resizeOffset != null) {
                    // Resize the frame
                    int preferredWidth = -1;
                    int preferredHeight = -1;

                    if (palette.isPreferredWidthSet()) {
                        preferredWidth = Math.max(location.x - palette.getX() + resizeOffset.x,
                            titleBarTablePane.getPreferredWidth(-1) + 2);
                        preferredWidth = Math.min(preferredWidth, palette.getMaximumWidth());
                        preferredWidth = Math.max(preferredWidth, palette.getMinimumWidth());
                    }

                    if (palette.isPreferredHeightSet()) {
                        preferredHeight = Math.max(location.y - palette.getY() + resizeOffset.y,
                            titleBarTablePane.getHeight() + resizeHandle.getHeight() + 7);
                        preferredHeight = Math.min(preferredHeight, palette.getMaximumHeight());
                        preferredHeight = Math.max(preferredHeight, palette.getMinimumHeight());
                    }

                    palette.setPreferredSize(preferredWidth, preferredHeight);
                }
            }
        } else {
            Cursor cursor = null;
            if (resizeHandle.isVisible()
                && x > resizeHandle.getX()
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
                }
            }

            component.setCursor(cursor);
        }

        return consumed;
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        Window window = (Window)getComponent();
        boolean maximized = window.isMaximized();

        if (button == Mouse.Button.LEFT
            && !maximized) {
            Bounds titleBarBounds = titleBarTablePane.getBounds();

            if (titleBarBounds.contains(x, y)) {
                dragOffset = new Point(x, y);
                Mouse.capture(component);
            } else {
                if (resizeHandle.isVisible() && x > resizeHandle.getX() && y > resizeHandle.getY()) {
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
        super.titleChanged(window, previousTitle);

        String title = window.getTitle();
        titleLabel.setVisible(title != null);
        titleLabel.setText(title != null ? title : "");
    }

    @Override
    public void windowOpened(Window window) {
        super.windowOpened(window);

        Window.getWindowClassListeners().add(windowClassListener);
    }

    @Override
    public void windowClosed(Window window, Display display, Window owner) {
        super.windowClosed(window, display, owner);

        Window.getWindowClassListeners().remove(windowClassListener);
    }
}
