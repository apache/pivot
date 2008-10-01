/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.collections;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;

import pivot.util.ImmutableIterator;
import pivot.util.ListenerList;

/**
 * Implementation of the {@link Set} interface that is backed by a
 * hashtable.
 * <p>
 * TODO We're temporarily using a java.util.HashSet to back this set.
 * Eventually, we'll replace this with an internal set representation.
 */
public class HashSet<E> implements Set<E>, Serializable {
    public static final long serialVersionUID = 0;

    protected java.util.HashSet<E> hashSet = null;

    private Comparator<E> comparator = null;
    private transient SetListenerList<E> setListeners = new SetListenerList<E>();

    public HashSet() {
        hashSet = new java.util.HashSet<E>();
    }

    public HashSet(Set<E> set) {
        hashSet = new java.util.HashSet<E>();

        for (E element : set) {
            add(element);
        }
    }

    public HashSet(Comparator<E> comparator) {
        throw new UnsupportedOperationException("HashSet auto-sorting is not yet supported.");

        // this.comparator = comparator;
    }

    public void add(E element) {
        if (!hashSet.contains(element)) {
            hashSet.add(element);
            setListeners.elementAdded(this, element);
        }
    }

    public void remove(E element) {
        if (hashSet.contains(element)) {
            hashSet.remove(element);
            setListeners.elementRemoved(this, element);
        }
    }

    public void clear() {
        hashSet.clear();
        setListeners.setCleared(this);
    }

    public boolean contains(E element) {
        return hashSet.contains(element);
    }

    public boolean isEmpty() {
        return hashSet.isEmpty();
    }

    public Comparator<E> getComparator() {
        return comparator;
    }

    public void setComparator(Comparator<E> comparator) {
        // TODO
        throw new UnsupportedOperationException("HashSet auto-sorting is not yet supported.");
    }

    public Iterator<E> iterator() {
        // TODO Return an iterator that supports modification?
        return new ImmutableIterator<E>(hashSet.iterator());
    }

    public ListenerList<SetListener<E>> getSetListeners() {
        return setListeners;
    }
}
