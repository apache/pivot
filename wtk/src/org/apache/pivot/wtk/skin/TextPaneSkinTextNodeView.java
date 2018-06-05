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
import java.awt.font.LineBreakMeasurer;
import java.awt.font.LineMetrics;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;

import org.apache.pivot.text.AttributedStringCharacterIterator;
import org.apache.pivot.text.CompositeIterator;
import org.apache.pivot.util.ClassUtils;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TextPane;
import org.apache.pivot.wtk.text.Element;
import org.apache.pivot.wtk.text.Paragraph;
import org.apache.pivot.wtk.text.TextNode;
import org.apache.pivot.wtk.text.TextNodeListener;

/**
 * Text node view.
 */
class TextPaneSkinTextNodeView extends TextPaneSkinNodeView implements TextNodeListener {
    private int start;
    private int length = 0;
    private TextLayout textLayout = null;
    private TextPaneSkinTextNodeView next = null;

    public TextPaneSkinTextNodeView(final TextPaneSkin textPaneSkin, final TextNode textNode) {
        this(textPaneSkin, textNode, 0);
    }

    public TextPaneSkinTextNodeView(final TextPaneSkin textPaneSkin, final TextNode textNode, final int start) {
        super(textPaneSkin, textNode);
        this.start = start;
    }

    @Override
    protected void attach() {
        super.attach();

        TextNode textNode = (TextNode) getNode();
        textNode.getTextNodeListeners().add(this);
    }

    @Override
    protected void detach() {
        super.detach();

        TextNode textNode = (TextNode) getNode();
        textNode.getTextNodeListeners().remove(this);
    }

    @Override
    public void invalidateUpTree() {
        length = 0;
        next = null;
        textLayout = null;

        super.invalidateUpTree();
    }

    /**
     * Do the heavy lifting to figure out the size of the text that is
     * being displayed.  This could be a combination of composed and committed
     * text, so we need a {@link TextLayout} which is derived from the combined
     * attributed text.
     *
     * @param textLayout The text to measure.
     * @return The dimensions of the text in pixel amounts, or 0,0 if
     * there is no text currently.
     */
    private static Dimensions getTextSize(final TextLayout textLayout) {
        if (textLayout != null) {
            int lineHeight = (int) Math.ceil(textLayout.getAscent() + textLayout.getDescent()
                + textLayout.getLeading());
            return new Dimensions((int) Math.ceil(textLayout.getAdvance()), lineHeight);
        }
        // TODO: should this be 0 height?  Maybe use average character height
        return Dimensions.ZERO;
    }

    private AttributedStringCharacterIterator getCharIterator(final TextNode textNode, final int start,
        final int end, final Font font) {
        CharSequence characters;
        int num = end - start;
        if (num == textNode.getCharacterCount()) {
            characters = textNode.getCharacters();
        } else {
            characters = textNode.getCharacters(start, end);
        }
        return new AttributedStringCharacterIterator(characters, font);
    }

