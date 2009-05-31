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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.util.ListenerList;

/**
 * Implementation of the {@link List} interface that is backed by an enum.
 *
 * @author tvolkert
 * @author gbrown
 */
public class EnumList<E extends Enum<E>> implements List<E>, Serializable {
    private static final long serialVersionUID = 0;

    private Class<E> enumClass;
    private E[] elements;

    private transient ListListenerList<E> listListeners = null;

    public EnumList() {
        this(null);
    }

    public EnumList(Class<E> enumClass) {
        this.enumClass = enumClass;

        if (enumClass == null) {
            elements = null;
        } else {
            elements = enumClass.getEnumConstants();
        }
    }

    public Class<E> getEnumClass() {
        return enumClass;
    }

    public void setEnumClass(Class<E> enumClass) {
        Class<E> previousEnumClass = this.enumClass;

        if (enumClass != previousEnumClass) {
            this.enumClass = enumClass;

            // Clear old elements
            if (elements != null) {
                elements = null;

                // Notify listeners
                if (listListeners != null) {
                    listListeners.listCleared(this);
                }
            }

            // Add new elements
            elements = enumClass.getEnumConstants();

            if (elements != null) {
                // Notify listeners of the new elements
                if (listListeners != null) {
                    for (int i = 0; i < elements.length; i++) {
                        listListeners.itemInserted(this, i);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public final void setEnumClass(String enumClassName) {
        Class<E> enumClass;
        try {
            enumClass = (Class<E>)Class.forName(enumClassName);
        } catch (ClassNotFoundException exception) {
            throw new IllegalArgumentException(exception);
        }

        setEnumClass(enumClass);
    }

    public int add(E item) {
        throw new UnsupportedOperationException();
    }

    public void insert(E item, int index) {
        throw new UnsupportedOperationException();
    }

    public E update(int index, E item) {
        throw new UnsupportedOperationException();
    }

    public int remove(E item) {
        throw new UnsupportedOperationException();
    }

    public Sequence<E> remove(int index, int count) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public E get(int index) {
        return (elements != null ? elements[index] : null);
    }

    public int indexOf(E item) {
        int index = -1;

        if (elements != null) {
            for (int i = 0; i < elements.length; i++) {
                if (elements[i] == item) {
                    index = i;
                    break;
                }
            }
        }

        return index;
    }

    public int getLength() {
        return (elements != null ? elements.length : 0);
    }

    @SuppressWarnings("unchecked")
    public E[] toArray() {
        Object[] array = null;

        if (elements != null) {
            array = new Object[elements.length];
            System.arraycopy(elements, 0, array, 0, elements.length);
        }

        return (E[])array;
    }

    public Comparator<E> getComparator() {
        return null;
    }

    public void setComparator(Comparator<E> comparator) {
        throw new UnsupportedOperationException();
    }

    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private int i = 0;

            public boolean hasNext() {
                return (elements != null && i < elements.length);
            }

            public E next() {
                if (elements == null || i >= elements.length) {
                    throw new NoSuchElementException();
                }

                return elements[i++];
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public ListenerList<ListListener<E>> getListListeners() {
        if (listListeners == null) {
            listListeners = new ListListenerList<E>();
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
