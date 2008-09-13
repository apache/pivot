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

import java.util.Comparator;

/**
 * <p>Map listener interface.</p>
 *
 * @author gbrown
 */
public interface MapListener<K, V> {
    /**
     * Called when a key/value pair has been added to a map.
     *
     * @param map
     * The source of the map event.
     *
     * @param key
     * The key that was added to the map.
     */
    public void valueAdded(Map<K, V> map, K key);

    /**
     * Called when a map value has been updated.
     *
     * @param map
     * The source of the map event.
     *
     * @param key
     * The key whose value was updated.
     *
     * @param previousValue
     * The value that was previously associated with the key.
     */
    public void valueUpdated(Map<K, V> map, K key, V previousValue);

    /**
     * Called when a key/value pair has been removed from a map.
     *
     * @param map
     * The source of the map event.
     *
     * @param key
     * The key that was removed.
     *
     * @param value
     * The value that was removed.
     */
    public void valueRemoved(Map<K, V> map, K key, V value);

    /**
     * Called when map data has been reset.
     *
     * @param map
     * The source of the map event.
     */
    public void mapCleared(Map<K, V> map);

    /**
     * Called when a map's comparator has changed.
     *
     * @param map
     * The source of the event.
     *
     * @param previousComparator
     * The previous comparator value.
     */
    public void comparatorChanged(Map<K, V> map, Comparator<K> previousComparator);
}

