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
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.StringCharacterIterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
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
 * <p>
 * TODO Add a showEllipsis style.
 */
public class LabelSkin extends ComponentSkin implements LabelListener {
    private Font font;
    private Color color;
    private Color backgroundColor;
    private TextDecoration textDecoration;
    private HorizontalAlignment horizontalAlignment;
    private VerticalAlignment verticalAlignment;
    private Insets padding;
    private boolean wrapText;

    private ArrayList<GlyphVector> glyphVectors = null;
    private float textHeight = -1;

    private static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(null, true, true);

    public LabelSkin() {
        Theme theme = Theme.getTheme();
        font = theme.getFont();
        color = Color.BLACK;
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

        Label label = (Label)getComponent();
        label.getLabelListeners().add(this);
    }

    @Override
    public int getPreferredWidth(int height) {
        Label label = (Label)getComponent();
        String text = label.getText();

        int preferredWidth;
        if (text != null
            && text.length() > 0) {
            Rectangle2D stringBounds = font.getStringBounds(text, FONT_RENDER_CONTEXT);
            preferredWidth = (int)Math.ceil(stringBounds.getWidth());
        } else {
            preferredWidth = 0;
        }

        preferredWidth += (padding.left + padding.right);

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        Label label = (Label)getComponent();
        String text = label.getText();

        LineMetrics lm = font.getLineMetrics("", FONT_RENDER_CONTEXT);
        float lineHeight = lm.getHeight();

        float preferredHeight = lineHeight;

        if (text != null
            && wrapText
            && width != -1) {
            int n = text.length();

            if (n > 0) {
                // Adjust width for padding
                width -= (padding.left + padding.right);

                float lineWidth = 0;
                int lastWhitespaceIndex = -1;

                int i = 0;
                while (i < n) {
                    char c = text.charAt(i);
                    if (Character.isWhitespace(c)) {
                        lastWhitespaceIndex = i;
                    }

                    Rectangle2D characterBounds = font.getStringBounds(text, i, i + 1,
                        FONT_RENDER_CONTEXT);
                    lineWidth += characterBounds.getWidth();

                    if (lineWidth > width
                        && lastWhitespaceIndex != -1) {
                        i = lastWhitespaceIndex;

                        lineWidth = 0;
                        lastWhitespaceIndex = -1;

                        preferredHeight += lineHeight;
                    }

                    i++;
                }
            }
        }

        preferredHeight += (padding.top + padding.bottom);

        return (int)Math.ceil(preferredHeight);
    }

    @Override
    public Dimensions getPreferredSize() {
        Label label = (Label)getComponent();
        String text = label.getText();

        int preferredWidth;
        if (text != null
            && text.length() > 0) {
            Rectangle2D stringBounds = font.getStringBounds(text, FONT_RENDER_CONTEXT);
            preferredWidth = (int)Math.ceil(stringBounds.getWidth());
        } else {
            preferredWidth = 0;
        }

        preferredWidth += (padding.left + padding.right);

        LineMetrics lm = font.getLineMetrics("", FONT_RENDER_CONTEXT);
        int preferredHeight = (int)Math.ceil(lm.getHeight()) + (padding.top + padding.bottom);

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width) {
        LineMetrics lm = font.getLineMetrics("", FONT_RENDER_CONTEXT);
        return (int)Math.ceil(lm.getAscent() - 2);
    }

