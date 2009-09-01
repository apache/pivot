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

import java.util.Comparator;

import org.apache.pivot.util.ListenerList;

/**
 * Collection interface representing a group of unique elements.
 */
public interface Set<E> extends Group<E>, Collection<E> {
    /**
     * Set listener list.
     */
    public static class SetListenerList<E>
        extends ListenerList<SetListener<E>> implements SetListener<E> {
        @Override
        public void elementAdded(Set<E> set, E element) {
            for (SetListener<E> listener : this) {
                listener.elementAdded(set, element);
            }
        }

        @Override
        public void elementRemoved(Set<E> set, E element) {
            for (SetListener<E> listener : this) {
                listener.elementRemoved(set, element);
            }
        }

        @Override
        public void setCleared(Set<E> set) {
            for (SetListener<E> listener : this) {
                listener.setCleared(set);
            }
        }

        @Override
        public void comparatorChanged(Set<E> set, Comparator<E> previousComparator) {
            for (SetListener<E> listener : this) {
                listener.comparatorChanged(set, previousComparator);
            }
        }
    }

    /**
     * @see SetListener#elementAdded(Set, Object)
     */
    @Override
    public boolean add(E element);

    /**
     * @see SetListener#elementRemoved(Set, Object)
     */
    @Override
    public boolean remove(E element);

    /**
     * @see SetListener#setCleared(Set)
     */
    @Override
    public void clear();

    /**
     * Returns the number of elements in the set.
     */
    public int getCount();

    /**
     * @see SetListener#setCleared(Set)
     */
    @Override
    public void setComparator(Comparator<E> comparator);

    /**
     * Returns the set listener collection.
     */
    public ListenerList<SetListener<E>> getSetListeners();
}
