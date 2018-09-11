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
package org.apache.pivot.collections.immutable;

import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;

/**
 * Unmodifiable implementation of the {@link List} interface.
 * @param <T> Type of elements in this list.
 */
public final class ImmutableList<T> implements List<T> {
    private List<T> list = null;

    private ListListenerList<T> listListeners = new ListListenerList<>();

    private static final String ERROR_MSG = "An Immutable List cannot be modified.";

    public ImmutableList(final List<T> list) {
        Utils.checkNull(list, "list");

        this.list = list;
    }

    @Override
    @UnsupportedOperation
    public int add(final T item) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    @UnsupportedOperation
    public void insert(final T item, final int index) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    @UnsupportedOperation
    public T update(final int index, final T item) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    @UnsupportedOperation
    public int remove(final T item) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    @UnsupportedOperation
    public Sequence<T> remove(final int index, final int count) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    @UnsupportedOperation
    public void clear() {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public T get(final int index) {
        return list.get(index);
    }

    @Override
    public int indexOf(final T item) {
        return list.indexOf(item);
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public int getLength() {
        return list.getLength();
    }

    @Override
    public Comparator<T> getComparator() {
        return null;
    }

    @Override
    @UnsupportedOperation
    public void setComparator(final Comparator<T> comparator) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public Iterator<T> iterator() {
        return new ImmutableIterator<>(list.iterator());
    }

    @Override
    public String toString() {
        return list.toString();
    }

    /**
     * Get the list of listeners for this list.
     * <p> Not clear why this would be used, since the listener(s)
     * would only be called on changes to the list, which can't happen
     * on an immutable list.
     *
     * @return The list listener list.
     */
    @Override
    public ListenerList<ListListener<T>> getListListeners() {
        return listListeners;
    }
}
