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
 * Class representing minimum and maximum values.
 *
 * @author tvolkert
 */
public final class Limits implements Serializable {
    private static final long serialVersionUID = 0;

    public final int min;
    public final int max;

    public static final String MIN_KEY = "min";
    public static final String MAX_KEY = "max";

    public Limits(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min > max");
        }

        this.min = min;
        this.max = max;
    }

    public Limits(Limits limits) {
        if (limits == null) {
            throw new IllegalArgumentException("limits is null.");
        }

        min = limits.min;
        max = limits.max;
    }

    public Limits(Dictionary<String, ?> limits) {
        if (limits == null) {
            throw new IllegalArgumentException("limits is null.");
        }

        if (limits.containsKey(MIN_KEY)) {
            min = (Integer)limits.get(MIN_KEY);
        } else {
            min = Integer.MIN_VALUE;
        }

        if (limits.containsKey(MAX_KEY)) {
            max = (Integer)limits.get(MAX_KEY);
        } else {
            max = Integer.MAX_VALUE;
        }

        if (min > max) {
            throw new IllegalArgumentException("min > max");
        }
    }

    /**
     * Limits the specified value to the min and max values of this object.
     *
     * @param value
     * The value to limit
     *
     * @return
     * The bounded value
     */
    public int limit(int value) {
        if (value < min) {
            value = min;
        } else if (value > max) {
            value = max;
        }

        return value;
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof Limits) {
            Limits limits = (Limits)object;
            equals = (min == limits.min
                && max == limits.max);
        }

        return equals;
    }

    @Override
    public int hashCode() {
        // TODO This may not be the most optimal hashing function
        return min * max;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(getClass().getName());
        buf.append(" [");

        if (min == Integer.MIN_VALUE) {
            buf.append("MIN");
        } else {
            buf.append(min);
        }

        buf.append("-");

        if (max == Integer.MAX_VALUE) {
            buf.append("MAX");
        } else {
            buf.append(max);
        }

        buf.append("]");

        return buf.toString();
    }

    public static Limits decode(String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        Limits limits;
        try {
            limits = new Limits(JSONSerializer.parseMap(value));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }

        return limits;
    }
}
