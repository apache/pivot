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
 */
public class EnumList<E extends Enum<E>> implements List<E>, Serializable {
    private static final long serialVersionUID = 5104856822133576300L;

    private class ItemIterator implements Iterator<E> {
        private int i = 0;

        @Override
        public boolean hasNext() {
            return (i < items.length);
        }

        @Override
        public E next() {
            if (i >= items.length) {
                throw new NoSuchElementException();
            }

            return items[i++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

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

    @Override
    public int add(E item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insert(E item, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E update(int index, E item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int remove(E item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Sequence<E> remove(int index, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E get(int index) {
        return items[index];
    }

    @Override
    public int indexOf(E item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null.");
        }

        return item.ordinal();
    }

    @Override
    public boolean isEmpty() {
        return (items.length == 0);
    }

    @Override
    public int getLength() {
        return items.length;
    }

    public E[] toArray() {
        return Arrays.copyOf(items, items.length);
    }

    @Override
    public Comparator<E> getComparator() {
        return null;
    }

    @Override
    public void setComparator(Comparator<E> comparator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        return new ItemIterator();
    }

    @Override
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
