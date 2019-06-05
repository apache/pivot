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
package org.apache.pivot.wtk;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.util.Locale;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.json.JSON;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Utils;

/**
 * Contains utility methods dealing with the Java2D API.
 */
public final class GraphicsUtilities {
    /**
     * Enumeration representing a paint type.
     */
    public enum PaintType {
        /** A solid color value. */
        SOLID_COLOR,
        /** A gradient that proceeds smoothly from the starting (X,Y) to the ending (X,Y) positions. */
        GRADIENT,
        /** A gradient that proceeds smoothly from color to color along the line from start to end position. */
        LINEAR_GRADIENT,
        /** A gradient that proceeds from color to color starting from a central position out to a given radius. */
        RADIAL_GRADIENT
    }

    public static final String PAINT_TYPE_KEY = "paintType";

    public static final String COLOR_KEY = "color";

    public static final String START_X_KEY = "startX";
    public static final String START_Y_KEY = "startY";
    public static final String END_X_KEY = "endX";
    public static final String END_Y_KEY = "endY";

    public static final String START_COLOR_KEY = "startColor";
    public static final String END_COLOR_KEY = "endColor";

    public static final String CENTER_X_KEY = "centerX";
    public static final String CENTER_Y_KEY = "centerY";
    public static final String RADIUS_KEY = "radius";

    public static final String STOPS_KEY = "stops";
    public static final String OFFSET_KEY = "offset";

    /** A bit mask for 8 bits (max size of a color value). */
    private static final int EIGHT_BITS = 0xff;
    /** A scale factor used to divide an 8-bit color value by to get a fraction from 0.0 to 1.0. */
    private static final float EIGHT_BIT_FLOAT = 255f;
    /** The default thickness of lines and borders. */
    private static final int DEFAULT_THICKNESS = 1;

    /** Number of digits in a hex RGB color value. */
    private static final int RGB_DIGIT_LENGTH = 6;
    /** Number of digits in a hex full color value (including alpha). */
    private static final int FULL_DIGIT_LENGTH = RGB_DIGIT_LENGTH + 2;
    /** Radix for hex digit values. */
    private static final int HEX_RADIX = 16;

    /** Shift value for 16 bits (or two color values). */
    private static final int TWO_BYTES = 16;
    /** Shift value for 8 bits (or one color value). */
    private static final int ONE_BYTE = 8;


    /** Utility classes should not have public constructors. */
    private GraphicsUtilities() {
    }

    /**
     * Set anti-aliasing on for the given graphics context.
     *
     * @param graphics The 2D graphics context to set the attribute for.
     */
    public static void setAntialiasingOn(final Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
    }

