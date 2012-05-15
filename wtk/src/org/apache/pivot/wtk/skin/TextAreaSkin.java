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
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TextAreaContentListener;
import org.apache.pivot.wtk.TextAreaListener;
import org.apache.pivot.wtk.TextAreaSelectionListener;
import org.apache.pivot.wtk.Theme;

/**
 * Text area skin.
 */
public class TextAreaSkin extends ComponentSkin implements TextArea.Skin, TextAreaListener,
    TextAreaContentListener, TextAreaSelectionListener {
    private class BlinkCaretCallback implements Runnable {
        @Override
        public void run() {
            caretOn = !caretOn;

            if (selection == null) {
                TextArea textArea = (TextArea) getComponent();
                textArea.repaint(caret.x, caret.y, caret.width, caret.height);
            }
        }
    }

    private class ScrollSelectionCallback implements Runnable {
        @Override
        public void run() {
            TextArea textArea = (TextArea)getComponent();
            int selectionStart = textArea.getSelectionStart();
            int selectionLength = textArea.getSelectionLength();
            int selectionEnd = selectionStart + selectionLength - 1;

            switch (scrollDirection) {
                case UP: {
                    // Get previous offset
                    int index = getNextInsertionPoint(mouseX, selectionStart, scrollDirection);

                    if (index != -1) {
                        textArea.setSelection(index, selectionEnd - index + 1);
                        scrollCharacterToVisible(index + 1);
                    }

                    break;
                }

                case DOWN: {
                    // Get next offset
                    int index = getNextInsertionPoint(mouseX, selectionEnd, scrollDirection);

                    if (index != -1) {
                        // If the next character is a paragraph terminator, increment
                        // the selection
                        if (index < textArea.getCharacterCount()
                            && textArea.getCharacterAt(index) == '\n') {
                            index++;
                        }

                        textArea.setSelection(selectionStart, index - selectionStart);
                        scrollCharacterToVisible(index - 1);
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
    private TextArea.ScrollDirection scrollDirection = null;
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
    private Insets margin;
    private boolean wrapText;
    private int tabWidth;
    private int lineWidth;
    private boolean acceptsEnter = true;

    private Dimensions averageCharacterSize;

    private ArrayList<TextAreaSkinParagraphView> paragraphViews = new ArrayList<TextAreaSkinParagraphView>();

    private static final int SCROLL_RATE = 30;

    public TextAreaSkin() {
        Theme theme = Theme.getTheme();
        font = theme.getFont();
        color = Color.BLACK;
        backgroundColor = null;
        inactiveColor = Color.GRAY;
        selectionColor = Color.LIGHT_GRAY;
        selectionBackgroundColor = Color.BLACK;
        inactiveSelectionColor = Color.LIGHT_GRAY;
        inactiveSelectionBackgroundColor = Color.BLACK;
        margin = new Insets(4);
        wrapText = true;
        tabWidth = 4;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        TextArea textArea = (TextArea)component;
        textArea.getTextAreaListeners().add(this);
        textArea.getTextAreaContentListeners().add(this);
        textArea.getTextAreaSelectionListeners().add(this);
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        if (lineWidth <= 0) {
            for (TextAreaSkinParagraphView paragraphView : paragraphViews) {
                paragraphView.setBreakWidth(Integer.MAX_VALUE);
                preferredWidth = Math.max(preferredWidth, paragraphView.getWidth());
            }
        } else {
            preferredWidth = averageCharacterSize.width * lineWidth;
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

        for (TextAreaSkinParagraphView paragraphView : paragraphViews) {
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

        for (TextAreaSkinParagraphView paragraphView : paragraphViews) {
            paragraphView.setBreakWidth(Integer.MAX_VALUE);
            preferredWidth = Math.max(preferredWidth, paragraphView.getWidth());
            preferredHeight += paragraphView.getHeight();
        }

        preferredWidth += margin.left + margin.right;
        preferredHeight += margin.top + margin.bottom;

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @SuppressWarnings("unused")
    @Override
    public void layout() {
        TextArea textArea = (TextArea)getComponent();

        int width = getWidth();
        int breakWidth = (wrapText) ? Math.max(width - (margin.left + margin.right), 0)
            : Integer.MAX_VALUE;

        int y = margin.top;
        int lastY = 0;
        int lastHeight = 0;

        int rowOffset = 0;
        int index = 0;
        for (TextAreaSkinParagraphView paragraphView : paragraphViews) {
            paragraphView.setBreakWidth(breakWidth);
            paragraphView.setX(margin.left);
            paragraphView.setY(y);
            lastY = y;
            y += paragraphView.getHeight();
            lastHeight = paragraphView.getHeight();

            paragraphView.setRowOffset(rowOffset);
            rowOffset += paragraphView.getRowCount();
            index++;
        }

        updateSelection();
        caretX = caret.x;

        if (textArea.isFocused()) {
            scrollCharacterToVisible(textArea.getSelectionStart());
            showCaret(textArea.getSelectionLength() == 0);
        } else {
            showCaret(false);
        }
    }

    @Override
    public int getBaseline(int width, int height) {
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();
        LineMetrics lm = font.getLineMetrics("", fontRenderContext);

        return Math.round(margin.top + lm.getAscent());
    }

    @Override
    public void paint(Graphics2D graphics) {
        TextArea textArea = (TextArea)getComponent();
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
                && textArea.isEditable() ? selectionBackgroundColor : inactiveSelectionBackgroundColor);
            graphics.fill(selection);
        }

        // Draw the text
        graphics.setFont(font);
        graphics.translate(0, margin.top);

        int breakWidth = (wrapText) ? Math.max(width - (margin.left + margin.right), 0)
            : Integer.MAX_VALUE;

        for (int i = 0, n = paragraphViews.getLength(); i < n; i++) {
            TextAreaSkinParagraphView paragraphView = paragraphViews.get(i);
            paragraphView.setBreakWidth(breakWidth);
            paragraphView.validate();

            int x = paragraphView.getX();
            graphics.translate(x, 0);
            paragraphView.paint(graphics);
            graphics.translate(-x, 0);

            graphics.translate(0, paragraphView.getHeight());
        }
    }

    @Override
    public boolean isOpaque() {
        return (backgroundColor != null
            && backgroundColor.getTransparency() == Transparency.OPAQUE);
    }

    @Override
    public int getInsertionPoint(int x, int y) {
        int index = -1;

        if (paragraphViews.getLength() > 0) {
            TextAreaSkinParagraphView lastParagraphView = paragraphViews.get(paragraphViews.getLength() - 1);
            if (y > lastParagraphView.getY() + lastParagraphView.getHeight()) {
                // Select the character at x in the last row
                TextAreaSkinParagraphView paragraphView = paragraphViews.get(paragraphViews.getLength() - 1);
                index = paragraphView.getNextInsertionPoint(x, -1, TextArea.ScrollDirection.UP)
                    + paragraphView.getParagraph().getOffset();
            } else if (y < margin.top) {
                // Select the character at x in the first row
                TextAreaSkinParagraphView paragraphView = paragraphViews.get(0);
                index = paragraphView.getNextInsertionPoint(x, -1, TextArea.ScrollDirection.DOWN);
            } else {
                // Select the character at x in the row at y
                for (int i = 0, n = paragraphViews.getLength(); i < n; i++) {
                    TextAreaSkinParagraphView paragraphView = paragraphViews.get(i);

                    int paragraphViewY = paragraphView.getY();
                    if (y >= paragraphViewY
                        && y < paragraphViewY + paragraphView.getHeight()) {
                        index = paragraphView.getInsertionPoint(x - paragraphView.getX(), y - paragraphViewY)
                            + paragraphView.getParagraph().getOffset();
                        break;
                    }
                }
            }
        }

        return index;
    }

    @Override
    public int getNextInsertionPoint(int x, int from, TextArea.ScrollDirection direction) {
        int index = -1;

        if (paragraphViews.getLength() > 0) {
            if (from == -1) {
                int i = (direction == TextArea.ScrollDirection.DOWN) ? 0 : paragraphViews.getLength() - 1;

                TextAreaSkinParagraphView paragraphView = paragraphViews.get(i);
                index = paragraphView.getNextInsertionPoint(x - paragraphView.getX(), -1, direction);

                if (index != -1) {
                    index += paragraphView.getParagraph().getOffset();
                }
            } else {
                TextArea textArea = (TextArea)getComponent();
                int i = textArea.getParagraphAt(from);

                TextAreaSkinParagraphView paragraphView = paragraphViews.get(i);
                index = paragraphView.getNextInsertionPoint(x - paragraphView.getX(),
                    from - paragraphView.getParagraph().getOffset(), direction);

                if (index == -1) {
                    // Move to the next or previous paragraph view
                    if (direction == TextArea.ScrollDirection.DOWN) {
                        paragraphView = (i < paragraphViews.getLength() - 1) ? paragraphViews.get(i + 1) : null;
                    } else {
                        paragraphView = (i > 0) ? paragraphViews.get(i - 1) : null;
                    }

                    if (paragraphView != null) {
                        index = paragraphView.getNextInsertionPoint(x - paragraphView.getX(), -1, direction);
                    }
                }

                if (index != -1) {
                    index += (paragraphView != null) ? paragraphView.getParagraph().getOffset() : 0;
                }
            }
        }

        return index;
    }

    @Override
    public int getRowAt(int index) {
        int rowIndex = -1;

        if (paragraphViews.getLength() > 0) {
            TextArea textArea = (TextArea)getComponent();
            TextAreaSkinParagraphView paragraphView = paragraphViews.get(textArea.getParagraphAt(index));

            rowIndex = paragraphView.getRowAt(index - paragraphView.getParagraph().getOffset())
                + paragraphView.getRowOffset();
        }

        return rowIndex;
    }

    @Override
    public int getRowOffset(int index) {
        int rowOffset = -1;

        if (paragraphViews.getLength() > 0) {
            TextArea textArea = (TextArea)getComponent();
            TextAreaSkinParagraphView paragraphView = paragraphViews.get(textArea.getParagraphAt(index));

            rowOffset = paragraphView.getRowOffset(index - paragraphView.getParagraph().getOffset())
                + paragraphView.getParagraph().getOffset();
        }

        return rowOffset;
    }

    @Override
    public int getRowLength(int index) {
        int rowLength = -1;

        if (paragraphViews.getLength() > 0) {
            TextArea textArea = (TextArea)getComponent();
            TextAreaSkinParagraphView paragraphView = paragraphViews.get(textArea.getParagraphAt(index));

            rowLength = paragraphView.getRowLength(index - paragraphView.getParagraph().getOffset());
        }

        return rowLength;
    }

    @Override
    public int getRowCount() {
        int rowCount = 0;

        for (TextAreaSkinParagraphView paragraphView : paragraphViews) {
            rowCount += paragraphView.getRowCount();
        }

        return rowCount;
    }

    @Override
    public Bounds getCharacterBounds(int index) {
        Bounds characterBounds = null;

        if (paragraphViews.getLength() > 0) {
            TextArea textArea = (TextArea)getComponent();
            TextAreaSkinParagraphView paragraphView = paragraphViews.get(textArea.getParagraphAt(index));
            characterBounds = paragraphView.getCharacterBounds(index
                - paragraphView.getParagraph().getOffset());

            characterBounds = new Bounds(characterBounds.x + paragraphView.getX(),
                characterBounds.y + paragraphView.getY(),
                characterBounds.width, characterBounds.height);
        }

        return characterBounds;
    }

    public Area getSelection() {
        return selection;
    }

    private void scrollCharacterToVisible(int index) {
        Bounds characterBounds = getCharacterBounds(index);

        if (characterBounds != null) {
            TextArea textArea = (TextArea)getComponent();
            textArea.scrollAreaToVisible(characterBounds.x, characterBounds.y,
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

        int missingGlyphCode = font.getMissingGlyphCode();
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();

        GlyphVector missingGlyphVector = font.createGlyphVector(fontRenderContext,
            new int[] {missingGlyphCode});
        Rectangle2D textBounds = missingGlyphVector.getLogicalBounds();

        Rectangle2D maxCharBounds = font.getMaxCharBounds(fontRenderContext);
        averageCharacterSize = new Dimensions((int)Math.ceil(textBounds.getWidth()),
            (int)Math.ceil(maxCharBounds.getHeight()));

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

    /**
     * Returns the amount of space between the edge of the TextArea and its text
     */
    public Insets getMargin() {
        return margin;
    }

    /**
     * Sets the amount of space between the edge of the TextArea and its text
     */
    public void setMargin(Insets margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        this.margin = margin;
        invalidateComponent();
    }

    /**
     * Sets the amount of space between the edge of the TextArea and its text
     * @param margin A dictionary with keys in the set {left, top, bottom, right}.
     */
    public final void setMargin(Dictionary<String, ?> margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        setMargin(new Insets(margin));
    }

    /**
     * Sets the amount of space between the edge of the TextArea and its text
     */
    public final void setMargin(int margin) {
        setMargin(new Insets(margin));
    }

    /**
     * Sets the amount of space between the edge of the TextArea and its text
     */
    public final void setMargin(Number margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        setMargin(margin.intValue());
    }

    /**
     * Sets the amount of space between the edge of the TextArea and its text
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
        this.wrapText = wrapText;
        invalidateComponent();
    }

    public boolean getAcceptsEnter() {
        return acceptsEnter;
    }

    public void setAcceptsEnter(boolean acceptsEnter) {
        this.acceptsEnter = acceptsEnter;
    }

    public int getTabWidth() {
        return tabWidth;
    }

    public void setTabWidth(int tabWidth) {
        if (tabWidth < 0) {
            throw new IllegalArgumentException("tabWidth is negative.");
        }

        this.tabWidth = tabWidth;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        if (this.lineWidth != lineWidth) {
            this.lineWidth = lineWidth;

            int missingGlyphCode = font.getMissingGlyphCode();
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();

            GlyphVector missingGlyphVector = font.createGlyphVector(fontRenderContext,
                new int[] {missingGlyphCode});
            Rectangle2D textBounds = missingGlyphVector.getLogicalBounds();

            Rectangle2D maxCharBounds = font.getMaxCharBounds(fontRenderContext);
            averageCharacterSize = new Dimensions((int)Math.ceil(textBounds.getWidth()),
                (int)Math.ceil(maxCharBounds.getHeight()));

            invalidateComponent();
        }
    }

    @Override
    public boolean mouseMove(Component component, int x, int y) {
        boolean consumed = super.mouseMove(component, x, y);

        if (Mouse.getCapturer() == component) {
            TextArea textArea = (TextArea)getComponent();

            Bounds visibleArea = textArea.getVisibleArea();
            visibleArea = new Bounds(visibleArea.x, visibleArea.y, visibleArea.width,
                visibleArea.height);

            // if it's inside the visible area, stop the scroll timer
            if (y >= visibleArea.y
                && y < visibleArea.y + visibleArea.height) {
                // Stop the scroll selection timer
                if (scheduledScrollSelectionCallback != null) {
                    scheduledScrollSelectionCallback.cancel();
                    scheduledScrollSelectionCallback = null;
                }

                scrollDirection = null;
            } else {
                // if it's outside the visible area, start the scroll timer
                if (scheduledScrollSelectionCallback == null) {
                    scrollDirection = (y < visibleArea.y) ? TextArea.ScrollDirection.UP
                        : TextArea.ScrollDirection.DOWN;

                    scheduledScrollSelectionCallback = ApplicationContext.scheduleRecurringCallback(
                        scrollSelectionCallback, SCROLL_RATE);

                    // Run the callback once now to scroll the selection immediately
                    scrollSelectionCallback.run();
                }
            }

            int index = getInsertionPoint(x, y);

            if (index != -1) {
                // Select the range
                if (index > anchor) {
                    textArea.setSelection(anchor, index - anchor);
                } else {
                    textArea.setSelection(index, anchor - index);
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

        TextArea textArea = (TextArea)component;

        if (button == Mouse.Button.LEFT) {
            anchor = getInsertionPoint(x, y);

            if (anchor != -1) {
                if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                    // Select the range
                    int selectionStart = textArea.getSelectionStart();

                    if (anchor > selectionStart) {
                        textArea.setSelection(selectionStart, anchor - selectionStart);
                    } else {
                        textArea.setSelection(anchor, selectionStart - anchor);
                    }
                } else {
                    // Move the caret to the insertion point
                    textArea.setSelection(anchor, 0);
                    consumed = true;
                }
            }

            caretX = caret.x;

            // Set focus to the text input
            textArea.requestFocus();
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
    public boolean keyTyped(Component component, char character) {
        boolean consumed = super.keyTyped(component, character);

        if (paragraphViews.getLength() > 0) {
            TextArea textArea = (TextArea)getComponent();

            if (textArea.isEditable()) {
                // Ignore characters in the control range and the ASCII delete
                // character as well as meta key presses
                if (character > 0x1F
                    && character != 0x7F
                    && !Keyboard.isPressed(Keyboard.Modifier.META)) {
                    int selectionLength = textArea.getSelectionLength();

                    if (textArea.getCharacterCount() - selectionLength + 1 > textArea.getMaximumLength()) {
                        Toolkit.getDefaultToolkit().beep();
                    } else {
                        int selectionStart = textArea.getSelectionStart();
                        textArea.removeText(selectionStart, selectionLength);
                        textArea.insertText(Character.toString(character), selectionStart);
                    }

                    showCaret(true);
                }
            }
        }

        return consumed;
    }

    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (paragraphViews.getLength() > 0) {
            TextArea textArea = (TextArea)getComponent();
            Keyboard.Modifier commandModifier = Platform.getCommandModifier();
            Keyboard.Modifier wordNavigationModifier = Platform.getWordNavigationModifier();

            if (keyCode == Keyboard.KeyCode.ENTER
                && acceptsEnter
                && textArea.isEditable()
                && Keyboard.getModifiers() == 0) {
                int index = textArea.getSelectionStart();
                textArea.removeText(index, textArea.getSelectionLength());
                textArea.insertText("\n", index);

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.DELETE
                && textArea.isEditable()) {
                int index = textArea.getSelectionStart();

                if (index < textArea.getCharacterCount()) {
                    int count = Math.max(textArea.getSelectionLength(), 1);
                    textArea.removeText(index, count);

                    consumed = true;
                }
            } else if (keyCode == Keyboard.KeyCode.BACKSPACE
                && textArea.isEditable()) {
                int index = textArea.getSelectionStart();
                int count = textArea.getSelectionLength();

                if (count == 0
                    && index > 0) {
                    textArea.removeText(index - 1, 1);
                    consumed = true;
                } else {
                    textArea.removeText(index, count);
                    consumed = true;
                }
            } else if (keyCode == Keyboard.KeyCode.TAB
                && Keyboard.isPressed(Keyboard.Modifier.CTRL)
                && textArea.isEditable()) {
                int selectionLength = textArea.getSelectionLength();

                StringBuilder tabBuilder = new StringBuilder(tabWidth);
                for (int i = 0; i < tabWidth; i++) {
                    tabBuilder.append(" ");
                }

                if (textArea.getCharacterCount() - selectionLength + tabWidth > textArea.getMaximumLength()) {
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    int selectionStart = textArea.getSelectionStart();
                    textArea.removeText(selectionStart, selectionLength);
                    textArea.insertText(tabBuilder, selectionStart);
                }

                showCaret(true);

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.HOME
                || (keyCode == Keyboard.KeyCode.LEFT
                    && Keyboard.isPressed(Keyboard.Modifier.META))) {
                // Move the caret to the beginning of the line
                int selectionStart = textArea.getSelectionStart();
                int selectionLength = textArea.getSelectionLength();
                int rowOffset = getRowOffset(selectionStart);

                if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                    selectionLength += selectionStart - rowOffset;
                }

                if (selectionStart >= 0) {
                    textArea.setSelection(rowOffset, selectionLength);
                    scrollCharacterToVisible(rowOffset);

                    caretX = caret.x;

                    consumed = true;
                }
            } else if (keyCode == Keyboard.KeyCode.END
                || (keyCode == Keyboard.KeyCode.RIGHT
                    && Keyboard.isPressed(Keyboard.Modifier.META))) {
                // Move the caret to the end of the line
                int selectionStart = textArea.getSelectionStart();
                int selectionLength = textArea.getSelectionLength();

                int index = selectionStart + selectionLength;
                int rowOffset = getRowOffset(index);
                int rowLength = getRowLength(index);

                if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                    selectionLength += (rowOffset + rowLength) - index;
                } else {
                    selectionStart = rowOffset + rowLength;
                    if (selectionStart < textArea.getCharacterCount()
                            && textArea.getCharacterAt(selectionStart) != '\n') {
                        selectionStart--;
                    }

                    selectionLength = 0;
                }

                if (selectionStart + selectionLength <= textArea.getCharacterCount()) {
                    textArea.setSelection(selectionStart, selectionLength);
                    scrollCharacterToVisible(selectionStart + selectionLength);

                    caretX = caret.x;
                    if (selection != null) {
                        caretX += selection.getBounds2D().getWidth();
                    }

                    consumed = true;
                }
            } else if (keyCode == Keyboard.KeyCode.LEFT) {
                int selectionStart = textArea.getSelectionStart();
                int selectionLength = textArea.getSelectionLength();

                if (Keyboard.isPressed(wordNavigationModifier)) {
                    // Move the caret to the start of the next word to the left
                    if (selectionStart > 0) {
                        // Skip over any space immediately to the left
                        int index = selectionStart;
                        while (index > 0
                            && Character.isWhitespace(textArea.getCharacterAt(index - 1))) {
                            index--;
                        }

                        // Skip over any word-letters to the left
                        while (index > 0
                            && !Character.isWhitespace(textArea.getCharacterAt(index - 1))) {
                            index--;
                        }

                        if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                            selectionLength += selectionStart - index;
                        } else {
                            selectionLength = 0;
                        }

                        selectionStart = index;
                    }
                } else if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                    // Add the previous character to the selection
                    if (selectionStart > 0) {
                        selectionStart--;
                        selectionLength++;
                    }
                } else {
                    // Move the caret back by one character
                    if (selectionLength == 0
                        && selectionStart > 0) {
                        selectionStart--;
                    }

                    // Clear the selection
                    selectionLength = 0;
                }

                if (selectionStart >= 0) {
                    textArea.setSelection(selectionStart, selectionLength);
                    scrollCharacterToVisible(selectionStart);

                    caretX = caret.x;

                    consumed = true;
                }
            } else if (keyCode == Keyboard.KeyCode.RIGHT) {
                int selectionStart = textArea.getSelectionStart();
                int selectionLength = textArea.getSelectionLength();

                if (Keyboard.isPressed(wordNavigationModifier)) {
                    // Move the caret to the start of the next word to the right
                    if (selectionStart < textArea.getCharacterCount()) {
                        int index = selectionStart + selectionLength;

                        // Skip over any space immediately to the right
                        while (index < textArea.getCharacterCount()
                            && Character.isWhitespace(textArea.getCharacterAt(index))) {
                            index++;
                        }

                        // Skip over any word-letters to the right
                        while (index < textArea.getCharacterCount()
                            && !Character.isWhitespace(textArea.getCharacterAt(index))) {
                            index++;
                        }

                        if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                            selectionLength = index - selectionStart;
                        } else {
                            selectionStart = index;
                            selectionLength = 0;
                        }
                    }
                } else if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                    // Add the next character to the selection
                    selectionLength++;
                } else {
                    // Move the caret forward by one character
                    if (selectionLength == 0) {
                        selectionStart++;
                    } else {
                        selectionStart += selectionLength;
                    }

                    // Clear the selection
                    selectionLength = 0;
                }

                if (selectionStart + selectionLength <= textArea.getCharacterCount()) {
                    textArea.setSelection(selectionStart, selectionLength);
                    scrollCharacterToVisible(selectionStart + selectionLength);

                    caretX = caret.x;
                    if (selection != null) {
                        caretX += selection.getBounds2D().getWidth();
                    }

                    consumed = true;
                }
            } else if (keyCode == Keyboard.KeyCode.UP) {
                int selectionStart = textArea.getSelectionStart();

                int index = getNextInsertionPoint(caretX, selectionStart, TextArea.ScrollDirection.UP);

                if (index != -1) {
                    int selectionLength;
                    if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                        int selectionEnd = selectionStart + textArea.getSelectionLength() - 1;
                        selectionLength = selectionEnd - index + 1;
                    } else {
                        selectionLength = 0;
                    }

                    textArea.setSelection(index, selectionLength);
                    scrollCharacterToVisible(index);
                }

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.DOWN) {
                int selectionStart = textArea.getSelectionStart();
                int selectionLength = textArea.getSelectionLength();

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
                        from = selectionStart + selectionLength;

                        Bounds trailingSelectionBounds = getCharacterBounds(from);
                        x = trailingSelectionBounds.x + trailingSelectionBounds.width;
                    }

                    int index = getNextInsertionPoint(x, from, TextArea.ScrollDirection.DOWN);

                    if (index != -1) {
                        // If the next character is a paragraph terminator and is
                        // not the final terminator character, increment the selection
                        if (index < textArea.getCharacterCount() - 1
                            && textArea.getCharacterAt(index) == '\n') {
                            index++;
                        }

                        textArea.setSelection(selectionStart, index - selectionStart);
                        scrollCharacterToVisible(index);
                    }
                } else {
                    int from;
                    if (selectionLength == 0) {
                        // Get next insertion point from leading selection character
                        from = selectionStart;
                    } else {
                        // Get next insertion point from trailing selection character
                        from = selectionStart + selectionLength - 1;
                    }

                    int index = getNextInsertionPoint(caretX, from, TextArea.ScrollDirection.DOWN);

                    if (index != -1) {
                        textArea.setSelection(index, 0);
                        scrollCharacterToVisible(index);
                    }
                }

                consumed = true;
            } else if (Keyboard.isPressed(commandModifier)) {
                if (keyCode == Keyboard.KeyCode.A) {
                    textArea.setSelection(0, textArea.getCharacterCount());
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.X
                    && textArea.isEditable()) {
                    textArea.cut();
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.C) {
                    textArea.copy();
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.V
                    && textArea.isEditable()) {
                    textArea.paste();
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.Z
                    && textArea.isEditable()) {
                    if (!Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                        textArea.undo();
                    }

                    consumed = true;
                }
            } else if (keyCode == Keyboard.KeyCode.INSERT) {
                if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)
                    && textArea.isEditable()) {
                    textArea.paste();
                    consumed = true;
                }
            } else {
                consumed = super.keyPressed(component, keyCode, keyLocation);
            }
        }

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

        TextArea textArea = (TextArea)getComponent();
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
    public void maximumLengthChanged(TextArea textArea, int previousMaximumLength) {
        // No-op
    }

    @Override
    public void editableChanged(TextArea textArea) {
        // No-op
    }

    @Override
    public void paragraphInserted(TextArea textArea, int index) {
        // Create paragraph view and add as paragraph listener
        TextArea.Paragraph paragraph = textArea.getParagraphs().get(index);
        TextAreaSkinParagraphView paragraphView = new TextAreaSkinParagraphView(this, paragraph);
        paragraph.getParagraphListeners().add(paragraphView);

        // Insert view
        paragraphViews.insert(paragraphView, index);

        invalidateComponent();
    }

    @Override
    public void paragraphsRemoved(TextArea textArea, int index, Sequence<TextArea.Paragraph> removed) {
        // Remove paragraph views as paragraph listeners
        int count = removed.getLength();

        for (int i = 0; i < count; i++) {
            TextArea.Paragraph paragraph = removed.get(i);
            TextAreaSkinParagraphView paragraphView = paragraphViews.get(i + index);
            paragraph.getParagraphListeners().remove(paragraphView);
        }

        // Remove views
        paragraphViews.remove(index, count);

        invalidateComponent();
    }

    @Override
    public void textChanged(TextArea textArea) {
        // No-op
    }

    @Override
    public void selectionChanged(TextArea textArea, int previousSelectionStart,
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
        TextArea textArea = (TextArea)getComponent();

        if (paragraphViews.getLength() > 0) {
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

    private void showCaret(boolean show) {
        if (scheduledBlinkCaretCallback != null) {
            scheduledBlinkCaretCallback.cancel();
        }

        if (show) {
            caretOn = true;
            scheduledBlinkCaretCallback = ApplicationContext.scheduleRecurringCallback(
                blinkCaretCallback, Platform.getCursorBlinkRate());

            // Run the callback once now to show the cursor immediately
            blinkCaretCallback.run();
        } else {
            scheduledBlinkCaretCallback = null;
        }
    }
}
