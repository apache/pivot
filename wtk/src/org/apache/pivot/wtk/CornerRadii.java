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
 * Class representing the corner radii of a rectangular object.
 */
public final class CornerRadii implements Serializable {
    private static final long serialVersionUID = -433469769555042467L;

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

        if (cornerRadii.topLeft < 0) {
            throw new IllegalArgumentException("cornerRadii.topLeft is negative.");
        }

        if (cornerRadii.topRight < 0) {
            throw new IllegalArgumentException("cornerRadii.topRight is negative.");
        }

        if (cornerRadii.bottomLeft < 0) {
            throw new IllegalArgumentException("cornerRadii.bottomLeft is negative.");
        }

        if (cornerRadii.bottomRight < 0) {
            throw new IllegalArgumentException("cornerRadii.bottomRight is negative.");
        }

        this.topLeft = cornerRadii.topLeft;
        this.topRight = cornerRadii.topRight;
        this.bottomLeft = cornerRadii.bottomLeft;
        this.bottomRight = cornerRadii.bottomRight;
    }

    public CornerRadii(int topLeft, int topRight, int bottomLeft, int bottomRight) {
        if (topLeft < 0) {
            throw new IllegalArgumentException("topLeft is negative.");
        }

        if (topRight < 0) {
            throw new IllegalArgumentException("topRight is negative.");
        }

        if (bottomLeft < 0) {
            throw new IllegalArgumentException("bottomLeft is negative.");
        }

        if (bottomRight < 0) {
            throw new IllegalArgumentException("bottomRight is negative.");
        }

        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
    }

    /**
     * Construct a {@link CornerRadii} object from a dictionary specifying values for
     * each of the four corners
     * @param cornerRadii A dictionary with keys {@value #TOP_LEFT_KEY},
     * {@value #TOP_RIGHT_KEY}, {@value #BOTTOM_LEFT_KEY}, {@value #BOTTOM_RIGHT_KEY}, all
     * with numeric values.  Omitted values are treated as zero.
     */
    public CornerRadii(Dictionary<String, ?> cornerRadii) {
        if (cornerRadii == null) {
            throw new IllegalArgumentException("cornerRadii is null.");
        }

        if (cornerRadii.containsKey(TOP_LEFT_KEY)) {
            topLeft = (Integer)cornerRadii.get(TOP_LEFT_KEY);

            if (topLeft < 0) {
                throw new IllegalArgumentException("\"topLeft\" is negative.");
            }
        } else {
            topLeft = 0;
        }

        if (cornerRadii.containsKey(TOP_RIGHT_KEY)) {
            topRight = (Integer)cornerRadii.get(TOP_RIGHT_KEY);

            if (topRight < 0) {
                throw new IllegalArgumentException("\"topRight\" is negative.");
            }
        } else {
            topRight = 0;
        }

        if (cornerRadii.containsKey(BOTTOM_LEFT_KEY)) {
            bottomLeft = (Integer)cornerRadii.get(BOTTOM_LEFT_KEY);

            if (bottomLeft < 0) {
                throw new IllegalArgumentException("\"bottomLeft\" is negative.");
            }
        } else {
            bottomLeft = 0;
        }

        if (cornerRadii.containsKey(BOTTOM_RIGHT_KEY)) {
            bottomRight = (Integer)cornerRadii.get(BOTTOM_RIGHT_KEY);

            if (bottomRight < 0) {
                throw new IllegalArgumentException("\"bottomRight\" is negative.");
            }
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
        final int prime = 31;
        int result = 1;
        result = prime * result + topLeft;
        result = prime * result + topRight;
        result = prime * result + bottomLeft;
        result = prime * result + bottomRight;
        return result;
    }

    @Override
    public String toString() {
        return getClass().getName() + " [" + topLeft + ", " + topRight
            + bottomLeft + ", " + bottomRight + "]";
    }

    public static CornerRadii decode(String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        CornerRadii cornerRadii;
        if (value.startsWith("{")) {
            try {
                cornerRadii = new CornerRadii(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            cornerRadii = new CornerRadii(Integer.parseInt(value));
        }

        return cornerRadii;
    }
}
