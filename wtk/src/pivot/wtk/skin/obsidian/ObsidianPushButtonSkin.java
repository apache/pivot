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
package pivot.wtk.skin.obsidian;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import pivot.wtk.Button;
import pivot.wtk.Dimensions;
import pivot.wtk.Insets;
import pivot.wtk.PushButton;
import pivot.wtk.skin.PushButtonSkin;

/**
 * Obsidian push button skin.
 *
 * @author gbrown
 */
public class ObsidianPushButtonSkin extends PushButtonSkin {
    // Style properties
    private Font font = new Font("Verdana", Font.PLAIN, 11);
    private Color color = Color.WHITE;
    private Color disabledColor = new Color(0x66, 0x66, 0x66);
    private Insets padding = new Insets(4, 8, 4, 8);

    private Color borderColor = new Color(0x4c, 0x4c, 0x4c);
    private Color gradientStartColor = new Color(0x66, 0x66, 0x66);
    private Color gradientEndColor = new Color(0x00, 0x00, 0x00);

    private Color highlightedBorderColor = new Color(0x4c, 0x4c, 0x4c);
    private Color highlightedGradientStartColor = new Color(0x99, 0x99, 0x99);
    private Color highlightedGradientEndColor = new Color(0x00, 0x00, 0x00);

    private Color pressedBorderColor = new Color(0x80, 0x80, 0x80);
    private Color pressedGradientStartColor = new Color(0x00, 0x00, 0x00);
    private Color pressedGradientEndColor = new Color(0x66, 0x66, 0x66);

    private Color disabledBorderColor = new Color(0x80, 0x80, 0x80);
    private Color disabledGradientStartColor = new Color(0x4c, 0x4c, 0x4c);
    private Color disabledGradientEndColor = new Color(0x4c, 0x4c, 0x4c);

    private int cornerRadius = 6;

    public int getPreferredWidth(int height) {
        PushButton pushButton = (PushButton)getComponent();
        Button.DataRenderer dataRenderer = pushButton.getDataRenderer();

        dataRenderer.render(pushButton.getButtonData(), pushButton, false);

        // Include padding in constraint
        if (height != -1) {
            height = Math.max(height - (padding.top + padding.bottom + 2), 0);
        }

        int preferredWidth = dataRenderer.getPreferredWidth(height)
            + padding.left + padding.right + 2;

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        PushButton pushButton = (PushButton)getComponent();
        Button.DataRenderer dataRenderer = pushButton.getDataRenderer();

        dataRenderer.render(pushButton.getButtonData(), pushButton, false);

        // Include padding in constraint
        if (width != -1) {
            width = Math.max(width - (padding.left + padding.right + 2), 0);
        }

        int preferredHeight = dataRenderer.getPreferredHeight(width)
            + padding.top + padding.bottom + 2;

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        PushButton pushButton = (PushButton)getComponent();
        Button.DataRenderer dataRenderer = pushButton.getDataRenderer();

        dataRenderer.render(pushButton.getButtonData(), pushButton, false);

        Dimensions preferredContentSize = dataRenderer.getPreferredSize();

        int preferredWidth = preferredContentSize.width
            + padding.left + padding.right + 2;

        int preferredHeight = preferredContentSize.height
            + padding.top + padding.bottom + 2;

        return new Dimensions(preferredWidth, preferredHeight);
    }

    public void paint(Graphics2D graphics) {
        PushButton pushButton = (PushButton)getComponent();

        Color borderColor = null;
        Color gradientStartColor = null;
        Color gradientEndColor = null;

        if (pushButton.isEnabled()) {
            if (pressed
                || pushButton.isSelected()) {
                borderColor = pressedBorderColor;
                gradientStartColor = pressedGradientStartColor;
                gradientEndColor = pressedGradientEndColor;
            } else {
                if (highlighted) {
                    borderColor = highlightedBorderColor;
                    gradientStartColor = highlightedGradientStartColor;
                    gradientEndColor = highlightedGradientEndColor;
                } else {
                    borderColor = this.borderColor;
                    gradientStartColor = this.gradientStartColor;
                    gradientEndColor = this.gradientEndColor;
                }
            }
        }
        else {
            borderColor = disabledBorderColor;
            gradientStartColor = disabledGradientStartColor;
            gradientEndColor = disabledGradientEndColor;
        }

        int width = getWidth();
        int height = getHeight();

        Graphics2D contentGraphics = (Graphics2D)graphics.create();

        // Paint the background
        RoundRectangle2D buttonRectangle = new RoundRectangle2D.Double(0, 0,
            width - 1, height - 1, cornerRadius, cornerRadius);

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setPaint(new GradientPaint(width / 2, 0, gradientStartColor,
            width / 2, height, gradientEndColor));
        graphics.fill(buttonRectangle);

        // Paint the border
        graphics.setPaint(borderColor);
        graphics.setStroke(new BasicStroke());
        graphics.draw(buttonRectangle);

        // Paint the content
        Button.DataRenderer dataRenderer = pushButton.getDataRenderer();
        dataRenderer.render(pushButton.getButtonData(), pushButton, false);
        dataRenderer.setSize(Math.max(width - (padding.left + padding.right + 2), 0),
            Math.max(getHeight() - (padding.top + padding.bottom + 2), 0));

        contentGraphics.translate(padding.left + 1, padding.top + 1);
        contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
        dataRenderer.paint(contentGraphics);

        // Paint the focus state
        if (pushButton.isFocused()) {
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] {1.0f, 1.0f}, 0.0f);

            graphics.setColor(highlightedGradientStartColor);
            graphics.setStroke(dashStroke);

            graphics.draw(new RoundRectangle2D.Double(2, 2, Math.max(width - 5, 0),
                Math.max(height - 5, 0), cornerRadius - 2, cornerRadius - 2));
        }
    }

    public Font getFont() {
        return font;
    }

    public Color getColor() {
        return color;
    }

    public Color getDisabledColor() {
        return disabledColor;
    }
}
