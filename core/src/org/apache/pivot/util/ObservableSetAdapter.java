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
package org.apache.pivot.util;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Observable set that is backed by an instance of {@link Set}.
 */
public class ObservableSetAdapter<E> extends AbstractSet<E>
    implements ObservableSet<E> {
    private class ObservableSetIterator implements Iterator<E> {
        private Iterator<E> iterator;
        private E element = null;

        public ObservableSetIterator(Iterator<E> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public E next() {
            element = iterator.next();
            return element;
        }

        @Override
        public void remove() {
            if (element == null) {
                throw new IllegalStateException();
            }

            iterator.remove();

            observableSetListeners.elementRemoved(ObservableSetAdapter.this,
                element);

            element = null;
        }
    }

    private Set<E> set;
    private ObservableSetListenerList<E> observableSetListeners =
        new ObservableSetListenerList<E>();

    public ObservableSetAdapter(Set<E> set) {
        if (set == null) {
            throw new IllegalArgumentException();
        }

        this.set = set;
    }

    public Set<E> getSet() {
        return set;
    }

    @Override
    public boolean add(E element) {
        boolean added = set.add(element);

        if (added) {
            observableSetListeners.elementAdded(this, element);
        }

        return added;
    }

    @Override
    public boolean remove(Object element) {
        boolean removed = set.remove(element);

        if (removed) {
            observableSetListeners.elementRemoved(this, element);
        }

        return removed;
    }

    @Override
    public boolean contains(Object element) {
        return set.contains(element);
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public Iterator<E> iterator() {
        return new ObservableSetIterator(set.iterator());
    }

    @Override
    public boolean equals(Object object) {
        return set.equals(object);
    }

    @Override
    public int hashCode() {
        return set.hashCode();
    }

    @Override
    public ListenerList<ObservableSetListener<E>> getObservableSetListeners() {
        return observableSetListeners;
    }

    public static <E> ObservableSet<E> observableHashSet() {
        return new ObservableSetAdapter<E>(new HashSet<E>());
    }
}
