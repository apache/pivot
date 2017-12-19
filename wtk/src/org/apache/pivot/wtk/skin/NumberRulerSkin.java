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
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.NumberRuler;
import org.apache.pivot.wtk.NumberRulerListener;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Theme;

public class NumberRulerSkin extends ComponentSkin implements NumberRulerListener {
    private Font font;
    private Color color;
    private Color backgroundColor;
    private int padding = 2;

    @Override
    public void install(Component component) {
        super.install(component);

        Theme theme = Theme.getTheme();
        font = theme.getFont();
        color = theme.getColor(0);
        backgroundColor = theme.getColor(19);

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

        return (orientation == Orientation.HORIZONTAL) ? 20 : 0;
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

    @Override
    public void paint(Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();
        Rectangle clipRect = graphics.getClipBounds();

        NumberRuler ruler = (NumberRuler) getComponent();
        int textSize = ruler.getTextSize();

        graphics.setColor(backgroundColor);
        graphics.fillRect(0, 0, width, height);

        graphics.setColor(color);

        Orientation orientation = ruler.getOrientation();
        Rectangle fullRect = new Rectangle(width, height);
        Rectangle clippedRect = fullRect.intersection(clipRect);

        switch (orientation) {
            case HORIZONTAL: {
                Rectangle lineRect = new Rectangle(0, height - 1, width - 1, 0);
                Rectangle clippedLineRect = lineRect.intersection(clipRect);
                graphics.drawLine(clippedLineRect.x, clippedLineRect.y, clippedLineRect.x + clippedLineRect.width, clippedLineRect.y);

                for (int i = 0, n = width / 5 + 1; i < n; i++) {
                    int x = i * 5;

                    if (i % 4 == 0) {
                        graphics.drawLine(x, 0, x, height / 2);
                    } else {
                        graphics.drawLine(x, 0, x, height / 4);
                    }
                }
                // TODO: put in the numbers every so often

                break;
            }

            case VERTICAL: {
                Rectangle lineRect = new Rectangle(width - 1, 0, 0, height - 1);
                Rectangle clippedLineRect = lineRect.intersection(clipRect);
                graphics.drawLine(clippedLineRect.x, clippedLineRect.y, clippedLineRect.x, clippedLineRect.y + clippedLineRect.height);

                FontRenderContext fontRenderContext = Platform.getFontRenderContext();
                LineMetrics lm = font.getLineMetrics("", fontRenderContext);
                float descent = lm.getDescent();
                int lineHeight = (int)Math.ceil(lm.getHeight());
                graphics.setFont(font);

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

}
