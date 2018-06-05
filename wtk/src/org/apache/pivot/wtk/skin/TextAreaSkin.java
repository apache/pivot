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
            TextArea textArea = (TextArea) getComponent();
            int selectionStart = textArea.getSelectionStart();
            int selectionLength = textArea.getSelectionLength();
            int selectionEnd = selectionStart + selectionLength - 1;
            int index;

            switch (scrollDirection) {
                case UP:
                    // Get previous offset
                    index = getNextInsertionPoint(mouseX, selectionStart, scrollDirection);

                    if (index != -1) {
                        textArea.setSelection(index, selectionEnd - index + 1);
                        scrollCharacterToVisible(index + 1);
                    }

                    break;

                case DOWN:
                    // Get next offset
                    index = getNextInsertionPoint(mouseX, selectionEnd, scrollDirection);

                    if (index != -1) {
                        // If the next character is a paragraph terminator, increment the selection
                        if (index < textArea.getCharacterCount()
                            && textArea.getCharacterAt(index) == '\n') {
                            index++;
                        }

                        textArea.setSelection(selectionStart, index - selectionStart);
                        scrollCharacterToVisible(index - 1);
                    }

                    break;

                default:
                    break;
            }
        }
    }

    private int caretX = 0;
    private Rectangle caret = new Rectangle();
    private Area selection = null;

    private boolean caretOn = false;

    private int anchor = -1;
    private TextArea.ScrollDirection scrollDirection = null;
    private SelectDirection selectDirection = null;
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
    private boolean acceptsTab = false;

    private Dimensions averageCharacterSize;

    private ArrayList<TextAreaSkinParagraphView> paragraphViews = new ArrayList<>();

    private static final int SCROLL_RATE = 30;

    public TextAreaSkin() {
        Theme theme = currentTheme();
        font = theme.getFont();

        // TODO: find a way to set this in the theme defaults.json file
        // TODO: these conflict with the values set in TerraTextAreaSkin...
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

        // Remaining default styles set in the theme defaults.json file
    }

    @Override
    public void install(final Component component) {
        super.install(component);

        Theme theme = currentTheme();
        theme.setDefaultStyles(this);

        TextArea textArea = (TextArea) component;
        textArea.getTextAreaListeners().add(this);
        textArea.getTextAreaContentListeners().add(this);
        textArea.getTextAreaSelectionListeners().add(this);

        textArea.setCursor(Cursor.TEXT);
    }

    @Override
    public int getPreferredWidth(final int height) {
        int preferredWidth = 0;

        if (lineWidth <= 0) {
            for (TextAreaSkinParagraphView paragraphView : paragraphViews) {
                paragraphView.setBreakWidth(Integer.MAX_VALUE);
                preferredWidth = Math.max(preferredWidth, paragraphView.getWidth());
            }
        } else {
            preferredWidth = averageCharacterSize.width * lineWidth;
        }

        preferredWidth += margin.getWidth();

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(final int width) {
        int preferredHeight = 0;

        // Include margin in constraint
        int breakWidth = (wrapText && width != -1)
            ? Math.max(width - margin.getWidth(), 0) : Integer.MAX_VALUE;

        for (TextAreaSkinParagraphView paragraphView : paragraphViews) {
            paragraphView.setBreakWidth(breakWidth);
            preferredHeight += paragraphView.getHeight();
        }

        preferredHeight += margin.getHeight();

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

        preferredWidth += margin.getWidth();
        preferredHeight += margin.getHeight();

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @SuppressWarnings("unused")
    @Override
    public void layout() {
        TextArea textArea = (TextArea) getComponent();

        int width = getWidth();
        int breakWidth = (wrapText) ? Math.max(width - margin.getWidth(), 0)
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
    public int getBaseline(final int width, final int height) {
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();
        LineMetrics lm = font.getLineMetrics("", fontRenderContext);

        return Math.round(margin.top + lm.getAscent());
    }

    @Override
    public void paint(final Graphics2D graphics) {
        TextArea textArea = (TextArea) getComponent();
        int width = getWidth();
        int height = getHeight();

        // Draw the background
        if (backgroundColor != null) {
            graphics.setPaint(backgroundColor);
            graphics.fillRect(0, 0, width, height);
        }

        // Draw the caret/selection
        if (selection == null) {
            if (caretOn && textArea.isFocused()) {
                graphics.setColor(textArea.isEditable() ? color : inactiveColor);
                graphics.fill(caret);
            }
        } else {
            graphics.setColor(textArea.isFocused() && textArea.isEditable() ? selectionBackgroundColor
                : inactiveSelectionBackgroundColor);
            graphics.fill(selection);
        }

        // Draw the text
        graphics.setFont(font);
        graphics.translate(0, margin.top);

        int breakWidth = (wrapText) ? Math.max(width - margin.getWidth(), 0)
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
        return (backgroundColor != null && backgroundColor.getTransparency() == Transparency.OPAQUE);
    }

    @Override
    public int getInsertionPoint(final int x, final int y) {
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
                    if (y >= paragraphViewY && y < paragraphViewY + paragraphView.getHeight()) {
                        index = paragraphView.getInsertionPoint(x - paragraphView.getX(), y
                            - paragraphViewY)
                            + paragraphView.getParagraph().getOffset();
                        break;
                    }
                }
            }
        }

        return index;
    }

    @Override
    public int getNextInsertionPoint(final int x, final int from, final TextArea.ScrollDirection direction) {
        int index = -1;
        if (paragraphViews.getLength() > 0) {
            if (from == -1) {
                int i = (direction == TextArea.ScrollDirection.DOWN) ? 0
                    : paragraphViews.getLength() - 1;

                TextAreaSkinParagraphView paragraphView = paragraphViews.get(i);
                index = paragraphView.getNextInsertionPoint(x - paragraphView.getX(), -1, direction);

                if (index != -1) {
                    index += paragraphView.getParagraph().getOffset();
                }
            } else {
                TextArea textArea = (TextArea) getComponent();
                int i = textArea.getParagraphAt(from);

                TextAreaSkinParagraphView paragraphView = paragraphViews.get(i);
                index = paragraphView.getNextInsertionPoint(x - paragraphView.getX(), from
                    - paragraphView.getParagraph().getOffset(), direction);

                if (index == -1) {
                    // Move to the next or previous paragraph view
                    if (direction == TextArea.ScrollDirection.DOWN) {
                        paragraphView = (i < paragraphViews.getLength() - 1) ? paragraphViews.get(i + 1)
                            : null;
                    } else {
                        paragraphView = (i > 0) ? paragraphViews.get(i - 1) : null;
                    }

                    if (paragraphView != null) {
                        index = paragraphView.getNextInsertionPoint(x - paragraphView.getX(), -1,
                            direction);
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
    public int getRowAt(final int index) {
        int rowIndex = -1;

        if (paragraphViews.getLength() > 0) {
            TextArea textArea = (TextArea) getComponent();
            TextAreaSkinParagraphView paragraphView = paragraphViews.get(textArea.getParagraphAt(index));

            rowIndex = paragraphView.getRowAt(index - paragraphView.getParagraph().getOffset())
                + paragraphView.getRowOffset();
        }

        return rowIndex;
    }

    @Override
    public int getRowOffset(final int index) {
        int rowOffset = -1;

        if (paragraphViews.getLength() > 0) {
            TextArea textArea = (TextArea) getComponent();
            TextAreaSkinParagraphView paragraphView = paragraphViews.get(textArea.getParagraphAt(index));

            rowOffset = paragraphView.getRowOffset(index - paragraphView.getParagraph().getOffset())
                + paragraphView.getParagraph().getOffset();
        }

        return rowOffset;
    }

    @Override
    public int getRowLength(final int index) {
        int rowLength = -1;

        if (paragraphViews.getLength() > 0) {
            TextArea textArea = (TextArea) getComponent();
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
    public Bounds getCharacterBounds(final int index) {
        Bounds characterBounds = null;

        if (paragraphViews.getLength() > 0) {
            TextArea textArea = (TextArea) getComponent();
            TextAreaSkinParagraphView paragraphView = paragraphViews.get(textArea.getParagraphAt(index));
            characterBounds = paragraphView.getCharacterBounds(index
                - paragraphView.getParagraph().getOffset());

            characterBounds = new Bounds(characterBounds.x + paragraphView.getX(),
                characterBounds.y + paragraphView.getY(), characterBounds.width,
                characterBounds.height);
        }

        return characterBounds;
    }

    public Area getSelection() {
        return selection;
    }

    private void scrollCharacterToVisible(final int index) {
        Bounds characterBounds = getCharacterBounds(index);

        if (characterBounds != null) {
            TextArea textArea = (TextArea) getComponent();
            textArea.scrollAreaToVisible(characterBounds.x, characterBounds.y,
                characterBounds.width, characterBounds.height);
        }
    }

    /**
     * @return The font of the text.
     */
    public final Font getFont() {
        return font;
    }

    /**
     * Sets the font of the text.
     *
     * @param font The new font for the text.
     */
    public final void setFont(final Font font) {
        Utils.checkNull(font, "font");

        this.font = font;

        averageCharacterSize = GraphicsUtilities.getAverageCharacterSize(font);

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
    public final Color getColor() {
        return color;
    }

    /**
     * Sets the foreground color of the text.
     *
     * @param color The new foreground text color.
     */
    public final void setColor(final Color color) {
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

    public final Color getBackgroundColor() {
        return backgroundColor;
    }

    public final void setBackgroundColor(final Color backgroundColor) {
        // Null background is allowed here
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    public final void setBackgroundColor(final String backgroundColor) {
        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor, "backgroundColor"));
    }

    public final Color getInactiveColor() {
        return inactiveColor;
    }

    public final void setInactiveColor(final Color inactiveColor) {
        Utils.checkNull(inactiveColor, "inactiveColor");

        this.inactiveColor = inactiveColor;
        repaintComponent();
    }

    public final void setInactiveColor(final String inactiveColor) {
        setColor(GraphicsUtilities.decodeColor(inactiveColor, "inactiveColor"));
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
        setSelectionColor(GraphicsUtilities.decodeColor(selectionColor,  "selectionColor"));
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

    public final Color getInactiveSelectionColor() {
        return inactiveSelectionColor;
    }

    public final void setInactiveSelectionColor(final Color inactiveSelectionColor) {
        Utils.checkNull(inactiveSelectionColor, "inactiveSelectionColor");

        this.inactiveSelectionColor = inactiveSelectionColor;
        repaintComponent();
    }

    public final void setInactiveSelectionColor(final String inactiveSelectionColor) {
        setInactiveSelectionColor(GraphicsUtilities.decodeColor(inactiveSelectionColor,
            "inactiveSelectionColor"));
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
        setInactiveSelectionBackgroundColor(GraphicsUtilities.decodeColor(inactiveSelectionBackgroundColor,
            "inactiveSelectionBackgroundColor"));
    }

    /**
     * @return The amount of space between the edge of the TextArea and its text.
     */
    public final Insets getMargin() {
        return margin;
    }

    /**
     * Sets the amount of space between the edge of the TextArea and its text.
     *
     * @param margin The individual margin values for all edges.
     */
    public final void setMargin(final Insets margin) {
        Utils.checkNull(margin, "margin");

        this.margin = margin;
        invalidateComponent();
    }

    /**
     * Sets the amount of space between the edge of the TextArea and its text.
     *
     * @param margin A dictionary with keys in the set {top, left, bottom, right}.
     */
    public final void setMargin(final Dictionary<String, ?> margin) {
        setMargin(new Insets(margin));
    }

    /**
     * Sets the amount of space between the edge of the TextArea and its text.
     *
     * @param margin A sequence with values in the order [top, left, bottom, right].
     */
    public final void setMargin(final Sequence<?> margin) {
        setMargin(new Insets(margin));
    }

    /**
     * Sets the amount of space between the edge of the TextArea and its text.
     *
     * @param margin The single value to use for all the margins.
     */
    public final void setMargin(final int margin) {
        setMargin(new Insets(margin));
    }

    /**
     * Sets the amount of space between the edge of the TextArea and its text.
     *
     * @param margin The single value to use for all the margins.
     */
    public final void setMargin(final Number margin) {
        setMargin(new Insets(margin));
    }

    /**
     * Sets the amount of space between the edge of the TextArea and its text.
     *
     * @param margin A string containing an integer or a JSON dictionary or list with
     * keys top, left, bottom, and/or right.
     */
    public final void setMargin(final String margin) {
        setMargin(Insets.decode(margin));
    }

    public final boolean getWrapText() {
        return wrapText;
    }

    public final void setWrapText(final boolean wrapText) {
        this.wrapText = wrapText;
        invalidateComponent();
    }

    public final boolean getAcceptsEnter() {
        return acceptsEnter;
    }

    public final void setAcceptsEnter(final boolean acceptsEnter) {
        this.acceptsEnter = acceptsEnter;
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
    public final boolean getAcceptsTab() {
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
    public final void setAcceptsTab(final boolean acceptsTab) {
        this.acceptsTab = acceptsTab;
    }

    @Override
    public final int getTabWidth() {
        return tabWidth;
    }

    public final void setTabWidth(final int tabWidth) {
        Utils.checkNonNegative(tabWidth, "tabWidth");

        this.tabWidth = tabWidth;
    }

    public final int getLineWidth() {
        return lineWidth;
    }

    public final void setLineWidth(final int lineWidth) {
        if (this.lineWidth != lineWidth) {
            this.lineWidth = lineWidth;

            int missingGlyphCode = font.getMissingGlyphCode();
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();

            GlyphVector missingGlyphVector = font.createGlyphVector(fontRenderContext,
                new int[] {missingGlyphCode});
            Rectangle2D textBounds = missingGlyphVector.getLogicalBounds();

            Rectangle2D maxCharBounds = font.getMaxCharBounds(fontRenderContext);
            averageCharacterSize = new Dimensions((int) Math.ceil(textBounds.getWidth()),
                (int) Math.ceil(maxCharBounds.getHeight()));

            invalidateComponent();
        }
    }

    @Override
    public boolean mouseMove(final Component component, final int x, final int y) {
        boolean consumed = super.mouseMove(component, x, y);

        if (Mouse.getCapturer() == component) {
            TextArea textArea = (TextArea) getComponent();

            Bounds visibleArea = textArea.getVisibleArea();
            visibleArea = new Bounds(visibleArea.x, visibleArea.y, visibleArea.width,
                visibleArea.height);

            // if it's inside the visible area, stop the scroll timer
            if (y >= visibleArea.y && y < visibleArea.y + visibleArea.height) {
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

                    // Run the callback once now to scroll the selection
                    // immediately, then scroll repeatedly
                    scheduledScrollSelectionCallback = ApplicationContext.runAndScheduleRecurringCallback(
                        scrollSelectionCallback, SCROLL_RATE);
                }
            }

            int index = getInsertionPoint(x, y);

            if (index != -1) {
                // Select the range
                if (index > anchor) {
                    textArea.setSelection(anchor, index - anchor);
                    selectDirection = SelectDirection.DOWN;
                } else {
                    textArea.setSelection(index, anchor - index);
                    selectDirection = SelectDirection.UP;
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

        TextArea textArea = (TextArea) component;

        if (button == Mouse.Button.LEFT) {
            anchor = getInsertionPoint(x, y);

            if (anchor != -1) {
                if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                    // Select the range
                    int selectionStart = textArea.getSelectionStart();

                    if (anchor > selectionStart) {
                        textArea.setSelection(selectionStart, anchor - selectionStart);
                        selectDirection = SelectDirection.RIGHT;
                    } else {
                        textArea.setSelection(anchor, selectionStart - anchor);
                        selectDirection = SelectDirection.LEFT;
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

        scrollDirection = null;
        mouseX = -1;

        return consumed;
    }

    @Override
    public boolean mouseClick(final Component component, final Mouse.Button button,
            final int x, final int y, final int count) {
        boolean consumed = super.mouseClick(component, button, x, y, count);

        TextArea textArea = (TextArea) component;

        if (button == Mouse.Button.LEFT) {
            int index = getInsertionPoint(x, y);
            if (index != -1) {
                if (count == 2) {
                    int offset = getRowOffset(index);
                    CharSpan charSpan = CharUtils.selectWord(textArea.getRowCharacters(index), index - offset);
                    if (charSpan != null) {
                        textArea.setSelection(charSpan.offset(offset));
                    }
                } else if (count == 3) {
                    textArea.setSelection(textArea.getRowOffset(index),
                        textArea.getRowLength(index));
                }
            }
        }
        return consumed;
    }

    @Override
    public boolean keyTyped(final Component component, final char character) {
        boolean consumed = super.keyTyped(component, character);

        if (paragraphViews.getLength() > 0) {
            TextArea textArea = (TextArea) getComponent();

            if (textArea.isEditable()) {
                // Ignore characters in the control range and the ASCII delete
                // character as well as meta key presses
                if (character > 0x1F && character != 0x7F
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
    public boolean keyPressed(final Component component, final int keyCode, final Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (paragraphViews.getLength() > 0) {
            TextArea textArea = (TextArea) getComponent();
            boolean commandPressed = Keyboard.isPressed(Platform.getCommandModifier());
            boolean wordNavPressed = Keyboard.isPressed(Platform.getWordNavigationModifier());
            boolean shiftPressed = Keyboard.isPressed(Keyboard.Modifier.SHIFT);
            boolean ctrlPressed = Keyboard.isPressed(Keyboard.Modifier.CTRL);
            boolean metaPressed = Keyboard.isPressed(Keyboard.Modifier.META);
            boolean isEditable = textArea.isEditable();

            int selectionStart = textArea.getSelectionStart();
            int selectionLength = textArea.getSelectionLength();
            int count = textArea.getCharacterCount();

            if (keyCode == Keyboard.KeyCode.ENTER && acceptsEnter && isEditable
                && Keyboard.getModifiers() == 0) {
                textArea.removeText(selectionStart, selectionLength);
                textArea.insertText("\n", selectionStart);

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.DELETE && isEditable) {
                if (selectionStart < count) {
                    textArea.removeText(selectionStart, Math.max(selectionLength, 1));
                    anchor = -1;
                    consumed = true;
                }
            } else if (keyCode == Keyboard.KeyCode.BACKSPACE && isEditable) {
                if (selectionLength == 0 && selectionStart > 0) {
                    textArea.removeText(selectionStart - 1, 1);
                    consumed = true;
                } else {
                    textArea.removeText(selectionStart, selectionLength);
                    consumed = true;
                }
                anchor = -1;
            } else if (keyCode == Keyboard.KeyCode.TAB && (acceptsTab != ctrlPressed) && isEditable) {
                int rowOffset = textArea.getRowOffset(selectionStart);
                int linePos = selectionStart - rowOffset;
                StringBuilder tabBuilder = new StringBuilder(tabWidth);
                for (int i = 0; i < tabWidth - (linePos % tabWidth); i++) {
                    tabBuilder.append(" ");
                }

                if (count - selectionLength + tabBuilder.length() > textArea.getMaximumLength()) {
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    textArea.removeText(selectionStart, selectionLength);
                    textArea.insertText(tabBuilder, selectionStart);
                }

                showCaret(true);

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.HOME
                || (keyCode == Keyboard.KeyCode.LEFT && metaPressed)) {
                int start;

                if (commandPressed) {
                    // Find the very beginning of the text
                    start = 0;
                } else {
                    // Find the start of the current line
                    start = getRowOffset(selectionStart);
                }

                if (shiftPressed) {
                    // Select from the beginning of the text to the current pivot position
                    if (selectDirection == SelectDirection.UP || selectDirection == SelectDirection.LEFT) {
                        selectionLength += selectionStart - start;
                    } else {
                        selectionLength = selectionStart - start;
                    }
                    selectDirection = SelectDirection.LEFT;
                } else {
                    selectionLength = 0;
                    selectDirection = null;
                }

                if (start >= 0) {
                    textArea.setSelection(start, selectionLength);
                    scrollCharacterToVisible(start);

                    caretX = caret.x;

                    consumed = true;
                }
            } else if (keyCode == Keyboard.KeyCode.END
                || (keyCode == Keyboard.KeyCode.RIGHT && metaPressed)) {
                int end;
                int index = selectionStart + selectionLength;

                if (commandPressed) {
                    // Find the very end of the text
                    end = count;
                } else {
                    // Find the end of the current line
                    int rowOffset = getRowOffset(index);
                    int rowLength = getRowLength(index);
                    end = rowOffset + rowLength;
                }

                if (shiftPressed) {
                    // Select from current pivot position to the end of the text
                    if (selectDirection == SelectDirection.UP || selectDirection == SelectDirection.LEFT) {
                        selectionStart += selectionLength;
                    }
                    selectionLength = end - selectionStart;
                    selectDirection = SelectDirection.RIGHT;
                } else {
                    selectionStart = end;
                    if (selectionStart < count
                        && textArea.getCharacterAt(selectionStart) != '\n') {
                        selectionStart--;
                    }

                    selectionLength = 0;
                    selectDirection = null;
                }

                if (selectionStart + selectionLength <= count) {
                    textArea.setSelection(selectionStart, selectionLength);
                    scrollCharacterToVisible(selectionStart + selectionLength);

                    caretX = caret.x;
                    if (selection != null) {
                        caretX += selection.getBounds2D().getWidth();
                    }

                    consumed = true;
                }
            } else if (keyCode == Keyboard.KeyCode.LEFT) {
                if (wordNavPressed) {
                    int wordStart = (selectDirection == SelectDirection.RIGHT)
                        ? selectionStart + selectionLength : selectionStart;
                    // Move the caret to the start of the next word to the left
                    if (wordStart > 0) {
                        int index = CharUtils.findPriorWord(textArea.getCharacters(), wordStart);

                        if (shiftPressed) {
                            // TODO: depending on prior selectDirection, may just reduce previous right selection
                            selectionLength += selectionStart - index;
                            selectDirection = SelectDirection.LEFT;
                        } else {
                            selectionLength = 0;
                            selectDirection = null;
                        }

                        selectionStart = index;
                    }
                } else if (shiftPressed) {
                    if (anchor != -1) {
                        if (selectionStart < anchor) {
                            if (selectionStart > 0) {
                                selectionStart--;
                                selectionLength++;
                            }
                            selectDirection = SelectDirection.LEFT;
                        } else {
                            if (selectionLength > 0) {
                                selectionLength--;
                            } else {
                                selectionStart--;
                                selectionLength++;
                                selectDirection = SelectDirection.LEFT;
                            }
                        }
                    } else {
                        // Add the previous character to the selection
                        anchor = selectionStart;
                        if (selectionStart > 0) {
                            selectionStart--;
                            selectionLength++;
                        }
                        selectDirection = SelectDirection.LEFT;
                    }
                } else {
                    // Move the caret back by one character
                    if (selectionLength == 0 && selectionStart > 0) {
                        selectionStart--;
                    }

                    // Clear the selection
                    anchor = -1;
                    selectionLength = 0;
                    selectDirection = null;
                }

                if (selectionStart >= 0) {
                    textArea.setSelection(selectionStart, selectionLength);
                    scrollCharacterToVisible(selectionStart);

                    caretX = caret.x;

                    consumed = true;
                }
            } else if (keyCode == Keyboard.KeyCode.RIGHT) {
                if (wordNavPressed) {
                    int wordStart = (selectDirection == SelectDirection.LEFT)
                        ? selectionStart : selectionStart + selectionLength;
                    // Move the caret to the start of the next word to the right
                    if (wordStart < count) {
                        int index = CharUtils.findNextWord(textArea.getCharacters(), wordStart);

                        if (shiftPressed) {
                            // TODO: depending on prior selectDirection, may just reduce previous left selection
                            selectionLength = index - selectionStart;
                        } else {
                            selectionStart = index;
                            selectionLength = 0;
                        }
                    }
                } else if (shiftPressed) {
                    if (anchor != -1) {
                        if (selectionStart < anchor) {
                            selectionStart++;
                            selectionLength--;
                        } else {
                            selectionLength++;
                            selectDirection = SelectDirection.RIGHT;
                        }
                    } else {
                        // Add the next character to the selection
                        anchor = selectionStart;
                        selectionLength++;
                        selectDirection = SelectDirection.RIGHT;
                    }
                } else {
                    // Move the caret forward by one character
                    if (selectionLength == 0) {
                        selectionStart++;
                    } else {
                        selectionStart += selectionLength;
                    }

                    // Clear the selection
                    anchor = -1;
                    selectionLength = 0;
                    selectDirection = null;
                }

                if (selectionStart + selectionLength <= count) {
                    textArea.setSelection(selectionStart, selectionLength);
                    scrollCharacterToVisible(selectionStart + selectionLength);

                    caretX = caret.x;
                    if (selection != null) {
                        caretX += selection.getBounds2D().getWidth();
                    }

                    consumed = true;
                }
            } else if (keyCode == Keyboard.KeyCode.UP) {
                int index = -1;
                if (shiftPressed) {
                    if (anchor == -1) {
                        anchor = selectionStart;
                        index = getNextInsertionPoint(caretX, selectionStart,
                            TextArea.ScrollDirection.UP);
                        if (index != -1) {
                            selectionLength = selectionStart - index;
                        }
                    } else {
                        if (selectionStart < anchor) {
                            // continue upwards
                            index = getNextInsertionPoint(caretX, selectionStart,
                                TextArea.ScrollDirection.UP);
                            if (index != -1) {
                                selectionLength = selectionStart + selectionLength - index;
                            }
                        } else {
                            // reduce downward size
                            Bounds trailingSelectionBounds = getCharacterBounds(selectionStart
                                + selectionLength - 1);
                            int x = trailingSelectionBounds.x + trailingSelectionBounds.width;
                            index = getNextInsertionPoint(x, selectionStart + selectionLength - 1,
                                TextArea.ScrollDirection.UP);
                            if (index != -1) {
                                if (index < anchor) {
                                    selectionLength = anchor - index;
                                } else {
                                    selectionLength = index - selectionStart;
                                    index = selectionStart;
                                }
                            }
                        }
                    }
                } else {
                    index = getNextInsertionPoint(caretX, selectionStart,
                        TextArea.ScrollDirection.UP);
                    if (index != -1) {
                        selectionLength = 0;
                    }
                    anchor = -1;
                }

                if (index != -1) {
                    textArea.setSelection(index, selectionLength);
                    scrollCharacterToVisible(index);
                    caretX = caret.x;
                }

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.DOWN) {
                if (shiftPressed) {
                    int from;
                    int x;
                    int index;

                    if (anchor == -1) {
                        anchor = selectionStart;
                        index = getNextInsertionPoint(caretX, selectionStart,
                            TextArea.ScrollDirection.DOWN);
                        if (index != -1) {
                            selectionLength = index - selectionStart;
                        }
                    } else {
                        if (selectionStart < anchor) {
                            // Reducing upward size
                            // Get next insertion point from leading selection character
                            from = selectionStart;
                            x = caretX;

                            index = getNextInsertionPoint(x, from, TextArea.ScrollDirection.DOWN);

                            if (index != -1) {
                                if (index < anchor) {
                                    selectionStart = index;
                                    selectionLength = anchor - index;
                                } else {
                                    selectionStart = anchor;
                                    selectionLength = index - anchor;
                                }

                                textArea.setSelection(selectionStart, selectionLength);
                                scrollCharacterToVisible(selectionStart);
                            }
                        } else {
                            // Increasing downward size
                            // Get next insertion point from right edge of trailing selection
                            // character
                            from = selectionStart + selectionLength - 1;

                            Bounds trailingSelectionBounds = getCharacterBounds(from);
                            x = trailingSelectionBounds.x + trailingSelectionBounds.width;

                            index = getNextInsertionPoint(x, from, TextArea.ScrollDirection.DOWN);

                            if (index != -1) {
                                // If the next character is a paragraph terminator and is
                                // not the final terminator character, increment
                                // the selection
                                if (index < count - 1
                                    && textArea.getCharacterAt(index) == '\n') {
                                    index++;
                                }

                                textArea.setSelection(selectionStart, index - selectionStart);
                                scrollCharacterToVisible(index);
                            }
                        }
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
                        caretX = caret.x;
                    }
                    anchor = -1;
                }

                consumed = true;
            } else if (commandPressed) {
                if (keyCode == Keyboard.KeyCode.A) {
                    textArea.setSelection(0, count);
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.X && isEditable) {
                    textArea.cut();
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.C) {
                    textArea.copy();
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.V && isEditable) {
                    textArea.paste();
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.Z && isEditable) {
                    if (!shiftPressed) {
                        textArea.undo();
                    }
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.TAB) {
                    // Only here if acceptsTab is false
                    consumed = super.keyPressed(component, keyCode, keyLocation);
                }
            } else if (keyCode == Keyboard.KeyCode.INSERT) {
                if (shiftPressed && isEditable) {
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
    public void enabledChanged(final Component component) {
        super.enabledChanged(component);
        repaintComponent();
    }

    @Override
    public void focusedChanged(final Component component, final Component obverseComponent) {
        super.focusedChanged(component, obverseComponent);

        TextArea textArea = (TextArea) getComponent();
        if (textArea.isFocused() && textArea.getSelectionLength() == 0) {
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
    public void maximumLengthChanged(final TextArea textArea, final int previousMaximumLength) {
        // No-op
    }

    @Override
    public void editableChanged(final TextArea textArea) {
        // No-op
    }

    @Override
    public void paragraphInserted(final TextArea textArea, final int index) {
        // Create paragraph view and add as paragraph listener
        TextArea.Paragraph paragraph = textArea.getParagraphs().get(index);
        TextAreaSkinParagraphView paragraphView = new TextAreaSkinParagraphView(this, paragraph);
        paragraph.getParagraphListeners().add(paragraphView);

        // Insert view
        paragraphViews.insert(paragraphView, index);

        invalidateComponent();
    }

    @Override
    public void paragraphsRemoved(final TextArea textArea, final int index,
        final Sequence<TextArea.Paragraph> removed) {
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
    public void textChanged(final TextArea textArea) {
        // No-op
    }

    @Override
    public void selectionChanged(final TextArea textArea, final int previousSelectionStart,
        final int previousSelectionLength) {
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
        TextArea textArea = (TextArea) getComponent();

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
                            + leadingSelectionBounds.height, width - margin.getWidth(),
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
            // Run the callback once now to show the cursor immediately, then blink at the given rate
            scheduledBlinkCaretCallback = ApplicationContext.runAndScheduleRecurringCallback(
                blinkCaretCallback, Platform.getCursorBlinkRate());
        } else {
            scheduledBlinkCaretCallback = null;
        }
    }
}
