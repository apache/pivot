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
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Utils;

/**
 * Class representing the bounds of an object (that is, the X- and
 * Y-position plus the width and height).
 */
public final class Bounds implements Serializable {
    private static final long serialVersionUID = -2473226417628417475L;

    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public static final String X_KEY = "x";
    public static final String Y_KEY = "y";
    public static final String WIDTH_KEY = "width";
    public static final String HEIGHT_KEY = "height";

    /**
     * Construct a bounds object given all four values for it.
     * @param x      The starting X-position of the area.
     * @param y      The starting Y-position.
     * @param width  The width of the bounded area.
     * @param height The height of the area.
     */
    public Bounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Construct a bounds object given the origin position and size.
     * @param origin The origin point of the area (must not be {@code null}).
     * @param size   The size of the bounded area (must not be {@code null}).
     * @throws IllegalArgumentException if either argument is {@code null}.
     */
    public Bounds(Point origin, Dimensions size) {
        Utils.checkNull(origin, "origin");
        Utils.checkNull(size, "size");

        x = origin.x;
        y = origin.y;
        width = size.width;
        height = size.height;
    }

    /**
     * Construct a new bounds object from an existing bounds.
     * @param bounds The existing bounds to copy (cannot be {@code null}).
     * @throws IllegalArgumentException if the argument is {@code null}.
     */
    public Bounds(Bounds bounds) {
        Utils.checkNull(bounds, "bounds");

        x = bounds.x;
        y = bounds.y;
        width = bounds.width;
        height = bounds.height;
    }

    /**
     * Construct a new bounds object given a map/dictionary of the
     * four needed values. If any of the values is missing from the
     * dictionary, the corresponding value in the bounds will be set
     * to zero.
     * @param bounds The dictionary containing the bounds values,
     * which should contain an entry for {@link #X_KEY}, {@link #Y_KEY},
     * {@link #WIDTH_KEY} and {@link #HEIGHT_KEY}.
     * @throws IllegalArgumentException if the bounds argument is {@code null}.
     */
    public Bounds(Dictionary<String, ?> bounds) {
        Utils.checkNull(bounds, "bounds");

        if (bounds.containsKey(X_KEY)) {
            x = ((Integer) bounds.get(X_KEY)).intValue();
        } else {
            x = 0;
        }

        if (bounds.containsKey(Y_KEY)) {
            y = ((Integer) bounds.get(Y_KEY)).intValue();
        } else {
            y = 0;
        }

        if (bounds.containsKey(WIDTH_KEY)) {
            width = ((Integer) bounds.get(WIDTH_KEY)).intValue();
        } else {
            width = 0;
        }

        if (bounds.containsKey(HEIGHT_KEY)) {
            height = ((Integer) bounds.get(HEIGHT_KEY)).intValue();
        } else {
            height = 0;
        }
    }

    /**
     * Convert a {@link java.awt.Rectangle} to one of our bounds
     * objects.
     * @param rectangle The existing rectangle to convert (cannot
     * be {@code null}).
     * @throws IllegalArgumentException if the rectangle is {@code null}.
     */
    public Bounds(java.awt.Rectangle rectangle) {
        Utils.checkNull(rectangle, "rectangle");

        x = rectangle.x;
        y = rectangle.y;
        width = rectangle.width;
        height = rectangle.height;
    }

    /**
     * @return The X- and Y-location of this bounded area in {@link Point}
     * form.
     */
    public Point getLocation() {
        return new Point(x, y);
    }

    /**
     * @return The width and height of this bounded area in {@link Dimensions}
     * form.
     */
    public Dimensions getSize() {
        return new Dimensions(width, height);
    }

