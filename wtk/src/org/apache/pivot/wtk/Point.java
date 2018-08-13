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
 * An immutable class representing the location of an object.
 * <p> This class is immutable (unlike a {@link java.awt.Point}), so that
 * the {@link #translate} method returns a new object, rather than
 * modifying the original (for instance).
 */
public final class Point implements Serializable {
    private static final long serialVersionUID = 5193175754909343769L;

    public final int x;
    public final int y;

    public static final String X_KEY = "x";
    public static final String Y_KEY = "y";

    public Point(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public Point(final Point point) {
        Utils.checkNull(point, "point");

        this.x = point.x;
        this.y = point.y;
    }

    public Point(final Dictionary<String, ?> point) {
        Utils.checkNull(point, "point");

        this.x = point.getInt(X_KEY);
        this.y = point.getInt(Y_KEY);
    }

    public Point(final Sequence<?> point) {
        Utils.checkNull(point, "point");

        this.x = ((Number) point.get(0)).intValue();
        this.y = ((Number) point.get(1)).intValue();
    }

    /**
     * Return a new <tt>Point</tt> object which represents
     * this point moved to a new location, <tt>dx</tt> and
     * <tt>dy</tt> away from the original.
     *
     * @param dx The distance to move in the horizontal
     * direction (positive or negative).
     * @param dy The distance to move in the vertical
     * direction (positive moves downward on the screen,
     * and negative to move upward).
     * @return A new object represented the translated
     * location.
     */
    public Point translate(final int dx, final int dy) {
        return new Point(x + dx, y + dy);
    }

    @Override
    public boolean equals(final Object object) {
        boolean equals = false;

        if (object instanceof Point) {
            Point point = (Point) object;
            equals = (x == point.x && y == point.y);
        }

        return equals;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + x + "," + y + "]";
    }

    /**
     * Decode a JSON-formatted string (map or list) that contains the two
     * values for a new point.
     * <p> The format of a JSON map would be:
     * <pre>{ "x":nnn, "y":nnn }</pre>
     * <p> The format for a JSON list would be:
     * <pre>[ x, y ]</pre>
     *
     * @param value The JSON string to be interpreted (must not be {@code null}).
     * @return The new Point object if the string can be decoded successfully.
     * @throws IllegalArgumentException if the input is {@code null} or if the
     * value could not be successfully decoded as either a JSON map or list.
     * @see #Point(Dictionary)
     * @see #Point(int, int)
     */
    public static Point decode(final String value) {
        Utils.checkNull(value);

        Point point;
        if (value.startsWith("{")) {
            try {
                point = new Point(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else if (value.startsWith("[")) {
            try {
                point = new Point(JSONSerializer.parseList(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            throw new IllegalArgumentException("Invalid format for Point.");
        }

        return point;
    }

}
