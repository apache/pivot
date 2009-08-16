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
package org.apache.pivot.wtk.skin.obsidian;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.skin.RadioButtonSkin;


/**
 * Obsidian radio button skin.
 * <p>
 * TODO Button alignment style (vertical only).
 */
public class ObsidianRadioButtonSkin extends RadioButtonSkin {
    private Font font = new Font("Verdana", Font.PLAIN, 11);
    private Color color = Color.BLACK;
    private Color disabledColor = new Color(0x99, 0x99, 0x99);
    private int spacing = 3;

    private static final Color BUTTON_COLOR = Color.WHITE;
    private static final Color BUTTON_BORDER_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Color BUTTON_SELECTION_COLOR = Color.BLACK;
    private static final Color DISABLED_BUTTON_COLOR = Color.WHITE;
    private static final Color DISABLED_BUTTON_BORDER_COLOR = new Color(0xcc, 0xcc, 0xcc);
    private static final Color DISABLED_BUTTON_SELECTION_COLOR = new Color(0x99, 0x99, 0x99);

    private static final int BUTTON_DIAMETER = 14;
    private static final int BUTTON_SELECTION_DIAMETER = 6;

    public int getPreferredWidth(int height) {
        RadioButton radioButton = (RadioButton)getComponent();
        Button.DataRenderer dataRenderer = radioButton.getDataRenderer();

        dataRenderer.render(radioButton.getButtonData(), radioButton, false);

        int preferredWidth = BUTTON_DIAMETER
            + dataRenderer.getPreferredWidth(height)
            + spacing * 2;

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        RadioButton radioButton = (RadioButton)getComponent();
        Button.DataRenderer dataRenderer = radioButton.getDataRenderer();

        dataRenderer.render(radioButton.getButtonData(), radioButton, false);

        if (width != -1) {
            width = Math.max(width - (BUTTON_DIAMETER + spacing), 0);
        }

        int preferredHeight = Math.max(BUTTON_DIAMETER,
            dataRenderer.getPreferredHeight(width));

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        RadioButton radioButton = (RadioButton)getComponent();
        Button.DataRenderer dataRenderer = radioButton.getDataRenderer();

        dataRenderer.render(radioButton.getButtonData(), radioButton, false);

        int preferredWidth = BUTTON_DIAMETER
            + dataRenderer.getPreferredWidth(-1)
            + spacing * 2;

        int preferredHeight = Math.max(BUTTON_DIAMETER,
            dataRenderer.getPreferredHeight(-1));

        return new Dimensions(preferredWidth, preferredHeight);
    }

    public void paint(Graphics2D graphics) {
        RadioButton radioButton = (RadioButton)getComponent();
        int width = getWidth();
        int height = getHeight();

        // Paint the button
        Graphics2D buttonGraphics = (Graphics2D)graphics.create();
        paintButton(buttonGraphics, radioButton, height);
        buttonGraphics.dispose();

        // Paint the content
        Button.DataRenderer dataRenderer = radioButton.getDataRenderer();
        dataRenderer.render(radioButton.getButtonData(), radioButton, false);
        dataRenderer.setSize(Math.max(width - (BUTTON_DIAMETER + spacing * 2), 0), height);

        Graphics2D contentGraphics = (Graphics2D)graphics.create();
        contentGraphics.translate(BUTTON_DIAMETER + spacing, 0);
        contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
        dataRenderer.paint(contentGraphics);
        contentGraphics.dispose();

        // Paint the focus state
        if (radioButton.isFocused()) {
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] {1.0f, 1.0f}, 0.0f);

            graphics.setStroke(dashStroke);
            graphics.setColor(BUTTON_BORDER_COLOR);

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);

            graphics.drawRect(BUTTON_DIAMETER + 1, 0,
                dataRenderer.getWidth() + spacing * 2 - 2,
                dataRenderer.getHeight() - 1);
        }
    }

    private void paintButton(Graphics2D graphics, RadioButton radioButton, int height) {
        Color buttonColor = null;
        Color buttonBorderColor = null;
        Color buttonSelectionColor = null;

        if (radioButton.isEnabled()) {
            buttonColor = BUTTON_COLOR;
            buttonBorderColor = BUTTON_BORDER_COLOR;
            buttonSelectionColor = BUTTON_SELECTION_COLOR;
        }
        else {
            buttonColor = DISABLED_BUTTON_COLOR;
            buttonBorderColor = DISABLED_BUTTON_BORDER_COLOR;
            buttonSelectionColor = DISABLED_BUTTON_SELECTION_COLOR;
        }

        // Center the button vertically
        graphics.translate(0, (height - BUTTON_DIAMETER) / 2);

        // Paint the border
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        Ellipse2D buttonCircle = new Ellipse2D.Double(0, 0,
            BUTTON_DIAMETER - 1, BUTTON_DIAMETER - 1);
        graphics.setColor(buttonBorderColor);
        graphics.fill(buttonCircle);

        Ellipse2D innerButtonCircle = new Ellipse2D.Double(1, 1,
            BUTTON_DIAMETER - 3, BUTTON_DIAMETER - 3);
        graphics.setColor(buttonBorderColor);
        graphics.fill(buttonCircle);

        graphics.setColor(buttonColor);
        graphics.fill(innerButtonCircle);

        if (radioButton.isSelected()) {
            Ellipse2D buttonSelectionCircle = new Ellipse2D.Double((BUTTON_DIAMETER
                - (BUTTON_SELECTION_DIAMETER - 1)) / 2,
                (BUTTON_DIAMETER - (BUTTON_SELECTION_DIAMETER - 1)) / 2,
                BUTTON_SELECTION_DIAMETER - 1, BUTTON_SELECTION_DIAMETER - 1);
            graphics.setColor(buttonSelectionColor);
            graphics.fill(buttonSelectionCircle);
        }
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

        setFont(Font.decode(font));
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
