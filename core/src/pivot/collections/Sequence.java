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
package pivot.collections;

import java.util.Comparator;

/**
 * Interface representing an ordered sequence of items.
 *
 * @author gbrown
 */
public interface Sequence<T> {
    /**
     * Collection of static utility methods providing path access to nested
     * sequence data.
     *
     * @author gbrown
     */
    public static class Tree {
        /**
         * Adds an item to a nested sequence.
         *
         * @param sequence
         * The root sequence.
         *
         * @param item
         * The item to be added to the sequence.
         *
         * @param path
         * The path of the sequence to which the item should be added.
         *
         * @return
         * The index at which the item was inserted, relative to the parent
         * sequence.
         */
        @SuppressWarnings("unchecked")
        public static <T> int add(Sequence<T> sequence, T item, Sequence<Integer> path) {
            return ((Sequence<T>)get(sequence, path)).add(item);
        }

        /**
         * Inserts an item into a nested sequence.
         *
         * @param sequence
         * The root sequence.
         *
         * @param item
         * The item to be inserted into the sequence.
         *
         * @param path
         * The path of the sequence into which the item should be inserted.
         *
         * @param index
         * The index at which the item should be inserted within the parent
         * sequence.
         */
        @SuppressWarnings("unchecked")
        public static <T> void insert(Sequence<T> sequence, T item, Sequence<Integer> path,
            int index) {
            ((Sequence<T>)get(sequence, path)).insert(item, index);
        }

        /**
         * Updates an item in a nested sequence.
         *
         * @param sequence
         * The root sequence.
         *
         * @param path
         * The path of the item to update.
         *
         * @param item
         * The item that will replace any existing value at the given path.
         *
         * @return
         * The item that was previously stored at the given path.
         */
        @SuppressWarnings("unchecked")
        public static <T> T update(Sequence<T> sequence, Sequence<Integer> path, T item) {
            if (sequence == null) {
                throw new IllegalArgumentException("sequence is null.");
            }

            if (path == null) {
                throw new IllegalArgumentException("path is null.");
            }

            int i = 0, n = path.getLength() - 1;
            while (i < n) {
                sequence = (Sequence<T>)sequence.get(path.get(i++));
            }

            return sequence.update(path.get(i), item);
        }

        /**
         * Removes the first occurrence of an item from a nested sequence.
         *
         * @param sequence
         * The root sequence.
         *
         * @param item
         * The item to remove.
         *
         * @return
         * The path of the item that was removed.
         */
        public static <T> Sequence<Integer> remove(Sequence<T> sequence, T item) {
            Sequence<Integer> path = pathOf(sequence, item);
            if (path == null) {
                throw new IllegalArgumentException("item is not a descendant of sequence.");
            }

            remove(sequence, path);

            return path;
        }

        /**
         * Removes an item from a nested sequence.
         *
         * @param sequence
         * The root sequence.
         *
         * @param path
         * The path of the item to remove.
         */
        @SuppressWarnings("unchecked")
        public static <T> void remove(Sequence<T> sequence, Sequence<Integer> path) {
            if (sequence == null) {
                throw new IllegalArgumentException("sequence is null.");
            }

            if (path == null) {
                throw new IllegalArgumentException("path is null.");
            }

            int i = 0, n = path.getLength() - 1;
            while (i < n) {
                sequence = (Sequence<T>)sequence.get(path.get(i++));
            }

            sequence.remove(path.get(i), 1);
        }

        /**
         * Retrieves an item from a nested sequence.
         *
         * @param sequence
         * The root sequence.
         *
         * @param path
         * The path of the item to retrieve.
         *
         * @return
         * The item at the given path, or <tt>null</tt> if the path is empty.
         */
        @SuppressWarnings("unchecked")
        public static <T> T get(Sequence<T> sequence, Sequence<Integer> path) {
            if (sequence == null) {
                throw new IllegalArgumentException("sequence is null.");
            }

            if (path == null) {
                throw new IllegalArgumentException("path is null.");
            }

            int i = 0, n = path.getLength() - 1;
            while (i < n) {
                sequence = (Sequence<T>)sequence.get(path.get(i++));
            }

            return sequence.get(i);
        }

        /**
         * Returns the path to an item in a nested sequence.
         *
         * @param sequence
         * The root sequence.
         *
         * @param item
         * The item to locate.
         *
         * @return
         * The path of first occurrence of the item if it exists in the
         * sequence; <tt>null</tt>, otherwise.
         */
        @SuppressWarnings("unchecked")
        public static <T> Sequence<Integer> pathOf(Sequence<T> sequence, T item) {
            if (sequence == null) {
                throw new IllegalArgumentException("sequence is null.");
            }

            if (item == null) {
                throw new IllegalArgumentException("item is null.");
            }

            Sequence<Integer> path = null;

            for (int i = 0, n = sequence.getLength(); i < n && path == null; i++) {
                T t = sequence.get(i);

                if (t.equals(item)) {
                    path = new ArrayList<Integer>();
                    path.add(i);
                } else {
                    if (t instanceof Sequence) {
                        path = pathOf((Sequence<T>)t, item);

                        if (path != null) {
                            path.insert(0, i);
                        }
                    }
                }
            }

            return path;
        }

