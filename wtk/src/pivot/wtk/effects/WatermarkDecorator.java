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
package pivot.wtk.effects;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

/**
 * Decorator that overlays a watermark over a component.
 *
 * @author tvolkert
 */
public class WatermarkDecorator extends AbstractDecorator {
    private String text;
    private Font font = DEFAULT_FONT;
    private float opacity = DEFAULT_OPACITY;

    private transient Rectangle2D stringBounds;

    private static final Font DEFAULT_FONT = new Font
        ("Verdana,Bitstream Vera Sans,sans-serif", Font.BOLD, 60);
    private static final float DEFAULT_OPACITY = 0.1f;

    private static final FontRenderContext fontRenderContext =
        new FontRenderContext(null, false, false);

    /**
     * Cretes a new <tt>WatermarkDecorator</tt> with the empty string as its
     * text.
     */
    public WatermarkDecorator() {
        this("");
    }

    /**
     * Cretes a new <tt>WatermarkDecorator</tt> with the specified string as
     * its text.
     *
     * @param text
     * The decorator's text to paint over the decorated visual
     */
    public WatermarkDecorator(String text) {
        setText(text);
    }

    /**
     * Gets the text that will be painted over this decorator's visual.
     *
     * @return
     * This decorator's text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text that will be painted over this decorator's visual.
     *
     * @param text
     * This decorator's text
     */
    public void setText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text is null.");
        }

        this.text = text;
        updateStringBounds();
    }

    /**
     * Gets the font that will be used when painting this decorator's text.
     *
     * @return
     * This decorator's font
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets the font that will be used when painting this decorator's text.
     *
     * @param font
     * This decorator's font
     */
    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        this.font = font;
        updateStringBounds();
    }

    /**
     * Sets the font that will be used when painting this decorator's text.
     *
     * @param font
     * This decorator's font
     */
    public final void setFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Font.decode(font));
    }

    /**
     * Gets the opacity of the watermark.
     *
     * @return
     * This decorator's opacity
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     * Gets the opacity of the watermark.
     *
     * @param opacity
     * This decorator's opacity
     */
    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    /**
     * Updates this decorator's transient string bounds property.
     */
    private void updateStringBounds() {
        stringBounds = font.getStringBounds(text, fontRenderContext);
    }

    /**
     * Paints this decorator to the specified graphics context. It first
     * paints this decorator's visual, then overlays the watermark.
     *
     * @param graphics
     * The graphics context
     */
    public void paint(Graphics2D graphics) {
        visual.paint(graphics);

        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        graphics.setColor(Color.BLACK);
        graphics.rotate(Math.PI / 4);
        graphics.setFont(font);

        int width = (int)stringBounds.getWidth();
        int height = (int)stringBounds.getHeight();

        for (int n = visual.getWidth(), x = -n, p = 0; x < n; x += 1.5 * width, p = 0) {
            for (int m = visual.getHeight(), y = -m; y < m; y += 2 * height, p = 1 - p) {
                float xOffset = p - 0.5f;
                graphics.drawString(text, x + (xOffset * width), y);
            }
        }
    }
}