    @Override
    protected void childLayout(final int breakWidth) {
        TextNode textNode = (TextNode) getNode();

        textLayout = null;

        Font effectiveFont = getEffectiveFont();
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();

        TextPane textPane = (TextPane) getTextPaneSkin().getComponent();
        AttributedStringCharacterIterator composedText = textPane.getComposedText();

        Element parent = textNode.getParent();
        // The calculations below really need to know if we're at the end of the paragraph
        // when composing text, so make sure we ignore intervening span or other nodes, but
        // also update the offset relative to the paragraph in the process.
        int relStart = start;
        if (parent != null && !(parent instanceof Paragraph)) {
            relStart += parent.getOffset();
            parent = parent.getParent();
        }
        int parentCount = parent == null ? 0 : parent.getCharacterCount();

        int selectionStart = textPane.getSelectionStart();
        int selectionLength = textPane.getSelectionLength();
        int documentOffset = textNode.getDocumentOffset();
        int charCount = textNode.getCharacterCount();
        boolean composedIntersects = false;

        if (composedText != null) {
            int composedTextBegin = composedText.getBeginIndex();
            int composedTextEnd = composedText.getEndIndex();
            int composedTextLength = composedTextEnd - composedTextBegin; /* exclusive - inclusive, so no +1 needed */
            // If this text node is at the end of a paragraph, increase the span by 1 for the newline
            Span ourSpan;
            if (parent instanceof Paragraph && charCount + relStart == parentCount - 1) {
                ourSpan = new Span(documentOffset + start, documentOffset + charCount);
            } else {
                ourSpan = new Span(documentOffset + start, documentOffset + charCount - 1);
            }
            // The "composed span" just encompasses the start position, because this is "phantom" text,
            // so it exists between any two other "real" text positions.
            Span composedSpan = new Span(selectionStart + composedTextBegin);
            composedIntersects = composedSpan.intersects(ourSpan);
        }

        if (charCount == 0 && !composedIntersects) {
            Dimensions charSize = GraphicsUtilities.getAverageCharacterSize(effectiveFont);
            setSize(0, charSize.height);
            length = 0;
            next = null;
        } else {
            AttributedCharacterIterator text = null;
            boolean underlined = getEffectiveUnderline();
            boolean struckthrough = getEffectiveStrikethrough();

            if (composedText != null && composedIntersects) {
                int composedPos = selectionStart - documentOffset;
                if (composedPos == 0) {
                    if (charCount - start == 0) {
                        text = composedText;
                    } else {
                        AttributedStringCharacterIterator fullText =
                            getCharIterator(textNode, start, charCount, effectiveFont);
                        // Note: only apply the underline and strikethrough to our text, not the composed text
                        fullText.addUnderlineAttribute(underlined);
                        fullText.addStrikethroughAttribute(struckthrough);
                        text = new CompositeIterator(composedText, fullText);
                    }
                } else if (composedPos == charCount) {
                    // Composed text is at the end
                    AttributedStringCharacterIterator fullText =
                        getCharIterator(textNode, start, charCount, effectiveFont);
                    // Note: only apply the underline and strikethrough to our text, not the composed text
                    fullText.addUnderlineAttribute(underlined);
                    fullText.addStrikethroughAttribute(struckthrough);
                    text = new CompositeIterator(fullText, composedText);
                } else {
                    // Composed text is somewhere in the middle
                    AttributedStringCharacterIterator leadingText =
                        getCharIterator(textNode, start, composedPos, effectiveFont);
                    leadingText.addUnderlineAttribute(underlined);
                    leadingText.addStrikethroughAttribute(struckthrough);
                    AttributedStringCharacterIterator trailingText =
                        getCharIterator(textNode, composedPos, charCount, effectiveFont);
                    trailingText.addUnderlineAttribute(underlined);
                    trailingText.addStrikethroughAttribute(struckthrough);
                    text = new CompositeIterator(leadingText, composedText, trailingText);
                }
            } else {
                AttributedStringCharacterIterator fullText =
                    getCharIterator(textNode, start, charCount, effectiveFont);
                fullText.addUnderlineAttribute(underlined);
                fullText.addStrikethroughAttribute(struckthrough);
                text = fullText;
            }

            if (getTextPaneSkin().getWrapText()) {
                LineBreakMeasurer measurer = new LineBreakMeasurer(text, fontRenderContext);
                float wrappingWidth = (float) breakWidth;
                textLayout = measurer.nextLayout(wrappingWidth);
                length = textLayout.getCharacterCount();
                Dimensions size = getTextSize(textLayout);
                float advance = textLayout.getAdvance();
                setSize(size);
                if (start + measurer.getPosition() < textNode.getCharacterCount()) {
                    next = new TextPaneSkinTextNodeView(getTextPaneSkin(), textNode, start + measurer.getPosition());
                    next.setParent(getParent());
                } else {
                    next = null;
                }
            } else {
                // Not wrapping the text, then the width is of the whole thing
                textLayout = new TextLayout(text, fontRenderContext);
                length = textLayout.getCharacterCount();
                Dimensions size = getTextSize(textLayout);
                float advance = textLayout.getAdvance();
                setSize(size);
                // set to null in case this node used to be broken across multiple, but is no longer
                next = null;
            }
        }
    }

