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
package pivot.collections.immutable;

import java.util.Comparator;
import java.util.Iterator;

import pivot.collections.Set;
import pivot.collections.SetListener;
import pivot.util.ImmutableIterator;
import pivot.util.ListenerList;

/**
 * Unmodifiable implementation of the {@link Set} interface.
 *
 * @author gbrown
 */
public class ImmutableSet<E> implements Set<E> {
    private Set<E> set = null;

    public ImmutableSet(Set<E> set) {
        if (set == null) {
            throw new IllegalArgumentException("set is null.");
        }

        this.set = set;
    }

    public void add(E element) {
        throw new UnsupportedOperationException();
    }

    public void remove(E element) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
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

    public void setComparator(Comparator<E> comparator) {
        throw new UnsupportedOperationException();
    }

    public Iterator<E> iterator() {
        return new ImmutableIterator<E>(set.iterator());
    }

    public ListenerList<SetListener<E>> getSetListeners() {
        throw new UnsupportedOperationException();
    }
}
