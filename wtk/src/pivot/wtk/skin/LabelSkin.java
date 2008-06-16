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
package pivot.wtk.skin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import pivot.collections.Map;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Insets;
import pivot.wtk.Label;
import pivot.wtk.LabelListener;
import pivot.wtk.TextDecoration;
import pivot.wtk.VerticalAlignment;

/**
 * NOTE Labels always present a single line of text; carriage return characters
 * are ignored.
 *
 * TODO showEllipsis style
 *
 * @author gbrown
 */
public class LabelSkin extends ComponentSkin implements LabelListener {
    protected FontRenderContext fontRenderContext = new FontRenderContext(null, true, true);

    // Style properties
    protected Color color = DEFAULT_COLOR;
    protected Font font = DEFAULT_FONT;
    protected TextDecoration textDecoration = DEFAULT_TEXT_DECORATION;
    protected HorizontalAlignment horizontalAlignment = DEFAULT_HORIZONTAL_ALIGNMENT;
    protected VerticalAlignment verticalAlignment = DEFAULT_VERTICAL_ALIGNMENT;
    protected Insets padding = DEFAULT_PADDING;
    protected boolean wrapText = DEFAULT_WRAP_TEXT;

    // Default style values
    private static final Font DEFAULT_FONT = new Font("Verdana", Font.PLAIN, 11);
    private static final Color DEFAULT_COLOR = Color.BLACK;
    private static final TextDecoration DEFAULT_TEXT_DECORATION = null;
    private static final HorizontalAlignment DEFAULT_HORIZONTAL_ALIGNMENT = HorizontalAlignment.LEFT;
    private static final VerticalAlignment DEFAULT_VERTICAL_ALIGNMENT = VerticalAlignment.TOP;
    private static final Insets DEFAULT_PADDING = new Insets(0);
    private static final boolean DEFAULT_WRAP_TEXT = false;

    // Style keys
    protected static final String COLOR_KEY = "color";
    protected static final String FONT_KEY = "font";
    protected static final String TEXT_DECORATION_KEY = "textDecoration";
    protected static final String HORIZONTAL_ALIGNMENT_KEY = "horizontalAlignment";
    protected static final String VERTICAL_ALIGNMENT_KEY = "verticalAlignment";
    protected static final String PADDING_KEY = "padding";
    protected static final String WRAP_TEXT_KEY = "wrapText";

    @Override
    public void install(Component component) {
        validateComponentType(component, Label.class);

        super.install(component);

        Label label = (Label)getComponent();
        label.getLabelListeners().add(this);
    }

    @Override
    public void uninstall() {
        Label label = (Label)getComponent();
        label.getLabelListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        Label label = (Label)getComponent();
        String text = label.getText();

        if (text != null
            && text.length() > 0) {
            Rectangle2D stringBounds = font.getStringBounds(text, fontRenderContext);
            preferredWidth = (int)Math.ceil(stringBounds.getWidth());
        }

        preferredWidth += (padding.left + padding.right);

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        Label label = (Label)getComponent();
        String text = label.getText();

        if (text == null) {
            text = "";
        }

        if (wrapText
            && width != -1
            && text.length() > 0) {
            int contentWidth = width - (padding.left + padding.right);

            AttributedString attributedText = new AttributedString(text);
            attributedText.addAttribute(TextAttribute.FONT, font);

            AttributedCharacterIterator aci = attributedText.getIterator();
            LineBreakMeasurer lbm = new LineBreakMeasurer(aci, fontRenderContext);

            float lineHeights = 0;
            while (lbm.getPosition() < aci.getEndIndex()) {
                int offset = lbm.nextOffset(contentWidth);

                LineMetrics lm = font.getLineMetrics(aci,
                    lbm.getPosition(), offset, fontRenderContext);

                float lineHeight = lm.getAscent() + lm.getDescent()
                    + lm.getLeading();
                lineHeights += lineHeight;

                lbm.setPosition(offset);
            }

            preferredHeight = (int)Math.ceil(lineHeights);
        } else {
            LineMetrics lm = font.getLineMetrics(text, fontRenderContext);
            preferredHeight = (int)Math.ceil(lm.getAscent() + lm.getDescent()
                + lm.getLeading());
        }

        preferredHeight += (padding.top + padding.bottom);

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        Label label = (Label)getComponent();
        String text = label.getText();

        if (text != null
            && text.length() > 0) {
            Rectangle2D stringBounds = font.getStringBounds(text, fontRenderContext);
            preferredWidth = (int)Math.ceil(stringBounds.getWidth());
            preferredHeight = (int)Math.ceil(stringBounds.getHeight());
        }

        preferredWidth += (padding.left + padding.right);
        preferredHeight += (padding.top + padding.bottom);

        return new Dimensions(preferredWidth, preferredHeight);
    }