    @Override
    public Dimensions getPreferredSize(final int breakWidth) {
        TextNode textNode = (TextNode) getNode();

        Font effectiveFont = getEffectiveFont();

        // TODO: figure out if the composedText impinges on this node or not
        // and construct an iterator based on that
        // For now, just get the committed text
        if (textNode.getCharacterCount() == 0 /* && composedText == null || doesn't impinge on this node */) {
            Dimensions charSize = GraphicsUtilities.getAverageCharacterSize(effectiveFont);
            return new Dimensions(0, charSize.height);
        } else {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();

            // TODO: deal with composed text here
            AttributedCharacterIterator text = new AttributedStringCharacterIterator(
                textNode.getCharacters(), start, effectiveFont);

            // Note: we don't add the underline/strikethrough attributes here because
            // they shouldn't affect the sizing...

            TextLayout currentTextLayout;
            if (getTextPaneSkin().getWrapText()) {
                LineBreakMeasurer measurer = new LineBreakMeasurer(text, fontRenderContext);
                float wrappingWidth = (float) breakWidth;
                currentTextLayout = measurer.nextLayout(wrappingWidth);
            } else {
                // Not wrapping the text, then the width is of the whole thing
                currentTextLayout = new TextLayout(text, fontRenderContext);
            }
            return getTextSize(currentTextLayout);
        }

    }

    @Override
    public int getBaseline() {
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();
        LineMetrics lm = getEffectiveFont().getLineMetrics("", fontRenderContext);
        float ascent = lm.getAscent();
        return (int) ascent;
    }

    @Override
    protected void setSkinLocation(final int skinX, final int skinY) {
        // empty block
    }

    @Override
    public void paint(final Graphics2D graphics) {
        if (textLayout != null) {
            TextPane textPane = (TextPane) getTextPaneSkin().getComponent();

            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            Font effectiveFont = getEffectiveFont();
            LineMetrics lm = effectiveFont.getLineMetrics("", fontRenderContext);
            float ascent = lm.getAscent() + lm.getLeading();

            graphics.setFont(effectiveFont);

            int selectionStart = textPane.getSelectionStart();
            int selectionLength = textPane.getSelectionLength();
            Span selectionRange = new Span(selectionStart, selectionStart + selectionLength - 1);

            int documentOffset = getDocumentOffset();
            Span characterRange = new Span(documentOffset, documentOffset + getCharacterCount() - 1);

            int width = getWidth();
            int height = getHeight();

            if (selectionLength > 0 && characterRange.intersects(selectionRange)) {
                // Determine the selection bounds
                int x0;
                if (selectionRange.start > characterRange.start) {
                    Bounds leadingSelectionBounds = getCharacterBounds(selectionRange.start
                        - documentOffset);
                    x0 = leadingSelectionBounds.x;
                } else {
                    x0 = 0;
                }

                int x1;
                if (selectionRange.end < characterRange.end) {
                    Bounds trailingSelectionBounds = getCharacterBounds(selectionRange.end
                        - documentOffset);
                    x1 = trailingSelectionBounds.x + trailingSelectionBounds.width;
                } else {
                    x1 = width;
                }

                int selectionWidth = x1 - x0;
                Rectangle selection = new Rectangle(x0, 0, selectionWidth, height);

                // Paint the unselected text
                Area unselectedArea = new Area();
                unselectedArea.add(new Area(new Rectangle(0, 0, width, height)));
                unselectedArea.subtract(new Area(selection));

                Graphics2D textGraphics = (Graphics2D) graphics.create();
                textGraphics.setColor(getEffectiveForegroundColor());
                textGraphics.clip(unselectedArea);
                textLayout.draw(textGraphics, 0, ascent);
                textGraphics.dispose();

                // Paint the selection
                Color selectionColor;
                if (textPane.isFocused()) {
                    selectionColor = getTextPaneSkin().getSelectionColor();
                } else {
                    selectionColor = getTextPaneSkin().getInactiveSelectionColor();
                }

                Graphics2D selectedTextGraphics = (Graphics2D) graphics.create();
                selectedTextGraphics.setColor(textPane.isFocused() && textPane.isEditable() ? selectionColor
                    : getTextPaneSkin().getInactiveSelectionColor());
                selectedTextGraphics.clip(selection.getBounds());
                textLayout.draw(selectedTextGraphics, 0, ascent);
                selectedTextGraphics.dispose();
            } else {
                // Draw the text
                graphics.setColor(getEffectiveForegroundColor());
                textLayout.draw(graphics, 0, ascent);
            }
        }
    }

    @Override
    public final int getOffset() {
        return super.getOffset() + start;
    }

    @Override
    public final int getCharacterCount() {
        return length;
    }

