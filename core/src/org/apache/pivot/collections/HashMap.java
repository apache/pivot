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

import java.io.Serializable;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;

/**
 * Implementation of the {@link Map} interface that is backed by a
 * hash table.
 * <p>
 * TODO Optimize bucket management when a comparator is applied by using binary search to
 * sort bucket contents and locate pairs.
 */
public class HashMap<K, V> implements Map<K, V>, Serializable {
    private class KeyIterator implements Iterator<K> {
        private int bucketIndex = 0;
        private int pairIndex = 0;
        private int count;

        public KeyIterator() {
            count = HashMap.this.count;
        }

        public boolean hasNext() {
            // Locate the next pair
            while (bucketIndex < buckets.getLength()
                && pairIndex == buckets.get(bucketIndex).getLength()) {
                bucketIndex++;
                pairIndex = 0;
            }

            return (bucketIndex < buckets.getLength()
                && pairIndex < buckets.get(bucketIndex).getLength());
        }

        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            if (count != HashMap.this.count) {
                throw new ConcurrentModificationException();
            }

            // Return the current pair
            ArrayList<Pair<K, V>> bucket = buckets.get(bucketIndex);
            Pair<K, V> pair = bucket.get(pairIndex++);

            return pair.key;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static final long serialVersionUID = 0;

    private ArrayList<ArrayList<Pair<K, V>>> buckets;
    private float loadFactor;

    private int count = 0;
    private ArrayList<K> keys = null;

    private transient MapListenerList<K, V> mapListeners = new MapListenerList<K, V>();

    public static final int DEFAULT_CAPACITY = 16;
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;

    public HashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public HashMap(int capacity) {
        this(capacity, DEFAULT_LOAD_FACTOR);
    }

    public HashMap(int capacity, float loadFactor) {
        this.loadFactor = loadFactor;

        rehash(capacity);
    }

    public HashMap(Pair<K, V>... pairs) {
        this(Math.max((int)((float)pairs.length / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_CAPACITY));

        for (int i = 0; i < pairs.length; i++) {
            Pair<K, V> pair = pairs[i];
            put(pair.key, pair.value);
        }
    }

    public HashMap(Map<K, V> map) {
        this(Math.max((int)((float)map.getCount() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_CAPACITY));

        if (map != null) {
            for (K key : map) {
                put(key, map.get(key));
            }
        }
    }

    public HashMap(Comparator<K> comparator) {
        this();

        setComparator(comparator);
    }

    public V get(K key) {
        V value = null;

        // Locate the pair
        int bucketIndex = getBucketIndex(key);
        ArrayList<Pair<K, V>> bucket = buckets.get(bucketIndex);

        int n = bucket.getLength();
        int i = 0;

        while (i < n
            && !bucket.get(i).key.equals(key)) {
            i++;
        }

        if (i < n) {
            value = bucket.get(i).value;
        }

        return value;
    }

    public V put(K key, V value) {
        V previousValue = null;

        // Locate the pair
        int bucketIndex = getBucketIndex(key);
        ArrayList<Pair<K, V>> bucket = buckets.get(bucketIndex);

        int n = bucket.getLength();
        int i = 0;

        while (i < n
            && !bucket.get(i).key.equals(key)) {
            i++;
        }

        if (i < n) {
            // Update the pair
            previousValue = bucket.update(i, new Pair<K, V>(key, value)).value;
            mapListeners.valueUpdated(this, key, previousValue);
        } else {
            // Add the pair
            bucket.add(new Pair<K, V>(key, value));

            if (keys != null) {
                keys.add(key);
            }

            count++;

            int capacity = getCapacity();
            if (count > (int)((float)capacity * loadFactor)) {
                rehash(capacity * 2);
            }

            mapListeners.valueAdded(this, key);
        }

        return previousValue;
    }

    public V remove(K key) {
        V value = null;

        // Locate the pair
        int bucketIndex = getBucketIndex(key);
        ArrayList<Pair<K, V>> bucket = buckets.get(bucketIndex);

        int n = bucket.getLength();
        int i = 0;

        while (i < n
            && !bucket.get(i).key.equals(key)) {
            i++;
        }

        if (i < n) {
            // Remove the pair
            Sequence<Pair<K, V>> removed = bucket.remove(i, 1);
            value = removed.get(0).value;

            if (keys != null) {
                keys.remove(key);
            }

            count--;

            mapListeners.valueRemoved(this, key, value);
        }

        return value;
    }

    public void clear() {
        if (count > 0) {
            for (ArrayList<Pair<K, V>> bucket : buckets) {
                bucket.clear();
            }

            if (keys != null) {
                keys.clear();
            }

            count = 0;

            mapListeners.mapCleared(this);
        }
    }

    public boolean containsKey(K key) {
        // Locate the pair
        int bucketIndex = getBucketIndex(key);
        ArrayList<Pair<K, V>> bucket = buckets.get(bucketIndex);

        int n = bucket.getLength();
        int i = 0;

        while (i < n
            && !bucket.get(i).key.equals(key)) {
            i++;
        }

        return (i < n);
    }

    public boolean isEmpty() {
        return (count == 0);
    }

    public int getCount() {
        return count;
    }

    public int getCapacity() {
        return buckets.getLength();
    }

    private void rehash(int capacity) {
        ArrayList<ArrayList<Pair<K, V>>> previousBuckets = this.buckets;
        buckets = new ArrayList<ArrayList<Pair<K, V>>>(capacity);

        for (int i = 0; i < capacity; i++) {
            buckets.add(new ArrayList<Pair<K, V>>());
        }

        if (previousBuckets != null) {
            for (ArrayList<Pair<K, V>> bucket : previousBuckets) {
                for (Pair<K, V> pair : bucket) {
                    put(pair.key, pair.value);
                }
            }
        }
    }

    private int getBucketIndex(K key) {
        int hashCode = key.hashCode();
        return Math.abs(hashCode % getCapacity());
    }

    public Comparator<K> getComparator() {
        return (keys == null) ? null : keys.getComparator();
    }

    public void setComparator(Comparator<K> comparator) {
        Comparator<K> previousComparator = getComparator();

        if (comparator != previousComparator) {
            if (comparator == null) {
                keys = null;
            } else {
                if (keys == null) {
                    // Populate key list
                    ArrayList<K> keys = new ArrayList<K>((int)((float)getCapacity() * loadFactor));
                    for (K key : this) {
                        keys.add(key);
                    }

                    this.keys = keys;
                }

                keys.setComparator(comparator);
            }

            mapListeners.comparatorChanged(this, previousComparator);
        }
    }

    public Iterator<K> iterator() {
        return (keys == null) ? new KeyIterator() : new ImmutableIterator<K>(keys.iterator());
    }

    public ListenerList<MapListener<K, V>> getMapListeners() {
        return mapListeners;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getClass().getName());
        sb.append(" {");

        int i = 0;
        for (K key : this) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(key + ":" + get(key));
            i++;
        }

        sb.append("}");

        return sb.toString();
    }
}
