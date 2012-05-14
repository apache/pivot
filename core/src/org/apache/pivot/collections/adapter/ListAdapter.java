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
package org.apache.pivot.collections.adapter;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.RandomAccess;

import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;

/**
 * Implementation of the {@link List} interface that is backed by an
 * instance of {@link java.util.List}.
 */
public class ListAdapter<T> implements List<T>, Serializable {
    private static final long serialVersionUID = 1649736907064653706L;

    private java.util.List<T> list = null;
    private Comparator<T> comparator = null;

    private transient ListListenerList<T> listListeners = new ListListenerList<T>();

    public ListAdapter(java.util.List<T> list) {
        if (list == null) {
            throw new IllegalArgumentException("list is null.");
        }

        this.list = list;
    }

    public java.util.List<T> getList() {
        return list;
    }

    @Override
    public int add(T item) {
        int index = -1;

        if (comparator == null) {
            index = getLength();
        } else {
            // Perform a binary search to find the insertion point
            index = Collections.binarySearch(list, item, comparator);
            if (index < 0) {
                index = -(index + 1);
            }
        }

        list.add(index, item);
        listListeners.itemInserted(this, index);

        return index;
    }

    @Override
    public void insert(T item, int index) {
        if (comparator != null
            && Collections.binarySearch(list, item, comparator) != -(index + 1)) {
            throw new IllegalArgumentException("Illegal insertion point.");
        }

        list.add(index, item);
        listListeners.itemInserted(this, index);
    }

    @Override
    public T update(int index, T item) {
        if (comparator != null) {
            // Ensure that the new item is greater or equal to its
            // predecessor and less than or equal to its successor
            T predecessor = null;
            T successor = null;

            if (list instanceof RandomAccess) {
                if (index > 0) {
                    predecessor = list.get(index - 1);
                }

                if (index < getLength() - 1) {
                    successor = list.get(index + 1);
                }
            } else {
                if (index == 0) {
                    // We're at the head of the list; successor is at index 1
                    successor = list.get(1);
                } else {
                    ListIterator<T> listIterator = list.listIterator(index - 1);

                    // Get the predecessor
                    predecessor = listIterator.next();

                    // Advance to the item being updated
                    listIterator.next();

                    // Get the successor if one exists
                    if (listIterator.hasNext()) {
                        successor = listIterator.next();
                    }
                }
            }

            if ((predecessor != null
                    && comparator.compare(item, predecessor) < 0)
                || (successor != null
                        && comparator.compare(item, successor) > 0)) {
                throw new IllegalArgumentException("Illegal item modification.");
            }
        }

        T previousItem;

        if (list instanceof RandomAccess) {
            previousItem = list.get(index);

            if (previousItem != item) {
                list.set(index, item);
            }

            listListeners.itemUpdated(this, index, previousItem);
        } else {
            ListIterator<T> listIterator = list.listIterator(index);
            previousItem = listIterator.next();

            if (previousItem != item) {
                try {
                    listIterator.set(item);
                } catch (UnsupportedOperationException exception) {
                    list.set(index, item);
                }
            }

            listListeners.itemUpdated(this, index, previousItem);
        }

        return previousItem;
    }

    @Override
    public int remove(T item) {
        int index = indexOf(item);
        if (index != -1) {
            remove(index, 1);
        }

        return index;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Sequence<T> remove(int index, int count) {
        java.util.List<T> removedList = null;
        try {
            removedList = list.getClass().newInstance();
        } catch(IllegalAccessException exception) {
            throw new RuntimeException(exception);
        } catch(InstantiationException exception) {
            throw new RuntimeException(exception);
        }

        List<T> removed = new ListAdapter<T>(removedList);

        if (count > 0) {
            for (int i = count - 1; i >= 0; i--) {
                removedList.add(0, list.remove(index + i));
            }

            listListeners.itemsRemoved(this, index, removed);
        }

        return removed;
    }

    @Override
    public void clear() {
        if (getLength() > 0) {
            list.clear();
            listListeners.listCleared(this);
        }
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public int indexOf(T item) {
        return list.indexOf(item);
    }

    @Override
    public boolean isEmpty() {
        return (list.isEmpty());
    }

    @Override
    public int getLength() {
        return list.size();
    }

    @Override
    public Comparator<T> getComparator() {
        return comparator;
    }

    @Override
    public void setComparator(Comparator<T> comparator) {
        Comparator<T> previousComparator = this.comparator;

        if (previousComparator != comparator) {
            if (comparator != null) {
                Collections.sort(list, comparator);
            }

            this.comparator = comparator;

            listListeners.comparatorChanged(this, previousComparator);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new ImmutableIterator<T>(list.iterator());
    }

    @Override
    public ListenerList<ListListener<T>> getListListeners() {
        return listListeners;
    }

    @Override
    public String toString() {
        return list.toString();
    }
}
