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
package org.apache.pivot.collections.immutable;

import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.MapListener;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;

/**
 * Unmodifiable implementation of the {@link Map} interface.
 */
public class ImmutableMap<K, V> implements Map<K, V> {
    private Map<K, V> map = null;

    private MapListenerList<K, V> mapListeners = new MapListenerList<K, V>();

    public ImmutableMap(Map<K, V> map) {
        if (map == null) {
            throw new IllegalArgumentException("map is null.");
        }

        this.map = map;
    }

    @Override
    public V get(K key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
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
        return map.getCount();
    }

    @Override
    public Comparator<K> getComparator() {
        return null;
    }

    @Override
    public void setComparator(Comparator<K> comparator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        return new ImmutableIterator<K>(map.iterator());
    }

    @Override
    public String toString() {
        return map.toString();
    }

    @Override
    public ListenerList<MapListener<K, V>> getMapListeners() {
        return mapListeners;
    }
}
