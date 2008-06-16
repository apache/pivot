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

import java.util.Comparator;
import pivot.util.ListenerList;

/**
 * Collection interface representing a group of unique elements.
 *
 * @author gbrown
 */
public interface Set<E> extends Group<E>, Collection<E> {
    /**
     * Set listener list implementation.
     *
     * @author gbrown
     */
    public static class SetListenerList<E>
        extends ListenerList<SetListener<E>> implements SetListener<E> {
        public void elementAdded(Set<E> set, E element) {
            for (SetListener<E> listener : this) {
                listener.elementAdded(set, element);
            }
        }

        public void elementRemoved(Set<E> set, E element) {
            for (SetListener<E> listener : this) {
                listener.elementRemoved(set, element);
            }
        }

        public void setCleared(Set<E> set) {
            for (SetListener<E> listener : this) {
                listener.setCleared(set);
            }
        }

        public void comparatorChanged(Set<E> set, Comparator<E> previousComparator) {
            for (SetListener<E> listener : this) {
                listener.comparatorChanged(set, previousComparator);
            }
        }
    }

    /**
     * @see pivot.collections.SetListener#elementAdded(Set, Object)
     */
    public void add(E element);

    /**
     * @see pivot.collections.SetListener#elementRemoved(Set, Object)
     */
    public void remove(E element);

    /**
     * @see pivot.collections.SetListener#setCleared(Set)
     */
    public void clear();

    /**
     * @see pivot.collections.SetListener#setCleared(Set)
     */
    public void setComparator(Comparator<E> comparator);

    /**
     * Returns the set listener collection.
     */
    public ListenerList<SetListener<E>> getSetListeners();
}
