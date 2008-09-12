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

import java.util.Comparator;

import pivot.collections.ArrayList;
import pivot.collections.Sequence;

/**
 * <p>Class representing a sequence of sorted, consolidated spans.</p>
 *
 * @author gbrown
 */
public class SpanSequence implements Sequence<Span> {
    /**
     * <p>Determines the relative order of two spans.</p>
     *
     * @author gbrown
     */
    public static class SpanComparator implements Comparator<Span> {
        /**
         * Compares two span values. A span is considered less than or greater
         * than another span if and only if it is absolutely less than or
         * greater than the other span; if the spans intersect in any way, they
         * are considered equal.
         *
         * @return A positive value if <tt>span1</tt> is greater than
         * <tt>span2</tt>; a negative value if <tt>span1</tt> is less than
         * <tt>span2</tt>; a value of zero if <tt>span1</tt> intersects
         * with <tt>span2</tt>.
         */
        public int compare(Span span1, Span span2) {
            int result = (span1.getStart() > span2.getEnd()) ? 1
                : (span2.getStart() > span1.getEnd()) ? -1 : 0;

            return result;
        }
    }

    /**
     * The list of sorted, consolidated spans.
     */
    private ArrayList<Span> spans = new ArrayList<Span>();

    /**
     * Comparator used to locate and sort spans.
     */
    private static SpanComparator spanComparator = new SpanComparator();

    /**
     * Adds a span to the sequence, merging and removing intersecting spans
     * as needed.
     *
     * @param span
     * The span to add to the sequence.
     *
     * @return
     * The index at which the span was added.
     */
    public int add(Span span) {
        if (span == null) {
            throw new IllegalArgumentException("span is null.");
        }

        // Get the insertion point
        int i = indexOf(span);

        if (i < 0) {
            // The span did not intersect or connect with any currently
            // selected span; insert it into the sequence
            if (span.getLength() > 0) {
                i = -(i + 1);
                spans.insert(new Span(span), i);
            }
        } else {
            // The new span intersects or connects with one or more currently
            // selected spans; walk the selection list backward and forward from
            // the insertion point to determine the consolidation range
            int j = i;
            int k = i;

            int n = spans.getLength();

            // Walk the list backward from the insertion point
            while (j >= 0) {
                Span existingSpan = spans.get(j);

                if (existingSpan.getEnd() < span.getStart() - 1) {
                    // The selected span falls outside the intersection
                    // and connection range; exit the loop
                    break;
                }

                // Get the previous selected span
                existingSpan = (--j >= 0) ? spans.get(j) : null;
            }

            // Increment the lower bound index, since it points to one less than
            // the index of the first intersecting span
            j++;

            // Walk the list forward from the insertion point
            while (k < n) {
                Span existingSpan = spans.get(k);

                if (existingSpan.getStart() > span.getEnd() + 1) {
                    // The selected span falls outside the intersection
                    // and connection range; exit the loop
                    break;
                }

                // Get the next selected span
                existingSpan = (++k < n) ? spans.get(k) : null;
            }

            // Update the upper bound index, since it points to one more than
            // the index of the last intersecting span
            k--;

            // Consolidate the selection and remove any redundant spans
            Span lowerSpan = spans.get(j);
            Span upperSpan = spans.get(k);

            int start = Math.min(span.getStart(), lowerSpan.getStart());
            int end = Math.max(span.getEnd(), upperSpan.getEnd());

            lowerSpan.setRange(start, end);
            spans.remove(j + 1, k - j);
        }

        return i;
    }

