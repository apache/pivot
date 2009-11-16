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
package org.apache.pivot.collections.concurrent;

import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.collections.Set;
import org.apache.pivot.collections.SetListener;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;

/**
 * Synchronized implementation of the {@link Set} interface.
 */
public class SynchronizedSet<E> implements Set<E> {
    private static class SynchronizedSetListenerList<E>
        extends SetListenerList<E> {
        @Override
        public synchronized void add(SetListener<E> listener) {
            super.add(listener);
        }

        @Override
        public synchronized void remove(SetListener<E> listener) {
            super.remove(listener);
        }

        @Override
        public synchronized void elementAdded(Set<E> set, E element) {
            super.elementAdded(set, element);
        }

        @Override
        public synchronized void elementRemoved(Set<E> set, E element) {
            super.elementRemoved(set, element);
        }

        @Override
        public synchronized void setCleared(Set<E> set) {
            super.setCleared(set);
        }

        @Override
        public synchronized void comparatorChanged(Set<E> set, Comparator<E> previousComparator) {
            super.comparatorChanged(set, previousComparator);
        }
    }

    private Set<E> set;
    private SynchronizedSetListenerList<E> setListeners = new SynchronizedSetListenerList<E>();

    public SynchronizedSet(Set<E> set) {
        if (set == null) {
            throw new IllegalArgumentException("set cannot be null.");
        }

        this.set = set;
    }

    @Override
    public synchronized boolean add(E element) {
        boolean added = false;

        if (!contains(element)) {
            set.add(element);
            added = true;

            setListeners.elementAdded(this, element);
        }

        return added;
    }

    @Override
    public synchronized boolean remove(E element) {
        boolean removed = false;

        if (contains(element)) {
            set.remove(element);
            removed = true;

            setListeners.elementRemoved(this, element);
        }

        return removed;
    }

    @Override
    public synchronized boolean contains(E element) {
        return set.contains(element);
    }

    @Override
    public synchronized boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public synchronized void clear() {
        if (!set.isEmpty()) {
            set.clear();
            setListeners.setCleared(this);
        }
    }

    @Override
    public synchronized int getCount() {
        return set.getCount();
    }

    @Override
    public synchronized Comparator<E> getComparator() {
        return set.getComparator();
    }

    @Override
    public synchronized void setComparator(Comparator<E> comparator) {
        Comparator<E> previousComparator = getComparator();
        set.setComparator(comparator);
        setListeners.comparatorChanged(this, previousComparator);
    }

    /**
     * NOTE Callers must manually synchronize on the SynchronizedSet
     * instance to ensure thread safety during iteration.
     */
    @Override
    public Iterator<E> iterator() {
        return new ImmutableIterator<E>(set.iterator());
    }

    @Override
    public ListenerList<SetListener<E>> getSetListeners() {
        return setListeners;
    }
}
