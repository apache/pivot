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
import org.apache.pivot.collections.Set;
import org.apache.pivot.collections.SetListener;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;

/**
 * Unmodifiable implementation of the {@link Set} interface.
 * @param <E> Type of elements in this set.
 */
public final class ImmutableSet<E> implements Set<E> {
    private Set<E> set = null;

    private SetListener.Listeners<E> setListeners = new SetListener.Listeners<>();

    private static final String ERROR_MSG = "An Immutable Set cannot be modified.";

    public ImmutableSet(final Set<E> set) {
        Utils.checkNull(set, "set");

        this.set = set;
    }

    @Override
    @UnsupportedOperation
    public boolean add(final E element) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    @UnsupportedOperation
    public boolean remove(final E element) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    @UnsupportedOperation
    public void clear() {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public boolean contains(final E element) {
        return set.contains(element);
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public int getCount() {
        return set.getCount();
    }

    @Override
    public Comparator<E> getComparator() {
        return null;
    }

    @Override
    @UnsupportedOperation
    public void setComparator(final Comparator<E> comparator) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public Iterator<E> iterator() {
        return new ImmutableIterator<>(set.iterator());
    }

    @Override
    public String toString() {
        return set.toString();
    }

    @Override
    public ListenerList<SetListener<E>> getSetListeners() {
        return setListeners;
    }
}
