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

import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.util.ListenerList;

/**
 * Collection interface representing an ordered sequence of items.
 */
public interface List<T> extends Sequence<T>, Collection<T> {
    /**
     * Optional item iterator interface.
     */
    public interface ItemIterator<T> extends Iterator<T> {
        public boolean hasPrevious();
        public T previous();

        public void toStart();
        public void toEnd();

        public void insert(T item);
        public void update(T item);
    }

    /**
     * List listener list.
     */
    public static class ListListenerList<T>
        extends ListenerList<ListListener<T>> implements ListListener<T> {
        @Override
        public void itemInserted(List<T> list, int index) {
            for (ListListener<T> listener : this) {
                listener.itemInserted(list, index);
            }
        }

        @Override
        public void itemsRemoved(List<T> list, int index, Sequence<T> items) {
            for (ListListener<T> listener : this) {
                listener.itemsRemoved(list, index, items);
            }
        }

        @Override
        public void itemUpdated(List<T> list, int index, T previousItem) {
            for (ListListener<T> listener : this) {
                listener.itemUpdated(list, index, previousItem);
            }
        }

        @Override
        public void listCleared(List<T> list) {
            for (ListListener<T> listener : this) {
                listener.listCleared(list);
            }
        }

        @Override
        public void comparatorChanged(List<T> list, Comparator<T> previousComparator) {
            for (ListListener<T> listener : this) {
                listener.comparatorChanged(list, previousComparator);
            }
        }
    }

    /**
     * Adds an item to the list. If the list is unsorted, the item is appended
     * to the end of the list. Otherwise, it is inserted at the appropriate
     * index.
     *
     * @see org.apache.pivot.collections.ListListener#itemInserted(List, int)
     *
     * @return
     * The index at which the item was added.
     */
    @Override
    public int add(T item);

    /**
     * Inserts an item into the list.
     *
     * @param item
     * The item to be added to the list.
     *
     * @param index
     * The index at which the item should be inserted. Must be a value between
     * <tt>0</tt> and <tt>getLength()</tt>.
     *
     * @throws IllegalArgumentException
     * If the list is sorted and the insertion point of the item does not match
     * the given index.
     *
     * @see ListListener#itemInserted(List, int)
     */
    @Override
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
     * @throws IllegalArgumentException
     * If the list is sorted and the index of the updated item would be
     * different than its current index.
     *
     * @see ListListener#itemUpdated(List, int, Object)
     */
    @Override
    public T update(int index, T item);

    /**
     * @see ListListener#itemsRemoved(List, int, Sequence)
     */
    @Override
    public Sequence<T> remove(int index, int count);

    /**
     * @see ListListener#itemsRemoved(List, int, Sequence)
     */
    @Override
    public void clear();

    /**
     * Returns the length of the list.
     *
     * @return
     * The number of items in the list, or -1 if the list's length is
     * not known. In this case, the iterator must be used to retrieve the
     * contents of the list.
     */
    @Override
    public int getLength();

    /**
     * @see ListListener#comparatorChanged(List, Comparator)
     */
    @Override
    public void setComparator(Comparator<T> comparator);

    /**
     * Returns the list listener list.
     */
    public ListenerList<ListListener<T>> getListListeners();
}