    public void layout() {
        // No-op
    }

    public void paint(Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();

        Label label = (Label)getComponent();
        String text = label.getText();

        if (text != null
            && text.length() > 0) {
            if (fontRenderContext.isAntiAliased()) {
                // TODO Use VALUE_TEXT_ANTIALIAS_LCD_HRGB when JDK 1.6 is
                // available on OSX?
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }

            if (fontRenderContext.usesFractionalMetrics()) {
                graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            }

            graphics.setFont(font);
            graphics.setPaint(color);

            float y = 0;
            switch (verticalAlignment) {
                case TOP: {
                    y = padding.top;
                    break;
                }

                case BOTTOM: {
                    y = height - getPreferredHeight(wrapText ? width : -1) + padding.top;
                    break;
                }

                case CENTER: {
                    y = (height - getPreferredHeight(wrapText ? width : -1)) / 2 + padding.top;
                    break;
                }
            }

            if (wrapText) {
                AttributedString attributedText = new AttributedString(text);
                attributedText.addAttribute(TextAttribute.FONT, font);

                AttributedCharacterIterator aci = attributedText.getIterator();
                LineBreakMeasurer lbm = new LineBreakMeasurer(aci, fontRenderContext);

                int contentWidth = width - (padding.left + padding.right);

                while (lbm.getPosition() < aci.getEndIndex()) {
                    TextLayout textLayout = lbm.nextLayout(contentWidth);
                    y += textLayout.getAscent();
                    drawText(graphics, textLayout, y);
                    y += textLayout.getDescent() + textLayout.getLeading();
                }
            } else {
                TextLayout textLayout = new TextLayout(text, font, fontRenderContext);
                drawText(graphics, textLayout, y + textLayout.getAscent());
            }
        }
    }

    private void drawText(Graphics2D graphics, TextLayout textLayout, float y) {
        float width = getWidth();
        Rectangle2D textBounds = textLayout.getBounds();

        float x = 0;
        switch (horizontalAlignment) {
            case LEFT: {
                x = padding.left;
                break;
            }

            case RIGHT: {
                x = width - (float)(textBounds.getX() + textBounds.getWidth()) -
                    padding.right;
                break;
            }

            case CENTER: {
                x = (width - (padding.left + padding.right) -
                    (float)(textBounds.getX() + textBounds.getWidth())) / 2f +
                    padding.left;
                break;
            }
        }

        textLayout.draw(graphics, x, y);

        if (textDecoration != null) {
            graphics.setStroke(new BasicStroke());

            float offset = 0;

            switch (textDecoration) {
                case UNDERLINE: {
                    offset = y + 2;
                    break;
                }

                case STRIKETHROUGH: {
                    offset = y - textLayout.getAscent() / 3f + 1;
                    break;
                }
            }

            Line2D line = new Line2D.Float(x, offset, x + (float)textBounds.getWidth(), offset);
            graphics.draw(line);
        }
    }

    /**
     * @return
     * <tt>false</tt>; labels are not focusable.
     */
    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object value = null;

