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
package org.apache.pivot.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Observable map that is backed by an instance of {@link Map}.
 */
public class ObservableMapAdapter<K, V> extends AbstractMap<K, V>
    implements ObservableMap<K, V> {
    private class ObservableMapEntrySet extends AbstractSet<Entry<K, V>> {
        @Override
        public int size() {
            return map.size();
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new ObservableMapEntrySetIterator(map.entrySet().iterator());
        }
    }

    private class ObservableMapEntrySetIterator implements Iterator<Entry<K, V>> {
        private Iterator<Entry<K, V>> iterator;
        private ObservableMapEntry entry = null;

        public ObservableMapEntrySetIterator(Iterator<Entry<K, V>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            entry = new ObservableMapEntry(iterator.next());
            return entry;
        }

        @Override
        public void remove() {
            if (entry == null) {
                throw new IllegalStateException();
            }

            iterator.remove();

            observableMapListeners.valueRemoved(ObservableMapAdapter.this,
                entry.getKey(), entry.getValue());

            entry = null;
        }
    }

    private class ObservableMapEntry implements Entry<K, V> {
        private Entry<K, V> entry;

        public ObservableMapEntry(Entry<K, V> entry) {
            this.entry = entry;
        }

        @Override
        public K getKey() {
            return entry.getKey();
        }

        @Override
        public V getValue() {
            return entry.getValue();
        }

        @Override
        public V setValue(V value) {
            V previousValue = entry.setValue(value);

            observableMapListeners.valueUpdated(ObservableMapAdapter.this,
                getKey(), previousValue);

            return previousValue;
        }

        @Override
        public boolean equals(Object object) {
            return entry.equals(object);
        }

        @Override
        public int hashCode() {
            return entry.hashCode();
        }
    }

    private Map<K, V> map;
    private ObservableMapEntrySet entrySet = new ObservableMapEntrySet();
    private ObservableMapListenerList<K, V> observableMapListeners =
        new ObservableMapListenerList<K, V>();

    public ObservableMapAdapter(Map<K, V> map) {
        if (map == null) {
            throw new IllegalArgumentException();
        }

        this.map = map;
    }

    public Map<K, V> getMap() {
        return map;
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        boolean update = containsKey(key);
        V previousValue = map.put(key, value);

        if (update) {
            observableMapListeners.valueUpdated(this, key, previousValue);
        } else {
            observableMapListeners.valueAdded(this, key);
        }

        return previousValue;
    }

    @Override
    public V remove(Object key) {
        V value = null;

        if (containsKey(key)) {
            value = map.remove(key);
            observableMapListeners.valueRemoved(this, key, value);
        }

        return value;
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return entrySet;
    }

    @Override
    public boolean equals(Object object) {
        return map.equals(object);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public ListenerList<ObservableMapListener<K, V>> getObservableMapListeners() {
        return observableMapListeners;
    }

    public static <K, V> ObservableMapAdapter<K, V> observableHashMap() {
        return new ObservableMapAdapter<K, V>(new HashMap<K, V>());
    }
}
