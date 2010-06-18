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
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;

/**
 * Class representing a range of integer values. The range includes all
 * values in the interval <i>[start, end]</i>. Values may be negative, and the
 * value of <tt>start</tt> may be less than or equal to the value of
 * <tt>end</tt>.
 */
public final class Span {
    public final int start;
    public final int end;

    public static final String START_KEY = "start";
    public static final String END_KEY = "end";

    public Span(int index) {
        start = index;
        end = index;
    }

    public Span(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public Span(Span span) {
        if (span == null) {
            throw new IllegalArgumentException("span is null.");
        }

        start = span.start;
        end = span.end;
    }

    public Span(Dictionary<String, ?> span) {
        if (span == null) {
            throw new IllegalArgumentException("span is null.");
        }

        if (!span.containsKey(START_KEY)) {
            throw new IllegalArgumentException(START_KEY + " is required.");
        }

        if (!span.containsKey(END_KEY)) {
            throw new IllegalArgumentException(END_KEY + " is required.");
        }

        start = (Integer)span.get(START_KEY);
        end = (Integer)span.get(END_KEY);
    }

    /**
     * Returns the length of the span.
     *
     * @return
     * The absolute value of (<tt>end</tt> minus <tt>start</tt>) + 1.
     */
    public long getLength() {
        return Math.abs((long)end - (long)start) + 1;
    }

    /**
     * Determines whether this span contains another span.
     *
     * @param span
     * The span to test for containment.
     *
     * @return
     * <tt>true</tt> if this span contains <tt>span</tt>; <tt>false</tt>,
     * otherwise.
     */
    public boolean contains(Span span) {
        if (span == null) {
            throw new IllegalArgumentException("span is null.");
        }

        Span normalizedSpan = span.normalize();

        boolean contains;
        if (start < end) {
            contains = (start <= normalizedSpan.start
                && end >= normalizedSpan.end);
        } else {
            contains = (end <= normalizedSpan.start
                && start >= normalizedSpan.end);
        }

        return contains;
    }

    /**
     * Determines whether this span intersects with another span.
     *
     * @param span
     * The span to test for intersection.
     *
     * @return
     * <tt>true</tt> if this span intersects with <tt>span</tt>;
     * <tt>false</tt>, otherwise.
     */
    public boolean intersects(Span span) {
        if (span == null) {
            throw new IllegalArgumentException("span is null.");
        }

        Span normalizedSpan = span.normalize();

        boolean intersects;
        if (start < end) {
            intersects = (start <= normalizedSpan.end
                && end >= normalizedSpan.start);
        } else {
            intersects = (end <= normalizedSpan.end
                && start >= normalizedSpan.start);
        }

        return intersects;
    }

    /**
     * Calculates the intersection of this span and another span.
     *
     * @param span
     * The span to intersect with this span.
     *
     * @return
     * A new Span instance representing the intersection of this span and
     * <tt>span</tt>, or null if the spans do not intersect.
     */
    public Span intersect(Span span) {
        if (span == null) {
            throw new IllegalArgumentException("span is null.");
        }

        Span intersection = null;

        if (intersects(span)) {
            intersection = new Span(Math.max(start, span.start),
                Math.min(end, span.end));
        }

        return intersection;
    }

    /**
     * Calculates the union of this span and another span.
     *
     * @param span
     * The span to union with this span.
     *
     * @return
     * A new Span instance representing the union of this span and
     * <tt>span</tt>.
     */
    public Span union(Span span) {
        if (span == null) {
            throw new IllegalArgumentException("span is null.");
        }

        return new Span(Math.min(start, span.start),
            Math.max(end, span.end));
    }

    /**
     * Returns a normalized equivalent of the span in which
     * <tt>start</tt> is guaranteed to be less than end.
     */
    public Span normalize() {
        return new Span(Math.min(start, end), Math.max(start, end));
    }

    @Override
    public boolean equals(Object o) {
        boolean equal = false;

        if (o instanceof Span) {
            Span span = (Span)o;
            equal = (start == span.start
                && end == span.end);
        }

        return equal;
    }

    @Override
    public int hashCode() {
        return 31 * start + end;
    }

    @Override
    public String toString() {
        return ("{start: " + start + ", end: " + end + "}");
    }

    public static Span decode(String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        Span span;
        if (value.startsWith("{")) {
            try {
                span = new Span(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            span = new Span(Integer.parseInt(value));
        }

        return span;
    }
}
