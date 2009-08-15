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
package org.apache.pivot.collections;

import java.util.Iterator;

import org.apache.pivot.util.ImmutableIterator;


/**
 * Interface representing an ordered sequence of items.
 *
 */
public interface Sequence<T> {
    /**
     * Collection of static utility methods providing path access to nested
     * sequence data.
     *
     */
    public static class Tree {
        /**
         * An object representing a path to a nested node in nested sequence
         * data.
         *
         */
        public static class Path implements Sequence<Integer>, Iterable<Integer> {
            private ArrayList<Integer> elements;

            public Path() {
                elements = new ArrayList<Integer>();
            }

            public Path(Integer... elements) {
                this.elements = new ArrayList<Integer>(elements);
            }

            public Path(Path path) {
                this(path, path.getLength());
            }

            public Path(Path path, int depth) {
                elements = new ArrayList<Integer>(depth);

                for (int i = 0; i < depth; i++) {
                    elements.add(path.get(i));
                }
            }

            private Path(ArrayList<Integer> elements) {
                this.elements = elements;
            }

            public int add(Integer item) {
                return elements.add(item);
            }

            public void insert(Integer item, int index) {
                elements.insert(item, index);
            }

            public Integer update(int index, Integer item) {
                return elements.update(index, item);
            }

            public int remove(Integer item) {
                throw new UnsupportedOperationException();
            }

            public Sequence<Integer> remove(int index, int count) {
                return elements.remove(index, count);
            }

            public Integer get(int index) {
                return elements.get(index);
            }

            public int indexOf(Integer item) {
                return elements.indexOf(item);
            }

            public int getLength() {
                return elements.getLength();
            }

            public Iterator<Integer> iterator() {
                return new ImmutableIterator<Integer>(elements.iterator());
            }

            public Integer[] toArray() {
                return elements.toArray(Integer[].class);
            }

            public static Path forDepth(int depth) {
                return new Path(new ArrayList<Integer>(depth));
            }
        }

        /**
         * Class representing an immutable path.
         *
         */
        public static class ImmutablePath extends Path {
            public ImmutablePath(Integer... elements) {
                super(elements);
            }

            public ImmutablePath(Path path) {
                super(path);
            }

            @Override
            public int add(Integer item) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void insert(Integer item, int index) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Integer update(int index, Integer item) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Sequence<Integer> remove(int index, int count) {
                throw new UnsupportedOperationException();
            }
        }

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
        public static <T> int add(Sequence<T> sequence, T item, Path path) {
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
        public static <T> void insert(Sequence<T> sequence, T item, Path path,
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
        public static <T> T update(Sequence<T> sequence, Path path, T item) {
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
        public static <T> Path remove(Sequence<T> sequence, T item) {
            Path path = pathOf(sequence, item);
            if (path == null) {
                throw new IllegalArgumentException("item is not a descendant of sequence.");
            }

            remove(sequence, path, 1);

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
        public static <T> Sequence<T> remove(Sequence<T> sequence, Path path, int count) {
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

            return sequence.remove(path.get(i), count);
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
        public static <T> T get(Sequence<T> sequence, Path path) {
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

            return sequence.get(path.get(i));
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
        @SuppressWarnings({"unchecked"})
        public static <T> Path pathOf(Sequence<T> sequence, T item) {
            if (sequence == null) {
                throw new IllegalArgumentException("sequence is null.");
            }

            if (item == null) {
                throw new IllegalArgumentException("item is null.");
            }

            Path path = null;

            for (int i = 0, n = sequence.getLength(); i < n && path == null; i++) {
                T t = sequence.get(i);

                if (t.equals(item)) {
                    path = new Path();
                    path.add(i);
                } else {
                    if (t instanceof Sequence<?>) {
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
         * Determines whether the path represented by the second argument is
         * a descendant of the path represented by the first argument.
         *
         * @param ancestorPath
         * The ancestor path to test.
         *
         * @param descendantPath
         * The descendant path to test.
         */
        public static boolean isDescendant(Path ancestorPath, Path descendantPath) {
            int ancestorLength = ancestorPath.getLength();
            int descendantLength = descendantPath.getLength();

            boolean result = (ancestorLength <= descendantLength);

            if (result) {
                for (int i = 0, n = ancestorLength; i < n; i++) {
                    int index1 = ancestorPath.get(i);
                    int index2 = descendantPath.get(i);

                    if (index1 != index2) {
                        result = false;
                        break;
                    }
                }
            }

            return result;
        }
    }

    /**
     * Adds an item to the sequence.
     *
     * @param item
     * The item to be added to the sequence.
     *
     * @return
     * The index at which the item was added, or <tt>-1</tt> if the item
     * was not added to the sequence.
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
