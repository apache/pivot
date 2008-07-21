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

public class Dimensions {
    public int width = 0;
    public int height = 0;

    public static final String WIDTH_KEY = "width";
    public static final String HEIGHT_KEY = "height";

    public Dimensions() {
        this(0, 0);
    }

    public Dimensions(String dimensions) {
        this(JSONSerializer.parseMap(dimensions));
    }

    public Dimensions(Dictionary<String, ?> dimensions) {
        this(0, 0);

        if (dimensions.containsKey(WIDTH_KEY)) {
            width = (Integer)dimensions.get(WIDTH_KEY);
        }

        if (dimensions.containsKey(HEIGHT_KEY)) {
            height = (Integer)dimensions.get(HEIGHT_KEY);
        }
    }

    public Dimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Dimensions(Dimensions dimensions) {
        this(dimensions.width, dimensions.height);
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void setSize(double width, double height) {
        this.width = (int)Math.round(width);
        this.height = (int)Math.round(height);
    }

    public final void setSize(Dimensions size) {
        setSize(size.getWidth(), size.getHeight());
    }

    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof Dimensions) {
            Dimensions dimensions = (Dimensions)object;
            equals = (width == dimensions.width
                && height == dimensions.height);
        }

        return equals;
    }

    public String toString() {
        return getClass().getName() + " [" + width + "x" + height + "]";
    }
}
