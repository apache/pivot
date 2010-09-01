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
 * Class representing the dimensions of an object.
 */
public final class Dimensions implements Serializable {
    private static final long serialVersionUID = -3644511824857807902L;

    public final int width;
    public final int height;

    public static final String WIDTH_KEY = "width";
    public static final String HEIGHT_KEY = "height";

    public Dimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Dimensions(Dimensions dimensions) {
        if (dimensions == null) {
            throw new IllegalArgumentException("dimensions is null.");
        }

        this.width = dimensions.width;
        this.height = dimensions.height;
    }

    public Dimensions(Dictionary<String, ?> dimensions) {
        if (dimensions == null) {
            throw new IllegalArgumentException("dimensions is null.");
        }

        if (dimensions.containsKey(WIDTH_KEY)) {
            width = (Integer)dimensions.get(WIDTH_KEY);
        } else {
            width = 0;
        }

        if (dimensions.containsKey(HEIGHT_KEY)) {
            height = (Integer)dimensions.get(HEIGHT_KEY);
        } else {
            height = 0;
        }
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof Dimensions) {
            Dimensions dimensions = (Dimensions)object;
            equals = (width == dimensions.width
                && height == dimensions.height);
        }

        return equals;
    }

    @Override
    public int hashCode() {
        return 31 * width + height;
    }

    @Override
    public String toString() {
        return getClass().getName() + " [" + width + "x" + height + "]";
    }

    public static Dimensions decode(String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        Dimensions dimensions;
        try {
            dimensions = new Dimensions(JSONSerializer.parseMap(value));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }

        return dimensions;
    }
}
