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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

import pivot.wtk.Button;
import pivot.wtk.Dimensions;
import pivot.wtk.RadioButton;
import pivot.wtk.Theme;
import pivot.wtk.skin.RadioButtonSkin;

/**
 * Terra radio button skin.
 * <p>
 * TODO Button alignment style (vertical only).
 *
 * @author gbrown
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
        color = theme.getColor(0);
        disabledColor = theme.getColor(3);
        spacing = 3;

        buttonColor = theme.getColor(1);
        buttonBorderColor = theme.getColor(3);
        buttonSelectionColor = theme.getColor(7);
        disabledButtonColor = theme.getColor(1);
        disabledButtonBorderColor = theme.getColor(10);
        disabledButtonSelectionColor = theme.getColor(3);
    }

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
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);

            graphics.setStroke(dashStroke);
            graphics.setColor(buttonBorderColor);

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

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
            buttonColor = this.buttonColor;
            buttonBorderColor = this.buttonBorderColor;
            buttonSelectionColor = this.buttonSelectionColor;
        }
        else {
            buttonColor = disabledButtonColor;
            buttonBorderColor = disabledButtonBorderColor;
            buttonSelectionColor = disabledButtonSelectionColor;
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

        setColor(Color.decode(color));
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

        setDisabledColor(Color.decode(disabledColor));
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
