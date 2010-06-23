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
 * Class representing minimum and maximum values.
 */
public final class Limits implements Serializable {
    private static final long serialVersionUID = -1420266625812552298L;

    public final int minimum;
    public final int maximum;

    public static final String MINIMUM_KEY = "minimum";
    public static final String MAXIMUM_KEY = "maximum";

    public Limits(int minimum, int maximum) {
        if (minimum > maximum) {
            throw new IllegalArgumentException("minimum is greater than maximum.");
        }

        this.minimum = minimum;
        this.maximum = maximum;
    }

    public Limits(Limits limits) {
        if (limits == null) {
            throw new IllegalArgumentException("limits is null.");
        }

        minimum = limits.minimum;
        maximum = limits.maximum;
    }

    public Limits(Dictionary<String, ?> limits) {
        if (limits == null) {
            throw new IllegalArgumentException("limits is null.");
        }

        if (limits.containsKey(MINIMUM_KEY)) {
            minimum = (Integer)limits.get(MINIMUM_KEY);
        } else {
            minimum = Integer.MIN_VALUE;
        }

        if (limits.containsKey(MAXIMUM_KEY)) {
            maximum = (Integer)limits.get(MAXIMUM_KEY);
        } else {
            maximum = Integer.MAX_VALUE;
        }

        if (minimum > maximum) {
            throw new IllegalArgumentException("minimum is greater than maximum.");
        }
    }

    /**
     * Limits the specified value to the minimum and maximum values of
     * this object.
     *
     * @param value
     * The value to limit.
     *
     * @return
     * The bounded value.
     */
    public int constrain(int value) {
        if (value < minimum) {
            value = minimum;
        } else if (value > maximum) {
            value = maximum;
        }

        return value;
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof Limits) {
            Limits limits = (Limits)object;
            equals = (minimum == limits.minimum
                && maximum == limits.maximum);
        }

        return equals;
    }

    @Override
    public int hashCode() {
        return 31 * minimum + maximum;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(getClass().getName());
        buf.append(" [");

        if (minimum == Integer.MIN_VALUE) {
            buf.append("MIN");
        } else {
            buf.append(minimum);
        }

        buf.append("-");

        if (maximum == Integer.MAX_VALUE) {
            buf.append("MAX");
        } else {
            buf.append(maximum);
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
