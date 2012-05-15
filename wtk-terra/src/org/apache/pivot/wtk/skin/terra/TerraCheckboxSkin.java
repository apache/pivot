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
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.skin.CheckboxSkin;

/**
 * Terra checkbox skin.
 * <p>
 * TODO Button alignment style (vertical only).
 */
public class TerraCheckboxSkin extends CheckboxSkin {
    private Font font;
    private Color color;
    private Color disabledColor;
    private int spacing;

    private Color buttonColor;
    private Color buttonBorderColor;
    private Color buttonSelectionColor;
    private Color disabledButtonColor;
    private Color disabledButtonBorderColor;
    private Color disabledButtonSelectionColor;

    private static final int CHECKBOX_SIZE = 14;
    private static final int CHECKMARK_SIZE = 10;

    public TerraCheckboxSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        font = theme.getFont();
        color = theme.getColor(1);
        disabledColor = theme.getColor(7);
        spacing = 3;

        buttonColor = theme.getColor(4);
        buttonBorderColor = theme.getColor(7);
        buttonSelectionColor = theme.getColor(15);
        disabledButtonColor = theme.getColor(3);
        disabledButtonBorderColor = theme.getColor(7);
        disabledButtonSelectionColor = theme.getColor(7);
    }

    @Override
    public int getPreferredWidth(int height) {
        Checkbox checkbox = (Checkbox)getComponent();
        Button.DataRenderer dataRenderer = checkbox.getDataRenderer();

        int preferredWidth = CHECKBOX_SIZE;

        Object buttonData = checkbox.getButtonData();
        if (buttonData != null) {
            dataRenderer.render(buttonData, checkbox, false);
            preferredWidth += dataRenderer.getPreferredWidth(height)
                + spacing * 2;
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        Checkbox checkbox = (Checkbox)getComponent();
        Button.DataRenderer dataRenderer = checkbox.getDataRenderer();

        int preferredHeight = CHECKBOX_SIZE;

        Object buttonData = checkbox.getButtonData();
        if (buttonData != null) {
            if (width != -1) {
                width = Math.max(width - (CHECKBOX_SIZE + spacing), 0);
            }

            dataRenderer.render(checkbox.getButtonData(), checkbox, false);

            preferredHeight = Math.max(preferredHeight,
                dataRenderer.getPreferredHeight(width));
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        Checkbox checkbox = (Checkbox)getComponent();
        Button.DataRenderer dataRenderer = checkbox.getDataRenderer();

        dataRenderer.render(checkbox.getButtonData(), checkbox, false);

        int preferredWidth = CHECKBOX_SIZE;
        int preferredHeight = CHECKBOX_SIZE;

        Object buttonData = checkbox.getButtonData();
        if (buttonData != null) {
            dataRenderer.render(buttonData, checkbox, false);
            preferredWidth += dataRenderer.getPreferredWidth(-1)
                + spacing * 2;

            preferredHeight = Math.max(preferredHeight,
                dataRenderer.getPreferredHeight(-1));
        }

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        Checkbox checkbox = (Checkbox) getComponent();

        int baseline = -1;

        Button.DataRenderer dataRenderer = checkbox.getDataRenderer();
        dataRenderer.render(checkbox.getButtonData(), checkbox, false);

        int clientWidth = Math.max(width - (CHECKBOX_SIZE + spacing), 0);
        baseline = dataRenderer.getBaseline(clientWidth, height);

        return baseline;
    }

    @Override
    public void paint(Graphics2D graphics) {
        Checkbox checkbox = (Checkbox)getComponent();
        int width = getWidth();
        int height = getHeight();

        // Paint the button
        int offset = (height - CHECKBOX_SIZE) / 2;
        graphics.translate(0, offset);
        paintButton(graphics, checkbox.isEnabled(), checkbox.getState());
        graphics.translate(0, -offset);

        // Paint the content
        Button.DataRenderer dataRenderer = checkbox.getDataRenderer();
        Object buttonData = checkbox.getButtonData();
        dataRenderer.render(buttonData, checkbox, false);
        dataRenderer.setSize(Math.max(width - (CHECKBOX_SIZE + spacing * 2), 0), height);

        Graphics2D contentGraphics = (Graphics2D)graphics.create();
        contentGraphics.translate(CHECKBOX_SIZE + spacing, 0);
        contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
        dataRenderer.paint(contentGraphics);
        contentGraphics.dispose();

        // Paint the focus state
        if (checkbox.isFocused()) {
            if (buttonData == null) {
                Color focusColor = new Color(buttonSelectionColor.getRed(),
                    buttonSelectionColor.getGreen(),
                    buttonSelectionColor.getBlue(), 0x44);
                graphics.setColor(focusColor);
                graphics.fillRect(0, 0, CHECKBOX_SIZE, CHECKBOX_SIZE);
            } else {
                BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);

                graphics.setStroke(dashStroke);
                graphics.setColor(buttonBorderColor);

                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

                Rectangle2D focusRectangle = new Rectangle2D.Double(CHECKBOX_SIZE + 1, 0.5,
                    dataRenderer.getWidth() + spacing * 2 - 2,
                    dataRenderer.getHeight() - 1);
                graphics.draw(focusRectangle);
            }
        }
    }

    private void paintButton(Graphics2D graphics, boolean enabled, Button.State state) {
        Paint buttonPaint;
        Color buttonBorderColorLocal;
        Color buttonSelectionColorLocal;

        if (enabled) {
            buttonPaint = new GradientPaint(CHECKBOX_SIZE / 2, 0, TerraTheme.darken(buttonColor),
                CHECKBOX_SIZE / 2, CHECKBOX_SIZE, buttonColor);
            buttonBorderColorLocal = this.buttonBorderColor;
            buttonSelectionColorLocal = this.buttonSelectionColor;
        } else {
            buttonPaint = disabledButtonColor;
            buttonBorderColorLocal = disabledButtonBorderColor;
            buttonSelectionColorLocal = disabledButtonSelectionColor;
        }

        // Paint the background
        graphics.setPaint(buttonPaint);
        graphics.fillRect(0, 0, CHECKBOX_SIZE, CHECKBOX_SIZE);

        // Paint the border
        graphics.setPaint(buttonBorderColorLocal);
        GraphicsUtilities.drawRect(graphics, 0, 0, CHECKBOX_SIZE, CHECKBOX_SIZE);

        // Paint the checkmark
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        if (state == Button.State.SELECTED) {
            graphics.setColor(buttonSelectionColorLocal);
            graphics.setStroke(new BasicStroke(2.5f));

            // Draw a checkmark
            int n = CHECKMARK_SIZE / 2;
            int m = CHECKMARK_SIZE / 4;
            int offsetX = (CHECKBOX_SIZE - (n + m)) / 2;
            int offsetY = (CHECKBOX_SIZE - n) / 2;

            graphics.drawLine(offsetX, (n - m) + offsetY,
                m + offsetX, n + offsetY);
            graphics.drawLine(m + offsetX, n + offsetY,
                (m + n) + offsetX, offsetY);
        } else {
            if (state == Button.State.MIXED) {
                graphics.setColor(buttonSelectionColorLocal);
                GraphicsUtilities.drawLine(graphics, 4, (CHECKBOX_SIZE - 3) / 2 + 1, CHECKBOX_SIZE - 8,
                    Orientation.HORIZONTAL, 2);
            }
        }

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_OFF);
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
}
