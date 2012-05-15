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
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.skin.RadioButtonSkin;

/**
 * Terra radio button skin.
 * <p>
 * TODO Button alignment style (vertical only).
 */
public class TerraRadioButtonSkin extends RadioButtonSkin {
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

    private static final int BUTTON_DIAMETER = 14;
    private static final int BUTTON_SELECTION_DIAMETER = 6;

    public TerraRadioButtonSkin() {
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
        RadioButton radioButton = (RadioButton)getComponent();
        Button.DataRenderer dataRenderer = radioButton.getDataRenderer();

        int preferredWidth = BUTTON_DIAMETER;

        Object buttonData = radioButton.getButtonData();
        if (buttonData != null) {
            dataRenderer.render(buttonData, radioButton, false);
            preferredWidth += dataRenderer.getPreferredWidth(height)
                + spacing * 2;
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        RadioButton radioButton = (RadioButton)getComponent();
        Button.DataRenderer dataRenderer = radioButton.getDataRenderer();

        int preferredHeight = BUTTON_DIAMETER;

        Object buttonData = radioButton.getButtonData();
        if (buttonData != null) {
            if (width != -1) {
                width = Math.max(width - (BUTTON_DIAMETER + spacing), 0);
            }

            dataRenderer.render(buttonData, radioButton, false);

            preferredHeight = Math.max(preferredHeight,
                dataRenderer.getPreferredHeight(width));
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        RadioButton radioButton = (RadioButton)getComponent();
        Button.DataRenderer dataRenderer = radioButton.getDataRenderer();

        int preferredWidth = BUTTON_DIAMETER;
        int preferredHeight = BUTTON_DIAMETER;

        Object buttonData = radioButton.getButtonData();
        if (buttonData != null) {
            dataRenderer.render(buttonData, radioButton, false);
            preferredWidth += dataRenderer.getPreferredWidth(-1)
                + spacing * 2;

            preferredHeight = Math.max(preferredHeight,
                dataRenderer.getPreferredHeight(-1));
        }


        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        RadioButton radioButton = (RadioButton)getComponent();

        int baseline = -1;

        Button.DataRenderer dataRenderer = radioButton.getDataRenderer();
        dataRenderer.render(radioButton.getButtonData(), radioButton, false);

        int clientWidth = Math.max(width - (BUTTON_DIAMETER + spacing), 0);
        baseline = dataRenderer.getBaseline(clientWidth, height);

        return baseline;
    }

    @Override
    public void paint(Graphics2D graphics) {
        RadioButton radioButton = (RadioButton)getComponent();
        int width = getWidth();
        int height = getHeight();

        // Paint the button
        int offset = (height - BUTTON_DIAMETER) / 2;
        graphics.translate(0, offset);
        paintButton(graphics, radioButton.isEnabled(), radioButton.isSelected());
        graphics.translate(0, -offset);

        // Paint the content
        Button.DataRenderer dataRenderer = radioButton.getDataRenderer();
        Object buttonData = radioButton.getButtonData();
        dataRenderer.render(buttonData, radioButton, false);
        dataRenderer.setSize(Math.max(width - (BUTTON_DIAMETER + spacing * 2), 0), height);

        Graphics2D contentGraphics = (Graphics2D)graphics.create();
        contentGraphics.translate(BUTTON_DIAMETER + spacing, 0);
        contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
        dataRenderer.paint(contentGraphics);
        contentGraphics.dispose();

        // Paint the focus state
        if (radioButton.isFocused()) {
            if (buttonData == null) {
                Color focusColor = new Color(buttonSelectionColor.getRed(),
                    buttonSelectionColor.getGreen(),
                    buttonSelectionColor.getBlue(), 0x44);
                graphics.setColor(focusColor);
                graphics.fillOval(0, 0, BUTTON_DIAMETER - 1, BUTTON_DIAMETER - 1);
            } else {
                BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);

                graphics.setStroke(dashStroke);
                graphics.setColor(buttonBorderColor);

                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

                Rectangle2D focusRectangle = new Rectangle2D.Double(BUTTON_DIAMETER + 1, 0.5,
                    dataRenderer.getWidth() + spacing * 2 - 2,
                    dataRenderer.getHeight() - 1);
                graphics.draw(focusRectangle);
            }
        }
    }

    private void paintButton(Graphics2D graphics, boolean enabled, boolean selected) {
        Paint buttonPaint;
        Color buttonBorderColorLocal = null;
        Color buttonSelectionColorLocal = null;

        Ellipse2D buttonBackgroundCircle = new Ellipse2D.Double(1, 1,
            BUTTON_DIAMETER - 3, BUTTON_DIAMETER - 3);

        if (enabled) {
            buttonPaint = new RadialGradientPaint((float)buttonBackgroundCircle.getCenterX(),
                (float)buttonBackgroundCircle.getCenterY(),
                (float)buttonBackgroundCircle.getWidth() * 2 / 3,
                new float[] {0f, 1f}, new Color[] {TerraTheme.darken(buttonColor), buttonColor});

            buttonBorderColorLocal = this.buttonBorderColor;
            buttonSelectionColorLocal = this.buttonSelectionColor;
        }
        else {
            buttonPaint = disabledButtonColor;
            buttonBorderColorLocal = disabledButtonBorderColor;
            buttonSelectionColorLocal = disabledButtonSelectionColor;
        }

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        // Paint the border
        graphics.setColor(buttonBorderColorLocal);
        graphics.fillOval(0, 0, BUTTON_DIAMETER - 1, BUTTON_DIAMETER - 1);

        // Paint the background
        graphics.setPaint(buttonPaint);
        graphics.fill(buttonBackgroundCircle);

        // Paint the selection
        if (selected) {
            Ellipse2D buttonSelectionCircle = new Ellipse2D.Double((BUTTON_DIAMETER
                - (BUTTON_SELECTION_DIAMETER - 1)) / 2,
                (BUTTON_DIAMETER - (BUTTON_SELECTION_DIAMETER - 1)) / 2,
                BUTTON_SELECTION_DIAMETER - 1, BUTTON_SELECTION_DIAMETER - 1);
            graphics.setColor(buttonSelectionColorLocal);
            graphics.fill(buttonSelectionCircle);
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
