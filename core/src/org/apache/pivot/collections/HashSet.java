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

import org.apache.pivot.util.ListenerList;

/**
 * Implementation of the {@link Set} interface that is backed by a
 * hash table.
 */
public class HashSet<E> implements Set<E>, Serializable {
    private static final long serialVersionUID = 0;

    protected HashMap<E, Void> hashMap = new HashMap<E, Void>();

    private transient SetListenerList<E> setListeners = new SetListenerList<E>();

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

    public boolean add(E element) {
        boolean added = false;

        if (!hashMap.containsKey(element)) {
            hashMap.put(element, null);
            added = true;

            setListeners.elementAdded(this, element);
        }

        return added;
    }

    public boolean remove(E element) {
        boolean removed = false;

        if (hashMap.containsKey(element)) {
            hashMap.remove(element);
            removed = true;

            setListeners.elementRemoved(this, element);
        }

        return removed;
    }

    public void clear() {
        if (!hashMap.isEmpty()) {
            hashMap.clear();
            setListeners.setCleared(this);
        }
    }

    public boolean contains(E element) {
        return hashMap.containsKey(element);
    }

    public boolean isEmpty() {
        return hashMap.isEmpty();
    }

    public int count() {
        return hashMap.count();
    }

    public Comparator<E> getComparator() {
        return hashMap.getComparator();
    }

    public void setComparator(Comparator<E> comparator) {
        hashMap.setComparator(comparator);
    }

    public Iterator<E> iterator() {
        return hashMap.iterator();
    }

    public ListenerList<SetListener<E>> getSetListeners() {
        return setListeners;
    }
}
