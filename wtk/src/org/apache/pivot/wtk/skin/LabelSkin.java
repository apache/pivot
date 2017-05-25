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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.PrintGraphics;
import java.awt.Transparency;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.StringCharacterIterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.LabelListener;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.TextDecoration;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.VerticalAlignment;

/**
 * Label skin.
 */
public class LabelSkin extends ComponentSkin implements LabelListener {
    private Font font;
    private Color color;
    private Color disabledColor;
    private Color backgroundColor;
    private TextDecoration textDecoration;
    private HorizontalAlignment horizontalAlignment;
    private VerticalAlignment verticalAlignment;
    private Insets padding;
    private boolean wrapText;

    private ArrayList<GlyphVector> glyphVectors = null;
    private float textHeight = 0;

    public LabelSkin() {
        font = currentTheme().getFont();
        color = defaultForegroundColor();
        disabledColor = Color.GRAY;
        backgroundColor = null;
        textDecoration = null;
        horizontalAlignment = HorizontalAlignment.LEFT;
        verticalAlignment = VerticalAlignment.TOP;
        padding = Insets.NONE;
        wrapText = false;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Label label = (Label) getComponent();
        label.getLabelListeners().add(this);
    }

    @Override
    public int getPreferredWidth(int height) {
        Label label = (Label) getComponent();
        String text = label.getText();

        int preferredWidth = 0;
        if (text != null && text.length() > 0) {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            String str[];
            if (wrapText) {
                str = text.split("\n");
            } else {
                str = new String[] { text };
            }

            for (String line : str) {
                Rectangle2D stringBounds = font.getStringBounds(line, fontRenderContext);
                int w = (int) Math.ceil(stringBounds.getWidth());

                if (w > preferredWidth) {
                    preferredWidth = w;
                }
            }
        }

        preferredWidth += (padding.left + padding.right);

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        Label label = (Label) getComponent();
        String text = label.getText();

        float preferredHeight;
        if (text != null) {
            int widthUpdated = width;
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            LineMetrics lm = font.getLineMetrics("", fontRenderContext);
            float lineHeight = lm.getHeight();

            preferredHeight = lineHeight;

            int n = text.length();
            if (n > 0 && wrapText && widthUpdated != -1) {
                // Adjust width for padding
                widthUpdated -= (padding.left + padding.right);

                float lineWidth = 0;
                int lastWhitespaceIndex = -1;

                int i = 0;
                while (i < n) {
                    char c = text.charAt(i);
                    if (c == '\n') {
                        lineWidth = 0;
                        lastWhitespaceIndex = -1;

                        preferredHeight += lineHeight;
                    } else {
                        if (Character.isWhitespace(c)) {
                            lastWhitespaceIndex = i;
                        }

                        Rectangle2D characterBounds = font.getStringBounds(text, i, i + 1,
                            fontRenderContext);
                        lineWidth += characterBounds.getWidth();

                        if (lineWidth > widthUpdated && lastWhitespaceIndex != -1) {
                            i = lastWhitespaceIndex;

                            lineWidth = 0;
                            lastWhitespaceIndex = -1;

                            preferredHeight += lineHeight;
                        }
                    }

                    i++;
                }
            }
        } else {
            preferredHeight = 0;
        }

        preferredHeight += (padding.top + padding.bottom);

        return (int) Math.ceil(preferredHeight);
    }

