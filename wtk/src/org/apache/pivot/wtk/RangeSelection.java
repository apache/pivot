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
import org.apache.pivot.collections.immutable.ImmutableList;

/**
 * Class for managing a set of indexed range selections.
 */
public class RangeSelection {
    // The coalesced selected ranges
    private ArrayList<Span> selectedRanges = new ArrayList<Span>();

    // Comparator that determines the index of the first intersecting range.
    private static final Comparator<Span> START_COMPARATOR = new Comparator<Span>() {
        @Override
        public int compare(Span range1, Span range2) {
            return (range1.end - range2.start);
        }
    };

    // Comparator that determines the index of the last intersecting range.
    private static final Comparator<Span> END_COMPARATOR = new Comparator<Span>() {
        @Override
        public int compare(Span range1, Span range2) {
            return (range1.start - range2.end);
        }
    };

    // Comparator that determines if two ranges intersect.
    private static final Comparator<Span> INTERSECTION_COMPARATOR = new Comparator<Span>() {
        @Override
        public int compare(Span range1, Span range2) {
            return (range1.start > range2.end) ? 1 : (range2.start > range1.end) ? -1 : 0;
        }
    };

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
        ArrayList<Span> addedRanges = new ArrayList<Span>();

        Span range = normalize(start, end);
        assert(range.start >= 0);

        int n = selectedRanges.getLength();

        if (n == 0) {
            // The selection is currently empty; append the new range
            // and add it to the added range list
            selectedRanges.add(range);
            addedRanges.add(range);
        } else {
            // Locate the lower bound of the intersection
            int i = ArrayList.binarySearch(selectedRanges, range, START_COMPARATOR);
            if (i < 0) {
                i = -(i + 1);
            }

            // Merge the selection with the previous range, if necessary
            if (i > 0) {
                Span previousRange = selectedRanges.get(i - 1);
                if (range.start == previousRange.end + 1) {
                    i--;
                }
            }

            if (i == n) {
                // The new range starts after the last existing selection
                // ends; append it and add it to the added range list
                selectedRanges.add(range);
                addedRanges.add(range);
            } else {
                // Locate the upper bound of the intersection
                int j = ArrayList.binarySearch(selectedRanges, range, END_COMPARATOR);
                if (j < 0) {
                    j = -(j + 1);
                } else {
                    j++;
                }

                // Merge the selection with the next range, if necessary
                if (j < n) {
                    Span nextRange = selectedRanges.get(j);
                    if (range.end == nextRange.start - 1) {
                        j++;
                    }
                }

                if (i == j) {
                    selectedRanges.insert(range, i);
                    addedRanges.add(range);
                } else {
                    // Create a new range representing the union of the intersecting ranges
                    Span lowerRange = selectedRanges.get(i);
                    Span upperRange = selectedRanges.get(j - 1);

                    range = new Span(Math.min(range.start, lowerRange.start),
                        Math.max(range.end, upperRange.end));

                    // Add the gaps to the added list
                    if (range.start < lowerRange.start) {
                        addedRanges.add(new Span(range.start, lowerRange.start - 1));
                    }

                    for (int k = i; k < j - 1; k++) {
                        Span selectedRange = selectedRanges.get(k);
                        Span nextSelectedRange = selectedRanges.get(k + 1);
                        addedRanges.add(new Span(selectedRange.end + 1, nextSelectedRange.start - 1));
                    }

                    if (range.end > upperRange.end) {
                        addedRanges.add(new Span(upperRange.end + 1, range.end));
                    }

                    // Remove all redundant ranges
                    selectedRanges.update(i, range);

                    if (i < j) {
                        selectedRanges.remove(i + 1, j - i - 1);
                    }
                }
            }
        }

        return addedRanges;
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
        ArrayList<Span> removedRanges = new ArrayList<Span>();

        Span range = normalize(start, end);
        assert(range.start >= 0);

        int n = selectedRanges.getLength();

