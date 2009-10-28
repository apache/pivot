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
package org.apache.pivot.wtk.media.drawing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.text.StringCharacterIterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Platform;

/**
 * Shape representing a block of text.
 */
public class Text extends Shape {
    private static class TextListenerList extends ListenerList<TextListener>
        implements TextListener {
        public void textChanged(Text text, String previousText) {
            for (TextListener listener : this) {
                listener.textChanged(text, previousText);
            }
        }

        public void fontChanged(Text text, Font previousFont) {
            for (TextListener listener : this) {
                listener.fontChanged(text, previousFont);
            }
        }

        public void widthChanged(Text text, int previousWidth) {
            for (TextListener listener : this) {
                listener.widthChanged(text, previousWidth);
            }
        }

        public void alignmentChanged(Text text, HorizontalAlignment previousAlignment) {
            for (TextListener listener : this) {
                listener.alignmentChanged(text, previousAlignment);
            }
        }
    }

    private String text = null;
    private Font font = DEFAULT_FONT;
    private int width = -1;
    private HorizontalAlignment alignment = HorizontalAlignment.CENTER;

    private ArrayList<GlyphVector> glyphVectors = null;

    private TextListenerList textListeners = new TextListenerList();

    public static final Font DEFAULT_FONT = new Font("Verdana", Font.PLAIN, 11);
    private static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(null, true, true);

    public Text() {
        setFill(Color.BLACK);
        setStrokeThickness(0);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        String previousText = this.text;
        if (previousText != text) {
            this.text = text;
            invalidate();
            textListeners.textChanged(this, previousText);
        }
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        Font previousFont = this.font;
        if (previousFont != font) {
            this.font = font;
            invalidate();
            textListeners.fontChanged(this, previousFont);
        }
    }

    public final void setFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Font.decode(font));
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        if (width < -1) {
            throw new IllegalArgumentException();
        }

        int previousWidth = this.width;
        if (previousWidth != width) {
            this.width = width;
            invalidate();
            textListeners.widthChanged(this, previousWidth);
        }
    }

    public HorizontalAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(HorizontalAlignment alignment) {
        if (alignment == null) {
            throw new IllegalArgumentException();
        }

        HorizontalAlignment previousAlignment = this.alignment;
        if (previousAlignment != alignment) {
            this.alignment = alignment;
            update();
            textListeners.alignmentChanged(this, previousAlignment);
        }
    }

    public final void setAlignment(String alignment) {
        if (alignment == null) {
            throw new IllegalArgumentException();
        }

        setAlignment(HorizontalAlignment.valueOf(alignment.toUpperCase()));
    }

    @Override
    public boolean contains(int x, int y) {
        // TODO Perform hit testing on the glyph vectors themselves

        Bounds bounds = getBounds();

        return (x >= 0
            && x < bounds.width
            && y >= 0
            && y < bounds.height);
    }

    @Override
    public void draw(Graphics2D graphics) {
        int width = getWidth();

        // Draw the text
        if (glyphVectors.getLength() > 0) {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            if (FONT_RENDER_CONTEXT.isAntiAliased()) {
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    Platform.getTextAntialiasingHint());
            }

            if (FONT_RENDER_CONTEXT.usesFractionalMetrics()) {
                graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            }

            Paint fill = getFill();
            Paint stroke = getStroke();
            int strokeThickness = getStrokeThickness();

            LineMetrics lm = font.getLineMetrics("", FONT_RENDER_CONTEXT);
            float ascent = lm.getAscent();

            float y = 0;

            for (int i = 0, n = glyphVectors.getLength(); i < n; i++) {
                GlyphVector glyphVector = glyphVectors.get(i);

                Rectangle2D logicalBounds = glyphVector.getLogicalBounds();
                float lineWidth = (float)logicalBounds.getWidth();

                float x = 0;
                switch(alignment) {
                    case LEFT: {
                        x = 0;
                        break;
                    }

                    case RIGHT: {
                        x = width - lineWidth;
                        break;
                    }

                    case CENTER: {
                        x = (width - lineWidth) / 2;
                        break;
                    }
                }

                if (fill != null) {
                    graphics.setFont(font);
                    graphics.setPaint(fill);
                    graphics.drawGlyphVector(glyphVector, x, y + ascent);
                }

                // TODO Would caching the outlines help optimize this method, or are they
                // already cached by the glyph vector itself?
                if (stroke != null
                    && strokeThickness > 0) {
                    java.awt.Shape outline = glyphVector.getOutline();

                    graphics.setPaint(stroke);
                    graphics.setStroke(new BasicStroke(strokeThickness));

                    graphics.translate(x, y + ascent);
                    graphics.draw(outline);
                    graphics.translate(-x, -(y + ascent));
                }

                y += logicalBounds.getHeight();
            }
        }
    }

    @Override
    protected void validate() {
        if (!isValid()) {
            glyphVectors = new ArrayList<GlyphVector>();

            int width, height;
            if (this.width == -1) {
                if (text == null
                    || text.length() == 0) {
                    width = 0;
                    height = 0;
                } else {
                    // Create a single glyph vector representing the entire string
                    GlyphVector glyphVector = font.createGlyphVector(FONT_RENDER_CONTEXT, text);
                    glyphVectors.add(glyphVector);

                    Rectangle2D logicalBounds = glyphVector.getLogicalBounds();
                    width = (int)Math.ceil(logicalBounds.getWidth());
                    height = (int)Math.ceil(logicalBounds.getHeight());
                }
            } else {
                float textWidth = 0;
                float textHeight = 0;

                int n = text.length();
                if (n > 0) {
                    float lineWidth = 0;
                    int lastWhitespaceIndex = -1;

                    int start = 0;
                    int i = 0;
                    while (i < n) {
                        char c = text.charAt(i);
                        if (Character.isWhitespace(c)) {
                            lastWhitespaceIndex = i;
                        }

                        Rectangle2D characterBounds = font.getStringBounds(text, i, i + 1,
                            FONT_RENDER_CONTEXT);
                        lineWidth += characterBounds.getWidth();

                        if (lineWidth > this.width
                            && lastWhitespaceIndex != -1) {
                            i = lastWhitespaceIndex;

                            lineWidth = 0;
                            lastWhitespaceIndex = -1;

                            // Append the current line
                            if ((i - 1) - start > 0) {
                                StringCharacterIterator line = new StringCharacterIterator(text, start, i, start);
                                GlyphVector glyphVector = font.createGlyphVector(FONT_RENDER_CONTEXT, line);
                                glyphVectors.add(glyphVector);

                                Rectangle2D logicalBounds = glyphVector.getLogicalBounds();
                                textWidth = (float)Math.max(logicalBounds.getWidth(), textWidth);
                                textHeight += logicalBounds.getHeight();
                            }

                            start = i + 1;
                        }

                        i++;
                    }

                    // Append the final line
                    if ((i - 1) - start > 0) {
                        StringCharacterIterator line = new StringCharacterIterator(text, start, i, start);
                        GlyphVector glyphVector = font.createGlyphVector(FONT_RENDER_CONTEXT, line);
                        glyphVectors.add(glyphVector);

                        Rectangle2D logicalBounds = glyphVector.getLogicalBounds();
                        textWidth = (float)Math.max(logicalBounds.getWidth(), textWidth);
                        textHeight += logicalBounds.getHeight();
                    }

                    width = (int)Math.ceil(textWidth);
                    height = (int)Math.ceil(textHeight);
                } else {
                    width = this.width;
                    height = 0;
                }
            }

            setBounds(0, 0, width, height);
        }
    }

    public ListenerList<TextListener> getTextListeners() {
        return textListeners;
    }
}
