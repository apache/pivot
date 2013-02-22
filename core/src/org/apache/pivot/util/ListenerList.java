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
package org.apache.pivot.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Abstract base class for listener lists.
 * <p>
 * NOTE This class is not inherently thread safe. Subclasses that require
 * thread-safe access should synchronize method access appropriately. Callers
 * must manually synchronize on the listener list instance to ensure thread
 * safety during iteration.
 */
public abstract class ListenerList<T> implements Iterable<T> {

    // Iterator through the current array of elements
    private class NodeIterator implements Iterator<T> {
        private int index;

        public NodeIterator() {
            this.index = 0;
        }

        @Override
        public boolean hasNext() {
            return (index < last);
        }

        @Override
        public T next() {
            if (index >= last) {
                throw new NoSuchElementException();
            }

            return list[index++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static final int DEFAULT_SIZE = 5;

    // The current array of items (some of which are null)
    // All non-null objects are at the beginning of the array
    // and the array is reorganized on "remove"
    @SuppressWarnings({"unchecked"})
    private T[] list = (T[])new Object[DEFAULT_SIZE];
    // The current length of the active list
    private int last = 0;

    /**
     * Adds a listener to the list, if it has not previously been added.
     *
     * @param listener
     */
    public void add(T listener) {
        if (indexOf(listener) >= 0) {
            System.err.println("Duplicate listener " + listener + " added to " + this);
            return;
        }

        // If no slot is available, increase the size of the array
        if (last >= list.length) {
            list = Arrays.copyOf(list, list.length + DEFAULT_SIZE);
        }

        list[last++] = listener;
    }

    /**
     * Removes a listener from the list, if it has previously been added.
     *
     * @param listener
     */
    public void remove(T listener) {
        int index = indexOf(listener);

        if (index < 0) {
            System.err.println("Nonexistent listener " + listener + " removed from " + this);
            return;
        }

        // Once we find the entry in the list, copy the rest of the
        // existing entries down by one position
        if (index < last - 1) {
            System.arraycopy(list, index + 1, list, index, last - 1 - index);
        }

        list[--last] = null;
    }

    private int indexOf(T listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null.");
        }
        for (int i = 0; i < last; i++) {
            if (list[i] == listener) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Tests the existence of a listener in the list.
     *
     * @param listener
     *
     * @return
     * <tt>true</tt> if the listener exists in the list; <tt>false</tt>,
     * otherwise.
     */
    public boolean contains(T listener) {
        return indexOf(listener) >= 0;
    }

    /**
     * Tests the emptiness of the list.
     *
     * @return
     * <tt>true</tt> if the list contains no listeners; <tt>false</tt>,
     * otherwise.
     */
    public boolean isEmpty() {
        return last == 0;
    }

    /**
     * Get the number of elements in the list.
     *
     * @return
     * the number of elements.
     */
    public int getLength() {
        return last;
    }

    @Override
    public Iterator<T> iterator() {
        return new NodeIterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getClass().getName());
        sb.append(" [");

        int i = 0;
        for (T item : this) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(item);
            i++;
        }

        sb.append("]");

        return sb.toString();
    }

}
