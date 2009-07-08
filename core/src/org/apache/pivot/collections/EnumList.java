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
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;


/**
 * Implementation of the {@link List} interface that is backed by an enum.
 *
 * @author tvolkert
 * @author gbrown
 */
public class EnumList<E extends Enum<E>> implements List<E>, Serializable {
    private class ItemIterator implements Iterator<E> {
        private int i = 0;

        public boolean hasNext() {
            return (i < items.length);
        }

        public E next() {
            if (i >= items.length) {
                throw new NoSuchElementException();
            }

            return items[i++];
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    };

    private static final long serialVersionUID = 0;

    private Class<E> enumClass;
    private E[] items;

    private transient ListListenerList<E> listListeners = new ListListenerList<E>();

    public EnumList(Class<E> enumClass) {
        this.enumClass = enumClass;
        items = enumClass.getEnumConstants();
    }

    public Class<E> getEnumClass() {
        return enumClass;
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
        return items[index];
    }

    public int indexOf(E item) {
        if (item == null) {
            throw new IllegalArgumentException();
        }

        return item.ordinal();
    }

    public int getLength() {
        return items.length;
    }

    public E[] toArray() {
        return Arrays.copyOf(items, items.length);
    }

    public Comparator<E> getComparator() {
        return null;
    }

    public void setComparator(Comparator<E> comparator) {
        throw new UnsupportedOperationException();
    }

    public Iterator<E> iterator() {
        return new ItemIterator();
    }

    public ListenerList<ListListener<E>> getListListeners() {
        return listListeners;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getClass().getName());
        sb.append(" [");

        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(items[i]);
        }

        sb.append("]");

        return sb.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        return (o instanceof EnumList<?>
            && ((EnumList<E>)o).enumClass == enumClass);
    }

    @Override
    public int hashCode() {
        return enumClass.hashCode();
    }
}