    @Override
    public Dimensions getPreferredSize() {
        Label label = (Label) getComponent();
        String text = label.getText();

        FontRenderContext fontRenderContext = Platform.getFontRenderContext();

        LineMetrics lm = font.getLineMetrics("", fontRenderContext);
        int lineHeight = (int) Math.ceil(lm.getHeight());

        int preferredHeight = 0;
        int preferredWidth = 0;

        if (text != null && text.length() > 0) {
            String str[];
            if (wrapText) {
                str = text.split("\n");
            } else {
                str = new String[] { text };
            }

            for (String line : str) {
                Rectangle2D stringBounds = font.getStringBounds(line, fontRenderContext);
                int w = (int) Math.ceil(stringBounds.getWidth());

                if (w > preferredWidth) {
                    preferredWidth = w;
                }
                preferredHeight += lineHeight;
            }
        } else {
            preferredHeight += lineHeight;
        }

        preferredHeight += (padding.top + padding.bottom);
        preferredWidth += (padding.left + padding.right);

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();
        LineMetrics lm = font.getLineMetrics("", fontRenderContext);
        float ascent = lm.getAscent();

        float textHeightLocal;
        if (wrapText) {
            textHeightLocal = Math.max(getPreferredHeight(width) - (padding.top + padding.bottom),
                0);
        } else {
            textHeightLocal = (int) Math.ceil(lm.getHeight());
        }

        int baseline = -1;
        switch (verticalAlignment) {
            case TOP: {
                baseline = Math.round(padding.top + ascent);
                break;
            }

            case CENTER: {
                baseline = Math.round((height - textHeightLocal) / 2 + ascent);
                break;
            }

            case BOTTOM: {
                baseline = Math.round(height - (textHeightLocal + padding.bottom) + ascent);
                break;
            }

            default: {
                break;
            }
        }

        return baseline;
    }

    @Override
    public void layout() {
        Label label = (Label) getComponent();
        String text = label.getText();

        glyphVectors = new ArrayList<>();
        textHeight = 0;

        if (text != null) {
            int n = text.length();

            if (n > 0) {
                FontRenderContext fontRenderContext = Platform.getFontRenderContext();

                if (wrapText) {
                    int width = getWidth() - (padding.left + padding.right);

                    int i = 0;
                    int start = 0;
                    float lineWidth = 0;
                    int lastWhitespaceIndex = -1;

                    // NOTE: We use a character iterator here only because it is the most
                    // efficient way to measure the character bounds (as of Java 6, the version
                    // of Font#getStringBounds() that takes a String performs a string copy,
                    // whereas the version that takes a character iterator does not).
                    StringCharacterIterator ci = new StringCharacterIterator(text);
                    while (i < n) {
                        char c = text.charAt(i);
                        if (c == '\n') {
                            appendLine(text, start, i, fontRenderContext);

                            start = i + 1;
                            lineWidth = 0;
                            lastWhitespaceIndex = -1;
                        } else {
                            if (Character.isWhitespace(c)) {
                                lastWhitespaceIndex = i;
                            }

                            Rectangle2D characterBounds = font.getStringBounds(ci, i, i + 1,
                                fontRenderContext);
                            lineWidth += characterBounds.getWidth();

                            if (lineWidth > width && lastWhitespaceIndex != -1) {
                                appendLine(text, start, lastWhitespaceIndex, fontRenderContext);

                                i = lastWhitespaceIndex;
                                start = i + 1;
                                lineWidth = 0;
                                lastWhitespaceIndex = -1;
                            }
                        }

                        i++;
                    }

                    appendLine(text, start, i, fontRenderContext);
                } else {
                    appendLine(text, 0, text.length(), fontRenderContext);
                }
            }
        }
    }

    private void appendLine(String text, int start, int end, FontRenderContext fontRenderContext) {
        StringCharacterIterator line = new StringCharacterIterator(text, start, end, start);
        GlyphVector glyphVector = font.createGlyphVector(fontRenderContext, line);
        glyphVectors.add(glyphVector);

        Rectangle2D textBounds = glyphVector.getLogicalBounds();
        textHeight += textBounds.getHeight();
    }

