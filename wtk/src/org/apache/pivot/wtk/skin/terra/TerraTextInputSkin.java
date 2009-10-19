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
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Cursor;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Direction;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputCharacterListener;
import org.apache.pivot.wtk.TextInputListener;
import org.apache.pivot.wtk.TextInputSelectionListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.skin.ComponentSkin;
import org.apache.pivot.wtk.text.TextNode;
import org.apache.pivot.wtk.text.validation.Validator;

/**
 * Text input skin.
 */
public class TerraTextInputSkin extends ComponentSkin
    implements TextInputListener, TextInputCharacterListener, TextInputSelectionListener {
    private class BlinkCursorCallback implements Runnable {
        @Override
        public void run() {
            caretOn = !caretOn;

            java.awt.Rectangle caretBounds = caretShapes[0].getBounds();
            LineMetrics lm = font.getLineMetrics("", FONT_RENDER_CONTEXT);

            int ascent = Math.round(lm.getAscent());
            caretBounds.x += (padding.left - scrollLeft + 1);
            caretBounds.y += (padding.top + ascent + 1);

            if (caretBounds.width == 0) {
                caretBounds.width++;
            }

            TextInput textInput = (TextInput)getComponent();
            textInput.repaint(caretBounds.x, caretBounds.y,
                caretBounds.width, caretBounds.height, true);
        }
    }

    private class ScrollSelectionCallback implements Runnable {
        private int x = 0;

        @Override
        public void run() {
            TextInput textInput = (TextInput)getComponent();
            TextNode textNode = textInput.getTextNode();

            int selectionStart = textInput.getSelectionStart();
            int selectionLength = textInput.getSelectionLength();

            if (x < 0) {
                // Add the previous character to the selection
                if (selectionStart > 0) {
                    selectionStart--;
                    selectionLength++;
                }
            } else {
                // Add the next character to the selection
                if (selectionStart + selectionLength < textNode.getCharacterCount()) {
                    selectionLength++;
                }
            }

            textInput.setSelection(selectionStart, selectionLength);
        }
    }


    private boolean caretOn = true;
    private Shape[] caretShapes = null;
    private Shape logicalHighlightShape = null;

    private int scrollLeft = 0;

    private BlinkCursorCallback blinkCursorCallback = new BlinkCursorCallback();
    private ApplicationContext.ScheduledCallback scheduledBlinkCursorCallback = null;

    private ScrollSelectionCallback scrollSelectionCallback = new ScrollSelectionCallback();
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
    private Insets padding;
    private boolean strictValidation;

    private Color selectionColor;
    private Color selectionBackgroundColor;
    private Color inactiveSelectionColor;
    private Color inactiveSelectionBackgroundColor;

    // Derived colors
    private Color bevelColor;
    private Color disabledBevelColor;
    private Color invalidBevelColor;

    private static final int SCROLL_RATE = 50;

    private static final FontRenderContext FONT_RENDER_CONTEXT =
        new FontRenderContext(null, true, false);

    public TerraTextInputSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        font = theme.getFont();
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
        strictValidation = false;

        selectionColor = theme.getColor(4);
        selectionBackgroundColor = theme.getColor(19);
        inactiveSelectionColor = theme.getColor(1);
        inactiveSelectionBackgroundColor = theme.getColor(9);

        // Set the derived colors
        bevelColor = TerraTheme.darken(backgroundColor);
        disabledBevelColor = disabledBackgroundColor;
        invalidBevelColor = TerraTheme.darken(invalidBackgroundColor);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        TextInput textInput = (TextInput)component;
        textInput.getTextInputListeners().add(this);
        textInput.getTextInputCharacterListeners().add(this);
        textInput.getTextInputSelectionListeners().add(this);

        textInput.setCursor(Cursor.TEXT);

        selectionChanged(textInput, 0, 0);
    }

    @Override
    public int getPreferredWidth(int height) {
        TextInput textInput = (TextInput)getComponent();
        int textSize = textInput.getTextSize();

        // TODO Use the missing character glyph bounds (see Font#getMissingGlyphCode())
        // rather than calculating an average width
        String testString = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz";

        Rectangle2D testStringBounds = font.getStringBounds(testString, FONT_RENDER_CONTEXT);
        int averageCharWidth = (int)Math.round((testStringBounds.getWidth() / testString.length()));

        return textSize * averageCharWidth + (padding.left + padding.right) + 2;
    }

    @Override
    public int getPreferredHeight(int width) {
        Rectangle2D maxCharBounds = font.getMaxCharBounds(FONT_RENDER_CONTEXT);
        int maxCharHeight = (int)Math.ceil(maxCharBounds.getHeight());

        return maxCharHeight + (padding.top + padding.bottom) + 2;
    }

    @Override
    public Dimensions getPreferredSize() {
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    @Override
    public int getBaseline(int width) {
        LineMetrics lm = font.getLineMetrics("", FONT_RENDER_CONTEXT);
        return (int)Math.ceil(lm.getAscent() - 2) + (padding.top + 1);
    }

    @Override
    public void layout() {
        // No-op
    }

    @Override
    public void paint(Graphics2D graphics) {
        TextInput textInput = (TextInput)getComponent();

        int width = getWidth();
        int height = getHeight();

        Color backgroundColor;
        Color borderColor;
        Color bevelColor;

        if (textInput.isEnabled()) {
            if (textInput.isTextValid()) {
                backgroundColor = this.backgroundColor;
                bevelColor = this.bevelColor;
            } else {
                backgroundColor = invalidBackgroundColor;
                bevelColor = invalidBevelColor;
            }

            borderColor = this.borderColor;
        } else {
            backgroundColor = disabledBackgroundColor;
            borderColor = disabledBorderColor;
            bevelColor = disabledBevelColor;
        }

        graphics.setStroke(new BasicStroke());

        // Paint the background
        graphics.setPaint(backgroundColor);
        graphics.fillRect(0, 0, width, height);

        if (debugBaseline) {
            drawBaselineDebug(graphics);
        }

        // Paint the bevel
        graphics.setPaint(bevelColor);
        GraphicsUtilities.drawLine(graphics, 1, 1, width - 2, Orientation.HORIZONTAL);

        // Paint the border
        graphics.setPaint(borderColor);
        GraphicsUtilities.drawRect(graphics, 0, 0, width, height);

        // Paint the content
        String text = getText();

        boolean prompt = false;
        if (text.length() == 0
            && !textInput.isFocused()) {
            text = textInput.getPrompt();

            if (text == null) {
                text = "";
            } else {
                prompt = true;
            }
        }

        boolean textValid = textInput.isTextValid();

        LineMetrics lm = font.getLineMetrics("", FONT_RENDER_CONTEXT);
        int ascent = Math.round(lm.getAscent());

        graphics.translate(padding.left - scrollLeft + 1, padding.top + ascent + 1);

        if (text.length() > 0) {
            // Paint the text
            if (FONT_RENDER_CONTEXT.isAntiAliased()) {
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    Platform.getTextAntialiasingHint());
            }

            if (FONT_RENDER_CONTEXT.usesFractionalMetrics()) {
                graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            }

            Color color;
            if (textInput.isEnabled()) {
                if (prompt) {
                    color = promptColor;
                } else if (!textValid) {
                    color = invalidColor;
                } else {
                    color = this.color;
                }
            } else {
               color = disabledColor;
            }

            graphics.setFont(font);
            graphics.setPaint(color);
            graphics.drawString(text, 0, 0);

            if (textInput.getSelectionLength() > 0) {
                // Paint the selection
                Graphics2D selectionGraphics = (Graphics2D)graphics.create();
                selectionGraphics.clip(logicalHighlightShape.getBounds());

                Color selectionColor;
                Color selectionBackgroundColor;

                if (textInput.isFocused()) {
                    selectionColor = this.selectionColor;
                    selectionBackgroundColor = this.selectionBackgroundColor;
                } else {
                    selectionColor = inactiveSelectionColor;
                    selectionBackgroundColor = inactiveSelectionBackgroundColor;
                }

                selectionGraphics.setPaint(selectionBackgroundColor);
                selectionGraphics.fill(logicalHighlightShape);

                selectionGraphics.setPaint(selectionColor);
                selectionGraphics.drawString(text, 0, 0);

                selectionGraphics.dispose();
            }
        }

        if (textInput.getSelectionLength() == 0
            && textInput.isFocused()
            && caretOn) {
            Color color;
            if (!textValid) {
                color = invalidColor;
            } else {
                color = this.color;
            }

            graphics.setPaint(color);
            graphics.draw(caretShapes[0]);
        }
    }

    protected String getText() {
        TextInput textInput = (TextInput)getComponent();

        // TODO Use the internal character iterator instead of getting a copy
        // of the string
        String text = textInput.getText();

        if (textInput.isPassword()) {
            int n = text.length();
            StringBuilder passwordTextBuilder = new StringBuilder(n);
            for (int i = 0; i < n; i++) {
                passwordTextBuilder.append("*");
            }

            text = passwordTextBuilder.toString();
        }

        return text;
    }

    protected int getInsertionIndex(String text, int x) {
        TextLayout textLayout = new TextLayout(text, font, FONT_RENDER_CONTEXT);
        TextHitInfo textHitInfo = textLayout.hitTestChar(x + scrollLeft - padding.left - 1, 0);
        int index = textHitInfo.getInsertionIndex();

        return index;
    }

    public void showCaret(boolean show) {
        if (show) {
            if (scheduledBlinkCursorCallback == null) {
                scheduledBlinkCursorCallback =
                    ApplicationContext.scheduleRecurringCallback(blinkCursorCallback,
                        Platform.getCursorBlinkRate());

                // Run the callback once now to show the cursor immediately
                blinkCursorCallback.run();
            }
        } else {
            if (scheduledBlinkCursorCallback != null) {
                scheduledBlinkCursorCallback.cancel();
                scheduledBlinkCursorCallback = null;
            }
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

    public final void setColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setColor(theme.getColor(color));
    }

    public Color getPromptColor() {
        return promptColor;
    }

    public void setPromptColor(Color promptColor) {
        if (promptColor == null) {
            throw new IllegalArgumentException("promptColor is null.");
        }

        this.promptColor = promptColor;
        repaintComponent();
    }

    public final void setPromptColor(String promptColor) {
        if (promptColor == null) {
            throw new IllegalArgumentException("promptColor is null.");
        }

        setPromptColor(GraphicsUtilities.decodeColor(promptColor));
    }

    public final void setPromptColor(int promptColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setPromptColor(theme.getColor(promptColor));
    }

    public Color getDisabledColor() {
        return disabledColor;
    }

    public void setDisabledColor(Color disabledColor) {
        if (disabledColor == null) {
            throw new IllegalArgumentException("disabledColor is null.");
        }

        this.disabledColor = disabledColor;
        repaintComponent();
    }

    public final void setDisabledColor(String disabledColor) {
        if (disabledColor == null) {
            throw new IllegalArgumentException("disabledColor is null.");
        }

        setDisabledColor(GraphicsUtilities.decodeColor(disabledColor));
    }

    public final void setDisabledColor(int disabledColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setDisabledColor(theme.getColor(disabledColor));
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        this.backgroundColor = backgroundColor;
        bevelColor = TerraTheme.darken(backgroundColor);
        repaintComponent();
    }

    public final void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor));
    }

    public final void setBackgroundColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(color));
    }

    public Color getInvalidColor() {
        return invalidColor;
    }

    public void setInvalidColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.invalidColor = color;
        repaintComponent();
    }

    public final void setInvalidColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setInvalidColor(GraphicsUtilities.decodeColor(color));
    }

    public final void setInvalidColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setInvalidColor(theme.getColor(color));
    }

    public Color getInvalidBackgroundColor() {
        return invalidBackgroundColor;
    }

    public void setInvalidBackgroundColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.invalidBackgroundColor = color;
        invalidBevelColor = TerraTheme.darken(color);
        repaintComponent();
    }

    public final void setInvalidBackgroundColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("invalidBackgroundColor is null.");
        }

        setInvalidBackgroundColor(GraphicsUtilities.decodeColor(color));
    }

    public final void setInvalidBackgroundColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setInvalidBackgroundColor(theme.getColor(color));
    }

    public Color getDisabledBackgroundColor() {
        return disabledBackgroundColor;
    }

    public void setDisabledBackgroundColor(Color disabledBackgroundColor) {
        if (disabledBackgroundColor == null) {
            throw new IllegalArgumentException("disabledBackgroundColor is null.");
        }

        this.disabledBackgroundColor = disabledBackgroundColor;
        disabledBevelColor = disabledBackgroundColor;
        repaintComponent();
    }

    public final void setDisabledBackgroundColor(String disabledBackgroundColor) {
        if (disabledBackgroundColor == null) {
            throw new IllegalArgumentException("disabledBackgroundColor is null.");
        }

        setDisabledBackgroundColor(GraphicsUtilities.decodeColor(disabledBackgroundColor));
    }

    public final void setDisabledBackgroundColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setDisabledBackgroundColor(theme.getColor(color));
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        this.borderColor = borderColor;
        repaintComponent();
    }

    public final void setBorderColor(String borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        setBorderColor(GraphicsUtilities.decodeColor(borderColor));
    }

    public final void setBorderColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBorderColor(theme.getColor(color));
    }

    public Color getDisabledBorderColor() {
        return disabledBorderColor;
    }

    public void setDisabledBorderColor(Color disabledBorderColor) {
        if (disabledBorderColor == null) {
            throw new IllegalArgumentException("disabledBorderColor is null.");
        }

        this.disabledBorderColor = disabledBorderColor;
        repaintComponent();
    }

    public final void setDisabledBorderColor(String disabledBorderColor) {
        if (disabledBorderColor == null) {
            throw new IllegalArgumentException("disabledBorderColor is null.");
        }

        setDisabledBorderColor(GraphicsUtilities.decodeColor(disabledBorderColor));
    }

    public final void setDisabledBorderColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setDisabledBorderColor(theme.getColor(color));
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

    public final void setSelectionColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setSelectionColor(theme.getColor(color));
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

    public final void setSelectionBackgroundColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setSelectionBackgroundColor(theme.getColor(color));
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

    public final void setInactiveSelectionColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setInactiveSelectionColor(theme.getColor(color));
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

    public final void setInactiveSelectionBackgroundColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setInactiveSelectionBackgroundColor(theme.getColor(color));
    }

    public Insets getPadding() {
        return padding;
    }

    public void setPadding(Insets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        this.padding = padding;
        invalidateComponent();
    }

    public final void setPadding(Dictionary<String, ?> padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(new Insets(padding));
    }

    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(padding.intValue());
    }

    public final void setPadding(String padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(Insets.decode(padding));
    }

    public boolean getStrictValidation() {
        return strictValidation;
    }

    public void setStrictValidation(boolean strictValidation) {
        this.strictValidation = strictValidation;
    }

    @Override
    public boolean mouseMove(Component component, int x, int y) {
        boolean consumed = super.mouseMove(component, x, y);

        if (Mouse.getCapturer() == component) {
            String text = getText();

            if (text.length() > 0) {
                TextInput textInput = (TextInput)getComponent();

                if (x >= 0
                    && x < textInput.getWidth()) {
                    // Stop the scroll selection timer
                    if (scheduledScrollSelectionCallback != null) {
                        scheduledScrollSelectionCallback.cancel();
                        scheduledScrollSelectionCallback = null;
                    }

                    // Get the current selection
                    int selectionStart = textInput.getSelectionStart();
                    int selectionLength = textInput.getSelectionLength();

                    // Get the insertion index
                    int index = getInsertionIndex(text, x);

                    if (index < selectionStart) {
                        selectionLength += (selectionStart - index);
                        selectionStart = index;
                    } else {
                        if (index > selectionStart + selectionLength) {
                            selectionLength = index - selectionStart;
                        }
                    }

                    textInput.setSelection(selectionStart, selectionLength);
                } else {
                    scrollSelectionCallback.x = x;

                    if (scheduledScrollSelectionCallback == null) {
                        scheduledScrollSelectionCallback =
                            ApplicationContext.scheduleRecurringCallback(scrollSelectionCallback,
                                SCROLL_RATE);

                        // Run the callback once now to scroll the selection immediately
                        scrollSelectionCallback.run();
                    }
                }
            }
        } else {
            if (Mouse.isPressed(Mouse.Button.LEFT)
                && Mouse.getCapturer() == null) {
                // Capture the mouse so we can select text
                Mouse.capture(component);
            }
        }

        return consumed;
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        if (button == Mouse.Button.LEFT) {
            // Move the caret to the insertion point
            TextInput textInput = (TextInput)getComponent();
            String text = getText();

            if (text.length() > 0) {
                int index = getInsertionIndex(text, x);
                textInput.setSelection(index, 0);
            }

            // Set focus to the text input
            textInput.requestFocus();
        }

        return super.mouseDown(component, button, x, y);
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

        return consumed;
    }

    @Override
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        if (button == Mouse.Button.LEFT
            && count > 1) {
            TextInput textInput = (TextInput)getComponent();
            TextNode textNode = textInput.getTextNode();
            textInput.setSelection(0, textNode.getCharacterCount());
        }

        return super.mouseClick(component, button, x, y, count);
    }

    @Override
    public boolean keyTyped(Component component, char character) {
        boolean consumed = super.keyTyped(component, character);

        // Ignore characters in the control range and the ASCII delete
        // character
        if (character > 0x1F
            && character != 0x7F) {
            TextInput textInput = (TextInput)getComponent();
            TextNode textNode = textInput.getTextNode();

            if (textNode.getCharacterCount() < textInput.getMaximumLength()) {
                int index = textInput.getSelectionStart();
                Validator validator = textInput.getValidator();

                if (validator != null
                    && strictValidation) {
                    StringBuilder buf = new StringBuilder(textNode.getText());
                    buf.insert(index, character);

                    if (validator.isValid(buf.toString())) {
                        textInput.insertText(character, index);
                    } else {
                        ApplicationContext.beep();
                    }
                } else {
                    textInput.insertText(character, index);
                }
            } else {
                ApplicationContext.beep();
            }
        }

        return consumed;
    }

    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(component, keyCode, keyLocation);

        TextInput textInput = (TextInput)getComponent();
        TextNode textNode = textInput.getTextNode();

        Keyboard.Modifier commandModifier = Platform.getCommandModifier();
        if (keyCode == Keyboard.KeyCode.DELETE
            || keyCode == Keyboard.KeyCode.BACKSPACE) {
            consumed = true;

            Direction direction = (keyCode == Keyboard.KeyCode.DELETE ?
                Direction.FORWARD : Direction.BACKWARD);

            Validator validator = textInput.getValidator();

            if (validator != null
                && strictValidation) {
                StringBuilder buf = new StringBuilder(textNode.getText());
                int index = textInput.getSelectionStart();
                int count = textInput.getSelectionLength();

                if (count > 0) {
                    buf.delete(index, index + count);
                } else {
                    if (direction == Direction.BACKWARD) {
                        index--;
                    }

                    if (index >= 0
                        && index < textNode.getCharacterCount()) {
                        buf.deleteCharAt(index);
                    }
                }

                if (validator.isValid(buf.toString())) {
                    textInput.delete(direction);
                } else {
                    ApplicationContext.beep();
                }
            } else {
                textInput.delete(direction);
            }
        } else if (keyCode == Keyboard.KeyCode.LEFT) {
            consumed = true;

            int selectionStart = textInput.getSelectionStart();
            int selectionLength = textInput.getSelectionLength();

            if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)
                && Keyboard.isPressed(Keyboard.Modifier.CTRL)) {
                // Add all preceding text to the selection
                selectionLength = selectionStart + selectionLength;
                selectionStart = 0;
            } else if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                // Add the previous character to the selection
                if (selectionStart > 0) {
                    selectionStart--;
                    selectionLength++;
                }
            } else if (Keyboard.isPressed(Keyboard.Modifier.CTRL)) {
                // Clear the selection and move the caret to the beginning of
                // the text
                selectionStart = 0;
                selectionLength = 0;
            } else {
                // Clear the selection and move the caret back by one
                // character
                if (selectionLength == 0) {
                    if (selectionStart > 0) {
                        selectionStart--;
                    } else {
                        consumed = false;
                    }
                }

                selectionLength = 0;
            }

            textInput.setSelection(selectionStart, selectionLength);
        } else if (keyCode == Keyboard.KeyCode.RIGHT) {
            consumed = true;

            int selectionStart = textInput.getSelectionStart();
            int selectionLength = textInput.getSelectionLength();

            if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)
                && Keyboard.isPressed(Keyboard.Modifier.CTRL)) {
                // Add all subsequent text to the selection
                selectionLength = textNode.getCharacterCount() - selectionStart;
            } else if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                // Add the next character to the selection
                if (selectionStart + selectionLength < textNode.getCharacterCount()) {
                    selectionLength++;
                }
            } else if (Keyboard.isPressed(Keyboard.Modifier.CTRL)) {
                // Clear the selection and move the caret to the end of
                // the text
                selectionStart = textNode.getCharacterCount();
                selectionLength = 0;
            } else {
                // Clear the selection and move the caret forward by one
                // character
                selectionStart += selectionLength;

                if (selectionLength == 0) {
                    if (selectionStart < textNode.getCharacterCount()) {
                        selectionStart++;
                    } else {
                        consumed = false;
                    }
                }

                selectionLength = 0;
            }

            textInput.setSelection(selectionStart, selectionLength);
        } else if (keyCode == Keyboard.KeyCode.HOME) {
            // Move the caret to the beginning of the text
            if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                textInput.setSelection(0, textInput.getSelectionStart());
            } else {
                textInput.setSelection(0, 0);
            }

            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.END) {
            // Move the caret to the end of the text
            if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                int selectionStart = textInput.getSelectionStart();
                textInput.setSelection(selectionStart, textNode.getCharacterCount()
                    - selectionStart);
            } else {
                textInput.setSelection(textNode.getCharacterCount(), 0);
            }

            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.A
            && Keyboard.isPressed(commandModifier)) {
            consumed = true;

            // Select all
            textInput.setSelection(0, textNode.getCharacterCount());
        } else if (keyCode == Keyboard.KeyCode.X
            && Keyboard.isPressed(commandModifier)) {
            consumed = true;

            if (textInput.isPassword()) {
                ApplicationContext.beep();
            } else {
                textInput.cut();
            }
        } else if (keyCode == Keyboard.KeyCode.C
            && Keyboard.isPressed(commandModifier)) {
            consumed = true;

            if (textInput.isPassword()) {
                ApplicationContext.beep();
            } else {
                textInput.copy();
            }
        } else if (keyCode == Keyboard.KeyCode.V
            && Keyboard.isPressed(commandModifier)) {
            consumed = true;

            textInput.paste();
        } else {
            consumed = super.keyPressed(component, keyCode, keyLocation);
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

        TextInput textInput = (TextInput)component;
        Window window = textInput.getWindow();

        if (component.isFocused()) {
            // If focus was permanently transferred within this window,
            // select all
            if (obverseComponent == null
                || obverseComponent.getWindow() == window) {
                if (Mouse.getCapturer() != component) {
                    textInput.selectAll();
                }
            }

            showCaret(textInput.getSelectionLength() == 0);
        } else {
            // If focus was permanently transferred within this window,
            // clear the selection
            if (obverseComponent == null
                || obverseComponent.getWindow() == window) {
                textInput.clearSelection();
            }

            showCaret(false);
        }

        repaintComponent();
    }

    // Text input events
    @Override
    public void textNodeChanged(TextInput textInput, TextNode previousTextNode) {
        updateSelection(0);
    }

    @Override
    public void textSizeChanged(TextInput textInput, int previousTextSize) {
        invalidateComponent();
    }

    @Override
    public void maximumLengthChanged(TextInput textInput, int previousMaximumLength) {
        // No-op
    }

    @Override
    public void passwordChanged(TextInput textInput) {
        repaintComponent();
    }

    @Override
    public void promptChanged(TextInput textInput, String previousPrompt) {
      repaintComponent();
    }

    @Override
    public void textKeyChanged(TextInput textInput, String previousTextKey) {
        // No-op
    }

    @Override
    public void textValidChanged(TextInput textInput) {
        repaintComponent();
    }

    @Override
    public void textValidatorChanged(TextInput textInput, Validator previousValidator) {
        // No-op
    }

    // Text input character events
    @Override
    public void charactersInserted(TextInput textInput, int index, int count) {
        updateSelection(0);
    }

    @Override
    public void charactersRemoved(TextInput textInput, int index, int count) {
        String text = getText();
        Rectangle2D textBounds = font.getStringBounds(text, FONT_RENDER_CONTEXT);

        int textWidth = (int)textBounds.getWidth();
        int width = getWidth();

        // If the right edge of the text is less than the right inset, align
        // the text's right edge with the inset
        if (textWidth - scrollLeft + padding.left + 1 < width - padding.right - 1) {
            scrollLeft = Math.max(textWidth + (padding.left + padding.right + 2) - width, 0);
        }

        updateSelection(0);
    }

    // Text input selection events
    @Override
    public void selectionChanged(TextInput textInput, int previousSelectionStart,
        int previousSelectionLength) {
        int selectionStart = textInput.getSelectionStart();
        int selectionLength = textInput.getSelectionLength();

        int bias;
        if (selectionStart < previousSelectionStart) {
            bias = -1;
        } else if (selectionLength > previousSelectionLength) {
            bias = 1;
        } else {
            bias = 0;
        }

        updateSelection(bias);
    }

    private void updateSelection(int bias) {
        // Update the selection bounding box
        String text = getText();

        // NOTE For some reason, TextLayout does not accept zero-length
        // strings. This may be a bug in AWT, since an empty string should be
        // valid, and is necessary to determine the caret shape for an empty
        // text input.
        // TODO Report this issue to Sun?
        if (text.length() == 0) {
            text = " ";
        }

        TextInput textInput = (TextInput)getComponent();

        int selectionStart = textInput.getSelectionStart();
        int selectionLength = textInput.getSelectionLength();

        TextLayout textLayout = new TextLayout(text, font, FONT_RENDER_CONTEXT);

        caretShapes = textLayout.getCaretShapes(selectionStart);
        logicalHighlightShape = textLayout.getLogicalHighlightShape(selectionStart,
            selectionStart + selectionLength);

        int width = getWidth();

        if (width <= padding.left + padding.right + 2) {
            scrollLeft = 0;
        } else {
            if (textInput.getSelectionLength() == 0) {
                Rectangle2D caretBounds = caretShapes[0].getBounds();
                int caretLeft = (int)caretBounds.getX();

                if (caretLeft - scrollLeft < 0
                    && bias <= 0) {
                    // Ensure that the left edge of caret is visible
                    scrollLeft = caretLeft;
                } else {
                    // Ensure that the right edge of the caret is visible
                    int caretRight = (int)caretBounds.getMaxX();

                    if (caretRight - scrollLeft + padding.left + 1 > width - padding.right - 1) {
                        scrollLeft = Math.max(caretRight
                            - (width - (padding.left + padding.right + 2)), 0);
                    }
                }
            } else {
                Rectangle2D logicalHighlightBounds = logicalHighlightShape.getBounds();
                int logicalHighlightLeft = (int)logicalHighlightBounds.getX();

                if (logicalHighlightLeft - scrollLeft < 0
                    && bias <= 0) {
                    // Ensure that the left edge of the highlight is visible
                    scrollLeft = logicalHighlightLeft;
                } else {
                    // Ensure that the right edge of the highlight is visible
                    int logicalHighlightRight = (int)logicalHighlightBounds.getMaxX();

                    if (logicalHighlightRight - scrollLeft + padding.left + 1 > width - padding.right - 1) {
                        scrollLeft = Math.max(logicalHighlightRight
                            - (width - (padding.left + padding.right + 2)), 0);
                    }
                }
            }
        }

        showCaret(textInput.isFocused()
            && textInput.getSelectionLength() == 0);

        repaintComponent();
    }
}
