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
 * Class representing the corner radii of a rectangular object.
 */
public final class CornerRadii implements Serializable {
    private static final long serialVersionUID = -433469769555042467L;

    public final int topLeft;
    public final int topRight;
    public final int bottomLeft;
    public final int bottomRight;

    public static final String TOP_LEFT_KEY = "topLeft";
    public static final String TOP_RIGHT_KEY = "topRight";
    public static final String BOTTOM_LEFT_KEY = "bottomLeft";
    public static final String BOTTOM_RIGHT_KEY = "bottomRight";

    /**
     * Corner radii whose top, left, bottom, and right values are all zero.
     */
    public static final CornerRadii NONE = new CornerRadii(0);

    public CornerRadii(int radius) {
        this(radius, radius, radius, radius);
    }

    public CornerRadii(Number radius) {
        Utils.checkNull(radius, "radius");
        int radii = radius.intValue();
        Utils.checkNonNegative(radii, "radii");

        this.topLeft = radii;
        this.topRight = radii;
        this.bottomLeft = radii;
        this.bottomRight = radii;
    }

    private void check(CornerRadii radii) {
        check(radii.topLeft, radii.topRight, radii.bottomLeft, radii.bottomRight);
    }

    private void check(int topLeft, int topRight, int bottomLeft, int bottomRight) {
        Utils.checkNonNegative(topLeft, "topLeft");
        Utils.checkNonNegative(topRight, "topRight");
        Utils.checkNonNegative(bottomLeft, "bottomLeft");
        Utils.checkNonNegative(bottomRight, "bottomRight");
    }

    public CornerRadii(CornerRadii cornerRadii) {
        Utils.checkNull(cornerRadii, "cornerRadii");

        check(cornerRadii);

        this.topLeft = cornerRadii.topLeft;
        this.topRight = cornerRadii.topRight;
        this.bottomLeft = cornerRadii.bottomLeft;
        this.bottomRight = cornerRadii.bottomRight;
    }

    public CornerRadii(int topLeft, int topRight, int bottomLeft, int bottomRight) {
        check(topLeft, topRight, bottomLeft, bottomRight);

        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
    }

    /**
     * Construct a {@link CornerRadii} object from a dictionary specifying
     * values for each of the four corners.
     *
     * @param cornerRadii A dictionary with keys {@value #TOP_LEFT_KEY},
     * {@value #TOP_RIGHT_KEY}, {@value #BOTTOM_LEFT_KEY},
     * {@value #BOTTOM_RIGHT_KEY}, all with numeric values. Omitted values are
     * treated as zero.
     */
    public CornerRadii(Dictionary<String, ?> cornerRadii) {
        Utils.checkNull(cornerRadii, "cornerRadii");

        topLeft = cornerRadii.getInt(TOP_LEFT_KEY, 0);
        topRight = cornerRadii.getInt(TOP_RIGHT_KEY, 0);
        bottomLeft = cornerRadii.getInt(BOTTOM_LEFT_KEY, 0);
        bottomRight = cornerRadii.getInt(BOTTOM_RIGHT_KEY, 0);

        check(this);
    }

    public CornerRadii(Sequence<?> cornerRadii) {
        Utils.checkNull(cornerRadii, "cornerRadii");

        topLeft = ((Number) cornerRadii.get(0)).intValue();
        topRight = ((Number) cornerRadii.get(1)).intValue();
        bottomLeft = ((Number) cornerRadii.get(2)).intValue();
        bottomRight = ((Number) cornerRadii.get(3)).intValue();

        check(this);
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof CornerRadii) {
            CornerRadii cornerRadii = (CornerRadii) object;
            equals = (topLeft == cornerRadii.topLeft && topRight == cornerRadii.topRight
                && bottomLeft == cornerRadii.bottomLeft && bottomRight == cornerRadii.bottomRight);
        }

        return equals;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + topLeft;
        result = prime * result + topRight;
        result = prime * result + bottomLeft;
        result = prime * result + bottomRight;
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + topLeft + "," + topRight + "; "
            + bottomLeft + "," + bottomRight + "]";
    }

    /**
     * Convert a string into corner radii.
     * <p> If the string value is a JSON map, then parse the map
     * and construct using the {@link #CornerRadii(Dictionary)} method.
     * <p> If the string value is a JSON list, then parse the list
     * and construct using the first four values as top left, top right,
     * bottom left, and bottom right respectively, using the
     * {@link #CornerRadii(int, int, int, int)} constructor.
     * <p> A form of 4 integers values separate by commas or semicolons
     * is also accepted, as in "n, n; n, n", where the values are in the
     * same order as the JSON list form.
     * <p> Otherwise the string should be a single integer value
     * that will be used to construct the radii using the {@link #CornerRadii(int)}
     * constructor.
     *
     * @param value The string value to decode into new corner radii.
     * @return The decoded corner radii.
     * @throws IllegalArgumentException if the value is {@code null} or
     * if the string starts with <code>"{"</code> but it cannot be parsed as
     * a JSON map, or if it starts with <code>"["</code> but cannot be parsed
     * as a JSON list.
     */
    public static CornerRadii decode(String value) {
        Utils.checkNullOrEmpty(value, "value");

        CornerRadii cornerRadii;
        if (value.startsWith("{")) {
            try {
                cornerRadii = new CornerRadii(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else if (value.startsWith("[")) {
            try {
                cornerRadii = new CornerRadii(JSONSerializer.parseList(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            String[] parts = value.split("\\s*[,;]\\s*");
            if (parts.length == 4) {
                try {
                    cornerRadii = new CornerRadii(
                        Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(ex);
                }
            } else if (parts.length == 1) {
                cornerRadii = new CornerRadii(Integer.parseInt(value));
            } else {
                throw new IllegalArgumentException("Bad format for corner radii value: " + value);
            }
        }

        return cornerRadii;
    }

}
