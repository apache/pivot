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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

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

    private ArrayList<TextLayout> lines = new ArrayList<TextLayout>();
    private FontRenderContext fontRenderContext = new FontRenderContext(null, true, true);

    private TextListenerList textListeners = new TextListenerList();

    public static final Font DEFAULT_FONT = new Font("Verdana", Font.PLAIN, 11);

    public Text() {
        super.setFill(Color.BLACK);
        super.setStroke((Paint)null);
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
            throw new IllegalArgumentException();
        }

        setFont(Font.decode(font));
    }

    public int getFontSize() {
        return font.getSize();
    }

    public final void setFontSize(int fontSize) {
        setFont(font.deriveFont((float)fontSize));
    }

    public boolean isFontBold() {
        return ((font.getStyle() & Font.BOLD) == Font.BOLD);
    }

    public final void setFontBold(boolean fontBold) {
        if (isFontBold() != fontBold) {
            if (fontBold) {
                setFont(font.deriveFont(Font.BOLD));
            } else {
                setFont(font.deriveFont(font.getStyle() & (~Font.BOLD)));
            }
        }
    }

    public boolean isFontItalic() {
        return ((font.getStyle() & Font.ITALIC) == Font.ITALIC);
    }

    public final void setFontItalic(boolean fontItalic) {
        if (isFontItalic() != fontItalic) {
            if (fontItalic) {
                setFont(font.deriveFont(Font.ITALIC));
            } else {
                setFont(font.deriveFont(font.getStyle() & (~Font.ITALIC)));
            }
        }
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
    public void setFill(Paint fill) {
        if (fill == null) {
            // Text must have a fill
            throw new IllegalArgumentException();
        }

        super.setFill(fill);
    }

    @Override
    public void setStroke(Paint stroke) {
        // Text cannot have a stroke
        throw new UnsupportedOperationException();
    }

    @Override
    public void draw(Graphics2D graphics) {
        if (fontRenderContext.isAntiAliased()) {
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                Platform.getTextAntialiasingHint());
        }

        if (fontRenderContext.usesFractionalMetrics()) {
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        }

        graphics.setFont(font);
        graphics.setPaint(getFill());

        Bounds bounds = getBounds();

        float y = 0;
        for (TextLayout line : lines) {
            Rectangle2D lineBounds = line.getBounds();

            float x;
            switch (alignment) {
                case LEFT: {
                    x = 0;
                    break;
                }

                case RIGHT: {
                    x = bounds.width - (float)(lineBounds.getX() + lineBounds.getWidth());
                    break;
                }

                case CENTER: {
                    x = (bounds.width - (float)(lineBounds.getX() + lineBounds.getWidth())) / 2f;
                    break;
                }

                default: {
                    throw new UnsupportedOperationException();
                }
            }

            y += line.getAscent();
            line.draw(graphics, x, y);
            y += line.getDescent() + line.getLeading();
        }
    }

    @Override
    protected void validate() {
        if (!isValid()) {
            lines = new ArrayList<TextLayout>();

            int width, height;
            if (this.width == -1) {
                if (text == null
                    || text.length() == 0) {
                    width = 0;
                    height = 0;
                } else {
                    TextLayout line = new TextLayout(text, font, fontRenderContext);
                    lines.add(line);
                    Rectangle2D lineBounds = line.getBounds();
                    width = (int)Math.ceil(lineBounds.getWidth());
                    height = (int)Math.ceil(line.getAscent() + line.getDescent()
                        + line.getLeading());
                }
            } else {
                width = this.width;

                AttributedString attributedText = new AttributedString(text);
                attributedText.addAttribute(TextAttribute.FONT, font);

                AttributedCharacterIterator aci = attributedText.getIterator();
                LineBreakMeasurer lbm = new LineBreakMeasurer(aci, fontRenderContext);

                float lineHeights = 0;
                while (lbm.getPosition() < aci.getEndIndex()) {
                    TextLayout line = lbm.nextLayout(width);
                    lines.add(line);
                    lineHeights += line.getAscent() + line.getDescent()
                        + line.getLeading();
                }

                height = (int)Math.ceil(lineHeights);
            }

            setBounds(0, 0, width, height);
        }
    }

    public ListenerList<TextListener> getTextListeners() {
        return textListeners;
    }
}
