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

public class Point {
    public int x = 0;
    public int y = 0;

    public static final String X_KEY = "x";
    public static final String Y_KEY = "y";

    public Point() {
    }

    public Point(String point) {
        this(JSONSerializer.parseMap(point));
    }

    public Point(Dictionary<String, ?> point) {
        this(0, 0);

        if (point.containsKey(X_KEY)) {
            x = (Integer)point.get(X_KEY);
        }

        if (point.containsKey(Y_KEY)) {
            y = (Integer)point.get(Y_KEY);
        }
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point point) {
        this(point.x, point.y);
    }

    public void translate(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof Point) {
            Point point = (Point)object;
            equals = (x == point.x
                && y == point.y);
        }

        return equals;
    }

    public String toString() {
        return getClass().getName() + " [" + x + "," + y + "]";
    }
}
