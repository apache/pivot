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
package pivot.collections.concurrent;

import java.util.Comparator;
import pivot.collections.Set;
import pivot.collections.SetListener;
import pivot.util.ListenerList;
import pivot.util.concurrent.SynchronizedListenerList;

/**
 * Synchronized set wrapper.
 *
 * @author gbrown
 */
public class SynchronizedSet<E> extends SynchronizedCollection<E>
    implements Set<E> {
    /**
     * Synchronized set listener list implementation. Proxies events fired
     * by inner set to listeners of synchronized set.
     *
     * @author gbrown
     */
    private class SynchronizedSetListenerList
        extends SynchronizedListenerList<SetListener<E>>
        implements SetListener<E> {
        public void elementAdded(Set<E> set, E element) {
            for (SetListener<E> listener : this) {
                listener.elementAdded(set, element);
            }
        }

        public void elementRemoved(Set<E> set, E element) {
            for (SetListener<E> listener : this) {
                listener.elementRemoved(SynchronizedSet.this, element);
            }
        }

        public void setCleared(Set<E> set) {
            for (SetListener<E> listener : this) {
                listener.setCleared(SynchronizedSet.this);
            }
        }

        public void comparatorChanged(Set<E> set, Comparator<E> previousComparator) {
            for (SetListener<E> listener : this) {
                listener.comparatorChanged(SynchronizedSet.this, previousComparator);
            }
        }
    }

    private SynchronizedSetListenerList setListeners = new SynchronizedSetListenerList();

    public SynchronizedSet(Set<E> set) {
        super(set);

        set.getSetListeners().add(setListeners);
    }

    public synchronized void add(E element) {
        ((Set<E>)collection).add(element);
    }

    public synchronized void remove(E element) {
        ((Set<E>)collection).remove(element);
    }

    public synchronized void clear() {
        ((Set<E>)collection).clear();
    }

    public synchronized boolean contains(E element) {
        return ((Set<E>)collection).contains(element);
    }

    public synchronized boolean isEmpty() {
        return ((Set<E>)collection).isEmpty();
    }

    public ListenerList<SetListener<E>> getSetListeners() {
        return setListeners;
    }
}