        /**
         * Returns the rank of an item in a nested sequence. An item's rank
         * is defined as its position in a depth-first traversal of the tree.
         *
         * @param sequence
         * The root sequence.
         *
         * @param item
         * The item to locate.
         *
         * @return
         * The rank of the first occurrence of the item if it exists in the
         * sequence, or <tt>-(<i>descendant count</i> + 1)</tt> if the item is not
         * found.
         */
        @SuppressWarnings("unchecked")
        public static <T> int rankOf(Sequence<T> sequence, T item) {
            if (sequence == null) {
                throw new IllegalArgumentException("sequence is null.");
            }

            if (item == null) {
                throw new IllegalArgumentException("item is null.");
            }

            int rank = -1;

            for (int i = 0, n = sequence.getLength(); i < n && rank < 0; i++) {
                rank--;

                T t = sequence.get(i);

                if (t.equals(item)) {
                    // The item was found in this sequence
                    rank = -(rank + 1);
                } else {
                    if (t instanceof Sequence) {
                        int r = rankOf((Sequence<T>)t, item);

                        if (r < 0) {
                            // The item is not a descendant of this sequence
                            rank += (r + 1);
                        } else {
                            // The item was found in a nested sequence
                            rank = -(rank + 1) + r;
                        }
                    }
                }
            }

            return rank;
        }
    }

    /**
     * Contains utility methods for searching sequences.
     *
     * @author gbrown, tvolkert
     */
    public static class Search {
        /**
         * Performs a binary search of a sequence for the given comparable item.
         * See {@link #binarySearch(Sequence, Object, Comparator)}.
         */
        public static <T extends Comparable<? super T>> int binarySearch(Sequence<T> sequence, T item) {
            Comparator<T> comparator = new Comparator<T>() {
                public int compare(T t1, T t2) {
                    return t1.compareTo(t2);
                }
            };

            return binarySearch(sequence, item, comparator);
        }

        /**
         * Performs a binary search of a sequence for the given item.
         *
         * @param sequence
         * The sequence to search. If the sequence is not sorted, the behavior
         * is undefined.
         *
         * @param item
         * The item to search for.
         *
         * @param comparator
         * Comparator that determines element order.
         *
         * @return
         * The index of <tt>item</tt>, if it is contained in the sequence;
         * otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>. The insertion
         * point is defined as the point at which the item would be inserted
         * into the sequence: the index of the first element greater than
         * <tt>item</tt>, or <tt>sequence.getLength()</tt>, if all items in the
         * sequence are less than <tt>item</tt>. Note that this guarantees that
         * the return value will be greater than 0 if and only if the item is
         * found.
         * <p>
         * If the sequence contains multiple elements equal to the specified
         * item, there is no guarantee which one will be found.
         */
        public static <T> int binarySearch(Sequence<T> sequence, T item,
            Comparator<T> comparator) {
            int low = 0;
            int high = sequence.getLength() - 1;

            while (low <= high) {
                int mid = (low + high) >> 1;
                T midVal = sequence.get(mid);
                int cmp = comparator.compare(midVal, item);

                if (cmp < 0) {
                   low = mid + 1;
                }
                else if (cmp > 0) {
                   high = mid - 1;
                }
                else {
                   // Item found
                   return mid;
                }
            }

            // Item not found
            return -(low + 1);
        }
    }

    /**
     * Contains utility methods for sorting sequences.
     *
     * @author gbrown
     */
    public static class Sort {
        /**
         * Performs a quicksort on the sequence.
         *
         * @param sequence
         * The sequence to sort.
         *
         * @param comparator
         * Comparator that determines element order.
         */
        public static <T> void quickSort(Sequence<T> sequence, Comparator<T> comparator) {
            // TODO Implement internally rather than copying to java.util.ArrayList

            java.util.ArrayList<T> arrayList = new java.util.ArrayList<T>(sequence.getLength());

            for (int i = 0, n = sequence.getLength(); i < n; i++) {
                arrayList.add(sequence.get(i));
            }

            java.util.Collections.sort(arrayList, comparator);

            for (int i = 0, n = arrayList.size(); i < n; i++) {
                sequence.update(i, arrayList.get(i));
            }
        }
    }

    /**
     * Adds an item to the sequence.
     *
     * @param item
     * The item to be added to the sequence.
     *
     * @return
     * The index at which the item was added.
     */
    public int add(T item);

    /**
     * Inserts an item into the sequence at a specific index.
     *
     * @param item
     * The item to be added to the sequence.
     *
     * @param index
     * The index at which the item should be inserted. Must be a value between
     * <tt>0</tt> and <tt>getLength()</tt>.
     */
    public void insert(T item, int index);

    /**
     * Updates the item at the given index.
     *
     * @param index
     * The index of the item to update.
     *
     * @param item
     * The item that will replace any existing value at the given index.
     *
     * @return
     * The item that was previously stored at the given index.
     */
    public T update(int index, T item);

    /**
     * Removes the first occurrence of the given item from the sequence.
     *
     * @param item
     * The item to remove.
     *
     * @return
     * The index of the item that was removed, or <tt>-1</tt> if the item
     * could not be found.
     *
     * @see #remove(int, int)
     */
    public int remove(T item);

    /**
     * Removes one or more items from the sequence.
     *
     * @param index
     * The starting index to remove.
     *
     * @param count
     * The number of items to remove, beginning with <tt>index</tt>.
     *
     * @return
     * A sequence containing the items that were removed.
     */
    public Sequence<T> remove(int index, int count);

    /**
     * Retrieves the item at the given index.
     *
     * @param index
     * The index of the item to retrieve.
     */
    public T get(int index);

    /**
     * Returns the index of an item in the sequence.
     *
     * @param item
     * The item to locate.
     *
     * @return
     * The index of first occurrence of the item if it exists in the sequence;
     * <tt>-1</tt>, otherwise.
     */
    public int indexOf(T item);

    /**
     * Returns the length of the sequence.
     *
     * @return
     * The number of items in the sequence.
     */
    public int getLength();
}
