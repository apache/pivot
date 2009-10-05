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
import java.awt.geom.AffineTransform;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Orientation;


/**
 * Contains utility methods dealing with the Java2D API.
 */
public final class GraphicsUtilities {
    /**
     * Enumeration representing a paint type.
     */
    public enum PaintType {
        SOLID_COLOR,
        GRADIENT,
        LINEAR_GRADIENT,
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

    public static final String NAME_KEY = "name";
    public static final String SIZE_KEY = "size";
    public static final String BOLD_KEY = "bold";
    public static final String ITALIC_KEY = "italic";

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
            case HORIZONTAL:
                graphics.fillRect(x, y, length, thickness);
                break;

            case VERTICAL:
                graphics.fillRect(x, y, thickness, length);
                break;
            }
        }
    }

    /**
     * Draws a rectangle with a thickness of 1 pixel at the specified
     * coordinates whose <u>outer border</u> is the specified width and height.
     * In other words, the distance from the left edge of the leftmost pixel to
     * the left edge of the rightmost pixel is <tt>width - 1</tt>.
     * <p>
     * This method provides more reliable pixel rounding behavior than
     * <tt>java.awt.Graphics#drawRect</tt> when scaling is applied because this
     * method does not stroke the shape but instead explicitly fills the
     * desired pixels with the graphics context's paint. For this reason, and
     * because Pivot supports scaling the display host, it is recommended that
     * skins use this method over <tt>java.awt.Graphics#drawRect</tt>.
     *
     * @param graphics
     * The graphics context that will be used to perform the operation.
     *
     * @param x
     * The x-coordinate of the upper-left corner of the rectangle.
     *
     * @param y
     * The y-coordinate of the upper-left corner of the rectangle.
     *
     * @param width
     * The <i>outer width</i> of the rectangle.
     *
     * @param height
     * The <i>outer height</i> of the rectangle.
     */
    public static final void drawRect(final Graphics2D graphics, final int x, final int y,
        final int width, final int height) {
        drawRect(graphics, x, y, width, height, 1);
    }

    /**
     * Draws a rectangle with the specified thickness at the specified
     * coordinates whose <u>outer border</u> is the specified width and height.
     * In other words, the distance from the left edge of the leftmost pixel to
     * the left edge of the rightmost pixel is <tt>width - thickness</tt>.
     * <p>
     * This method provides more reliable pixel rounding behavior than
     * <tt>java.awt.Graphics#drawRect</tt> when scaling is applied because this
     * method does not stroke the shape but instead explicitly fills the
     * desired pixels with the graphics context's paint. For this reason, and
     * because Pivot supports scaling the display host, it is recommended that
     * skins use this method over <tt>java.awt.Graphics#drawRect</tt>.
     *
     * @param graphics
     * The graphics context that will be used to perform the operation.
     *
     * @param x
     * The x-coordinate of the upper-left corner of the rectangle.
     *
     * @param y
     * The y-coordinate of the upper-left corner of the rectangle.
     *
     * @param width
     * The <i>outer width</i> of the rectangle.
     *
     * @param height
     * The <i>outer height</i> of the rectangle.
     *
     * @param thickness
     * The thickness of each edge.
     */
    public static final void drawRect(final Graphics2D graphics, final int x, final int y,
        final int width, final int height, final int thickness) {
        Graphics2D rectGraphics = graphics;

        if ((graphics.getTransform().getType() & AffineTransform.TYPE_MASK_SCALE) != 0) {
            rectGraphics = (Graphics2D)graphics.create();
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

    public static Color decodeColor(String value) throws NumberFormatException {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        value = value.toLowerCase();

        int rgb;
        float alpha;
        if (value.startsWith("0x")) {
            value = value.substring(2);
            if (value.length() != 8) {
                throw new IllegalArgumentException();
            }

            rgb = Integer.parseInt(value.substring(0, 6), 16);
            alpha = Integer.parseInt(value.substring(6, 8), 16) / 255f;
        } else if (value.startsWith("#")) {
            value = value.substring(1);
            if (value.length() != 6) {
                throw new IllegalArgumentException();
            }

            rgb = Integer.parseInt(value, 16);
            alpha = 1.0f;
        } else {
            throw new IllegalArgumentException();
        }

        float red = ((rgb >> 16) & 0xff) / 255f;
        float green = ((rgb >> 8) & 0xff) / 255f;
        float blue = (rgb >> 0 & 0xff) / 255f;

        Color color = new Color(red, green, blue, alpha);

        return color;
    }

    public static Paint decodePaint(String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        Paint paint;
        if (value.startsWith("#")
            || value.startsWith("0x")
            || value.startsWith("0X")) {
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

    @SuppressWarnings("unchecked")
    public static Paint decodePaint(Dictionary<String, ?> value) {
        String paintType = JSONSerializer.getString(value, PAINT_TYPE_KEY);
        if (paintType == null) {
            throw new IllegalArgumentException(PAINT_TYPE_KEY + " is required.");
        }

        Paint paint;
        switch(PaintType.valueOf(paintType.toUpperCase())) {
            case SOLID_COLOR: {
                String color = JSONSerializer.getString(value, COLOR_KEY);
                paint = decodeColor(color);
                break;
            }

            case GRADIENT: {
                float startX = JSONSerializer.getFloat(value, START_X_KEY);
                float startY = JSONSerializer.getFloat(value, START_Y_KEY);
                float endX = JSONSerializer.getFloat(value, END_X_KEY);
                float endY = JSONSerializer.getFloat(value, END_Y_KEY);
                Color startColor = Color.decode(JSONSerializer.getString(value, START_COLOR_KEY));
                Color endColor = Color.decode(JSONSerializer.getString(value, END_COLOR_KEY));
                paint = new GradientPaint(startX, startY, startColor, endX, endY, endColor);
                break;
            }

            case LINEAR_GRADIENT: {
                float startX = JSONSerializer.getFloat(value, START_X_KEY);
                float startY = JSONSerializer.getFloat(value, START_Y_KEY);
                float endX = JSONSerializer.getFloat(value, END_X_KEY);
                float endY = JSONSerializer.getFloat(value, END_Y_KEY);

                List<Dictionary<String, ?>> stops =
                    (List<Dictionary<String, ?>>)JSONSerializer.getList(value, STOPS_KEY);

                int n = stops.getLength();
                float[] fractions = new float[n];
                Color[] colors = new Color[n];
                for (int i = 0; i < n; i++) {
                    Dictionary<String, ?> stop = stops.get(i);

                    float offset = JSONSerializer.getFloat(stop, OFFSET_KEY);
                    fractions[i] = offset;

                    Color color = Color.decode(JSONSerializer.getString(stop, COLOR_KEY));
                    colors[i] = color;
                }

                paint = new LinearGradientPaint(startX, startY, endX, endY, fractions, colors);
                break;
            }

            case RADIAL_GRADIENT: {
                float centerX = JSONSerializer.getFloat(value, CENTER_X_KEY);
                float centerY = JSONSerializer.getFloat(value, CENTER_Y_KEY);
                float radius = JSONSerializer.getFloat(value, RADIUS_KEY);

                List<Dictionary<String, ?>> stops =
                    (List<Dictionary<String, ?>>)JSONSerializer.getList(value, STOPS_KEY);

                int n = stops.getLength();
                float[] fractions = new float[n];
                Color[] colors = new Color[n];
                for (int i = 0; i < n; i++) {
                    Dictionary<String, ?> stop = stops.get(i);

                    float offset = JSONSerializer.getFloat(stop, OFFSET_KEY);
                    fractions[i] = offset;

                    Color color = Color.decode(JSONSerializer.getString(stop, COLOR_KEY));
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

    public static Font decodeFont(String value) {
        Font font;
        if (value.startsWith("{")) {
            try {
                font = decodeFont(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            font = Font.decode(value);
        }

        return font;
    }

    public static Font decodeFont(Dictionary<String, ?> value) {
        Font font = Theme.getTheme().getFont();

        String name;
        if (value.containsKey(NAME_KEY)) {
            name = (String)value.get(NAME_KEY);
        } else {
            name = font.getName();
        }

        int size;
        if (value.containsKey(SIZE_KEY)) {
            size = (Integer)value.get(SIZE_KEY);
        } else {
            size = font.getSize();
        }

        int style = font.getStyle();

        if (value.containsKey(BOLD_KEY)) {
            boolean bold = (Boolean)value.get(BOLD_KEY);

            if (bold) {
                style |= Font.BOLD;
            } else {
                style &= ~Font.BOLD;
            }
        }

        if (value.containsKey(ITALIC_KEY)) {
            boolean italic = (Boolean)value.get(ITALIC_KEY);

            if (italic) {
                style |= Font.ITALIC;
            } else {
                style &= ~Font.ITALIC;
            }
        }

        return new Font(name, style, size);
    }
}
