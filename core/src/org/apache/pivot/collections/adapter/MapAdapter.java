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

/**
 * Implementation of the {@link Map} interface that is backed by an instance of
 * {@link java.util.Map}.
 */
public class MapAdapter<K, V> implements Map<K, V>, Serializable {
    private static final long serialVersionUID = 4005649560306864969L;

    private java.util.Map<K, V> map = null;
    private transient MapListenerList<K, V> mapListeners = new MapListenerList<K, V>();

    public MapAdapter(java.util.Map<K, V> map) {
        if (map == null) {
            throw new IllegalArgumentException("map is null.");
        }

        this.map = map;
    }

    public java.util.Map<K, V> getMap() {
        return map;
    }

    @Override
    public V get(K key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
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
    public V remove(K key) {
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
    public boolean containsKey(K key) {
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
            return (Comparator<K>)((SortedMap<?, ?>)this.map).comparator();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setComparator(Comparator<K> comparator) {
        Comparator<K> previousComparator = getComparator();

        // If the adapted map supports it, construct a new sorted map
        if (this.map instanceof SortedMap<?, ?>) {
            try {
                Constructor<?> constructor = this.map.getClass().getConstructor(Comparator.class);
                if (constructor != null) {
                    java.util.Map<K, V> mapLocal = (java.util.Map<K, V>)constructor.newInstance(comparator);
                    mapLocal.putAll(this.map);
                    this.map = mapLocal;
                }
            } catch (SecurityException exception) {
                throw new RuntimeException(exception);
            } catch (NoSuchMethodException exception) {
                throw new RuntimeException(exception);
            } catch (IllegalArgumentException exception) {
                throw new RuntimeException(exception);
            } catch (InstantiationException exception) {
                throw new RuntimeException(exception);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(exception);
            } catch (InvocationTargetException exception) {
                throw new RuntimeException(exception);
            }
        }

        mapListeners.comparatorChanged(this, previousComparator);
    }

    @Override
    public Iterator<K> iterator() {
        return new ImmutableIterator<K>(map.keySet().iterator());
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
