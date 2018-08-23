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
package org.apache.pivot.wtk.skin.terra;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.InputMethodEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.font.LineMetrics;
import java.awt.font.TextHitInfo;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.text.AttributedStringCharacterIterator;
import org.apache.pivot.text.CharSpan;
import org.apache.pivot.text.CompositeIterator;
import org.apache.pivot.util.CharUtils;
import org.apache.pivot.util.StringUtils;
import org.apache.pivot.util.Utils;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Cursor;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.KeyLocation;
import org.apache.pivot.wtk.Keyboard.Modifier;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.SelectDirection;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.apache.pivot.wtk.TextInputListener;
import org.apache.pivot.wtk.TextInputMethodListener;
import org.apache.pivot.wtk.TextInputSelectionListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.skin.ComponentSkin;
import org.apache.pivot.wtk.validation.Validator;

/**
 * Text input skin.
 */
public class TerraTextInputSkin extends ComponentSkin implements TextInput.Skin, TextInputListener,
    TextInputContentListener, TextInputSelectionListener {

    private Rectangle getCaretRectangle(final TextHitInfo textCaret) {
        TextInput textInput = (TextInput) getComponent();
        AttributedStringCharacterIterator composedText = textInput.getComposedText();
        Bounds selectionStartBounds = getCharacterBounds(textInput.getSelectionStart());
        return GraphicsUtilities.getCaretRectangle(textCaret, composedText, selectionStartBounds.x, padding.top + 1);
    }

    /**
     * Private class that handles interaction with the Input Method Editor,
     * including requests and events.
     */
    private class TextInputMethodHandler implements TextInputMethodListener {

        @Override
        public AttributedCharacterIterator getCommittedText(final int beginIndex, final int endIndex,
            final AttributedCharacterIterator.Attribute[] attributes) {
            TextInput textInput = (TextInput) getComponent();
            return new AttributedStringCharacterIterator(textInput.getText(), beginIndex, endIndex, attributes);
        }

        @Override
        public int getCommittedTextLength() {
            TextInput textInput = (TextInput) getComponent();
            return textInput.getCharacterCount();
        }

        @Override
        public int getInsertPositionOffset() {
            TextInput textInput = (TextInput) getComponent();
            return textInput.getSelectionStart();
        }

        @Override
        public TextHitInfo getLocationOffset(final int x, final int y) {
            return null;
        }

        @Override
        public AttributedCharacterIterator getSelectedText(final AttributedCharacterIterator.Attribute[] attributes) {
            TextInput textInput = (TextInput) getComponent();
            return new AttributedStringCharacterIterator(textInput.getSelectedText(), attributes);
        }

        private Rectangle offsetToScreen(final Rectangle clientRectangle) {
            TextInput textInput = (TextInput) getComponent();
            return textInput.offsetToScreen(clientRectangle);
        }

        @Override
        public Rectangle getTextLocation(final TextHitInfo offset) {
            TextInput textInput = (TextInput) getComponent();
            AttributedStringCharacterIterator composedText = textInput.getComposedText();

            if (composedText == null) {
                return offsetToScreen(caret);
            } else {
                // The offset should be into the composed text, not the whole text
                Rectangle caretRect = getCaretRectangle(composedTextCaret != null ? composedTextCaret : offset);
                return offsetToScreen(caretRect);
            }
        }

        private String getCommittedText(final AttributedCharacterIterator fullTextIter, final int count) {
            StringBuilder buf = new StringBuilder(count);
            buf.setLength(count);
            if (fullTextIter != null) {
                char ch = fullTextIter.first();
                for (int i = 0; i < count; i++) {
                    buf.setCharAt(i, ch);
                    ch = fullTextIter.next();
                }
            }
            return buf.toString();
        }

        private AttributedStringCharacterIterator getComposedText(final AttributedCharacterIterator fullTextIter,
            final int start) {
            if (fullTextIter != null) {
                if (start < fullTextIter.getEndIndex()) {
                    return new AttributedStringCharacterIterator(fullTextIter, start, fullTextIter.getEndIndex());
                }
            }
            return null;
        }

        @Override
        public void inputMethodTextChanged(final InputMethodEvent event) {
            TextInput textInput = (TextInput) getComponent();
            AttributedCharacterIterator iter = event.getText();
            AttributedStringCharacterIterator composedIter = null;

            if (iter != null) {
                int endOfCommittedText = event.getCommittedCharacterCount();
                if (deleteSelectionDuringTyping(textInput, endOfCommittedText)) {
                    textInput.insertText(getCommittedText(iter, endOfCommittedText), textInput.getSelectionStart());
                    composedIter = getComposedText(iter, endOfCommittedText);
                }
            }

            textInput.setComposedText(composedIter);
            if (composedIter != null) {
                composedTextCaret = event.getCaret();
                composedVisiblePosition = event.getVisiblePosition();
            } else {
                composedTextCaret = null;
                composedVisiblePosition = null;
            }

            layout();
            repaintComponent();

            selectionChanged(textInput, textInput.getSelectionStart(), textInput.getSelectionLength());
            showCaret(textInput.isFocused() && textInput.getSelectionLength() == 0);
        }

        @Override
        public void caretPositionChanged(final InputMethodEvent event) {
            TextInput textInput = (TextInput) getComponent();
            // TODO:  so far I have not seen this called, so ???
        }

    }

    private TextLayout textLayout = null;

    private int anchor = -1;
    private SelectDirection selectDirection = null;

    private Rectangle caret = new Rectangle();
    private Rectangle selection = null;

    private TextHitInfo composedTextCaret = null;
    private TextHitInfo composedVisiblePosition = null;

    private int scrollLeft = 0;

    private boolean caretOn = true;

    private FocusTraversalDirection scrollDirection = null;

    private Runnable blinkCaretCallback = () -> {
        caretOn = !caretOn;

        if (caret != null) {
            TextInput textInput = (TextInput) getComponent();
            textInput.repaint(caret.x, caret.y, caret.width, caret.height);
        }
    };

    private Runnable scrollSelectionCallback = () -> {
        TextInput textInput = (TextInput) getComponent();
        int selectionStart = textInput.getSelectionStart();
        int selectionLength = textInput.getSelectionLength();
        int selectionEnd = selectionStart + selectionLength - 1;

        switch (scrollDirection) {
            case FORWARD:
                if (selectionEnd < textInput.getCharacterCount() - 1) {
                    selectionEnd++;
                    textInput.setSelection(selectionStart, selectionEnd - selectionStart + 1);
                    scrollCharacterToVisible(selectionEnd);
                }
                break;

            case BACKWARD:
                if (selectionStart > 0) {
                    selectionStart--;
                    textInput.setSelection(selectionStart, selectionEnd - selectionStart + 1);
                    scrollCharacterToVisible(selectionStart);
                }
                break;

            default:
                throw new RuntimeException();
        }
    };

    private ApplicationContext.ScheduledCallback scheduledBlinkCaretCallback = null;
    private ApplicationContext.ScheduledCallback scheduledScrollSelectionCallback = null;

    private Font font;
    private Color color;
    private Color disabledColor;
    private Color promptColor;
    private Color backgroundColor;
    private Color disabledBackgroundColor;
    private Color invalidColor;
    private Color invalidBackgroundColor;
    private Color borderColor;
    private Color disabledBorderColor;
    private Color selectionColor;
    private Color selectionBackgroundColor;
    private Color inactiveSelectionColor;
    private Color inactiveSelectionBackgroundColor;

    private Color bevelColor;
    private Color disabledBevelColor;
    private Color invalidBevelColor;

    private Insets padding;

    private HorizontalAlignment horizontalAlignment;

    private Dimensions averageCharacterSize;

    private TextInputMethodHandler textInputMethodHandler = new TextInputMethodHandler();


    private static final int SCROLL_RATE = 50;
    private static final char BULLET = 0x2022;

    public TerraTextInputSkin() {
        Theme theme = currentTheme();
        setFont(theme.getFont());

        color = theme.getColor(1);
        promptColor = theme.getColor(7);
        disabledColor = theme.getColor(7);
        backgroundColor = theme.getColor(11);
        disabledBackgroundColor = theme.getColor(10);
        invalidColor = theme.getColor(4);
        invalidBackgroundColor = theme.getColor(22);
        borderColor = theme.getColor(7);
        disabledBorderColor = theme.getColor(7);
        padding = new Insets(2);
        horizontalAlignment = HorizontalAlignment.LEFT;

        selectionColor = theme.getColor(4);
        selectionBackgroundColor = theme.getColor(14);
        inactiveSelectionColor = theme.getColor(1);
        inactiveSelectionBackgroundColor = theme.getColor(9);

        bevelColor = TerraTheme.darken(backgroundColor);
        disabledBevelColor = disabledBackgroundColor;
        invalidBevelColor = TerraTheme.darken(invalidBackgroundColor);
    }

    @Override
    public void install(final Component component) {
        super.install(component);

        TextInput textInput = (TextInput) component;
        textInput.getTextInputListeners().add(this);
        textInput.getTextInputContentListeners().add(this);
        textInput.getTextInputSelectionListeners().add(this);

        textInput.setCursor(Cursor.TEXT);

        updateSelection();
    }

    @Override
    public int getPreferredWidth(final int height) {
        TextInput textInput = (TextInput) getComponent();
        int textSize = textInput.getTextSize();

        return averageCharacterSize.width * textSize + padding.getWidth() + 2;
    }

    @Override
    public int getPreferredHeight(final int width) {
        return averageCharacterSize.height + padding.getHeight() + 2;
    }

    @Override
    public Dimensions getPreferredSize() {
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    @Override
    public int getBaseline(final int width, final int height) {
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();
        LineMetrics lm = font.getLineMetrics("", fontRenderContext);
        float ascent = lm.getAscent();
        float textHeight = lm.getHeight();

        int baseline = Math.round((height - textHeight) / 2 + ascent);

        return baseline;
    }

    private AttributedCharacterIterator getCharIterator(final TextInput textInput, final int start, final int end) {
        CharSequence characters;
        int num = end - start;
        if (textInput.isPassword()) {
            characters = StringUtils.fromNChars(BULLET, num);
        } else {
            if (num == textInput.getCharacterCount()) {
                characters = textInput.getCharacters();
            } else {
                characters = textInput.getCharacters(start, end);
            }
        }
        return new AttributedStringCharacterIterator(characters, font);
    }

    /**
     * The text width is the just the "advance" value of the {@link TextLayout}
     * (if any) or 0 if {@code null}.
     *
     * @param textLayout The existing text (if any).
     * @return The "advance" value or 0 if there is no text.
     */
    private int getTextWidth(final TextLayout textLayout) {
        if (textLayout != null) {
            return (int) Math.ceil(textLayout.getAdvance());
        }
        return 0;
    }

    @Override
    public void layout() {
        TextInput textInput = (TextInput) getComponent();

        textLayout = null;

        int n = textInput.getCharacterCount();
        AttributedStringCharacterIterator composedText = textInput.getComposedText();

        if (n > 0 || composedText != null) {
            AttributedCharacterIterator text = null;

            if (n > 0) {
                int insertPos = textInput.getSelectionStart();
                if (composedText == null) {
                    text = getCharIterator(textInput, 0, n);
                } else if (insertPos == n) {
                    // The composed text position is the end of the committed text
                    text = new CompositeIterator(getCharIterator(textInput, 0, n), composedText);
                } else if (insertPos == 0) {
                    text = new CompositeIterator(composedText, getCharIterator(textInput, 0, n));
                } else {
                    // The composed text is somewhere in the middle of the text
                    text = new CompositeIterator(
                            getCharIterator(textInput, 0, insertPos),
                            composedText,
                            getCharIterator(textInput, insertPos, n));
                }
            } else {
                text = composedText;
            }

            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            textLayout = new TextLayout(text, fontRenderContext);
            int textWidth = getTextWidth(textLayout);
            int width = getWidth();

            if (textWidth - scrollLeft + padding.left + 1 < width - padding.right - 1) {
                // The right edge of the text is less than the right inset;
                // align the text's right edge with the inset
                scrollLeft = Math.max(textWidth + padding.getWidth() + 2 - width, 0);
            } else {
                // Scroll selection start to visible
                int selectionStart = textInput.getSelectionStart();
                if (textInput.isFocused()) {
                    if (composedVisiblePosition != null) {
                        scrollCharacterToVisible(selectionStart + composedVisiblePosition.getInsertionIndex());
                    } else {
                        if (selectionStart <= n) {
                            scrollCharacterToVisible(selectionStart);
                        }
                    }
                }
            }
        } else {
            scrollLeft = 0;
        }

        updateSelection();
        showCaret(textInput.isFocused() && textInput.getSelectionLength() == 0);
    }

    private int getAlignmentDeltaX(final TextLayout textLayout) {
        int alignmentDeltaX = 0;

        TextInput textInput;
        int txtWidth, availWidth;

        switch (horizontalAlignment) {
            case LEFT:
            default:
                break;
            case CENTER:
                textInput = (TextInput) getComponent();
                txtWidth = getTextWidth(textLayout);
                availWidth = textInput.getWidth() - (padding.getWidth() + 2);
                alignmentDeltaX = (availWidth - txtWidth) / 2;
                break;
            case RIGHT:
                textInput = (TextInput) getComponent();
                txtWidth = getTextWidth(textLayout);
                availWidth = textInput.getWidth() - (padding.getWidth() + 2 + caret.width);
                alignmentDeltaX = (availWidth - txtWidth);
                break;
        }

        return alignmentDeltaX;
    }

    @Override
    public void paint(final Graphics2D graphics) {
        TextInput textInput = (TextInput) getComponent();

        int width = getWidth();
        int height = getHeight();

        Color backgroundColorLocal;
        Color borderColorLocal;
        Color bevelColorLocal;

        if (textInput.isEnabled()) {
            if (textInput.isTextValid()) {
                backgroundColorLocal = this.backgroundColor;
                bevelColorLocal = this.bevelColor;
            } else {
                backgroundColorLocal = invalidBackgroundColor;
                bevelColorLocal = invalidBevelColor;
            }

            borderColorLocal = this.borderColor;
        } else {
            backgroundColorLocal = disabledBackgroundColor;
            borderColorLocal = disabledBorderColor;
            bevelColorLocal = disabledBevelColor;
        }

        graphics.setStroke(new BasicStroke());

        // Paint the background
        graphics.setColor(backgroundColorLocal);
        graphics.fillRect(0, 0, width, height);

        if (!themeIsFlat()) {
            // Paint the bevel
            graphics.setColor(bevelColorLocal);
            GraphicsUtilities.drawLine(graphics, 0, 0, width, Orientation.HORIZONTAL);
        }

        // Paint the content
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();
        LineMetrics lm = font.getLineMetrics("", fontRenderContext);
        float ascent = lm.getAscent();
        float textHeight = lm.getHeight();

        String prompt = textInput.getPrompt();

        Color caretColor;

        TextLayout drawingTextLayout = textLayout;
        if (textLayout == null && prompt != null && !prompt.isEmpty()) {
            AttributedStringCharacterIterator promptText = new AttributedStringCharacterIterator(prompt);
            drawingTextLayout = new TextLayout(promptText, fontRenderContext);
        }

        int alignmentDeltaX = getAlignmentDeltaX(drawingTextLayout);
        int xpos = padding.left - scrollLeft + 1 + alignmentDeltaX;

        if (textLayout == null && prompt != null && !prompt.isEmpty()) {
            GraphicsUtilities.prepareForText(graphics, fontRenderContext, font, promptColor);
            graphics.drawString(prompt, xpos, (height - textHeight) / 2 + ascent);

            caretColor = color;
        } else {
            boolean textValid = textInput.isTextValid();

            Color colorLocal;
            if (textInput.isEnabled()) {
                if (!textValid) {
                    colorLocal = invalidColor;
                } else {
                    colorLocal = this.color;
                }
            } else {
                colorLocal = disabledColor;
            }

            caretColor = colorLocal;

            if (textLayout != null) {
                graphics.setFont(font);

                float ypos = (height - textHeight) / 2 + ascent;

                if (selection == null) {
                    // Paint the text
                    graphics.setColor(colorLocal);
                    textLayout.draw(graphics, xpos, ypos);
                } else {
                    // Paint the unselected text
                    Area unselectedArea = new Area();
                    unselectedArea.add(new Area(new Rectangle(0, 0, width, height)));
                    unselectedArea.subtract(new Area(selection));

                    Graphics2D textGraphics = (Graphics2D) graphics.create();
                    textGraphics.setColor(colorLocal);
                    textGraphics.clip(unselectedArea);
                    textLayout.draw(textGraphics, xpos, ypos);
                    textGraphics.dispose();

                    // Paint the selection
                    Color selectionColorLocal;
                    Color selectionBackgroundColorLocal;

                    if (textInput.isFocused() && textInput.isEditable()) {
                        selectionColorLocal = this.selectionColor;
                        selectionBackgroundColorLocal = this.selectionBackgroundColor;
                    } else {
                        selectionColorLocal = inactiveSelectionColor;
                        selectionBackgroundColorLocal = inactiveSelectionBackgroundColor;
                    }

                    graphics.setColor(selectionBackgroundColorLocal);
                    graphics.fill(selection);

                    Graphics2D selectedTextGraphics = (Graphics2D) graphics.create();
                    selectedTextGraphics.setColor(selectionColorLocal);
                    selectedTextGraphics.clip(selection.getBounds());
                    textLayout.draw(selectedTextGraphics, xpos, ypos);
                    selectedTextGraphics.dispose();
                }
            }
        }

        // Paint the caret
        if (selection == null && caretOn && textInput.isFocused()) {
            graphics.setColor(caretColor);
            graphics.fill(caret);
        }

        if (!themeIsFlat()) {
            // Paint the border
            graphics.setColor(borderColorLocal);
            GraphicsUtilities.drawRect(graphics, 0, 0, width, height);
        }
    }

    /**
     * Calculate the text insertion point given the mouse X-position relative
     * to the component's X-position.
     * <p> Note: if the given X-position is on the right-hand side of a glyph
     * then the insertion point will be after that character, while an X-position
     * within the left half of a glyph will position the insert before that
     * character.
     *
     * @param x The relative X-position.
     * @return The offset into the text determined by the X-position.
     */
    @Override
    public int getInsertionPoint(final int x) {
        int offset = -1;

        if (textLayout == null) {
            offset = 0;
        } else {
            // Translate to glyph coordinates
            float xt = x - (padding.left - scrollLeft + 1 + getAlignmentDeltaX(textLayout));

            TextHitInfo hitInfo = textLayout.hitTestChar(xt, 0);
            offset = hitInfo.getInsertionIndex();
        }

        return offset;
    }

    /**
     * Determine the bounding box of the character at the given index
     * in the text in coordinates relative to the entire component (that is,
     * adding in the insets and padding, etc.).
     *
     * @param index The 0-based index of the character to inquire about.
     * @return The bounding box within the component where that character
     * will be displayed, or {@code null} if there is no text.
     */
    @Override
    public Bounds getCharacterBounds(final int index) {
        Bounds characterBounds = null;

        if (textLayout != null) {
            int x, width;
            if (index < textLayout.getCharacterCount()) {
                Shape glyphShape = textLayout.getLogicalHighlightShape(index, index + 1);
                Rectangle2D glyphBounds2D = glyphShape.getBounds2D();

                x = (int) Math.floor(glyphBounds2D.getX());
                width = (int) Math.ceil(glyphBounds2D.getWidth());
            } else {
                // This is the terminator character
                x = getTextWidth(textLayout);
                width = 0;
            }

            characterBounds = new Bounds(x + padding.left - scrollLeft + 1 + getAlignmentDeltaX(textLayout),
                padding.top + 1, width, getHeight() - (padding.getHeight() + 2));
        }

        return characterBounds;
    }

    private void setScrollLeft(final int scrollLeft) {
        this.scrollLeft = scrollLeft;
        updateSelection();
        repaintComponent();
    }

    private void scrollCharacterToVisible(final int offset) {
        int width = getWidth();
        Bounds characterBounds = getCharacterBounds(offset);

        if (characterBounds != null) {
            int glyphX = characterBounds.x - (padding.left + 1) + scrollLeft;

            if (characterBounds.x < padding.left + 1) {
                setScrollLeft(glyphX);
            } else if (characterBounds.x + characterBounds.width > width - (padding.right + 1)) {
                setScrollLeft(glyphX + (padding.getWidth() + 2) + characterBounds.width - width);
            }
        }
    }

    public final Font getFont() {
        return font;
    }

    /**
     * Set the new font to use to render the text in this component.
     * <p> Also calculate the {@link #averageCharacterSize} value based
     * on this font, which will be the width of the "missing glyph code"
     * and the maximum height of any character in the font.
     *
     * @param font The new font to use.
     * @throws IllegalArgumentException if the <tt>font</tt> argument is <tt>null</tt>.
     */
    public final void setFont(final Font font) {
        Utils.checkNull(font, "font");

        this.font = font;

        averageCharacterSize = GraphicsUtilities.getAverageCharacterSize(font);

        invalidateComponent();
    }

    public final void setFont(final String font) {
        setFont(decodeFont(font));
    }

    public final void setFont(final Dictionary<String, ?> font) {
        setFont(Theme.deriveFont(font));
    }

    public final Color getColor() {
        return color;
    }

    public final void setColor(final Color color) {
        Utils.checkNull(color, "color");

        this.color = color;
        repaintComponent();
    }

    public final void setColor(final String color) {
        setColor(GraphicsUtilities.decodeColor(color, "color"));
    }

    public final void setColor(final int color) {
        Theme theme = currentTheme();
        setColor(theme.getColor(color));
    }

    public final Color getPromptColor() {
        return promptColor;
    }

    public final void setPromptColor(final Color promptColor) {
        Utils.checkNull(promptColor, "promptColor");

        this.promptColor = promptColor;
        repaintComponent();
    }

    public final void setPromptColor(final String promptColor) {
        setPromptColor(GraphicsUtilities.decodeColor(promptColor, "promptColor"));
    }

    public final void setPromptColor(final int promptColor) {
        Theme theme = currentTheme();
        setPromptColor(theme.getColor(promptColor));
    }

    public final Color getDisabledColor() {
        return disabledColor;
    }

    public final void setDisabledColor(final Color disabledColor) {
        Utils.checkNull(disabledColor, "disabledColor");

        this.disabledColor = disabledColor;
        repaintComponent();
    }

    public final void setDisabledColor(final String disabledColor) {
        setDisabledColor(GraphicsUtilities.decodeColor(disabledColor, "disabledColor"));
    }

    public final void setDisabledColor(final int disabledColor) {
        Theme theme = currentTheme();
        setDisabledColor(theme.getColor(disabledColor));
    }

    public final Color getBackgroundColor() {
        return backgroundColor;
    }

    public final void setBackgroundColor(final Color backgroundColor) {
        Utils.checkNull(backgroundColor, "backgroundColor");

        this.backgroundColor = backgroundColor;
        bevelColor = TerraTheme.darken(backgroundColor);

        repaintComponent();
    }

    public final void setBackgroundColor(final String backgroundColor) {
        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor, "backgroundColor"));
    }

    public final void setBackgroundColor(final int color) {
        Theme theme = currentTheme();
        setBackgroundColor(theme.getColor(color));
    }

    public final Color getInvalidColor() {
        return invalidColor;
    }

    public final void setInvalidColor(final Color color) {
        Utils.checkNull(color, "invalidColor");

        this.invalidColor = color;
        repaintComponent();
    }

    public final void setInvalidColor(final String color) {
        setInvalidColor(GraphicsUtilities.decodeColor(color, "invalidColor"));
    }

    public final void setInvalidColor(final int color) {
        Theme theme = currentTheme();
        setInvalidColor(theme.getColor(color));
    }

    public final Color getInvalidBackgroundColor() {
        return invalidBackgroundColor;
    }

    public final void setInvalidBackgroundColor(final Color color) {
        Utils.checkNull(color, "invalidBackgroundColor");

        this.invalidBackgroundColor = color;
        invalidBevelColor = TerraTheme.darken(color);

        repaintComponent();
    }

    public final void setInvalidBackgroundColor(final String color) {
        setInvalidBackgroundColor(GraphicsUtilities.decodeColor(color, "invalidBackgroundColor"));
    }

    public final void setInvalidBackgroundColor(final int color) {
        Theme theme = currentTheme();
        setInvalidBackgroundColor(theme.getColor(color));
    }

    public final Color getDisabledBackgroundColor() {
        return disabledBackgroundColor;
    }

    public final void setDisabledBackgroundColor(final Color disabledBackgroundColor) {
        Utils.checkNull(disabledBackgroundColor, "disabledBackgroundColor");

        this.disabledBackgroundColor = disabledBackgroundColor;
        disabledBevelColor = disabledBackgroundColor;

        repaintComponent();
    }

    public final void setDisabledBackgroundColor(final String disabledBackgroundColor) {
        setDisabledBackgroundColor(
            GraphicsUtilities.decodeColor(disabledBackgroundColor, "disabledBackgroundColor"));
    }

    public final void setDisabledBackgroundColor(final int color) {
        Theme theme = currentTheme();
        setDisabledBackgroundColor(theme.getColor(color));
    }

    public final Color getBorderColor() {
        return borderColor;
    }

    public final void setBorderColor(final Color borderColor) {
        Utils.checkNull(borderColor, "borderColor");

        this.borderColor = borderColor;
        repaintComponent();
    }

    public final void setBorderColor(final String borderColor) {
        setBorderColor(GraphicsUtilities.decodeColor(borderColor, "borderColor"));
    }

    public final void setBorderColor(final int color) {
        Theme theme = currentTheme();
        setBorderColor(theme.getColor(color));
    }

    public final Color getDisabledBorderColor() {
        return disabledBorderColor;
    }

    public final void setDisabledBorderColor(final Color disabledBorderColor) {
        Utils.checkNull(disabledBorderColor, "disabledBorderColor");

        this.disabledBorderColor = disabledBorderColor;
        repaintComponent();
    }

    public final void setDisabledBorderColor(final String disabledBorderColor) {
        setDisabledBorderColor(GraphicsUtilities.decodeColor(disabledBorderColor, "disabledBorderColor"));
    }

    public final void setDisabledBorderColor(final int color) {
        Theme theme = currentTheme();
        setDisabledBorderColor(theme.getColor(color));
    }

    public final Color getSelectionColor() {
        return selectionColor;
    }

    public final void setSelectionColor(final Color selectionColor) {
        Utils.checkNull(selectionColor, "selectionColor");

        this.selectionColor = selectionColor;
        repaintComponent();
    }

    public final void setSelectionColor(final String selectionColor) {
        setSelectionColor(GraphicsUtilities.decodeColor(selectionColor, "selectionColor"));
    }

    public final void setSelectionColor(final int color) {
        Theme theme = currentTheme();
        setSelectionColor(theme.getColor(color));
    }

    public final Color getSelectionBackgroundColor() {
        return selectionBackgroundColor;
    }

    public final void setSelectionBackgroundColor(final Color selectionBackgroundColor) {
        Utils.checkNull(selectionBackgroundColor, "selectionBackgroundColor");

        this.selectionBackgroundColor = selectionBackgroundColor;
        repaintComponent();
    }

    public final void setSelectionBackgroundColor(final String selectionBackgroundColor) {
        setSelectionBackgroundColor(GraphicsUtilities.decodeColor(selectionBackgroundColor,
            "selectionBackgroundColor"));
    }

    public final void setSelectionBackgroundColor(final int color) {
        Theme theme = currentTheme();
        setSelectionBackgroundColor(theme.getColor(color));
    }

    public final Color getInactiveSelectionColor() {
        return inactiveSelectionColor;
    }

    public final void setInactiveSelectionColor(final Color inactiveSelectionColor) {
        Utils.checkNull(inactiveSelectionColor, "inactiveSelectionColor");

        this.inactiveSelectionColor = inactiveSelectionColor;
        repaintComponent();
    }

    public final void setInactiveSelectionColor(final String inactiveSelectionColor) {
        setInactiveSelectionColor(GraphicsUtilities.decodeColor(inactiveSelectionColor, "inactiveSelectionColor"));
    }

    public final void setInactiveSelectionColor(final int color) {
        Theme theme = currentTheme();
        setInactiveSelectionColor(theme.getColor(color));
    }

    public final Color getInactiveSelectionBackgroundColor() {
        return inactiveSelectionBackgroundColor;
    }

    public final void setInactiveSelectionBackgroundColor(final Color inactiveSelectionBackgroundColor) {
        Utils.checkNull(inactiveSelectionBackgroundColor, "inactiveSelectionBackgroundColor");

        this.inactiveSelectionBackgroundColor = inactiveSelectionBackgroundColor;
        repaintComponent();
    }

    public final void setInactiveSelectionBackgroundColor(final String inactiveSelectionBackgroundColor) {
        setInactiveSelectionBackgroundColor(
            GraphicsUtilities.decodeColor(inactiveSelectionBackgroundColor, "inactiveSelectionBackgroundColor"));
    }

    public final void setInactiveSelectionBackgroundColor(final int color) {
        Theme theme = currentTheme();
        setInactiveSelectionBackgroundColor(theme.getColor(color));
    }

    public final Insets getPadding() {
        return padding;
    }

    public final void setPadding(final Insets padding) {
        Utils.checkNull(padding, "padding");

        this.padding = padding;
        invalidateComponent();
    }

    public final void setPadding(final Dictionary<String, ?> padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(final Sequence<?> padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(final int padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(final Number padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(final String padding) {
        setPadding(Insets.decode(padding));
    }

    public final HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public final void setHorizontalAlignment(final HorizontalAlignment alignment) {
        Utils.checkNull(alignment, "horizontalAlignment");

        this.horizontalAlignment = alignment;
        invalidateComponent();
    }

    @Override
    public boolean mouseMove(final Component component, final int x, final int y) {
        boolean consumed = super.mouseMove(component, x, y);

        if (Mouse.getCapturer() == component) {
            TextInput textInput = (TextInput) getComponent();
            int width = getWidth();

            if (x >= 0 && x < width) {
                // Stop the scroll selection timer
                if (scheduledScrollSelectionCallback != null) {
                    scheduledScrollSelectionCallback.cancel();
                    scheduledScrollSelectionCallback = null;
                }

                scrollDirection = null;

                int offset = getInsertionPoint(x);

                if (offset != -1) {
                    // Select the range
                    if (offset > anchor) {
                        textInput.setSelection(anchor, offset - anchor);
                        selectDirection = SelectDirection.RIGHT;
                    } else {
                        textInput.setSelection(offset, anchor - offset);
                        selectDirection = SelectDirection.LEFT;
                    }
                }
            } else {
                if (scheduledScrollSelectionCallback == null) {
                    scrollDirection = (x < 0) ? FocusTraversalDirection.BACKWARD
                        : FocusTraversalDirection.FORWARD;
                    selectDirection = (x < 0) ? SelectDirection.LEFT : SelectDirection.RIGHT;

                    // Run the callback once now to scroll the selection immediately
                    scheduledScrollSelectionCallback = ApplicationContext.runAndScheduleRecurringCallback(
                        scrollSelectionCallback, SCROLL_RATE);
                }
            }
        } else {
            if (Mouse.isPressed(Mouse.Button.LEFT) && Mouse.getCapturer() == null && anchor != -1) {
                // Capture the mouse so we can select text
                Mouse.capture(component);
                selectDirection = null;
            }
        }

        return consumed;
    }

    @Override
    public boolean mouseDown(final Component component, final Mouse.Button button, final int x, final int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        if (button == Mouse.Button.LEFT) {
            TextInput textInput = (TextInput) getComponent();

            anchor = getInsertionPoint(x);

            // TODO: this logic won't work in the presence of composed (but not yet committed) text...
            if (anchor != -1) {
                if (Keyboard.isPressed(Modifier.SHIFT)) {
                    // Select the range
                    int selectionStart = textInput.getSelectionStart();

                    if (anchor > selectionStart) {
                        textInput.setSelection(selectionStart, anchor - selectionStart);
                    } else {
                        textInput.setSelection(anchor, selectionStart - anchor);
                    }
                } else {
                    // Move the caret to the insertion point
                    textInput.setSelection(anchor, 0);
                    selectDirection = null;
                    consumed = true;
                }
            }

            // Set focus to the text input
            textInput.requestFocus();
        }

        return consumed;
    }

    @Override
    public boolean mouseUp(final Component component, final Mouse.Button button, final int x, final int y) {
        boolean consumed = super.mouseUp(component, button, x, y);

        if (Mouse.getCapturer() == component) {
            // Stop the scroll selection timer
            if (scheduledScrollSelectionCallback != null) {
                scheduledScrollSelectionCallback.cancel();
                scheduledScrollSelectionCallback = null;
            }

            Mouse.release();
        }

        anchor = -1;

        return consumed;
    }

    @Override
    public boolean mouseClick(final Component component, final Mouse.Button button, final int x, final int y,
        final int count) {
        if (button == Mouse.Button.LEFT && count > 1) {
            TextInput textInput = (TextInput) getComponent();
            if (count == 2) {
                CharSpan charSpan = CharUtils.selectWord(textInput.getCharacters(), getInsertionPoint(x));
                if (charSpan != null) {
                    textInput.setSelection(charSpan);
                }
            } else if (count == 3) {
                textInput.selectAll();
            }
            selectDirection = null;
        }

        return super.mouseClick(component, button, x, y, count);
    }

    private boolean deleteSelectionDuringTyping(final TextInput textInput, final int count) {
        int selectionLength = textInput.getSelectionLength();

        if (textInput.getCharacterCount() - selectionLength + count > textInput.getMaximumLength()) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            textInput.removeText(textInput.getSelectionStart(), selectionLength);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyTyped(final Component component, final char character) {
        boolean consumed = super.keyTyped(component, character);
        TextInput textInput = (TextInput) getComponent();

        if (textInput.isEditable()) {
            // Ignore characters in the control range and the ASCII delete
            // character as well as meta key presses
            if (character > 0x1F && character != 0x7F
                && !Keyboard.isPressed(Modifier.META)) {
                if (deleteSelectionDuringTyping(textInput, 1)) {
                    // NOTE We explicitly call getSelectionStart() a second time
                    // here in case the remove event is vetoed
                    textInput.insertText(Character.toString(character),
                        textInput.getSelectionStart());
                }
            }
        }

        return consumed;
    }

    private boolean handleLeftRightKeys(final TextInput textInput, final int keyCode, final boolean isShiftPressed,
        final int selStart, final int selLength) {
        boolean consumed = false;
        int start = selStart, length = selLength;
        Modifier wordNavigationModifier = Platform.getWordNavigationModifier();

        // Sometimes while selecting we need to make the opposite end visible
        if (keyCode == KeyCode.LEFT) {
            SelectDirection visiblePosition = SelectDirection.LEFT;

            if (Keyboard.isPressed(wordNavigationModifier)) {
                int wordStart = (selectDirection == SelectDirection.RIGHT) ? start + length : start;
                // Find the start of the next word to the left
                if (wordStart > 0) {
                    wordStart = CharUtils.findPriorWord(textInput.getCharacters(), wordStart);

                    if (isShiftPressed) {
                        if (wordStart >= start) {
                            // We've just reduced the previous right selection, so leave the anchor alone
                            length = wordStart - start;
                            wordStart = start;
                            visiblePosition = selectDirection;
                        } else {
                            if (selectDirection == SelectDirection.RIGHT) {
                                // We've "crossed over" the start, so reverse direction
                                length = start - wordStart;
                            } else {
                                // Just increase the selection in the same direction
                                length += start - wordStart;
                            }
                            selectDirection = SelectDirection.LEFT;
                        }
                    } else {
                        length = 0;
                        selectDirection = null;
                    }

                    start = wordStart;
                }
            } else if (isShiftPressed) {
                // If the previous direction was LEFT, then increase the selection
                // else decrease the selection back to the anchor.
                if (selectDirection != null) {
                    switch (selectDirection) {
                        case LEFT:
                            if (start > 0) {
                                start--;
                                length++;
                            }
                            break;
                        case RIGHT:
                            if (length == 0) {
                                if (start > 0) {
                                    start--;
                                    length++;
                                    selectDirection = SelectDirection.LEFT;
                                }
                            } else {
                                if (--length == 0) {
                                    if (start > 0) {
                                        start--;
                                        length++;
                                        selectDirection = SelectDirection.LEFT;
                                    }
                                } else {
                                    visiblePosition = selectDirection;
                                }
                            }
                            break;
                        default:
                            break;
                    }
                } else {
                    // Add one to the selection
                    if (start > 0) {
                        start--;
                        length++;
                        selectDirection = SelectDirection.LEFT;
                    }
                }
            } else {
                selectDirection = null;
                // Move the caret back by one character
                if (length == 0 && start > 0) {
                    start--;
                }

                // Clear the selection
                length = 0;
            }

            if (start >= 0) {
                textInput.setSelection(start, length);
                switch (visiblePosition) {
                    case LEFT:
                        scrollCharacterToVisible(start);
                        break;
                    case RIGHT:
                        scrollCharacterToVisible(start + length);
                        break;
                    default:
                        break;
                }

                consumed = true;
            }
        } else if (keyCode == KeyCode.RIGHT) {
            SelectDirection visiblePosition = SelectDirection.RIGHT;

            if (Keyboard.isPressed(wordNavigationModifier)) {
                int wordStart = (selectDirection == SelectDirection.LEFT) ? start : start + length;
                // Find the start of the next word to the right
                if (wordStart < textInput.getCharacterCount()) {
                    wordStart = CharUtils.findNextWord(textInput.getCharacters(), wordStart);

                    if (isShiftPressed) {
                        if (wordStart <= start + length) {
                            // We've just reduced the previous left selection, so leave the anchor alone
                            length -= wordStart - start;
                            start = wordStart;
                            visiblePosition = selectDirection;
                        } else {
                            if (selectDirection == SelectDirection.LEFT) {
                                // We've "crossed over" the start, so reverse direction
                                start += length;
                                length = wordStart - start;
                            } else {
                                // Just increase the selection in the same direction
                                length = wordStart - start;
                            }
                            selectDirection = SelectDirection.RIGHT;
                        }
                    } else {
                        start = wordStart;
                        length = 0;
                        selectDirection = null;
                    }
                }
            } else if (isShiftPressed) {
                // If the previous direction was RIGHT, then increase the selection
                // else decrease the selection back to the anchor.
                if (selectDirection != null) {
                    switch (selectDirection) {
                        case RIGHT:
                            length++;
                            break;
                        case LEFT:
                            if (length == 0) {
                                length++;
                                selectDirection = SelectDirection.RIGHT;
                            } else {
                                start++;
                                if (--length == 0) {
                                    length++;
                                    selectDirection = SelectDirection.RIGHT;
                                } else {
                                    visiblePosition = selectDirection;
                                }
                            }
                            break;
                        default:
                            break;
                    }
                } else {
                    // Add the next character to the selection
                    length++;
                    selectDirection = SelectDirection.RIGHT;
                }
            } else {
                selectDirection = null;
                // Move the caret forward by one character
                if (length == 0) {
                    start++;
                } else {
                    start += length;
                }

                // Clear the selection
                length = 0;
            }

            if (start + length <= textInput.getCharacterCount()) {
                textInput.setSelection(start, length);
                switch (visiblePosition) {
                    case LEFT:
                        scrollCharacterToVisible(start);
                        break;
                    case RIGHT:
                        scrollCharacterToVisible(start + length);
                        break;
                    default:
                        break;
                }

                consumed = true;
            }
        }

        return consumed;
    }

    /**
     * {@link KeyCode#DELETE DELETE}
     * Delete the character after the caret or the entire selection if there is one.<br>
     * {@link KeyCode#BACKSPACE BACKSPACE}
     * Delete the character before the caret or the entire selection if there is one.
     * <p> {@link KeyCode#HOME HOME}
     * Move the caret to the beginning of the text. <br>
     * {@link KeyCode#LEFT LEFT} + {@link Modifier#META META}
     * Move the caret to the beginning of the text.
     * <p> {@link KeyCode#HOME HOME} + {@link Modifier#SHIFT SHIFT}
     * Select from the caret to the beginning of the text.<br>
     * {@link KeyCode#LEFT LEFT} + {@link Modifier#META META} + {@link Modifier#SHIFT SHIFT}
     * Select from the caret to the beginning of the text.
     * <p> {@link KeyCode#END END}
     * Move the caret to the end of the text.<br>
     * {@link KeyCode#RIGHT RIGHT} + {@link Modifier#META META}
     * Move the caret to the end of the text.
     * <p> {@link KeyCode#END END} + {@link Modifier#SHIFT SHIFT}
     * Select from the caret to the end of the text.<br>
     * {@link KeyCode#RIGHT RIGHT} + {@link Modifier#META META} + {@link Modifier#SHIFT SHIFT}
     * Select from the caret to the end of the text.
     * <p> {@link KeyCode#LEFT LEFT}
     * Clear the selection and move the caret back by one character.<br>
     * {@link KeyCode#LEFT LEFT} + {@link Modifier#SHIFT SHIFT}
     * Add the previous character to the selection.<br>
     * {@link KeyCode#LEFT LEFT} + {@link Modifier#CTRL CTRL}
     * Clear the selection and move the caret to the beginning of the text.<br>
     * {@link KeyCode#LEFT LEFT} + {@link Modifier#CTRL CTRL} + {@link Modifier#SHIFT SHIFT}
     * Add all preceding text to the selection.
     * <p> {@link KeyCode#RIGHT RIGHT}
     * Clear the selection and move the caret forward by one character.<br>
     * {@link KeyCode#RIGHT RIGHT} + {@link Modifier#SHIFT SHIFT}
     * Add the next character to the selection.<br>
     * {@link KeyCode#RIGHT RIGHT} + {@link Modifier#CTRL CTRL}
     * Clear the selection and move the caret to the end of the text.<br>
     * {@link KeyCode#RIGHT RIGHT} + {@link Modifier#CTRL CTRL} + {@link Modifier#SHIFT SHIFT}
     * Add all subsequent text to the selection.
     * <p> CommandModifier + {@link KeyCode#A A}
     * Select all.<br>
     * CommandModifier + {@link KeyCode#X X}
     * Cut selection to clipboard (if not a password TextInput).<br>
     * CommandModifier + {@link KeyCode#C C}
     * Copy selection to clipboard (if not a password TextInput).<br>
     * CommandModifier + {@link KeyCode#V V}
     * Paste from clipboard.<br>
     * CommandModifier + {@link KeyCode#Z Z}
     * Undo.
     *
     * @see Platform#getCommandModifier()
     */
    @Override
    public boolean keyPressed(final Component component, final int keyCode, final KeyLocation keyLocation) {
        boolean consumed = false;

        TextInput textInput = (TextInput) getComponent();
        boolean isEditable = textInput.isEditable();

        int start = textInput.getSelectionStart();
        int length = textInput.getSelectionLength();

        boolean isMetaPressed = Keyboard.isPressed(Modifier.META);
        boolean isShiftPressed = Keyboard.isPressed(Modifier.SHIFT);

        if (keyCode == KeyCode.DELETE && isEditable) {
            if (start < textInput.getCharacterCount()) {
                int count = Math.max(length, 1);
                textInput.removeText(start, count);

                consumed = true;
            }
        } else if (keyCode == KeyCode.BACKSPACE && isEditable) {
            if (length == 0 && start > 0) {
                textInput.removeText(start - 1, 1);
                consumed = true;
            } else {
                textInput.removeText(start, length);
                consumed = true;
            }
        } else if (keyCode == KeyCode.HOME || (keyCode == KeyCode.LEFT && isMetaPressed)) {
            if (isShiftPressed) {
                // Select from the beginning of the text to the current pivot position
                if (selectDirection == SelectDirection.LEFT) {
                    textInput.setSelection(0, start + length);
                } else {
                    textInput.setSelection(0, start);
                }
                selectDirection = SelectDirection.LEFT;
            } else {
                // Move the caret to the beginning of the text
                textInput.setSelection(0, 0);
                selectDirection = null;
            }

            scrollCharacterToVisible(0);

            consumed = true;
        } else if (keyCode == KeyCode.END || (keyCode == KeyCode.RIGHT && isMetaPressed)) {
            int n = textInput.getCharacterCount();

            if (isShiftPressed) {
                // Select from current pivot position to the end of the text
                if (selectDirection == SelectDirection.LEFT) {
                    start += length;
                }
                textInput.setSelection(start, n - start);
                selectDirection = SelectDirection.RIGHT;
            } else {
                // Move the caret to the end of the text
                textInput.setSelection(n, 0);
                selectDirection = null;
            }

            scrollCharacterToVisible(n);

            consumed = true;
        } else if (keyCode == KeyCode.LEFT) {
            consumed = handleLeftRightKeys(textInput, keyCode, isShiftPressed, start, length);
        } else if (keyCode == KeyCode.RIGHT) {
            consumed = handleLeftRightKeys(textInput, keyCode, isShiftPressed, start, length);
        } else if (Keyboard.isPressed(Platform.getCommandModifier())) {
            if (keyCode == KeyCode.A) {
                textInput.setSelection(0, textInput.getCharacterCount());
                consumed = true;
            } else if (keyCode == KeyCode.X && isEditable) {
                if (textInput.isPassword()) {
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    textInput.cut();
                }

                consumed = true;
            } else if (keyCode == KeyCode.C) {
                if (textInput.isPassword()) {
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    textInput.copy();
                }

                consumed = true;
            } else if (keyCode == KeyCode.V && isEditable) {
                textInput.paste();
                consumed = true;
            } else if (keyCode == KeyCode.Z && isEditable) {
                if (!isShiftPressed) {
                    textInput.undo();
                }

                consumed = true;
            }
        } else if (keyCode == KeyCode.INSERT) {
            if (isShiftPressed && isEditable) {
                textInput.paste();
                consumed = true;
            }
        } else {
            consumed = super.keyPressed(component, keyCode, keyLocation);
        }

        return consumed;
    }

    // Component state events
    @Override
    public void enabledChanged(final Component component) {
        super.enabledChanged(component);

        repaintComponent();
    }

    @Override
    public void focusedChanged(final Component component, final Component obverseComponent) {
        super.focusedChanged(component, obverseComponent);

        TextInput textInput = (TextInput) component;
        Window window = textInput.getWindow();

        if (component.isFocused()) {
            // If focus was permanently transferred within this window, select all
            if (obverseComponent == null || obverseComponent.getWindow() == window) {
                if (Mouse.getCapturer() != component) {
                    textInput.selectAll();
                }
            }

            if (textInput.getSelectionLength() == 0) {
                int selectionStart = textInput.getSelectionStart();
                if (selectionStart < textInput.getCharacterCount()) {
                    scrollCharacterToVisible(selectionStart);
                }

                showCaret(true);
            } else {
                showCaret(false);
            }
        } else {
            // If focus was permanently transferred within this window,
            // clear the selection
            if (obverseComponent == null || obverseComponent.getWindow() == window) {
                textInput.clearSelection();
            }

            showCaret(false);
        }

        repaintComponent();
    }

    // Text input events
    @Override
    public void textSizeChanged(final TextInput textInput, final int previousTextSize) {
        invalidateComponent();
    }

    @Override
    public void maximumLengthChanged(final TextInput textInput, final int previousMaximumLength) {
        // No-op
    }

    @Override
    public void passwordChanged(final TextInput textInput) {
        layout();
        repaintComponent();
    }

    @Override
    public void promptChanged(final TextInput textInput, final String previousPrompt) {
        repaintComponent();
    }

    @Override
    public void textValidatorChanged(final TextInput textInput, final Validator previousValidator) {
        repaintComponent();
    }

    @Override
    public void strictValidationChanged(final TextInput textInput) {
        // No-op
    }

    @Override
    public void textValidChanged(final TextInput textInput) {
        repaintComponent();
    }

    // Text input character events
    @Override
    public Vote previewInsertText(final TextInput textInput, final CharSequence text, final int index) {
        Vote vote = Vote.APPROVE;

        if (textInput.isStrictValidation()) {
            Validator validator = textInput.getValidator();
            if (validator != null) {
                StringBuilder textBuilder = new StringBuilder();
                textBuilder.append(textInput.getText(0, index));
                textBuilder.append(text);
                textBuilder.append(textInput.getText(index, textInput.getCharacterCount()));

                if (!validator.isValid(textBuilder.toString())) {
                    vote = Vote.DENY;
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }

        return vote;
    }

    @Override
    public void insertTextVetoed(final TextInput textInput, final Vote reason) {
        // No-op
    }

    @Override
    public void textInserted(final TextInput textInput, final int index, final int count) {
        // No-op
    }

    @Override
    public Vote previewRemoveText(final TextInput textInput, final int index, final int count) {
        Vote vote = Vote.APPROVE;

        if (textInput.isStrictValidation()) {
            Validator validator = textInput.getValidator();
            if (validator != null) {
                StringBuilder textBuilder = new StringBuilder();
                textBuilder.append(textInput.getText(0, index));
                textBuilder.append(textInput.getText(index + count, textInput.getCharacterCount()));

                if (!validator.isValid(textBuilder.toString())) {
                    vote = Vote.DENY;
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }

        return vote;
    }

    @Override
    public void setSize(final int width, final int height) {
        boolean invalidate = (horizontalAlignment != HorizontalAlignment.LEFT)
            && (width != this.getWidth());
        super.setSize(width, height);
        if (invalidate) {
            updateSelection();
            invalidateComponent();
        }
    }

    @Override
    public void removeTextVetoed(final TextInput textInput, final Vote reason) {
        // No-op
    }

    @Override
    public void textRemoved(final TextInput textInput, final int index, final int count) {
        // No-op
    }

    @Override
    public void textChanged(final TextInput textInput) {
        layout();
        repaintComponent();
    }

    @Override
    public void editableChanged(final TextInput textInput) {
        repaintComponent();
    }

    // Text input selection events
    @Override
    public void selectionChanged(final TextInput textInput, final int previousSelectionStart,
        final int previousSelectionLength) {
        // If the text input is valid, repaint the selection state; otherwise,
        // the selection will be updated in layout()
        if (textInput.isValid()) {
            // Repaint any previous caret bounds
            if (caret != null) {
                textInput.repaint(caret.x, caret.y, caret.width, caret.height);
            }

            // Repaint any previous selection bounds
            if (selection != null) {
                Rectangle bounds = selection.getBounds();
                textInput.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            if (textInput.getSelectionLength() == 0) {
                updateSelection();
                showCaret(textInput.isFocused());
            } else {
                updateSelection();
                showCaret(false);

                Rectangle bounds = selection.getBounds();
                textInput.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }
    }

    private void updateSelection() {
        TextInput textInput = (TextInput) getComponent();

        int height = getHeight();

        int selectionStart = textInput.getSelectionStart();
        int selectionLength = textInput.getSelectionLength();

        int n = textInput.getCharacterCount();

        Bounds leadingSelectionBounds;
        if (selectionStart < n) {
            leadingSelectionBounds = getCharacterBounds(selectionStart);
        } else {
            // The insertion point is after the last character
            int x = padding.left + 1 - scrollLeft + getAlignmentDeltaX(textLayout);
            if (n > 0) {
                x += getTextWidth(textLayout);
            }

            int y = padding.top + 1;

            leadingSelectionBounds = new Bounds(x, y,
                0, height - (padding.top + padding.bottom + 2));
        }

        if (composedTextCaret != null) {
            caret = getCaretRectangle(composedTextCaret);
        } else {
            caret = leadingSelectionBounds.toRectangle();
        }
        caret.width = 1;

        if (selectionLength > 0) {
            Bounds trailingSelectionBounds = getCharacterBounds(selectionStart + selectionLength - 1);
            selection = new Rectangle(leadingSelectionBounds.x, leadingSelectionBounds.y,
                trailingSelectionBounds.x + trailingSelectionBounds.width - leadingSelectionBounds.x,
                trailingSelectionBounds.y + trailingSelectionBounds.height - leadingSelectionBounds.y);
        } else {
            selection = null;
        }
    }

    public void showCaret(final boolean show) {
        if (scheduledBlinkCaretCallback != null) {
            scheduledBlinkCaretCallback.cancel();
        }

        if (show) {
            caretOn = true;
            // Run the callback once now to show the cursor immediately
            scheduledBlinkCaretCallback = ApplicationContext.runAndScheduleRecurringCallback(
                blinkCaretCallback, Platform.getCursorBlinkRate());
        } else {
            scheduledBlinkCaretCallback = null;
        }
    }

    @Override
    public TextInputMethodListener getTextInputMethodListener() {
        TextInput textInput = (TextInput) getComponent();
        return textInput.isEditable() ? textInputMethodHandler : null;
    }

}
