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

import pivot.wtk.Button;
import pivot.wtk.Dimensions;
import pivot.wtk.Checkbox;
import pivot.wtk.Bounds;
import pivot.wtk.Theme;
import pivot.wtk.skin.CheckboxSkin;

/**
 * Terra checkbox skin.
 * <p>
 * TODO Button alignment style (vertical only).
 *
 * @author gbrown
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

    // Derived colors
    private Color buttonBevelColor;
    private Color disabledButtonBevelColor;

    private static final int CHECKBOX_SIZE = 14;
    private static final int CHECKMARK_SIZE = 10;

    public TerraCheckboxSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        font = theme.getFont();
        color = theme.getColor(0);
        disabledColor = theme.getColor(2);
        spacing = 3;

        buttonColor = theme.getColor(1);
        buttonBorderColor = theme.getColor(2);
        buttonSelectionColor = TerraTheme.darken(theme.getColor(5));
        disabledButtonColor = theme.getColor(1);
        disabledButtonBorderColor = theme.getColor(2);
        disabledButtonSelectionColor = theme.getColor(2);

        // Set the derived colors
        buttonBevelColor = TerraTheme.darken(buttonColor);
        disabledButtonBevelColor = disabledButtonColor;
    }

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

    public void paint(Graphics2D graphics) {
        Checkbox checkbox = (Checkbox)getComponent();
        int width = getWidth();
        int height = getHeight();

        // Paint the button
        Graphics2D buttonGraphics = (Graphics2D)graphics.create();
        paintButton(buttonGraphics, checkbox, height);
        buttonGraphics.dispose();

        // Paint the content
        Button.DataRenderer dataRenderer = checkbox.getDataRenderer();
        dataRenderer.render(checkbox.getButtonData(), checkbox, false);
        dataRenderer.setSize(Math.max(width - (CHECKBOX_SIZE + spacing * 2), 0), height);

        Graphics2D contentGraphics = (Graphics2D)graphics.create();
        contentGraphics.translate(CHECKBOX_SIZE + spacing, 0);
        contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
        dataRenderer.paint(contentGraphics);
        contentGraphics.dispose();

        // Paint the focus state
        if (checkbox.isFocused()) {
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);

            graphics.setStroke(dashStroke);
            graphics.setColor(buttonBorderColor);

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.drawRect(CHECKBOX_SIZE + 1, 0,
                dataRenderer.getWidth() + spacing * 2 - 2,
                dataRenderer.getHeight() - 1);
        }
    }

    private void paintButton(Graphics2D graphics, Checkbox checkbox, int height) {
        Color buttonColor = null;
        Color buttonBevelColor = null;
        Color buttonBorderColor = null;
        Color buttonSelectionColor = null;

        if (checkbox.isEnabled()) {
            buttonColor = this.buttonColor;
            buttonBevelColor = this.buttonBevelColor;
            buttonBorderColor = this.buttonBorderColor;
            buttonSelectionColor = this.buttonSelectionColor;
        } else {
            buttonColor = disabledButtonColor;
            buttonBevelColor = disabledButtonBevelColor;
            buttonBorderColor = disabledButtonBorderColor;
            buttonSelectionColor = disabledButtonSelectionColor;
        }

        // Center the button vertically
        graphics.translate(0, (height - CHECKBOX_SIZE) / 2);

        // Paint the border
        Bounds buttonRectangle = new Bounds(0, 0,
            CHECKBOX_SIZE - 1, CHECKBOX_SIZE - 1);
        graphics.setPaint(buttonColor);
        graphics.fillRect(buttonRectangle.x, buttonRectangle.y, buttonRectangle.width, buttonRectangle.height);
        graphics.setPaint(buttonBorderColor);
        graphics.drawRect(buttonRectangle.x, buttonRectangle.y, buttonRectangle.width, buttonRectangle.height);

        // Paint the bevel
        graphics.setPaint(buttonBevelColor);
        graphics.drawLine(1, 1, CHECKBOX_SIZE - 2, 1);

        // Paint the checkmark
        Button.State state = checkbox.getState();

        if (state == Button.State.SELECTED) {
            graphics.setColor(buttonSelectionColor);
            graphics.setStroke(new BasicStroke(2.5f));

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw a checkmark
            int n = CHECKMARK_SIZE / 2;
            int m = CHECKMARK_SIZE / 4;
            int offsetX = (CHECKBOX_SIZE - (n + m)) / 2;
            int offsetY = (CHECKBOX_SIZE - n) / 2;

            graphics.drawLine(offsetX, (n - m) + offsetY,
                m + offsetX, n + offsetY);
            graphics.drawLine(m + offsetX, n + offsetY,
                (m + n) + offsetX, offsetY);

            /*
            // Draw an "X"
            int checkSize = (CHECKBOX_SIZE - (CHECKMARK_SIZE - 1)) / 2;
            graphics.draw(new Line2D.Double(checkSize, checkSize,
                checkSize + CHECKMARK_SIZE - 1, checkSize + CHECKMARK_SIZE - 1));
            graphics.draw(new Line2D.Double(checkSize, checkSize + CHECKMARK_SIZE - 1,
                checkSize + CHECKMARK_SIZE - 1, checkSize));
            */
        } else {
            if (state == Button.State.MIXED) {
                graphics.setColor(buttonSelectionColor);
                graphics.setStroke(new BasicStroke(3f));

                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

                int x0 = 4;
                int x1 = CHECKBOX_SIZE - 5;
                int y = CHECKBOX_SIZE / 2;

                graphics.drawLine(x0, y, x1, y);
            }
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

        setColor(decodeColor(color));
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

        setDisabledColor(decodeColor(disabledColor));
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
