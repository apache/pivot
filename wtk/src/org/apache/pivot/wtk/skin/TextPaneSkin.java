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
import java.awt.font.LineMetrics;
import java.awt.geom.Area;

import org.apache.pivot.collections.Dictionary;
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
import org.apache.pivot.wtk.TextPane;
import org.apache.pivot.wtk.TextPaneListener;
import org.apache.pivot.wtk.TextPaneSelectionListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.text.BulletedList;
import org.apache.pivot.wtk.text.ComponentNode;
import org.apache.pivot.wtk.text.Document;
import org.apache.pivot.wtk.text.ImageNode;
import org.apache.pivot.wtk.text.List;
import org.apache.pivot.wtk.text.Node;
import org.apache.pivot.wtk.text.NumberedList;
import org.apache.pivot.wtk.text.Paragraph;
import org.apache.pivot.wtk.text.TextNode;
import org.apache.pivot.wtk.text.TextSpan;

/**
 * Text pane skin.
 */
public class TextPaneSkin extends ContainerSkin implements TextPane.Skin, TextPaneListener,
    TextPaneSelectionListener {
    private class BlinkCaretCallback implements Runnable {
        @Override
        public void run() {
            caretOn = !caretOn;

            if (selection == null) {
                TextPane textPane = (TextPane)getComponent();
                textPane.repaint(caret.x, caret.y, caret.width, caret.height);
            }
        }
    }

    private class ScrollSelectionCallback implements Runnable {
        @Override
        public void run() {
            TextPane textPane = (TextPane)getComponent();
            int selectionStart = textPane.getSelectionStart();
            int selectionLength = textPane.getSelectionLength();
            int selectionEnd = selectionStart + selectionLength - 1;

            switch (scrollDirection) {
                case UP: {
                    // Get previous offset
                    int offset = getNextInsertionPoint(mouseX, selectionStart, scrollDirection);

                    if (offset != -1) {
                        textPane.setSelection(offset, selectionEnd - offset + 1);
                        scrollCharacterToVisible(offset + 1);
                    }

                    break;
                }

                case DOWN: {
                    // Get next offset
                    int offset = getNextInsertionPoint(mouseX, selectionEnd, scrollDirection);

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
                }

                default: {
                    throw new RuntimeException();
                }
            }
        }
    }

    private TextPaneSkinDocumentView documentView = null;

    private int caretX = 0;
    private Rectangle caret = new Rectangle();
    private Area selection = null;

    private boolean caretOn = false;

    private int anchor = -1;
    private TextPane.ScrollDirection scrollDirection = null;
    private int mouseX = -1;

    private BlinkCaretCallback blinkCaretCallback = new BlinkCaretCallback();
    private ApplicationContext.ScheduledCallback scheduledBlinkCaretCallback = null;

    private ScrollSelectionCallback scrollSelectionCallback = new ScrollSelectionCallback();
    private ApplicationContext.ScheduledCallback scheduledScrollSelectionCallback = null;

    private Font font;
    private Color color;
    private Color inactiveColor;
    private Color selectionColor;
    private Color selectionBackgroundColor;
    private Color inactiveSelectionColor;
    private Color inactiveSelectionBackgroundColor;

    private Insets margin = new Insets(4);

    private boolean wrapText = true;

    private static final int SCROLL_RATE = 30;

    public TextPaneSkin() {
        Theme theme = Theme.getTheme();
        font = theme.getFont();
        color = Color.BLACK;
        inactiveColor = Color.GRAY;
        selectionColor = Color.LIGHT_GRAY;
        selectionBackgroundColor = Color.BLACK;
        inactiveSelectionColor = Color.LIGHT_GRAY;
        inactiveSelectionBackgroundColor = Color.BLACK;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        TextPane textPane = (TextPane)component;
        textPane.getTextPaneListeners().add(this);
        textPane.getTextPaneSelectionListeners().add(this);

        textPane.setCursor(Cursor.TEXT);

        Document document = textPane.getDocument();
        if (document != null) {
            documentView = (TextPaneSkinDocumentView)createNodeView(document);
            documentView.attach();
            updateSelection();
        }
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth;

        if (documentView == null) {
           preferredWidth = 0;
        } else {
            Dimensions documentDimensions = documentView.getPreferredSize(Integer.MAX_VALUE);

            preferredWidth = documentDimensions.width + margin.left + margin.right;
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight;

        if (documentView == null
            || width == -1) {
            preferredHeight = 0;
        } else {
            int breakWidth;
            if (wrapText) {
                breakWidth = Math.max(width - (margin.left + margin.right), 0);
            } else {
                breakWidth = Integer.MAX_VALUE;
            }

            Dimensions documentDimensions = documentView.getPreferredSize(breakWidth);

            preferredHeight = documentDimensions.height + margin.top + margin.bottom;
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

            preferredWidth = documentDimensions.width + margin.left + margin.right;
            preferredHeight = documentDimensions.height + margin.top + margin.bottom;
        }

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();
        LineMetrics lm = font.getLineMetrics("", fontRenderContext);
        float ascent = lm.getAscent();
        return margin.top + Math.round(ascent);
    }

    @Override
    public void layout() {
        if (documentView != null) {
            TextPane textPane = (TextPane)getComponent();
            int width = getWidth();

            int breakWidth;
            if (wrapText) {
                breakWidth = Math.max(width - (margin.left + margin.right), 0);
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

            showCaret(textPane.isFocused()
                && textPane.getSelectionLength() == 0);
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        TextPane textPane = (TextPane)getComponent();

        if (documentView != null) {
            // Draw the selection highlight
            if (selection != null) {
                graphics.setColor(textPane.isFocused()
                    && textPane.isEditable() ?
                    selectionBackgroundColor : inactiveSelectionBackgroundColor);
                graphics.fill(selection);
            }

            int width = getWidth();
            int breakWidth;
            if (wrapText) {
                breakWidth = Math.max(width - (margin.left + margin.right), 0);
            } else {
                breakWidth = Integer.MAX_VALUE;
            }
            documentView.layout(breakWidth);

            // Draw the document content
            graphics.translate(margin.left, margin.top);
            documentView.paint(graphics);
            graphics.translate(-margin.left, -margin.top);

            // Draw the caret
            if (selection == null
                && caretOn
                && textPane.isFocused()) {
                graphics.setColor(textPane.isEditable() ? color : inactiveColor);
                graphics.fill(caret);
            }
        }
    }

    @Override
    public int getInsertionPoint(int x, int y) {
        int offset;

        if (documentView == null) {
            offset = -1;
        } else {
            x = Math.min(documentView.getWidth() - 1, Math.max(x - margin.left, 0));

            if (y < margin.top) {
                offset = documentView.getNextInsertionPoint(x, -1, TextPane.ScrollDirection.DOWN);
            } else if (y > documentView.getHeight() + margin.top) {
                offset = documentView.getNextInsertionPoint(x, -1, TextPane.ScrollDirection.UP);
            } else {
                offset = documentView.getInsertionPoint(x, y - margin.top);
            }
        }

        return offset;
    }

    @Override
    public int getNextInsertionPoint(int x, int from, TextPane.ScrollDirection direction) {
        int offset;

        if (documentView == null) {
            offset = -1;
        } else {
            offset = documentView.getNextInsertionPoint(x - margin.left, from, direction);
        }

        return offset;
    }

    @Override
    public int getRowAt(int offset) {
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
    public Bounds getCharacterBounds(int offset) {
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

    private void scrollCharacterToVisible(int offset) {
        TextPane textPane = (TextPane)getComponent();
        Bounds characterBounds = getCharacterBounds(offset);

        if (characterBounds != null) {
            textPane.scrollAreaToVisible(characterBounds.x, characterBounds.y,
                characterBounds.width, characterBounds.height);
        }
    }

    /**
     * Returns the font of the text
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets the font of the text
     */
    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        this.font = font;
        invalidateComponent();
    }

    /**
     * Sets the font of the text
     * @param font A {@link ComponentSkin#decodeFont(String) font specification}
     */
    public final void setFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(decodeFont(font));
    }

    /**
     * Sets the font of the text
     * @param font A dictionary {@link Theme#deriveFont describing a font}
     */
    public final void setFont(Dictionary<String, ?> font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Theme.deriveFont(font));
    }

    /**
     * Returns the foreground color of the text
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the foreground color of the text
     */
    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.color = color;
        repaintComponent();
    }

    /**
     * Sets the foreground color of the text
     * @param color Any of the {@linkplain GraphicsUtilities#decodeColor color values recognized by Pivot}.
     */
    public final void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(GraphicsUtilities.decodeColor(color));
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

    /**
     * Returns the amount of space between the edge of the TextPane and its Document
     */
    public Insets getMargin() {
        return margin;
    }

    /**
     * Sets the amount of space between the edge of the TextPane and its Document
     */
    public void setMargin(Insets margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        this.margin = margin;
        invalidateComponent();
    }

    /**
     * Sets the amount of space between the edge of the TextPane and its Document
     * @param margin A dictionary with keys in the set {left, top, bottom, right}.
     */
    public final void setMargin(Dictionary<String, ?> margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        setMargin(new Insets(margin));
    }

    /**
     * Sets the amount of space between the edge of the TextPane and its Document
     */
    public final void setMargin(int margin) {
        setMargin(new Insets(margin));
    }

    /**
     * Sets the amount of space between the edge of the TextPane and its Document
     */
    public final void setMargin(Number margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        setMargin(margin.intValue());
    }

    /**
     * Sets the amount of space between the edge of the TextPane and its Document
     * @param margin A string containing an integer or a JSON dictionary with keys
     * left, top, bottom, and/or right.
     */
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
        if (this.wrapText != wrapText) {
            this.wrapText = wrapText;

            if (documentView != null) {
                documentView.invalidateUpTree();
            }
        }
    }

    @Override
    public boolean mouseMove(Component component, int x, int y) {
        boolean consumed = super.mouseMove(component, x, y);

        if (Mouse.getCapturer() == component) {
            TextPane textPane = (TextPane)getComponent();

            Bounds visibleArea = textPane.getVisibleArea();
            visibleArea = new Bounds(visibleArea.x, visibleArea.y,
                visibleArea.width, visibleArea.height);

            if (y >= visibleArea.y
                && y < visibleArea.y + visibleArea.height) {
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
                    scrollDirection = (y < visibleArea.y) ? TextPane.ScrollDirection.UP : TextPane.ScrollDirection.DOWN;

                    scheduledScrollSelectionCallback =
                        ApplicationContext.scheduleRecurringCallback(scrollSelectionCallback,
                            SCROLL_RATE);

                    // Run the callback once now to scroll the selection immediately
                    scrollSelectionCallback.run();
                }
            }

            mouseX = x;
        } else {
            if (Mouse.isPressed(Mouse.Button.LEFT)
                && Mouse.getCapturer() == null
                && anchor != -1) {
                // Capture the mouse so we can select text
                Mouse.capture(component);
            }
        }

        return consumed;
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        if (button == Mouse.Button.LEFT) {
            TextPane textPane = (TextPane)component;

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
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
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
    public boolean keyTyped(final Component component, char character) {
        boolean consumed = super.keyTyped(component, character);

        final TextPane textPane = (TextPane)getComponent();

        if (textPane.isEditable()) {
            Document document = textPane.getDocument();

            if (document != null) {
                // Ignore characters in the control range and the ASCII delete
                // character as well as meta key presses
                if (character > 0x1F
                    && character != 0x7F
                    && !Keyboard.isPressed(Keyboard.Modifier.META)) {
                    textPane.insert(character);
                    showCaret(true);
                }
            }
        }

        return consumed;
    }

    @Override
    public boolean keyPressed(final Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        final TextPane textPane = (TextPane)getComponent();
        Document document = textPane.getDocument();

        Keyboard.Modifier commandModifier = Platform.getCommandModifier();
        if (document != null) {
            if (keyCode == Keyboard.KeyCode.ENTER
                && textPane.isEditable()) {
                textPane.insertParagraph();

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.DELETE
                && textPane.isEditable()) {
                textPane.delete(false);

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.BACKSPACE
                && textPane.isEditable()) {
                textPane.delete(true);

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.LEFT) {
                int selectionStart = textPane.getSelectionStart();
                int selectionLength = textPane.getSelectionLength();

                if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                    // Add the previous character to the selection
                    if (selectionStart > 0) {
                        selectionStart--;
                        selectionLength++;
                    }
                } else if (Keyboard.isPressed(Keyboard.Modifier.CTRL)) {
                    // Move the caret to the start of the next word to our left
                    if (selectionStart > 0) {
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

                        selectionLength = 0;
                    }
                } else {
                    // Clear the selection and move the caret back by one
                    // character
                    if (selectionLength == 0
                        && selectionStart > 0) {
                        selectionStart--;
                    }

                    selectionLength = 0;
                }

                textPane.setSelection(selectionStart, selectionLength);
                scrollCharacterToVisible(selectionStart);

                caretX = caret.x;

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.RIGHT) {
                int selectionStart = textPane.getSelectionStart();
                int selectionLength = textPane.getSelectionLength();

                if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                    // Add the next character to the selection
                    if (selectionStart + selectionLength < document.getCharacterCount()) {
                        selectionLength++;
                    }

                    textPane.setSelection(selectionStart, selectionLength);
                    scrollCharacterToVisible(selectionStart + selectionLength);
                } else if (Keyboard.isPressed(Keyboard.Modifier.CTRL)) {
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
                    // Clear the selection and move the caret forward by one
                    // character
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
                int selectionStart = textPane.getSelectionStart();

                int offset = getNextInsertionPoint(caretX, selectionStart, TextPane.ScrollDirection.UP);

                if (offset == -1) {
                    offset = 0;
                }

                int selectionLength;
                if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                    int selectionEnd = selectionStart + textPane.getSelectionLength() - 1;
                    selectionLength = selectionEnd - offset + 1;
                } else {
                    selectionLength = 0;
                }

                textPane.setSelection(offset, selectionLength);
                scrollCharacterToVisible(offset);

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.DOWN) {
                int selectionStart = textPane.getSelectionStart();
                int selectionLength = textPane.getSelectionLength();

                if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                    int from;
                    int x;
                    if (selectionLength == 0) {
                        // Get next insertion point from leading selection character
                        from = selectionStart;
                        x = caretX;
                    } else {
                        // Get next insertion point from right edge of trailing selection
                        // character
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
            } else if (Keyboard.isPressed(commandModifier)
                    && keyCode == Keyboard.KeyCode.TAB
                    && textPane.isEditable()) {
                    textPane.insert("\t");
                    showCaret(true);

                    consumed = true;
            } else if (Keyboard.isPressed(commandModifier)) {
                if (keyCode == Keyboard.KeyCode.A) {
                    textPane.setSelection(0, document.getCharacterCount());
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.X
                    && textPane.isEditable()) {
                    textPane.cut();
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.C) {
                    textPane.copy();
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.V
                    && textPane.isEditable()) {
                    textPane.paste();
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.Z
                    && textPane.isEditable()) {
                    if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                        textPane.redo();
                    } else {
                        textPane.undo();
                    }

                    consumed = true;
                }
            } else if (keyCode == Keyboard.KeyCode.HOME) {
                // Move the caret to the beginning of the text
                if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                    textPane.setSelection(0, textPane.getSelectionStart());
                } else {
                    textPane.setSelection(0, 0);
                }
                scrollCharacterToVisible(0);

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.END) {
                // Move the caret to the end of the text
                if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                    int selectionStart = textPane.getSelectionStart();
                    textPane.setSelection(selectionStart, textPane.getCharacterCount()
                        - selectionStart);
                } else {
                    textPane.setSelection(textPane.getCharacterCount() - 1, 0);
                }
                scrollCharacterToVisible(textPane.getCharacterCount() - 1);

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.INSERT) {
                if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)
                    && textPane.isEditable()) {
                    textPane.paste();
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
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        repaintComponent();
    }

    @Override
    public void focusedChanged(Component component, Component obverseComponent) {
        super.focusedChanged(component, obverseComponent);

        TextPane textPane = (TextPane)getComponent();
        if (textPane.isFocused()
            && textPane.getSelectionLength() == 0) {
            scrollCharacterToVisible(textPane.getSelectionStart());
            showCaret(true);
        } else {
            showCaret(false);
        }

        repaintComponent();
    }

    // Text pane events
    @Override
    public void documentChanged(TextPane textPane, Document previousDocument) {
        if (documentView != null) {
            documentView.detach();
            documentView = null;
        }

        Document document = textPane.getDocument();
        if (document != null) {
            documentView = (TextPaneSkinDocumentView)createNodeView(document);
            documentView.attach();
        }

        invalidateComponent();
    }

    @Override
    public void editableChanged(TextPane textPane) {
        // No-op
    }

    // Text pane selection events
    @Override
    public void selectionChanged(TextPane textPane, int previousSelectionStart,
        int previousSelectionLength) {
        // If the document view is valid, repaint the selection state; otherwise,
        // the selection will be updated in layout()
        if (documentView != null
            && documentView.isValid()) {
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

    TextPaneSkinNodeView createNodeView(Node node) {
        TextPaneSkinNodeView nodeView = null;

        if (node instanceof Document) {
            nodeView = new TextPaneSkinDocumentView(this, (Document)node);
        } else if (node instanceof Paragraph) {
            nodeView = new TextPaneSkinParagraphView((Paragraph)node);
        } else if (node instanceof TextNode) {
            nodeView = new TextPaneSkinTextNodeView((TextNode)node);
        } else if (node instanceof ImageNode) {
            nodeView = new TextPaneSkinImageNodeView((ImageNode)node);
        } else if (node instanceof ComponentNode) {
            nodeView = new TextPaneSkinComponentNodeView((ComponentNode)node);
        } else if (node instanceof TextSpan) {
            nodeView = new TextPaneSkinSpanView((TextSpan)node);
        } else if (node instanceof NumberedList) {
            nodeView = new TextPaneSkinNumberedListView((NumberedList)node);
        } else if (node instanceof BulletedList) {
            nodeView = new TextPaneSkinBulletedListView((BulletedList)node);
        } else if (node instanceof List.Item) {
            nodeView = new TextPaneSkinListItemView((List.Item)node);
        } else {
            throw new IllegalArgumentException("Unsupported node type: "
                + node.getClass().getName());
        }

        return nodeView;
    }

    private void updateSelection() {
        if (documentView.getCharacterCount() > 0) {
            TextPane textPane = (TextPane)getComponent();

            // Update the caret
            int selectionStart = textPane.getSelectionStart();

            Bounds leadingSelectionBounds = getCharacterBounds(selectionStart);
            // sanity check - this is where a lot of bugs show up
            if (leadingSelectionBounds == null) {
                throw new IllegalStateException("no bounds for selection " + selectionStart);
            }
            caret = leadingSelectionBounds.toRectangle();
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

    Area getSelection() {
        return selection;
    }

    void invalidateNodeViewTree() {
        this.documentView.invalidateDownTree();
        invalidateComponent();
    }
}
