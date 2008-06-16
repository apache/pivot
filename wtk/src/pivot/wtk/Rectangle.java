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

import java.awt.geom.Rectangle2D;
import pivot.collections.Dictionary;

public class Rectangle extends Rectangle2D {
    public int x = 0;
    public int y = 0;
    public int width = 0;
    public int height = 0;

    public static final String X_KEY = "x";
    public static final String Y_KEY = "y";
    public static final String WIDTH_KEY = "width";
    public static final String HEIGHT_KEY = "height";

    public Rectangle() {
    }

    public Rectangle(Dictionary<String, ?> rectangle) {
        this(0, 0, 0, 0);

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

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle(Point origin, Dimensions size) {
        this(origin.x, origin.y, size.width, size.height);
    }

    public Rectangle(Rectangle rectangle) {
        this(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Point getLocation() {
        return new Point(x, y);
    }

    public Dimensions getSize() {
        return new Dimensions(width, height);
    }

    public void setBounds(double x, double y, double width, double height) {
        this.x = (int)Math.floor(x);
        this.y = (int)Math.floor(y);
        this.width = (int)Math.ceil(x + width);
        this.height = (int)Math.ceil(y + height);
    }

    public Rectangle createUnion(Rectangle rectangle) {
        Rectangle destination = new Rectangle();
        union(this, rectangle, destination);
        return destination;
    }

    public Rectangle2D createUnion(Rectangle2D rectangle) {
        Rectangle2D.Double destination = new Rectangle2D.Double();
        union(this, rectangle, destination);
        return destination;
    }

    public Rectangle createIntersection(Rectangle rectangle) {
        Rectangle destination = new Rectangle();
        intersect(this, rectangle, destination);
        return destination;
    }

    public Rectangle2D createIntersection(Rectangle2D rectangle) {
        Rectangle2D.Double destination = new Rectangle2D.Double();
        intersect(this, rectangle, destination);
        return destination;
    }

    public void setRect(double x, double y, double width, double height) {
        setBounds(x, y, width, height);
    }

    public boolean isEmpty() {
        return (width <= 0
            || height <= 0);
    }

    public int outcode(double x, double y) {
        // TODO Is this the best way to do this? java.awt.Rectangle does
        // something similar but warns of side effects related to bug 4320890.
        Rectangle2D.Double rectangle = new Rectangle2D.Double(x, y, width, height);
        return rectangle.outcode(x, y);
    }

    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof Rectangle) {
            Rectangle rectangle = (Rectangle)object;
            equals = (x == rectangle.x
                && y == rectangle.y
                && width == rectangle.width
                && height == rectangle.height);
        }
        else {
            equals = super.equals(object);
        }

        return equals;
    }

    public String toString() {
        return getClass().getName() + " [" + x + "," + y + ";" + width + "x" + height + "]";
    }
}
