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
package pivot.collections;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.util.ListenerList;

/**
 * Exposes enumeration values as a <tt>List</tt>, allowing callers to use
 * <tt>enum</tt>s as their data models.
 *
 * @author tvolkert
 */
public class EnumList implements List<Enum> {
    private Enum[] elements;

    private Class<? extends Enum> enumClass = null;

    private Comparator<Enum> comparator = null;
    private ListListenerList<Enum> listListeners = null;

    public EnumList() {
        this.enumClass = Enum.class;
        elements = new Enum[0];
    }

    public EnumList(Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;
        elements = enumClass.getEnumConstants();
    }

    public Class<? extends Enum> getEnumClass() {
        return enumClass;
    }

    public void setEnumClass(Class<? extends Enum> enumClass) {
        if (enumClass.getEnumConstants() == null) {
            throw new ClassCastException();
        }

        Class <? extends Enum> previousEnumClass = this.enumClass;

        if (enumClass != previousEnumClass) {
            this.enumClass = enumClass;

            // Clear old elements
            elements = new Enum[0];

            // Notify listeners
            if (listListeners != null) {
                listListeners.itemsRemoved(this, 0, null);
            }

            // Add new elements
            elements = enumClass.getEnumConstants();

            // Sort if necessary
            if (comparator != null) {
                Arrays.sort(elements, comparator);
            }

            // Notify listeners of the new elements
            if (listListeners != null) {
                for (int i = 0, n = elements.length; i < n; i++) {
                    listListeners.itemInserted(this, i);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public final void setEnumClass(String enumClass) {
        try {
            setEnumClass((Class<? extends Enum>)Class.forName(enumClass));
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public int add(Enum item) {
        throw new UnsupportedOperationException();
    }

    public void insert(Enum item, int index) {
        throw new UnsupportedOperationException();
    }

    public Enum update(int index, Enum item) {
        throw new UnsupportedOperationException();
    }

    public int remove(Enum item) {
        throw new UnsupportedOperationException();
    }

    public Sequence<Enum> remove(int index, int count) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public Enum get(int index) {
        return elements[index];
    }

    public int indexOf(Enum item) {
        int index = -1;

        if (comparator == null) {
            for (int i = 0, n = elements.length; i < n && index == -1; i++) {
                if (elements[i] == item) {
                    index = i;
                }
            }
        } else {
            // Perform a binary search to find the index
            index = Search.binarySearch(this, item, comparator);
            if (index < 0) {
                index = -1;
            }
        }

        return index;
    }

    public int getLength() {
        return elements.length;
    }

    @SuppressWarnings("unchecked")
    public Enum[] toArray() {
        Enum[] array = null;

        int n = elements.length;
        if (n > 0) {
            Class<?> type = elements[0].getClass();
            array = (Enum[])Array.newInstance(type, n);
            System.arraycopy(elements, 0, array, 0, n);
        }

        return array;
    }

    public Comparator<Enum> getComparator() {
        return comparator;
    }

    public void setComparator(Comparator<Enum> comparator) {
        Comparator<Enum> previousComparator = this.comparator;

        if (previousComparator != comparator) {
            this.comparator = comparator;

            // Perform the sort
            if (comparator != null) {
                Arrays.sort(elements, comparator);
            }

            // Notify listeners
            if (listListeners != null) {
                listListeners.comparatorChanged(this, previousComparator);
            }
        }
    }

    public Iterator<Enum> iterator() {
        return new Iterator<Enum>() {
            private int i = 0;

            public boolean hasNext() {
                return (i < elements.length);
            }

            public Enum next() {
                if (i >= elements.length) {
                    throw new NoSuchElementException();
                }

                return elements[i++];
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public ListenerList<ListListener<Enum>> getListListeners() {
        if (listListeners == null) {
            listListeners = new ListListenerList<Enum>();
        }

        return listListeners;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[");

        for (int i = 0, n = getLength(); i < n; i++) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(get(i));
        }

        sb.append("]");

        return sb.toString();
    }
}
