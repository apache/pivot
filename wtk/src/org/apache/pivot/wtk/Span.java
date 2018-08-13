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

import java.util.Comparator;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
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
    public Span(final int index) {
        start = index;
        end = index;
    }

    /**
     * Construct a new span with the given bounds.
     * @param start The start of this span - inclusive.
     * @param end The end of the span - inclusive.
     */
    public Span(final int start, final int end) {
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
    public Span(final Span span) {
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
    public Span(final Dictionary<String, ?> span) {
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
     * Construct a new span from the given sequence with two
     * numeric values corresponding to the start and end values
     * respectively.
     *
     * @param span A sequence containing the start and end values.
     * @throws IllegalArgumentException if the given span is {@code null}.
     */
    public Span(final Sequence<?> span) {
        Utils.checkNull(span, "span");

        start = ((Number) span.get(0)).intValue();
        end = ((Number) span.get(1)).intValue();
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
    public boolean contains(final Span span) {
        Utils.checkNull(span, "span");

        int otherNormalStart = span.normalStart();
        int otherNormalEnd = span.normalEnd();

        boolean contains;
        if (start < end) {
            contains = (start <= otherNormalStart && end >= otherNormalEnd);
        } else {
            contains = (end <= otherNormalStart && start >= otherNormalEnd);
        }

        return contains;
    }

    /**
     * Determines whether this span is adjacent to another span.
     * <p>Adjacency means that one end of this span is +/-1 from
     * either end of the other span (since start and end are inclusive).
     *
     * @param span The span to test for adjacency.
     * @return <tt>true</tt> if this span is adjacent <tt>span</tt>; <tt>false</tt>,
     * otherwise.
     * @throws IllegalArgumentException if the given span is {@code null}.
     */
    public boolean adjacentTo(final Span span) {
        Utils.checkNull(span, "span");

        int otherNormalStart = span.normalStart();
        int otherNormalEnd = span.normalEnd();

        boolean adjacentTo;
        if (start < end) {
            adjacentTo = (end + 1 == otherNormalStart || start - 1 == otherNormalEnd);
        } else {
            adjacentTo = (start + 1 == otherNormalStart || end - 1 == otherNormalEnd);
        }

        return adjacentTo;
    }

    /**
     * Determines whether this span is "before" another span.
     * <p>"Before" means that the normalized end of this span is &lt; the
     * normalized start of the other span.
    *
     * @param span The span to test.
     * @return <tt>true</tt> if this span is "before" <tt>span</tt>; <tt>false</tt>,
     * otherwise.
     * @throws IllegalArgumentException if the given span is {@code null}.
     */
    public boolean before(final Span span) {
        Utils.checkNull(span, "span");

        return normalEnd() < span.normalStart();
    }

    /**
     * Determines whether this span is "after" another span.
     * <p>"After" means that the normalized start of this span is &gt; the
     * normalized end of the other span.
    *
     * @param span The span to test.
     * @return <tt>true</tt> if this span is "after" <tt>span</tt>; <tt>false</tt>,
     * otherwise.
     * @throws IllegalArgumentException if the given span is {@code null}.
     */
    public boolean after(final Span span) {
        Utils.checkNull(span, "span");

        return normalStart() > span.normalEnd();
    }

    /**
     * Determines whether this span intersects with another span.
     *
     * @param span The span to test for intersection.
     * @return <tt>true</tt> if this span intersects with <tt>span</tt>;
     * <tt>false</tt>, otherwise.
     * @throws IllegalArgumentException if the given span is {@code null}.
     */
    public boolean intersects(final Span span) {
        Utils.checkNull(span, "span");

        int otherNormalStart = span.normalStart();
        int otherNormalEnd = span.normalEnd();

        boolean intersects;
        if (start < end) {
            intersects = (start <= otherNormalEnd && end >= otherNormalStart);
        } else {
            intersects = (end <= otherNormalEnd && start >= otherNormalStart);
        }

        return intersects;
    }

    /**
     * Calculates the intersection of this span and another span.
     *
     * @param span The span to intersect with this span.
     * @return A new Span instance representing the intersection of this span and
     * <tt>span</tt>, or <tt>null</tt> if the spans do not intersect.
     * @throws IllegalArgumentException if the given span is {@code null}.
     */
    public Span intersect(final Span span) {
        Utils.checkNull(span, "span");

        if (intersects(span)) {
            return new Span(Math.max(start, span.start), Math.min(end, span.end));
        }

        return null;
    }

    /**
     * Calculates the union of this span and another span.
     *
     * @param span The span to union with this span.
     * @return A new Span instance representing the union of this span and
     * <tt>span</tt>.
     */
    public Span union(final Span span) {
        Utils.checkNull(span, "span");

        return new Span(Math.min(start, span.start), Math.max(end, span.end));
    }

    /**
     * @return The normalized start of this span, which is the lesser of the
     * current start and end.
     */
    public int normalStart() {
        return Math.min(start, end);
    }

    /**
     * @return The normalized end of this span, which is the greater of the
     * current start and end.
     */
    public int normalEnd() {
        return Math.max(start, end);
    }

    /**
     * Create a span where the start value is less than or equal to the end value.
     *
     * @param start The new proposed start value.
     * @param end The new proposed end.
     * @return A span containing the normalized range.
     */
    public static Span normalize(final int start, final int end) {
        return new Span(Math.min(start, end), Math.max(start, end));
    }

    /**
     * @return A normalized equivalent of the span in which <tt>start</tt> is
     * guaranteed to be less than <tt>end</tt>.
     */
    public Span normalize() {
        return normalize(start, end);
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
    public Span offset(final int offset) {
        return new Span(this.start + offset, this.end + offset);
    }

    /**
     * Returns a new {@link Span} with the end value offset by the given value.
     *
     * @param offset The positive or negative amount by which to "lengthen" this
     * span (only the end).
     * @return A new {@link Span} with updated value.
     */
    public Span lengthen(final int offset) {
        return new Span(this.start, this.end + offset);
    }

    /**
     * Returns a new {@link Span} with the start value offset by the given value.
     *
     * @param offset The positive or negative amount by which to "shift" this
     * span (only the start).
     * @return A new {@link Span} with updated value.
     */
    public Span move(final int offset) {
        return new Span(this.start + offset, this.end);
    }

    /**
     * Decides whether the normalized version of this span is equal to the
     * normalized version of the other span.  Saves the overhead of making
     * a new object (with {@link #normalize}).
     *
     * @param span The span to test against this span.
     * @return Whether or not the normalized values of both spans are the same.
     * @throws IllegalArgumentException if the other span is {@code null}.
     */
    public boolean normalEquals(final Span span) {
        Utils.checkNull(span, "span");

        return (normalStart() == span.normalStart())
            && (normalEnd() == span.normalEnd());
    }

    @Override
    public boolean equals(final Object o) {
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
        return getClass().getSimpleName() + " {start:" + start + ", end:" + end + "}";
    }

    /**
     * Convert a string into a span.
     * <p> If the string value is a JSON map, then parse the map
     * and construct using the {@link #Span(Dictionary)} method.
     * <p> If the string value is a JSON list, then parse the list
     * and construct using the first two values as start and end
     * respectively, using the {@link #Span(int, int)} constructor.
     * <p> Also accepted is a simple list of two integer values
     * separated by comma or semicolon.
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
     * @see #Span(Dictionary)
     * @see #Span(Sequence)
     * @see #Span(int, int)
     * @see #Span(int)
     */
    public static Span decode(final String value) {
        Utils.checkNullOrEmpty(value, "value");

        Span span;
        if (value.startsWith("{")) {
            try {
                span = new Span(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else if (value.startsWith("[")) {
            try {
                span = new Span(JSONSerializer.parseList(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            String[] parts = value.split("\\s*[,;]\\s*");
            try {
                if (parts.length == 2) {
                    span = new Span(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                } else if (parts.length == 1) {
                    span = new Span(Integer.parseInt(value));
                } else {
                    throw new IllegalArgumentException("Unknown format for Span: " + value);
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        return span;
    }

    /** Comparator that determines the index of the first intersecting range. */
    public static final Comparator<Span> START_COMPARATOR = new Comparator<Span>() {
        @Override
        public int compare(final Span range1, final Span range2) {
            return (range1.end - range2.start);
        }
    };

    /** Comparator that determines the index of the last intersecting range. */
    public static final Comparator<Span> END_COMPARATOR = new Comparator<Span>() {
        @Override
        public int compare(final Span range1, final Span range2) {
            return (range1.start - range2.end);
        }
    };

    /** Comparator that determines if two ranges intersect. */
    public static final Comparator<Span> INTERSECTION_COMPARATOR = new Comparator<Span>() {
        @Override
        public int compare(final Span range1, final Span range2) {
            return (range1.start > range2.end) ? 1 : (range2.start > range1.end) ? -1 : 0;
        }
    };


}
