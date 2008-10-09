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
import java.awt.geom.GeneralPath;

import pivot.collections.Dictionary;
import pivot.wtk.Bounds;
import pivot.wtk.Button;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Insets;
import pivot.wtk.MenuButton;
import pivot.wtk.Theme;
import pivot.wtk.skin.MenuButtonSkin;

/**
 * Terra menu button skin.
 *
 * @author gbrown
 */
public class TerraMenuButtonSkin extends MenuButtonSkin {
    private Font font;
    private Color color;
    private Color disabledColor;
    private Color backgroundColor;
    private Color disabledBackgroundColor;
    private Color borderColor;
    private Color disabledBorderColor;
    private Color bevelColor;
    private Color pressedBevelColor;
    private Color disabledBevelColor;
    private Insets padding;
    private int spacing;
    private boolean toolbar;

    private static final int TRIGGER_WIDTH = 10;

    public TerraMenuButtonSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        font = theme.getFont();
        color = theme.getColor(0);
        disabledColor = theme.getColor(2);
        backgroundColor = theme.getColor(5);
        disabledBackgroundColor = theme.getColor(6);
        borderColor = theme.getColor(2);
        disabledBorderColor = theme.getColor(4);
        bevelColor = theme.getColor(6);
        pressedBevelColor = theme.getColor(4);
        disabledBevelColor = theme.getColor(1);
        padding = new Insets(3);
        spacing = 0;
        toolbar = false;
    }

    public int getPreferredWidth(int height) {
        MenuButton menuButton = (MenuButton)getComponent();
        Button.DataRenderer dataRenderer = menuButton.getDataRenderer();

        if (height != -1) {
            height = Math.max(height - (padding.top + padding.bottom + 2), 0);
        }

        dataRenderer.render(menuButton.getButtonData(), menuButton, false);

        int preferredWidth = dataRenderer.getPreferredWidth(-1) + TRIGGER_WIDTH
            + padding.left + padding.right + spacing + 2;

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        MenuButton menuButton = (MenuButton)getComponent();
        Button.DataRenderer dataRenderer = menuButton.getDataRenderer();

        dataRenderer.render(menuButton.getButtonData(), menuButton, false);

        int preferredHeight = dataRenderer.getPreferredHeight(-1)
            + padding.top + padding.bottom + 2;

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        // TODO Optimize by performing calcuations locally
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    public void layout() {
        // No-op
    }

    public void paint(Graphics2D graphics) {
        MenuButton menuButton = (MenuButton)getComponent();

        int width = getWidth();
        int height = getHeight();

        Color backgroundColor = null;
        Color bevelColor = null;
        Color borderColor = null;

        if (!toolbar
            || highlighted
            || menuPopup.isOpen()) {
            if (menuButton.isEnabled()) {
                backgroundColor = this.backgroundColor;
                bevelColor = (pressed) ? pressedBevelColor : this.bevelColor;
                borderColor = this.borderColor;
            } else {
                backgroundColor = disabledBackgroundColor;
                bevelColor = disabledBevelColor;
                borderColor = disabledBorderColor;
            }
        }

        // Paint the background
        if (backgroundColor != null) {
            graphics.setPaint(backgroundColor);
            graphics.fillRect(0, 0, width, height);
        }

        // Draw all lines with a 1px solid stroke
        graphics.setStroke(new BasicStroke());

        // Paint the bevel
        if (bevelColor != null) {
            graphics.setPaint(bevelColor);
            graphics.drawLine(1, 1, width - 2, 1);
        }

        // Paint the border
        if (borderColor != null) {
            graphics.setPaint(borderColor);
            graphics.drawRect(0, 0, width - 1, height - 1);
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

        int tx = triggerBounds.x + Math.round((triggerBounds.width - triggerIconShape.getBounds().width) / 2);
        int ty = triggerBounds.y + Math.round((triggerBounds.height - triggerIconShape.getBounds().height) / 2f);
        triggerGraphics.translate(tx, ty);

        triggerGraphics.draw(triggerIconShape);
        triggerGraphics.fill(triggerIconShape);

        triggerGraphics.dispose();

        // Paint the focus state
        if (menuButton.isFocused()) {
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);

            graphics.setStroke(dashStroke);
            graphics.setColor(borderColor);

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.drawRect(2, 2, Math.max(width - 5, 0),
                Math.max(height - 5, 0));

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        }
    }

    @Override
    public boolean isFocusable() {
        return !toolbar;
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

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    public final void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        setBackgroundColor(decodeColor(backgroundColor));
    }

    public Color getDisabledBackgroundColor() {
        return disabledBackgroundColor;
    }

    public void setDisabledBackgroundColor(Color disabledBackgroundColor) {
        if (disabledBackgroundColor == null) {
            throw new IllegalArgumentException("disabledBackgroundColor is null.");
        }

        this.disabledBackgroundColor = disabledBackgroundColor;
        repaintComponent();
    }

    public final void setDisabledBackgroundColor(String disabledBackgroundColor) {
        if (disabledBackgroundColor == null) {
            throw new IllegalArgumentException("disabledBackgroundColor is null.");
        }

        setDisabledBackgroundColor(decodeColor(disabledBackgroundColor));
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

        setBorderColor(decodeColor(borderColor));
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

        setDisabledBorderColor(decodeColor(disabledBorderColor));
    }

    public Color getBevelColor() {
        return bevelColor;
    }

    public void setBevelColor(Color bevelColor) {
        if (bevelColor == null) {
            throw new IllegalArgumentException("bevelColor is null.");
        }

        this.bevelColor = bevelColor;
        repaintComponent();
    }

    public final void setBevelColor(String bevelColor) {
        if (bevelColor == null) {
            throw new IllegalArgumentException("bevelColor is null.");
        }

        setBevelColor(decodeColor(bevelColor));
    }

    public Color getPressedBevelColor() {
        return pressedBevelColor;
    }

    public void setPressedBevelColor(Color pressedBevelColor) {
        if (pressedBevelColor == null) {
            throw new IllegalArgumentException("pressedBevelColor is null.");
        }

        this.pressedBevelColor = pressedBevelColor;
        repaintComponent();
    }

    public final void setPressedBevelColor(String pressedBevelColor) {
        if (pressedBevelColor == null) {
            throw new IllegalArgumentException("pressedBevelColor is null.");
        }

        setPressedBevelColor(decodeColor(pressedBevelColor));
    }

    public Color getDisabledBevelColor() {
        return disabledBevelColor;
    }

    public void setDisabledBevelColor(Color disabledBevelColor) {
        if (disabledBevelColor == null) {
            throw new IllegalArgumentException("disabledBevelColor is null.");
        }

        this.disabledBevelColor = disabledBevelColor;
        repaintComponent();
    }

    public final void setDisabledBevelColor(String disabledBevelColor) {
        if (disabledBevelColor == null) {
            throw new IllegalArgumentException("disabledBevelColor is null.");
        }

        setDisabledBackgroundColor(decodeColor(disabledBevelColor));
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
}
