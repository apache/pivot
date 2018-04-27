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
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Utils;
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
 * Border skin. <p> TODO Add styles to support different border styles (e.g.
 * inset, outset) or create subclasses for these border types.
 */
public class BorderSkin extends ContainerSkin implements BorderListener {
    private Font font;
    private Color color;
    private Color titleColor;
    private int thickness;
    private int topThickness;
    private float titleAscent;
    private Insets padding;
    private CornerRadii cornerRadii;

    public BorderSkin() {
        font = currentTheme().getFont().deriveFont(Font.BOLD);

        setBackgroundColor(defaultBackgroundColor());
        color = defaultForegroundColor();
        titleColor = defaultForegroundColor();

        thickness = topThickness = 1;
        titleAscent = 0.0f;
        padding = Insets.NONE;
        cornerRadii = CornerRadii.NONE;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Border border = (Border) component;
        border.getBorderListeners().add(this);
    }

    private int paddingThicknessWidth() {
        return padding.getWidth() + (thickness * 2);
    }

    private int paddingThicknessHeight() {
        return padding.getHeight() + (topThickness + thickness);
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        Border border = (Border) getComponent();

        String title = border.getTitle();
        if (title != null && title.length() > 0) {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            Rectangle2D headingBounds = font.getStringBounds(title, fontRenderContext);
            preferredWidth = (int) Math.ceil(headingBounds.getWidth());
        }

        Component content = border.getContent();
        if (content != null) {
            int heightUpdated = height;
            if (heightUpdated != -1) {
                heightUpdated = Math.max(heightUpdated - paddingThicknessHeight(), 0);
            }

            preferredWidth = Math.max(preferredWidth, content.getPreferredWidth(heightUpdated));
        }

        preferredWidth += paddingThicknessWidth();

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        Border border = (Border) getComponent();

        Component content = border.getContent();
        if (content != null) {
            int widthUpdated = width;
            if (widthUpdated != -1) {
                widthUpdated = Math.max(widthUpdated - paddingThicknessWidth(), 0);
            }

            preferredHeight = content.getPreferredHeight(widthUpdated);
        }

        preferredHeight += paddingThicknessHeight();

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        Border border = (Border) getComponent();

        String title = border.getTitle();
        if (title != null && title.length() > 0) {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            Rectangle2D headingBounds = font.getStringBounds(title, fontRenderContext);
            preferredWidth = (int) Math.ceil(headingBounds.getWidth());
        }

        Component content = border.getContent();
        if (content != null) {
            Dimensions preferredSize = content.getPreferredSize();
            preferredWidth = Math.max(preferredWidth, preferredSize.width);
            preferredHeight += preferredSize.height;
        }

        preferredWidth += paddingThicknessWidth();
        preferredHeight += paddingThicknessHeight();

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        int baseline = -1;

        Border border = (Border) getComponent();

        // Delegate baseline calculation to the content component
        Component content = border.getContent();
        if (content != null) {
            int clientWidth = Math.max(width - paddingThicknessWidth(), 0);
            int clientHeight = Math.max(height - paddingThicknessHeight(), 0);

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

        Border border = (Border) getComponent();

        Component content = border.getContent();
        if (content != null) {
            content.setLocation(padding.left + thickness, padding.top + topThickness);

            int contentWidth = Math.max(width - paddingThicknessWidth(), 0);
            int contentHeight = Math.max(height - paddingThicknessHeight(), 0);

            content.setSize(contentWidth, contentHeight);
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        Border border = (Border) getComponent();

        String title = border.getTitle();

        // TODO Java2D doesn't support variable corner radii; we'll need to
        // "fake" this by drawing multiple arcs
        int cornerRadius = cornerRadii.topLeft;

        int width = getWidth();
        int height = getHeight();

        int strokeX = thickness / 2;
        int strokeY = topThickness / 2;
        int strokeWidth = Math.max(width - thickness, 0);
        int strokeHeight = Math.max(height - (int) Math.ceil((topThickness + thickness) * 0.5), 0);

        // Draw the background
        Paint backgroundPaint = getBackgroundPaint();
        if (backgroundPaint != null) {
            graphics.setPaint(backgroundPaint);

            if (cornerRadius > 0) {
                GraphicsUtilities.setAntialiasingOn(graphics);

                graphics.fillRoundRect(strokeX, strokeY, strokeWidth, strokeHeight,
                    cornerRadius, cornerRadius);

                GraphicsUtilities.setAntialiasingOff(graphics);
            } else {
                graphics.fillRect(strokeX, strokeY, strokeWidth, strokeHeight);
            }
        }

        // Draw the title
        if (title != null) {
            FontRenderContext fontRenderContext = GraphicsUtilities.prepareForText(graphics, font, titleColor);

            // Note that we add one pixel to the string bounds for spacing
            Rectangle2D titleBounds = font.getStringBounds(title, fontRenderContext);
            titleBounds = new Rectangle2D.Double(
                padding.left + thickness, (topThickness - titleBounds.getHeight()) / 2,
                titleBounds.getWidth() + 1, titleBounds.getHeight());

            graphics.drawString(title, (int) titleBounds.getX(),
                (int) (titleBounds.getY() + titleAscent));

            Area titleClip = new Area(graphics.getClip());
            titleClip.subtract(new Area(titleBounds));
            graphics.clip(titleClip);
        }

        // Draw the border
        if (thickness > 0 && !themeIsFlat()) {
            graphics.setPaint(color);

            if (cornerRadius > 0) {
                GraphicsUtilities.setAntialiasingOn(graphics);

                graphics.setStroke(new BasicStroke(thickness));
                graphics.draw(new RoundRectangle2D.Double(0.5 * thickness, 0.5 * topThickness,
                    strokeWidth, strokeHeight, cornerRadius, cornerRadius));

                GraphicsUtilities.setAntialiasingOff(graphics);
            } else {
                int y = (topThickness - thickness) / 2;
                GraphicsUtilities.drawRect(graphics, 0, y, width, Math.max(height - y, 0),
                    thickness);
            }
        }
    }

    /**
     * @return The font used in rendering the title.
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets the font used in rendering the title.
     *
     * @param font The new font to use for the border title.
     */
    public void setFont(Font font) {
        Utils.checkNull(font, "font");

        this.font = font;
        invalidateComponent();
    }

    /**
     * Sets the font used in rendering the title.
     *
     * @param font A {@linkplain ComponentSkin#decodeFont(String) font specification}.
     */
    public final void setFont(String font) {
        setFont(decodeFont(font));
    }

    /**
     * Sets the font used in rendering the title.
     *
     * @param font A dictionary {@linkplain Theme#deriveFont describing a font}.
     */
    public final void setFont(Dictionary<String, ?> font) {
        setFont(Theme.deriveFont(font));
    }

    /**
     * @return The color of the border.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the color of the border.
     *
     * @param color The new color for the border.
     */
    public void setColor(Color color) {
        Utils.checkNull(color, "color");

        this.color = color;
        repaintComponent();
    }

    /**
     * Sets the color of the border.
     *
     * @param color Any of the {@linkplain GraphicsUtilities#decodeColor color
     * values recognized by Pivot}.
     */
    public final void setColor(String color) {
        setColor(GraphicsUtilities.decodeColor(color, "color"));
    }

    public Color getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(Color titleColor) {
        Utils.checkNull(titleColor, "titleColor");

        this.titleColor = titleColor;
        repaintComponent();
    }

    public final void setTitleColor(String titleColor) {
        setTitleColor(GraphicsUtilities.decodeColor(titleColor, "titleColor"));
    }

    /**
     * @return The thickness of the border.
     */
    public int getThickness() {
        return thickness;
    }

    /**
     * Sets the thickness of the border.
     *
     * @param thickness The border thickness (in pixels).
     */
    public void setThickness(int thickness) {
        Utils.checkNonNegative(thickness, "thickness");

        this.thickness = thickness;
        invalidateComponent();
    }

    /**
     * Sets the thickness of the border.
     *
     * @param thickness The border thickness (integer value in pixels).
     */
    public void setThickness(Number thickness) {
        Utils.checkNull(thickness, "thickness");

        setThickness(thickness.intValue());
    }

    /**
     * @return The amount of space between the edge of the Border and its
     * content.
     */
    public Insets getPadding() {
        return padding;
    }

    /**
     * Sets the amount of space to leave between the edge of the Border and its
     * content.
     *
     * @param padding The set of padding values.
     */
    public void setPadding(Insets padding) {
        Utils.checkNull(padding, "padding");

        this.padding = padding;
        invalidateComponent();
    }

    /**
     * Sets the amount of space to leave between the edge of the Border and its
     * content.
     *
     * @param padding A dictionary with keys in the set {top, left, bottom, right}.
     */
    public final void setPadding(Dictionary<String, ?> padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the Border and its
     * content.
     *
     * @param padding A sequence with values in the order [top, left, bottom, right].
     */
    public final void setPadding(Sequence<?> padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the Border and its
     * content, uniformly on all four edges.
     *
     * @param padding The padding value (in pixels) to use for all four sides.
     */
    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the Border and its
     * content, uniformly on all four edges.
     *
     * @param padding The padding value (integer value in pixels) to use for all four sides.
     */
    public void setPadding(Number padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the Border and its
     * content.
     *
     * @param padding A string containing an integer or a JSON dictionary with
     * keys left, top, bottom, and/or right.
     */
    public final void setPadding(String padding) {
        setPadding(Insets.decode(padding));
    }

    /**
     * @return A {@link CornerRadii}, describing the radius of each of the
     * Border's corners.
     */
    public CornerRadii getCornerRadii() {
        return cornerRadii;
    }

    /**
     * Sets the radii of the Border's corners.
     *
     * @param cornerRadii The radii for each of the corners.
     */
    public void setCornerRadii(CornerRadii cornerRadii) {
        Utils.checkNull(cornerRadii, "cornerRadii");

        this.cornerRadii = cornerRadii;
        repaintComponent();
    }

    /**
     * Sets the radii of the Border's corners.
     *
     * @param cornerRadii A Dictionary
     * {@linkplain CornerRadii#CornerRadii(Dictionary) specifying the four corners}.
     */
    public final void setCornerRadii(Dictionary<String, ?> cornerRadii) {
        setCornerRadii(new CornerRadii(cornerRadii));
    }

    /**
     * Sets the radii of the Border's four corners to the same value.
     *
     * @param cornerRadii The integer value to set all four corners' radii.
     */
    public final void setCornerRadii(int cornerRadii) {
        setCornerRadii(new CornerRadii(cornerRadii));
    }

    /**
     * Sets the radii of the Border's four corners to the same value.
     *
     * @param cornerRadii The value for the radii (integer value in pixels).
     */
    public final void setCornerRadii(Number cornerRadii) {
        setCornerRadii(new CornerRadii(cornerRadii));
    }

    /**
     * Sets the radii of the Border's corners.
     *
     * @param cornerRadii A single integer value, or a JSON dictionary
     * {@linkplain CornerRadii#CornerRadii(Dictionary) specifying the four corners}.
     */
    public final void setCornerRadii(String cornerRadii) {
        setCornerRadii(CornerRadii.decode(cornerRadii));
    }

    // Border events
    @Override
    public void titleChanged(Border border, String previousTitle) {
        // Redo the top thickness calculation when the title changes
        topThickness = thickness;
        titleAscent = 0f;

        String title = border.getTitle();
        if (title != null && title.length() > 0) {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            LineMetrics lm = font.getLineMetrics(title, fontRenderContext);
            titleAscent = lm.getAscent();
            topThickness = Math.max((int) Math.ceil(lm.getHeight()), topThickness);
        }

        invalidateComponent();
    }

    @Override
    public void contentChanged(Border border, Component previousContent) {
        invalidateComponent();
    }
}
