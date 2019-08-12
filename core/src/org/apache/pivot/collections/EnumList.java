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

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;

/**
 * A read-only implementation of the {@link List} interface that is backed by an enum.
 * <p> An {@code EnumList} cannot be modified once constructed and only ever contains all the
 * enum constant values defined in the class. As such, the {@code "add"} and {@code "remove"}
 * (and related) methods throw exceptions.
 * <p> This class is meant to facilitate using enum constants as elements in a dropdown list
 * (for instance). A useful way to do this is to override the {@code "toString()"} method of
 * the enum to provide a human-readable version of the enum constant value, which will then
 * appear in the UI.
 *
 * @param <E> The underlying enum type that backs this list.
 */
public class EnumList<E extends Enum<E>> extends ReadOnlySequence<E> implements List<E>, Serializable {
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
        @UnsupportedOperation
        public void remove() {
            throw new UnsupportedOperationException(unsupportedOperationMsg);
        }
    }

    private Class<E> enumClass;
    private E[] items;

    private transient ListListenerList<E> listListeners = new ListListenerList<>();

    /**
     * Construct the full list populated by the enum constants of the given class.
     *
     * @param enumClass The enum class whose constant values are used to fully populate the list.
     */
    public EnumList(final Class<E> enumClass) {
        this.enumClass = enumClass;
        items = enumClass.getEnumConstants();
    }

    public Class<E> getEnumClass() {
        return enumClass;
    }

    @Override
    @UnsupportedOperation
    public void clear() {
        throw new UnsupportedOperationException(unsupportedOperationMsg);
    }

    @Override
    public E get(final int index) {
        return items[index];
    }

    @Override
    public int indexOf(final E item) {
        Utils.checkNull(item, "item");

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

    /**
     * Always returns {@code null} because there can never be a {@link Comparator}
     * set to change from the "natural" ordering of the {@code Enum}.
     * @return {@code null} always.
     */
    @Override
    public Comparator<E> getComparator() {
        return null;
    }

    /**
     * Unsupported because the list is always ordered in the "natural" order of the
     * backing {@code Enum}.
     * @throws UnsupportedOperationException always.
     */
    @Override
    @UnsupportedOperation
    public void setComparator(final Comparator<E> comparator) {
        throw new UnsupportedOperationException(unsupportedOperationMsg);
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
    public boolean equals(final Object o) {
        return (o instanceof EnumList<?> && ((EnumList<E>) o).enumClass == enumClass);
    }

    @Override
    public int hashCode() {
        return enumClass.hashCode();
    }
}
