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
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.MenuButton;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;
import org.apache.pivot.wtk.skin.MenuButtonSkin;

/**
 * Terra menu button skin.
 */
public class TerraMenuButtonSkin extends MenuButtonSkin {
    private Font font;
    private Color color;
    private Color disabledColor;
    private Color backgroundColor;
    private Color disabledBackgroundColor;
    private Color borderColor;
    private Color disabledBorderColor;
    private Insets padding;
    private int spacing;
    private boolean toolbar;

    // Derived colors
    private Color bevelColor;
    private Color pressedBevelColor;
    private Color disabledBevelColor;

    private WindowStateListener menuPopupWindowStateListener = new WindowStateListener.Adapter() {
        @Override
        public void windowClosed(Window window, Display display, Window owner) {
            repaintComponent();
        }
    };

    private static final int TRIGGER_WIDTH = 10;

    public TerraMenuButtonSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();

        font = theme.getFont();
        color = theme.getColor(1);
        disabledColor = theme.getColor(7);
        backgroundColor = theme.getColor(10);
        disabledBackgroundColor = theme.getColor(10);
        borderColor = theme.getColor(7);
        disabledBorderColor = theme.getColor(7);
        padding = new Insets(3);
        spacing = 0;
        toolbar = false;

        // Set the derived colors
        bevelColor = TerraTheme.brighten(backgroundColor);
        pressedBevelColor = TerraTheme.darken(backgroundColor);
        disabledBevelColor = disabledBackgroundColor;

