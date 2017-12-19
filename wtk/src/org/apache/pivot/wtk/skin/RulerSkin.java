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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.text.StringCharacterIterator;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Borders;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.CSSColor;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Ruler;
import org.apache.pivot.wtk.RulerListener;
import org.apache.pivot.wtk.Theme;

public class RulerSkin extends ComponentSkin implements RulerListener {
    private static final int MAJOR_SIZE = 10;
    private static final int MINOR_SIZE = 8;
    private static final int REGULAR_SIZE = 5;

    private Color color;
    private Color backgroundColor;
    private int markerSpacing;
    private Insets markerInsets;
    private boolean flip;
    private Borders borders;
    private int majorDivision;
    private int minorDivision;
    private boolean showMajorNumbers;
    private boolean showMinorNumbers;
    private Font font;
    private float charWidth, charHeight, descent;

    public RulerSkin() {
        // For now the default colors are not from the Theme.
        setColor(Color.BLACK);
        setBackgroundColor(CSSColor.LightYellow.getColor());

        markerSpacing = 5;
        markerInsets = new Insets(0);
        flip = false;
        borders = Borders.ALL;
        majorDivision = 4;
        minorDivision = 2;
        showMajorNumbers = showMinorNumbers = false;

        Theme theme = currentTheme();
        setFont(theme.getFont());
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Ruler ruler = (Ruler) component;
        ruler.getRulerListeners().add(this);
    }

    @Override
    public void layout() {
        // No-op
    }

    @Override
    public int getPreferredHeight(int width) {
        Ruler ruler = (Ruler) getComponent();
        Orientation orientation = ruler.getOrientation();

        // Give a little extra height if showing numbers
        return (orientation == Orientation.HORIZONTAL) ?
            ((showMajorNumbers || showMinorNumbers) ?
                ((int)Math.ceil(charHeight) + MAJOR_SIZE + 5) : MAJOR_SIZE * 2) : 0;
    }

    @Override
    public int getPreferredWidth(int height) {
        Ruler ruler = (Ruler) getComponent();
        Orientation orientation = ruler.getOrientation();

        // Give a little extra width if showing numbers
        return (orientation == Orientation.VERTICAL) ?
            ((showMajorNumbers || showMinorNumbers) ?
                ((int)Math.ceil(charWidth) + MAJOR_SIZE + 5) : MAJOR_SIZE * 2) : 0;
    }

