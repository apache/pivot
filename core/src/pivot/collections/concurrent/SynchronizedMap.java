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
import pivot.collections.Map;
import pivot.collections.MapListener;
import pivot.util.ListenerList;
import pivot.util.concurrent.SynchronizedListenerList;

/**
 * Synchronized map wrapper.
 *
 * @author gbrown
 */
public class SynchronizedMap<K, V> extends SynchronizedCollection<K>
    implements Map<K, V> {
    /**
     * Synchronized map listener list implementation. Proxies events fired
     * by inner map to listeners of synchronized map.
     *
     * @author gbrown
     */
    private class SynchronizedMapListenerList
        extends SynchronizedListenerList<MapListener<K, V>>
        implements MapListener<K, V> {
        public synchronized void valueAdded(Map<K, V> map, K key) {
            for (MapListener<K, V> listener : this) {
                listener.valueAdded(SynchronizedMap.this, key);
            }
        }

        public synchronized void valueRemoved(Map<K, V> map, K key, V value) {
            for (MapListener<K, V> listener : this) {
                listener.valueRemoved(SynchronizedMap.this, key, value);
            }
        }

        public synchronized void valueUpdated(Map<K, V> map, K key, V previousValue) {
            for (MapListener<K, V> listener : this) {
                listener.valueUpdated(SynchronizedMap.this, key, previousValue);
            }
        }

        public synchronized void mapCleared(Map<K, V> map) {
            for (MapListener<K, V> listener : this) {
                listener.mapCleared(SynchronizedMap.this);
            }
        }

        public synchronized void comparatorChanged(Map<K, V> map, Comparator<K> previousComparator) {
            for (MapListener<K, V> listener : this) {
                listener.comparatorChanged(SynchronizedMap.this, previousComparator);
            }
        }
    }

    private SynchronizedMapListenerList mapListeners = new SynchronizedMapListenerList();

    public SynchronizedMap(Map<K, V> map) {
        super(map);

        map.getMapListeners().add(mapListeners);
    }

    @SuppressWarnings("unchecked")
    public synchronized V get(K key) {
        return ((Map<K, V>)collection).get(key);
    }

    @SuppressWarnings("unchecked")
    public synchronized V put(K key, V value) {
        return ((Map<K, V>)collection).put(key, value);
    }

    @SuppressWarnings("unchecked")
    public synchronized V remove(K key) {
        return ((Map<K, V>)collection).remove(key);
    }

    @SuppressWarnings("unchecked")
    public synchronized void clear() {
        ((Map<K, V>)collection).clear();
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean isEmpty() {
        return ((Map<K, V>)collection).isEmpty();
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean containsKey(K key) {
        return ((Map<K, V>)collection).containsKey(key);
    }

    public ListenerList<MapListener<K, V>> getMapListeners() {
        return mapListeners;
    }
}
