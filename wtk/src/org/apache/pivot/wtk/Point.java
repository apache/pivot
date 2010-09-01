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
 * Class representing the location of an object.
 */
public final class Point implements Serializable {
    private static final long serialVersionUID = 5193175754909343769L;

    public final int x;
    public final int y;

    public static final String X_KEY = "x";
    public static final String Y_KEY = "y";

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point point) {
        if (point == null) {
            throw new IllegalArgumentException("point is null.");
        }

        this.x = point.x;
        this.y = point.y;
    }

    public Point(Dictionary<String, ?> point) {
        if (point == null) {
            throw new IllegalArgumentException("point is null.");
        }

        if (point.containsKey(X_KEY)) {
            x = (Integer)point.get(X_KEY);
        } else {
            x = 0;
        }

        if (point.containsKey(Y_KEY)) {
            y = (Integer)point.get(Y_KEY);
        } else {
            y = 0;
        }
    }

    public Point translate(int dx, int dy) {
        return new Point(x + dx, y + dy);
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof Point) {
            Point point = (Point)object;
            equals = (x == point.x
                && y == point.y);
        }

        return equals;
    }

    @Override
    public int hashCode() {
        return 31 * x  + y;
    }

    @Override
    public String toString() {
        return getClass().getName() + " [" + x + "," + y + "]";
    }

    public static Point decode(String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        Point point;
        try {
            point = new Point(JSONSerializer.parseMap(value));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }

        return point;
    }
}
