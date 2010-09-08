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
import java.awt.Transparency;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.Locale;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.text.CharSequenceCharacterIterator;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.TextArea2;
import org.apache.pivot.wtk.TextAreaListener2;
import org.apache.pivot.wtk.TextAreaContentListener2;
import org.apache.pivot.wtk.TextAreaSelectionListener2;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Visual;

/**
 * Text area skin.
 */
public class TextAreaSkin2 extends ComponentSkin implements TextArea2.Skin,
    TextAreaListener2, TextAreaContentListener2, TextAreaSelectionListener2 {
    /**
     * Paragraph view.
     */
    public class ParagraphView implements Visual, TextArea2.ParagraphListener {
        private TextArea2.Paragraph paragraph;

        private int x = 0;
        private int y = 0;
        private float width = 0;
        private float height = 0;

        private int breakWidth = Integer.MAX_VALUE;

        private boolean valid = false;
        private ArrayList<GlyphVector> glyphVectors = new ArrayList<GlyphVector>();

        public ParagraphView(TextArea2.Paragraph paragraph) {
            this.paragraph = paragraph;
        }

        public TextArea2.Paragraph getParagraph() {
            return paragraph;
        }

        @Override
        public int getWidth() {
            validate();
            return (int)Math.ceil(width);
        }

        @Override
        public int getHeight() {
            validate();
            return (int)Math.ceil(height);
        }

        public int getBreakWidth() {
            return breakWidth;
        }

        public void setBreakWidth(int breakWidth) {
            int previousBreakWidth = this.breakWidth;
            this.breakWidth = breakWidth;

            if (previousBreakWidth > breakWidth) {
                invalidate();
            }
        }

        @Override
        public int getBaseline() {
            return -1;
        }

        @Override
        public void paint(Graphics2D graphics) {
            // Draw the text
            int width = getWidth();

            // TODO Only paint visible glyphs

            // TODO Paint text using selection color when appropriate

            int n = glyphVectors.getLength();

            if (n > 0) {
                FontRenderContext fontRenderContext = Platform.getFontRenderContext();
                LineMetrics lm = font.getLineMetrics("", fontRenderContext);
                float ascent = lm.getAscent();

                float y = 0;
                for (int i = 0; i < n; i++) {
                    GlyphVector glyphVector = glyphVectors.get(i);

                    Rectangle2D textBounds = glyphVector.getLogicalBounds();
                    float lineWidth = (float)textBounds.getWidth();

                    float x = 0;
                    switch (horizontalAlignment) {
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

                    graphics.drawGlyphVector(glyphVector, x, y + ascent);

                    y += textBounds.getHeight();
                }
            }
        }

        public void invalidate() {
            valid = false;
        }

        public void validate() {
            // TODO Validate from invalid offset rather than 0

            if (!valid) {
                glyphVectors = new ArrayList<GlyphVector>();
                width = 0;
                height = 0;

                // Re-layout glyphs and recalculate size
                FontRenderContext fontRenderContext = Platform.getFontRenderContext();

                CharSequence characters = paragraph.getCharacters();
                int n = characters.length();

                int i = 0;
                int start = 0;
                float lineWidth = 0;
                int lastWhitespaceIndex = -1;

                // NOTE We use a character iterator here only because it is the most
                // efficient way to measure the character bounds (as of Java 6, the version
                // of Font#getStringBounds() that takes a String performs a string copy,
                // whereas the version that takes a character iterator does not)
                CharSequenceCharacterIterator ci = new CharSequenceCharacterIterator(characters);
                while (i < n) {
                    char c = characters.charAt(i);
                    if (Character.isWhitespace(c)) {
                        lastWhitespaceIndex = i;
                    }

                    Rectangle2D characterBounds = font.getStringBounds(ci, i, i + 1, fontRenderContext);
                    lineWidth += characterBounds.getWidth();

                    if (lineWidth > breakWidth) {
                        if (lastWhitespaceIndex == -1) {
                            if (start == i) {
                                appendLine(characters, start, start + 1, fontRenderContext);
                            } else {
                                appendLine(characters, start, i, fontRenderContext);
                                i--;
                            }
                        } else {
                            appendLine(characters, start, lastWhitespaceIndex + 1, fontRenderContext);
                            i = lastWhitespaceIndex;
                        }

                        start = i + 1;

                        lineWidth = 0;
                        lastWhitespaceIndex = -1;
                    }

                    i++;
                }

                appendLine(characters, start, i, fontRenderContext);
            }

            valid = true;
        }

        private void appendLine(CharSequence characters, int start, int end,
            FontRenderContext fontRenderContext) {
            CharSequenceCharacterIterator line =
                new CharSequenceCharacterIterator(characters, start, end, start);
            GlyphVector glyphVector = font.createGlyphVector(fontRenderContext, line);
            glyphVectors.add(glyphVector);

            Rectangle2D textBounds = glyphVector.getLogicalBounds();
            width = Math.max(width, (float)textBounds.getWidth());
            height += textBounds.getHeight();
        }

        public Bounds getCharacterBounds(int index) {
            Bounds characterBounds = null;

            CharSequence characters = paragraph.getCharacters();
            int characterCount = characters.length();

            int lineIndex = glyphVectors.getLength() - 1;
            GlyphVector glyphVector = glyphVectors.get(lineIndex);

            int x, width;
            if (index == characterCount) {
                // This is the terminator character
                Rectangle2D glyphVectorBounds = glyphVector.getLogicalBounds();
                x = (int)Math.floor(glyphVectorBounds.getWidth());
                width = PARAGRAPH_TERMINATOR_WIDTH;

            } else {
                // This is a visible character
                int lineOffset = characterCount - glyphVector.getNumGlyphs();

                while (lineOffset > index) {
                    glyphVector = glyphVectors.get(--lineIndex);
                    lineOffset -= glyphVector.getNumGlyphs();
                }

                Shape glyphBounds = glyphVector.getGlyphLogicalBounds(index - lineOffset);
                Rectangle2D glyphBounds2D = glyphBounds.getBounds2D();

                x = (int)Math.floor(glyphBounds2D.getX());
                width = (int)Math.ceil(glyphBounds2D.getWidth());
            }

            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            LineMetrics lm = font.getLineMetrics("", fontRenderContext);
            float lineHeight = lm.getAscent() + lm.getDescent();

            characterBounds = new Bounds(x, (int)Math.floor(lineIndex * lineHeight),
                width, (int)Math.ceil(lineHeight));

            return characterBounds;
        }

        @Override
        public void textInserted(TextArea2.Paragraph paragraph, int index, int count) {
            invalidate();
            invalidateComponent();
        }

        @Override
        public void textRemoved(TextArea2.Paragraph paragraph, int index, int count) {
            invalidate();
            invalidateComponent();
        }
    }

    private class BlinkCaretCallback implements Runnable {
        @Override
        public void run() {
            caretOn = !caretOn;

            if (selection == null) {
                TextArea2 textArea = (TextArea2)getComponent();
                textArea.repaint(caret.x, caret.y, caret.width, caret.height, true);
            }
        }
    }

    private class ScrollSelectionCallback implements Runnable {
        @Override
        public void run() {
            TextArea2 textArea = (TextArea2)getComponent();
            int selectionStart = textArea.getSelectionStart();
            int selectionLength = textArea.getSelectionLength();
            int selectionEnd = selectionStart + selectionLength - 1;

            switch (scrollDirection) {
                case UP: {
                    // Get previous offset
                    int offset = getNextInsertionPoint(mouseX, selectionStart, scrollDirection);

                    if (offset != -1) {
                        textArea.setSelection(offset, selectionEnd - offset + 1);
                        scrollCharacterToVisible(offset + 1);
                    }

                    break;
                }

                case DOWN: {
                    // Get next offset
                    int offset = getNextInsertionPoint(mouseX, selectionEnd, scrollDirection);

                    if (offset != -1) {
                        // If the next character is a paragraph terminator, increment the selection
                        if (textArea.getCharacterAt(offset) == '\n') {
                            offset++;
                        }

                        textArea.setSelection(selectionStart, offset - selectionStart);
                        scrollCharacterToVisible(offset - 1);
                    }

                    break;
                }
            }
        }
    }

    private int caretX = 0;
    private Rectangle caret = new Rectangle();
    private Area selection = null;

    private boolean caretOn = false;

    private int anchor = -1;
    private TextArea2.ScrollDirection scrollDirection = null;
    private int mouseX = -1;

    private BlinkCaretCallback blinkCaretCallback = new BlinkCaretCallback();
    private ApplicationContext.ScheduledCallback scheduledBlinkCaretCallback = null;

    private ScrollSelectionCallback scrollSelectionCallback = new ScrollSelectionCallback();
    private ApplicationContext.ScheduledCallback scheduledScrollSelectionCallback = null;

    private Font font;
    private Color color;
    private Color backgroundColor;
    private Color inactiveColor;
    private Color selectionColor;
    private Color selectionBackgroundColor;
    private Color inactiveSelectionColor;
    private Color inactiveSelectionBackgroundColor;
    private HorizontalAlignment horizontalAlignment;
    private Insets margin;
    private boolean wrapText;

    private ArrayList<ParagraphView> paragraphViews = new ArrayList<ParagraphView>();

    private static final int PARAGRAPH_TERMINATOR_WIDTH = 2;

    public TextAreaSkin2() {
        Theme theme = Theme.getTheme();
        font = theme.getFont();
        color = Color.BLACK;
        backgroundColor = Color.WHITE;
        inactiveColor = Color.GRAY;
        selectionColor = Color.LIGHT_GRAY;
        selectionBackgroundColor = Color.BLACK;
        inactiveSelectionColor = Color.LIGHT_GRAY;
        inactiveSelectionBackgroundColor = Color.BLACK;
        horizontalAlignment = HorizontalAlignment.LEFT;
        margin = new Insets(4);
        wrapText = true;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        TextArea2 textArea = (TextArea2)component;
        textArea.getTextAreaListeners().add(this);
        textArea.getTextAreaContentListeners().add(this);
        textArea.getTextAreaSelectionListeners().add(this);
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        for (ParagraphView paragraphView : paragraphViews) {
            paragraphView.setBreakWidth(Integer.MAX_VALUE);
            preferredWidth = Math.max(preferredWidth, paragraphView.getWidth());
        }

        preferredWidth += margin.left + margin.right;

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        // Include margin in constraint
        int breakWidth = (wrapText
            && width != -1) ? Math.max(width - (margin.left + margin.right), 0) : Integer.MAX_VALUE;

        for (ParagraphView paragraphView : paragraphViews) {
            paragraphView.setBreakWidth(breakWidth);
            preferredHeight += paragraphView.getHeight();
        }

        preferredHeight += margin.top + margin.bottom;

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        for (ParagraphView paragraphView : paragraphViews) {
            paragraphView.setBreakWidth(Integer.MAX_VALUE);
            preferredWidth = Math.max(preferredWidth, paragraphView.getWidth());
            preferredHeight += paragraphView.getHeight();
        }

        preferredWidth += margin.left + margin.right;
        preferredHeight += margin.top + margin.bottom;

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public void layout() {
        TextArea2 textArea = (TextArea2)getComponent();

        int width = getWidth();
        int breakWidth = (wrapText) ? Math.max(width - (margin.left + margin.right), 0) : Integer.MAX_VALUE;

        int y = margin.top;

        for (ParagraphView paragraphView : paragraphViews) {
            paragraphView.setBreakWidth(breakWidth);

            // Set location
            switch (horizontalAlignment) {
                case LEFT: {
                    paragraphView.x = margin.left;
                    break;
                }

                case RIGHT: {
                    paragraphView.x = width - (paragraphView.getWidth() + margin.right);
                    break;
                }

                case CENTER: {
                    paragraphView.x = (width - paragraphView.getWidth()) / 2;
                    break;
                }
            }

            paragraphView.y = y;
            y += paragraphView.getHeight();
        }

        updateSelection();
        caretX = caret.x;

        if (textArea.isFocused()) {
            scrollCharacterToVisible(textArea.getSelectionStart());
        }

        showCaret(textArea.isFocused()
            && textArea.getSelectionLength() == 0);
    }

    @Override
    public int getBaseline(int width, int height) {
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();
        LineMetrics lm = font.getLineMetrics("", fontRenderContext);

        return Math.round(margin.top + lm.getAscent());
    }

    @Override
    public void paint(Graphics2D graphics) {
        TextArea2 textArea = (TextArea2)getComponent();
        int width = getWidth();
        int height = getHeight();

        // Draw the background
        if (backgroundColor != null) {
            graphics.setPaint(backgroundColor);
            graphics.fillRect(0, 0, width, height);
        }

        // Draw the caret/selection
        if (selection == null) {
            if (caretOn
                && textArea.isFocused()) {
                graphics.setColor(textArea.isEditable() ? color : inactiveColor);
                graphics.fill(caret);
            }
        } else {
            graphics.setColor(textArea.isFocused()
                && textArea.isEditable() ?
                selectionBackgroundColor : inactiveSelectionBackgroundColor);
            graphics.fill(selection);
        }

        // Draw the text
        graphics.setFont(font);
        graphics.setPaint(color);

        graphics.translate(0, margin.top);

        for (int i = 0, n = paragraphViews.getLength(); i < n; i++) {
            ParagraphView paragraphView = paragraphViews.get(i);

            graphics.translate(paragraphView.x, 0);
            paragraphView.paint(graphics);
            graphics.translate(-paragraphView.x, 0);

            graphics.translate(0, paragraphView.getHeight());
        }
    }

    @Override
    public boolean isOpaque() {
        return (backgroundColor != null
            && backgroundColor.getTransparency() == Transparency.OPAQUE);
    }

    public int getInsertionPoint(int x, int y) {
        // TODO
        return -1;
    }

    public int getNextInsertionPoint(int x, int from, TextArea2.ScrollDirection direction) {
        // TODO
        return -1;
    }

    public int getRowIndex(int index) {
        // TODO
        return -1;
    }

    public int getRowCount() {
        // TODO
        return 0;
    }

    public Bounds getCharacterBounds(int index) {
        TextArea2 textArea = (TextArea2)getComponent();
        int paragraphIndex = textArea.getParagraphAt(index);
        ParagraphView paragraphView = paragraphViews.get(paragraphIndex);
        Bounds characterBounds = paragraphView.getCharacterBounds(index -
            paragraphView.paragraph.getOffset());

        return new Bounds(characterBounds.x + paragraphView.x,
            characterBounds.y + paragraphView.y,
            characterBounds.width, characterBounds.height);
    }

    private void scrollCharacterToVisible(int offset) {
        TextArea2 textArea = (TextArea2)getComponent();
        Bounds characterBounds = getCharacterBounds(offset);

        if (characterBounds != null) {
            textArea.scrollAreaToVisible(characterBounds.x, characterBounds.y,
                characterBounds.width, characterBounds.height);
        }
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        this.font = font;
        invalidateComponent();
    }

    public final void setFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(decodeFont(font));
    }

    public final void setFont(Dictionary<String, ?> font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Theme.deriveFont(font));
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.color = color;
        repaintComponent();
    }

    public final void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(GraphicsUtilities.decodeColor(color));
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    public final void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null");
        }

        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor));
    }

    public Color getInactiveColor() {
        return inactiveColor;
    }

    public void setInactiveColor(Color inactiveColor) {
        if (inactiveColor == null) {
            throw new IllegalArgumentException("inactiveColor is null.");
        }

        this.inactiveColor = inactiveColor;
        repaintComponent();
    }

    public final void setInactiveColor(String inactiveColor) {
        if (inactiveColor == null) {
            throw new IllegalArgumentException("inactiveColor is null.");
        }

        setColor(GraphicsUtilities.decodeColor(inactiveColor));
    }

    public Color getSelectionColor() {
        return selectionColor;
    }

    public void setSelectionColor(Color selectionColor) {
        if (selectionColor == null) {
            throw new IllegalArgumentException("selectionColor is null.");
        }

        this.selectionColor = selectionColor;
        repaintComponent();
    }

    public final void setSelectionColor(String selectionColor) {
        if (selectionColor == null) {
            throw new IllegalArgumentException("selectionColor is null.");
        }

        setSelectionColor(GraphicsUtilities.decodeColor(selectionColor));
    }

    public Color getSelectionBackgroundColor() {
        return selectionBackgroundColor;
    }

    public void setSelectionBackgroundColor(Color selectionBackgroundColor) {
        if (selectionBackgroundColor == null) {
            throw new IllegalArgumentException("selectionBackgroundColor is null.");
        }

        this.selectionBackgroundColor = selectionBackgroundColor;
        repaintComponent();
    }

    public final void setSelectionBackgroundColor(String selectionBackgroundColor) {
        if (selectionBackgroundColor == null) {
            throw new IllegalArgumentException("selectionBackgroundColor is null.");
        }

        setSelectionBackgroundColor(GraphicsUtilities.decodeColor(selectionBackgroundColor));
    }

    public Color getInactiveSelectionColor() {
        return inactiveSelectionColor;
    }

    public void setInactiveSelectionColor(Color inactiveSelectionColor) {
        if (inactiveSelectionColor == null) {
            throw new IllegalArgumentException("inactiveSelectionColor is null.");
        }

        this.inactiveSelectionColor = inactiveSelectionColor;
        repaintComponent();
    }

    public final void setInactiveSelectionColor(String inactiveSelectionColor) {
        if (inactiveSelectionColor == null) {
            throw new IllegalArgumentException("inactiveSelectionColor is null.");
        }

        setInactiveSelectionColor(GraphicsUtilities.decodeColor(inactiveSelectionColor));
    }

    public Color getInactiveSelectionBackgroundColor() {
        return inactiveSelectionBackgroundColor;
    }

    public void setInactiveSelectionBackgroundColor(Color inactiveSelectionBackgroundColor) {
        if (inactiveSelectionBackgroundColor == null) {
            throw new IllegalArgumentException("inactiveSelectionBackgroundColor is null.");
        }

        this.inactiveSelectionBackgroundColor = inactiveSelectionBackgroundColor;
        repaintComponent();
    }

    public final void setInactiveSelectionBackgroundColor(String inactiveSelectionBackgroundColor) {
        if (inactiveSelectionBackgroundColor == null) {
            throw new IllegalArgumentException("inactiveSelectionBackgroundColor is null.");
        }

        setInactiveSelectionBackgroundColor(GraphicsUtilities.decodeColor(inactiveSelectionBackgroundColor));
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        if (horizontalAlignment == null) {
            throw new IllegalArgumentException("horizontalAlignment is null.");
        }

        this.horizontalAlignment = horizontalAlignment;
        repaintComponent();
    }

    public final void setHorizontalAlignment(String horizontalAlignment) {
        if (horizontalAlignment == null) {
            throw new IllegalArgumentException("horizontalAlignment is null.");
        }

        setHorizontalAlignment(HorizontalAlignment.valueOf(horizontalAlignment.toUpperCase(Locale.ENGLISH)));
    }

    public Insets getMargin() {
        return margin;
    }

    public void setMargin(Insets margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        this.margin = margin;
        invalidateComponent();
    }

    public final void setMargin(Dictionary<String, ?> margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        setMargin(new Insets(margin));
    }

    public final void setMargin(int margin) {
        setMargin(new Insets(margin));
    }

    public final void setMargin(Number margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        setMargin(margin.intValue());
    }

    public final void setMargin(String margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        setMargin(Insets.decode(margin));
    }

    public boolean getWrapText() {
        return wrapText;
    }

    public void setWrapText(boolean wrapText) {
        this.wrapText = wrapText;
        invalidateComponent();
    }

    @Override
    public boolean mouseMove(Component component, int x, int y) {
        boolean consumed = super.mouseMove(component, x, y);

        // TODO

        return consumed;
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        // TODO

        return consumed;
    }

    @Override
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseUp(component, button, x, y);

        // TODO

        return consumed;
    }

    @Override
    public boolean keyTyped(Component component, char character) {
        boolean consumed = super.keyTyped(component, character);

        // TODO

        return consumed;
    }

    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(component, keyCode, keyLocation);

        // TODO

        return consumed;
    }

    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);
        repaintComponent();
    }

    @Override
    public void focusedChanged(Component component, Component obverseComponent) {
        super.focusedChanged(component, obverseComponent);

        TextArea2 textArea = (TextArea2)getComponent();
        if (textArea.isFocused()
            && textArea.getSelectionLength() == 0) {
            if (textArea.isValid()) {
                scrollCharacterToVisible(textArea.getSelectionStart());
            }

            showCaret(true);
        } else {
            showCaret(false);
        }

        repaintComponent();
    }

    @Override
    public void maximumLengthChanged(TextArea2 textArea, int previousMaximumLength) {
        // No-op
    }

    @Override
    public void editableChanged(TextArea2 textArea) {
        // No-op
    }

    @Override
    public void paragraphInserted(TextArea2 textArea, int index) {
        // Create paragraph view and add as paragraph listener
        TextArea2.Paragraph paragraph = textArea.getParagraphs().get(index);
        ParagraphView paragraphView = new ParagraphView(paragraph);
        paragraph.getParagraphListeners().add(paragraphView);

        // Insert view
        paragraphViews.insert(paragraphView, index);
    }

    @Override
    public void paragraphsRemoved(TextArea2 textArea, int index, Sequence<TextArea2.Paragraph> removed) {
        // Remove paragraph views as paragraph listeners
        int count = removed.getLength();

        for (int i = 0; i < count; i++) {
            TextArea2.Paragraph paragraph = removed.get(i);
            ParagraphView paragraphView = paragraphViews.get(i + index);
            paragraph.getParagraphListeners().remove(paragraphView);
        }

        // Remove views
        paragraphViews.remove(index, count);
    }

    @Override
    public void textChanged(TextArea2 textArea) {
        // No-op
    }

    @Override
    public void selectionChanged(TextArea2 textArea, int previousSelectionStart,
        int previousSelectionLength) {
        // If the text area is valid, repaint the selection state; otherwise,
        // the selection will be updated in layout()
        if (textArea.isValid()) {
            if (selection == null) {
                // Repaint previous caret bounds
                textArea.repaint(caret.x, caret.y, caret.width, caret.height);
            } else {
                // Repaint previous selection bounds
                Rectangle bounds = selection.getBounds();
                textArea.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            updateSelection();

            if (selection == null) {
                showCaret(textArea.isFocused());
            } else {
                showCaret(false);

                // Repaint current selection bounds
                Rectangle bounds = selection.getBounds();
                textArea.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }
    }

    private void updateSelection() {
        TextArea2 textArea = (TextArea2)getComponent();

        if (textArea.getCharacterCount() > 0) {
            // Update the caret
            int selectionStart = textArea.getSelectionStart();

            Bounds leadingSelectionBounds = getCharacterBounds(selectionStart);
            caret = leadingSelectionBounds.toRectangle();
            caret.width = 1;

            // Update the selection
            int selectionLength = textArea.getSelectionLength();

            if (selectionLength > 0) {
                int selectionEnd = selectionStart + selectionLength - 1;
                Bounds trailingSelectionBounds = getCharacterBounds(selectionEnd);
                selection = new Area();

                int firstRowIndex = getRowIndex(selectionStart);
                int lastRowIndex = getRowIndex(selectionEnd);

                if (firstRowIndex == lastRowIndex) {
                    selection.add(new Area(new Rectangle(leadingSelectionBounds.x, leadingSelectionBounds.y,
                        trailingSelectionBounds.x + trailingSelectionBounds.width - leadingSelectionBounds.x,
                        trailingSelectionBounds.y + trailingSelectionBounds.height - leadingSelectionBounds.y)));
                } else {
                    int width = getWidth();

                    selection.add(new Area(new Rectangle(leadingSelectionBounds.x,
                        leadingSelectionBounds.y,
                        width - margin.right - leadingSelectionBounds.x,
                        leadingSelectionBounds.height)));

                    if (lastRowIndex - firstRowIndex > 0) {
                        selection.add(new Area(new Rectangle(margin.left,
                            leadingSelectionBounds.y + leadingSelectionBounds.height,
                            width - (margin.left + margin.right),
                            trailingSelectionBounds.y - (leadingSelectionBounds.y
                                + leadingSelectionBounds.height))));
                    }

                    selection.add(new Area(new Rectangle(margin.left, trailingSelectionBounds.y,
                        trailingSelectionBounds.x + trailingSelectionBounds.width - margin.left,
                        trailingSelectionBounds.height)));
                }
            } else {
                selection = null;
            }
        } else {
            // Clear the caret and the selection
            caret = new Rectangle();
            selection = null;
        }
    }

    private void showCaret(boolean show) {
        if (scheduledBlinkCaretCallback != null) {
            scheduledBlinkCaretCallback.cancel();
        }

        if (show) {
            caretOn = true;
            scheduledBlinkCaretCallback =
                ApplicationContext.scheduleRecurringCallback(blinkCaretCallback,
                    Platform.getCursorBlinkRate());

            // Run the callback once now to show the cursor immediately
            blinkCaretCallback.run();
        } else {
            scheduledBlinkCaretCallback = null;
        }
    }
}
