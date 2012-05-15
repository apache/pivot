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
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.text.CharacterIterator;

import org.apache.pivot.text.CharSequenceCharacterIterator;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TextPane;
import org.apache.pivot.wtk.text.Element;
import org.apache.pivot.wtk.text.TextNode;
import org.apache.pivot.wtk.text.TextNodeListener;

/**
 * Text node view.
 */
class TextPaneSkinTextNodeView extends TextPaneSkinNodeView implements TextNodeListener {
    private int start;
    private int length = 0;
    private GlyphVector glyphVector = null;
    private TextPaneSkinTextNodeView next = null;

    public TextPaneSkinTextNodeView(TextNode textNode) {
        this(textNode, 0);
    }

    public TextPaneSkinTextNodeView(TextNode textNode, int start) {
        super(textNode);
        this.start = start;
    }

    @Override
    protected void attach() {
        super.attach();

        TextNode textNode = (TextNode)getNode();
        textNode.getTextNodeListeners().add(this);
    }

    @Override
    protected void detach() {
        super.detach();

        TextNode textNode = (TextNode)getNode();
        textNode.getTextNodeListeners().remove(this);
    }

    @Override
    public void invalidateUpTree() {
        length = 0;
        next = null;
        glyphVector = null;

        super.invalidateUpTree();
    }

    @Override
    protected void childLayout(int breakWidth) {
        TextNode textNode = (TextNode)getNode();
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();

        CharSequenceCharacterIterator ci = new CharSequenceCharacterIterator(textNode.getCharacters(), start);

        float lineWidth = 0;
        int lastWhitespaceIndex = -1;

        Font effectiveFont = getEffectiveFont();
        char c = ci.first();
        while (c != CharacterIterator.DONE
            && lineWidth < breakWidth) {
            if (Character.isWhitespace(c)) {
                lastWhitespaceIndex = ci.getIndex();
            }

            int i = ci.getIndex();
            Rectangle2D characterBounds = effectiveFont.getStringBounds(ci, i, i + 1, fontRenderContext);
            lineWidth += characterBounds.getWidth();

            c = ci.current();
        }

        int end;
        if (getTextPaneSkin().getWrapText()) {
            if (textNode.getCharacterCount() == 0) {
                end = start;
            } else {
                if (lineWidth < breakWidth) {
                    end = ci.getEndIndex();
                } else {
                    if (lastWhitespaceIndex == -1) {
                        end = ci.getIndex() - 1;
                        if (end <= start) {
                            end = start + 1;
                        }
                    } else {
                        end = lastWhitespaceIndex + 1;
                    }
                }
            }
        } else {
            end = ci.getEndIndex();
        }

        glyphVector = getEffectiveFont().createGlyphVector(fontRenderContext,
            new CharSequenceCharacterIterator(textNode.getCharacters(), start, end));

        if (end < ci.getEndIndex()) {
            length = end - start;
            next = new TextPaneSkinTextNodeView(textNode, end);
            next.setParent(getParent());
        } else {
            length = ci.getEndIndex() - start;
            // set to null in case this node used to be broken across multiple, but is no longer
            next = null;
        }

        Rectangle2D textBounds = glyphVector.getLogicalBounds();
        setSize((int)Math.ceil(textBounds.getWidth()),
            (int)Math.ceil(textBounds.getHeight()));
    }

    @Override
    public Dimensions getPreferredSize(int breakWidth) {
        TextNode textNode = (TextNode)getNode();
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();

        CharSequenceCharacterIterator ci = new CharSequenceCharacterIterator(textNode.getCharacters(), start);

        float lineWidth = 0;
        int lastWhitespaceIndex = -1;

        Font effectiveFont = getEffectiveFont();
        char c = ci.first();
        while (c != CharacterIterator.DONE
            && lineWidth < breakWidth) {
            if (Character.isWhitespace(c)) {
                lastWhitespaceIndex = ci.getIndex();
            }

            int i = ci.getIndex();
            Rectangle2D characterBounds = effectiveFont.getStringBounds(ci, i, i + 1, fontRenderContext);
            lineWidth += characterBounds.getWidth();

            c = ci.current();
        }

        int end;
        if (getTextPaneSkin().getWrapText()) {
            if (textNode.getCharacterCount() == 0) {
                end = start;
            } else {
                if (lineWidth < breakWidth) {
                    end = ci.getEndIndex();
                } else {
                    if (lastWhitespaceIndex == -1) {
                        end = ci.getIndex() - 1;
                        if (end <= start) {
                            end = start + 1;
                        }
                    } else {
                        end = lastWhitespaceIndex + 1;
                    }
                }
            }
        } else {
            end = ci.getEndIndex();
        }

        GlyphVector glyphVectorLocal = getEffectiveFont().createGlyphVector(fontRenderContext,
            new CharSequenceCharacterIterator(textNode.getCharacters(), start, end));

        Rectangle2D textBounds = glyphVectorLocal.getLogicalBounds();
        return new Dimensions((int)Math.ceil(textBounds.getWidth()),
            (int)Math.ceil(textBounds.getHeight()));
    }

