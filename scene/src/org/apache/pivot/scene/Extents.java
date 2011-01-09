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
package org.apache.pivot.scene;

import java.util.Map;

import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.io.SerializationException;
import org.apache.pivot.json.JSONSerializer;

/**
 * Class representing the extents of an object.
 */
public class Extents {
    public final int minimumX;
    public final int maximumX;
    public final int minimumY;
    public final int maximumY;

    public static final String MINIMUM_X_KEY = "minimumX";
    public static final String MAXIMUM_X_KEY = "maximumX";
    public static final String MINIMUM_Y_KEY = "minimumY";
    public static final String MAXIMUM_Y_KEY = "maximumX";

    public static final Extents EMPTY = new Extents(0, 0, 0, 0);

    public Extents(int minimumX, int maximumX, int minimumY, int maximumY) {
        this.minimumX = minimumX;
        this.maximumX = maximumX;
        this.minimumY = minimumY;
        this.maximumY = maximumY;
    }

    public Extents(Limits xLimits, Limits yLimits) {
        if (xLimits == null) {
            throw new IllegalArgumentException("xLimits is null.");
        }

        if (yLimits == null) {
            throw new IllegalArgumentException("yLimits is null.");
        }

        this.minimumX = xLimits.minimum;
        this.maximumX = xLimits.maximum;
        this.minimumY = yLimits.minimum;
        this.maximumY = yLimits.maximum;
    }

    public Extents(Extents extents) {
        if (extents == null) {
            throw new IllegalArgumentException("extents is null.");
        }

        this.minimumX = extents.minimumX;
        this.maximumX = extents.maximumX;
        this.minimumY = extents.minimumY;
        this.maximumY = extents.maximumY;
    }

    public Limits getXLimits() {
        return new Limits(minimumX, maximumX);
    }

    public Limits getYLimits() {
        return new Limits(minimumY, maximumY);
    }

    public Extents union(int minimumX, int maximumX, int minimumY, int maximumY) {
        minimumX = Math.min(this.minimumX, minimumX);
        maximumX = Math.max(this.maximumX, maximumX);
        minimumY = Math.min(this.minimumY, minimumY);
        maximumY = Math.max(this.maximumY, maximumY);

        return new Extents(minimumX, maximumX, minimumY, maximumY);

    }

    public Extents union(Extents extents) {
        return union(extents.minimumX, extents.maximumX, extents.minimumY, extents.maximumY);
    }

    public Extents intersect(int minimumX, int maximumX, int minimumY, int maximumY) {
        minimumX = Math.max(this.minimumX, minimumX);
        maximumX = Math.min(this.maximumX, maximumX);
        minimumY = Math.max(this.minimumY, minimumY);
        maximumY = Math.min(this.maximumY, maximumY);

        return new Extents(minimumX, maximumX, minimumY, maximumY);
    }

    public Extents intersect(Extents extents) {
        return intersect(extents.minimumX, extents.maximumX, extents.minimumY, extents.maximumY);
    }

    public Extents translate(int dx, int dy) {
        return new Extents(minimumX + dx, maximumX + dx, minimumY + dy, maximumY + dy);
    }

    public Extents translate(Point offset) {
        if (offset == null) {
            throw new IllegalArgumentException("offset is null");
        }

        return translate(offset.x, offset.y);
    }

    public boolean contains(Point point) {
        if (point == null) {
            throw new IllegalArgumentException("point is null");
        }

        return contains(point.x, point.y);
    }

    public boolean contains(int x, int y) {
        return (x >= this.minimumX
            && x < this.maximumX
            && y >= this.minimumY
            && y < this.maximumY);
    }

    public boolean contains(Extents extents) {
        if (extents == null) {
            throw new IllegalArgumentException("extents is null");
        }

        return contains(extents.minimumX, extents.maximumX, extents.minimumY, extents.maximumY);
    }

    public boolean contains(int minimumX, int maximumX, int minimumY, int maximumY) {
        return (!isEmpty()
            && minimumX >= this.minimumX
            && maximumX < this.maximumX
            && minimumY >= this.maximumX
            && maximumY < this.maximumY);
    }

    public boolean intersects(Extents extents) {
        if (extents == null) {
            throw new IllegalArgumentException("extents is null");
        }

        return intersects(extents.minimumX, extents.maximumX, extents.minimumY, extents.maximumY);
    }

    public boolean intersects(int minimumX, int maximumX, int minimumY, int maximumY) {
        return (!isEmpty()
            && maximumX > this.minimumX
            && minimumX < this.maximumX
            && maximumY > this.minimumY
            && minimumY < this.maximumY);
    }

    public boolean isEmpty() {
        return (maximumX <= minimumX
            || maximumY <= minimumY);
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof Extents) {
            Extents extents = (Extents)object;
            equals = (minimumX == extents.minimumX
                && maximumX == extents.maximumX
                && minimumY == extents.minimumY
                && maximumY == extents.maximumY);
        }

        return equals;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + minimumX;
        result = prime * result + maximumX;
        result = prime * result + minimumY;
        result = prime * result + maximumY;

        return result;
    }

    @Override
    public String toString() {
        return getClass().getName() + " [" + minimumX + ".." + maximumX  + ";" + minimumY + ".." + maximumY + "]";
    }

    public static Extents decode(String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        Map<String, ?> map;
        try {
            map = JSONSerializer.parseMap(value);
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }

        int minimumX = BeanAdapter.getInt(map, MINIMUM_X_KEY);
        int maximumX = BeanAdapter.getInt(map, MAXIMUM_X_KEY);
        int minimumY = BeanAdapter.getInt(map, MINIMUM_Y_KEY);
        int maximumY = BeanAdapter.getInt(map, MINIMUM_Y_KEY);

        return new Extents(minimumX, maximumX, minimumY, maximumY);
    }
}
