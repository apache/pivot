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

import java.awt.Rectangle;
import java.io.Serializable;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
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

    public static final Bounds EMPTY = new Bounds(0, 0, 0, 0);

    /**
     * Construct a bounds object given all four values for it.
     * @param x      The starting X-position of the area.
     * @param y      The starting Y-position.
     * @param width  The width of the bounded area.
     * @param height The height of the area.
     */
    public Bounds(final int x, final int y, final int width, final int height) {
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
    public Bounds(final Point origin, final Dimensions size) {
        Utils.checkNull(origin, "origin");
        Utils.checkNull(size, "size");

        x = origin.x;
        y = origin.y;
        width = size.width;
        height = size.height;
    }

    /**
     * Construct a new Bounds which has the given size and a (0, 0) origin.
     * @param size The size of the bounded area (must not be {@code null}).
     * @throws IllegalArgumentException if the size is {@code null}.
     */
    public Bounds(final Dimensions size) {
        Utils.checkNull(size, "size");

        x = 0;
        y = 0;
        width = size.width;
        height = size.height;
    }

    /**
     * Construct a new bounds object from an existing bounds.
     * @param bounds The existing bounds to copy (cannot be {@code null}).
     * @throws IllegalArgumentException if the argument is {@code null}.
     */
    public Bounds(final Bounds bounds) {
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
    public Bounds(final Dictionary<String, ?> bounds) {
        Utils.checkNull(bounds, "bounds");

        x = bounds.getInt(X_KEY);
        y = bounds.getInt(Y_KEY);
        width = bounds.getInt(WIDTH_KEY);
        height = bounds.getInt(HEIGHT_KEY);
    }

    /**
     * Construct a new bounds object given a sequence of the
     * four needed values.
     *
     * @param bounds The sequence containing the bounds values,
     * in the order of <tt>[ x, y, width, height ]</tt>
     * @throws IllegalArgumentException if the bounds argument is {@code null}.
     */
    public Bounds(final Sequence<?> bounds) {
        Utils.checkNull(bounds, "bounds");

        x = ((Number) bounds.get(0)).intValue();
        y = ((Number) bounds.get(1)).intValue();
        width = ((Number) bounds.get(2)).intValue();
        height = ((Number) bounds.get(3)).intValue();
    }

    /**
     * Convert a {@link Rectangle} to one of our bounds objects.
     * @param rectangle The existing rectangle to convert (cannot
     * be {@code null}).
     * @throws IllegalArgumentException if the rectangle is {@code null}.
     */
    public Bounds(final Rectangle rectangle) {
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
     * @param xValue      The X-position of the bounded area to union with this one.
     * @param yValue      The Y-position of the other bounded area.
     * @param widthValue  The width of the other area to union with this one.
     * @param heightValue The other area's height.
     * @return A new bounds that is the union of this one with the bounds specified by
     * the given arguments.
     */
    public Bounds union(final int xValue, final int yValue, final int widthValue, final int heightValue) {
        int x1 = Math.min(this.x, xValue);
        int y1 = Math.min(this.y, yValue);
        int x2 = Math.max(this.x + this.width, xValue + widthValue);
        int y2 = Math.max(this.y + this.height, yValue + heightValue);

        return new Bounds(x1, y1, x2 - x1, y2 - y1);

    }

    /**
     * @return A new bounds object that is the union of this bounds with the given one.
     * @param bounds The other bounds to union with this one.
     * @see #union(int, int, int, int)
     * @throws IllegalArgumentException if the given bounds is {@code null}.
     */
    public Bounds union(final Bounds bounds) {
        Utils.checkNull(bounds, "bounds");

        return union(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /**
     * Create a new bounds that is the intersection of this one and the bounded area specified
     * by the given arguments.
     * <p> "Intersection" means the new bounds will include only the area that is common to
     * both areas (X- and Y-position will be the maximum of either, while width and height will
     * be the minimum of the two).
     * @param xValue      The X-position of the other area to intersect with.
     * @param yValue      The Y-position of the other area.
     * @param widthValue  The width of the other bounded area.
     * @param heightValue The height of the other area.
     * @return The new bounds that is the intersection of this one and the given area.
     */
    public Bounds intersect(final int xValue, final int yValue, final int widthValue, final int heightValue) {
        int x1 = Math.max(this.x, xValue);
        int y1 = Math.max(this.y, yValue);
        int x2 = Math.min(this.x + this.width, xValue + widthValue);
        int y2 = Math.min(this.y + this.height, yValue + heightValue);

        return new Bounds(x1, y1, x2 - x1, y2 - y1);
    }

    /**
     * @return A new bounds that is the intersection of this one and the given one.
     * @param bounds The other area to intersect with this one (cannot be {@code null}).
     * @throws IllegalArgumentException if the given bounds is {@code null}.
     * @see #intersect(int, int, int, int)
     */
    public Bounds intersect(final Bounds bounds) {
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
    public Bounds intersect(final Rectangle rect) {
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
    public Bounds translate(final int dx, final int dy) {
        return new Bounds(x + dx, y + dy, width, height);
    }

    /**
     * Create a new bounds object that represents this bounds expanded/contracted by
     * the given width/height values (negative represent contraction).
     * <p> The new bounds have the same x- and y-origin values as the original.
     * @param dw The amount of expansion(contraction) in the width.
     * @param dh The amount of expansion(contraction) in the height.
     * @return A new bounds expanded by this amount.
     */
    public Bounds expand(final int dw, final int dh) {
        return new Bounds(x, y, width + dw, height + dh);
    }

    /**
     * @return A new bounds offset by the amounts given by the point.
     * @param offset X- and Y-values which are used to offset this bounds
     * to a new position (must not be {@code null}).
     * @throws IllegalArgumentException if the offset value is {@code null}.
     * @see #translate(int, int)
     */
    public Bounds translate(final Point offset) {
        Utils.checkNull(offset, "offset");

        return translate(offset.x, offset.y);
    }

    /**
     * @return Whether this bounded area contains the given point.
     * @param point The other point to test (must not be {@code null}).
     * @throws IllegalArgumentException if the point argument is {@code null}.
     * @see #contains(int, int)
     */
    public boolean contains(final Point point) {
        Utils.checkNull(point, "point");

        return contains(point.x, point.y);
    }

    /**
     * Does this bounded area contain the point defined by the given arguments?
     * @param xValue The X-position of the other point to test.
     * @param yValue The Y-position of the other point to test.
     * @return Whether this bounds contains the given point.
     */
    public boolean contains(final int xValue, final int yValue) {
        return (xValue >= this.x
             && yValue >= this.y
             && xValue < this.x + width
             && yValue < this.y + height);
    }

    /**
     * @return Does this bounded area completely contain (could be coincident with) the bounded area
     * specified by the given argument?
     * @param bounds The other bounded area to check (must not be {@code null}).
     * @throws IllegalArgumentException if the given bounds is {@code null}.
     * @see #contains(int, int, int, int)
     */
    public boolean contains(final Bounds bounds) {
        Utils.checkNull(bounds, "bounds");

        return contains(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /**
     * @return Does this bounded area completely contain (could be coincident with) the bounded area
     * specified by the given arguments?
     * @param xValue      The X-position of the area to test.
     * @param yValue      The Y-position of the other area.
     * @param widthValue  The width of the other area.
     * @param heightValue The height of the area to test.
     */
    public boolean contains(final int xValue, final int yValue, final int widthValue, final int heightValue) {
        return (!isEmpty()
             && xValue >= this.x
             && yValue >= this.y
             && xValue + widthValue <= this.x + this.width
             && yValue + heightValue <= this.y + this.height);
    }

    /**
     * @return Does this bounded area intersect with the bounded area given by the argument?
     * @param bounds The other area to test (must not be {@code null}).
     * @throws IllegalArgumentException if the given bounds is {@code null}.
     * @see #intersects(int, int, int, int)
     */
    public boolean intersects(final Bounds bounds) {
        Utils.checkNull(bounds, "bounds");

        return intersects(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /**
     * @return Does this bounded area intersect with the bounded area given by the arguments?
     * @param xValue      The X-position of the other area to check.
     * @param yValue      The Y-position of the other area.
     * @param widthValue  The width of the other bounded area.
     * @param heightValue The height of the other area.
     */
    public boolean intersects(final int xValue, final int yValue, final int widthValue, final int heightValue) {
        return (!isEmpty()
             && xValue + widthValue > this.x
             && yValue + heightValue > this.y
             && xValue < this.x + this.width
             && yValue < this.y + this.height);
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
    public boolean equals(final Object object) {
        boolean equals = false;

        if (object instanceof Bounds) {
            Bounds bounds = (Bounds) object;
            equals = (x == bounds.x && y == bounds.y
                   && width == bounds.width && height == bounds.height);
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
    public Rectangle toRectangle() {
        return new Rectangle(x, y, width, height);
    }

    /**
     * @return A more-or-less human-readable representation of this object, which looks like:
     * <pre>Bounds [X,Y;WxH]</pre>
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + x + "," + y + ";" + width + "x" + height + "]";
    }

    /**
     * Decode a JSON-encoded string (map or list) that contains the values for a new
     * bounded area.
     * <p> The format of a JSON map format will be:
     * <pre>{ "x": nnn, "y": nnn, "width": nnn, "height": nnn }</pre>
     * <p> The format of a JSON list format will be:
     * <pre>[ x, y, width, height ]</pre>
     * <p> Also accepted is a simple list (comma- or semicolon-separated) of four
     * integer values.
     *
     * @param boundsValue The JSON string containing the map or list of bounds values
     * (must not be {@code null}).
     * @return The new bounds object if the string can be successfully decoded.
     * @throws IllegalArgumentException if the given string is {@code null} or
     * empty or the string could not be parsed as a JSON map or list.
     * @see #Bounds(Dictionary)
     * @see #Bounds(int, int, int, int)
     */
    public static Bounds decode(final String boundsValue) {
        Utils.checkNullOrEmpty(boundsValue, "boundsValue");

        Bounds bounds;
        if (boundsValue.startsWith("{")) {
            try {
                bounds = new Bounds(JSONSerializer.parseMap(boundsValue));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else if (boundsValue.startsWith("[")) {
            try {
                bounds = new Bounds(JSONSerializer.parseList(boundsValue));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            String[] parts = boundsValue.split("\\s*[,;]\\s*");
            if (parts.length != 4) {
                throw new IllegalArgumentException("Invalid format for Bounds: " + boundsValue);
            }
            try {
                bounds = new Bounds(
                    Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        return bounds;
    }

}
