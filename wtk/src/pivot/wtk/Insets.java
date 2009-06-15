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
 * Class representing the insets of an object.
 *
 * @author gbrown
 */
public final class Insets {
    public final int top;
    public final int left;
    public final int bottom;
    public final int right;

    public static final String TOP_KEY = "top";
    public static final String LEFT_KEY = "left";
    public static final String BOTTOM_KEY = "bottom";
    public static final String RIGHT_KEY = "right";

    public Insets(int inset) {
        top = inset;
        left = inset;
        bottom = inset;
        right = inset;
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
            top = ((Number)insets.get(TOP_KEY)).intValue();
        } else {
            top = 0;
        }

        if (insets.containsKey(LEFT_KEY)) {
            left = ((Number)insets.get(LEFT_KEY)).intValue();
        } else {
            left = 0;
        }

        if (insets.containsKey(BOTTOM_KEY)) {
            bottom = ((Number)insets.get(BOTTOM_KEY)).intValue();
        } else {
            bottom = 0;
        }

        if (insets.containsKey(RIGHT_KEY)) {
            right = ((Number)insets.get(RIGHT_KEY)).intValue();
        } else {
            right = 0;
        }
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof Insets) {
            Insets insets = (Insets)object;
            equals = (top == insets.top
                && left == insets.left
                && bottom == insets.bottom
                && right == insets.right);
        }

        return equals;
    }

    @Override
    public int hashCode() {
        // TODO This may not be the most optimal hashing function
        return (top * left) ^ (bottom * right);
    }

    public String toString() {
        return getClass().getName() + " [" + top + ", " + left + ", "
            + bottom + ", " + right + "]";
    }
}
