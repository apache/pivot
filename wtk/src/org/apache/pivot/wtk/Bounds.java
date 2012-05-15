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

/**
 * Class representing the bounds of an object.
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

    public Bounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Bounds(Point origin, Dimensions size) {
        if (origin == null) {
            throw new IllegalArgumentException("origin is null.");
        }

        if (size == null) {
            throw new IllegalArgumentException("size is null.");
        }

        x = origin.x;
        y = origin.y;
        width = size.width;
        height = size.height;
    }

    public Bounds(Bounds bounds) {
        if (bounds == null) {
            throw new IllegalArgumentException("bounds is null.");
        }

        x = bounds.x;
        y = bounds.y;
        width = bounds.width;
        height = bounds.height;
    }

    public Bounds(Dictionary<String, ?> bounds) {
        if (bounds == null) {
            throw new IllegalArgumentException("bounds is null.");
        }

        if (bounds.containsKey(X_KEY)) {
            x = (Integer)bounds.get(X_KEY);
        } else {
            x = 0;
        }

        if (bounds.containsKey(Y_KEY)) {
            y = (Integer)bounds.get(Y_KEY);
        } else {
            y = 0;
        }

        if (bounds.containsKey(WIDTH_KEY)) {
            width = (Integer)bounds.get(WIDTH_KEY);
        } else {
            width = 0;
        }

        if (bounds.containsKey(HEIGHT_KEY)) {
            height = (Integer)bounds.get(HEIGHT_KEY);
        } else {
            height = 0;
        }
    }

    public Bounds(java.awt.Rectangle rectangle) {
        if (rectangle == null) {
            throw new IllegalArgumentException("rectangle is null.");
        }

        x = rectangle.x;
        y = rectangle.y;
        width = rectangle.width;
        height = rectangle.height;
    }

    public Point getLocation() {
        return new Point(x, y);
    }

    public Dimensions getSize() {
        return new Dimensions(width, height);
    }

    public Bounds union(int xArgument, int yArgument, int widthArgument, int heightArgument) {
        int x1 = Math.min(this.x, xArgument);
        int y1 = Math.min(this.y, yArgument);
        int x2 = Math.max(this.x + this.width, xArgument + widthArgument);
        int y2 = Math.max(this.y + this.height, yArgument + heightArgument);

        return new Bounds(x1, y1, x2 - x1, y2 - y1);

    }

    public Bounds union(Bounds bounds) {
        return union(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public Bounds intersect(int xArgument, int yArgument, int widthArgument, int heightArgument) {
        int x1 = Math.max(this.x, xArgument);
        int y1 = Math.max(this.y, yArgument);
        int x2 = Math.min(this.x + this.width, xArgument + widthArgument);
        int y2 = Math.min(this.y + this.height, yArgument + heightArgument);

        return new Bounds(x1, y1, x2 - x1, y2 - y1);
    }

    public Bounds intersect(Bounds bounds) {
        return intersect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public Bounds intersect(java.awt.Rectangle rect) {
        return intersect(rect.x, rect.y, rect.width, rect.height);
    }

    public Bounds translate(int dx, int dy) {
        return new Bounds(x + dx, y + dy, width, height);
    }

    public Bounds translate(Point offset) {
        return translate(offset.x, offset.y);
    }

    public boolean contains(Point point) {
        if (point == null) {
            throw new IllegalArgumentException("point is null");
        }

        return contains(point.x, point.y);
    }

    public boolean contains(int xArgument, int yArgument) {
        return (xArgument >= this.x
            && yArgument >= this.y
            && xArgument < this.x + width
            && yArgument < this.y + height);
    }

    public boolean contains(Bounds bounds) {
        if (bounds == null) {
            throw new IllegalArgumentException("bounds is null");
        }

        return contains(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public boolean contains(int xArgument, int yArgument, int widthArgument, int heightArgument) {
        return (!isEmpty()
            && xArgument >= this.x
            && yArgument >= this.y
            && xArgument + widthArgument <= this.x + this.width
            && yArgument + heightArgument <= this.y + this.height);
    }

    public boolean intersects(Bounds bounds) {
        if (bounds == null) {
            throw new IllegalArgumentException("bounds is null");
        }

        return intersects(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public boolean intersects(int xArgument, int yArgument, int widthArgument, int heightArgument) {
        return (!isEmpty()
            && xArgument + widthArgument > this.x
            && yArgument + heightArgument > this.y
            && xArgument < this.x + this.width
            && yArgument < this.y + this.height);
    }

    public boolean isEmpty() {
        return (width <= 0
            || height <= 0);
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof Bounds) {
            Bounds bounds = (Bounds)object;
            equals = (x == bounds.x
                && y == bounds.y
                && width == bounds.width
                && height == bounds.height);
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


    public java.awt.Rectangle toRectangle() {
        return new java.awt.Rectangle(x, y, width, height);
    }

    @Override
    public String toString() {
        return getClass().getName() + " [" + x + "," + y + ";" + width + "x" + height + "]";
    }

    public static Bounds decode(String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        Bounds bounds;
        try {
            bounds = new Bounds(JSONSerializer.parseMap(value));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }

        return bounds;
    }
}
