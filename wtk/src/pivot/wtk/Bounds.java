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
package pivot.wtk;

import org.apache.pivot.collections.Dictionary;

/**
 * Class representing the bounds of an object.
 *
 * @author gbrown
 */
public final class Bounds {
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

    public Bounds union(int x, int y, int width, int height) {
        int x1 = Math.min(this.x, x);
        int y1 = Math.min(this.y, y);
        int x2 = Math.max(this.x + this.width, x + width);
        int y2 = Math.max(this.y + this.height, y + height);

        return new Bounds(x1, y1, x2 - x1, y2 - y1);

    }

    public Bounds union(Bounds bounds) {
        return union(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public Bounds intersect(int x, int y, int width, int height) {
        int x1 = Math.max(this.x, x);
        int y1 = Math.max(this.y, y);
        int x2 = Math.min(this.x + this.width, x + width);
        int y2 = Math.min(this.y + this.height, y + height);

        return new Bounds(x1, y1, x2 - x1, y2 - y1);
    }

    public Bounds intersect(Bounds bounds) {
        return intersect(bounds.x, bounds.y, bounds.width, bounds.height);
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

    public boolean contains(int x, int y) {
        return (x >= this.x
            && y >= this.y
            && x < this.x + width
            && y < this.y + height);
    }

    public boolean contains(Bounds bounds) {
        if (bounds == null) {
            throw new IllegalArgumentException("bounds is null");
        }

        return contains(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public boolean contains(int x, int y, int width, int height) {
        return (!isEmpty()
            && x >= this.x
            && y >= this.y
            && x + width <= this.x + this.width
            && y + height <= this.y + this.height);
    }

    public boolean intersects(Bounds bounds) {
        if (bounds == null) {
            throw new IllegalArgumentException("bounds is null");
        }

        return intersects(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public boolean intersects(int x, int y, int width, int height) {
        return (!isEmpty()
            && x + width > this.x
            && y + height > this.y
            && x < this.x + this.width
            && y < this.y + this.height);
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
        // TODO This may not be the most optimal hashing function
        return (x * y) ^ (width * height);
    }

    public java.awt.Rectangle toRectangle() {
        return new java.awt.Rectangle(x, y, width, height);
    }

    public String toString() {
        return getClass().getName() + " [" + x + "," + y + ";" + width + "x" + height + "]";
    }
}