    @Override
    public int getBaseline() {
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();
        LineMetrics lm = getEffectiveFont().getLineMetrics("", fontRenderContext);
        float ascent = lm.getAscent();
        return (int) ascent;
    }

    @Override
    protected void setSkinLocation(int skinX, int skinY) {
        // empty block
    }

    @Override
    public void paint(Graphics2D graphics) {
        if (glyphVector != null) {
            TextPane textPane = (TextPane)getTextPaneSkin().getComponent();

            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            LineMetrics lm = getEffectiveFont().getLineMetrics("", fontRenderContext);
            float ascent = lm.getAscent();
            int strikethroughX = Math.round(lm.getAscent() + lm.getStrikethroughOffset());
            int underlineX = Math.round(lm.getAscent() + lm.getUnderlineOffset());
            boolean underline = getEffectiveUnderline();
            boolean strikethrough = getEffectiveStrikethrough();

            graphics.setFont(getEffectiveFont());

            int selectionStart = textPane.getSelectionStart();
            int selectionLength = textPane.getSelectionLength();
            Span selectionRange = new Span(selectionStart, selectionStart + selectionLength - 1);

            int documentOffset = getDocumentOffset();
            Span characterRange = new Span(documentOffset, documentOffset + getCharacterCount() - 1);

            int width = getWidth();
            int height = getHeight();

            if (selectionLength > 0
                && characterRange.intersects(selectionRange)) {
                // Determine the selection bounds
                int x0;
                if (selectionRange.start > characterRange.start) {
                    Bounds leadingSelectionBounds = getCharacterBounds(selectionRange.start - documentOffset);
                    x0 = leadingSelectionBounds.x;
                } else {
                    x0 = 0;
                }

                int x1;
                if (selectionRange.end < characterRange.end) {
                    Bounds trailingSelectionBounds = getCharacterBounds(selectionRange.end - documentOffset);
                    x1 = trailingSelectionBounds.x + trailingSelectionBounds.width;
                } else {
                    x1 = width;
                }

                Rectangle selection = new Rectangle(x0, 0, x1 - x0, height);

                // Paint the unselected text
                Area unselectedArea = new Area();
                unselectedArea.add(new Area(new Rectangle(0, 0, width, height)));
                unselectedArea.subtract(new Area(selection));

                Graphics2D textGraphics = (Graphics2D)graphics.create();
                textGraphics.setColor(getEffectiveForegroundColor());
                textGraphics.clip(unselectedArea);
                textGraphics.drawGlyphVector(glyphVector, 0, ascent);
                if (underline) {
                    textGraphics.drawLine(x0, underlineX, x1 - x0, underlineX);
                }
                if (strikethrough) {
                    textGraphics.drawLine(x0, strikethroughX, x1 - x0, strikethroughX);
                }
                textGraphics.dispose();

                // Paint the selection
                Color selectionColor;
                if (textPane.isFocused()) {
                    selectionColor = getTextPaneSkin().getSelectionColor();
                } else {
                    selectionColor = getTextPaneSkin().getInactiveSelectionColor();
                }

                Graphics2D selectedTextGraphics = (Graphics2D)graphics.create();
                selectedTextGraphics.setColor(textPane.isFocused() &&
                    textPane.isEditable() ? selectionColor : getTextPaneSkin().getInactiveSelectionColor());
                selectedTextGraphics.clip(selection.getBounds());
                selectedTextGraphics.drawGlyphVector(glyphVector, 0, ascent);
                if (underline) {
                    selectedTextGraphics.drawLine(0, underlineX, width, underlineX);
                }
                if (strikethrough) {
                    selectedTextGraphics.drawLine(0, strikethroughX, width, strikethroughX);
                }
                selectedTextGraphics.dispose();
            } else {
                // Draw the text
                graphics.setColor(getEffectiveForegroundColor());
                graphics.drawGlyphVector(glyphVector, 0, ascent);
                if (underline) {
                    graphics.drawLine(0, underlineX, width, underlineX);
                }
                if (strikethrough) {
                    graphics.drawLine(0, strikethroughX, width, strikethroughX);
                }
            }
        }
    }

