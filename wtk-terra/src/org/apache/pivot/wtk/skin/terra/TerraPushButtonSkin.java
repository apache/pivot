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
import java.awt.geom.RoundRectangle2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.skin.PushButtonSkin;

/**
 * Terra push button skin.
 */
public class TerraPushButtonSkin extends PushButtonSkin {
    private Font font;
    private Color color;
    private Color disabledColor;
    private Color backgroundColor;
    private Color disabledBackgroundColor;
    private Color borderColor;
    private Color disabledBorderColor;
    private Insets padding;
    private float minimumAspectRatio;
    private float maximumAspectRatio;
    private boolean toolbar;

    private Color bevelColor;
    private Color pressedBevelColor;
    private Color disabledBevelColor;

    private static final int CORNER_RADIUS = 4;

    public TerraPushButtonSkin() {
        Theme theme = currentTheme();

        font = theme.getFont();
        color = theme.getColor(1);
        disabledColor = theme.getColor(7);
        if (!themeIsFlat()) {
            backgroundColor = theme.getColor(10);
        } else {
            backgroundColor = theme.getColor(9);
        }
        disabledBackgroundColor = theme.getColor(10);
        borderColor = theme.getColor(7);
        disabledBorderColor = theme.getColor(7);
        padding = new Insets(2, 3, 2, 3);
        minimumAspectRatio = Float.NaN;
        maximumAspectRatio = Float.NaN;
        toolbar = false;

        // Set the derived colors
        bevelColor = TerraTheme.brighten(backgroundColor);
        pressedBevelColor = TerraTheme.darken(backgroundColor);
        disabledBevelColor = disabledBackgroundColor;
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        if (height == -1) {
            preferredWidth = getPreferredSize().width;
        } else {
            PushButton pushButton = (PushButton) getComponent();
            Button.DataRenderer dataRenderer = pushButton.getDataRenderer();

            dataRenderer.render(pushButton.getButtonData(), pushButton, false);

            // Include padding in constraint
            int contentHeight = height;
            if (contentHeight != -1) {
                contentHeight = Math.max(contentHeight - paddingHeight(), 0);
            }

            preferredWidth = dataRenderer.getPreferredWidth(contentHeight) + paddingWidth();

            // Adjust for preferred aspect ratio
            if (!Float.isNaN(minimumAspectRatio)
                && (float) preferredWidth / (float) height < minimumAspectRatio) {
                preferredWidth = (int) (height * minimumAspectRatio);
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
            PushButton pushButton = (PushButton) getComponent();
            Button.DataRenderer dataRenderer = pushButton.getDataRenderer();

            dataRenderer.render(pushButton.getButtonData(), pushButton, false);

            // Include padding in constraint
            int contentWidth = width;
            if (contentWidth != -1) {
                contentWidth = Math.max(contentWidth - paddingWidth(), 0);
            }

            preferredHeight = dataRenderer.getPreferredHeight(contentWidth) + paddingHeight();

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
        PushButton pushButton = (PushButton) getComponent();
        Button.DataRenderer dataRenderer = pushButton.getDataRenderer();

        dataRenderer.render(pushButton.getButtonData(), pushButton, false);

        Dimensions preferredContentSize = dataRenderer.getPreferredSize();
        int preferredWidth = preferredContentSize.width + paddingWidth();
        int preferredHeight = preferredContentSize.height + paddingHeight();

        // Adjust for preferred aspect ratio
        float aspectRatio = (float) preferredWidth / (float) preferredHeight;

        if (!Float.isNaN(minimumAspectRatio) && aspectRatio < minimumAspectRatio) {
            preferredWidth = (int) (preferredHeight * minimumAspectRatio);
        }

        if (!Float.isNaN(maximumAspectRatio) && aspectRatio > maximumAspectRatio) {
            preferredHeight = (int) (preferredWidth / maximumAspectRatio);
        }

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        PushButton pushButton = (PushButton) getComponent();

        Button.DataRenderer dataRenderer = pushButton.getDataRenderer();
        dataRenderer.render(pushButton.getButtonData(), pushButton, false);

        int clientWidth = Math.max(width - paddingWidth(), 0);
        int clientHeight = Math.max(height - paddingHeight(), 0);

        int baseline = dataRenderer.getBaseline(clientWidth, clientHeight);
        if (baseline != -1) {
            baseline += padding.top + 1;
        }

        return baseline;
    }

    @Override
    public void paint(Graphics2D graphics) {
        PushButton pushButton = (PushButton) getComponent();

        int width = getWidth();
        int height = getHeight();

        Color backgroundColorLocal = null;
        Color bevelColorLocal = null;
        Color borderColorLocal = null;

        if (!toolbar || highlighted || pushButton.isFocused()) {
            if (pushButton.isEnabled()) {
                backgroundColorLocal = this.backgroundColor;
                bevelColorLocal = (pressed || pushButton.isSelected()) ? pressedBevelColor
                    : this.bevelColor;
                borderColorLocal = this.borderColor;
            } else {
                backgroundColorLocal = disabledBackgroundColor;
                bevelColorLocal = disabledBevelColor;
                borderColorLocal = disabledBorderColor;
            }
        }

        // Paint the background
        GraphicsUtilities.setAntialiasingOn(graphics);

        if (backgroundColorLocal != null && bevelColorLocal != null) {
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

        Button.DataRenderer dataRenderer = pushButton.getDataRenderer();
        dataRenderer.render(pushButton.getButtonData(), pushButton, highlighted);
        dataRenderer.setSize(Math.max(width - paddingWidth(), 0),
            Math.max(getHeight() - paddingHeight(), 0));

        Graphics2D contentGraphics = (Graphics2D) graphics.create();
        contentGraphics.translate(padding.left + 1, padding.top + 1);
        contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
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
        if (pushButton.isFocused() && !toolbar) {
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);
            graphics.setStroke(dashStroke);
            graphics.setColor(this.borderColor);
            graphics.draw(new RoundRectangle2D.Double(2.5, 2.5, Math.max(width - 5, 0), Math.max(
                height - 5, 0), CORNER_RADIUS / 2, CORNER_RADIUS / 2));
        }
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    @Override
    public boolean isOpaque() {
        PushButton pushButton = (PushButton) getComponent();
        return (!toolbar || highlighted || pushButton.isFocused());
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
        setDisabledBackgroundColor(GraphicsUtilities.decodeColor(disabledBackgroundColor, "disabledBackgroundColor"));
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

    private int paddingWidth() {
        return padding.getWidth() + 2;
    }

    private int paddingHeight() {
        return padding.getHeight() + 2;
    }

    public float getMinimumAspectRatio() {
        return minimumAspectRatio;
    }

    public void setMinimumAspectRatio(float minimumAspectRatio) {
        if (!Float.isNaN(maximumAspectRatio) && minimumAspectRatio > maximumAspectRatio) {
            throw new IllegalArgumentException(
                "minimumAspectRatio is greater than maximumAspectRatio.");
        }

        this.minimumAspectRatio = minimumAspectRatio;
        invalidateComponent();
    }

    public final void setMinimumAspectRatio(Number minimumAspectRatio) {
        Utils.checkNull(minimumAspectRatio, "minimumAspectRatio");

        setMinimumAspectRatio(minimumAspectRatio.floatValue());
    }

    public float getMaximumAspectRatio() {
        return maximumAspectRatio;
    }

    public void setMaximumAspectRatio(float maximumAspectRatio) {
        if (!Float.isNaN(minimumAspectRatio) && maximumAspectRatio < minimumAspectRatio) {
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
