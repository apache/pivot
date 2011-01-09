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
package org.apache.pivot.scene;

import java.util.Locale;
import java.util.Map;

import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.io.SerializationException;
import org.apache.pivot.json.JSONSerializer;

/**
 * Class representing a color.
 */
public class Color {
    public final int red;
    public final int green;
    public final int blue;
    public final int alpha;

    public static final String RED_KEY = "red";
    public static final String GREEN_KEY = "green";
    public static final String BLUE_KEY = "blue";
    public static final String ALPHA_KEY = "alpha";

    public static final Color WHITE = new Color(0xff, 0xff, 0xff);
    public static final Color SILVER = new Color(0xc0, 0xc0, 0xc0);
    public static final Color GRAY = new Color(0x80, 0x80, 0x80);
    public static final Color BLACK = new Color(0x00, 0x00, 0x00);
    public static final Color RED = new Color(0xff, 0x00, 0x00);
    public static final Color MAROON = new Color(0x80, 0x00, 0x00);
    public static final Color YELLOW = new Color(0xff, 0xff, 0x00);
    public static final Color OLIVE = new Color(0x80, 0x80, 0x00);
    public static final Color LIME = new Color(0x00, 0xff, 0x00);
    public static final Color GREEN = new Color(0x00, 0x80, 0x00);
    public static final Color AQUA = new Color(0x00, 0xff, 0xff);
    public static final Color TEAL = new Color(0x00, 0x80, 0x80);
    public static final Color BLUE = new Color(0x00, 0x00, 0xff);
    public static final Color NAVY = new Color(0x00, 0x00, 0x80);
    public static final Color FUSCHIA = new Color(0xff, 0x00, 0xff);
    public static final Color PURPLE = new Color(0x80, 0x00, 0x80);

    public Color(Color color) {
        this(color.red, color.green, color.blue, color.alpha);
    }

    public Color(int red, int green, int blue) {
        this(red, green, blue, 0xff);
    }

    public Color(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public Color(int color) {
        alpha = color & 0xff;
        blue = (color >>= 8) & 0xff;
        green = (color >>= 8) & 0xff;
        red = (color >>= 8) & 0xff;
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof Color) {
            Color color = (Color)object;
            equals = (red == color.red
                && green == color.green
                && blue == color.blue
                && alpha == color.alpha);
        }

        return equals;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + red;
        result = prime * result + green;
        result = prime * result + blue;
        result = prime * result + alpha;

        return result;
    }

    @Override
    public String toString() {
        return getClass().getName() + " 0x" + toInt();
    }

    public int toInt() {
        return (red << 24) | (green << 16) | (blue << 8) | alpha;
    }

    public static Color decode(String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        value = value.toLowerCase(Locale.ENGLISH);

        Color color;
        if (value.startsWith("0x")) {
            value = value.substring(2);
            if (value.length() != 8) {
                throw new IllegalArgumentException();
            }

            color = new Color(Integer.parseInt(value, 16));
        } else if (value.startsWith("#")) {
            value = value.substring(1);
            if (value.length() != 6) {
                throw new IllegalArgumentException();
            }

            color = new Color((Integer.parseInt(value, 16) << 8) & 0xff);
        } else if (value.startsWith("{")) {
            Map<String, ?> map;
            try {
                map = JSONSerializer.parseMap(value);
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }

            int red = BeanAdapter.getInt(map, RED_KEY);
            int green = BeanAdapter.getInt(map, GREEN_KEY);
            int blue = BeanAdapter.getInt(map, BLUE_KEY);
            int alpha = BeanAdapter.getInt(map, ALPHA_KEY);

            color = new Color(red, green, blue, alpha);
        } else {
            if (value.length() > 0
                && Character.isLowerCase(value.charAt(0))) {
                value = BeanAdapter.toAllCaps(value);
            }

            try {
                color = (Color)Color.class.getDeclaredField(value).get(null);
            } catch (Exception exception) {
                throw new IllegalArgumentException("\"" + value + "\" is not a valid color constant.");
            }
        }

        return color;
    }

    // TODO Add static methods to darken/brighten, etc.
}
