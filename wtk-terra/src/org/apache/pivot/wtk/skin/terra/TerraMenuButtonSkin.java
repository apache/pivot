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
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Utils;
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
import org.apache.pivot.wtk.Style;
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
    private float minumumAspectRatio;
    private float maximumAspectRatio;
    private boolean toolbar;

    private Color bevelColor;
    private Color pressedBevelColor;
    private Color disabledBevelColor;

    private static final int CORNER_RADIUS = 4;

    private WindowStateListener menuPopupWindowStateListener = new WindowStateListener() {
        @Override
        public void windowOpened(Window window) {
            MenuButton menuButton = (MenuButton) getComponent();

            // Size and position the popup
            Display display = menuButton.getDisplay();
            Dimensions displaySize = display.getSize();

            Point buttonLocation = menuButton.mapPointToAncestor(display, 0, 0);
            window.setLocation(buttonLocation.x, buttonLocation.y + getHeight() - 1);

            int width = getWidth();
            window.setMinimumWidth(width - TRIGGER_WIDTH - 1);

            // If the popup extends over the right edge of the display,
            // move it so that the right edge of the popup lines up with the
            // right edge of the button
            int popupWidth = window.getPreferredWidth();
            if (buttonLocation.x + popupWidth > displaySize.width) {
                window.setX(buttonLocation.x + width - popupWidth);
            }

            window.setMaximumHeight(Integer.MAX_VALUE);
            int popupHeight = window.getPreferredHeight();
            int maximumHeight = displaySize.height - window.getY();
            if (popupHeight > maximumHeight && buttonLocation.y > maximumHeight) {
                window.setMaximumHeight(buttonLocation.y);
                window.setY(buttonLocation.y - window.getPreferredHeight() + 1);
            } else {
                window.setMaximumHeight(maximumHeight);
            }

            repaintComponent();
        }

        @Override
        public void windowClosed(Window window, Display display, Window owner) {
            repaintComponent();
        }
    };

    private static final int TRIGGER_WIDTH = 10;

    public TerraMenuButtonSkin() {
        Theme theme = currentTheme();

        font = theme.getFont();
        color = theme.getColor(1);
        disabledColor = theme.getColor(7);
        setBackgroundColor(10);
        setDisabledBackgroundColor(10);
        borderColor = theme.getColor(7);
        disabledBorderColor = theme.getColor(7);
        padding = new Insets(3);
        spacing = 0;
        minumumAspectRatio = Float.NaN;
        maximumAspectRatio = Float.NaN;
        toolbar = false;

        menuPopup.getWindowStateListeners().add(menuPopupWindowStateListener);
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        if (height == -1) {
            preferredWidth = getPreferredSize().width;
        } else {
            MenuButton menuButton = (MenuButton) getComponent();
            Button.DataRenderer dataRenderer = menuButton.getDataRenderer();
            dataRenderer.render(menuButton.getButtonData(), menuButton, false);

            preferredWidth = dataRenderer.getPreferredWidth(-1) + TRIGGER_WIDTH + padding.left
                + padding.right + spacing + 2;

            // Adjust for preferred aspect ratio
            if (!Float.isNaN(minumumAspectRatio)
                && (float) preferredWidth / (float) height < minumumAspectRatio) {
                preferredWidth = (int) (height * minumumAspectRatio);
            }
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        if (width == -1) {
            preferredHeight = getPreferredSize().height;
        } else {
            MenuButton menuButton = (MenuButton) getComponent();

            Button.DataRenderer dataRenderer = menuButton.getDataRenderer();
            dataRenderer.render(menuButton.getButtonData(), menuButton, false);

            preferredHeight = dataRenderer.getPreferredHeight(-1) + padding.top + padding.bottom
                + 2;

            // Adjust for preferred aspect ratio
            if (!Float.isNaN(maximumAspectRatio)
                && (float) width / (float) preferredHeight > maximumAspectRatio) {
                preferredHeight = (int) (width / maximumAspectRatio);
            }
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        MenuButton menuButton = (MenuButton) getComponent();

        Button.DataRenderer dataRenderer = menuButton.getDataRenderer();
        dataRenderer.render(menuButton.getButtonData(), menuButton, false);

        Dimensions contentSize = dataRenderer.getPreferredSize();
        int preferredWidth = contentSize.width + TRIGGER_WIDTH + padding.left + padding.right + 2;
        int preferredHeight = contentSize.height + padding.top + padding.bottom + 2;

        // Adjust for preferred aspect ratio
        float aspectRatio = (float) preferredWidth / (float) preferredHeight;

        if (!Float.isNaN(minumumAspectRatio) && aspectRatio < minumumAspectRatio) {
            preferredWidth = (int) (preferredHeight * minumumAspectRatio);
        }

        if (!Float.isNaN(maximumAspectRatio) && aspectRatio > maximumAspectRatio) {
            preferredHeight = (int) (preferredWidth / maximumAspectRatio);
        }

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        MenuButton menuButton = (MenuButton) getComponent();

        Button.DataRenderer dataRenderer = menuButton.getDataRenderer();
        dataRenderer.render(menuButton.getButtonData(), menuButton, false);

        int clientWidth = Math.max(width - (TRIGGER_WIDTH + padding.left + padding.right + 2), 0);
        int clientHeight = Math.max(height - (padding.top + padding.bottom + 2), 0);

        int baseline = dataRenderer.getBaseline(clientWidth, clientHeight);

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
        MenuButton menuButton = (MenuButton) getComponent();

        int width = getWidth();
        int height = getHeight();

        Color colorLocal = null;
        Color backgroundColorLocal = null;
        Color bevelColorLocal = null;
        Color borderColorLocal = null;

        if (!toolbar || highlighted || menuButton.isFocused() || menuPopup.isOpen()) {
            if (menuButton.isEnabled()) {
                colorLocal = this.color;
                backgroundColorLocal = this.backgroundColor;
                bevelColorLocal = (pressed || (menuPopup.isOpen() && !menuPopup.isClosing())) ? pressedBevelColor
                    : this.bevelColor;
                borderColorLocal = this.borderColor;
            } else {
                colorLocal = disabledColor;
                backgroundColorLocal = disabledBackgroundColor;
                bevelColorLocal = disabledBevelColor;
                borderColorLocal = disabledBorderColor;
            }
        }

        // Paint the background
        if (backgroundColorLocal != null && bevelColorLocal != null) {
            GraphicsUtilities.setAntialiasingOn(graphics);

            if (!themeIsFlat()) {
                graphics.setPaint(new GradientPaint(width / 2f, 0, bevelColorLocal, width / 2f,
                    height / 2f, backgroundColorLocal));
            } else {
                graphics.setPaint(backgroundColorLocal);
            }
            graphics.fill(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1,
                CORNER_RADIUS, CORNER_RADIUS));
        }

        // Paint the content
        GraphicsUtilities.setAntialiasingOff(graphics);

        Bounds contentBounds = new Bounds(padding.left + 1, padding.top + 1,
            Math.max(width - (padding.getWidth() + spacing + TRIGGER_WIDTH + 2), 0),
            Math.max(height - (padding.getHeight() + 2), 0));
        Button.DataRenderer dataRenderer = menuButton.getDataRenderer();
        dataRenderer.render(menuButton.getButtonData(), menuButton, highlighted);
        dataRenderer.setSize(contentBounds.getSize());

        Graphics2D contentGraphics = (Graphics2D) graphics.create();
        contentGraphics.translate(contentBounds.x, contentBounds.y);
        contentGraphics.clipRect(0, 0, contentBounds.width, contentBounds.height);
        dataRenderer.paint(contentGraphics);
        contentGraphics.dispose();

        GraphicsUtilities.setAntialiasingOn(graphics);

        // Paint the border
        if (borderColorLocal != null && !themeIsFlat()) {
            graphics.setPaint(borderColorLocal);
            graphics.setStroke(new BasicStroke(1));
            graphics.draw(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1,
                CORNER_RADIUS, CORNER_RADIUS));
        }

        // Paint the focus state
        if (menuButton.isFocused() && !toolbar) {
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);
            graphics.setStroke(dashStroke);
            graphics.setColor(this.borderColor);
            graphics.draw(new RoundRectangle2D.Double(2.5, 2.5, Math.max(width - 5, 0), Math.max(
                height - 5, 0), CORNER_RADIUS / 2, CORNER_RADIUS / 2));
        }

        GraphicsUtilities.setAntialiasingOff(graphics);

        // Paint the trigger
        GeneralPath triggerIconShape = new GeneralPath(Path2D.WIND_EVEN_ODD);
        triggerIconShape.moveTo(0, 0);
        triggerIconShape.lineTo(3, 3);
        triggerIconShape.lineTo(6, 0);
        triggerIconShape.closePath();

        Graphics2D triggerGraphics = (Graphics2D) graphics.create();
        triggerGraphics.setStroke(new BasicStroke(0));
        triggerGraphics.setPaint(colorLocal);

        Bounds triggerBounds = new Bounds(Math.max(width - (padding.right + TRIGGER_WIDTH), 0), 0,
            // TODO: this calculation doesn't look right \\// (should be + not -?)
            TRIGGER_WIDTH, Math.max(height - (padding.top - padding.bottom), 0));
        int tx = triggerBounds.x + (triggerBounds.width - triggerIconShape.getBounds().width) / 2;
        int ty = triggerBounds.y + (triggerBounds.height - triggerIconShape.getBounds().height) / 2;
        triggerGraphics.translate(tx, ty);

        triggerGraphics.draw(triggerIconShape);
        triggerGraphics.fill(triggerIconShape);

        triggerGraphics.dispose();
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    @Override
    public boolean isOpaque() {
        MenuButton menuButton = (MenuButton) getComponent();
        return (!toolbar || highlighted || menuButton.isFocused());
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        Utils.checkNull(font, "font");

        this.font = font;
        invalidateComponent();
    }

    public final void setFont(String font) {
        setFont(decodeFont(font));
    }

    public final void setFont(Dictionary<String, ?> font) {
        setFont(Theme.deriveFont(font));
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        Utils.checkNull(color, "color");

        this.color = color;
        repaintComponent();
    }

    public final void setColor(String color) {
        setColor(GraphicsUtilities.decodeColor(color, "color"));
    }

    public final void setColor(int color) {
        Theme theme = currentTheme();
        setColor(theme.getColor(color));
    }

    public Color getDisabledColor() {
        return disabledColor;
    }

    public void setDisabledColor(Color disabledColor) {
        Utils.checkNull(disabledColor, "disabledColor");

        this.disabledColor = disabledColor;
        repaintComponent();
    }

    public final void setDisabledColor(String disabledColor) {
        setDisabledColor(GraphicsUtilities.decodeColor(disabledColor, "disabledColor"));
    }

    public final void setDisabledColor(int disabledColor) {
        Theme theme = currentTheme();
        setDisabledColor(theme.getColor(disabledColor));
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        Utils.checkNull(backgroundColor, "backgroundColor");
        // Note: in the paint code null is okay, but not for these derived colors...
        this.backgroundColor = backgroundColor;
        bevelColor = TerraTheme.brighten(backgroundColor);
        pressedBevelColor = TerraTheme.darken(backgroundColor);
        repaintComponent();
    }

    public final void setBackgroundColor(String backgroundColor) {
        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor, "backgroundColor"));
    }

    public final void setBackgroundColor(int backgroundColor) {
        Theme theme = currentTheme();
        setBackgroundColor(theme.getColor(backgroundColor));
    }

    public Color getDisabledBackgroundColor() {
        return disabledBackgroundColor;
    }

    public void setDisabledBackgroundColor(Color disabledBackgroundColor) {
        Utils.checkNull(disabledBackgroundColor, "disabledBackgroundColor");

        this.disabledBackgroundColor = disabledBackgroundColor;
        disabledBevelColor = disabledBackgroundColor;
        repaintComponent();
    }

    public final void setDisabledBackgroundColor(String disabledBackgroundColor) {
        setDisabledBackgroundColor(GraphicsUtilities.decodeColor(disabledBackgroundColor,
            "disabledBackgroundColor"));
    }

    public final void setDisabledBackgroundColor(int disabledBackgroundColor) {
        Theme theme = currentTheme();
        setDisabledBackgroundColor(theme.getColor(disabledBackgroundColor));
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        Utils.checkNull(borderColor, "borderColor");

        this.borderColor = borderColor;
        menuPopup.getStyles().put(Style.borderColor, borderColor);
        repaintComponent();
    }

    public final void setBorderColor(String borderColor) {
        setBorderColor(GraphicsUtilities.decodeColor(borderColor, "borderColor"));
    }

    public final void setBorderColor(int borderColor) {
        Theme theme = currentTheme();
        setBorderColor(theme.getColor(borderColor));
    }

    public Color getDisabledBorderColor() {
        return disabledBorderColor;
    }

    public void setDisabledBorderColor(Color disabledBorderColor) {
        Utils.checkNull(disabledBorderColor, "disabledBorderColor");

        this.disabledBorderColor = disabledBorderColor;
        repaintComponent();
    }

    public final void setDisabledBorderColor(String disabledBorderColor) {
        setDisabledBorderColor(GraphicsUtilities.decodeColor(disabledBorderColor, "disabledBorderColor"));
    }

    public final void setDisabledBorderColor(int disabledBorderColor) {
        Theme theme = currentTheme();
        setDisabledBorderColor(theme.getColor(disabledBorderColor));
    }

    public Insets getPadding() {
        return padding;
    }

    public void setPadding(Insets padding) {
        Utils.checkNull(padding, "padding");

        this.padding = padding;
        invalidateComponent();
    }

    public final void setPadding(Dictionary<String, ?> padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(Sequence<?> padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(Number padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(String padding) {
        setPadding(Insets.decode(padding));
    }

    public int getSpacing() {
        return spacing;
    }

    public void setSpacing(int spacing) {
        Utils.checkNonNegative(spacing, "spacing");

        this.spacing = spacing;
        invalidateComponent();
    }

    public final void setSpacing(Number spacing) {
        Utils.checkNull(spacing, "spacing");

        setSpacing(spacing.intValue());
    }

    public float getMinimumAspectRatio() {
        return minumumAspectRatio;
    }

    public void setMinimumAspectRatio(float minumumAspectRatio) {
        if (!Float.isNaN(maximumAspectRatio) && minumumAspectRatio > maximumAspectRatio) {
            throw new IllegalArgumentException(
                "minumumAspectRatio is greater than maximumAspectRatio.");
        }

        this.minumumAspectRatio = minumumAspectRatio;
        invalidateComponent();
    }

    public final void setMinimumAspectRatio(Number minumumAspectRatio) {
        Utils.checkNull(minumumAspectRatio, "minumumAspectRatio");

        setMinimumAspectRatio(minumumAspectRatio.floatValue());
    }

    public float getMaximumAspectRatio() {
        return maximumAspectRatio;
    }

    public void setMaximumAspectRatio(float maximumAspectRatio) {
        if (!Float.isNaN(minumumAspectRatio) && maximumAspectRatio < minumumAspectRatio) {
            throw new IllegalArgumentException(
                "maximumAspectRatio is less than minimumAspectRatio.");
        }

        this.maximumAspectRatio = maximumAspectRatio;
        invalidateComponent();
    }

    public final void setMaximumAspectRatio(Number maximumAspectRatio) {
        Utils.checkNull(maximumAspectRatio, "maximumAspectRatio");

        setMaximumAspectRatio(maximumAspectRatio.floatValue());
    }

    public boolean isToolbar() {
        return toolbar;
    }

    public void setToolbar(boolean toolbar) {
        this.toolbar = toolbar;

        if (toolbar && getComponent().isFocused()) {
            Component.clearFocus();
        }

        repaintComponent();
    }

    public int getCloseTransitionDuration() {
        return menuPopup.getStyles().getInt(Style.closeTransitionDuration);
    }

    public void setCloseTransitionDuration(int closeTransitionDuration) {
        menuPopup.getStyles().put(Style.closeTransitionDuration, closeTransitionDuration);
        MenuButton menuButton = (MenuButton) getComponent();
        menuButton.setQueuedActionDelay(closeTransitionDuration + 50);
    }

    public int getCloseTransitionRate() {
        return menuPopup.getStyles().getInt(Style.closeTransitionRate);
    }

    public void setCloseTransitionRate(int closeTransitionRate) {
        menuPopup.getStyles().put(Style.closeTransitionRate, closeTransitionRate);
    }

    @Override
    public void mouseOut(Component component) {
        super.mouseOut(component);

        if (toolbar && component.isFocused()) {
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
}
