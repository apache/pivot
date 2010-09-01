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
 * Class representing the insets of an object.
 */
public final class Insets implements Serializable {
    private static final long serialVersionUID = -8528862892185591370L;

    public final int top;
    public final int left;
    public final int bottom;
    public final int right;

    public static final String TOP_KEY = "top";
    public static final String LEFT_KEY = "left";
    public static final String BOTTOM_KEY = "bottom";
    public static final String RIGHT_KEY = "right";

    /**
     * Insets whose top, left, bottom, and right values are all zero.
     */
    public static final Insets NONE = new Insets(0);

    public Insets(int inset) {
        this.top = inset;
        this.left = inset;
        this.bottom = inset;
        this.right = inset;
    }

    public Insets(int top, int left, int bottom, int right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    public Insets(Insets insets) {
        if (insets == null) {
            throw new IllegalArgumentException("insets is null.");
        }

        this.top = insets.top;
        this.left = insets.left;
        this.bottom = insets.bottom;
        this.right = insets.right;
    }

    public Insets(Dictionary<String, ?> insets) {
        if (insets == null) {
            throw new IllegalArgumentException("insets is null.");
        }

        if (insets.containsKey(TOP_KEY)) {
            top = ((Number) insets.get(TOP_KEY)).intValue();
        } else {
            top = 0;
        }

        if (insets.containsKey(LEFT_KEY)) {
            left = ((Number) insets.get(LEFT_KEY)).intValue();
        } else {
            left = 0;
        }

        if (insets.containsKey(BOTTOM_KEY)) {
            bottom = ((Number) insets.get(BOTTOM_KEY)).intValue();
        } else {
            bottom = 0;
        }

        if (insets.containsKey(RIGHT_KEY)) {
            right = ((Number) insets.get(RIGHT_KEY)).intValue();
        } else {
            right = 0;
        }
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof Insets) {
            Insets insets = (Insets) object;
            equals = (top == insets.top && left == insets.left && bottom == insets.bottom && right == insets.right);
        }

        return equals;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + top;
        result = prime * result + left;
        result = prime * result + bottom;
        result = prime * result + right;
        return result;
    }

    @Override
    public String toString() {
        return getClass().getName() + " [" + top + ", " + left + ", " + bottom + ", " + right + "]";
    }

    public static Insets decode(String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        Insets insets;
        if (value.startsWith("{")) {
            try {
                insets = new Insets(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            insets = new Insets(Integer.parseInt(value));
        }

        return insets;
    }
}
