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
 * Implementation of the {@link Set} interface that is backed by an array of
 * enum values.
 */
public class EnumSet<E extends Enum<E>> implements Set<E>, Serializable {
    private static final long serialVersionUID = 3544488357505145448L;

    private class ElementIterator implements Iterator<E> {
        private int i = 0;
        private E next = null;

        @Override
        public boolean hasNext() {
            if (next == null) {
                while (i < elements.length
                    && !members[i]) {
                    i++;
                }

                if (i < elements.length) {
                    next = elements[i];
                } else {
                    next = null;
                }
            }

            return (next != null);
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            E nextLocal = this.next;
            this.next = null;
            i++;

            return nextLocal;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private Class<E> enumClass;
    private E[] elements;
    private boolean[] members;
    private int count = 0;

    private transient SetListenerList<E> setListeners = new SetListenerList<E>();

    public EnumSet(Class<E> enumClass) {
        this.enumClass = enumClass;

        elements = enumClass.getEnumConstants();
        members = new boolean[elements.length];
    }

    public Class<E> getEnumClass() {
        return enumClass;
    }

    @Override
    public boolean add(E element) {
        boolean added = false;

        int ordinal = element.ordinal();
        if (!members[ordinal]) {
            members[ordinal] = true;
            added = true;
            count++;

            setListeners.elementAdded(this, element);
        }

        return added;
    }

    @Override
    public boolean remove(E element) {
        boolean removed = false;

        int ordinal = element.ordinal();
        if (members[ordinal]) {
            members[ordinal] = false;
            removed = true;
            count--;

            setListeners.elementRemoved(this, element);
        }

        return removed;
    }

    @Override
    public void clear() {
        if (count > 0) {
            members = new boolean[members.length];
            count = 0;
            setListeners.setCleared(this);
        }
    }

    @Override
    public boolean contains(E element) {
        return members[element.ordinal()];
    }

    @Override
    public boolean isEmpty() {
        return count == 0;
    }

    @Override
    public int getCount() {
        return count;
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
        return new ElementIterator();
    }

    @Override
    public ListenerList<SetListener<E>> getSetListeners() {
        return setListeners;
    }
}
