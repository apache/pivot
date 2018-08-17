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
import java.awt.event.InputMethodEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextHitInfo;
import java.awt.geom.Area;
import java.text.AttributedCharacterIterator;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.text.AttributedStringCharacterIterator;
import org.apache.pivot.text.CharSpan;
import org.apache.pivot.util.CharUtils;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Cursor;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.SelectDirection;
import org.apache.pivot.wtk.TextInputMethodListener;
import org.apache.pivot.wtk.TextPane;
import org.apache.pivot.wtk.TextPaneListener;
import org.apache.pivot.wtk.TextPaneSelectionListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.text.Document;
import org.apache.pivot.wtk.text.Node;
import org.apache.pivot.wtk.text.Paragraph;

/**
 * Text pane skin.
 */
public class TextPaneSkin extends ContainerSkin implements TextPane.Skin, TextPaneListener,
    TextPaneSelectionListener {

    /**
     * A class used for blinking the caret as a recurring callback on the
     * application context queue.
     */
    private class BlinkCaretCallback implements Runnable {
        @Override
        public void run() {
            caretOn = !caretOn;

            if (selection == null) {
                getTextPane().repaint(caret.x, caret.y, caret.width, caret.height);
            }
        }
    }

    /**
     * Callback to implement scrolling during mouse selection.
     */
    private class ScrollSelectionCallback implements Runnable {
        @Override
        public void run() {
            TextPane textPane = getTextPane();
            int selectionStart = textPane.getSelectionStart();
            int selectionLength = textPane.getSelectionLength();
            int selectionEnd = selectionStart + selectionLength - 1;

            int offset;
            switch (scrollDirection) {
                case UP:
                    // Get previous offset
                    offset = getNextInsertionPoint(mouseX, selectionStart, scrollDirection);

                    if (offset != -1) {
                        textPane.setSelection(offset, selectionEnd - offset + 1);
                        scrollCharacterToVisible(offset + 1);
                    }
                    break;

                case DOWN:
                    // Get next offset
                    offset = getNextInsertionPoint(mouseX, selectionEnd, scrollDirection);

                    if (offset != -1) {
                        // If the next character is a paragraph terminator and is not the
                        // final terminator character, increment the selection
                        Document document = textPane.getDocument();
                        if (document.getCharacterAt(offset) == '\n'
                            && offset < documentView.getCharacterCount() - 1) {
                            offset++;
                        }

                        textPane.setSelection(selectionStart, offset - selectionStart);
                        scrollCharacterToVisible(offset - 1);
                    }
                    break;

                default:
                    throw new RuntimeException();
            }
        }
    }

    private TextPane getTextPane() {
        return (TextPane) getComponent();
    }

    private Rectangle getCaretRectangle(final TextHitInfo textCaret) {
        TextPane textPane = getTextPane();
        AttributedStringCharacterIterator composedText = textPane.getComposedText();

        // Special case that tweaks the bounds at the end of line so that the entire
        // composed text width isn't added in here.... (yeah, I know it's ugly...)
        int selectionStart = textPane.getSelectionStart();
        this.doingCaretCalculations = true;
        Bounds selectionStartBounds = getCharacterBounds(selectionStart);
        this.doingCaretCalculations = false;

        // Sometimes (maybe timing-related) we get back null, so just retry the calculation
        // without the "doingCaretCalculations" flag to get back something non-null
        if (selectionStartBounds == null) {
            selectionStartBounds = getCharacterBounds(selectionStart);
org.apache.pivot.util.Console.getDefault().logMethod("****",
    "null selection bounds: selectionStart=%1$d, updated bounds=%2$s", selectionStart, selectionStartBounds);
        }

        return GraphicsUtilities.getCaretRectangle(textCaret, composedText,
            selectionStartBounds.x, selectionStartBounds.y);
    }

    /**
     * Private class that handles interaction with the Input Method Editor,
     * including requests and events.
     */
    private class TextInputMethodHandler implements TextInputMethodListener {

        @Override
        public AttributedCharacterIterator getCommittedText(final int beginIndex, final int endIndex,
            final AttributedCharacterIterator.Attribute[] attributes) {
            return new AttributedStringCharacterIterator(getTextPane().getText(), beginIndex, endIndex, attributes);
        }

        @Override
        public int getCommittedTextLength() {
            return getTextPane().getCharacterCount();
        }

        @Override
        public int getInsertPositionOffset() {
            return getTextPane().getSelectionStart();
        }

        @Override
        public TextHitInfo getLocationOffset(final int x, final int y) {
            return null;
        }

        @Override
        public AttributedCharacterIterator getSelectedText(final AttributedCharacterIterator.Attribute[] attributes) {
            String selectedText = getTextPane().getSelectedText();
            if (selectedText != null && !selectedText.isEmpty()) {
                return new AttributedStringCharacterIterator(selectedText, attributes);
            }
            return null;
        }

        private Rectangle offsetToScreen(final Rectangle clientRectangle) {
            return getTextPane().offsetToScreen(clientRectangle);
        }

        @Override
        public Rectangle getTextLocation(final TextHitInfo offset) {
            AttributedStringCharacterIterator composedText = getTextPane().getComposedText();

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
            TextPane textPane = getTextPane();
            AttributedCharacterIterator iter = event.getText();
            AttributedStringCharacterIterator composedIter = null;

            if (iter != null) {
                int endOfCommittedText = event.getCommittedCharacterCount();
                if (endOfCommittedText > 0) {
                    String committedText = getCommittedText(iter, endOfCommittedText);
                    textPane.insertText(committedText, textPane.getSelectionStart());
                }
                composedIter = getComposedText(iter, endOfCommittedText);
            }

            textPane.setComposedText(composedIter);
            if (composedIter != null) {
                composedTextCaret = event.getCaret();
                composedVisiblePosition = event.getVisiblePosition();
            } else {
                composedTextCaret = null;
                composedVisiblePosition = null;
            }

            invalidateNodeViewTree();
            layout();
            repaintComponent();

            selectionChanged(textPane, textPane.getSelectionStart(), textPane.getSelectionLength());
            showCaret(textPane.isFocused() && textPane.getSelectionLength() == 0);
        }

        @Override
        public void caretPositionChanged(final InputMethodEvent event) {
            // TODO:  so far I have not seen this called, so ???
        }

    }

    private TextPaneSkinDocumentView documentView = null;

    private int caretX = 0;
    private Rectangle caret = new Rectangle();
    private Area selection = null;

    private TextHitInfo composedTextCaret = null;
    private TextHitInfo composedVisiblePosition = null;

    private boolean caretOn = false;

    protected boolean doingCaretCalculations = false;

    private int anchor = -1;
    private SelectDirection selectDirection = null;
    private TextPane.ScrollDirection scrollDirection = null;
    private int mouseX = -1;

    private BlinkCaretCallback blinkCaretCallback = new BlinkCaretCallback();
    private ApplicationContext.ScheduledCallback scheduledBlinkCaretCallback = null;

    private ScrollSelectionCallback scrollSelectionCallback = new ScrollSelectionCallback();
    private ApplicationContext.ScheduledCallback scheduledScrollSelectionCallback = null;

    private TextInputMethodHandler textInputMethodHandler = new TextInputMethodHandler();

    private Font font;
    private Color color;
    private Color inactiveColor;
    private Color selectionColor;
    private Color selectionBackgroundColor;
    private Color inactiveSelectionColor;
    private Color inactiveSelectionBackgroundColor;

    private Insets margin = new Insets(4);

    private boolean wrapText = true;
    private int tabWidth = 4;
    private boolean acceptsTab = false;

    private static final int SCROLL_RATE = 30;

    public TextPaneSkin() {
        Theme theme = Theme.getTheme();
        font = theme.getFont();

        color = defaultForegroundColor();
        selectionBackgroundColor = defaultForegroundColor();
        inactiveSelectionBackgroundColor = defaultForegroundColor();
        if (!themeIsDark()) {
            selectionColor = Color.LIGHT_GRAY;
            inactiveSelectionColor = Color.LIGHT_GRAY;
        } else {
            selectionColor = Color.DARK_GRAY;
            inactiveSelectionColor = Color.DARK_GRAY;
        }

        inactiveColor = Color.GRAY;
    }

    @Override
    public void install(final Component component) {
        super.install(component);

        TextPane textPane = (TextPane) component;
        textPane.getTextPaneListeners().add(this);
        textPane.getTextPaneSelectionListeners().add(this);

        textPane.setCursor(Cursor.TEXT);

        Document document = textPane.getDocument();
        if (document != null) {
            documentView = (TextPaneSkinDocumentView) TextPaneSkinNodeView.createNodeView(this, document);
            documentView.attach();
            updateSelection();
        }
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    @Override
    public int getPreferredWidth(final int height) {
        int preferredWidth;

        if (documentView == null) {
            preferredWidth = 0;
        } else {
            Dimensions documentDimensions = documentView.getPreferredSize(Integer.MAX_VALUE);

            preferredWidth = documentDimensions.width + margin.getWidth();
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(final int width) {
        int preferredHeight;

        if (documentView == null || width == -1) {
            preferredHeight = 0;
        } else {
            int breakWidth;
            if (wrapText) {
                breakWidth = Math.max(width - margin.getWidth(), 0);
            } else {
                breakWidth = Integer.MAX_VALUE;
            }

            Dimensions documentDimensions = documentView.getPreferredSize(breakWidth);

            preferredHeight = documentDimensions.height + margin.getHeight();
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredHeight;
        int preferredWidth;

        if (documentView == null) {
            preferredWidth = 0;
            preferredHeight = 0;
        } else {
            Dimensions documentDimensions = documentView.getPreferredSize(Integer.MAX_VALUE);

            preferredWidth = documentDimensions.width + margin.getWidth();
            preferredHeight = documentDimensions.height + margin.getHeight();
        }

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(final int width, final int height) {
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();
        LineMetrics lm = font.getLineMetrics("", fontRenderContext);
        float ascent = lm.getAscent();
        return margin.top + Math.round(ascent);
    }

    @Override
    public void layout() {
        if (documentView != null) {
            TextPane textPane = getTextPane();
            int width = getWidth();

            int breakWidth;
            if (wrapText) {
                breakWidth = Math.max(width - margin.getWidth(), 0);
            } else {
                breakWidth = Integer.MAX_VALUE;
            }
            documentView.layout(breakWidth);
            documentView.setSkinLocation(margin.left, margin.top);

            updateSelection();
            caretX = caret.x;

            if (textPane.isFocused()) {
                scrollCharacterToVisible(textPane.getSelectionStart());
            }

            showCaret(textPane.isFocused() && textPane.getSelectionLength() == 0);
        }
    }

    @Override
    public void paint(final Graphics2D graphics) {
        super.paint(graphics);

        TextPane textPane = getTextPane();

        if (documentView != null) {
            // Draw the selection highlight
            if (selection != null) {
                graphics.setColor(textPane.isFocused() && textPane.isEditable()
                    ? selectionBackgroundColor : inactiveSelectionBackgroundColor);
                graphics.fill(selection);
            }

            int width = getWidth();
            int breakWidth;
            if (wrapText) {
                breakWidth = Math.max(width - margin.getWidth(), 0);
            } else {
                breakWidth = Integer.MAX_VALUE;
            }
            documentView.layout(breakWidth);

            // Draw the document content
            graphics.translate(margin.left, margin.top);
            documentView.paint(graphics);
            graphics.translate(-margin.left, -margin.top);

            // Draw the caret
            if (selection == null && caretOn && textPane.isFocused()) {
                graphics.setColor(textPane.isEditable() ? color : inactiveColor);
                graphics.fill(caret);
            }
        }
    }

    @Override
    public int getInsertionPoint(final int x, final int y) {
        int offset;

        if (documentView == null) {
            offset = -1;
        } else {
            int xUpdated = Math.min(documentView.getWidth() - 1, Math.max(x - margin.left, 0));

            if (y < margin.top) {
                offset = documentView.getNextInsertionPoint(xUpdated, -1,
                    TextPane.ScrollDirection.DOWN);
            } else if (y > documentView.getHeight() + margin.top) {
                offset = documentView.getNextInsertionPoint(xUpdated, -1,
                    TextPane.ScrollDirection.UP);
            } else {
                offset = documentView.getInsertionPoint(xUpdated, y - margin.top);
            }
        }

        return offset;
    }

    @Override
    public int getNextInsertionPoint(final int x, final int from, final TextPane.ScrollDirection direction) {
        int offset;

        if (documentView == null) {
            offset = -1;
        } else {
            offset = documentView.getNextInsertionPoint(x - margin.left, from, direction);
        }

        return offset;
    }

    @Override
    public int getRowAt(final int offset) {
        int rowIndex;

        if (documentView == null) {
            rowIndex = -1;
        } else {
            rowIndex = documentView.getRowAt(offset);
        }

        return rowIndex;
    }

    @Override
    public int getRowCount() {
        int rowCount;

        if (documentView == null) {
            rowCount = 0;
        } else {
            rowCount = documentView.getRowCount();
        }

        return rowCount;
    }

    @Override
    public Bounds getCharacterBounds(final int offset) {
        Bounds characterBounds;

        if (documentView == null) {
            characterBounds = null;
        } else {
            characterBounds = documentView.getCharacterBounds(offset);

            if (characterBounds != null) {
                characterBounds = characterBounds.translate(margin.left, margin.top);
            }
        }

        return characterBounds;
    }

    /**
     * Gets current value of style that determines the behavior of <tt>TAB</tt>
     * and <tt>Ctrl-TAB</tt> characters.
     *
     * @return <tt>true</tt> if <tt>TAB</tt> inserts an appropriate number of
     * spaces, while <tt>Ctrl-TAB</tt> shifts focus to next component.
     * <tt>false</tt> (default) means <tt>TAB</tt> shifts focus and
     * <tt>Ctrl-TAB</tt> inserts spaces.
     */
    public boolean getAcceptsTab() {
        return acceptsTab;
    }

    /**
     * Sets current value of style that determines the behavior of <tt>TAB</tt>
     * and <tt>Ctrl-TAB</tt> characters.
     *
     * @param acceptsTab <tt>true</tt> if <tt>TAB</tt> inserts an appropriate
     * number of spaces, while <tt>Ctrl-TAB</tt> shifts focus to next component.
     * <tt>false</tt> (default) means <tt>TAB</tt> shifts focus and
     * <tt>Ctrl-TAB</tt> inserts spaces.
     */
    public void setAcceptsTab(final boolean acceptsTab) {
        this.acceptsTab = acceptsTab;
    }

    @Override
    public int getTabWidth() {
        return tabWidth;
    }

    public void setTabWidth(final int tabWidth) {
        Utils.checkNonNegative(tabWidth, "tabWidth");

        this.tabWidth = tabWidth;
    }

    private void scrollCharacterToVisible(final int offset) {
        Bounds characterBounds = getCharacterBounds(offset);

        if (characterBounds != null) {
            getTextPane().scrollAreaToVisible(characterBounds.x, characterBounds.y,
                characterBounds.width, characterBounds.height);
        }
    }

    /**
     * @return The font of the text.
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets the font of the text.
     *
     * @param font The new font for all the text.
     */
    public void setFont(final Font font) {
        Utils.checkNull(font, "font");

        this.font = font;
        invalidateComponent();
    }

    /**
     * Sets the font of the text.
     *
     * @param font A {@link ComponentSkin#decodeFont(String) font specification}
     */
    public final void setFont(final String font) {
        setFont(decodeFont(font));
    }

    /**
     * Sets the font of the text.
     *
     * @param font A dictionary {@link Theme#deriveFont describing a font}
     */
    public final void setFont(final Dictionary<String, ?> font) {
        setFont(Theme.deriveFont(font));
    }

    /**
     * @return The foreground color of the text.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the foreground color of the text.
     *
     * @param color The new text color.
     */
    public void setColor(final Color color) {
        Utils.checkNull(color, "color");

        this.color = color;
        repaintComponent();
    }

    /**
     * Sets the foreground color of the text.
     *
     * @param color Any of the {@linkplain GraphicsUtilities#decodeColor color
     * values recognized by Pivot}.
     */
    public final void setColor(final String color) {
        setColor(GraphicsUtilities.decodeColor(color, "color"));
    }

    public Color getInactiveColor() {
        return inactiveColor;
    }

    public void setInactiveColor(final Color inactiveColor) {
        Utils.checkNull(inactiveColor, "inactiveColor");

        this.inactiveColor = inactiveColor;
        repaintComponent();
    }

    public final void setInactiveColor(final String inactiveColor) {
        setColor(GraphicsUtilities.decodeColor(inactiveColor, "inactiveColor"));
    }

    public Color getSelectionColor() {
        return selectionColor;
    }

    public void setSelectionColor(final Color selectionColor) {
        Utils.checkNull(selectionColor, "selectionColor");

        this.selectionColor = selectionColor;
        repaintComponent();
    }

    public final void setSelectionColor(final String selectionColor) {
        setSelectionColor(GraphicsUtilities.decodeColor(selectionColor, "selectionColor"));
    }

    public Color getSelectionBackgroundColor() {
        return selectionBackgroundColor;
    }

    public void setSelectionBackgroundColor(final Color selectionBackgroundColor) {
        Utils.checkNull(selectionBackgroundColor, "selectionBackgroundColor");

        this.selectionBackgroundColor = selectionBackgroundColor;
        repaintComponent();
    }

    public final void setSelectionBackgroundColor(final String selectionBackgroundColor) {
        setSelectionBackgroundColor(GraphicsUtilities.decodeColor(selectionBackgroundColor,
            "selectionBackgroundColor"));
    }

    public Color getInactiveSelectionColor() {
        return inactiveSelectionColor;
    }

    public void setInactiveSelectionColor(final Color inactiveSelectionColor) {
        Utils.checkNull(inactiveSelectionColor, "inactiveSelectionColor");

        this.inactiveSelectionColor = inactiveSelectionColor;
        repaintComponent();
    }

    public final void setInactiveSelectionColor(final String inactiveSelectionColor) {
        setInactiveSelectionColor(GraphicsUtilities.decodeColor(inactiveSelectionColor,
            "inactiveSelectionColor"));
    }

    public Color getInactiveSelectionBackgroundColor() {
        return inactiveSelectionBackgroundColor;
    }

    public void setInactiveSelectionBackgroundColor(final Color inactiveSelectionBackgroundColor) {
        Utils.checkNull(inactiveSelectionBackgroundColor, "inactiveSelectionBackgroundColor");

        this.inactiveSelectionBackgroundColor = inactiveSelectionBackgroundColor;
        repaintComponent();
    }

    public final void setInactiveSelectionBackgroundColor(final String inactiveSelectionBackgroundColor) {
        setInactiveSelectionBackgroundColor(GraphicsUtilities.decodeColor(inactiveSelectionBackgroundColor,
            "inactiveSelectionBackgroundColor"));
    }

    /**
     * @return The amount of space between the edge of the TextPane and its Document.
     */
    public Insets getMargin() {
        return margin;
    }

    /**
     * Sets the amount of space between the edge of the TextPane and its Document.
     *
     * @param margin The new set of margin values.
     */
    public void setMargin(final Insets margin) {
        Utils.checkNull(margin, "margin");

        this.margin = margin;
        invalidateComponent();
    }

    /**
     * Sets the amount of space between the edge of the TextPane and its Document.
     *
     * @param margin A dictionary with keys in the set {top, left, bottom, right}.
     */
    public final void setMargin(final Dictionary<String, ?> margin) {
        setMargin(new Insets(margin));
    }

    /**
     * Sets the amount of space between the edge of the TextPane and its Document.
     *
     * @param margin A sequence with values in the order [top, left, bottom, right].
     */
    public final void setMargin(final Sequence<?> margin) {
        setMargin(new Insets(margin));
    }

    /**
     * Sets the amount of space between the edge of the TextPane and its Document.
     *
     * @param margin The single margin value for all edges.
     */
    public final void setMargin(final int margin) {
        setMargin(new Insets(margin));
    }

    /**
     * Sets the amount of space between the edge of the TextPane and its Document.
     *
     * @param margin The new single margin value for all the edges.
     */
    public final void setMargin(final Number margin) {
        setMargin(new Insets(margin));
    }

    /**
     * Sets the amount of space between the edge of the TextPane and its Document.
     *
     * @param margin A string containing an integer or a JSON dictionary with
     * keys left, top, bottom, and/or right.
     */
    public final void setMargin(final String margin) {
        setMargin(Insets.decode(margin));
    }

    public boolean getWrapText() {
        return wrapText;
    }

    public void setWrapText(final boolean wrapText) {
        if (this.wrapText != wrapText) {
            this.wrapText = wrapText;

            if (documentView != null) {
                documentView.invalidateUpTree();
            }
        }
    }

    @Override
    public boolean mouseMove(final Component component, final int x, final int y) {
        boolean consumed = super.mouseMove(component, x, y);

        if (Mouse.getCapturer() == component) {
            TextPane textPane = getTextPane();

            Bounds visibleArea = textPane.getVisibleArea();
            visibleArea = new Bounds(visibleArea.x, visibleArea.y, visibleArea.width,
                visibleArea.height);

            if (y >= visibleArea.y && y < visibleArea.y + visibleArea.height) {
                // Stop the scroll selection timer
                if (scheduledScrollSelectionCallback != null) {
                    scheduledScrollSelectionCallback.cancel();
                    scheduledScrollSelectionCallback = null;
                }

                scrollDirection = null;
                int offset = getInsertionPoint(x, y);

                if (offset != -1) {
                    // Select the range
                    if (offset > anchor) {
                        textPane.setSelection(anchor, offset - anchor);
                    } else {
                        textPane.setSelection(offset, anchor - offset);
                    }
                }
            } else {
                if (scheduledScrollSelectionCallback == null) {
                    scrollDirection = (y < visibleArea.y) ? TextPane.ScrollDirection.UP
                        : TextPane.ScrollDirection.DOWN;

                    scheduledScrollSelectionCallback = ApplicationContext.runAndScheduleRecurringCallback(
                        scrollSelectionCallback, SCROLL_RATE);
                }
            }

            mouseX = x;
        } else {
            if (Mouse.isPressed(Mouse.Button.LEFT) && Mouse.getCapturer() == null && anchor != -1) {
                // Capture the mouse so we can select text
                Mouse.capture(component);
            }
        }

        return consumed;
    }

    @Override
    public boolean mouseDown(final Component component, final Mouse.Button button, final int x, final int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        if (button == Mouse.Button.LEFT) {
            TextPane textPane = (TextPane) component;

            anchor = getInsertionPoint(x, y);

            if (anchor != -1) {
                if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                    // Select the range
                    int selectionStart = textPane.getSelectionStart();

                    if (anchor > selectionStart) {
                        textPane.setSelection(selectionStart, anchor - selectionStart);
                    } else {
                        textPane.setSelection(anchor, selectionStart - anchor);
                    }
                } else {
                    // Move the caret to the insertion point
                    textPane.setSelection(anchor, 0);
                    consumed = true;
                }
            }

            caretX = caret.x;

            // Set focus to the text input
            textPane.requestFocus();
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
        scrollDirection = null;
        mouseX = -1;

        return consumed;
    }

    @Override
    public boolean mouseClick(final Component component, final Mouse.Button button, final int x, final int y,
        final int count) {
        boolean consumed = super.mouseClick(component, button, x, y, count);

        TextPane textPane = (TextPane) component;
        Document document = textPane.getDocument();

        if (button == Mouse.Button.LEFT) {
            int index = getInsertionPoint(x, y);
            if (index != -1) {
                int rowStart = getRowOffset(document, index);
                if (count == 2) {
                    CharSpan charSpan = CharUtils.selectWord(getRowCharacters(document, index), index - rowStart);
                    if (charSpan != null) {
                        textPane.setSelection(charSpan.offset(rowStart));
                    }
                } else if (count == 3) {
                    textPane.setSelection(rowStart, getRowLength(document, index));
                }
            }
        }
        return consumed;
    }

    @Override
    public boolean keyTyped(final Component component, final char character) {
        boolean consumed = super.keyTyped(component, character);

        final TextPane textPane = getTextPane();

        if (textPane.isEditable()) {
            Document document = textPane.getDocument();

            if (document != null) {
                // Ignore characters in the control range and the ASCII delete
                // character as well as meta key presses
                if (character > 0x1F && character != 0x7F
                    && !Keyboard.isPressed(Keyboard.Modifier.META)) {
                    textPane.insert(character);
                    showCaret(true);
                }
            }
        }

        return consumed;
    }

    private Node getParagraphAt(final Document document, final int index) {
        if (document != null) {
            Node node = document.getDescendantAt(index);
            while (node != null && !(node instanceof Paragraph)) {
                node = node.getParent();
            }
            return node;
        }
        return null;
    }

    private int getRowOffset(final Document document, final int index) {
        Node node = getParagraphAt(document, index);
        // TODO: doesn't take into account the line wrapping within a paragraph
        if (node != null) {
            return node.getDocumentOffset();
        }
        return 0;
    }

    private int getRowLength(final Document document, final int index) {
        Node node = getParagraphAt(document, index);
        // TODO: doesn't take into account the line wrapping within a paragraph
        // Assuming the node is a Paragraph, the count includes the trailing \n, so discount it
        if (node != null) {
            return node.getCharacterCount() - 1;
        }
        return 0;
    }

    private CharSequence getRowCharacters(final Document document, final int index) {
        Node node = getParagraphAt(document, index);
        // TODO: doesn't take into account the line wrapping within a paragraph
        if (node != null) {
            return node.getCharacters();
        }
        return null;
    }

    @Override
    public boolean keyPressed(final Component component, final int keyCode,
        final Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        final TextPane textPane = getTextPane();
        Document document = textPane.getDocument();

        int selectionStart = textPane.getSelectionStart();
        int selectionLength = textPane.getSelectionLength();

        boolean commandPressed = Keyboard.isPressed(Platform.getCommandModifier());
        boolean wordNavPressed = Keyboard.isPressed(Platform.getWordNavigationModifier());
        boolean shiftPressed = Keyboard.isPressed(Keyboard.Modifier.SHIFT);
        boolean metaPressed = Keyboard.isPressed(Keyboard.Modifier.META);
        boolean isEditable = textPane.isEditable();

        if (document != null) {
            if (keyCode == Keyboard.KeyCode.ENTER && isEditable) {
                textPane.insertParagraph();

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.DELETE && isEditable) {
                textPane.delete(false);

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.BACKSPACE && isEditable) {
                textPane.delete(true);

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.HOME
                   || (keyCode == Keyboard.KeyCode.LEFT && metaPressed)) {
                int start;
                if (commandPressed) {
                    // Move the caret to the beginning of the text
                    start = 0;
                } else {
                    // Move the caret to the beginning of the line
                    start = getRowOffset(document, selectionStart);
                }

                if (shiftPressed) {

                    // TODO: if last direction was left, then extend further left
                    // but if right, then reverse selection from the pivot point
                    selectionLength += selectionStart - start;
                } else {
                    selectionLength = 0;
                }

                if (start >= 0) {
                    textPane.setSelection(start, selectionLength);
                    scrollCharacterToVisible(start);

                    consumed = true;
                }
            } else if (keyCode == Keyboard.KeyCode.END
                   || (keyCode == Keyboard.KeyCode.RIGHT && metaPressed)) {
                int end;
                int index = selectionStart + selectionLength;

                if (commandPressed) {
                    // Move the caret to end of the text
                    end = textPane.getCharacterCount() - 1;
                } else {
                    // Move the caret to the end of the line
                    int rowOffset = getRowOffset(document, index);
                    int rowLength = getRowLength(document, index);
                    end = rowOffset + rowLength;
                }

                if (shiftPressed) {
                    // TODO: if last direction was right, then extend further right
                    // but if left, then reverse selection from the pivot point
                    selectionLength += end - index;
                } else {
                    selectionStart = end;
                    selectionLength = 0;
                }

                if (selectionStart + selectionLength <= textPane.getCharacterCount()) {
                    textPane.setSelection(selectionStart, selectionLength);
                    scrollCharacterToVisible(selectionStart + selectionLength);

                    consumed = true;
                }
            } else if (keyCode == Keyboard.KeyCode.LEFT) {
                if (wordNavPressed) {
                    // Move the caret to the start of the next word to our left
                    if (selectionStart > 0) {
                        int originalStart = selectionStart;
                        // TODO: what if last select direction was to the right?
                        // first, skip over any space immediately to our left
                        while (selectionStart > 0
                            && Character.isWhitespace(document.getCharacterAt(selectionStart - 1))) {
                            selectionStart--;
                        }
                        // then, skip over any word-letters to our left
                        while (selectionStart > 0
                            && !Character.isWhitespace(document.getCharacterAt(selectionStart - 1))) {
                            selectionStart--;
                        }

                        if (shiftPressed) {
                            selectionLength += (originalStart - selectionStart);
                        } else {
                            selectionLength = 0;
                        }
                    }
                } else if (shiftPressed) {
                    // TODO: undo last right select depending... see TextInput skin
                    // Add the previous character to the selection
                    if (selectionStart > 0) {
                        selectionStart--;
                        selectionLength++;
                    }
                } else {
                    // Clear the selection and move the caret back by one character
                    if (selectionLength == 0 && selectionStart > 0) {
                        selectionStart--;
                    }

                    selectionLength = 0;
                }

                textPane.setSelection(selectionStart, selectionLength);
                scrollCharacterToVisible(selectionStart);

                caretX = caret.x;

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.RIGHT) {
                if (shiftPressed) {
                    // TODO: possibly undo the last left select... see TextInput skin
                    // Add the next character to the selection
                    if (selectionStart + selectionLength < document.getCharacterCount()) {
                        selectionLength++;
                    }

                    textPane.setSelection(selectionStart, selectionLength);
                    scrollCharacterToVisible(selectionStart + selectionLength);
                } else if (wordNavPressed) {
                    // Move the caret to the start of the next word to our right
                    if (selectionStart < document.getCharacterCount()) {
                        // first, skip over any word-letters to our right
                        while (selectionStart < document.getCharacterCount() - 1
                            && !Character.isWhitespace(document.getCharacterAt(selectionStart))) {
                            selectionStart++;
                        }
                        // then, skip over any space immediately to our right
                        while (selectionStart < document.getCharacterCount() - 1
                            && Character.isWhitespace(document.getCharacterAt(selectionStart))) {
                            selectionStart++;
                        }

                        textPane.setSelection(selectionStart, 0);
                        scrollCharacterToVisible(selectionStart);

                        caretX = caret.x;
                    }
                } else {
                    // Clear the selection and move the caret forward by one character
                    if (selectionLength > 0) {
                        selectionStart += selectionLength - 1;
                    }

                    if (selectionStart < document.getCharacterCount() - 1) {
                        selectionStart++;
                    }

                    textPane.setSelection(selectionStart, 0);
                    scrollCharacterToVisible(selectionStart);

                    caretX = caret.x;
                }

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.UP) {
                int offset = getNextInsertionPoint(caretX, selectionStart,
                    TextPane.ScrollDirection.UP);

                if (offset == -1) {
                    offset = 0;
                }

                if (shiftPressed) {
                    selectionLength = selectionStart + selectionLength - offset;
                } else {
                    selectionLength = 0;
                }

                textPane.setSelection(offset, selectionLength);
                scrollCharacterToVisible(offset);

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.DOWN) {

                if (shiftPressed) {
                    int from;
                    int x;
                    if (selectionLength == 0) {
                        // Get next insertion point from leading selection character
                        from = selectionStart;
                        x = caretX;
                    } else {
                        // Get next insertion point from right edge of trailing
                        // selection character
                        from = selectionStart + selectionLength - 1;

                        Bounds trailingSelectionBounds = getCharacterBounds(from);
                        x = trailingSelectionBounds.x + trailingSelectionBounds.width;
                    }

                    int offset = getNextInsertionPoint(x, from, TextPane.ScrollDirection.DOWN);

                    if (offset == -1) {
                        offset = documentView.getCharacterCount() - 1;
                    } else {
                        // If the next character is a paragraph terminator and is not the
                        // final terminator character, increment the selection
                        if (document.getCharacterAt(offset) == '\n'
                            && offset < documentView.getCharacterCount() - 1) {
                            offset++;
                        }
                    }

                    textPane.setSelection(selectionStart, offset - selectionStart);
                    scrollCharacterToVisible(offset);
                } else {
                    int from;
                    if (selectionLength == 0) {
                        // Get next insertion point from leading selection character
                        from = selectionStart;
                    } else {
                        // Get next insertion point from trailing selection character
                        from = selectionStart + selectionLength - 1;
                    }

                    int offset = getNextInsertionPoint(caretX, from, TextPane.ScrollDirection.DOWN);

                    if (offset == -1) {
                        offset = documentView.getCharacterCount() - 1;
                    }

                    textPane.setSelection(offset, 0);
                    scrollCharacterToVisible(offset);
                }

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.TAB
                && (acceptsTab != Keyboard.isPressed(Keyboard.Modifier.CTRL))
                && isEditable) {
                if (textPane.getExpandTabs()) {
                    int linePos = selectionStart - getRowOffset(document, selectionStart);
                    StringBuilder tabBuilder = new StringBuilder(tabWidth);
                    for (int i = 0; i < tabWidth - (linePos % tabWidth); i++) {
                        tabBuilder.append(" ");
                    }
                    textPane.insert(tabBuilder.toString());
                } else {
                    textPane.insert("\t");
                }
                showCaret(true);

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.INSERT) {
                if (shiftPressed && isEditable) {
                    textPane.paste();
                    consumed = true;
                }
            } else if (commandPressed) {
                if (keyCode == Keyboard.KeyCode.A) {
                    textPane.setSelection(0, document.getCharacterCount());
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.X && isEditable) {
                    textPane.cut();
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.C) {
                    textPane.copy();
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.V && isEditable) {
                    textPane.paste();
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.Z && isEditable) {
                    if (shiftPressed) {
                        textPane.redo();
                    } else {
                        textPane.undo();
                    }

                    consumed = true;
                }
            } else {
                consumed = super.keyPressed(component, keyCode, keyLocation);
            }
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

        TextPane textPane = getTextPane();
        if (textPane.isFocused() && textPane.getSelectionLength() == 0) {
            scrollCharacterToVisible(textPane.getSelectionStart());
            showCaret(true);
        } else {
            showCaret(false);
        }

        repaintComponent();
    }

    // Text pane events
    @Override
    public void documentChanged(final TextPane textPane, final Document previousDocument) {
        if (documentView != null) {
            documentView.detach();
            documentView = null;
        }

        Document document = textPane.getDocument();
        if (document != null) {
            documentView = (TextPaneSkinDocumentView) TextPaneSkinNodeView.createNodeView(this, document);
            documentView.attach();
        }

        invalidateComponent();
    }

    @Override
    public void editableChanged(final TextPane textPane) {
        // No-op
    }

    // Text pane selection events
    @Override
    public void selectionChanged(final TextPane textPane, final int previousSelectionStart,
        final int previousSelectionLength) {
        // If the document view is valid, repaint the selection state;
        // otherwise, the selection will be updated in layout()
        if (documentView != null && documentView.isValid()) {
            if (selection == null) {
                // Repaint previous caret bounds
                textPane.repaint(caret.x, caret.y, caret.width, caret.height);
            } else {
                // Repaint previous selection bounds
                Rectangle bounds = selection.getBounds();
                textPane.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            updateSelection();

            if (selection == null) {
                showCaret(textPane.isFocused());
            } else {
                showCaret(false);

                // Repaint current selection bounds
                Rectangle bounds = selection.getBounds();
                textPane.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }
    }

    private void updateSelection() {
        if (documentView.getCharacterCount() > 0) {
            TextPane textPane = (TextPane) getComponent();

            // Update the caret
            int selectionStart = textPane.getSelectionStart();

            Bounds leadingSelectionBounds = getCharacterBounds(selectionStart);
            // sanity check - this is where a lot of bugs show up
            if (leadingSelectionBounds == null) {
                throw new IllegalStateException("no bounds for selection " + selectionStart);
            }

            if (composedTextCaret != null) {
                caret = getCaretRectangle(composedTextCaret);
            } else {
                caret = leadingSelectionBounds.toRectangle();
            }
            caret.width = 1;

            // Update the selection
            int selectionLength = textPane.getSelectionLength();

            if (selectionLength > 0) {
                int selectionEnd = selectionStart + selectionLength - 1;
                Bounds trailingSelectionBounds = getCharacterBounds(selectionEnd);
                selection = new Area();

                int firstRowIndex = getRowAt(selectionStart);
                int lastRowIndex = getRowAt(selectionEnd);

                if (firstRowIndex == lastRowIndex) {
                    selection.add(new Area(new Rectangle(leadingSelectionBounds.x,
                        leadingSelectionBounds.y, trailingSelectionBounds.x
                            + trailingSelectionBounds.width - leadingSelectionBounds.x,
                        trailingSelectionBounds.y + trailingSelectionBounds.height
                            - leadingSelectionBounds.y)));
                } else {
                    int width = getWidth();

                    selection.add(new Area(new Rectangle(leadingSelectionBounds.x,
                        leadingSelectionBounds.y, width - margin.right - leadingSelectionBounds.x,
                        leadingSelectionBounds.height)));

                    if (lastRowIndex - firstRowIndex > 0) {
                        selection.add(new Area(new Rectangle(margin.left, leadingSelectionBounds.y
                            + leadingSelectionBounds.height, width - (margin.left + margin.right),
                            trailingSelectionBounds.y
                                - (leadingSelectionBounds.y + leadingSelectionBounds.height))));
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

    private void showCaret(final boolean show) {
        if (scheduledBlinkCaretCallback != null) {
            scheduledBlinkCaretCallback.cancel();
        }

        if (show) {
            caretOn = true;
            scheduledBlinkCaretCallback = ApplicationContext.runAndScheduleRecurringCallback(
                blinkCaretCallback, Platform.getCursorBlinkRate());
        } else {
            scheduledBlinkCaretCallback = null;
        }
    }

    Area getSelection() {
        return selection;
    }

    void invalidateNodeViewTree() {
        this.documentView.invalidateDownTree();
        invalidateComponent();
    }

    @Override
    public TextInputMethodListener getTextInputMethodListener() {
        TextPane textPane = (TextPane) getComponent();
        return textPane.isEditable() ? textInputMethodHandler : null;
    }


}
