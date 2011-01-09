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
import java.util.Set;

/**
 * Map that maintains a reference-counted list of entries. When values are
 * retrieved from the map, the reference count is incremented. When values
 * are removed, the reference count is decremented. The value is removed
 * from the map when the reference count reaches zero.
 * <p>
 * Internally, the map is backed by an instance of {@link HashMap}.
 *
 * @param <K>
 * @param <V>
 */
public class ReferenceCountedMap<K, V> extends AbstractMap<K, V> {
    /**
     * Internal reference counter.
     *
     * @param <V>
     */
    private static class ReferenceCounter<V> {
        public V value;
        public int referenceCount = 0;

        public ReferenceCounter(V value) {
            this.value = value;
        }
    }

    /**
     * Reference-counted entry set.
     */
    private class ReferenceCountedMapEntrySet extends AbstractSet<Entry<K, V>> {
        @Override
        public int size() {
            return map.size();
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new ReferenceCountedEntrySetIterator(map.entrySet().iterator());
        }
    }

    /**
     * Reference-counted entry set iterator.
     */
    private class ReferenceCountedEntrySetIterator implements Iterator<Entry<K, V>> {
        private Iterator<Entry<K, ReferenceCounter<V>>> iterator;
        private ReferenceCountedMapEntry entry = null;

        public ReferenceCountedEntrySetIterator(Iterator<Entry<K, ReferenceCounter<V>>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            entry = new ReferenceCountedMapEntry(iterator.next());
            return entry;
        }

        @Override
        public void remove() {
            if (entry == null) {
                throw new IllegalStateException();
            }

            iterator.remove();

            entry = null;
        }
    }

    /**
     * Reference-counted entry.
     */
    private class ReferenceCountedMapEntry implements Entry<K, V> {
        private Entry<K, ReferenceCounter<V>> entry;

        public ReferenceCountedMapEntry(Entry<K, ReferenceCounter<V>> entry) {
            this.entry = entry;
        }

        @Override
        public K getKey() {
            return entry.getKey();
        }

        @Override
        public V getValue() {
            ReferenceCounter<V> referenceCounter = entry.getValue();
            referenceCounter.referenceCount++;

            return referenceCounter.value;
        }

        @Override
        public V setValue(V value) {
            ReferenceCounter<V> referenceCounter = entry.getValue();
            referenceCounter.referenceCount = 0;

            V previousValue = referenceCounter.value;
            referenceCounter.value = value;

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

    private HashMap<K, ReferenceCounter<V>> map = new HashMap<K, ReferenceCounter<V>>();
    private ReferenceCountedMapEntrySet entrySet = new ReferenceCountedMapEntrySet();

    @Override
    public V get(Object key) {
        // Return the value and increment the reference count
        ReferenceCounter<V> referenceCounter = map.get(key);

        V value;
        if (referenceCounter != null) {
            referenceCounter.referenceCount++;
            value = referenceCounter.value;
        } else {
            value = null;
        }

        return value;
    }

    @Override
    public V put(K key, V value) {
        // Add a new reference counter with no initial references
        ReferenceCounter<V> previousReferenceCounter = map.put(key, new ReferenceCounter<V>(value));

        V previousValue;
        if (previousReferenceCounter != null) {
            previousValue = previousReferenceCounter.value;
        } else {
            previousValue = null;
        }

        return previousValue;
    }

    @Override
    public V remove(Object key) {
        // Decremement the reference count, or remove the entry if no
        // references remain; only return a value when the entry has
        // actually been removed
        ReferenceCounter<V> referenceCounter = map.get(key);

        V value;
        if (referenceCounter != null) {
            if (referenceCounter.referenceCount == 0) {
                map.remove(key);
                value = referenceCounter.value;
            } else {
                referenceCounter.referenceCount--;
                value = null;
            }
        } else {
            value = null;
        }

        return value;
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    /**
     * Returns the reference count for an entry.
     *
     * @param key
     *
     * @return
     * The current reference count for the given entry, or <tt>0</tt> if the
     * entry does not exist in the map. {@link #containsKey(Object)} can be
     * used to distinguish between these two cases.
     */
    public int countOf(K key) {
        ReferenceCounter<V> referenceCounter = map.get(key);

        int count;
        if (referenceCounter != null) {
            count = referenceCounter.referenceCount;
        } else {
            count = 0;
        }

        return count;
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
}
