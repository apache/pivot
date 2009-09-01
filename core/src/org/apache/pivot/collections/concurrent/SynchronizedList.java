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

import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;


/**
 * Synchronized implementation of the {@link List} interface.
 */
public class SynchronizedList<T> extends SynchronizedCollection<T>
    implements List<T> {
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

    private SynchronizedListListenerList<T> listListeners = new SynchronizedListListenerList<T>();

    public SynchronizedList(List<T> list) {
        super(list);
    }

    @Override
    public synchronized int add(T item) {
        int index = ((List<T>)collection).add(item);
        listListeners.itemInserted(this, index);

        return index;
    }

    @Override
    public synchronized void insert(T item, int index) {
        ((List<T>)collection).insert(item, index);
        listListeners.itemInserted(this, index);
    }

    @Override
    public synchronized T update(int index, T item) {
        T previousItem = ((List<T>)collection).update(index, item);
        if (previousItem != item) {
            listListeners.itemUpdated(this, index, previousItem);
        }

        return previousItem;
    }

    @Override
    public synchronized int remove (T item) {
        int index = indexOf(item);
        if (index == -1) {
            throw new IllegalArgumentException();
        }

        remove(index, 1);

        return index;
    }

    @Override
    public synchronized Sequence<T> remove(int index, int count) {
        Sequence<T> removed = ((List<T>)collection).remove(index, count);
        if (count > 0) {
            listListeners.itemsRemoved(this, index, removed);
        }

        return removed;
    }

    @Override
    public synchronized T get(int index) {
        return ((List<T>)collection).get(index);
    }

    @Override
    public synchronized int indexOf(T item) {
        return ((List<T>)collection).indexOf(item);
    }

    @Override
    public synchronized int getLength() {
        return ((List<T>)collection).getLength();
    }

    @Override
    public ListenerList<ListListener<T>> getListListeners() {
        return listListeners;
    }
}
