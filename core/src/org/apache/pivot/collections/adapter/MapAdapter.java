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
package org.apache.pivot.collections.adapter;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedMap;

import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.MapListener;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;

/**
 * Implementation of the {@link Map} interface that is backed by an instance of
 * {@link java.util.Map}.
 *
 * @param <K> Type of the key objects.
 * @param <V> Type of the value objects.
 */
public class MapAdapter<K, V> implements Map<K, V>, Serializable {
    private static final long serialVersionUID = 4005649560306864969L;

    private java.util.Map<K, V> map = null;
    private transient MapListener.Listeners<K, V> mapListeners = new MapListener.Listeners<>();

    public MapAdapter(final java.util.Map<K, V> map) {
        Utils.checkNull(map, "map");

        this.map = map;
    }

    public java.util.Map<K, V> getMap() {
        return map;
    }

    @Override
    public V get(final K key) {
        return map.get(key);
    }

    @Override
    public V put(final K key, final V value) {
        boolean update = containsKey(key);
        V previousValue = map.put(key, value);

        if (update) {
            mapListeners.valueUpdated(this, key, previousValue);
        } else {
            mapListeners.valueAdded(this, key);
        }

        return previousValue;
    }

    @Override
    public V remove(final K key) {
        V value = null;

        if (containsKey(key)) {
            value = map.remove(key);
            mapListeners.valueRemoved(this, key, value);
        }

        return value;
    }

    @Override
    public void clear() {
        if (!isEmpty()) {
            map.clear();
            mapListeners.mapCleared(this);
        }
    }

    @Override
    public boolean containsKey(final K key) {
        return map.containsKey(key);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public int getCount() {
        return map.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Comparator<K> getComparator() {
        if (this.map instanceof SortedMap<?, ?>) {
            return (Comparator<K>) ((SortedMap<?, ?>) this.map).comparator();
        }
        return null;
    }

    @Override
    public void setComparator(final Comparator<K> comparator) {
        Comparator<K> previousComparator = getComparator();

        // If the adapted map supports it, construct a new sorted map
        if (this.map instanceof SortedMap<?, ?>) {
            try {
                Constructor<?> constructor = this.map.getClass().getConstructor(Comparator.class);
                if (constructor != null) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<K, V> mapLocal = (java.util.Map<K, V>) constructor.newInstance(comparator);
                    mapLocal.putAll(this.map);
                    this.map = mapLocal;
                }
            } catch (SecurityException | NoSuchMethodException | IllegalArgumentException
                   | InstantiationException | IllegalAccessException | InvocationTargetException exception) {
                throw new RuntimeException(exception);
            }
        }

        mapListeners.comparatorChanged(this, previousComparator);
    }

    @Override
    public Iterator<K> iterator() {
        return new ImmutableIterator<>(map.keySet().iterator());
    }

    @Override
    public ListenerList<MapListener<K, V>> getMapListeners() {
        return mapListeners;
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
