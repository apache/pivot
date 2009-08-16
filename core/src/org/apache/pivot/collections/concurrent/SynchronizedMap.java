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
package org.apache.pivot.collections.concurrent;

import java.util.Comparator;

import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.MapListener;
import org.apache.pivot.util.ListenerList;


/**
 * Synchronized implementation of the {@link Map} interface.
 */
public class SynchronizedMap<K, V> extends SynchronizedCollection<K>
    implements Map<K, V> {
    private static class SynchronizedMapListenerList<K, V>
        extends MapListenerList<K, V> {
        @Override
        public synchronized void add(MapListener<K, V> listener) {
            super.add(listener);
        }

        @Override
        public synchronized void remove(MapListener<K, V> listener) {
            super.remove(listener);
        }

        @Override
        public synchronized void valueAdded(Map<K, V> map, K key) {
            super.valueAdded(map, key);
        }

        @Override
        public synchronized void valueRemoved(Map<K, V> map, K key, V value) {
            super.valueRemoved(map, key, value);
        }

        @Override
        public synchronized void valueUpdated(Map<K, V> map, K key, V previousValue) {
            super.valueUpdated(map, key, previousValue);
        }

        @Override
        public synchronized void mapCleared(Map<K, V> map) {
            super.mapCleared(map);
        }

        @Override
        public synchronized void comparatorChanged(Map<K, V> map, Comparator<K> previousComparator) {
            super.comparatorChanged(map, previousComparator);
        }
    }

    private SynchronizedMapListenerList<K, V> mapListeners = new SynchronizedMapListenerList<K, V>();

    public SynchronizedMap(Map<K, V> map) {
        super(map);
    }

    @SuppressWarnings("unchecked")
    public synchronized V get(K key) {
        return ((Map<K, V>)collection).get(key);
    }

    @SuppressWarnings("unchecked")
    public synchronized V put(K key, V value) {
        boolean update = containsKey(key);
        V previousValue = ((Map<K, V>)collection).put(key, value);

        if (update) {
            mapListeners.valueUpdated(this, key, previousValue);
        }
        else {
            mapListeners.valueAdded(this, key);
        }

        return previousValue;
    }

    @SuppressWarnings("unchecked")
    public synchronized V remove(K key) {
        V value = null;

        if (containsKey(key)) {
            value = ((Map<K, V>)collection).remove(key);
            mapListeners.valueRemoved(this, key, value);
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean isEmpty() {
        return ((Map<K, V>)collection).isEmpty();
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean containsKey(K key) {
        return ((Map<K, V>)collection).containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public synchronized int count() {
        return ((Map<K, V>)collection).count();
    }

    public ListenerList<MapListener<K, V>> getMapListeners() {
        return mapListeners;
    }
}
