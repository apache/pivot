/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import pivot.collections.Dictionary;
import pivot.serialization.JSONSerializer;

/**
 * <p>Class representing the bounds of an object.</p>
 *
 * @author gbrown
 */
public class Bounds {
    public int x = 0;
    public int y = 0;
    public int width = 0;
    public int height = 0;

    public static final String X_KEY = "x";
    public static final String Y_KEY = "y";
    public static final String WIDTH_KEY = "width";
    public static final String HEIGHT_KEY = "height";

    public Bounds() {
    }

    public Bounds(String rectangle) {
        this(JSONSerializer.parseMap(rectangle));
    }

    public Bounds(Dictionary<String, ?> rectangle) {
        if (rectangle.containsKey(X_KEY)) {
            x = (Integer)rectangle.get(X_KEY);
        }

        if (rectangle.containsKey(Y_KEY)) {
            y = (Integer)rectangle.get(Y_KEY);
        }

        if (rectangle.containsKey(WIDTH_KEY)) {
            width = (Integer)rectangle.get(WIDTH_KEY);
        }

        if (rectangle.containsKey(HEIGHT_KEY)) {
            height = (Integer)rectangle.get(HEIGHT_KEY);
        }
    }

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

    protected Bounds(java.awt.Rectangle rectangle) {
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

    public void union(Bounds rectangle) {
        int x1 = Math.min(x, rectangle.x);
        int y1 = Math.min(y, rectangle.y);
        int x2 = Math.max(x + width, rectangle.x + rectangle.width);
        int y2 = Math.max(y + height, rectangle.y + rectangle.height);

        this.x = x1;
        this.y = y1;
        this.width = x2 - x1;
        this.height = y2 - y1;
    }

    public void intersect(Bounds rectangle) {
        int x1 = Math.max(x, rectangle.x);
        int y1 = Math.max(y, rectangle.y);
        int x2 = Math.min(x + width, rectangle.x + rectangle.width);
        int y2 = Math.min(y + height, rectangle.y + rectangle.height);

        this.x = x1;
        this.y = y1;
        this.width = x2 - x1;
        this.height = y2 - y1;
    }

    public void translate(int dx, int dy) {
        this.x += dx;
        this.y += dy;
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

    public boolean intersects(Bounds rectangle) {
        if (rectangle == null) {
            throw new IllegalArgumentException("rectangle is null");
        }

        return intersects(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
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

    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof Bounds) {
            Bounds rectangle = (Bounds)object;
            equals = (x == rectangle.x
                && y == rectangle.y
                && width == rectangle.width
                && height == rectangle.height);
        }

        return equals;
    }

    public String toString() {
        return getClass().getName() + " [" + x + "," + y + ";" + width + "x" + height + "]";
    }
}
