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
import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;

/**
 * Class representing the corner radii of a rectangular object.
 */
public final class CornerRadii implements Serializable {
    private static final long serialVersionUID = 0;

    public final int topLeft;
    public final int topRight;
    public final int bottomLeft;
    public final int bottomRight;

    public static final String TOP_LEFT_KEY = "topLeft";
    public static final String TOP_RIGHT_KEY = "topRight";
    public static final String BOTTOM_LEFT_KEY = "bottomLeft";
    public static final String BOTTOM_RIGHT_KEY = "bottomRight";

    /**
     * Corner radii whose top, left, bottom, and right values are all zero.
     */
    public static final CornerRadii NONE = new CornerRadii(0);

    public CornerRadii(int radius) {
        this(radius, radius, radius, radius);
    }

    public CornerRadii(CornerRadii cornerRadii) {
        if (cornerRadii == null) {
            throw new IllegalArgumentException("cornerRadii is null.");
        }

        this.topLeft = cornerRadii.topLeft;
        this.topRight = cornerRadii.topRight;
        this.bottomLeft = cornerRadii.bottomLeft;
        this.bottomRight = cornerRadii.bottomRight;
    }

    public CornerRadii(int topLeft, int topRight, int bottomLeft, int bottomRight) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
    }

    public CornerRadii(Dictionary<String, ?> cornerRadii) {
        if (cornerRadii == null) {
            throw new IllegalArgumentException("cornerRadii is null.");
        }

        if (cornerRadii.containsKey(TOP_LEFT_KEY)) {
            topLeft = (Integer)cornerRadii.get(TOP_LEFT_KEY);
        } else {
            topLeft = 0;
        }

        if (cornerRadii.containsKey(TOP_RIGHT_KEY)) {
            topRight = (Integer)cornerRadii.get(TOP_RIGHT_KEY);
        } else {
            topRight = 0;
        }

        if (cornerRadii.containsKey(BOTTOM_LEFT_KEY)) {
            bottomLeft = (Integer)cornerRadii.get(BOTTOM_LEFT_KEY);
        } else {
            bottomLeft = 0;
        }

        if (cornerRadii.containsKey(BOTTOM_RIGHT_KEY)) {
            bottomRight = (Integer)cornerRadii.get(BOTTOM_RIGHT_KEY);
        } else {
            bottomRight = 0;
        }
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof CornerRadii) {
            CornerRadii cornerRadii = (CornerRadii)object;
            equals = (topLeft == cornerRadii.topLeft
                && topRight == cornerRadii.topRight
                && bottomLeft == cornerRadii.bottomLeft
                && bottomRight == cornerRadii.bottomRight);
        }

        return equals;
    }

    @Override
    public int hashCode() {
        // TODO This may not be the most optimal hashing function
        return (topLeft * topRight) ^ (bottomLeft * bottomRight);
    }

    public String toString() {
        return getClass().getName() + " [" + topLeft + ", " + topRight
            + bottomLeft + ", " + bottomRight + "]";
    }

    public static CornerRadii decode(String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        CornerRadii cornerRadii;
        try {
            cornerRadii = new CornerRadii(JSONSerializer.parseMap(value));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }

        return cornerRadii;
    }
}
