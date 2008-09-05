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
package pivot.collections.adapter;

import java.util.Comparator;
import java.util.Iterator;

import pivot.collections.Set;
import pivot.collections.SetListener;
import pivot.util.ImmutableIterator;
import pivot.util.ListenerList;

/**
 * <p>Implementation of the {@link Set} interface that is backed by an
 * instance of <tt>java.util.Set</tt>.</p>
 */
public class SetAdapter<E> implements Set<E> {
    private java.util.Set<E> set = null;
    private SetListenerList<E> setListeners = new SetListenerList<E>();

    public SetAdapter(java.util.Set<E> set) {
        if (set == null) {
            throw new IllegalArgumentException("set is null.");
        }

        this.set = set;
    }

    public void add(E element) {
        if (!set.contains(element)) {
            set.add(element);
            setListeners.elementAdded(this, element);
        }
    }

    public void remove(E element) {
        if (set.contains(element)) {
            set.remove(element);
            setListeners.elementRemoved(this, element);
        }
    }

    public void clear() {
        set.clear();
        setListeners.setCleared(this);
    }

    public boolean contains(E element) {
        return set.contains(element);
    }

    public boolean isEmpty() {
        return set.isEmpty();
    }

    public Comparator<E> getComparator() {
        return null;
    }

    /**
     * NOTE This method is not supported because it cannot be efficiently
     * implemented for all set types.
     */
    public void setComparator(Comparator<E> comparator) {
        throw new UnsupportedOperationException();
    }

    public Iterator<E> iterator() {
        return new ImmutableIterator<E>(set.iterator());
    }

    public ListenerList<SetListener<E>> getSetListeners() {
        return setListeners;
    }
}