        if (key.equals(COLOR_KEY)) {
            value = color;
        } else if (key.equals(FONT_KEY)) {
            value = font;
        } else if (key.equals(TEXT_DECORATION_KEY)) {
            value = textDecoration;
        } else if (key.equals(HORIZONTAL_ALIGNMENT_KEY)) {
            value = horizontalAlignment;
        } else if (key.equals(VERTICAL_ALIGNMENT_KEY)) {
            value = verticalAlignment;
        } else if (key.equals(PADDING_KEY)) {
            value = padding;
        } else if (key.equals(WRAP_TEXT_KEY)) {
            value = wrapText;
        } else {
            value = super.get(key);
        }

        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object put(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = color;
            color = (Color)value;

            repaintComponent();
        } else if (key.equals(FONT_KEY)) {
            if (value instanceof String) {
                value = Font.decode((String)value);
            }

            validatePropertyType(key, value, Font.class, false);

            previousValue = font;
            font = (Font)value;

            invalidateComponent();
        } else if (key.equals(TEXT_DECORATION_KEY)) {
            if (value instanceof String) {
                value = TextDecoration.decode((String)value);
            }

            validatePropertyType(key, value, TextDecoration.class, true);

            previousValue = textDecoration;
            textDecoration = (TextDecoration)value;

            invalidateComponent();
        } else if (key.equals(HORIZONTAL_ALIGNMENT_KEY)) {
            if (value instanceof String) {
                value = HorizontalAlignment.decode((String)value);
            }

            validatePropertyType(key, value, HorizontalAlignment.class, false);

            previousValue = horizontalAlignment;
            horizontalAlignment = (HorizontalAlignment)value;

            repaintComponent();
        } else if (key.equals(VERTICAL_ALIGNMENT_KEY)) {
            if (value instanceof String) {
                value = VerticalAlignment.decode((String)value);
            }

            validatePropertyType(key, value, VerticalAlignment.class, false);

            previousValue = verticalAlignment;
            verticalAlignment = (VerticalAlignment)value;

            repaintComponent();
        } else if (key.equals(PADDING_KEY)) {
            if (value instanceof Number) {
                value = new Insets(((Number)value).intValue());
            } else {
                if (value instanceof Map<?, ?>) {
                    value = new Insets((Map<String, Object>)value);
                }
            }

            validatePropertyType(key, value, Insets.class, false);

            previousValue = padding;
            padding = (Insets)value;

            invalidateComponent();
        } else if (key.equals(WRAP_TEXT_KEY)) {
            if (value instanceof String) {
                value = Boolean.parseBoolean((String)value);
            }

            validatePropertyType(key, value, Boolean.class, false);

            previousValue = wrapText;
            wrapText = (Boolean)value;

            invalidateComponent();
        } else {
            previousValue = super.put(key, value);
        }

        return previousValue;
    }

    @Override
    public Object remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(COLOR_KEY)) {
            previousValue = put(key, DEFAULT_COLOR);
        } else if (key.equals(FONT_KEY)) {
            previousValue = put(key, DEFAULT_FONT);
        } else if (key.equals(TEXT_DECORATION_KEY)) {
            previousValue = put(key, DEFAULT_TEXT_DECORATION);
        } else if (key.equals(HORIZONTAL_ALIGNMENT_KEY)) {
            previousValue = put(key, DEFAULT_HORIZONTAL_ALIGNMENT);
        } else if (key.equals(VERTICAL_ALIGNMENT_KEY)) {
            previousValue = put(key, DEFAULT_VERTICAL_ALIGNMENT);
        } else if (key.equals(PADDING_KEY)) {
            previousValue = put(key, DEFAULT_PADDING);
        } else if (key.equals(WRAP_TEXT_KEY)) {
            previousValue = put(key, DEFAULT_WRAP_TEXT);
        } else {
            previousValue = super.remove(key);
        }

        return previousValue;
    }

    @Override
    public boolean containsKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        return (key.equals(COLOR_KEY)
            || key.equals(FONT_KEY)
            || key.equals(TEXT_DECORATION_KEY)
            || key.equals(HORIZONTAL_ALIGNMENT_KEY)
            || key.equals(VERTICAL_ALIGNMENT_KEY)
            || key.equals(PADDING_KEY)
            || key.equals(WRAP_TEXT_KEY)
            || super.containsKey(key));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    // LabelListener methods

    public void textChanged(Label label, String previousText) {
        invalidateComponent();
    }

    public void textKeyChanged(Label label, String previousTextKey) {
        // No-op
    }
}
