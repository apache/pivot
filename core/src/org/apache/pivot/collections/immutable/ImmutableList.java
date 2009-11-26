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

import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;

/**
 * Unmodifiable implementation of the {@link List} interface.
 */
public final class ImmutableList<T> implements List<T> {
    private List<T> list = null;

    private ListListenerList<T> listListeners = new ListListenerList<T>();

    public ImmutableList(List<T> list) {
        if (list == null) {
            throw new IllegalArgumentException("list is null.");
        }

        this.list = list;
    }

    @Override
    public int add(T item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insert(T item, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T update(int index, T item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int remove(T item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Sequence<T> remove(int index, int count) {
        throw new UnsupportedOperationException();    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
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
    public void setComparator(Comparator<T> comparator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        return new ImmutableIterator<T>(list.iterator());
    }

    @Override
    public String toString() {
        return list.toString();
    }

    @Override
    public ListenerList<ListListener<T>> getListListeners() {
        return listListeners;
    }
}