        if (n > 0) {
            // Locate the lower bound of the intersection
            int i = ArrayList.binarySearch(selectedRanges, range, START_COMPARATOR);
            if (i < 0) {
                i = -(i + 1);
            }

            if (i < n) {
                Span lowerRange = selectedRanges.get(i);

                if (lowerRange.start < range.start
                    && lowerRange.end > range.end) {
                    // Removing the range will split the intersecting selection
                    // into two ranges
                    selectedRanges.update(i, new Span(lowerRange.start, range.start - 1));
                    selectedRanges.insert(new Span(range.end + 1, lowerRange.end), i + 1);
                    removedRanges.add(range);
                } else {
                    Span leadingRemovedRange = null;
                    if (range.start > lowerRange.start) {
                        // Remove the tail of this range
                        selectedRanges.update(i, new Span(lowerRange.start, range.start - 1));
                        leadingRemovedRange = new Span(range.start, lowerRange.end);
                        i++;
                    }

                    // Locate the upper bound of the intersection
                    int j = ArrayList.binarySearch(selectedRanges, range, END_COMPARATOR);
                    if (j < 0) {
                        j = -(j + 1);
                    } else {
                        j++;
                    }

                    if (j > 0) {
                        Span upperRange = selectedRanges.get(j - 1);

                        Span trailingRemovedRange = null;
                        if (range.end < upperRange.end) {
                            // Remove the head of this range
                            selectedRanges.update(j - 1, new Span(range.end + 1, upperRange.end));
                            trailingRemovedRange = new Span(upperRange.start, range.end);
                            j--;
                        }

                        // Remove all cleared ranges
                        Sequence<Span> clearedRanges = selectedRanges.remove(i, j - i);

                        // Construct the removed range list
                        if (leadingRemovedRange != null) {
                            removedRanges.add(leadingRemovedRange);
                        }

                        for (int k = 0, c = clearedRanges.getLength(); k < c; k++) {
                            removedRanges.add(clearedRanges.get(k));
                        }

                        if (trailingRemovedRange != null) {
                            removedRanges.add(trailingRemovedRange);
                        }
                    }
                }
            }
        }

        return removedRanges;
    }

    /**
     * Clears the selection.
     */
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
     * Returns an immutable wrapper around the selected ranges.
     */
    public ImmutableList<Span> getSelectedRanges() {
        return new ImmutableList<Span>(selectedRanges);
    }

    /**
     * Determines the index of a range in the selection.
     *
     * @param range
     *
     * @return
     * The index of the range, if it exists in the selection; <tt>-1</tt>,
     * otherwise.
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
        Span range = new Span(index);
        int i = ArrayList.binarySearch(selectedRanges, range, INTERSECTION_COMPARATOR);

        return (i >= 0);
    }

    /**
     * Inserts an index into the span sequence (e.g. when items are inserted
     * into the model data).
     *
     * @param index
     *
     * @return
     * The number of ranges that were updated.
     */
    public int insertIndex(int index) {
        int updated = 0;

        // Get the insertion point for the range corresponding to the given index
        Span range = new Span(index);
        int i = ArrayList.binarySearch(selectedRanges, range, INTERSECTION_COMPARATOR);

        if (i < 0) {
            // The inserted index does not intersect with a selected range
            i = -(i + 1);
        } else {
            // The inserted index intersects with a currently selected range
            Span selectedRange = selectedRanges.get(i);

            // If the inserted index falls within the current range, increment
            // the endpoint only
            if (selectedRange.start < index) {
                selectedRanges.update(i, new Span(selectedRange.start, selectedRange.end + 1));

                // Start incrementing range bounds beginning at the next range
                i++;
            }
        }

        // Increment any subsequent selection indexes
        int n = selectedRanges.getLength();
        while (i < n) {
            Span selectedRange = selectedRanges.get(i);
            selectedRanges.update(i, new Span(selectedRange.start + 1, selectedRange.end + 1));
            updated++;
            i++;
        }

        return updated;
    }

    /**
     * Removes a range of indexes from the span sequence (e.g. when items
     * are removed from the model data).
     *
     * @param index
     * @param count
     *
     * @return
     * The number of ranges that were updated.
     */
    public int removeIndexes(int index, int count) {
        // Clear any selections in the given range
        Sequence<Span> removed = removeRange(index, (index + count) - 1);
        int updated = removed.getLength();

        // Decrement any subsequent selection indexes
        Span range = new Span(index);
        int i = ArrayList.binarySearch(selectedRanges, range, INTERSECTION_COMPARATOR);
        assert (i < 0) : "i should be negative, since index should no longer be selected";

        i = -(i + 1);

        // Determine the number of ranges to modify
        int n = selectedRanges.getLength();
        while (i < n) {
            Span selectedRange = selectedRanges.get(i);
            selectedRanges.update(i, new Span(selectedRange.start - count, selectedRange.end - count));
            updated++;
            i++;
        }

        return updated;
    }

    /**
     * Ensures that the start value is less than or equal to the end value.
     *
     * @param start
     * @param end
     *
     * @return
     * A span containing the normalized range.
     */
    public static Span normalize(int start, int end) {
        return new Span(Math.min(start, end), Math.max(start, end));
    }
}
