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
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BorderListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.CornerRadii;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Theme;

/**
 * Border skin.
 * <p>
 * TODO Add styles to support different border styles (e.g. inset, outset) or
 * create subclasses for these border types.
 */
public class BorderSkin extends ContainerSkin implements BorderListener {
    private Font font;
    private Color color;
    private Color titleColor;
    private int thickness;
    private Insets padding;
    private CornerRadii cornerRadii;

    public BorderSkin() {
        Theme theme = Theme.getTheme();
        setBackgroundColor(Color.WHITE);

        font = theme.getFont().deriveFont(Font.BOLD);
        color = Color.BLACK;
        titleColor = Color.BLACK;
        thickness = 1;
        padding = Insets.NONE;
        cornerRadii = CornerRadii.NONE;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Border border = (Border)component;
        border.getBorderListeners().add(this);
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        Border border = (Border)getComponent();
        int topThickness = thickness;

        String title = border.getTitle();
        if (title != null
            && title.length() > 0) {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            Rectangle2D headingBounds = font.getStringBounds(title, fontRenderContext);
            preferredWidth = (int)Math.ceil(headingBounds.getWidth());

            LineMetrics lm = font.getLineMetrics(title, fontRenderContext);
            topThickness = Math.max((int)Math.ceil(lm.getHeight()), topThickness);
        }

        Component content = border.getContent();
        if (content != null) {
            if (height != -1) {
                height = Math.max(height - (topThickness + thickness) -
                    padding.top - padding.bottom, 0);
            }

            preferredWidth = Math.max(preferredWidth, content.getPreferredWidth(height));
        }

        preferredWidth += (padding.left + padding.right) + (thickness * 2);

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        Border border = (Border)getComponent();
        int topThickness = thickness;

        String title = border.getTitle();
        if (title != null
            && title.length() > 0) {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            LineMetrics lm = font.getLineMetrics(title, fontRenderContext);
            topThickness = Math.max((int)Math.ceil(lm.getHeight()), topThickness);
        }

        Component content = border.getContent();
        if (content != null) {
            if (width != -1) {
                width = Math.max(width - (thickness * 2)
                    - padding.left - padding.right, 0);
            }

            preferredHeight = content.getPreferredHeight(width);
        }

        preferredHeight += (padding.top + padding.bottom) + (topThickness + thickness);

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        Border border = (Border)getComponent();
        int topThickness = thickness;

        String title = border.getTitle();
        if (title != null
            && title.length() > 0) {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            Rectangle2D headingBounds = font.getStringBounds(title, fontRenderContext);
            preferredWidth = (int)Math.ceil(headingBounds.getWidth());

            LineMetrics lm = font.getLineMetrics(title, fontRenderContext);
            topThickness = Math.max((int)Math.ceil(lm.getHeight()), topThickness);
        }

        Component content = border.getContent();
        if (content != null) {
            Dimensions preferredSize = content.getPreferredSize();
            preferredWidth = Math.max(preferredWidth, preferredSize.width);
            preferredHeight += preferredSize.height;
        }

        preferredWidth += (padding.left + padding.right) + (thickness * 2);
        preferredHeight += (padding.top + padding.bottom) + (topThickness + thickness);

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        int baseline = -1;

        Border border = (Border)getComponent();
        int topThickness = thickness;

        // Delegate baseline calculation to the content component
        Component content = border.getContent();
        if (content != null) {
            String title = border.getTitle();
            if (title != null
                && title.length() > 0) {
                FontRenderContext fontRenderContext = Platform.getFontRenderContext();
                LineMetrics lm = font.getLineMetrics(title, fontRenderContext);
                topThickness = Math.max((int)Math.ceil(lm.getHeight()), topThickness);
            }

            int clientWidth = Math.max(width - (thickness * 2)
                - (padding.left + padding.right), 0);
            int clientHeight = Math.max(height - (topThickness + thickness) -
                (padding.top + padding.bottom), 0);

            baseline = content.getBaseline(clientWidth, clientHeight);
        }

        // Include top padding value and top border thickness
        if (baseline != -1) {
            baseline += (padding.top + topThickness);
        }

        return baseline;
    }

