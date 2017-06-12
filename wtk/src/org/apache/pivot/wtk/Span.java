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

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Utils;

/**
 * Class representing a range of integer values. The range includes all values
 * in the interval <i>[start, end]</i>. Values may be negative, and the value of
 * <tt>start</tt> may be less than or equal to the value of <tt>end</tt>.
 */
public final class Span {
    public final int start;
    public final int end;

    public static final String START_KEY = "start";
    public static final String END_KEY = "end";

    /**
     * Construct a new span of length 1 at the given location.
     *
     * @param index The start and end of this span (inclusive).
     */
    public Span(int index) {
        start = index;
        end = index;
    }

    /**
     * Construct a new span with the given bounds.
     * @param start The start of this span - inclusive.
     * @param end The end of the span - inclusive.
     */
    public Span(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Construct a new span from another one (a "copy
     * constructor").
     *
     * @param span An existing span (which must not be {@code null}).
     * @throws IllegalArgumentException if the given span is {@code null}.
     */
    public Span(Span span) {
        Utils.checkNull(span, "span");

        start = span.start;
        end = span.end;
    }

    /**
     * Construct a new span from the given dictionary which must
     * contain the {@link #START_KEY} and {@link #END_KEY} keys.
     *
     * @param span A dictionary containing start and end values.
     * @throws IllegalArgumentException if the given span is {@code null}
     * or if the dictionary does not contain the start and end keys.
     */
    public Span(Dictionary<String, ?> span) {
    	Utils.checkNull(span, "span");

        if (!span.containsKey(START_KEY)) {
            throw new IllegalArgumentException(START_KEY + " is required.");
        }

        if (!span.containsKey(END_KEY)) {
            throw new IllegalArgumentException(END_KEY + " is required.");
        }

        start = span.getInt(START_KEY);
        end = span.getInt(END_KEY);
    }

    /**
     * Returns the length of the span.
     *
     * @return The absolute value of (<tt>end</tt> minus <tt>start</tt>) + 1.
     */
    public long getLength() {
        return Math.abs((long) end - (long) start) + 1;
    }

    /**
     * Determines whether this span contains another span.
     *
     * @param span The span to test for containment.
     * @return <tt>true</tt> if this span contains <tt>span</tt>; <tt>false</tt>,
     * otherwise.
     * @throws IllegalArgumentException if the given span is {@code null}.
     */
    public boolean contains(Span span) {
        Utils.checkNull(span, "span");

        Span normalizedSpan = span.normalize();

        boolean contains;
        if (start < end) {
            contains = (start <= normalizedSpan.start && end >= normalizedSpan.end);
        } else {
            contains = (end <= normalizedSpan.start && start >= normalizedSpan.end);
        }

        return contains;
    }

    /**
     * Determines whether this span intersects with another span.
     *
     * @param span The span to test for intersection.
     * @return <tt>true</tt> if this span intersects with <tt>span</tt>;
     * <tt>false</tt>, otherwise.
     * @throws IllegalArgumentException if the given span is {@code null}.
     */
    public boolean intersects(Span span) {
        Utils.checkNull(span, "span");

        Span normalizedSpan = span.normalize();

        boolean intersects;
        if (start < end) {
            intersects = (start <= normalizedSpan.end && end >= normalizedSpan.start);
        } else {
            intersects = (end <= normalizedSpan.end && start >= normalizedSpan.start);
        }

        return intersects;
    }

    /**
     * Calculates the intersection of this span and another span.
     *
     * @param span The span to intersect with this span.
     * @return A new Span instance representing the intersection of this span and
     * <tt>span</tt>, or null if the spans do not intersect.
     * @throws IllegalArgumentException if the given span is {@code null}.
     */
    public Span intersect(Span span) {
        Utils.checkNull(span, "span");

        Span intersection = null;

        if (intersects(span)) {
            intersection = new Span(Math.max(start, span.start), Math.min(end, span.end));
        }

        return intersection;
    }

    /**
     * Calculates the union of this span and another span.
     *
     * @param span The span to union with this span.
     * @return A new Span instance representing the union of this span and
     * <tt>span</tt>.
     */
    public Span union(Span span) {
        Utils.checkNull(span, "span");

        return new Span(Math.min(start, span.start), Math.max(end, span.end));
    }

    /**
     * @return A normalized equivalent of the span in which <tt>start</tt> is
     * guaranteed to be less than <tt>end</tt>.
     */
    public Span normalize() {
        return new Span(Math.min(start, end), Math.max(start, end));
    }

    /**
     * Returns a new {@link Span} with both values offset by the given value.
     * <p> This is useful while moving through a {@link TextPane} document
     * for instance, where you have to subtract off the starting offset for
     * child nodes.
     *
     * @param offset The positive or negative amount by which to "move" this
     * span (both start and end).
     * @return A new {@link Span} with updated values.
     */
    public Span offset(int offset) {
        return new Span(this.start + offset, this.end + offset);
    }

    @Override
    public boolean equals(Object o) {
        boolean equal = false;

        if (o instanceof Span) {
            Span span = (Span) o;
            equal = (start == span.start && end == span.end);
        }

        return equal;
    }

    @Override
    public int hashCode() {
        return 31 * start + end;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " {start: " + start + ", end: " + end + "}";
    }

    /**
     * Convert a string into a span.
     * <p> If the string value is a JSON map, then parse the map
     * and construct using the {@link #Span(Dictionary)} method.
     * <p> If the string value is a JSON list, then parse the list
     * and construct using the first two values as start and end
     * respectively, using the {@link #Span(int, int)} constructor.
     * <p> Otherwise the string should be a single integer value
     * that will be used to construct the span using the {@link #Span(int)}
     * constructor.
     *
     * @param value The string value to decode into a new span.
     * @return The decoded span.
     * @throws IllegalArgumentException if the value is {@code null} or
     * if the string starts with <code>"{"</code> but it cannot be parsed as
     * a JSON map, or if it starts with <code>"["</code> but cannot be parsed
     * as a JSON list.
     */
    public static Span decode(String value) {
        Utils.checkNull(value, "value");

        Span span;
        if (value.startsWith("{")) {
            try {
                span = new Span(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else if (value.startsWith("[")) {
            try {
                @SuppressWarnings("unchecked")
                List<Integer> values = (List<Integer>)JSONSerializer.parseList(value);
                span = new Span(values.get(0), values.get(1));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            span = new Span(Integer.parseInt(value));
        }

        return span;
    }
}