    /**
     * Create a new bounds that is the union of this one and the given arguments.
     * <p> "Union" means the new bounds will include all of this bounds and the
     * bounds specified by the arguments (X- and Y-position will be the minimum of either
     * and width and height will be the maximum).
     *
     * @param xArgument      The X-position of the bounded area to union with this one.
     * @param yArgument      The Y-position of the other bounded area.
     * @param widthArgument  The width of the other area to union with this one.
     * @param heightArgument The other area's height.
     * @return A new bounds that is the union of this one with the bounds specified by
     * the given arguments.
     */
    public Bounds union(int xArgument, int yArgument, int widthArgument, int heightArgument) {
        int x1 = Math.min(this.x, xArgument);
        int y1 = Math.min(this.y, yArgument);
        int x2 = Math.max(this.x + this.width, xArgument + widthArgument);
        int y2 = Math.max(this.y + this.height, yArgument + heightArgument);

        return new Bounds(x1, y1, x2 - x1, y2 - y1);

    }

    /**
     * @return A new bounds object that is the union of this bounds with the given one.
     * @param bounds The other bounds to union with this one.
     * @see #union(int, int, int, int)
     * @throws IllegalArgumentException if the given bounds is {@code null}.
     */
    public Bounds union(Bounds bounds) {
        Utils.checkNull(bounds, "bounds");

        return union(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /**
     * Create a new bounds that is the intersection of this one and the bounded area specified
     * by the given arguments.
     * <p> "Intersection" means the new bounds will include only the area that is common to
     * both areas (X- and Y-position will be the maximum of either, while width and height will
     * be the minimum of the two).
     * @param xArgument      The X-position of the other area to intersect with.
     * @param yArgument      The Y-position of the other area.
     * @param widthArgument  The width of the other bounded area.
     * @param heightArgument The height of the other area.
     * @return The new bounds that is the intersection of this one and the given area.
     */
    public Bounds intersect(int xArgument, int yArgument, int widthArgument, int heightArgument) {
        int x1 = Math.max(this.x, xArgument);
        int y1 = Math.max(this.y, yArgument);
        int x2 = Math.min(this.x + this.width, xArgument + widthArgument);
        int y2 = Math.min(this.y + this.height, yArgument + heightArgument);

        return new Bounds(x1, y1, x2 - x1, y2 - y1);
    }

    /**
     * @return A new bounds that is the intersection of this one and the given one.
     * @param bounds The other area to intersect with this one (cannot be {@code null}).
     * @throws IllegalArgumentException if the given bounds is {@code null}.
     * @see #intersect(int, int, int, int)
     */
    public Bounds intersect(Bounds bounds) {
        Utils.checkNull(bounds, "bounds");

        return intersect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /**
     * @return A new bounds object that is the intersection of this one with the given
     * rectangle.
     * @param rect The other rectangle to intersect with (must not be {@code null}).
     * @throws IllegalArgumentException if the rectangle is {@code null}.
     * @see #intersect(int, int, int, int)
     */
    public Bounds intersect(java.awt.Rectangle rect) {
        Utils.checkNull(rect, "rect");

        return intersect(rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Create a new bounds object that represents this bounds offset by the given
     * values.
     * <p> The new bounds has the same width and height, but the X- and Y-positions
     * have been offset by the given values (which can be either positive or negative).
     * @param dx The amount of translation in the X-direction.
     * @param dy The amount of translation in the Y-direction.
     * @return A new bounds offset by these amounts.
     */
    public Bounds translate(int dx, int dy) {
        return new Bounds(x + dx, y + dy, width, height);
    }

    /**
     * @return A new bounds offset by the amounts given by the point.
     * @param offset X- and Y-values which are used to offset this bounds
     * to a new position (must not be {@code null}).
     * @throws IllegalArgumentException if the offset value is {@code null}.
     * @see #translate(int, int)
     */
    public Bounds translate(Point offset) {
        Utils.checkNull(offset, "offset");

        return translate(offset.x, offset.y);
    }

    /**
     * @return Whether this bounded area contains the given point.
     * @param point The other point to test (must not be {@code null}).
     * @throws IllegalArgumentException if the point argument is {@code null}.
     * @see #contains(int, int)
     */
    public boolean contains(Point point) {
        Utils.checkNull(point, "point");

        return contains(point.x, point.y);
    }

    /**
     * Does this bounded area contain the point defined by the given arguments?
     * @param xArgument The X-position of the other point to test.
     * @param yArgument The Y-position of the other point to test.
     * @return Whether this bounds contains the given point.
     */
    public boolean contains(int xArgument, int yArgument) {
        return (xArgument >= this.x &&
                yArgument >= this.y &&
                xArgument < this.x + width &&
                yArgument < this.y + height);
    }

    /**
     * @return Does this bounded area completely contain (could be coincident with) the bounded area
     * specified by the given argument?
     * @param bounds The other bounded area to check (must not be {@code null}).
     * @throws IllegalArgumentException if the given bounds is {@code null}.
     * @see #contains(int, int, int, int)
     */
    public boolean contains(Bounds bounds) {
        Utils.checkNull(bounds, "bounds");

        return contains(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /**
     * @return Does this bounded area completely contain (could be coincident with) the bounded area
     * specified by the given arguments?
     * @param xArgument      The X-position of the area to test.
     * @param yArgument      The Y-position of the other area.
     * @param widthArgument  The width of the other area.
     * @param heightArgument The height of the area to test.
     */
    public boolean contains(int xArgument, int yArgument, int widthArgument, int heightArgument) {
        return (!isEmpty() &&
                xArgument >= this.x &&
                yArgument >= this.y &&
                xArgument + widthArgument <= this.x + this.width &&
                yArgument + heightArgument <= this.y + this.height);
    }

    /**
     * @return Does this bounded area intersect with the bounded area given by the argument?
     * @param bounds The other area to test (must not be {@code null}).
     * @throws IllegalArgumentException if the given bounds is {@code null}.
     * @see #intersects(int, int, int, int)
     */
    public boolean intersects(Bounds bounds) {
        Utils.checkNull(bounds, "bounds");

        return intersects(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /**
     * @return Does this bounded area intersect with the bounded area given by the arguments?
     * @param xArgument      The X-position of the other area to check.
     * @param yArgument      The Y-position of the other area.
     * @param widthArgument  The width of the other bounded area.
     * @param heightArgument The height of the other area.
     */
    public boolean intersects(int xArgument, int yArgument, int widthArgument, int heightArgument) {
        return (!isEmpty() &&
                xArgument + widthArgument > this.x &&
                yArgument + heightArgument > this.y &&
                xArgument < this.x + this.width &&
                yArgument < this.y + this.height);
    }

    /**
     * Does this bounds represent an empty area?
     * @return {@code true} if the width OR height of this bounded area is less than
     * or equal to zero (in other words if it has EITHER no width or no height).
     */
    public boolean isEmpty() {
        return (width <= 0 || height <= 0);
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof Bounds) {
            Bounds bounds = (Bounds) object;
            equals = (x == bounds.x && y == bounds.y && width == bounds.width && height == bounds.height);
        }

        return equals;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + height;
        result = prime * result + width;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    /**
     * @return This bounded area as a {@link java.awt.Rectangle}.
     */
    public java.awt.Rectangle toRectangle() {
        return new java.awt.Rectangle(x, y, width, height);
    }

    /**
     * @return A more-or-less human-readable representation of this object, which looks like:
     * <pre>org.apache.pivot.wtk.Bounds [X,Y;WxH]</pre>
     */
    @Override
    public String toString() {
        return getClass().getName() + " [" + x + "," + y + ";" + width + "x" + height + "]";
    }

    /**
     * Decode a JSON-encoded string (map) that contains the values for a new
     * bounded area.
     * @param boundsValue The JSON string containing the map of bounds values
     * (must not be {@code null}).
     * @return The new bounds object if the string can be successfully decoded.
     * @throws IllegalArgumentException if the given string is {@code null} or
     * the string could not be parsed as a JSON map.
     * @see #Bounds(Dictionary)
     */
    public static Bounds decode(String boundsValue) {
        Utils.checkNull(boundsValue, "boundsValue");

        Bounds bounds;
        try {
            bounds = new Bounds(JSONSerializer.parseMap(boundsValue));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }

        return bounds;
    }
}