    @Override
    public void layout() {
        int width = getWidth();
        int height = getHeight();

        Border border = (Border)getComponent();
        int topThickness = thickness;

        String title = border.getTitle();
        if (title != null
            && title.length() > 0) {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            LineMetrics lm = font.getLineMetrics(title, fontRenderContext);
            topThickness = Math.max((int)Math.ceil(lm.getHeight()), topThickness);
        }

        Component content = border.getContent();
        if (content != null) {
            content.setLocation(padding.left + thickness,
                padding.top + topThickness);

            int contentWidth = Math.max(width - (padding.left + padding.right
                + (thickness * 2)), 0);
            int contentHeight = Math.max(height - (padding.top + padding.bottom
                + (topThickness + thickness)), 0);

            content.setSize(contentWidth, contentHeight);
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        Border border = (Border)getComponent();
        int topThickness = thickness;
        float titleAscent = 0;

        String title = border.getTitle();
        if (title != null
            && title.length() > 0) {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            LineMetrics lm = font.getLineMetrics(title, fontRenderContext);
            titleAscent = lm.getAscent();
            topThickness = Math.max((int)Math.ceil(lm.getHeight()), topThickness);
        }

        // TODO Java2D doesn't support variable corner radii; we'll need to
        // "fake" this by drawing multiple arcs
        int cornerRadius = cornerRadii.topLeft;

        int width = getWidth();
        int height = getHeight();

        int strokeX = thickness / 2;
        int strokeY = topThickness / 2;
        int strokeWidth = Math.max(width - thickness, 0);
        int strokeHeight = Math.max(height - (int)Math.ceil((topThickness + thickness) * 0.5), 0);

        // Draw the background
        Paint backgroundPaint = getBackgroundPaint();
        if (backgroundPaint != null) {
            graphics.setPaint(backgroundPaint);

            if (cornerRadius > 0) {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

                graphics.fillRoundRect(strokeX, strokeY, strokeWidth, strokeHeight, cornerRadius, cornerRadius);

                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF);
            } else {
                graphics.fillRect(strokeX, strokeY, strokeWidth, strokeHeight);
            }
        }

        // Draw the title
        if (title != null) {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                fontRenderContext.getAntiAliasingHint());
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                fontRenderContext.getFractionalMetricsHint());

            // Note that we add one pixel to the string bounds for spacing
            Rectangle2D titleBounds = font.getStringBounds(title, fontRenderContext);
            titleBounds = new Rectangle2D.Double(padding.left + thickness,
                (topThickness - titleBounds.getHeight()) / 2,
                    titleBounds.getWidth() + 1, titleBounds.getHeight());

            graphics.setFont(font);
            graphics.setPaint(titleColor);
            graphics.drawString(title, (int)titleBounds.getX(),
                (int)(titleBounds.getY() + titleAscent));

