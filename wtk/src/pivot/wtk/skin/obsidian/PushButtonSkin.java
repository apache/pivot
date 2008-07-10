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
package pivot.wtk.skin.obsidian;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import pivot.wtk.Button;
import pivot.wtk.ButtonStateListener;
import pivot.wtk.Dimensions;
import pivot.wtk.Insets;
import pivot.wtk.PushButton;
import pivot.wtk.skin.AbstractPushButtonSkin;

public class PushButtonSkin extends AbstractPushButtonSkin
    implements ButtonStateListener {
    // Style properties
    protected static final Font FONT = new Font("Verdana", Font.PLAIN, 11);
    protected static final Color COLOR = Color.WHITE;
    protected static final Color DISABLED_COLOR = new Color(0x66, 0x66, 0x66);
    protected static final Insets PADDING = new Insets(4, 8, 4, 8);

    protected static final Color BORDER_COLOR = new Color(0x4c, 0x4c, 0x4c);
    protected static final Color GRADIENT_START_COLOR = new Color(0x66, 0x66, 0x66);
    protected static final Color GRADIENT_END_COLOR = new Color(0x00, 0x00, 0x00);

    protected static final Color HIGHLIGHTED_BORDER_COLOR = new Color(0x4c, 0x4c, 0x4c);
    protected static final Color HIGHLIGHTED_GRADIENT_START_COLOR = new Color(0x99, 0x99, 0x99);
    protected static final Color HIGHLIGHTED_GRADIENT_END_COLOR = new Color(0x00, 0x00, 0x00);

    protected static final Color PRESSED_BORDER_COLOR = new Color(0x80, 0x80, 0x80);
    protected static final Color PRESSED_GRADIENT_START_COLOR = new Color(0x00, 0x00, 0x00);
    protected static final Color PRESSED_GRADIENT_END_COLOR = new Color(0x66, 0x66, 0x66);

    protected static final Color DISABLED_BORDER_COLOR = new Color(0x80, 0x80, 0x80);
    protected static final Color DISABLED_GRADIENT_START_COLOR = new Color(0x4c, 0x4c, 0x4c);
    protected static final Color DISABLED_GRADIENT_END_COLOR = new Color(0x4c, 0x4c, 0x4c);

    protected static final int CORNER_RADIUS = 6;

    // Style keys
    protected static final String FONT_KEY = "font";
    protected static final String COLOR_KEY = "color";
    protected static final String DISABLED_COLOR_KEY = "disabledColor";

    public int getPreferredWidth(int height) {
        PushButton pushButton = (PushButton)getComponent();
        Button.DataRenderer dataRenderer = pushButton.getDataRenderer();

        dataRenderer.render(pushButton.getButtonData(), pushButton, false);

        // Include padding in constraint
        if (height != -1) {
            height = Math.max(height - (PADDING.top + PADDING.bottom + 2), 0);
        }

        int preferredWidth = dataRenderer.getPreferredWidth(height)
            + PADDING.left + PADDING.right + 2;

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        PushButton pushButton = (PushButton)getComponent();
        Button.DataRenderer dataRenderer = pushButton.getDataRenderer();

        dataRenderer.render(pushButton.getButtonData(), pushButton, false);

        // Include padding in constraint
        if (width != -1) {
            width = Math.max(width - (PADDING.left + PADDING.right + 2), 0);
        }

        int preferredHeight = dataRenderer.getPreferredHeight(width)
            + PADDING.top + PADDING.bottom + 2;

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        PushButton pushButton = (PushButton)getComponent();
        Button.DataRenderer dataRenderer = pushButton.getDataRenderer();

        dataRenderer.render(pushButton.getButtonData(), pushButton, false);

        Dimensions preferredContentSize = dataRenderer.getPreferredSize();

        int preferredWidth = preferredContentSize.width
            + PADDING.left + PADDING.right + 2;

        int preferredHeight = preferredContentSize.height
            + PADDING.top + PADDING.bottom + 2;

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
                borderColor = PRESSED_BORDER_COLOR;
                gradientStartColor = PRESSED_GRADIENT_START_COLOR;
                gradientEndColor = PRESSED_GRADIENT_END_COLOR;
            } else {
                if (highlighted) {
                    borderColor = HIGHLIGHTED_BORDER_COLOR;
                    gradientStartColor = HIGHLIGHTED_GRADIENT_START_COLOR;
                    gradientEndColor = HIGHLIGHTED_GRADIENT_END_COLOR;
                } else {
                    borderColor = BORDER_COLOR;
                    gradientStartColor = GRADIENT_START_COLOR;
                    gradientEndColor = GRADIENT_END_COLOR;
                }
            }
        }
        else {
            borderColor = DISABLED_BORDER_COLOR;
            gradientStartColor = DISABLED_GRADIENT_START_COLOR;
            gradientEndColor = DISABLED_GRADIENT_END_COLOR;
        }

        int width = getWidth();
        int height = getHeight();

        Graphics2D contentGraphics = (Graphics2D)graphics.create();

        // Paint the background
        RoundRectangle2D buttonRectangle = new RoundRectangle2D.Double(0, 0,
            width - 1, height - 1, CORNER_RADIUS, CORNER_RADIUS);

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
        dataRenderer.setSize(Math.max(width - (PADDING.left + PADDING.right + 2), 0),
            Math.max(getHeight() - (PADDING.top + PADDING.bottom + 2), 0));

        contentGraphics.translate(PADDING.left + 1, PADDING.top + 1);
        contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
        dataRenderer.paint(contentGraphics);

        // Paint the focus state
        if (pushButton.isFocused()) {
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);

            graphics.setColor(HIGHLIGHTED_GRADIENT_START_COLOR);
            graphics.setStroke(dashStroke);

            graphics.draw(new RoundRectangle2D.Double(2, 2, Math.max(width - 5, 0),
                Math.max(height - 5, 0), CORNER_RADIUS - 2, CORNER_RADIUS - 2));
        }
    }


    @Override
    public Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object value = null;

        if (key.equals(FONT_KEY)) {
            value = FONT;
        } else if (key.equals(COLOR_KEY)) {
            value = COLOR;
        } else if (key.equals(DISABLED_COLOR_KEY)) {
            value = DISABLED_COLOR;
        } else {
            value = super.get(key);
        }

        return value;
    }

    @Override
    public boolean containsKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        return (key.equals(FONT_KEY)
            || key.equals(COLOR_KEY)
            || key.equals(DISABLED_COLOR_KEY)
            || super.containsKey(key));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
