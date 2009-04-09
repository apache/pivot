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

import pivot.collections.Dictionary;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Insets;
import pivot.wtk.Label;
import pivot.wtk.LabelListener;
import pivot.wtk.Platform;
import pivot.wtk.TextDecoration;
import pivot.wtk.Theme;
import pivot.wtk.VerticalAlignment;

/**
 * Label skin.
 * <p>
 * TODO showEllipsis style
 * <p>
 * TODO breakOnWhitespaceOnly style
 *
 * @author gbrown
 */
public class LabelSkin extends ComponentSkin implements LabelListener {
    private FontRenderContext fontRenderContext = new FontRenderContext(null, true, true);

    private Font font;
    private Color color;
    private TextDecoration textDecoration;
    private HorizontalAlignment horizontalAlignment;
    private VerticalAlignment verticalAlignment;
    private Insets padding;
    private boolean wrapText;

    public LabelSkin() {
        Theme theme = Theme.getTheme();
        font = theme.getFont();
        color = Color.BLACK;
        textDecoration = null;
        horizontalAlignment = HorizontalAlignment.LEFT;
        verticalAlignment = VerticalAlignment.TOP;
        padding = new Insets(0);
        wrapText = false;
    }

    @Override
    public void install(Component component) {
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
        } else {
            LineMetrics lm = font.getLineMetrics("", fontRenderContext);
            preferredHeight = (int)Math.ceil(lm.getAscent() + lm.getDescent()
                + lm.getLeading());
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
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    Platform.getTextAntialiasingHint());
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

    public int getFontSize() {
        return font.getSize();
    }

    public void setFontSize(int fontSize) {
        font = font.deriveFont((float)fontSize);
    }

    public boolean isFontBold() {
        return ((font.getStyle() & Font.BOLD) == Font.BOLD);
    }

    public void setFontBold(boolean fontBold) {
        if (isFontBold() != fontBold) {
            if (fontBold) {
                font = font.deriveFont(Font.BOLD);
            } else {
                font = font.deriveFont(font.getStyle() & (~Font.BOLD));
            }
        }
    }

    public boolean isFontItalic() {
        return ((font.getStyle() & Font.ITALIC) == Font.ITALIC);
    }

    public void setFontItalic(boolean fontItalic) {
        if (isFontItalic() != fontItalic) {
            if (fontItalic) {
                font = font.deriveFont(Font.ITALIC);
            } else {
                font = font.deriveFont(font.getStyle() & (~Font.ITALIC));
            }
        }
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

    public TextDecoration getTextDecoration() {
        return textDecoration;
    }

    public void setTextDecoration(TextDecoration textDecoration) {
        this.textDecoration = textDecoration;
        invalidateComponent();
    }

    public final void setTextDecoration(String textDecoration) {
        if (textDecoration == null) {
            throw new IllegalArgumentException("textDecoration is null.");
        }

        setTextDecoration(TextDecoration.decode(textDecoration));
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        if (horizontalAlignment == null) {
            throw new IllegalArgumentException("horizontalAlignment is null.");
        }

        if (horizontalAlignment == HorizontalAlignment.JUSTIFY) {
            throw new IllegalArgumentException("JUSTIFY is not supported");
        }

        this.horizontalAlignment = horizontalAlignment;
        repaintComponent();
    }

    public final void setHorizontalAlignment(String horizontalAlignment) {
        if (horizontalAlignment == null) {
            throw new IllegalArgumentException("horizontalAlignment is null.");
        }

        setHorizontalAlignment(HorizontalAlignment.decode(horizontalAlignment));
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        if (verticalAlignment == null) {
            throw new IllegalArgumentException("verticalAlignment is null.");
        }

        if (verticalAlignment == VerticalAlignment.JUSTIFY) {
            throw new IllegalArgumentException("JUSTIFY is not supported");
        }

        this.verticalAlignment = verticalAlignment;
        repaintComponent();
    }

    public final void setVerticalAlignment(String verticalAlignment) {
        if (verticalAlignment == null) {
            throw new IllegalArgumentException("verticalAlignment is null.");
        }

        setVerticalAlignment(VerticalAlignment.decode(verticalAlignment));
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

    public boolean getWrapText() {
        return wrapText;
    }

    public void setWrapText(boolean wrapText) {
        this.wrapText = wrapText;
        invalidateComponent();
    }

    // Label events
    public void textChanged(Label label, String previousText) {
        invalidateComponent();
    }

    public void textKeyChanged(Label label, String previousTextKey) {
        // No-op
    }
}
