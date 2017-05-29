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
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
        SOLID_COLOR, GRADIENT, LINEAR_GRADIENT, RADIAL_GRADIENT
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

    public static Map<String, Color> CSS3ColorMap = new HashMap<>();
    
    static {
        // Initialize the table of CSS3/X11 color names and values
        // from here: http://www.w3.org/TR/css3-color/
        // but with the Java names (such as "black", "blue", etc.) omitted
        CSS3ColorMap.put("aliceblue",            new Color(240,248,255));
        CSS3ColorMap.put("antiquewhite",         new Color(250,235,215));
        CSS3ColorMap.put("aqua",                 new Color(  0,255,255));
        CSS3ColorMap.put("aquamarine",           new Color(127,255,212));
        CSS3ColorMap.put("azure",                new Color(240,255,255));
        CSS3ColorMap.put("beige",                new Color(245,245,220));
        CSS3ColorMap.put("bisque",               new Color(255,228,196));
        CSS3ColorMap.put("blanchedalmond",       new Color(255,235,205));
        CSS3ColorMap.put("blueviolet",           new Color(138, 43,226));
        CSS3ColorMap.put("brown",                new Color(165, 42, 42));
        CSS3ColorMap.put("burlywood",            new Color(222,184,135));
        CSS3ColorMap.put("cadetblue",            new Color( 95,158,160));
        CSS3ColorMap.put("chartreuse",           new Color(127,255,  0));
        CSS3ColorMap.put("chocolate",            new Color(210,105, 30));
        CSS3ColorMap.put("coral",                new Color(255,127, 80));
        CSS3ColorMap.put("cornflowerblue",       new Color(100,149,237));
        CSS3ColorMap.put("cornsilk",             new Color(255,248,220));
        CSS3ColorMap.put("crimson",              new Color(220, 20, 60));
        CSS3ColorMap.put("darkblue",             new Color(  0,  0,139));
        CSS3ColorMap.put("darkcyan",             new Color(  0,139,139));
        CSS3ColorMap.put("darkgoldenrod",        new Color(184,134, 11));
        CSS3ColorMap.put("darkgreen",            new Color(  0,100,  0));
        CSS3ColorMap.put("darkkhaki",            new Color(189,183,107));
        CSS3ColorMap.put("darkmagenta",          new Color(139,  0,139));
        CSS3ColorMap.put("darkolivegreen",       new Color( 85,107, 47));
        CSS3ColorMap.put("darkorange",           new Color(255,140,  0));
        CSS3ColorMap.put("darkorchid",           new Color(153, 50,204));
        CSS3ColorMap.put("darkred",              new Color(139,  0,  0));
        CSS3ColorMap.put("darksalmon",           new Color(233,150,122));
        CSS3ColorMap.put("darkseagreen",         new Color(143,188,143));
        CSS3ColorMap.put("darkslateblue",        new Color( 72, 61,139));
        CSS3ColorMap.put("darkslategray",        new Color( 47, 79, 79));
        CSS3ColorMap.put("darkslategrey",        new Color( 47, 79, 79));
        CSS3ColorMap.put("darkturquoise",        new Color(  0,206,209));
        CSS3ColorMap.put("darkviolet",           new Color(148,  0,211));
        CSS3ColorMap.put("deeppink",             new Color(255, 20,147));
        CSS3ColorMap.put("deepskyblue",          new Color(  0,191,255));
        CSS3ColorMap.put("dimgray",              new Color(105,105,105));
        CSS3ColorMap.put("dimgrey",              new Color(105,105,105));
        CSS3ColorMap.put("dodgerblue",           new Color( 30,144,255));
        CSS3ColorMap.put("firebrick",            new Color(178, 34, 34));
        CSS3ColorMap.put("floralwhite",          new Color(255,250,240));
        CSS3ColorMap.put("forestgreen",          new Color( 34,139, 34));
        CSS3ColorMap.put("fuchsia",              new Color(255,  0,255));
        CSS3ColorMap.put("gainsboro",            new Color(220,220,220));
        CSS3ColorMap.put("ghostwhite",           new Color(248,248,255));
        CSS3ColorMap.put("gold",                 new Color(255,215,  0));
        CSS3ColorMap.put("goldenrod",            new Color(218,165, 32));
        CSS3ColorMap.put("greenyellow",          new Color(173,255, 47));
        CSS3ColorMap.put("honeydew",             new Color(240,255,240));
        CSS3ColorMap.put("hotpink",              new Color(255,105,180));
        CSS3ColorMap.put("indianred",            new Color(205, 92, 92));
        CSS3ColorMap.put("indigo",               new Color( 75,  0,130));
        CSS3ColorMap.put("ivory",                new Color(255,255,240));
        CSS3ColorMap.put("khaki",                new Color(240,230,140));
        CSS3ColorMap.put("lavender",             new Color(230,230,250));
        CSS3ColorMap.put("lavenderblush",        new Color(255,240,245));
        CSS3ColorMap.put("lawngreen",            new Color(124,252,  0));
        CSS3ColorMap.put("lemonchiffon",         new Color(255,250,205));
        CSS3ColorMap.put("lightblue",            new Color(173,216,230));
        CSS3ColorMap.put("lightcoral",           new Color(240,128,128));
        CSS3ColorMap.put("lightcyan",            new Color(224,255,255));
        CSS3ColorMap.put("lightgoldenrodyellow", new Color(250,250,210));
        CSS3ColorMap.put("lightgreen",           new Color(144,238,144));
        CSS3ColorMap.put("lightpink",            new Color(255,182,193));
        CSS3ColorMap.put("lightsalmon",          new Color(255,160,122));
        CSS3ColorMap.put("lightseagreen",        new Color( 32,178,170));
        CSS3ColorMap.put("lightskyblue",         new Color(135,206,250));
        CSS3ColorMap.put("lightslategray",       new Color(119,136,153));
        CSS3ColorMap.put("lightslategrey",       new Color(119,136,153));
        CSS3ColorMap.put("lightsteelblue",       new Color(176,196,222));
        CSS3ColorMap.put("lightyellow",          new Color(255,255,224));
        CSS3ColorMap.put("lime",                 new Color(  0,255,  0));
        CSS3ColorMap.put("limegreen",            new Color( 50,205, 50));
        CSS3ColorMap.put("linen",                new Color(250,240,230));
        CSS3ColorMap.put("maroon",               new Color(128,  0,  0));
        CSS3ColorMap.put("mediumaquamarine",     new Color(102,205,170));
        CSS3ColorMap.put("mediumblue",           new Color(  0,  0,205));
        CSS3ColorMap.put("mediumorchid",         new Color(186, 85,211));
        CSS3ColorMap.put("mediumpurple",         new Color(147,112,219));
        CSS3ColorMap.put("mediumseagreen",       new Color( 60,179,113));
        CSS3ColorMap.put("mediumslateblue",      new Color(123,104,238));
        CSS3ColorMap.put("mediumspringgreen",    new Color(  0,250,154));
        CSS3ColorMap.put("mediumturquoise",      new Color( 72,209,204));
        CSS3ColorMap.put("mediumvioletred",      new Color(199, 21,133));
        CSS3ColorMap.put("midnightblue",         new Color( 25, 25,112));
        CSS3ColorMap.put("mintcream",            new Color(245,255,250));
        CSS3ColorMap.put("mistyrose",            new Color(255,228,225));
        CSS3ColorMap.put("moccasin",             new Color(255,228,181));
        CSS3ColorMap.put("navajowhite",          new Color(255,222,173));
        CSS3ColorMap.put("navy",                 new Color(  0,  0,128));
        CSS3ColorMap.put("oldlace",              new Color(253,245,230));
        CSS3ColorMap.put("olive",                new Color(128,128,  0));
        CSS3ColorMap.put("olivedrab",            new Color(107,142, 35));
        CSS3ColorMap.put("orangered",            new Color(255, 69,  0));
        CSS3ColorMap.put("orchid",               new Color(218,112,214));
        CSS3ColorMap.put("palegoldenrod",        new Color(238,232,170));
        CSS3ColorMap.put("palegreen",            new Color(152,251,152));
        CSS3ColorMap.put("paleturquoise",        new Color(175,238,238));
        CSS3ColorMap.put("palevioletred",        new Color(219,112,147));
        CSS3ColorMap.put("papayawhip",           new Color(255,239,213));
        CSS3ColorMap.put("peachpuff",            new Color(255,218,185));
        CSS3ColorMap.put("peru",                 new Color(205,133, 63));
        CSS3ColorMap.put("plum",                 new Color(221,160,221));
        CSS3ColorMap.put("powderblue",           new Color(176,224,230));
        CSS3ColorMap.put("purple",               new Color(128,  0,128));
        CSS3ColorMap.put("rosybrown",            new Color(188,143,143));
        CSS3ColorMap.put("royalblue",            new Color( 65,105,225));
        CSS3ColorMap.put("saddlebrown",          new Color(139, 69, 19));
        CSS3ColorMap.put("salmon",               new Color(250,128,114));
        CSS3ColorMap.put("sandybrown",           new Color(244,164, 96));
        CSS3ColorMap.put("seagreen",             new Color( 46,139, 87));
        CSS3ColorMap.put("seashell",             new Color(255,245,238));
        CSS3ColorMap.put("sienna",               new Color(160, 82, 45));
        CSS3ColorMap.put("silver",               new Color(192,192,192));
        CSS3ColorMap.put("skyblue",              new Color(135,206,235));
        CSS3ColorMap.put("slateblue",            new Color(106, 90,205));
        CSS3ColorMap.put("slategray",            new Color(112,128,144));
        CSS3ColorMap.put("slategrey",            new Color(112,128,144));
        CSS3ColorMap.put("snow",                 new Color(255,250,250));
        CSS3ColorMap.put("springgreen",          new Color(  0,255,127));
        CSS3ColorMap.put("steelblue",            new Color( 70,130,180));
        CSS3ColorMap.put("tan",                  new Color(210,180,140));
        CSS3ColorMap.put("teal",                 new Color(  0,128,128));
        CSS3ColorMap.put("thistle",              new Color(216,191,216));
        CSS3ColorMap.put("tomato",               new Color(255, 99, 71));
        CSS3ColorMap.put("turquoise",            new Color( 64,224,208));
        CSS3ColorMap.put("violet",               new Color(238,130,238));
        CSS3ColorMap.put("wheat",                new Color(245,222,179));
        CSS3ColorMap.put("whitesmoke",           new Color(245,245,245));
        CSS3ColorMap.put("yellowgreen",          new Color(154,205, 50));
    }

    private GraphicsUtilities() {
    }

    public static final void drawLine(final Graphics2D graphics, final int x, final int y,
        final int length, final Orientation orientation) {
        drawLine(graphics, x, y, length, orientation, 1);
    }

    public static final void drawLine(final Graphics2D graphics, final int x, final int y,
        final int length, final Orientation orientation, final int thickness) {
        if (length > 0 && thickness > 0) {
            switch (orientation) {
                case HORIZONTAL: {
                    graphics.fillRect(x, y, length, thickness);
                    break;
                }
                case VERTICAL: {
                    graphics.fillRect(x, y, thickness, length);
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    /**
     * Draws a rectangle with a thickness of 1 pixel at the specified
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
     * @param graphics The graphics context that will be used to perform the
     * operation.
     * @param x The x-coordinate of the upper-left corner of the rectangle.
     * @param y The y-coordinate of the upper-left corner of the rectangle.
     * @param width The <i>outer width</i> of the rectangle.
     * @param height The <i>outer height</i> of the rectangle.
     */
    public static final void drawRect(final Graphics2D graphics, final int x, final int y,
        final int width, final int height) {
        drawRect(graphics, x, y, width, height, 1);
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
     * @param graphics The graphics context that will be used to perform the
     * operation.
     * @param x The x-coordinate of the upper-left corner of the rectangle.
     * @param y The y-coordinate of the upper-left corner of the rectangle.
     * @param width The <i>outer width</i> of the rectangle.
     * @param height The <i>outer height</i> of the rectangle.
     * @param thickness The thickness of each edge.
     */
    public static final void drawRect(final Graphics2D graphics, final int x, final int y,
        final int width, final int height, final int thickness) {
        Graphics2D rectGraphics = graphics;

        if ((graphics.getTransform().getType() & AffineTransform.TYPE_MASK_SCALE) != 0) {
            rectGraphics = (Graphics2D) graphics.create();
            rectGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
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
     * (except the Java color names will be accepted first if there is a conflict).</li>
     * </ul>
     * @param argument A name for this color value (for the exception if it can't be decoded).
     * @return A {@link Color} on successful decoding
     * @throws NumberFormatException if the value in the first two cases
     * contains illegal hexadecimal digits.
     * @throws IllegalArgumentException if the value is not in one of the
     * formats listed above.
     */
    public static Color decodeColor(final String value, String argument) throws NumberFormatException {
        Utils.checkNullOrEmpty(value, argument == null ? "color" : argument);

        String valueLowercase = value.toLowerCase(Locale.ENGLISH);

        Color color;
        if (valueLowercase.startsWith("0x")) {
            valueLowercase = valueLowercase.substring(2);
            if (valueLowercase.length() != 8) {
                throw new IllegalArgumentException(
                    "Incorrect Color format.  Expecting exactly 8 digits after the '0x' prefix.");
            }

            int rgb = Integer.parseInt(valueLowercase.substring(0, 6), 16);
            float alpha = Integer.parseInt(valueLowercase.substring(6, 8), 16) / 255f;

            color = getColor(rgb, alpha);
        } else if (valueLowercase.startsWith("#")) {
            valueLowercase = valueLowercase.substring(1);
            if (valueLowercase.length() != 6) {
                throw new IllegalArgumentException(
                    "Incorrect Color format.  Expecting exactly 6 digits after the '#' prefix.");
            }

            int rgb = Integer.parseInt(valueLowercase, 16);
            float alpha = 1.0f;

            color = getColor(rgb, alpha);
        } else {
            try {
                color = (Color) Color.class.getDeclaredField(valueLowercase).get(null);
            } catch (Exception exception) {
                // PIVOT-985: special case for two values (plus spelling variants)
                // that don't work with just a pure lower case name lookup, plus the
                // British spelling variants of the standard "gray".
                if (valueLowercase.equals("darkgray") || valueLowercase.equals("darkgrey")) {
                    color = Color.darkGray;
                } else if (valueLowercase.equals("lightgray") || valueLowercase.equals("lightgrey")) {
                    color = Color.lightGray;
                } else if (valueLowercase.equals("grey")) {
                    color = Color.gray;
                } else {
                    // Otherwise try to decode an X11/CSS3 color name
                    if ((color = CSS3ColorMap.get(valueLowercase)) != null) {
                        return color;
                    }
                    throw new IllegalArgumentException("\"" + valueLowercase
                        + "\" is not a valid color constant.");
                }
            }
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
     * (except the Java color names will be accepted first if there is a conflict).</li>
     * </ul>
     * @return A {@link Color} on successful decoding
     * @throws NumberFormatException if the value in the first two cases
     * contains illegal hexadecimal digits.
     * @throws IllegalArgumentException if the value is not in one of the
     * formats listed above.
     * @see #decodeColor(String, String)
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
    public static Color getColor(int rgb, float alpha) {
        float red = ((rgb >> 16) & 0xff) / 255f;
        float green = ((rgb >> 8) & 0xff) / 255f;
        float blue = (rgb >> 0 & 0xff) / 255f;

        return new Color(red, green, blue, alpha);
    }

    /**
     * Interpret a string as a {@link Paint} value
     *
     * @param value Either (a) One of the
     * {@linkplain GraphicsUtilities#decodeColor color values recognized by
     * Pivot} or (b) A {@linkplain GraphicsUtilities#decodePaint(Dictionary)
     * JSON dictionary describing a Paint value}.
     * @return The decoded paint value.
     * @throws IllegalArgumentException if the given value is {@code null} or
     * there is a problem decoding the value.
     */
    public static Paint decodePaint(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Cannot decode a null String.");
        }

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
     * Interpret a dictionary as a {@link Paint} value
     *
     * @param dictionary A dictionary containing a key {@value #PAINT_TYPE_KEY}
     * and further elements according to its value: <ul> <li><b>solid_color</b>
     * - key {@value #COLOR_KEY} with value being any of the
     * {@linkplain GraphicsUtilities#decodeColor color values recognized by
     * Pivot}</li> <li><b>gradient</b> - keys {@value #START_X_KEY},
     * {@value #START_Y_KEY}, {@value #END_X_KEY}, {@value #END_Y_KEY} (values
     * are coordinates), {@value #START_COLOR_KEY}, {@value #END_COLOR_KEY}
     * (values are {@linkplain GraphicsUtilities#decodeColor colors})</li>
     * <li><b>linear_gradient</b> - keys {@value #START_X_KEY},
     * {@value #START_Y_KEY}, {@value #END_X_KEY}, {@value #END_Y_KEY}
     * (coordinates), {@value #STOPS_KEY} (a list of dictionaries with keys
     * {@value #OFFSET_KEY} (a number in [0,1]) and {@value #COLOR_KEY})</li>
     * <li><b>radial_gradient</b> - keys {@value #CENTER_X_KEY},
     * {@value #CENTER_Y_KEY} (coordinates), {@value #RADIUS_KEY} (a number),
     * {@value #STOPS_KEY} (a list of dictionaries with keys
     * {@value #OFFSET_KEY} and {@value #COLOR_KEY})</li> </ul>
     * @return The fully decoded paint value.
     * @throws IllegalArgumentException if there is no paint type key found. 
     */
    public static Paint decodePaint(Dictionary<String, ?> dictionary) {
        String paintType = JSON.get(dictionary, PAINT_TYPE_KEY);
        if (paintType == null) {
            throw new IllegalArgumentException(PAINT_TYPE_KEY + " is required.");
        }

        Paint paint;
        switch (PaintType.valueOf(paintType.toUpperCase(Locale.ENGLISH))) {
            case SOLID_COLOR: {
                String color = JSON.get(dictionary, COLOR_KEY);
                paint = decodeColor(color);
                break;
            }

            case GRADIENT: {
                float startX = JSON.getFloat(dictionary, START_X_KEY);
                float startY = JSON.getFloat(dictionary, START_Y_KEY);
                float endX = JSON.getFloat(dictionary, END_X_KEY);
                float endY = JSON.getFloat(dictionary, END_Y_KEY);
                Color startColor = decodeColor((String) JSON.get(dictionary, START_COLOR_KEY));
                Color endColor = decodeColor((String) JSON.get(dictionary, END_COLOR_KEY));
                paint = new GradientPaint(startX, startY, startColor, endX, endY, endColor);
                break;
            }

            case LINEAR_GRADIENT: {
                float startX = JSON.getFloat(dictionary, START_X_KEY);
                float startY = JSON.getFloat(dictionary, START_Y_KEY);
                float endX = JSON.getFloat(dictionary, END_X_KEY);
                float endY = JSON.getFloat(dictionary, END_Y_KEY);

                @SuppressWarnings("unchecked")
                List<Dictionary<String, ?>> stops = (List<Dictionary<String, ?>>) JSON.get(
                    dictionary, STOPS_KEY);

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

                paint = new LinearGradientPaint(startX, startY, endX, endY, fractions, colors);
                break;
            }

            case RADIAL_GRADIENT: {
                float centerX = JSON.getFloat(dictionary, CENTER_X_KEY);
                float centerY = JSON.getFloat(dictionary, CENTER_Y_KEY);
                float radius = JSON.getFloat(dictionary, RADIUS_KEY);

                @SuppressWarnings("unchecked")
                List<Dictionary<String, ?>> stops = (List<Dictionary<String, ?>>) JSON.get(
                    dictionary, STOPS_KEY);

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

                paint = new RadialGradientPaint(centerX, centerY, radius, fractions, colors);
                break;
            }

            default: {
                throw new UnsupportedOperationException();
            }
        }

        return paint;
    }

    /**
     * Set the context in the given graphics environment for subsequent font drawing.
     *
     * @param graphics          The graphics context.
     * @param fontRenderContext The font rendering context used to get the font drawing hints.
     * @param font              The font to use.
     * @param color             The foreground color for the text.
     */
    public static void prepareForText(Graphics2D graphics, FontRenderContext fontRenderContext, Font font, Color color) {
        graphics.setFont(font);
        graphics.setColor(color);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, fontRenderContext.getAntiAliasingHint());
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fontRenderContext.getFractionalMetricsHint());
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
    public static Dimensions getAverageCharacterSize(Font font) {
        int missingGlyphCode = font.getMissingGlyphCode();
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();

        GlyphVector missingGlyphVector = font.createGlyphVector(fontRenderContext,
            new int[] { missingGlyphCode });
        Rectangle2D textBounds = missingGlyphVector.getLogicalBounds();

        Rectangle2D maxCharBounds = font.getMaxCharBounds(fontRenderContext);
        return new Dimensions(
            (int) Math.ceil(textBounds.getWidth()),
            (int) Math.ceil(maxCharBounds.getHeight())
          );
    }

}