    /**
     * Not supported.
     */
    public final void insert(Span span, int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported.
     */
    public final Span update(int index, Span span) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes a span from the sequence, truncating and removing intersecting
     * spans as needed.
     *
     * @param span
     * The span to remove from the sequence.
     *
     * @return
     * The index from which the span was removed.
     */
    public int remove(Span span) {
        if (span == null) {
            throw new IllegalArgumentException("span is null.");
        }

        // Get the intersection index
        int i = indexOf(span);

        if (i >= 0) {
            Span existingSpan = spans.get(i);

            if (existingSpan.getStart() < span.getStart()
                && existingSpan.getEnd() > span.getEnd()) {
                // Removing the span will split the intersecting selection
                // into two spans
                spans.insert(new Span(span.getEnd() + 1, existingSpan.getEnd()), i + 1);
                existingSpan.setEnd(span.getStart() - 1);
            } else {
                // Determine the indexes of the upper and lower bounds of the
                // intersection
                int n = spans.getLength();

                int j = i;
                while (j >= 0
                    && spans.get(j).getEnd() >= span.getStart()) {
                    j--;
                }
                j++;

                int k = i;
                while (k < n
                    && spans.get(k).getStart() <= span.getEnd()) {
                    k++;
                }
                k--;

                Span lowerSpan = spans.get(j);
                Span upperSpan = spans.get(k);

                if (lowerSpan.getStart() < span.getStart()) {
                    // The lower bounding span will be partially cleared
                    lowerSpan.setEnd(span.getStart() - 1);

                    // Increment the lower bound index so this span isn't
                    // removed from the selection
                    j++;
                }

                if (upperSpan.getEnd() > span.getEnd()) {
                    // The upper bounding span will be partially cleared
                    upperSpan.setStart(span.getEnd() + 1);

                    // Decrement the upper bound index so this span isn't
                    // removed from the selection
                    k--;
                }

                // Remove all completely cleared spans from the selection
                if (k >= j) {
                    spans.remove(j, (k - j) + 1);
                }
            }
        }

        return i;
    }

    /**
     * Removes one or more spans from the sequence.
     *
     * @param index
     * The starting index to remove.
     *
     * @param count
     * The number of items to remove, beginning with <tt>index</tt>.
     *
     * @return
     * A sequence containing the spans that were removed.
     */
    public Sequence<Span> remove(int index, int count) {
        return spans.remove(index, count);
    }

    /**
     * Removes all spans from the sequence.
     */
    public void clear() {
        spans.clear();
    }

    /**
     * Retrieves the span at the given index.
     *
     * @param index
     * The index of the span to retrieve.
     */
    public Span get(int index) {
        return spans.get(index);
    }

    /**
     * Returns the index of the first identified span that intersects with
     * the given span, or a negative value representing the insertion point
     * of the span as defined by the binary search algorithm.
     */
    public int indexOf(Span span) {
        return Search.binarySearch(spans, span, spanComparator);
    }

    /**
     * Returns the length of the sequence.
     *
     * @return
     * The number of spans in the sequence.
     */
    public int getLength() {
        return spans.getLength();
    }

    /**
     * Inserts an index into the span sequence.
     *
     * @param index
     * The index to insert.
     *
     * @return
     * The number of spans that were modified as a result of the insertion.
     */
    public int insertIndex(int index) {
        // Keep track of the number of modified spans
        int m = 0;

        // Get the insertion point for the span corresponding to the given index
        Span indexSpan = new Span(index, index);
        int i = indexOf(indexSpan);

        if (i < 0) {
            // The inserted item does not intersect with a selected span
            i = -(i + 1);
        } else {
            // The inserted item intersects with a currently selected span
            Span span = get(i);

            // If the spans' start values are equal, shift the selection;
            // otherwise, insert the index into the selection
            if (span.getStart() > indexSpan.getStart()) {
                span.setEnd(span.getEnd() + 1);
                m++;

                // Start incrementing span bounds beginning at the next span index
                i++;
            }
        }

        // Increment any subsequent selection indexes
        int n = getLength();
        m += (n - i);

        while (i < n) {
            Span span = get(i);
            span.setRange(span.getStart() + 1, span.getEnd() + 1);
            i++;
        }

        return m;
    }

    /**
     * Removes a range of indexes from the span sequence.
     *
     * @param index
     * The first index to remove.
     *
     * @param count
     * The number of indexes to remove.
     *
     * @return
     * The number of spans that were modified as a result of the removal.
     */
    public int removeIndexes(int index, int count) {
        // Clear any selections in the given range
        Span rangeSpan = new Span(index, (index + count) - 1);
        remove(rangeSpan);

        // Decrement any subsequent selection indexes
        Span indexSpan = new Span(index, index);
        int i = indexOf(indexSpan);
        assert (i < 0) : "i should be negative, since index should no longer be selected";

        i = -(i + 1);

        // Determine the number of spans to modify
        int n = getLength();
        int m = (n - i);

        while (i < n) {
            Span span = get(i);
            span.setRange(span.getStart() - count, span.getEnd() - count);
            i++;
        }

        return m;
    }
}
