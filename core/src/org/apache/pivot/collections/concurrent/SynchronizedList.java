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
package org.apache.pivot.collections.concurrent;

import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;

/**
 * Synchronized implementation of the {@link List} interface.
 */
public class SynchronizedList<T> implements List<T> {
    private static class SynchronizedListListenerList<T>
        extends ListListenerList<T> {
        @Override
        public synchronized void add(ListListener<T> listener) {
            super.add(listener);
        }

        @Override
        public synchronized void remove(ListListener<T> listener) {
            super.remove(listener);
        }

        @Override
        public synchronized void itemInserted(List<T> list, int index) {
            super.itemInserted(list, index);
        }

        @Override
        public synchronized void itemsRemoved(List<T> list, int index, Sequence<T> items) {
            super.itemsRemoved(list, index, items);
        }

        @Override
        public synchronized void itemUpdated(List<T> list, int index, T previousItem) {
            super.itemUpdated(list, index, previousItem);
        }

        @Override
        public synchronized void listCleared(List<T> list) {
            super.listCleared(list);
        }

        @Override
        public synchronized void comparatorChanged(List<T> list, Comparator<T> previousComparator) {
            super.comparatorChanged(list, previousComparator);
        }
    }

    private List<T> list;
    private SynchronizedListListenerList<T> listListeners = new SynchronizedListListenerList<T>();

    public SynchronizedList(List<T> list) {
        if (list == null) {
            throw new IllegalArgumentException("list cannot be null.");
        }

        this.list = list;
    }

    @Override
    public synchronized int add(T item) {
        int index = list.add(item);
        listListeners.itemInserted(this, index);

        return index;
    }

    @Override
    public synchronized void insert(T item, int index) {
        list.insert(item, index);
        listListeners.itemInserted(this, index);
    }

    @Override
    public synchronized T update(int index, T item) {
        T previousItem = list.update(index, item);
        if (previousItem != item) {
            listListeners.itemUpdated(this, index, previousItem);
        }

        return previousItem;
    }

    @Override
    public synchronized int remove (T item) {
        int index = indexOf(item);
        if (index != -1) {
            remove(index, 1);
        }

        return index;
    }

    @Override
    public synchronized Sequence<T> remove(int index, int count) {
        Sequence<T> removed = list.remove(index, count);
        if (count > 0) {
            listListeners.itemsRemoved(this, index, removed);
        }

        return removed;
    }

    @Override
    public synchronized void clear() {
        if (list.getLength() > 0) {
            list.clear();
            listListeners.listCleared(this);
        }
    }

    @Override
    public synchronized T get(int index) {
        return list.get(index);
    }

    @Override
    public synchronized int indexOf(T item) {
        return list.indexOf(item);
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public synchronized int getLength() {
        return list.getLength();
    }

    @Override
    public synchronized Comparator<T> getComparator() {
        return list.getComparator();
    }

    @Override
    public synchronized void setComparator(Comparator<T> comparator) {
        Comparator<T> previousComparator = getComparator();
        list.setComparator(comparator);
        listListeners.comparatorChanged(this, previousComparator);
    }

    /**
     * NOTE Callers must manually synchronize on the SynchronizedList
     * instance to ensure thread safety during iteration.
     */
    @Override
    public Iterator<T> iterator() {
        return new ImmutableIterator<T>(list.iterator());
    }

    @Override
    public ListenerList<ListListener<T>> getListListeners() {
        return listListeners;
    }
}
