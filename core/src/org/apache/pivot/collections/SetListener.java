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
 * Set listener interface.
 */
public interface SetListener<E> {
    /**
     * Set listeners.
     */
    public static class Listeners<E> extends ListenerList<SetListener<E>> implements
        SetListener<E> {
        @Override
        public void elementAdded(Set<E> set, E element) {
            forEach(listener -> listener.elementAdded(set, element));
        }

        @Override
        public void elementRemoved(Set<E> set, E element) {
            forEach(listener -> listener.elementRemoved(set, element));
        }

        @Override
        public void setCleared(Set<E> set) {
            forEach(listener -> listener.setCleared(set));
        }

        @Override
        public void comparatorChanged(Set<E> set, Comparator<E> previousComparator) {
            forEach(listener -> listener.comparatorChanged(set, previousComparator));
        }
    }

    /**
     * Set listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter<E> implements SetListener<E> {
        @Override
        public void elementAdded(Set<E> set, E element) {
            // empty block
        }

        @Override
        public void elementRemoved(Set<E> set, E element) {
            // empty block
        }

        @Override
        public void setCleared(Set<E> set) {
            // empty block
        }

        @Override
        public void comparatorChanged(Set<E> set, Comparator<E> previousComparator) {
            // empty block
        }
    }

    /**
     * Called when an element is added to a set.
     *
     * @param set The source of the set event.
     * @param element The element that was added to the set.
     */
    default void elementAdded(Set<E> set, E element) {
    }

    /**
     * Called when an element is removed from the set.
     *
     * @param set The source of the set event.
     * @param element The element that was removed from the set.
     */
    default void elementRemoved(Set<E> set, E element) {
    }

    /**
     * Called when set data has been reset.
     *
     * @param set The source of the set event.
     */
    default void setCleared(Set<E> set) {
    }

    /**
     * Called when a set's comparator has changed.
     *
     * @param set The source of the event.
     * @param previousComparator The previous comparator value.
     */
    default void comparatorChanged(Set<E> set, Comparator<E> previousComparator) {
    }
}