    @Override
    public void paint(Graphics2D graphics) {
        Label label = (Label) this.getComponent();

        int width = getWidth();
        int height = getHeight();

        // Draw the background
        if (backgroundColor != null) {
            graphics.setPaint(backgroundColor);
            graphics.fillRect(0, 0, width, height);
        }

        // Draw the text
        if (glyphVectors != null && glyphVectors.getLength() > 0) {
            graphics.setFont(font);

            if (label.isEnabled()) {
                graphics.setPaint(color);
            } else {
                graphics.setPaint(disabledColor);
            }

            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            LineMetrics lm = font.getLineMetrics("", fontRenderContext);
            float ascent = lm.getAscent();
            float lineHeight = lm.getHeight();

            float y = 0;
            switch (verticalAlignment) {
                case TOP: {
                    y = padding.top;
                    break;
                }

                case BOTTOM: {
                    y = height - (textHeight + padding.bottom);
                    break;
                }

                case CENTER: {
                    y = (height - textHeight) / 2;
                    break;
                }

                default: {
                    break;
                }
            }

            for (int i = 0, n = glyphVectors.getLength(); i < n; i++) {
                GlyphVector glyphVector = glyphVectors.get(i);

                Rectangle2D textBounds = glyphVector.getLogicalBounds();
                float lineWidth = (float) textBounds.getWidth();

                float x = 0;
                switch (horizontalAlignment) {
                    case LEFT: {
                        x = padding.left;
                        break;
                    }
                    case RIGHT: {
                        x = width - (lineWidth + padding.right);
                        break;
                    }
                    case CENTER: {
                        x = (width - lineWidth) / 2;
                        break;
                    }
                    default: {
                        break;
                    }
                }

                if (graphics instanceof PrintGraphics) {
                    // Work-around for printing problem in applets
                    String text = label.getText();
                    if (text != null && text.length() > 0) {
                        graphics.drawString(text, x, y + ascent);
                    }
                } else {
                    graphics.drawGlyphVector(glyphVector, x, y + ascent);
                }

                // Draw the text decoration
                if (textDecoration != null) {
                    graphics.setStroke(new BasicStroke());

                    float offset = 0;

                    switch (textDecoration) {
                        case UNDERLINE: {
                            offset = y + ascent + 2;
                            break;
                        }
                        case STRIKETHROUGH: {
                            offset = y + lineHeight / 2 + 1;
                            break;
                        }
                        default: {
                            break;
                        }
                    }

                    Line2D line = new Line2D.Float(x, offset, x + lineWidth, offset);
                    graphics.draw(line);
                }

                y += textBounds.getHeight();
            }
        }
    }

    /**
     * @return <tt>false</tt>; labels are not focusable.
     */
    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public boolean isOpaque() {
        return (backgroundColor != null && backgroundColor.getTransparency() == Transparency.OPAQUE);
    }

    /**
     * @return The font used in rendering the Label's text.
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets the font used in rendering the Label's text.
     *
     * @param font The new font to use to render the text.
     */
    public void setFont(Font font) {
        Utils.checkNull(font, "font");

        this.font = font;
        invalidateComponent();
    }

    /**
     * Sets the font used in rendering the Label's text
     *
     * @param font A {@linkplain ComponentSkin#decodeFont(String) font specification}.
     */
    public final void setFont(String font) {
        Utils.checkNull(font, "font");

        setFont(decodeFont(font));
    }

    /**
     * Sets the font used in rendering the Label's text
     *
     * @param font A dictionary {@linkplain Theme#deriveFont describing a font}.
     */
    public final void setFont(Dictionary<String, ?> font) {
        Utils.checkNull(font, "font");

        setFont(Theme.deriveFont(font));
    }

    /**
     * @return The foreground color of the text of the label.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the foreground color of the text of the label.
     *
     * @param color The new foreground color for the label text.
     */
    public void setColor(Color color) {
        Utils.checkNull(color, "color");

        this.color = color;
        repaintComponent();
    }

    /**
     * Sets the foreground color of the text of the label.
     *
     * @param color Any of the {@linkplain GraphicsUtilities#decodeColor color
     * values recognized by Pivot}.
     */
    public final void setColor(String color) {
        Utils.checkNull(color, "color");

        setColor(GraphicsUtilities.decodeColor(color));
    }

    /**
     * @return The foreground color of the text of the label when disabled.
     */
    public Color getDisabledColor() {
        return disabledColor;
    }

