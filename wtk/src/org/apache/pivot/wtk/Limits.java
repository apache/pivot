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
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Utils;

/**
 * Immutable object representing minimum and maximum values.
 * <p> Note: these values are inclusive, and so the minimum can be
 * equal to the maximum, implying a range of one value.
 * <p> Also note that minimum must be less than or equal the maximum
 * at construction or decode time.
 * @see #contains
 * @see #constrain
 */
public final class Limits implements Serializable {
    private static final long serialVersionUID = -1420266625812552298L;

    public final int minimum;
    public final int maximum;

    public static final String MINIMUM_KEY = "minimum";
    public static final String MAXIMUM_KEY = "maximum";

    public Limits() {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public Limits(final int minimum, final int maximum) {
        if (minimum > maximum) {
            throw new IllegalArgumentException("minimum is greater than maximum.");
        }

        this.minimum = minimum;
        this.maximum = maximum;
    }

    /**
     * Construct a new limits of a single value, setting both
     * the minimum and maximum to this value.
     *
     * @param value The single value range for this limits.
     */
    public Limits(final int value) {
        this(value, value);
    }

    public Limits(final Limits limits) {
        Utils.checkNull(limits, "limits");

        minimum = limits.minimum;
        maximum = limits.maximum;
    }

    /**
     * Construct a new Limits based on the given Dictionary or Map.
     * <p> The map keys for the values are {@link #MINIMUM_KEY} and
     * {@link #MAXIMUM_KEY}.  Missing minimum value will set {@link Integer#MIN_VALUE}
     * as the min, and missing maximum will set {@link Integer#MAX_VALUE} as the max.
     *
     * @param limits The map/dictionary containing the desired limits values.
     * @throws IllegalArgumentException if the min is greater than the max.
     */
    public Limits(final Dictionary<String, ?> limits) {
        Utils.checkNull(limits, "limits");

        minimum = limits.getInt(MINIMUM_KEY, Integer.MIN_VALUE);
        maximum = limits.getInt(MAXIMUM_KEY, Integer.MAX_VALUE);

        if (minimum > maximum) {
            throw new IllegalArgumentException("minimum is greater than maximum.");
        }
    }

    public Limits(final Sequence<?> limits) {
        Utils.checkNull(limits, "limits");

        minimum = ((Number) limits.get(0)).intValue();
        maximum = ((Number) limits.get(1)).intValue();

        if (minimum > maximum) {
            throw new IllegalArgumentException("minimum is greater than maximum.");
        }
    }

    /**
     * @return The range of this limits, that is, the maximum less the minimum
     * plus one (since the limits are inclusive). Returns a long value because
     * the default min and max are the maximum range of the integers, so that
     * the range in this case cannot be represented by an integer.
     */
    public long range() {
        return ((long) maximum - (long) minimum + 1L);
    }

    /**
     * Limits the specified value to the minimum and maximum values of this
     * object.
     *
     * @param value The value to limit.
     * @return The bounded value.
     */
    public int constrain(final int value) {
        if (value < minimum) {
            return minimum;
        } else if (value > maximum) {
            return maximum;
        }

        return value;
    }

    /**
     * Determines whether the given value is contained by this Limits, that is,
     * whether the value is &gt;= the minimum and &lt;= the maximum.
     *
     * @param value The value to test.
     * @return Whether the value is contained within the limits.
     */
    public boolean contains(final int value) {
        return (value >= minimum) && (value <= maximum);
    }

    @Override
    public boolean equals(final Object object) {
        boolean equals = false;

        if (object instanceof Limits) {
            Limits limits = (Limits) object;
            equals = (minimum == limits.minimum && maximum == limits.maximum);
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

        buf.append(getClass().getSimpleName());
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

    /**
     * Decode a JSON-encoded string (map or list) that contains the values for a new limits.
     * <p> The format of a JSON map format will be:
     * <pre>{ "minimum": nnn, "maximum": nnn }</pre>
     * <p> The format of a JSON list format will be:
     * <pre>[ minimum, maximum ]</pre>
     * <p> Also accepted is a simple comma- or semicolon-separated list of two
     * integer values, as in: <pre>min, max</pre>, or as <pre>min-max</pre> (as
     * in the format produced by {@link #toString}).
     *
     * @param value The JSON string containing the map or list of limits values
     * (must not be {@code null}).
     * @return The new limits object if the string can be successfully decoded.
     * @throws IllegalArgumentException if the given string is {@code null} or
     * empty or the string could not be parsed as a JSON map or list.
     * @see #Limits(Dictionary)
     * @see #Limits(int, int)
     */
    public static Limits decode(final String value) {
        Utils.checkNull(value, "value");

        Limits limits;
        if (value.startsWith("{")) {
            try {
                limits = new Limits(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else if (value.startsWith("[")) {
            try {
                limits = new Limits(JSONSerializer.parseList(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            String[] parts = value.split("\\s*[,;\\-]\\s*");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid format for Limits: " + value);
            }
            try {
                limits = new Limits(
                    Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        return limits;
    }
}
