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
package pivot.collections.adapter;

import java.util.Comparator;
import java.util.Iterator;

import pivot.collections.Map;
import pivot.collections.MapListener;
import pivot.util.ImmutableIterator;
import pivot.util.ListenerList;

/**
 * Implementation of the {@link Map} interface that is backed by an
 * instance of <tt>java.util.Map</tt>.
 */
public class MapAdapter<K, V> implements Map<K, V> {
    private java.util.Map<K, V> map = null;
    private MapListenerList<K, V> mapListeners = new MapListenerList<K, V>();

    public MapAdapter(java.util.Map<K, V> map) {
        if (map == null) {
            throw new IllegalArgumentException("map is null.");
        }

        this.map = map;
    }

    public java.util.Map<K, V> getMap() {
        return map;
    }

    public V get(K key) {
        return map.get(key);
    }

    public V put(K key, V value) {
        boolean update = map.containsKey(key);
        V previousValue = map.put(key, value);

        if (update) {
            mapListeners.valueUpdated(this, key, previousValue);
        }
        else {
            mapListeners.valueAdded(this, key);
        }

        return previousValue;
    }

    public V remove(K key) {
        V value = null;

        if (map.containsKey(key)) {
            value = map.remove(key);
            mapListeners.valueRemoved(this, key, value);
        }

        return value;
    }

    public void clear() {
        map.clear();
        mapListeners.mapCleared(this);
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }


    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Comparator<K> getComparator() {
        return null;
    }

    public void setComparator(Comparator<K> comparator) {
        throw new UnsupportedOperationException();
    }

    public Iterator<K> iterator() {
        return new ImmutableIterator<K>(map.keySet().iterator());
    }

    public ListenerList<MapListener<K, V>> getMapListeners() {
        return mapListeners;
    }

    public String toString() {
        return map.toString();
    }
}
