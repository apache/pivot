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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;

/**
 * Class for managing a set of indexed range selections.
 *
 * @author gbrown
 */
class ListSelection {
    /**
     * Determines the relative order of two ranges.
     *
     * @author gbrown
     */
    public static class IntersectionComparator implements Comparator<Span> {
        /**
         * Compares two range values. A range is considered less than or greater
         * than another range if and only if it is absolutely less than or
         * greater than the other range; if the ranges overlap in any way,
         * they are considered equal.
         *
         * @return
         * A positive value if <tt>range1</tt> is greater than <tt>range2</tt>;
         * a negative value if <tt>range1</tt> is less than <tt>range2</tt>; a
         * value of zero if <tt>range1</tt> intersects with <tt>range2</tt>.
         */
        public int compare(Span range1, Span range2) {
            return (range1.start > range2.end) ? 1 : (range2.start > range1.end) ? -1 : 0;
        }
    }

    private ArrayList<Span> selectedRanges = new ArrayList<Span>();

    public static final IntersectionComparator INTERSECTION_COMPARATOR = new IntersectionComparator();

    /**
     * Adds a range to the selection, merging and removing intersecting ranges
     * as needed.
     *
     * @param start
     * @param end
     *
     * @return
     * A sequence containing the ranges that were added.
     */
    public Sequence<Span> addRange(int start, int end) {
        ArrayList<Span> added = new ArrayList<Span>();

        Span range = normalize(start, end);

        if (range.getLength() > 0) {
            int n = selectedRanges.getLength();

            if (n == 0) {
                // Nothing is currently selected
                selectedRanges.add(range);
                added.add(range);
            } else {
                // TODO
            }
        }

        return added;
    }

    /**
     * Removes a range from the selection, truncating and removing intersecting
     * ranges as needed.
     *
     * @param start
     * @param end
     *
     * @return
     * A sequence containing the ranges that were removed.
     */
    public Sequence<Span> removeRange(int start, int end) {
        ArrayList<Span> removed = new ArrayList<Span>();

        Span range = normalize(start, end);

        if (range.getLength() > 0
            && selectedRanges.getLength() > 0) {
            // TODO
        }

        return removed;
    }

    public void clear() {
        selectedRanges.clear();
    }

    /**
     * Returns the range at a given index.
     *
     * @param index
     */
    public Span get(int index) {
        return selectedRanges.get(index);
    }

    /**
     * Returns the number of ranges in the selection.
     */
    public int getLength() {
        return selectedRanges.getLength();
    }

    /**
     * Determines the index of a range in the selection.
     *
     * @param range
     *
     * @return
     * The index of the range, if it exists in the selection in its entirety;
     * <tt>-1</tt>, otherwise.
     */
    public int indexOf(Span range) {
        assert (range != null);

        int index = -1;
        int i = ArrayList.binarySearch(selectedRanges, range, INTERSECTION_COMPARATOR);

        if (i >= 0) {
            index = (range.equals(selectedRanges.get(i))) ? i : -1;
        }

        return index;
    }

    /**
     * Tests for the presence of an index in the selection.
     *
     * @param index
     *
     * @return
     * <tt>true</tt> if the index is selected; <tt>false</tt>, otherwise.
     */
    public boolean containsIndex(int index) {
        return (indexOf(new Span(index, index)) != -1);
    }

    /**
     * Inserts an index into the span sequence (e.g. when items are inserted
     * into the model data).
     *
     * @param index
     */
    public void insertIndex(int index) {
        // Get the insertion point for the range corresponding to the given index
        Span range = new Span(index, index);
        int i = indexOf(range);

        if (i < 0) {
            // The inserted item does not intersect with a selected range
            i = -(i + 1);
        } else {
            // The inserted item intersects with a currently selected range
            Span selectedRange = selectedRanges.get(i);

            // If the ranges' start values are equal, shift the selection;
            // otherwise, insert the index into the selection
            if (selectedRange.start > range.start) {
                selectedRanges.update(i, new Span(selectedRange.start, selectedRange.end + 1));

                // Start incrementing range bounds beginning at the next range index
                i++;
            }
        }

        // Increment any subsequent selection indexes
        int n = selectedRanges.getLength();
        while (i < n) {
            Span selectedRange = selectedRanges.get(i);
            selectedRanges.update(i, new Span(selectedRange.start + 1, selectedRange.end + 1));
            i++;
        }
    }

    /**
     * Removes a range of indexes from the span sequence (e.g. when items
     * are removed from the model data).
     *
     * @param index
     * @param count
     */
    public void removeIndexes(int index, int count) {
        // Clear any selections in the given range
        removeRange(index, (index + count) - 1);

        // Decrement any subsequent selection indexes
        Span range = new Span(index, index);
        int i = indexOf(range);
        assert (i < 0) : "i should be negative, since index should no longer be selected";

        i = -(i + 1);

        // Determine the number of ranges to modify
        int n = selectedRanges.getLength();
        while (i < n) {
            Span selectedRange = selectedRanges.get(i);
            selectedRanges.update(i, new Span(selectedRange.start - count, selectedRange.end - count));
            i++;
        }
    }

    public static Span normalize(int start, int end) {
        return new Span(Math.min(start, end), Math.max(start, end));
    }
}