    /**
     * Sets the foreground color of the text of the label when disabled.
     *
     * @param color The new disabled text color.
     */
    public void setDisabledColor(Color color) {
        Utils.checkNull(color, "disabledColor");

        this.disabledColor = color;
        repaintComponent();
    }

    /**
     * Sets the foreground color of the text of the label when disabled.
     *
     * @param color Any of the {@linkplain GraphicsUtilities#decodeColor color
     * values recognized by Pivot}.
     */
    public final void setDisabledColor(String color) {
        Utils.checkNull(color, "disabledColor");

        setDisabledColor(GraphicsUtilities.decodeColor(color));
    }

    /**
     * @return The background color of the label.
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color of the label.
     *
     * @param backgroundColor The new background color for the label.
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    /**
     * Sets the background color of the label.
     *
     * @param backgroundColor Any of the
     * {@linkplain GraphicsUtilities#decodeColor color values recognized by
     * Pivot}.
     */
    public final void setBackgroundColor(String backgroundColor) {
        Utils.checkNull(backgroundColor, "backgroundColor");

        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor));
    }

    public TextDecoration getTextDecoration() {
        return textDecoration;
    }

    public void setTextDecoration(TextDecoration textDecoration) {
        this.textDecoration = textDecoration;
        repaintComponent();
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        Utils.checkNull(horizontalAlignment, "horizontalAlignment");

        this.horizontalAlignment = horizontalAlignment;
        repaintComponent();
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        Utils.checkNull(verticalAlignment, "verticalAlignment");

        this.verticalAlignment = verticalAlignment;
        repaintComponent();
    }

    /**
     * @return The amount of space to leave between the edge of the Label and
     * its text.
     */
    public Insets getPadding() {
        return padding;
    }

    /**
     * Sets the amount of space to leave between the edge of the Label and its
     * text.
     *
     * @param padding The new value of the padding for each edge.
     */
    public void setPadding(Insets padding) {
        Utils.checkNull(padding, "padding");

        this.padding = padding;
        invalidateComponent();
    }

    /**
     * Sets the amount of space to leave between the edge of the Label and its
     * text.
     *
     * @param padding A dictionary with keys in the set {left, top, bottom,
     * right}.
     */
    public final void setPadding(Dictionary<String, ?> padding) {
        Utils.checkNull(padding, "padding");

        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the Label and its
     * text, uniformly on all four edges.
     *
     * @param padding The new single padding value to use for all edges.
     */
    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the Label and its
     * text, uniformly on all four edges.
     *
     * @param padding The new (integer) padding value to use for all edges.
     */
    public final void setPadding(Number padding) {
        Utils.checkNull(padding, "padding");

        setPadding(padding.intValue());
    }

    /**
     * Sets the amount of space to leave between the edge of the Label and its
     * text.
     *
     * @param padding A string containing an integer or a JSON dictionary with
     * keys left, top, bottom, and/or right.
     */
    public final void setPadding(String padding) {
        Utils.checkNull(padding, "padding");

        setPadding(Insets.decode(padding));
    }

    /**
     * @return {@code true} if the text of the label will be wrapped to fit the Label's
     * width.
     */
    public boolean getWrapText() {
        return wrapText;
    }

    /**
     * Sets whether the text of the label will be wrapped to fit the Label's
     * width. Note that for wrapping to occur, the Label must specify a
     * preferred width or be placed in a container that constrains its width.
     * <p>Also note that newline characters (if wrapping is set true) will cause a
     * hard line break.
     *
     * @param wrapText Whether or not to wrap the Label's text within its width.
     */
    public void setWrapText(boolean wrapText) {
        this.wrapText = wrapText;
        invalidateComponent();
    }

    // Label events
    @Override
    public void textChanged(Label label, String previousText) {
        invalidateComponent();
    }

    @Override
    public void maximumLengthChanged(Label label, int previousMaximumLength) {
        invalidateComponent();
    }

}
