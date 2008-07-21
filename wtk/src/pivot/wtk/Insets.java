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

public class Insets {
    public int top = 0;
    public int left = 0;
    public int bottom = 0;
    public int right = 0;

    public static final String TOP_KEY = "top";
    public static final String LEFT_KEY = "left";
    public static final String BOTTOM_KEY = "bottom";
    public static final String RIGHT_KEY = "right";

    public Insets(int inset) {
        this(inset, inset, inset, inset);
    }

    public Insets(String insets) {
        this(JSONSerializer.parseMap(insets));
    }

    public Insets(Dictionary<String, ?> insets) {
        this(0, 0, 0, 0);

        if (insets.containsKey(TOP_KEY)) {
            top = ((Number)insets.get(TOP_KEY)).intValue();
        }

        if (insets.containsKey(LEFT_KEY)) {
            left = ((Number)insets.get(LEFT_KEY)).intValue();
        }

        if (insets.containsKey(BOTTOM_KEY)) {
            bottom = ((Number)insets.get(BOTTOM_KEY)).intValue();
        }

        if (insets.containsKey(RIGHT_KEY)) {
            right = ((Number)insets.get(RIGHT_KEY)).intValue();
        }
    }

    public Insets(int top, int left, int bottom, int right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    public Insets(Insets insets) {
        this(insets.top, insets.left, insets.bottom, insets.right);
    }

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

    public String toString() {
        return getClass() + " [" + top + ", " + left + ", "
            + bottom + ", " + right + "]";
    }
}
