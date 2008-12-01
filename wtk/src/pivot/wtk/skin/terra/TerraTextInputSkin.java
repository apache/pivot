/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk.skin.terra;

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
import java.text.AttributedCharacterIterator;

import pivot.collections.Dictionary;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Clipboard;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.Cursor;
import pivot.wtk.Dimensions;
import pivot.wtk.Display;
import pivot.wtk.Insets;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.TextInput;
import pivot.wtk.TextInputListener;
import pivot.wtk.TextInputCharacterListener;
import pivot.wtk.TextInputSelectionListener;
import pivot.wtk.Theme;
import pivot.wtk.skin.ComponentSkin;

/**
 * Text input skin.
 *
 * @author gbrown
 */
public class TerraTextInputSkin extends ComponentSkin
    implements TextInputListener, TextInputCharacterListener, TextInputSelectionListener {
    private class MouseSelectionHandler
        implements ComponentMouseListener, ComponentMouseButtonListener {
        private class ScrollSelectionCallback implements Runnable {
            private int x = 0;

            public void run() {
                TextInput textInput = (TextInput)getComponent();
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
                    if (selectionStart + selectionLength < textInput.getCharacterCount()) {
                        selectionLength++;
                    }
                }

                textInput.setSelection(selectionStart, selectionLength);
            }
        }

        private ScrollSelectionCallback scrollSelectionCallback = new ScrollSelectionCallback();
        private int scrollSelectionIntervalID = -1;

        private static final int SCROLL_RATE = 50;

        public boolean mouseMove(Component component, int x, int y) {
            String text = getText();

            if (text.length() > 0) {
                Display display = (Display)component;

                TextInput textInput = (TextInput)getComponent();
                x = textInput.mapPointFromAncestor(display, x, 0).x;

                if (x >= 0
                    && x < textInput.getWidth()) {
                    // Stop the scroll selection timer
                    if (scrollSelectionIntervalID != -1) {
                        ApplicationContext.clearInterval(scrollSelectionIntervalID);
                        scrollSelectionIntervalID = -1;
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

                    if (scrollSelectionIntervalID == -1) {
                        scrollSelectionIntervalID =
                            ApplicationContext.setInterval(scrollSelectionCallback, SCROLL_RATE);

                        // Run the callback once now to scroll the selection immediately
                        scrollSelectionCallback.run();
                    }
                }
            }

            return false;
        }

        public void mouseOver(Component component) {
        }

        public void mouseOut(Component component) {
        }

        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            // Stop the scroll selection timer
            if (scrollSelectionIntervalID != -1) {
                ApplicationContext.clearInterval(scrollSelectionIntervalID);
                scrollSelectionIntervalID = -1;
            }

            // Remove the display mouse listeners
            assert (component instanceof Display);
            component.getComponentMouseListeners().remove(this);
            component.getComponentMouseButtonListeners().remove(this);

            return false;
        }

        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            return false;
        }
    }

    /**
     * TODO This class will be used to optimize rendering of text, so we don't
     * need to get a copy of the string via {@link TextInput#getText()}.
     *
     * NOTE We'll want to use this everywhere we are currently calling
     * getText(), if possible. This means that the character iterator will need
     * to return "*" characters when in password mode.
     *
     * @author gbrown
     */
    private static class TextInputCharacterIterator implements AttributedCharacterIterator {
        public TextInputCharacterIterator(TextInput textInput) {
            // TODO Need index arguments
        }

        public int getBeginIndex() {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getEndIndex() {
            // TODO Auto-generated method stub
            return 0;
        }

        public char setIndex(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getIndex() {
            // TODO Auto-generated method stub
            return 0;
        }

        public char current() {
            // TODO Auto-generated method stub
            return 0;
        }

        public char first() {
            // TODO Auto-generated method stub
            return 0;
        }

        public char last() {
            // TODO Auto-generated method stub
            return 0;
        }

        public char next() {
            // TODO Auto-generated method stub
            return 0;
        }

        public char previous() {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getRunLimit() {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getRunLimit(Attribute arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getRunLimit(java.util.Set<? extends Attribute> arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getRunStart() {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getRunStart(Attribute arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getRunStart(java.util.Set<? extends Attribute> arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        public Object getAttribute(Attribute arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public java.util.Map<Attribute, Object> getAttributes() {
            // TODO Auto-generated method stub
            return null;
        }

        public java.util.Set<Attribute> getAllAttributeKeys() {
            // TODO Auto-generated method stub
            return null;
        }

        public Object clone() {
            // TODO
            return null;
        }
    }

    private class BlinkCursorCallback implements Runnable {
        public void run() {
            caretOn = !caretOn;

            java.awt.Rectangle caretBounds = caretShapes[0].getBounds();
            LineMetrics lm = font.getLineMetrics("", fontRenderContext);

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

    protected FontRenderContext fontRenderContext = new FontRenderContext(null, true, false);

    private boolean caretOn = true;
    private Shape[] caretShapes = null;
    private Shape logicalHighlightShape = null;

    private int scrollLeft = 0;

    private BlinkCursorCallback blinkCursorCallback = new BlinkCursorCallback();
    private int blinkCursorIntervalID = -1;

    private Font font;
    private Color color;
    private Color disabledColor;
    private Color promptColor;
    private Color backgroundColor;
    private Color disabledBackgroundColor;
    private Color borderColor;
    private Color disabledBorderColor;
    private Insets padding;

    private Color selectionColor;
    private Color selectionBackgroundColor;
    private Color inactiveSelectionColor;
    private Color inactiveSelectionBackgroundColor;

    // Derived colors
    private Color bevelColor;
    private Color disabledBevelColor;

    public TerraTextInputSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        font = theme.getFont();
        color = theme.getColor(1);
        promptColor = theme.getColor(7);
        disabledColor = theme.getColor(7);
        backgroundColor = theme.getColor(11);
        disabledBackgroundColor = theme.getColor(10);
        borderColor = theme.getColor(7);
        disabledBorderColor = theme.getColor(7);
        padding = new Insets(2);

        selectionColor = theme.getColor(4);
        selectionBackgroundColor = theme.getColor(19);
        inactiveSelectionColor = theme.getColor(1);
        inactiveSelectionBackgroundColor = theme.getColor(9);

        // Set the derived colors
        bevelColor = TerraTheme.darken(backgroundColor);
        disabledBevelColor = disabledBackgroundColor;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        TextInput textInput = (TextInput)component;
        textInput.getTextInputCharacterListeners().add(this);
        textInput.getTextInputSelectionListeners().add(this);

        textInput.setCursor(Cursor.TEXT);

        selectionChanged(textInput, 0, 0);
    }

    @Override
    public void uninstall() {
        TextInput textInput = (TextInput)getComponent();
        textInput.getTextInputCharacterListeners().remove(this);
        textInput.getTextInputSelectionListeners().remove(this);

        textInput.setCursor(Cursor.DEFAULT);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        TextInput textInput = (TextInput)getComponent();
        int textSize = textInput.getTextSize();

        // TODO Localize?
        // TODO Recalculate only when font changes
        String testString = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz";

        Rectangle2D testStringBounds = font.getStringBounds(testString, fontRenderContext);
        int averageCharWidth = (int)Math.round((testStringBounds.getWidth() / testString.length()));

        return textSize * averageCharWidth + (padding.left + padding.right) + 2;
    }

    public int getPreferredHeight(int width) {
        // TODO Recalculate only when font changes
        Rectangle2D maxCharBounds = font.getMaxCharBounds(fontRenderContext);
        int maxCharHeight = (int)Math.ceil(maxCharBounds.getHeight());

        return maxCharHeight + (padding.top + padding.bottom) + 2;
    }

    public Dimensions getPreferredSize() {
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    public void layout() {
        // No-op
    }

    public void paint(Graphics2D graphics) {
        TextInput textInput = (TextInput)getComponent();

        int width = getWidth();
        int height = getHeight();

        Color backgroundColor;
        Color borderColor;
        Color bevelColor;

        if (textInput.isEnabled()) {
            backgroundColor = this.backgroundColor;
            borderColor = this.borderColor;
            bevelColor = this.bevelColor;
        }
        else {
            backgroundColor = disabledBackgroundColor;
            borderColor = this.disabledBorderColor;
            bevelColor = disabledBevelColor;
        }

        graphics.setStroke(new BasicStroke());

        // Paint the background
        graphics.setPaint(backgroundColor);
        graphics.fillRect(0, 0, width, height);

        // Paint the bevel
        graphics.setPaint(bevelColor);
        graphics.drawLine(1, 1, width - 2, 1);

        // Paint the border
        graphics.setPaint(borderColor);
        graphics.drawRect(0, 0, width - 1, height - 1);

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

        LineMetrics lm = font.getLineMetrics("", fontRenderContext);
        int ascent = Math.round(lm.getAscent());

        graphics.translate(padding.left - scrollLeft + 1, padding.top + ascent + 1);

        if (text.length() > 0) {
            // Paint the text
            if (fontRenderContext.isAntiAliased()) {
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    ApplicationContext.getTextAntialiasingHint());
            }

            if (fontRenderContext.usesFractionalMetrics()) {
                graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            }

            Color color;
            if (textInput.isEnabled()) {
            	color = prompt ? promptColor : this.color;
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
            graphics.setPaint(Color.BLACK);
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
        TextLayout textLayout = new TextLayout(text, font, fontRenderContext);
        TextHitInfo textHitInfo = textLayout.hitTestChar(x + scrollLeft - padding.left - 1, 0);
        int index = textHitInfo.getInsertionIndex();

        return index;
    }

    public void showCaret(boolean show) {
        if (show) {
            if (blinkCursorIntervalID == -1) {
                blinkCursorIntervalID = ApplicationContext.setInterval(blinkCursorCallback,
                    ApplicationContext.getCursorBlinkRate());

                // Run the callback once now to show the cursor immediately
                blinkCursorCallback.run();
            }
        } else {
            if (blinkCursorIntervalID != -1) {
                ApplicationContext.clearInterval(blinkCursorIntervalID);
                blinkCursorIntervalID = -1;
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

        setFont(Font.decode(font));
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

        setColor(decodeColor(color));
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

        setPromptColor(decodeColor(promptColor));
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

        setDisabledColor(decodeColor(disabledColor));
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

        setBackgroundColor(decodeColor(backgroundColor));
    }

    public final void setBackgroundColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(color));
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

        setDisabledBackgroundColor(decodeColor(disabledBackgroundColor));
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

        setBorderColor(decodeColor(borderColor));
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

        setDisabledBorderColor(decodeColor(disabledBorderColor));
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

        setSelectionColor(decodeColor(selectionColor));
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

        setSelectionBackgroundColor(decodeColor(selectionBackgroundColor));
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

        setInactiveSelectionColor(decodeColor(inactiveSelectionColor));
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

        setInactiveSelectionBackgroundColor(decodeColor(inactiveSelectionBackgroundColor));
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

            // Begin selecting text
            MouseSelectionHandler mouseSelectionHandler = new MouseSelectionHandler();

            Display display = textInput.getDisplay();
            display.getComponentMouseListeners().add(mouseSelectionHandler);
            display.getComponentMouseButtonListeners().add(mouseSelectionHandler);

            // Set focus to the text input
            textInput.requestFocus();
        }

        return super.mouseDown(component, button, x, y);
    }

    @Override
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        if (button == Mouse.Button.LEFT
            && count > 1) {
            TextInput textInput = (TextInput)getComponent();
            textInput.setSelection(0, textInput.getCharacterCount());
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

            int selectionStart = textInput.getSelectionStart();
            int selectionLength = textInput.getSelectionLength();

            if (selectionLength > 0) {
                textInput.removeText(selectionStart, selectionLength);
            }

            if (textInput.getCharacterCount() < textInput.getMaximumLength()) {
                textInput.insertText(character, selectionStart);
            } else {
                ApplicationContext.beep();
            }
        }

        return consumed;
    }

    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        TextInput textInput = (TextInput)getComponent();

        if (keyCode == Keyboard.KeyCode.DELETE) {
            int selectionStart = textInput.getSelectionStart();
            int selectionLength = textInput.getSelectionLength();

            if (selectionLength > 0) {
                // Delete any selected text
                textInput.removeText(selectionStart, selectionLength);
            } else {
                // Delete the character after the caret (if any)
                if (selectionStart < textInput.getCharacterCount()) {
                    textInput.removeText(selectionStart, 1);
                }
            }
        } else if (keyCode == Keyboard.KeyCode.BACKSPACE) {
            int selectionLength = textInput.getSelectionLength();

            if (selectionLength > 0) {
                // Delete any selected text
                textInput.removeText(textInput.getSelectionStart(), selectionLength);
            } else {
                // Delete the character before the caret (if any)
                int selectionStart = textInput.getSelectionStart();

                if (selectionStart > 0) {
                    textInput.removeText(selectionStart - 1, 1);
                }
            }
        } else if (keyCode == Keyboard.KeyCode.LEFT) {
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
                if (selectionLength == 0
                    && selectionStart > 0) {
                    selectionStart--;
                }

                selectionLength = 0;
            }

            textInput.setSelection(selectionStart, selectionLength);
        } else if (keyCode == Keyboard.KeyCode.RIGHT) {
            int selectionStart = textInput.getSelectionStart();
            int selectionLength = textInput.getSelectionLength();

            if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)
                && Keyboard.isPressed(Keyboard.Modifier.CTRL)) {
                // Add all subsequent text to the selection
                selectionLength = textInput.getCharacterCount() - selectionStart;
            } else if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                // Add the next character to the selection
                if (selectionStart + selectionLength < textInput.getCharacterCount()) {
                    selectionLength++;
                }
            } else if (Keyboard.isPressed(Keyboard.Modifier.CTRL)) {
                // Clear the selection and move the caret to the end of
                // the text
                selectionStart = textInput.getCharacterCount();
                selectionLength = 0;
            } else {
                // Clear the selection and move the caret forward by one
                // character
                selectionStart += selectionLength;

                if (selectionLength == 0
                    && selectionStart < textInput.getCharacterCount()) {
                    selectionStart++;
                }

                selectionLength = 0;
            }

            textInput.setSelection(selectionStart, selectionLength);
        } else if (keyCode == Keyboard.KeyCode.A
            && Keyboard.isPressed(Keyboard.Modifier.CTRL)) {
            // Select all
            textInput.setSelection(0, textInput.getCharacterCount());
        } else if (keyCode == Keyboard.KeyCode.X
            && Keyboard.isPressed(Keyboard.Modifier.CTRL)) {
            // "Cut"
            if (textInput.isPassword()) {
                ApplicationContext.beep();
            } else {
                // Delete any selected text and put it on the clipboard
                int selectionLength = textInput.getSelectionLength();
                if (selectionLength > 0) {
                    String removedText = textInput.removeText(textInput.getSelectionStart(),
                        selectionLength);

                    Clipboard.setContent(removedText);
                }
            }
        } else if (keyCode == Keyboard.KeyCode.C
            && Keyboard.isPressed(Keyboard.Modifier.CTRL)) {
            // "Copy"
            if (textInput.isPassword()) {
                ApplicationContext.beep();
            } else {
                // Copy selection to clipboard
                String selectedText = textInput.getSelectedText();
                if (selectedText != null) {
                    Clipboard.setContent(selectedText);
                }
            }
        } else if (keyCode == Keyboard.KeyCode.V
            && Keyboard.isPressed(Keyboard.Modifier.CTRL)) {
            // "Paste"
            Object clipboardContents = Clipboard.getContent();

            if (clipboardContents != null) {
                // Paste the string representation of the content
                String clipboardText = clipboardContents.toString();

                if ((clipboardText.length()
                    + textInput.getCharacterCount()) > textInput.getMaximumLength()) {
                    ApplicationContext.beep();
                } else {
                    // Remove any existing selection
                    int selectionLength = textInput.getSelectionLength();
                    if (selectionLength > 0) {
                        textInput.removeText(textInput.getSelectionStart(),
                            selectionLength);
                    }

                    // Insert the clipboard contents
                    int selectionStart = textInput.getSelectionStart();
                    textInput.insertText(clipboardText, selectionStart);
                }
            }
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
    public void focusedChanged(Component component, boolean temporary) {
        super.focusedChanged(component, temporary);

        TextInput textInput = (TextInput)getComponent();

        if (component.isFocused()) {
            showCaret(textInput.getSelectionLength() == 0);

            if (!temporary
                && Mouse.getButtons() == 0) {
                textInput.setSelection(0, textInput.getCharacterCount());
            }
        } else {
            if (!temporary) {
                textInput.setSelection(textInput.getSelectionStart()
                    + textInput.getSelectionLength(), 0);
            }

            showCaret(false);
        }

        repaintComponent();
    }

    // Text input events
    public void textSizeChanged(TextInput textInput, int previousTextSize) {
        invalidateComponent();
    }

    public void maximumLengthChanged(TextInput textInput, int previousMaximumLength) {
        // No-op
    }

    public void passwordChanged(TextInput textInput) {
        repaintComponent();
    }

    public void promptChanged(TextInput textInput, String previousPrompt) {
    	repaintComponent();
    }

    public void textKeyChanged(TextInput textInput, String previousTextKey) {
        // No-op
    }

    // Text input character events
    public void charactersInserted(TextInput textInput, int index, int count) {
        repaintComponent();
    }

    public void charactersRemoved(TextInput textInput, int index, int count) {
        String text = getText();
        Rectangle2D textBounds = font.getStringBounds(text, fontRenderContext);

        int textWidth = (int)textBounds.getWidth();
        int width = getWidth();

        // If the right edge of the text is less than the right inset, align
        // the text's right edge with the inset
        if (textWidth - scrollLeft + padding.left + 1 < width - padding.right - 1) {
            scrollLeft = Math.max(textWidth + (padding.left + padding.right + 2) - width, 0);
        }

        repaintComponent();
    }

    public void charactersReset(TextInput textInput) {
        repaintComponent();
    }

    // Text input selection events
    public void selectionChanged(TextInput textInput,
        int previousSelectionStart, int previousSelectionLength) {
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

        int selectionStart = textInput.getSelectionStart();
        int selectionLength = textInput.getSelectionLength();

        TextLayout textLayout = new TextLayout(text, font, fontRenderContext);

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

                if (caretLeft - scrollLeft < 0) {
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
                    && previousSelectionStart > selectionStart) {
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
            && selectionLength == 0);

        repaintComponent();
    }
}
