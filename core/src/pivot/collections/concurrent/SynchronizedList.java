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
package pivot.collections.concurrent;

import java.util.Comparator;
import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.util.ListenerList;
import pivot.util.concurrent.SynchronizedListenerList;

/**
 * Synchronized implementation of the {@link List} interface.
 *
 * @author gbrown
 */
public class SynchronizedList<T> extends SynchronizedCollection<T>
    implements List<T> {
    /**
     * Synchronized list listener list implementation. Proxies events fired
     * by inner list to listeners of synchronized list.
     *
     * @author gbrown
     */
    private class SynchronizedListListenerList
        extends SynchronizedListenerList<ListListener<T>>
        implements ListListener<T> {
        public synchronized void itemInserted(List<T> list, int index) {
            for (ListListener<T> listener : this) {
                listener.itemInserted(SynchronizedList.this, index);
            }
        }

        public synchronized void itemsRemoved(List<T> list, int index, Sequence<T> items) {
            for (ListListener<T> listener : this) {
                listener.itemsRemoved(SynchronizedList.this, index, items);
            }
        }

        public synchronized void itemUpdated(List<T> list, int index, T previousItem) {
            for (ListListener<T> listener : this) {
                listener.itemUpdated(SynchronizedList.this, index, previousItem);
            }
        }

        public synchronized void listCleared(List<T> list) {
            for (ListListener<T> listener : this) {
                listener.listCleared(SynchronizedList.this);
            }
        }

        public synchronized void comparatorChanged(List<T> list, Comparator<T> previousComparator) {
            for (ListListener<T> listener : this) {
                listener.comparatorChanged(SynchronizedList.this, previousComparator);
            }
        }
    }

    private SynchronizedListListenerList listListeners = new SynchronizedListListenerList();

    public SynchronizedList(List<T> list) {
        super(list);

        list.getListListeners().add(listListeners);
    }

    public synchronized int add(T item) {
        return ((List<T>)collection).add(item);
    }

    public synchronized void insert(T item, int index) {
        ((List<T>)collection).insert(item, index);
    }

    public synchronized T update(int index, T item) {
        return ((List<T>)collection).update(index, item);
    }

    public int remove (T item) {
        return ((List<T>)collection).remove(item);
    }

    public synchronized Sequence<T> remove(int index, int count) {
        return ((List<T>)collection).remove(index, count);
    }

    public synchronized T get(int index) {
        return ((List<T>)collection).get(index);
    }

    public synchronized int indexOf(T item) {
        return ((List<T>)collection).indexOf(item);
    }

    public synchronized int getLength() {
        return ((List<T>)collection).getLength();
    }

    public ListenerList<ListListener<T>> getListListeners() {
        return listListeners;
    }
}
