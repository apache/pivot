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

/**
 * Class representing a range of integer values. The range includes all
 * values in the interval <i>[start, end]</i>. Values may be negative, but the
 * value of <tt>start</tt> must be less than or equal to the value of
 * <tt>end</tt>.
 *
 * @author gbrown
 */
public class Span {
    private int start = 0;
    private int end = 0;

    public static final String START_KEY = "start";
    public static final String END_KEY = "end";

    public Span() {
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

        setRange((Integer)span.get(START_KEY), (Integer)span.get(END_KEY));
    }

    public Span(Span span) {
        if (span == null) {
            throw new IllegalArgumentException("span is null.");
        }

        setRange(span.start, span.end);
    }

    public Span(int start, int end) {
        setRange(start, end);
    }

    /**
     * Returns the first value in the span.
     */
    public int getStart() {
        return start;
    }

    /**
     * Sets the first value in the span.
     *
     * @param start
     * The first value in the span. Must be less than or equal to
     * the end value.
     */
    public void setStart(int start) {
        setRange(start, end);
    }

    /**
     * Returns the last value in the span.
     */
    public int getEnd() {
        return end;
    }

    /**
     * Sets the last value in the span.
     *
     * @param end
     * The last value in the span. Must be greater than or equal to
     * the start value.
     */
    public void setEnd(int end) {
        setRange(start, end);
    }

    /**
     * Returns the length of the span.
     *
     * @return
     * The length of the span (<tt>end</tt> minus <tt>start</tt> + 1).
     */
    public long getLength() {
        return (end - start) + 1;
    }

    /**
     * Sets the range of the span.
     *
     * @param start
     * The first value in the span. Must be less than or equal to
     * <tt>end</tt>.
     *
     * @param end
     * The last value in the span. Must be greater than or equal to
     * <tt>start</tt>.
     */
    public void setRange(int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException("start is greater than end.");
        }

        this.start = start;
        this.end = end;
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

        return (start <= span.start
            && end >= span.end);
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

        return (start >= span.end
            || end <= span.start);
    }

    /**
     * Determines the intersection of this span and another span.
     *
     * @param span
     * The span to intersect with this span.
     *
     * @return
     * A new Span instance representing the intersection of this span and
     * <tt>span</tt>, or null if the spans do not intersect.
     */
    public Span createIntersection(Span span) {
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
     * Determines the union of this span and another span.
     *
     * @param span
     * The span to union with this span.
     *
     * @return
     * A new Span instance representing the union of this span and
     * <tt>span</tt>.
     */
    public Span createUnion(Span span) {
        if (span == null) {
            throw new IllegalArgumentException("span is null.");
        }

        return new Span(Math.min(start, span.start),
            Math.max(end, span.end));
    }

    public boolean equals(Object o) {
        boolean equal = false;

        if (o instanceof Span) {
            Span span = (Span)o;
            equal = (start == span.start
                && end == span.end);
        }

        return equal;
    }

    public String toString() {
        return ("{start: " + start + ", end: " + end + "}");
    }
}
