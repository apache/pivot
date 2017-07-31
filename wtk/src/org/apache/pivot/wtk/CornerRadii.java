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
import org.apache.pivot.collections.List;
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

    private void check(CornerRadii radii) {
        check(radii.topLeft, radii.topRight, radii.bottomLeft, radii.bottomRight);
    }

    private void check(int topLeft, int topRight, int bottomLeft, int bottomRight) {
        if (topLeft < 0) {
            throw new IllegalArgumentException("topLeft is negative.");
        }

        if (topRight < 0) {
            throw new IllegalArgumentException("topRight is negative.");
        }

        if (bottomLeft < 0) {
            throw new IllegalArgumentException("bottomLeft is negative.");
        }

        if (bottomRight < 0) {
            throw new IllegalArgumentException("bottomRight is negative.");
        }

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
     * values for each of the four corners
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
        Utils.checkNull(value, "value");

        CornerRadii cornerRadii;
        if (value.startsWith("{")) {
            try {
                cornerRadii = new CornerRadii(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else if (value.startsWith("[")) {
            try {
                @SuppressWarnings("unchecked")
                List<Integer> values = (List<Integer>)JSONSerializer.parseList(value);
                cornerRadii = new CornerRadii(values.get(0), values.get(1), values.get(2), values.get(3));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            cornerRadii = new CornerRadii(Integer.parseInt(value));
        }

        return cornerRadii;
    }
}
