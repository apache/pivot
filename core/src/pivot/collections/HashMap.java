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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;

import pivot.util.ImmutableIterator;
import pivot.util.ListenerList;

/**
 * Implementation of the {@link Map} interface that is backed by a
 * hashtable.
 * <p>
 * TODO We're temporarily using a java.util.HashMap to back this map.
 * Eventually, we'll replace this with an internal map representation.
 */
public class HashMap<K, V> implements Map<K, V>, Serializable {
    private static final long serialVersionUID = 0;

    protected java.util.Map<K, V> hashMap = null;

    private Comparator<K> comparator = null;
    private transient MapListenerList<K, V> mapListeners = new MapListenerList<K, V>();

    public HashMap() {
        this(false, null);
    }

    public HashMap(boolean weak) {
        this(weak, null);
    }

    public HashMap(Map<K, V> map) {
        this(false, map);
    }

    public HashMap(boolean weak, Map<K, V> map) {
        hashMap = (weak) ? new java.util.WeakHashMap<K, V>() : new java.util.HashMap<K, V>();

        if (map != null) {
            for (K key : map) {
                put(key, map.get(key));
            }
        }
    }

    public HashMap(Comparator<K> comparator) {
        // TODO
        throw new UnsupportedOperationException("HashMap auto-sorting is not yet supported.");

        // this.comparator = comparator;
    }

    public V get(K key) {
        return hashMap.get(key);
    }

    public V put(K key, V value) {
        boolean update = hashMap.containsKey(key);
        V previousValue = hashMap.put(key, value);

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

        if (hashMap.containsKey(key)) {
            value = hashMap.remove(key);
            mapListeners.valueRemoved(this, key, value);
        }

        return value;
    }

    public void clear() {
        hashMap.clear();
        mapListeners.mapCleared(this);
    }

    public boolean containsKey(K key) {
        return hashMap.containsKey(key);
    }

    public boolean isEmpty() {
        return hashMap.isEmpty();
    }

    public Comparator<K> getComparator() {
        return comparator;
    }

    public void setComparator(Comparator<K> comparator) {
        // TODO
        throw new UnsupportedOperationException("HashMap auto-sorting is not yet supported.");
    }

    public Iterator<K> iterator() {
        // TODO Return an iterator that supports modification?
        return new ImmutableIterator<K>(hashMap.keySet().iterator());
    }

    public ListenerList<MapListener<K, V>> getMapListeners() {
        return mapListeners;
    }

    public void setMapListener(MapListener<K, V> listener) {
        mapListeners.add(listener);
    }

    public String toString() {
        return hashMap.toString();
    }
}