        menuPopup.getWindowStateListeners().add(menuPopupWindowStateListener);
    }

    @Override
    public int getPreferredWidth(int height) {
        MenuButton menuButton = (MenuButton)getComponent();

        Button.DataRenderer dataRenderer = menuButton.getDataRenderer();
        dataRenderer.render(menuButton.getButtonData(), menuButton, false);

        int preferredWidth = dataRenderer.getPreferredWidth(-1) + TRIGGER_WIDTH
            + padding.left + padding.right + spacing + 2;

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        MenuButton menuButton = (MenuButton)getComponent();

        Button.DataRenderer dataRenderer = menuButton.getDataRenderer();
        dataRenderer.render(menuButton.getButtonData(), menuButton, false);

        int preferredHeight = dataRenderer.getPreferredHeight(-1)
            + padding.top + padding.bottom + 2;

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        MenuButton menuButton = (MenuButton)getComponent();

        Button.DataRenderer dataRenderer = menuButton.getDataRenderer();
        dataRenderer.render(menuButton.getButtonData(), menuButton, false);

        Dimensions contentSize = dataRenderer.getPreferredSize();
        int preferredWidth = contentSize.width + TRIGGER_WIDTH + padding.left + padding.right + 2;
        int preferredHeight = contentSize.height + padding.top + padding.bottom + 2;

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        MenuButton menuButton = (MenuButton) getComponent();

        Button.DataRenderer dataRenderer = menuButton.getDataRenderer();
        dataRenderer.render(menuButton.getButtonData(), menuButton, false);

        width = Math.max(width - (TRIGGER_WIDTH + padding.left + padding.right + 2), 0);
        height = Math.max(height - (padding.top + padding.bottom + 2), 0);

        int baseline = dataRenderer.getBaseline(width, height);

        if (baseline != -1) {
            baseline += padding.top + 1;
        }

        return baseline;
    }

    @Override
    public void layout() {
        // No-op
    }

    @Override
    public void paint(Graphics2D graphics) {
        MenuButton menuButton = (MenuButton)getComponent();

        int width = getWidth();
        int height = getHeight();

        Color backgroundColor = null;
        Color bevelColor = null;
        Color borderColor = null;

        if (!toolbar
            || highlighted
            || menuButton.isFocused()
            || menuPopup.isOpen()) {
            if (menuButton.isEnabled()) {
                backgroundColor = this.backgroundColor;
                bevelColor = (pressed || menuPopup.isOpen()) ? pressedBevelColor : this.bevelColor;
                borderColor = this.borderColor;
            } else {
                backgroundColor = disabledBackgroundColor;
                bevelColor = disabledBevelColor;
                borderColor = disabledBorderColor;
            }
        }

        // Paint the background
        if (backgroundColor != null
            && bevelColor != null) {
            graphics.setPaint(new GradientPaint(width / 2, 0, bevelColor,
                width / 2, height / 2, backgroundColor));
            graphics.fillRect(0, 0, width, height);
        }

        // Paint the border
        if (borderColor != null) {
            graphics.setPaint(borderColor);
            GraphicsUtilities.drawRect(graphics, 0, 0, width, height);
        }

        Bounds contentBounds = new Bounds(padding.left + 1, padding.top + 1,
            Math.max(width - (padding.left + padding.right + spacing + TRIGGER_WIDTH + 2), 0),
            Math.max(height - (padding.top + padding.bottom + 2), 0));

        // Paint the content
        Button.DataRenderer dataRenderer = menuButton.getDataRenderer();
        dataRenderer.render(menuButton.getButtonData(), menuButton, highlighted);
        dataRenderer.setSize(contentBounds.width, contentBounds.height);

        Graphics2D contentGraphics = (Graphics2D)graphics.create();
        contentGraphics.translate(contentBounds.x, contentBounds.y);
        contentGraphics.clipRect(0, 0, contentBounds.width, contentBounds.height);
        dataRenderer.paint(contentGraphics);
        contentGraphics.dispose();

        // Paint the trigger
        Bounds triggerBounds = new Bounds(Math.max(width - (padding.right + TRIGGER_WIDTH), 0),
            0, TRIGGER_WIDTH, Math.max(height - (padding.top - padding.bottom), 0));

        GeneralPath triggerIconShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        triggerIconShape.moveTo(0, 0);
        triggerIconShape.lineTo(3, 3);
        triggerIconShape.lineTo(6, 0);
        triggerIconShape.closePath();

        Graphics2D triggerGraphics = (Graphics2D)graphics.create();
        triggerGraphics.setStroke(new BasicStroke(0));
        triggerGraphics.setPaint(color);

        int tx = triggerBounds.x + (triggerBounds.width - triggerIconShape.getBounds().width) / 2;
        int ty = triggerBounds.y + (triggerBounds.height - triggerIconShape.getBounds().height) / 2;
        triggerGraphics.translate(tx, ty);

        triggerGraphics.draw(triggerIconShape);
        triggerGraphics.fill(triggerIconShape);

        triggerGraphics.dispose();

        // Paint the focus state
        if (menuButton.isFocused()
            && !toolbar) {
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);

            graphics.setStroke(dashStroke);
            graphics.setColor(borderColor);

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.draw(new Rectangle2D.Double(2.5, 2.5, Math.max(width - 5, 0),
                Math.max(height - 5, 0)));
        }
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        this.font = font;
        invalidateComponent();
    }

    public final void setFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(decodeFont(font));
    }

    public final void setFont(Dictionary<String, ?> font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Theme.deriveFont(font));
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.color = color;
        repaintComponent();
    }

    public final void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(GraphicsUtilities.decodeColor(color));
    }

    public Color getDisabledColor() {
        return disabledColor;
    }

    public void setDisabledColor(Color disabledColor) {
        if (disabledColor == null) {
            throw new IllegalArgumentException("disabledColor is null.");
        }

        this.disabledColor = disabledColor;
        repaintComponent();
    }

    public final void setDisabledColor(String disabledColor) {
        if (disabledColor == null) {
            throw new IllegalArgumentException("disabledColor is null.");
        }

        setDisabledColor(GraphicsUtilities.decodeColor(disabledColor));
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        this.backgroundColor = backgroundColor;
        bevelColor = TerraTheme.brighten(backgroundColor);
        pressedBevelColor = TerraTheme.darken(backgroundColor);
        repaintComponent();
    }

    public final void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor));
    }

    public Color getDisabledBackgroundColor() {
        return disabledBackgroundColor;
    }

    public void setDisabledBackgroundColor(Color disabledBackgroundColor) {
        if (disabledBackgroundColor == null) {
            throw new IllegalArgumentException("disabledBackgroundColor is null.");
        }

        this.disabledBackgroundColor = disabledBackgroundColor;
        disabledBevelColor = disabledBackgroundColor;
        repaintComponent();
    }

    public final void setDisabledBackgroundColor(String disabledBackgroundColor) {
        if (disabledBackgroundColor == null) {
            throw new IllegalArgumentException("disabledBackgroundColor is null.");
        }

        setDisabledBackgroundColor(GraphicsUtilities.decodeColor(disabledBackgroundColor));
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        this.borderColor = borderColor;
        menuPopup.getStyles().put("borderColor", borderColor);
        repaintComponent();
    }

    public final void setBorderColor(String borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        setBorderColor(GraphicsUtilities.decodeColor(borderColor));
    }

    public Color getDisabledBorderColor() {
        return disabledBorderColor;
    }

    public void setDisabledBorderColor(Color disabledBorderColor) {
        if (disabledBorderColor == null) {
            throw new IllegalArgumentException("disabledBorderColor is null.");
        }

        this.disabledBorderColor = disabledBorderColor;
        repaintComponent();
    }

    public final void setDisabledBorderColor(String disabledBorderColor) {
        if (disabledBorderColor == null) {
            throw new IllegalArgumentException("disabledBorderColor is null.");
        }

        setDisabledBorderColor(GraphicsUtilities.decodeColor(disabledBorderColor));
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

    public int getSpacing() {
        return spacing;
    }

    public void setSpacing(int spacing) {
        if (spacing < 0) {
            throw new IllegalArgumentException("spacing is negative.");
        }
        this.spacing = spacing;
        invalidateComponent();
    }

    public final void setSpacing(Number spacing) {
        if (spacing == null) {
            throw new IllegalArgumentException("spacing is null.");
        }

        setSpacing(spacing.intValue());
    }

    public boolean isToolbar() {
        return toolbar;
    }

    public void setToolbar(boolean toolbar) {
        this.toolbar = toolbar;

        if (toolbar &&
            getComponent().isFocused()) {
            Component.clearFocus();
        }

        repaintComponent();
    }

    @Override
    public void mouseOut(Component component) {
        super.mouseOut(component);

        if (toolbar
            && component.isFocused()) {
            Component.clearFocus();
        }
    }

    @Override
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        if (!toolbar) {
            component.requestFocus();
        }

        return super.mouseClick(component, button, x, y, count);
    }

    // Button events
    @Override
    public void buttonPressed(Button button) {
        if (menuPopup.isOpen()) {
            menuPopup.close();
        } else {
            MenuButton menuButton = (MenuButton)getComponent();

            // Determine the popup's location and preferred size, relative
            // to the button
            Window window = menuButton.getWindow();

            if (window != null) {
                int width = getWidth();
                int height = getHeight();

                Display display = menuButton.getDisplay();

                // Ensure that the popup remains within the bounds of the display
                Point buttonLocation = menuButton.mapPointToAncestor(display, 0, 0);

                Dimensions displaySize = display.getSize();
                menuPopup.setPreferredSize(-1, -1);
                Dimensions popupSize = menuPopup.getPreferredSize();
                int popupWidth = Math.max(popupSize.width, menuButton.getWidth());
                int popupHeight = popupSize.height;

                int x = buttonLocation.x;
                if (popupWidth > width
                    && x + popupWidth > displaySize.width) {
                    x = buttonLocation.x + width - popupWidth;
                }

                int y = buttonLocation.y + height - 1;
                if (y + popupSize.height > displaySize.height) {
                    if (buttonLocation.y - popupSize.height > 0) {
                        y = buttonLocation.y - popupSize.height + 1;
                    } else {
                        popupHeight = displaySize.height - y;
                    }
                } else {
                    popupHeight = -1;
                }

                menuPopup.setLocation(x, y);
                menuPopup.setPreferredSize(popupWidth, popupHeight);
                menuPopup.open(menuButton.getWindow());

                menuPopup.requestFocus();
            }
        }
    }
}