    /**
     * Set anti-aliasing of for the given graphics context.
     *
     * @param graphics The 2D graphics context to clear the attribute for.
     */
    public static void setAntialiasingOff(final Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    /**
     * Draw a straight line in the given graphics context from a start position for a given
     * length along the given horizontal or vertical direction with default thickness.
     *
     * @param graphics     The graphics context to draw in.
     * @param x            Starting X position.
     * @param y            Starting Y position.
     * @param length       Length along the desired direction.
     * @param orientation  Whether the line is vertical or horizontal for the given length.
     * @see #drawLine(Graphics2D, int, int, int, Orientation, int)
     */
    public static void drawLine(final Graphics2D graphics, final int x, final int y,
        final int length, final Orientation orientation) {
        drawLine(graphics, x, y, length, orientation, DEFAULT_THICKNESS);
    }

    /**
     * Draw a straight line in the given graphics context from a start position for a given
     * length along the given horizontal or vertical direction with given thickness.
     *
     * @param graphics     The graphics context to draw in.
     * @param x            Starting X position.
     * @param y            Starting Y position.
     * @param length       Length along the desired direction.
     * @param orientation  Whether the line is vertical or horizontal for the given length.
     * @param thickness    The pixel thickness of the line.
     * @see #drawLine(Graphics2D, int, int, int, Orientation)
     */
    public static void drawLine(final Graphics2D graphics, final int x, final int y,
        final int length, final Orientation orientation, final int thickness) {
        if (length > 0 && thickness > 0) {
            switch (orientation) {
                case HORIZONTAL:
                    graphics.fillRect(x, y, length, thickness);
                    break;
                case VERTICAL:
                    graphics.fillRect(x, y, thickness, length);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown orientation " + orientation);
            }
        }
    }

    /**
     * Draws a rectangle with a thickness of one pixel at the specified
     * coordinates whose <u>outer border</u> is the specified width and height.
     * In other words, the distance from the left edge of the leftmost pixel to
     * the left edge of the rightmost pixel is <tt>width - 1</tt>. <p> This
     * method provides more reliable pixel rounding behavior than
     * <tt>java.awt.Graphics#drawRect</tt> when scaling is applied because this
     * method does not stroke the shape but instead explicitly fills the desired
     * pixels with the graphics context's paint. For this reason, and because
     * Pivot supports scaling the display host, it is recommended that skins use
     * this method over <tt>java.awt.Graphics#drawRect</tt>.
     *
     * @param graphics The graphics context that will be used to perform the operation.
     * @param x        The x-coordinate of the upper-left corner of the rectangle.
     * @param y        The y-coordinate of the upper-left corner of the rectangle.
     * @param width    The <i>outer width</i> of the rectangle.
     * @param height   The <i>outer height</i> of the rectangle.
     * @see #drawRect(Graphics2D, int, int, int, int, int)
     */
    public static void drawRect(final Graphics2D graphics, final int x, final int y,
        final int width, final int height) {
        drawRect(graphics, x, y, width, height, DEFAULT_THICKNESS);
    }

    /**
     * Draws a rectangle with the specified thickness at the specified
     * coordinates whose <u>outer border</u> is the specified width and height.
     * In other words, the distance from the left edge of the leftmost pixel to
     * the left edge of the rightmost pixel is <tt>width - thickness</tt>. <p>
     * This method provides more reliable pixel rounding behavior than
     * <tt>java.awt.Graphics#drawRect</tt> when scaling is applied because this
     * method does not stroke the shape but instead explicitly fills the desired
     * pixels with the graphics context's paint. For this reason, and because
     * Pivot supports scaling the display host, it is recommended that skins use
     * this method over <tt>java.awt.Graphics#drawRect</tt>.
     *
     * @param graphics  The graphics context that will be used to perform the operation.
     * @param x         The x-coordinate of the upper-left corner of the rectangle.
     * @param y         The y-coordinate of the upper-left corner of the rectangle.
     * @param width     The <i>outer width</i> of the rectangle.
     * @param height    The <i>outer height</i> of the rectangle.
     * @param thickness The thickness of each edge.
     * @see #drawRect(Graphics2D, int, int, int, int)
     */
    public static void drawRect(final Graphics2D graphics, final int x, final int y,
        final int width, final int height, final int thickness) {
        Graphics2D rectGraphics = graphics;

        if ((graphics.getTransform().getType() & AffineTransform.TYPE_MASK_SCALE) != 0) {
            rectGraphics = (Graphics2D) graphics.create();
            setAntialiasingOn(rectGraphics);
        }

        if (width > 0 && height > 0 && thickness > 0) {
            drawLine(rectGraphics, x, y, width, Orientation.HORIZONTAL, thickness);
            drawLine(rectGraphics, x + width - thickness, y, height, Orientation.VERTICAL, thickness);
            drawLine(rectGraphics, x, y + height - thickness, width, Orientation.HORIZONTAL, thickness);
            drawLine(rectGraphics, x, y, height, Orientation.VERTICAL, thickness);
        }

        if (rectGraphics != graphics) {
            rectGraphics.dispose();
        }
    }

    /**
     * Interprets a string as a color value.
     *
     * @param value One of the following forms:
     * <ul>
     * <li>0xdddddddd - 8 hexadecimal digits, specifying 8 bits each of red,
     * green, and blue, followed by 8 bits of alpha.</li>
     * <li>#dddddd - 6 hexadecimal digits, specifying 8 bits each of red,
     * green, and blue.</li>
     * <li>Any of the names of the static colors in the Java {@link Color} class.</li>
     * <li>Any of the CSS3/X11 color names from here:
     * <a href="http://www.w3.org/TR/css3-color/">http://www.w3.org/TR/css3-color/</a>
     * (except the Java color values will be used for the standard Java names).</li>
     * <li>null - case-insensitive</li>
     * </ul>
     * @param argument A name for this color value (for the exception if it can't be decoded).
     * @return A {@link Color} on successful decoding, which could be {@code null} for an input
     * of {@code "null"}.
     * @throws NumberFormatException if the value in the first two cases
     * contains illegal hexadecimal digits.
     * @throws IllegalArgumentException if the value is not in one of the
     * formats listed above.
     * @see CSSColor
     */
    public static Color decodeColor(final String value, final String argument) throws NumberFormatException {
        Utils.checkNullOrEmpty(value, argument == null ? "color" : argument);

        Color color = null;
        if (value.startsWith("0x") || value.startsWith("0X")) {
            String digits = value.substring(2);
            if (digits.length() != FULL_DIGIT_LENGTH) {
                throw new IllegalArgumentException(
                    "Incorrect Color format.  Expecting exactly " + FULL_DIGIT_LENGTH
                            + " digits after the '0x' prefix.");
            }

            int rgb = Integer.parseInt(digits.substring(0, RGB_DIGIT_LENGTH), HEX_RADIX);
            float alpha = Integer.parseInt(digits.substring(RGB_DIGIT_LENGTH, FULL_DIGIT_LENGTH), HEX_RADIX)
                    / EIGHT_BIT_FLOAT;

            color = getColor(rgb, alpha);
        } else if (value.startsWith("#")) {
            String digits = value.substring(1);
            if (digits.length() != RGB_DIGIT_LENGTH) {
                throw new IllegalArgumentException(
                    "Incorrect Color format.  Expecting exactly " + RGB_DIGIT_LENGTH
                            + " digits after the '#' prefix.");
            }

            int rgb = Integer.parseInt(digits, HEX_RADIX);
            float alpha = 1.0f;

            color = getColor(rgb, alpha);
        } else if (!value.equalsIgnoreCase("null")) {
            // PIVOT-985: new fix:  use the new CSSColor lookup for the name, which
            // has the spelling variants already included in the X11/CSS3 list, as well
            // as all the standard Java Color names, so we can do this without doing
            // the expensive reflection on the Color class. The lookup here is case-insensitive.
            // This method will throw if the name isn't valid.
            color = CSSColor.fromString(value).getColor();
        }

        return color;
    }

    /**
     * Interprets a string as a color value.
     *
     * @param value One of the following forms:
     * <ul>
     * <li>0xdddddddd - 8 hexadecimal digits, specifying 8 bits each of red,
     * green, and blue, followed by 8 bits of alpha.</li>
     * <li>#dddddd - 6 hexadecimal digits, specifying 8 bits each of red,
     * green, and blue.</li>
     * <li>Any of the names of the static colors in the Java {@link Color} class.</li>
     * <li>Any of the CSS3/X11 color names from here:
     * <a href="http://www.w3.org/TR/css3-color/">http://www.w3.org/TR/css3-color/</a>
     * (except the Java color values will be used for the standard Java names).</li>
     * <li>null - case-insensitive</li>
     * </ul>
     * @return A {@link Color} on successful decoding
     * @throws NumberFormatException if the value in the first two cases
     * contains illegal hexadecimal digits.
     * @throws IllegalArgumentException if the value is not in one of the
     * formats listed above.
     * @see #decodeColor(String, String)
     * @see CSSColor
     */
    public static Color decodeColor(final String value) throws NumberFormatException {
        return decodeColor(value, null);
    }

    /**
     * Generate a full color value given the RGB value along with the alpha
     * (opacity) value.
     *
     * @param rgb The 24-bit red, green, and blue value.
     * @param alpha The opacity value (0.0 - 1.0).
     * @return The full color value from these two parts.
     */
    public static Color getColor(final int rgb, final float alpha) {
        float red = ((rgb >> TWO_BYTES) & EIGHT_BITS) / EIGHT_BIT_FLOAT;
        float green = ((rgb >> ONE_BYTE) & EIGHT_BITS) / EIGHT_BIT_FLOAT;
        float blue = (rgb & EIGHT_BITS) / EIGHT_BIT_FLOAT;

        return new Color(red, green, blue, alpha);
    }

    /**
     * Interpret a string as a {@link Paint} value.
     *
     * @param value Either (a) One of the
     * {@linkplain GraphicsUtilities#decodeColor color values recognized by
     * Pivot} or (b) A {@linkplain GraphicsUtilities#decodePaint(Dictionary)
     * JSON dictionary describing a Paint value}.
     * @return The decoded paint value.
     * @throws IllegalArgumentException if the given value is {@code null} or
     * empty or there is a problem decoding the value.
     */
    public static Paint decodePaint(final String value) {
        Utils.checkNullOrEmpty(value, "paint");

        Paint paint;
        if (value.startsWith("#") || value.startsWith("0x") || value.startsWith("0X")) {
            paint = decodeColor(value);
        } else {
            try {
                paint = decodePaint(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        }

        return paint;
    }

    /**
     * Interpret a dictionary as a {@link Paint} value.
     *
     * @param dictionary A dictionary containing a key {@value #PAINT_TYPE_KEY}
     * and further elements according to its value:
     * <ul>
     * <li><b>solid_color</b> - key {@value #COLOR_KEY} with value being any of the
     * {@linkplain GraphicsUtilities#decodeColor color values recognized by
     * Pivot}</li>
     * <li><b>gradient</b> - keys {@value #START_X_KEY}, {@value #START_Y_KEY},
     * {@value #END_X_KEY}, {@value #END_Y_KEY} (values are coordinates),
     * {@value #START_COLOR_KEY}, {@value #END_COLOR_KEY} (values are
     * {@linkplain GraphicsUtilities#decodeColor colors})</li>
     * <li><b>linear_gradient</b> - keys {@value #START_X_KEY},
     * {@value #START_Y_KEY}, {@value #END_X_KEY}, {@value #END_Y_KEY}
     * (coordinates), {@value #STOPS_KEY} (a list of dictionaries with keys
     * {@value #OFFSET_KEY} (a number in [0,1]) and {@value #COLOR_KEY})</li>
     * <li><b>radial_gradient</b> - keys {@value #CENTER_X_KEY},
     * {@value #CENTER_Y_KEY} (coordinates), {@value #RADIUS_KEY} (a number),
     * {@value #STOPS_KEY} (a list of dictionaries with keys
     * {@value #OFFSET_KEY} and {@value #COLOR_KEY})</li>
     * </ul>
     * @return The fully decoded paint value.
     * @throws IllegalArgumentException if there is no paint type key found.
     */
    public static Paint decodePaint(final Dictionary<String, ?> dictionary) {
        Utils.checkNull(dictionary, "paint dictionary");

        String paintType = JSON.get(dictionary, PAINT_TYPE_KEY);
        if (paintType == null) {
            throw new IllegalArgumentException(PAINT_TYPE_KEY + " is required.");
        }

        Paint paint;
        float startX, startY;
        float endX, endY;

        PaintType pType = PaintType.valueOf(paintType.toUpperCase(Locale.ENGLISH));
        switch (pType) {
            case SOLID_COLOR:
                paint = decodeColor((String) JSON.get(dictionary, COLOR_KEY));
                break;

            case GRADIENT:
                startX = JSON.getFloat(dictionary, START_X_KEY);
                startY = JSON.getFloat(dictionary, START_Y_KEY);
                endX = JSON.getFloat(dictionary, END_X_KEY);
                endY = JSON.getFloat(dictionary, END_Y_KEY);
                Color startColor = decodeColor((String) JSON.get(dictionary, START_COLOR_KEY));
                Color endColor = decodeColor((String) JSON.get(dictionary, END_COLOR_KEY));
                paint = new GradientPaint(startX, startY, startColor, endX, endY, endColor);
                break;

            case LINEAR_GRADIENT:
            case RADIAL_GRADIENT:
                @SuppressWarnings("unchecked")
                List<Dictionary<String, ?>> stops = (List<Dictionary<String, ?>>) JSON.get(dictionary, STOPS_KEY);

                int n = stops.getLength();
                float[] fractions = new float[n];
                Color[] colors = new Color[n];
                for (int i = 0; i < n; i++) {
                    Dictionary<String, ?> stop = stops.get(i);

                    float offset = JSON.getFloat(stop, OFFSET_KEY);
                    fractions[i] = offset;

                    Color color = decodeColor((String) JSON.get(stop, COLOR_KEY));
                    colors[i] = color;
                }

                if (pType == PaintType.LINEAR_GRADIENT) {
                    startX = JSON.getFloat(dictionary, START_X_KEY);
                    startY = JSON.getFloat(dictionary, START_Y_KEY);
                    endX = JSON.getFloat(dictionary, END_X_KEY);
                    endY = JSON.getFloat(dictionary, END_Y_KEY);

                    paint = new LinearGradientPaint(startX, startY, endX, endY, fractions, colors);
                } else {
                    float centerX = JSON.getFloat(dictionary, CENTER_X_KEY);
                    float centerY = JSON.getFloat(dictionary, CENTER_Y_KEY);
                    float radius = JSON.getFloat(dictionary, RADIUS_KEY);

                    paint = new RadialGradientPaint(centerX, centerY, radius, fractions, colors);
                }
                break;

            default:
                throw new UnsupportedOperationException("Paint type " + paintType + " is not supported.");
        }

        return paint;
    }

    /**
     * Set the default font rendering hints for the given graphics object.
     *
     * @param graphics          The graphics object to initialize.
     * @param fontRenderContext The source of the font rendering hints.
     */
    public static void setFontRenderingHints(final Graphics2D graphics, final FontRenderContext fontRenderContext) {
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            fontRenderContext.getAntiAliasingHint());
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
            fontRenderContext.getFractionalMetricsHint());
    }

    /**
     * Set the context in the given graphics environment for subsequent font drawing.
     *
     * @param graphics          The graphics context.
     * @param fontRenderContext The font rendering context used to get the font drawing hints.
     * @param font              The font to use.
     * @param color             The foreground color for the text.
     */
    public static void prepareForText(final Graphics2D graphics, final FontRenderContext fontRenderContext,
            final Font font, final Color color) {
        setFontRenderingHints(graphics, fontRenderContext);

        graphics.setFont(font);
        graphics.setColor(color);
    }

    /**
     * Prepare for text rendering by getting the platform font render context and then setting the default
     * rendering hints in the graphics object.
     *
     * @param  graphics The graphics object to prepare.
     * @return          The {@link Platform#getFontRenderContext} value.
     */
    public static FontRenderContext prepareForText(final Graphics2D graphics) {
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();

        setFontRenderingHints(graphics, fontRenderContext);

        return fontRenderContext;
    }

    /**
     * Set the context in the given graphics environment for subsequent font drawing and return
     * the font render context.
     *
     * @param graphics  The graphics context.
     * @param font      The font to use.
     * @param color     The foreground color for the text.
     * @return          The font render context for the platform.
     */
    public static FontRenderContext prepareForText(final Graphics2D graphics, final Font font, final Color color) {
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();
        prepareForText(graphics, fontRenderContext, font, color);
        return fontRenderContext;
    }

    /**
     * Calculate the average character bounds for the given font.
     * <p> This bounds is the width of the "missing glyph code" and
     * the maximum character height of any glyph in the font.
     *
     * @param font The font in question.
     * @return The bounding rectangle to use for the average character size.
     * @see Platform#getFontRenderContext
     */
    public static Dimensions getAverageCharacterSize(final Font font) {
        int missingGlyphCode = font.getMissingGlyphCode();
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();

        GlyphVector missingGlyphVector = font.createGlyphVector(fontRenderContext,
            new int[] {missingGlyphCode});
        Rectangle2D textBounds = missingGlyphVector.getLogicalBounds();

        Rectangle2D maxCharBounds = font.getMaxCharBounds(fontRenderContext);
        return new Dimensions(
            (int) Math.ceil(textBounds.getWidth()),
            (int) Math.ceil(maxCharBounds.getHeight())
          );
    }

    /**
     * Get a caret rectangle from the given attributed text.
     *
     * @param caret The location within the text where the caret should be located.
     * @param text  The attributed text iterator.
     * @param leftOffset Horizontal offset within the control of the text (to add into the position).
     * @param topOffset Same for vertical offset.
     * @return The resulting rectangle for the caret.
     */
    public static Rectangle getCaretRectangle(final TextHitInfo caret, final AttributedCharacterIterator text,
            final int leftOffset, final int topOffset) {
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();
        TextLayout layout = new TextLayout(text, fontRenderContext);
        Shape caretShape = layout.getCaretShape(caret);
        Rectangle caretRect = caretShape.getBounds();
        caretRect.translate(leftOffset, topOffset + (int) Math.ceil(layout.getAscent() + layout.getDescent()));
        return caretRect;
    }

    /**
     * Draw borders around a rectangular area.
     *
     * @param graphics The graphics area to draw in.
     * @param borders The borders specification.
     * @param top The top coordinate (typically 0)
     * @param left The left coordinate (typically 0)
     * @param bottom The bottom interior coordinate (height - 1)
     * @param right The right interior coordinate (width - 1)
     */
    public static void drawBorders(final Graphics2D graphics, final Borders borders, final int top, final int left,
            final int bottom, final int right) {
        // The single line/object cases, or the first of the multiple line cases
        switch (borders) {
            default:
            case NONE:
                break;
            case ALL:
                graphics.drawRect(left, top, right, bottom);
                break;
            case TOP:
            case TOP_BOTTOM:
                // The top here
                graphics.drawLine(left, top, right, top);
                break;
            case BOTTOM:
                // The bottom here
                graphics.drawLine(left, bottom, right, bottom);
                break;
            case LEFT:
            case LEFT_RIGHT:
            case LEFT_TOP:
            case LEFT_BOTTOM:
            case NOT_RIGHT:
            case NOT_BOTTOM:
            case NOT_TOP:
                // The left here
                graphics.drawLine(0, 0, 0, bottom);
                break;
            case RIGHT:
            case RIGHT_TOP:
            case RIGHT_BOTTOM:
            case NOT_LEFT:
                // The right here
                graphics.drawLine(right, 0, right, bottom);
                break;
        }

        // The second of the double/triple line cases
        switch (borders) {
            case LEFT_RIGHT:
            case NOT_BOTTOM:
            case NOT_TOP:
                // The right side now
                graphics.drawLine(right, top, right, bottom);
                break;
            case TOP_BOTTOM:
            case LEFT_BOTTOM:
            case RIGHT_BOTTOM:
            case NOT_LEFT:
                // The bottom now
                graphics.drawLine(left, bottom, right, bottom);
                break;
            case LEFT_TOP:
            case RIGHT_TOP:
            case NOT_RIGHT:
                // The top now
                graphics.drawLine(left, top, right, top);
                break;
            default:
                break;
        }

        // Now the third of the triple line cases
        switch (borders) {
            case NOT_RIGHT:
            case NOT_TOP:
                // The bottom now
                graphics.drawLine(left, bottom, right, bottom);
                break;
            case NOT_LEFT:
            case NOT_BOTTOM:
                // The top now
                graphics.drawLine(left, top, right, top);
                break;
            default:
                break;
        }
    }
}

