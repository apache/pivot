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
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.text.StringCharacterIterator;
import java.util.Arrays;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.NumberRuler;
import org.apache.pivot.wtk.NumberRulerListener;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Theme;

public class NumberRulerSkin extends ComponentSkin implements NumberRulerListener {
    private static final int MAJOR_SIZE = 10;
    private static final int MINOR_SIZE = 8;
    private static final int REGULAR_SIZE = 5;

    private Font font;
    private Color color;
    private Color backgroundColor;
    private int padding = 2;
    private int markerSpacing;
    private Insets markerInsets;
    private int majorDivision;
    private int minorDivision;
    private boolean showMajorNumbers;
    private boolean showMinorNumbers;
    private float charHeight, descent;
    private int lineHeight;

    @Override
    public void install(Component component) {
        super.install(component);

        Theme theme = Theme.getTheme();
        setFont(theme.getFont());

        setColor(0);
        setBackgroundColor(19);

        markerSpacing = 5;
        markerInsets = new Insets(0);

        // Note: these aren't settable
        majorDivision = 10;
        minorDivision = 5;
        // But these are
        showMajorNumbers = true;
        showMinorNumbers = false;

        NumberRuler ruler = (NumberRuler) component;
        ruler.getRulerListeners().add(this);
    }

    @Override
    public void layout() {
        // No-op
    }

    @Override
    public int getPreferredHeight(int width) {
        NumberRuler ruler = (NumberRuler) getComponent();
        Orientation orientation = ruler.getOrientation();

        // Give a little extra height if showing numbers
        return (orientation == Orientation.HORIZONTAL) ?
            ((showMajorNumbers || showMinorNumbers) ?
                ((int)Math.ceil(charHeight) + MAJOR_SIZE + 5) : MAJOR_SIZE * 2) : 0;
    }

    @Override
    public int getPreferredWidth(int height) {
        NumberRuler ruler = (NumberRuler) getComponent();
        Orientation orientation = ruler.getOrientation();

        if (orientation == Orientation.VERTICAL) {
            int textSize = ruler.getTextSize();

            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            char[] digits = new char[textSize];
            Arrays.fill(digits, '0');
            String text = new String(digits);

            Rectangle2D stringBounds = font.getStringBounds(text, fontRenderContext);
            return (int) Math.ceil(stringBounds.getWidth()) + padding;
        }

        return 0;
    }

    private void showNumber(Graphics2D graphics, FontRenderContext fontRenderContext, int number, int x, int y) {
        String num = Integer.toString(number);

        StringCharacterIterator line;
        GlyphVector glyphVector;
        Rectangle2D textBounds;
        float width, height;
        float fx, fy;

        // Draw the whole number just off the tip of the line given by (x,y)
        line = new StringCharacterIterator(num);
        glyphVector = font.createGlyphVector(fontRenderContext, line);
        textBounds = glyphVector.getLogicalBounds();
        width = (float) textBounds.getWidth();
        height = (float) textBounds.getHeight();
        fx = (float)x - (width / 2.0f);
        fy = (float)(y - 2);
        graphics.drawGlyphVector(glyphVector, fx, fy);
    }