            Area titleClip = new Area(graphics.getClip());
            titleClip.subtract(new Area(titleBounds));
            graphics.clip(titleClip);
        }

        // Draw the border
        if (thickness > 0) {
            graphics.setPaint(color);

            if (cornerRadius > 0) {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

                graphics.setStroke(new BasicStroke(thickness));
                graphics.draw(new RoundRectangle2D.Double(0.5 * thickness, 0.5 * topThickness,
                    strokeWidth, strokeHeight, cornerRadius, cornerRadius));

                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF);
            } else {
                int y = (topThickness - thickness) / 2;
                GraphicsUtilities.drawRect(graphics, 0, y, width, Math.max(height - y, 0), thickness);
            }
        }
    }

    /**
     * Returns the font used in rendering the title
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets the font used in rendering the title
     */
    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        this.font = font;
        invalidateComponent();
    }

    /**
     * Sets the font used in rendering the title
     * @param font A {@link ComponentSkin#decodeFont(String) font specification}
     */
    public final void setFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(decodeFont(font));
    }

    /**
     * Sets the font used in rendering the title
     * @param font A dictionary {@link Theme#deriveFont describing a font}
     */
    public final void setFont(Dictionary<String, ?> font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Theme.deriveFont(font));
    }

    /**
     * Returns the color of the border
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the color of the border
     */
    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.color = color;
        repaintComponent();
    }

    /**
     * Sets the color of the border
     * @param color Any of the {@linkplain GraphicsUtilities#decodeColor color values recognized by Pivot}.
     */
    public final void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(GraphicsUtilities.decodeColor(color));
    }

    public Color getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(Color titleColor) {
        if (titleColor == null) {
            throw new IllegalArgumentException("titleColor is null.");
        }

        this.titleColor = titleColor;
        repaintComponent();
    }

    public final void setTitleColor(String titleColor) {
        if (titleColor == null) {
            throw new IllegalArgumentException("titleColor is null.");
        }

        setTitleColor(GraphicsUtilities.decodeColor(titleColor));
    }

    /**
     * Returns the thickness of the border
     */
    public int getThickness() {
        return thickness;
    }

    /**
     * Sets the thickness of the border
     */
    public void setThickness(int thickness) {
        if (thickness < 0) {
            throw new IllegalArgumentException("thickness is negative.");
        }

        this.thickness = thickness;
        invalidateComponent();
    }

    /**
     * Sets the thickness of the border
     */
    public void setThickness(Number thickness) {
        if (thickness == null) {
            throw new IllegalArgumentException("thickness is null.");
        }

        setThickness(thickness.intValue());
    }

    /**
     * Returns the amount of space between the edge of the Border and its content.
     */
    public Insets getPadding() {
        return padding;
    }

    /**
     * Sets the amount of space to leave between the edge of the Border and its content.
     */
    public void setPadding(Insets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        this.padding = padding;
        invalidateComponent();
    }

    /**
     * Sets the amount of space to leave between the edge of the Border and its content.
     *
     * @param padding A dictionary with keys in the set {left, top, bottom, right}.
     */
    public final void setPadding(Dictionary<String, ?> padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the Border and its content,
     * uniformly on all four edges.
     */
    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the Border and its content,
     * uniformly on all four edges.
     */
    public void setPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(padding.intValue());
    }

    /**
     * Sets the amount of space to leave between the edge of the Border and its content.
     *
     * @param padding A string containing an integer or a JSON dictionary with keys
     * left, top, bottom, and/or right.
     */
    public final void setPadding(String padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(Insets.decode(padding));
    }

    /**
     * Returns a {@link CornerRadii}, describing the radius of each of the Border's corners.
     */
    public CornerRadii getCornerRadii() {
        return cornerRadii;
    }

    /**
     * Sets the radii of the Border's corners
     */
    public void setCornerRadii(CornerRadii cornerRadii) {
        if (cornerRadii == null) {
            throw new IllegalArgumentException("cornerRadii is null.");
        }

        this.cornerRadii = cornerRadii;
        repaintComponent();
    }

    /**
     * Sets the radii of the Border's corners
     * @param cornerRadii A Dictionary
     * {@link CornerRadii#CornerRadii(Dictionary) specifying the four corners}
     */
    public final void setCornerRadii(Dictionary<String, ?> cornerRadii) {
        if (cornerRadii == null) {
            throw new IllegalArgumentException("cornerRadii is null.");
        }

        setCornerRadii(new CornerRadii(cornerRadii));
    }

    /**
     * Sets the radii of the Border's four corners to the same value
     */
    public final void setCornerRadii(int cornerRadii) {
        setCornerRadii(new CornerRadii(cornerRadii));
    }

    /**
     * Sets the radii of the Border's four corners to the same value
     */
    public final void setCornerRadii(Number cornerRadii) {
        if (cornerRadii == null) {
            throw new IllegalArgumentException("cornerRadii is null.");
        }

        setCornerRadii(cornerRadii.intValue());
    }

    /**
     * Sets the radii of the Border's corners
     * @param cornerRadii A single integer value, or a JSON dictionary
     * {@link CornerRadii#CornerRadii(Dictionary) specifying the four corners}
     */
    public final void setCornerRadii(String cornerRadii) {
        if (cornerRadii == null) {
            throw new IllegalArgumentException("cornerRadii is null.");
        }

        setCornerRadii(CornerRadii.decode(cornerRadii));
    }

    // Border events
    @Override
    public void titleChanged(Border border, String previousTitle) {
        invalidateComponent();
    }

    @Override
    public void contentChanged(Border border, Component previousContent) {
        invalidateComponent();
    }
}