    private void showNumber(Graphics2D graphics, FontRenderContext fontRenderContext, int number, int x, int y) {
        String num = Integer.toString(number);

        StringCharacterIterator line;
        GlyphVector glyphVector;
        Rectangle2D textBounds;
        float width, height;
        float fx, fy;

        Ruler ruler = (Ruler) getComponent();
        Orientation orientation = ruler.getOrientation();

        switch (orientation) {
            case HORIZONTAL:
                // Draw the whole number just off the tip of the line given by (x,y)
                line = new StringCharacterIterator(num);
                glyphVector = font.createGlyphVector(fontRenderContext, line);
                textBounds = glyphVector.getLogicalBounds();
                width = (float) textBounds.getWidth();
                height = (float) textBounds.getHeight();
                fx = (float)x - (width / 2.0f);
                if (flip) {
                    fy = (float)(y - 2);
                } else {
                    fy = (float)(y - 1) + height;
                }
                graphics.drawGlyphVector(glyphVector, fx, fy);
                break;
            case VERTICAL:
                // Draw the number one digit at a time, vertically just off the tip of the line
                if (flip) {
                    fx = (float)(x - 1) - charWidth;
                } else {
                    fx = (float)(x + 3);
                }
                int numDigits = num.length();
                float heightAdjust = (numDigits % 2 == 1) ? charHeight / 2.0f : 0.0f;
                for (int i = 0; i < numDigits; i++) {
                    line = new StringCharacterIterator(num.substring(i, i + 1));
                    glyphVector = font.createGlyphVector(fontRenderContext, line);
                    int midDigit = (numDigits + 1) / 2;
                    if (i <= midDigit) {
                        fy = (float)y + heightAdjust - descent - (float)(midDigit - i - 1) * charHeight;
                    } else {
                        fy = (float)y + heightAdjust - descent + (float)(i - midDigit - 1) * charHeight;
                    }
                    graphics.drawGlyphVector(glyphVector, fx, fy);
                }
                break;
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();

        int top = markerInsets.top;
        int left = markerInsets.left;
        int bottom = height - markerInsets.bottom;
        int right = width - markerInsets.right;

        Ruler ruler = (Ruler) getComponent();

        graphics.setColor(backgroundColor);
        graphics.fillRect(0, 0, width, height);

        graphics.setColor(color);
        GraphicsUtilities.drawBorders(graphics, borders, 0, 0, height - 1, width - 1);

        height -= markerInsets.getHeight();
        width -= markerInsets.getWidth();

        FontRenderContext fontRenderContext = showMajorNumbers || showMinorNumbers ?
            GraphicsUtilities.prepareForText(graphics, font, color) : null;

        Orientation orientation = ruler.getOrientation();
        switch (orientation) {
            case HORIZONTAL: {
                int start = flip ? bottom - 1 : top;
                int end2 = flip ? (start - (MAJOR_SIZE - 1)) : (MAJOR_SIZE - 1);
                int end3 = flip ? (start - (MINOR_SIZE - 1)) : (MINOR_SIZE - 1);
                int end4 = flip ? (start - (REGULAR_SIZE - 1)) : (REGULAR_SIZE - 1);

                for (int i = 0, n = right / markerSpacing + 1; i < n; i++) {
                    int x = i * markerSpacing + left;

                    if (majorDivision != 0 && i % majorDivision == 0) {
                        graphics.drawLine(x, start, x, end2);
                        // Don't show any numbers at 0 -- make a style for this?
                        if (showMajorNumbers && i > 0) {
                            showNumber(graphics, fontRenderContext, i, x, end2);
                        }
                    } else if (minorDivision != 0 && i % minorDivision == 0) {
                        graphics.drawLine(x, start, x, end3);
                        if (showMinorNumbers && i > 0) {
                            // Show the minor numbers at the same y point as the major
                            showNumber(graphics, fontRenderContext, i, x, end2);
                        }
                    } else {
                        graphics.drawLine(x, start, x, end4);
                    }
                }

                break;
            }

            case VERTICAL: {
                int start = flip ? right - 1 : left;
                int end2 = flip ? (start - (MAJOR_SIZE - 1)) : (MAJOR_SIZE - 1);
                int end3 = flip ? (start - (MINOR_SIZE - 1)) : (MINOR_SIZE - 1);
                int end4 = flip ? (start - (REGULAR_SIZE - 1)) : (REGULAR_SIZE - 1);

                for (int i = 0, n = bottom / markerSpacing + 1; i < n; i++) {
                    int y = i * markerSpacing + top;

                    if (majorDivision != 0 && i % majorDivision == 0) {
                        graphics.drawLine(start, y, end2, y);
                        // Don't show any numbers at 0 -- make a style for this?
                        if (showMajorNumbers && i > 0) {
                            showNumber(graphics, fontRenderContext, i, end2, y);
                        }
                    } else if (minorDivision != 0 && i % minorDivision == 0) {
                        graphics.drawLine(start, y, end3, y);
                        if (showMinorNumbers && i > 0) {
                            showNumber(graphics, fontRenderContext, i, end3, y);
                        }
                    } else {
                        graphics.drawLine(start, y, end4, y);
                    }
                }

                break;
            }

            default: {
                break;
            }
        }
    }

    @Override
    public void orientationChanged(Ruler ruler) {
        invalidateComponent();
    }

    /**
     * @return The interval at which the "major" (that is, the long)
     * markers are drawn.
     */
    public int getMajorDivision() {
        return majorDivision;
    }

    /**
     * Set the major division interval.
     *
     * @param major The number of markers interval at which to draw
     * a "major" (long) marker.  Can be zero to not draw any major
     * markers.
     */
    public final void setMajorDivision(int major) {
        Utils.checkNonNegative(major, "majorDivision");

        // TODO: check for sanity of major vs. minor here??
        this.majorDivision = major;
        repaintComponent();
    }

    public final void setMajorDivision(Number major) {
        Utils.checkNull(major, "majorDivision");

        setMajorDivision(major.intValue());
    }

    /**
     * @return The interval at which the "minor" (that is, the slightly
     * longer than normal) markers are drawn.
     */
    public int getMinorDivision() {
        return minorDivision;
    }

    /**
     * Set the minor division interval.
     *
     * @param minor The number of markers interval at which to draw
     * a "minor" (slightly longer than normal) marker.  Can be zero
     * to not draw any minor markers.
     */
    public final void setMinorDivision(int minor) {
        Utils.checkNonNegative(minor, "minorDivision");

        // TODO: check for sanity of major vs. minor here??
        this.minorDivision = minor;
        repaintComponent();
    }

    public final void setMinorDivision(Number minor) {
        Utils.checkNull(minor, "minorDivision");

        setMinorDivision(minor.intValue());
    }

    /**
     * @return The number of pixels interval at which to draw markers.
     */
    public int getMarkerSpacing() {
        return markerSpacing;
    }

    /**
     * Set the number of pixels interval at which to draw the markers.
     *
     * @param spacing The number of pixels between markers (must be &gt;= 1).
     */
    public final void setMarkerSpacing(int spacing) {
        Utils.checkPositive(spacing, "markerSpacing");

        this.markerSpacing = spacing;
        invalidateComponent();
    }

    public final void setMarkerSpacing(Number spacing) {
        Utils.checkNull(spacing, "markerSpacing");

        setMarkerSpacing(spacing.intValue());
    }

    /**
     * @return Whether the ruler is "flipped", that is the markers
     * start from the inside rather than the outside.
     */
    public boolean getFlip() {
        return flip;
    }

    public final void setFlip(boolean flip) {
        this.flip = flip;
    }

    /**
     * @return Whether to display numbers at each major division.
     */
    public boolean getShowMajorNumbers() {
        return showMajorNumbers;
    }

    /**
     * Sets the flag to say whether to show numbers at each major division.
     *
     * @param showMajorNumbers Whether numbers should be shown for major divisions.
     */
    public final void setShowMajorNumbers(boolean showMajorNumbers) {
        this.showMajorNumbers = showMajorNumbers;
        invalidateComponent();
    }

    /**
     * @return Whether to display numbers at each minor division.
     */
    public boolean getShowMinorNumbers() {
        return showMinorNumbers;
    }

    /**
     * Sets the flag to say whether to show numbers at each minor division.
     *
     * @param showMinorNumbers Whether numbers should be shown for minor divisions.
     */
    public final void setShowMinorNumbers(boolean showMinorNumbers) {
        this.showMinorNumbers = showMinorNumbers;
        invalidateComponent();
    }

    /**
     * @return The border configuration for this ruler.
     */
    public Borders getBorders() {
        return borders;
    }

    public final void setBorders(Borders borders) {
        Utils.checkNull(borders, "borders");

        this.borders = borders;
        repaintComponent();
    }

    /**
     * @return The insets for the markers (on each edge).
     */
    public Insets getMarkerInsets() {
        return markerInsets;
    }

    public final void setMarkerInsets(Insets insets) {
        Utils.checkNull(insets, "markerInsets");

        this.markerInsets = insets;
        repaintComponent();
    }

    public final void setMarkerInsets(Dictionary<String, ?> insets) {
        setMarkerInsets(new Insets(insets));
    }

    public final void setMarkerInsets(Sequence<?> insets) {
        setMarkerInsets(new Insets(insets));
    }

    public final void setMarkerInsets(int insets) {
        setMarkerInsets(new Insets(insets));
    }

    public final void setMarkerInsets(Number insets) {
        setMarkerInsets(new Insets(insets));
    }

    public final void setMarkerInsets(String insets) {
        setMarkerInsets(Insets.decode(insets));
    }

    /**
     * Returns the foreground color for the markers of the ruler.
     *
     * @return The foreground (marker) color.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the foreground color for the markers of the ruler.
     *
     * @param color The foreground (that is, the marker) color.
     */
    public final void setColor(Color color) {
        Utils.checkNull(color, "color");

        this.color = color;
        repaintComponent();
    }

    /**
     * Sets the foreground color for the markers of the ruler.
     *
     * @param color Any of the {@linkplain GraphicsUtilities#decodeColor color
     * values recognized by Pivot}.
     */
    public final void setColor(String color) {
        setColor(GraphicsUtilities.decodeColor(color, "color"));
    }

    public final void setColor(int color) {
        Theme theme = currentTheme();
        setColor(theme.getColor(color));
    }

    /**
     * Returns the background color of the ruler.
     *
     * @return The current background color.
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color of the ruler.
     *
     * @param backgroundColor New background color value (can be {@code null}
     * for a transparent background).
     */
    public final void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    /**
     * Sets the background color of the ruler.
     *
     * @param backgroundColor Any of the
     * {@linkplain GraphicsUtilities#decodeColor color values recognized by
     * Pivot}.
     */
    public final void setBackgroundColor(String backgroundColor) {
        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor, "backgroundColor"));
    }

    public final void setBackgroundColor(int backgroundColor) {
        Theme theme = currentTheme();
        setBackgroundColor(theme.getColor(backgroundColor));
    }

    /**
     * @return The font used to format division numbers (if enabled).
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets the font used in rendering the Ruler's text.
     *
     * @param font The new font to use.
     */
    public void setFont(Font font) {
        Utils.checkNull(font, "font");

        // The font we will use is the same name and style, but a 11 pt type
        this.font = font.deriveFont(11.0f);

        // Make some size calculations for the drawing code
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();
        GlyphVector glyphVector = this.font.createGlyphVector(fontRenderContext, "0");
        Rectangle2D textBounds = glyphVector.getLogicalBounds();
        this.charWidth = (float)textBounds.getWidth();
        // Since we're just drawing numbers, the line spacing can be just the ascent value for the font
        LineMetrics lm = this.font.getLineMetrics("0", fontRenderContext);
        this.charHeight = lm.getAscent();
        this.descent = lm.getDescent();

        invalidateComponent();
    }

    /**
     * Sets the font used in rendering the Ruler's text.
     *
     * @param font A {@link ComponentSkin#decodeFont(String) font specification}
     */
    public final void setFont(String font) {
        setFont(decodeFont(font));
    }

    /**
     * Sets the font used in rendering the Ruler's text.
     *
     * @param font A dictionary {@link Theme#deriveFont describing a font}
     */
    public final void setFont(Dictionary<String, ?> font) {
        setFont(Theme.deriveFont(font));
    }

}
