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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.pivot.util.ListenerList;

/**
 * Implementation of the {@link List} interface that is backed by an
 * array.
 * <p>
 * NOTE This class is not thread-safe. For concurrent access, use a
 * {@link org.apache.pivot.collections.concurrent.SynchronizedList}.
 */
public class ArrayList<T> implements List<T>, Serializable {
    private class ArrayListItemIterator implements ItemIterator<T> {
        private int index = 0;
        private int modificationCount;

        public ArrayListItemIterator() {
            modificationCount = ArrayList.this.modificationCount;
        }

        @Override
        public boolean hasNext() {
            return (index < getLength());
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            if (modificationCount != ArrayList.this.modificationCount) {
                throw new ConcurrentModificationException();
            }

            return get(index++);
        }

        @Override
        public boolean hasPrevious() {
            return (index >= 0);
        }

        @Override
        public T previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }

            if (modificationCount != ArrayList.this.modificationCount) {
                throw new ConcurrentModificationException();
            }

            return get(index--);
        }

        @Override
        public void insert(T item) {
            if (index < 0
                || index >= ArrayList.this.length) {
                throw new IllegalStateException();
            }

            ArrayList.this.insert(item, index);
            modificationCount++;
        }

        @Override
        public void update(T item) {
            if (index < 0
                || index >= ArrayList.this.length) {
                throw new IllegalStateException();
            }

            ArrayList.this.update(index, item);
            modificationCount++;
        }

        @Override
        public void remove() {
            if (index < 0
                || index >= ArrayList.this.length) {
                throw new IllegalStateException();
            }

            ArrayList.this.remove(index, 1);
            modificationCount++;
        }
    }

    private static final long serialVersionUID = 0;

    private Object[] items;
    private int length = 0;

    private Comparator<T> comparator = null;

    private transient int modificationCount = 0;
    private transient ListListenerList<T> listListeners = null;

    public static final int DEFAULT_CAPACITY = 10;

    public ArrayList() {
        items = new Object[DEFAULT_CAPACITY];
    }

    public ArrayList(Comparator<T> comparator) {
        this();
        this.comparator = comparator;
    }

    public ArrayList(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException();
        }

        items = new Object[capacity];
    }

    public ArrayList(T... items) {
        this(items, 0, items.length);
    }

    public ArrayList(T[] items, int index, int count) {
        if (items == null) {
            throw new IllegalArgumentException();
        }

        if (count < 0) {
            throw new IllegalArgumentException();
        }

        if (index < 0
            || index + count > items.length) {
            throw new IndexOutOfBoundsException();
        }

        this.items = new Object[count];
        System.arraycopy(items, index, this.items, 0, count);

        length = count;
    }

    public ArrayList(Sequence<T> items) {
        this(items, 0, items.getLength());
    }

    public ArrayList(Sequence<T> items, int index, int count) {
        if (items == null) {
            throw new IllegalArgumentException();
        }

        if (count < 0) {
            throw new IllegalArgumentException();
        }

        if (index < 0
            || index + count > items.getLength()) {
            throw new IndexOutOfBoundsException();
        }

        this.items = new Object[count];

        for (int i = 0; i < count; i++) {
            this.items[i] = items.get(index + i);
        }

        length = count;
    }

    @Override
    public int add(T item) {
        int index = -1;

        if (comparator == null) {
            index = getLength();
            insert(item, index);
        }
        else {
            // Perform a binary search to find the insertion point
            index = binarySearch(this, item, comparator);
            if (index < 0) {
                index = -(index + 1);
            }

            insert(item, index, false);
        }

        return index;
    }

    @Override
    public void insert(T item, int index) {
        insert(item, index, true);
    }

    private void insert(T item, int index, boolean validate) {
        if (index < 0
            || index > length) {
            throw new IndexOutOfBoundsException();
        }

        if (comparator != null
            && validate) {
            int i = binarySearch(this, item, comparator);
            if (i < 0) {
                i = -(i + 1);
            }

            if (index != i) {
                throw new IllegalArgumentException("Illegal insertion point.");
            }
        }

        // Insert item
        ensureCapacity(length + 1);
        System.arraycopy(items, index, items, index + 1, length - index);
        items[index] = item;

        length++;
        modificationCount++;

        if (listListeners != null) {
            listListeners.itemInserted(this, index);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T update(int index, T item) {
        if (index < 0
            || index >= length) {
            throw new IndexOutOfBoundsException();
        }

        T previousItem = (T)items[index];

        if (previousItem != item) {
            if (comparator != null) {
                // Ensure that the new item is greater or equal to its
                // predecessor and less than or equal to its successor
                T predecessorItem = (index > 0 ? (T)items[index - 1] : null);
                T successorItem = (index < length - 1 ? (T)items[index + 1] : null);

                if ((predecessorItem != null
                    && comparator.compare(item, predecessorItem) == -1)
                    || (successorItem != null
                    && comparator.compare(item, successorItem) == 1)) {
                    throw new IllegalArgumentException("Illegal item modification.");
                }
            }

            items[index] = item;

            modificationCount++;
        }

        if (listListeners != null) {
            listListeners.itemUpdated(this, index, previousItem);
        }

        return previousItem;
    }

    @Override
    public int remove(T item) {
        int index = indexOf(item);

        if (index >= 0) {
           remove(index, 1);
        }

        return index;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Sequence<T> remove(int index, int count) {
        if (index < 0
            || index + count > length) {
            throw new IndexOutOfBoundsException();
        }

        ArrayList<T> removed = new ArrayList<T>((T[])items, index, count);

        // Remove items
        if (count > 0) {
            int end = index + count;
            System.arraycopy(items, index + count, items, index, length - end);

            length -= count;
            modificationCount++;

            // Clear any orphaned references
            for (int i = length, n = length + count; i < n; i++) {
                items[i] =  null;
            }

            if (listListeners != null) {
                listListeners.itemsRemoved(this, index, removed);
            }
        }

        return removed;
    }

    @Override
    public void clear() {
        if (length > 0) {
            items = new Object[items.length];
            length = 0;
            modificationCount++;

            if (listListeners != null) {
                listListeners.listCleared(this);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T get(int index) {
        if (index < 0
            || index >= length) {
            throw new IndexOutOfBoundsException();
        }

        return (T)items[index];
    }

    @Override
    public int indexOf(T item) {
        int index = -1;

        if (comparator == null) {
            int i = 0;
            while (i < length) {
                if (item == null) {
                    if (items[i] == null) {
                        break;
                    }
                } else {
                    if (item.equals(items[i])) {
                        break;
                    }
                }

                i++;
            }

            if (i < length) {
                index = i;
            } else {
                index = -1;
            }
        }
        else {
            // Perform a binary search to find the index
            index = binarySearch(this, item, comparator);
            if (index < 0) {
                index = -1;
            }
        }

        return index;
    }

    @Override
    public int getLength() {
        return length;
    }

    public void trimToSize() {
        Object[] items = new Object[length];
        System.arraycopy(this.items, 0, items, 0, length);

        this.items = items;
        length = items.length;
    }

    public void ensureCapacity(int capacity) {
        if (capacity > items.length) {
            capacity = Math.max(this.items.length * 3 / 2, capacity);
            Object[] items = new Object[capacity];
            System.arraycopy(this.items, 0, items, 0, length);

            this.items = items;
        }
    }

    public int getCapacity() {
        return items.length;
    }

    public T[] toArray(Class<? extends T[]> type) {
        return Arrays.copyOf(items, length, type);
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
                sort(this, comparator);
            }

            // Set the new comparator
            this.comparator = comparator;

            if (listListeners != null) {
                listListeners.comparatorChanged(this, previousComparator);
            }
        }
    }

    @Override
    public ItemIterator<T> iterator() {
        return new ArrayListItemIterator();
    }

    @Override
    public ListenerList<ListListener<T>> getListListeners() {
        if (listListeners == null) {
            listListeners = new ListListenerList<T>();
        }

        return listListeners;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        boolean equals = true;

        if (o instanceof ArrayList<?>) {
            ArrayList<T> arrayList = (ArrayList<T>)o;

            if (getLength() == arrayList.getLength()) {
                Iterator<T> iterator = iterator();
                Iterator<T> arrayListIterator = arrayList.iterator();

                while (iterator.hasNext()
                    && arrayListIterator.hasNext()
                    && equals) {
                    equals &= iterator.next().equals(arrayListIterator.next());
                }
            } else {
                equals = false;
            }
        }

        return equals;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (T item : this) {
            hashCode = 31 * hashCode + (item == null ? 0 : item.hashCode());
        }

        return hashCode;
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

    public static <T> void sort(ArrayList<T> arrayList, Comparator<T> comparator) {
        sort(arrayList, 0, arrayList.getLength(), comparator);
    }

    @SuppressWarnings("unchecked")
    public static <T> void sort(ArrayList<T> arrayList, int from, int to, Comparator<T> comparator) {
        if (arrayList == null
            || comparator == null) {
            throw new IllegalArgumentException();
        }

        Arrays.sort((T[])arrayList.items, from, to, comparator);

        arrayList.modificationCount++;
    }

    public static <T extends Comparable<? super T>> void sort(ArrayList<T> arrayList) {
        sort(arrayList, new Comparator<T>() {
            @Override
            public int compare(T t1, T t2) {
                return t1.compareTo(t2);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> int binarySearch(ArrayList<T> arrayList, T item,
        final Comparator<T> comparator) {
        if (arrayList == null
            || item == null
            || comparator == null) {
            throw new IllegalArgumentException();
        }

        int index = Arrays.binarySearch((T[])arrayList.items, 0, arrayList.length, item, comparator);

        return index;
    }

    public static <T extends Comparable<? super T>> int binarySearch(ArrayList<T> arrayList,
        T item) {
        return binarySearch(arrayList, item, new Comparator<T>() {
            @Override
            public int compare(T t1, T t2) {
                return t1.compareTo(t2);
            }
        });
    }
}
