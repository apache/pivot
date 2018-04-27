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
 * Class representing the insets of an object, also called "padding"
 * (or in some classes, "margin").
 */
public final class Insets implements Serializable {
    private static final long serialVersionUID = -8528862892185591370L;

    public final int top;
    public final int left;
    public final int bottom;
    public final int right;

    public static final String TOP_KEY = "top";
    public static final String LEFT_KEY = "left";
    public static final String BOTTOM_KEY = "bottom";
    public static final String RIGHT_KEY = "right";

    /**
     * Insets whose top, left, bottom, and right values are all zero.
     */
    public static final Insets NONE = new Insets(0);

    public Insets(int inset) {
        this.top = inset;
        this.left = inset;
        this.bottom = inset;
        this.right = inset;
    }

    public Insets(Number inset) {
        Utils.checkNull(inset, "padding/margin");

        int value = inset.intValue();
        this.top = value;
        this.left = value;
        this.bottom = value;
        this.right = value;
    }

    public Insets(int top, int left, int bottom, int right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    /**
     * Construct an <tt>Insets</tt> value given the total
     * height and width values to produce.
     * <p> This will assign half the height to each of the top
     * and bottom, and half the width each to the left and right.
     * <p> Any excess (for odd values) will be assigned to the bottom
     * and right respectively.
     *
     * @param height  The total height to assign to this Insets value.
     * @param width   The total width to assign.
     * @see #getHeight
     * @see #getWidth
     */
    public Insets(int height, int width) {
        this.top = height / 2;
        // For odd height, assign the excess to the bottom
        this.bottom = height - this.top;
        this.left = width / 2;
        // Ditto for width, excess on the right
        this.right = width - this.left;
    }

    /**
     * Construct an <tt>Insets</tt> value given the total
     * dimensions of the value to produce.
     * <p> This will assign half the dimensions height to each of the top
     * and bottom, and half the dimensions width each to the left and right.
     * <p> Any excess (for odd values) will be assigned to the right
     * and bottom respectively.
     *
     * @param size The total size (height and width) to assign.
     * @see #getSize
     */
    public Insets(Dimensions size) {
        this(size.height, size.width);
    }

    public Insets(Insets insets) {
        Utils.checkNull(insets, "padding/margin");

        this.top = insets.top;
        this.left = insets.left;
        this.bottom = insets.bottom;
        this.right = insets.right;
    }

    public Insets(Dictionary<String, ?> insets) {
        Utils.checkNull(insets, "padding/margin");

        this.top = insets.getInt(TOP_KEY);
        this.left = insets.getInt(LEFT_KEY);
        this.bottom = insets.getInt(BOTTOM_KEY);
        this.right = insets.getInt(RIGHT_KEY);
    }

    public Insets(Sequence<?> insets) {
        Utils.checkNull(insets, "padding/margin");

        this.top = ((Number) insets.get(0)).intValue();
        this.left = ((Number) insets.get(1)).intValue();
        this.bottom = ((Number) insets.get(2)).intValue();
        this.right = ((Number) insets.get(3)).intValue();
    }

    /**
     * @return The total width of this Insets (that is, the
     * left + right values).
     */
    public int getWidth() {
        return left + right;
    }

    /**
     * @return The total height of this Insets (that is, the
     * top + bottom values).
     */
    public int getHeight() {
        return top + bottom;
    }

    /**
     * Return the total size of this insets value as a single
     * <tt>Dimensions</tt> value.
     *
     * @return The total width and height of this object.
     * @see #getWidth
     * @see #getHeight
     * @see #Insets(Dimensions)
     */
    public Dimensions getSize() {
        return new Dimensions(left + right, top + bottom);
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof Insets) {
            Insets insets = (Insets) object;
            equals = (top == insets.top && left == insets.left
                   && bottom == insets.bottom && right == insets.right);
        }

        return equals;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + top;
        result = prime * result + left;
        result = prime * result + bottom;
        result = prime * result + right;
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + top + ", " + left + ", " + bottom + ", " + right + "]";
    }

    /**
     * Decode a possible Insets value. The value can be in one of the
     * following forms:
     * <ul>
     * <li><pre>{ "top": nnn, "left": nnn, "bottom": nnn, "right": nnn }</pre></li>
     * <li><pre>[ top, left, bottom, right ]</pre></li>
     * <li><pre>top[,;] left[,;] bottom[,;] right</pre></li>
     * <li><em>nnnn</em></li>
     * </ul>
     *
     * @param value The string value of the Insets to decode.
     * @return The parsed <tt>Insets</tt> value.
     * @throws IllegalArgumentException if the input is not in one of these
     * formats.
     * @see #Insets(Dictionary)
     * @see #Insets(Sequence)
     * @see #Insets(int, int, int, int)
     * @see #Insets(int)
     */
    public static Insets decode(String value) {
        Utils.checkNullOrEmpty(value, "padding/margin");

        Insets insets;
        if (value.startsWith("{")) {
            try {
                insets = new Insets(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else if (value.startsWith("[")) {
            try {
                insets = new Insets(JSONSerializer.parseList(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            String[] parts = value.split("\\s*[,;]\\s*");
            if (parts.length == 4) {
                try {
                    insets = new Insets(
                        Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(ex);
                }
            } else if (parts.length == 1) {
                try {
                    insets = new Insets(Integer.parseInt(value));
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(ex);
                }
            } else {
                throw new IllegalArgumentException("Unknown format for Insets: " + value);
            }
        }

        return insets;
    }

}
