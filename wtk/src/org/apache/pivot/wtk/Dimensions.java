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

import java.io.Serializable;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Utils;

/**
 * Class representing the dimensions of an object.
 */
public final class Dimensions implements Serializable {
    private static final long serialVersionUID = -3644511824857807902L;

    public final int width;
    public final int height;

    public static final String WIDTH_KEY = "width";
    public static final String HEIGHT_KEY = "height";

    public static final Dimensions ZERO = new Dimensions(0);

    /**
     * Construct a "square" dimensions that has the same width
     * as height.
     *
     * @param side The width and height of this dimensions.
     */
    public Dimensions(final int side) {
        this.width = this.height = side;
    }

    public Dimensions(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    public Dimensions(final Dimensions dimensions) {
        Utils.checkNull(dimensions, "dimensions");

        this.width = dimensions.width;
        this.height = dimensions.height;
    }

    public Dimensions(final Dictionary<String, ?> dimensions) {
        Utils.checkNull(dimensions, "dimensions");

        width = dimensions.getInt(WIDTH_KEY, 0);
        height = dimensions.getInt(HEIGHT_KEY, 0);
    }

    public Dimensions(final Sequence<?> dimensions) {
        Utils.checkNull(dimensions, "dimensions");

        width = ((Number) dimensions.get(0)).intValue();
        height = ((Number) dimensions.get(1)).intValue();
    }

    /**
     * Expand this dimensions by the given amount (positive or
     * negative) in both width and height directions.
     *
     * @param factor The amount to add to/subtract from both the width and height.
     * @return The new dimensions with the changed values.
     */
    public Dimensions expand(final int factor) {
        return new Dimensions(width + factor, height + factor);
    }

    /**
     * Expand this dimensions by the given amounts (positive or
     * negative) separately in the width and height directions.
     *
     * @param widthDelta The amount to add to/subtract from the width.
     * @param heightDelta The amount to add to/subtract from the height.
     * @return The new dimensions with the changed values.
     */
    public Dimensions expand(final int widthDelta, final int heightDelta) {
        return new Dimensions(width + widthDelta, height + heightDelta);
    }

    /**
     * Expand this dimensions by the given {@link Insets} amounts
     * in the width and height directions.
     *
     * @param insets The padding amounts (width and height) to expand by.
     * @return The new dimensions with the changed values.
     */
    public Dimensions expand(final Insets insets) {
        return new Dimensions(width + insets.getWidth(), height + insets.getHeight());
    }

    @Override
    public boolean equals(final Object object) {
        boolean equals = false;

        if (object instanceof Dimensions) {
            Dimensions dimensions = (Dimensions) object;
            equals = (width == dimensions.width && height == dimensions.height);
        }

        return equals;
    }

    @Override
    public int hashCode() {
        return 31 * width + height;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + width + "x" + height + "]";
    }

    /**
     * Convert a string dimensions value to the object.
     * <p> The string can be in a variety of forms:
     * <ul>
     * <li>A JSON map like this: <pre>{ width:nnn, height:nnn }</pre></li>
     * <li>A JSON array list this: <pre>[ width, height ]</pre></li>
     * <li>A string formatted as: <pre>"widthXheight"</pre> (where the X is case-insensitive)</li>
     * <li>A simple comma-separated string with two numeric values: <pre>"width, height"</pre></li>
     * </ul>
     *
     * @param value The input string in one of these formats.
     * @return The parsed dimensions value if possible.
     * @throws IllegalArgumentException if the input value is null, empty, or cannot be parsed in
     * one of these forms.
     * @see #Dimensions(Dictionary)
     * @see #Dimensions(Sequence)
     * @see #Dimensions(int, int)
     */
    public static Dimensions decode(final String value) {
        Utils.checkNullOrEmpty(value, "dimensions");

        Dimensions dimensions;
        if (value.startsWith("{")) {
            try {
                dimensions = new Dimensions(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else if (value.startsWith("[")) {
            try {
                dimensions = new Dimensions(JSONSerializer.parseList(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            String[] parts = value.split("\\s*[xX]\\s*");
            if (parts.length != 2) {
                parts = value.split("\\s*[,;]\\s*");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Unknown format for Dimensions.decode: " + value);
                }
            }
            try {
                int width = Integer.parseInt(parts[0]);
                int height = Integer.parseInt(parts[1]);
                dimensions = new Dimensions(width, height);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        return dimensions;
    }
}