    @Override
    public void layout() {
        Label label = (Label)getComponent();
        String text = label.getText();

        glyphVectors = new ArrayList<GlyphVector>();
        textHeight = 0;

        if (text != null) {
            int n = text.length();

            if (n > 0) {
                if (wrapText) {
                    int width = getWidth() - (padding.left + padding.right);

                    float lineWidth = 0;
                    int lastWhitespaceIndex = -1;

                    int start = 0;
                    int i = 0;
                    while (i < n) {
                        char c = text.charAt(i);
                        if (Character.isWhitespace(c)) {
                            lastWhitespaceIndex = i;
                        }

                        Rectangle2D characterBounds = font.getStringBounds(text, i, i + 1,
                            FONT_RENDER_CONTEXT);
                        lineWidth += characterBounds.getWidth();

                        if (lineWidth > width
                            && lastWhitespaceIndex != -1) {
                            i = lastWhitespaceIndex;

                            lineWidth = 0;
                            lastWhitespaceIndex = -1;

                            // Append the current line
                            if ((i - 1) - start > 0) {
                                StringCharacterIterator line = new StringCharacterIterator(text, start, i, start);
                                GlyphVector glyphVector = font.createGlyphVector(FONT_RENDER_CONTEXT, line);
                                glyphVectors.add(glyphVector);

                                Rectangle2D logicalBounds = glyphVector.getLogicalBounds();
                                textHeight += logicalBounds.getHeight();
                            }

                            start = i + 1;
                        }

                        i++;
                    }

                    // Append the final line
                    if ((i - 1) - start > 0) {
                        StringCharacterIterator line = new StringCharacterIterator(text, start, i, start);
                        GlyphVector glyphVector = font.createGlyphVector(FONT_RENDER_CONTEXT, line);
                        glyphVectors.add(glyphVector);

                        Rectangle2D logicalBounds = glyphVector.getLogicalBounds();
                        textHeight += logicalBounds.getHeight();
                    }
                } else {
                    GlyphVector glyphVector = font.createGlyphVector(FONT_RENDER_CONTEXT, text);
                    glyphVectors.add(glyphVector);

                    Rectangle2D logicalBounds = glyphVector.getLogicalBounds();
                    textHeight += logicalBounds.getHeight();
                }
            }
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();

        // Draw the background
        if (backgroundColor != null) {
            graphics.setPaint(backgroundColor);
            graphics.fillRect(0, 0, width, height);
        }

        if (debugBaseline) {
            drawBaselineDebug(graphics);
        }

        // Draw the text
        if (glyphVectors.getLength() > 0) {
            graphics.setFont(font);
            graphics.setPaint(color);

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            if (FONT_RENDER_CONTEXT.isAntiAliased()) {
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    Platform.getTextAntialiasingHint());
            }

            if (FONT_RENDER_CONTEXT.usesFractionalMetrics()) {
                graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            }

            LineMetrics lm = font.getLineMetrics("", FONT_RENDER_CONTEXT);
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
            }

            for (int i = 0, n = glyphVectors.getLength(); i < n; i++) {
                GlyphVector glyphVector = glyphVectors.get(i);

                Rectangle2D logicalBounds = glyphVector.getLogicalBounds();
                float lineWidth = (float)logicalBounds.getWidth();

                float x = 0;
                switch(horizontalAlignment) {
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
                }

                graphics.drawGlyphVector(glyphVector, x, y + ascent);

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
                    }

                    Line2D line = new Line2D.Float(x, offset, x + lineWidth, offset);
                    graphics.draw(line);
                }

                y += logicalBounds.getHeight();
            }
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
    public boolean isOpaque() {
        return (backgroundColor != null
            && backgroundColor.getTransparency() == Transparency.OPAQUE);
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

        setTextDecoration(TextDecoration.valueOf(textDecoration.toUpperCase()));
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        if (horizontalAlignment == null) {
            throw new IllegalArgumentException("horizontalAlignment is null.");
        }

        this.horizontalAlignment = horizontalAlignment;
        repaintComponent();
    }

    public final void setHorizontalAlignment(String horizontalAlignment) {
        if (horizontalAlignment == null) {
            throw new IllegalArgumentException("horizontalAlignment is null.");
        }

        setHorizontalAlignment(HorizontalAlignment.valueOf(horizontalAlignment.toUpperCase()));
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        if (verticalAlignment == null) {
            throw new IllegalArgumentException("verticalAlignment is null.");
        }

        this.verticalAlignment = verticalAlignment;
        repaintComponent();
    }

    public final void setVerticalAlignment(String verticalAlignment) {
        if (verticalAlignment == null) {
            throw new IllegalArgumentException("verticalAlignment is null.");
        }

        setVerticalAlignment(VerticalAlignment.valueOf(verticalAlignment.toUpperCase()));
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

    public boolean getWrapText() {
        return wrapText;
    }

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
    public void textKeyChanged(Label label, String previousTextKey) {
        // No-op
    }
}
