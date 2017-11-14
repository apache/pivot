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
package org.apache.pivot.wtk.skin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Separator;
import org.apache.pivot.wtk.SeparatorListener;
import org.apache.pivot.wtk.Theme;

/**
 * Separator skin.
 */
public class SeparatorSkin extends ComponentSkin implements SeparatorListener {
    private Font font;
    private Color color;
    private Color headingColor;
    private int thickness;
    private Insets padding;

    public SeparatorSkin() {
        Theme theme = Theme.getTheme();
        font = theme.getFont().deriveFont(Font.BOLD);

        color = defaultForegroundColor();
        headingColor = defaultForegroundColor();

        thickness = 1;
        padding = new Insets(4, 0, 4, 4);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Separator separator = (Separator) component;
        separator.getSeparatorListeners().add(this);
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        Separator separator = (Separator) getComponent();
        String heading = separator.getHeading();

        if (heading != null && heading.length() > 0) {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            Rectangle2D headingBounds = font.getStringBounds(heading, fontRenderContext);
            preferredWidth = (int) Math.ceil(headingBounds.getWidth())
                + (padding.left + padding.right);
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = thickness;

        Separator separator = (Separator) getComponent();
        String heading = separator.getHeading();

        if (heading != null && heading.length() > 0) {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            LineMetrics lm = font.getLineMetrics(heading, fontRenderContext);
            preferredHeight = Math.max(
                (int) Math.ceil(lm.getAscent() + lm.getDescent() + lm.getLeading()),
                preferredHeight);
        }

        preferredHeight += (padding.top + padding.bottom);

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = thickness;

        Separator separator = (Separator) getComponent();
        String heading = separator.getHeading();

        if (heading != null && heading.length() > 0) {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            Rectangle2D headingBounds = font.getStringBounds(heading, fontRenderContext);
            LineMetrics lm = font.getLineMetrics(heading, fontRenderContext);
            preferredWidth = (int) Math.ceil(headingBounds.getWidth());
            preferredHeight = Math.max(
                (int) Math.ceil(lm.getAscent() + lm.getDescent() + lm.getLeading()),
                preferredHeight);
        }

        preferredHeight += (padding.top + padding.bottom);
        preferredWidth += (padding.left + padding.right);

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public void layout() {
        // No-op
    }

    @Override
    public void paint(Graphics2D graphics) {
        Separator separator = (Separator) getComponent();
        int width = getWidth();
        int separatorY = padding.top;

        String heading = separator.getHeading();

        if (heading != null && heading.length() > 0) {
            FontRenderContext fontRenderContext = GraphicsUtilities.prepareForText(graphics, font, headingColor);
            LineMetrics lm = font.getLineMetrics(heading, fontRenderContext);

            graphics.drawString(heading, padding.left, lm.getAscent() + padding.top);

            Rectangle2D headingBounds = font.getStringBounds(heading, fontRenderContext);

            Area titleClip = new Area(graphics.getClip());
            titleClip.subtract(new Area(new Rectangle2D.Double(padding.left, padding.top,
                headingBounds.getWidth() + padding.right, headingBounds.getHeight())));
            graphics.clip(titleClip);

            separatorY += (lm.getAscent() + lm.getDescent()) / 2 + 1;
        }

        graphics.setStroke(new BasicStroke(thickness));
        graphics.setColor(color);
        graphics.drawLine(0, separatorY, width, separatorY);
    }

    /**
     * @return <tt>false</tt>; spacers are not focusable.
     */
    @Override
    public boolean isFocusable() {
        return false;
    }

    /**
     * @return The font used in rendering the Separator's heading.
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets the font used in rendering the Separator's heading.
     *
     * @param font The new font for the heading.
     */
    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        this.font = font;
        invalidateComponent();
    }

    /**
     * Sets the font used in rendering the Separator's heading.
     *
     * @param font A {@linkplain ComponentSkin#decodeFont(String) font specification}.
     */
    public final void setFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(decodeFont(font));
    }

    /**
     * Sets the font used in rendering the Separator's heading.
     *
     * @param font A dictionary {@link Theme#deriveFont describing a font}.
     */
    public final void setFont(Dictionary<String, ?> font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Theme.deriveFont(font));
    }

    /**
     * @return The color of the Separator's horizontal rule.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the color of the Separator's horizontal rule.
     *
     * @param color The new color for the horizontal rule.
     */
    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.color = color;
        repaintComponent();
    }

    /**
     * Sets the color of the Separator's horizontal rule.
     *
     * @param color Any of the {@linkplain GraphicsUtilities#decodeColor color
     * values recognized by Pivot}.
     */
    public final void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(GraphicsUtilities.decodeColor(color));
    }

    /**
     * @return The color of the text in the heading.
     */
    public Color getHeadingColor() {
        return headingColor;
    }

    /**
     * Sets the color of the text in the heading.
     *
     * @param headingColor The new color for the heading text.
     */
    public void setHeadingColor(Color headingColor) {
        if (headingColor == null) {
            throw new IllegalArgumentException("headingColor is null.");
        }

        this.headingColor = headingColor;
        repaintComponent();
    }

    /**
     * Sets the color of the text in the heading.
     *
     * @param headingColor Any of the {@linkplain GraphicsUtilities#decodeColor
     * color values recognized by Pivot}.
     */
    public final void setHeadingColor(String headingColor) {
        if (headingColor == null) {
            throw new IllegalArgumentException("headingColor is null.");
        }

        setHeadingColor(GraphicsUtilities.decodeColor(headingColor));
    }

    /**
     * @return The thickness of the Separator's horizontal rule.
     */
    public int getThickness() {
        return thickness;
    }

    /**
     * Sets the thickness of the Separator's horizontal rule.
     *
     * @param thickness The new rule thickness (in pixels).
     */
    public void setThickness(int thickness) {
        if (thickness < 0) {
            throw new IllegalArgumentException("thickness is negative.");
        }
        this.thickness = thickness;
        invalidateComponent();
    }

    /**
     * Sets the thickness of the Separator's horizontal rule.
     *
     * @param thickness The new integer value for the rule thickness (in pixels).
     */
    public final void setThickness(Number thickness) {
        if (thickness == null) {
            throw new IllegalArgumentException("thickness is null.");
        }

        setThickness(thickness.intValue());
    }

    /**
     * @return The amount of space surrounding (left/right) the Separator's
     * heading, and above and below the entire component.
     */
    public Insets getPadding() {
        return padding;
    }

    /**
     * Sets the amount of space to leave around the Separator's heading, and
     * above and below the entire component.
     *
     * @param padding The new padding values.
     */
    public void setPadding(Insets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        this.padding = padding;
        invalidateComponent();
    }

    /**
     * Sets the amount of space to leave around the Separator's heading, and
     * above and below the entire component.
     *
     * @param padding A dictionary with keys in the set {left, top, bottom,
     * right}.
     */
    public final void setPadding(Dictionary<String, ?> padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave around the Separator's heading, and
     * above and below the entire component.
     *
     * @param padding The new single padding value for all areas.
     */
    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave around the Separator's heading, and
     * above and below the entire component.
     *
     * @param padding The new integer value to use for padding in all areas.
     */
    public final void setPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(padding.intValue());
    }

    /**
     * Sets the amount of space to leave around the Separator's heading, and
     * above and below the entire component.
     *
     * @param padding A string containing an integer or a JSON dictionary with
     * keys left, top, bottom, and/or right.
     */
    public final void setPadding(String padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(Insets.decode(padding));
    }

    // Separator events
    @Override
    public void headingChanged(Separator separator, String previousHeading) {
        invalidateComponent();
    }
}
