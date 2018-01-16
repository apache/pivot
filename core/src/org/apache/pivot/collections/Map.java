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
package org.apache.pivot.collections;

import java.util.Comparator;

import org.apache.pivot.util.ListenerList;

/**
 * Collection interface representing set of key/value pairs.
 */
public interface Map<K, V> extends Dictionary<K, V>, Collection<K> {
    /**
     * Sets the value of the given key, creating a new entry or replacing the
     * existing value, and firing a corresponding event.
     *
     * @param key The key whose value is to be set.
     * @param value The value to be associated with the given key.
     * @see MapListener#valueAdded(Map, Object)
     * @see MapListener#valueUpdated(Map, Object, Object)
     */
    @Override
    public V put(K key, V value);

    /**
     * @see MapListener#valueRemoved(Map, Object, Object)
     */
    @Override
    public V remove(K key);

    /**
     * Removes all entries in the map.
     *
     * @see MapListener#mapCleared(Map)
     */
    @Override
    public void clear();

    /**
     * Returns the number of entries in the map.
     * @return Current number of map entries.
     */
    public int getCount();

    /**
     * @see MapListener#comparatorChanged(Map, Comparator)
     */
    @Override
    public void setComparator(Comparator<K> comparator);

    /**
     * Returns the map listener collection.
     * @return Current list of listeners on this map.
     */
    public ListenerList<MapListener<K, V>> getMapListeners();
}
