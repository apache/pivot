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
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.pivot.util.ListenerList;

/**
 * Implementation of the {@link Set} interface that is backed by a
 * hash table.
 */
public class HashSet<E> implements Set<E>, Serializable {
    private static final long serialVersionUID = 4095129319373194969L;

    private class ElementIterator implements Iterator<E> {
        private Iterator<E> iterator;

        private E element = null;

        public ElementIterator(Iterator<E> iterator) {
            if (iterator == null) {
                throw new IllegalArgumentException("iterator is null.");
            }

            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            element = iterator.next();
            return element;
        }

        @Override
        public void remove() {
            if (element == null) {
                throw new IllegalStateException();
            }

            iterator.remove();

            if (setListeners != null) {
                setListeners.elementRemoved(HashSet.this, element);
            }

            element = null;
        }
    }

    protected HashMap<E, Void> hashMap = new HashMap<E, Void>();

    private transient SetListenerList<E> setListeners = null;

    public HashSet() {
    }

    public HashSet(Set<E> set) {
        for (E element : set) {
            add(element);
        }
    }

    public HashSet(E... elements) {
        for (int i = 0; i < elements.length; i++) {
            E element = elements[i];
            add(element);
        }
    }

    public HashSet(Comparator<E> comparator) {
        setComparator(comparator);
    }

    @Override
    public boolean add(E element) {
        boolean added = false;

        if (!hashMap.containsKey(element)) {
            hashMap.put(element, null);
            added = true;

            if (setListeners != null) {
                setListeners.elementAdded(this, element);
            }
        }

        return added;
    }

    @Override
    public boolean remove(E element) {
        boolean removed = false;

        if (hashMap.containsKey(element)) {
            hashMap.remove(element);
            removed = true;

            if (setListeners != null) {
                setListeners.elementRemoved(this, element);
            }
        }

        return removed;
    }

    @Override
    public void clear() {
        if (!hashMap.isEmpty()) {
            hashMap.clear();

            if (setListeners != null) {
                setListeners.setCleared(this);
            }
        }
    }

    @Override
    public boolean contains(E element) {
        return hashMap.containsKey(element);
    }

    @Override
    public boolean isEmpty() {
        return hashMap.isEmpty();
    }

    @Override
    public int getCount() {
        return hashMap.getCount();
    }

    @Override
    public Comparator<E> getComparator() {
        return hashMap.getComparator();
    }

    @Override
    public void setComparator(Comparator<E> comparator) {
        Comparator<E> previousComparator = getComparator();

        hashMap.setComparator(comparator);

        if (setListeners != null) {
            setListeners.comparatorChanged(this, previousComparator);
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new ElementIterator(hashMap.iterator());
    }

    @Override
    public ListenerList<SetListener<E>> getSetListeners() {
        if (setListeners == null) {
            setListeners = new SetListenerList<E>();
        }

        return setListeners;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        boolean equals = false;

        if (this == o) {
            equals = true;
        } else if (o instanceof Set<?>) {
            Set<E> set = (Set<E>)o;

            if (getCount() == set.getCount()) {
                for (E element : this) {
                    equals = set.contains(element);

                    if (!equals) {
                        break;
                    }
                }

            }
        }

        return equals;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;

        for (E element : this) {
            hashCode = 31 * hashCode + element.hashCode();
        }

        return hashCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getClass().getName());
        sb.append(" (");

        int i = 0;
        for (E element : this) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(element);
            i++;
        }

        sb.append(")");

        return sb.toString();
    }
}