    /**
     * Used by {@link TextPaneSkinParagraphView} when it breaks child nodes into
     * multiple views.
     *
     * @return The next node view in the document.
     */
    public final TextPaneSkinNodeView getNext() {
        return next;
    }

    private Font getEffectiveFont() {
        // Run up the tree until we find an element's style to apply
        for (Element element = getNode().getParent();
             element != null;
             element = element.getParent()) {
            Font font = element.getFont();
            if (font != null) {
                return font;
            }
        }
        // if we find nothing, use the default font
        return getTextPaneSkin().getFont();
    }

    private Color getEffectiveForegroundColor() {
        // Run up the tree until we find an element's style to apply
        for (Element element = getNode().getParent();
             element != null;
             element = element.getParent()) {
            Color foregroundColor = element.getForegroundColor();
            if (foregroundColor != null) {
                return foregroundColor;
            }
        }
        return getTextPaneSkin().getColor();
    }

    private boolean getEffectiveUnderline() {
        // Run up the tree until we find an element's style to apply
        for (Element element = getNode().getParent();
             element != null;
             element = element.getParent()) {
            if (element.isUnderline()) {
                return true;
            }
        }
        return false;
    }

    private boolean getEffectiveStrikethrough() {
        // Run up the tree until we find an element's style to apply
        for (Element element = getNode().getParent();
             element != null;
             element = element.getParent()) {
            if (element.isStrikethrough()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getInsertionPoint(final int x, final int y) {
        int offset = 0;

        if (textLayout != null) {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            LineMetrics lm = getEffectiveFont().getLineMetrics("", fontRenderContext);
            float ascent = lm.getAscent();

            // Translate to glyph coordinates
            float fx = (float) x;
            float fy = (float) y - ascent;

            TextHitInfo hitInfo = textLayout.hitTestChar(fx, fy);
            offset = hitInfo.getInsertionIndex();
        }

        return offset;
    }

    @Override
    public int getNextInsertionPoint(final int x, final int from, final TextPane.ScrollDirection direction) {
        int offset = -1;

        if (from == -1) {
            // TODO: do we need to check for textLayout != null?  previous code did not test glyphVector here
            Rectangle2D textBounds = textLayout.getBounds();
            // "hitTestChar" will map out-of-bounds points to the beginning or end of the text
            // but we need to know if the given x is inside or not, so test that first.
            if (textBounds.contains(x, 0)) {
                TextHitInfo hitInfo = textLayout.hitTestChar((float) x, 0f);
                offset = hitInfo.getInsertionIndex();
            }
        }

        return offset;
    }

    @Override
    public int getRowAt(final int offset) {
        return -1;
    }

    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public Bounds getCharacterBounds(final int offset) {
        Bounds characterBounds = null;
        if (textLayout != null) {
            // If the offest == length, then use the right hand edge of the previous
            // offset instead -- this is for positioning the caret at the end of the text
            int length = textLayout.getCharacterCount();
            int ix = (offset == length) ? offset - 1 : offset;
            Shape glyphShape = textLayout.getLogicalHighlightShape(ix, ix + 1);
            Rectangle2D glyphBounds2D = glyphShape.getBounds2D();

            if (offset == length) {
                characterBounds = new Bounds((int) Math.ceil(glyphBounds2D.getX() + glyphBounds2D.getWidth()), 0,
                    1, getHeight());
            } else {
                characterBounds = new Bounds((int) Math.floor(glyphBounds2D.getX()), 0,
                    (int) Math.ceil(glyphBounds2D.getWidth()), getHeight());
            }
        }

        return characterBounds;
    }

    @Override
    public void charactersInserted(final TextNode textNode, final int index, final int count) {
        invalidateUpTree();
    }

    @Override
    public void charactersRemoved(final TextNode textNode, final int index, final int count) {
        invalidateUpTree();
    }

    @Override
    public String toString() {
        TextNode textNode = (TextNode) getNode();
        String text = textNode.getText();
        // Sometimes the length may not be correct yet (i.e., during layout) so check before using
        String textSubString = "";
        if (length < 0 || start + length > text.length()) {
            textSubString = text.substring(start);
        } else {
            textSubString = text.substring(start, start + length);
        }
        return ClassUtils.simpleToString(this)
            + " start=" + start + ",length=" + length + " [" + textSubString + "]";
    }

}