    @Override
    public void paint(Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();
        int bottom = height - markerInsets.bottom;

        Rectangle clipRect = graphics.getClipBounds();

        NumberRuler ruler = (NumberRuler) getComponent();
        int textSize = ruler.getTextSize();

        graphics.setColor(backgroundColor);
        graphics.fillRect(0, 0, width, height);

        graphics.setColor(color);

        FontRenderContext fontRenderContext = Platform.getFontRenderContext();
        graphics.setFont(font);

        Orientation orientation = ruler.getOrientation();
        Rectangle fullRect = new Rectangle(width, height);
        Rectangle clippedRect = fullRect.intersection(clipRect);

        switch (orientation) {
            case HORIZONTAL: {
                int start = bottom - 1;
                int end2 = start - (MAJOR_SIZE - 1);
                int end3 = start - (MINOR_SIZE - 1);
                int end4 = start - (REGULAR_SIZE - 1);

                Rectangle lineRect = new Rectangle(0, height - 1, width - 1, 0);
                Rectangle clippedLineRect = lineRect.intersection(clipRect);
                graphics.drawLine(clippedLineRect.x, clippedLineRect.y, clippedLineRect.x + clippedLineRect.width, clippedLineRect.y);

                for (int i = 0, n = width / markerSpacing + 1; i < n; i++) {
                    int x = i * markerSpacing + markerInsets.left;

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
                Rectangle lineRect = new Rectangle(width - 1, 0, 0, height - 1);
                Rectangle clippedLineRect = lineRect.intersection(clipRect);
                graphics.drawLine(clippedLineRect.x, clippedLineRect.y, clippedLineRect.x, clippedLineRect.y + clippedLineRect.height);

                // Optimize drawing by only starting just above the current clip bounds
                // down to the bottom (plus one) of the end of the clip bounds.
                // This is a 100x speed improvement for 500,000 lines.
                int linesAbove = clipRect.y / lineHeight;
                int linesBelow = (height - (clipRect.y + clipRect.height)) / lineHeight;
                int totalLines = height / lineHeight + 1;

                for (int num = 1 + linesAbove, n = totalLines - (linesBelow - 1); num < n; num++) {
                    float y = (float)(num * lineHeight) - descent;

                    StringCharacterIterator line = new StringCharacterIterator(Integer.toString(num));
                    GlyphVector glyphVector = font.createGlyphVector(fontRenderContext, line);
                    Rectangle2D textBounds = glyphVector.getLogicalBounds();
                    float lineWidth = (float) textBounds.getWidth();
                    float x = (float)width - (lineWidth + (float)padding);
                    graphics.drawGlyphVector(glyphVector, x, y);
                }

                break;
            }

            default: {
                break;
            }
        }
    }

    @Override
    public void orientationChanged(NumberRuler ruler) {
        invalidateComponent();
    }

    @Override
    public void textSizeChanged(NumberRuler ruler, int previousSize) {
        invalidateComponent();
    }

    /**
     * @return The insets for the markers (only applicable for horizontal
     * orientation).
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
     * @return The number of pixels interval at which to draw markers.
     */
    public int getMarkerSpacing() {
        return markerSpacing;
    }

    /**
     * Set the number of pixels interval at which to draw the markers
     * (for horizontal orientation only).
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
     * @return Whether to display numbers at each major division
     * (only applicable for horizontal orientation).
     */
    public boolean getShowMajorNumbers() {
        return showMajorNumbers;
    }

    /**
     * Sets the flag to say whether to show numbers at each major division
     * (only for horizontal orientation).
     *
     * @param showMajorNumbers Whether numbers should be shown for major divisions.
     */
    public final void setShowMajorNumbers(boolean showMajorNumbers) {
        this.showMajorNumbers = showMajorNumbers;

        NumberRuler ruler = (NumberRuler)getComponent();
        if (ruler.getOrientation() == Orientation.HORIZONTAL) {
            invalidateComponent();
        }
    }

    /**
     * @return Whether to display numbers at each minor division
     * (only for horizontal orientation).
     */
    public boolean getShowMinorNumbers() {
        return showMinorNumbers;
    }

    /**
     * Sets the flag to say whether to show numbers at each minor division
     * (for horizontal orientation only).
     *
     * @param showMinorNumbers Whether numbers should be shown for minor divisions.
     */
    public final void setShowMinorNumbers(boolean showMinorNumbers) {
        this.showMinorNumbers = showMinorNumbers;

        NumberRuler ruler = (NumberRuler)getComponent();
        if (ruler.getOrientation() == Orientation.HORIZONTAL) {
            invalidateComponent();
        }
    }

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

        this.font = font;

        // Make some size calculations for the drawing code
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();
        LineMetrics lm = this.font.getLineMetrics("0", fontRenderContext);
        this.charHeight = lm.getAscent();
        this.descent = lm.getDescent();
        this.lineHeight = (int)Math.ceil(lm.getHeight());

        invalidateComponent();
    }

    /**
     * Sets the font used in rendering the Ruler's text
     *
     * @param font A {@link ComponentSkin#decodeFont(String) font specification}
     */
    public final void setFont(String font) {
        setFont(decodeFont(font));
    }

    /**
     * Sets the font used in rendering the Ruler's text
     *
     * @param font A dictionary {@link Theme#deriveFont describing a font}
     */
    public final void setFont(Dictionary<String, ?> font) {
        setFont(Theme.deriveFont(font));
    }

    /**
     * Returns the foreground color of the text of the ruler.
     *
     * @return The foreground (text) color.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the foreground color of the text of the ruler.
     *
     * @param color The foreground (that is, the text) color.
     */
    public void setColor(Color color) {
        Utils.checkNull(color, "color");

        this.color = color;
        repaintComponent();
    }

    /**
     * Sets the foreground color of the text of the ruler.
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
     * @param backgroundColor New background color value.
     */
    public void setBackgroundColor(Color backgroundColor) {
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

}