    @Override
    public int getOffset() {
        return super.getOffset() + start;
    }

    @Override
    public int getCharacterCount() {
        return length;
    }

    /**
     * Used by TextPaneSkinParagraphView when it breaks child nodes into multiple views.
     */
    public TextPaneSkinNodeView getNext() {
        return next;
    }

    @Override
    public int getInsertionPoint(int x, int y) {
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();
        LineMetrics lm = getEffectiveFont().getLineMetrics("", fontRenderContext);
        float ascent = lm.getAscent();

        int n = glyphVector.getNumGlyphs();
        int i = 0;

        while (i < n) {
            Shape glyphBounds = glyphVector.getGlyphLogicalBounds(i);

            if (glyphBounds.contains(x, y - ascent)) {
                Rectangle2D glyphBounds2D = glyphBounds.getBounds2D();

                if (x - glyphBounds2D.getX() > glyphBounds2D.getWidth() / 2
                    && i < n - 1) {
                    // The user clicked on the right half of the character; select
                    // the next character
                    i++;
                }

                break;
            }

            i++;
        }

        return i;
    }

    private Font getEffectiveFont() {
        Font font = null;
        // run up the tree until we find an element's style to apply
        Element element = getNode().getParent();
        while (element != null) {
            font = element.getFont();
            if (font != null) {
                break;
            }

            element = element.getParent();
        }
        // if we find nothing, use the default font
        if (element == null) {
            font = getTextPaneSkin().getFont();
        }
        return font;
    }

    private Color getEffectiveForegroundColor() {
        Color foregroundColor = null;
        // run up the tree until we find an element's style to apply
        Element element = getNode().getParent();
        while (element != null) {
            foregroundColor = element.getForegroundColor();
            if (foregroundColor != null) {
                break;
            }

            element = element.getParent();
        }
        // if we find nothing, use the default color
        if (element == null) {
            foregroundColor = getTextPaneSkin().getColor();
        }
        return foregroundColor;
    }

    private boolean getEffectiveUnderline() {
        // run up the tree until we find an element's style to apply
        Element element = getNode().getParent();
        while (element != null) {
            if (element.isUnderline()) {
                return true;
            }

            element = element.getParent();
        }
        return false;
    }

    private boolean getEffectiveStrikethrough() {
        // run up the tree until we find an element's style to apply
        Element element = getNode().getParent();
        while (element != null) {
            if (element.isStrikethrough()) {
                return true;
            }

            element = element.getParent();
        }
        return false;
    }

    @Override
    public int getNextInsertionPoint(int x, int from, TextPane.ScrollDirection direction) {
        int offset = -1;

        if (from == -1) {
            int n = glyphVector.getNumGlyphs();
            int i = 0;

            while (i < n) {
                Shape glyphBounds = glyphVector.getGlyphLogicalBounds(i);
                Rectangle2D glyphBounds2D = glyphBounds.getBounds2D();

                float glyphX = (float)glyphBounds2D.getX();
                float glyphWidth = (float)glyphBounds2D.getWidth();

                if (x >= glyphX && x < glyphX + glyphWidth) {
                    if (x - glyphX > glyphWidth / 2
                        && i < n - 1) {
                        // The x position falls within the right half of the character;
                        // select the next character
                        i++;
                    }

                    offset = i;
                    break;
                }

                i++;
            }
        }

        return offset;
    }

    @Override
    public int getRowAt(int offset) {
        return -1;
    }

    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public Bounds getCharacterBounds(int offset) {
        Shape glyphBounds = glyphVector.getGlyphLogicalBounds(offset);
        Rectangle2D glyphBounds2D = glyphBounds.getBounds2D();

        return new Bounds((int)Math.floor(glyphBounds2D.getX()), 0,
            (int)Math.ceil(glyphBounds2D.getWidth()), getHeight());
    }

    @Override
    public void charactersInserted(TextNode textNode, int index, int count) {
        invalidateUpTree();
    }

    @Override
    public void charactersRemoved(TextNode textNode, int index, int count) {
        invalidateUpTree();
    }

    @Override
    public String toString() {
        TextNode textNode = (TextNode)getNode();
        String text = textNode.getText();
        return "[" + text.substring(start, start + length) + "]";
    }
}