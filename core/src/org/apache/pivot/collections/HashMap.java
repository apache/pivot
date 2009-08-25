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

import org.apache.pivot.util.EmptyIterator;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;

/**
 * Implementation of the {@link Map} interface that is backed by a
 * hash table.
 */
public class HashMap<K, V> implements Map<K, V>, Serializable {
    private class KeyIterator implements Iterator<K> {
        private int bucketIndex;
        private Iterator<Pair<K, V>> entryIterator;
        private int count;

        public KeyIterator() {
            bucketIndex = 0;
            entryIterator = getBucketIterator(bucketIndex);

            count = HashMap.this.count;
        }

        public boolean hasNext() {
            // Move to the next bucket
            while (entryIterator != null
                && !entryIterator.hasNext()) {
                entryIterator = (bucketIndex < buckets.getLength()) ?
                    getBucketIterator(bucketIndex++) : null;
            }

            return (entryIterator != null);
        }

        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            if (count != HashMap.this.count) {
                throw new ConcurrentModificationException();
            }

            Pair<K, V> entry = entryIterator.next();

            return entry.key;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private Iterator<Pair<K, V>> getBucketIterator(int bucketIndex) {
            LinkedList<Pair<K, V>> bucket = buckets.get(bucketIndex);

            return (bucket == null) ? new EmptyIterator<Pair<K,V>>() : bucket.iterator();
        }
    }

    private static final long serialVersionUID = 0;

    private ArrayList<LinkedList<Pair<K, V>>> buckets;
    private float loadFactor;

    private int count = 0;
    private ArrayList<K> keys = null;

    private transient MapListenerList<K, V> mapListeners = null;

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

    public HashMap(Pair<K, V>... entries) {
        this(Math.max((int)((float)entries.length / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_CAPACITY));

        for (int i = 0; i < entries.length; i++) {
            Pair<K, V> entry = entries[i];
            put(entry.key, entry.value);
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

        // Locate the entry
        LinkedList<Pair<K, V>> bucket = getBucket(key);

        List.ItemIterator<Pair<K, V>> iterator = bucket.iterator();
        while (iterator.hasNext()) {
            Pair<K, V> entry = iterator.next();

            if (entry.key.equals(key)) {
                value = entry.value;
                break;
            }
        }

        return value;
    }

    public V put(K key, V value) {
        V previousValue = null;

        // Locate the entry
        LinkedList<Pair<K, V>> bucket = getBucket(key);

        int i = 0;
        List.ItemIterator<Pair<K, V>> iterator = bucket.iterator();
        while (iterator.hasNext()) {
            Pair<K, V> entry = iterator.next();

            if (entry.key.equals(key)) {
                // Update the entry
                previousValue = entry.value;
                iterator.update(new Pair<K, V>(key, value));

                if (mapListeners != null) {
                    mapListeners.valueUpdated(this, key, previousValue);
                }

                break;
            }

            i++;
        }

        if (i == bucket.getLength()) {
            // Add the entry
            bucket.add(new Pair<K, V>(key, value));

            if (keys != null) {
                keys.add(key);
            }

            // Increment the count
            count++;

            int capacity = getCapacity();
            if (count > (int)((float)capacity * loadFactor)) {
                rehash(capacity * 2);
            }

            if (mapListeners != null) {
                mapListeners.valueAdded(this, key);
            }
        }

        return previousValue;
    }

    public V remove(K key) {
        V value = null;

        // Locate the entry
        LinkedList<Pair<K, V>> bucket = getBucket(key);

        List.ItemIterator<Pair<K, V>> iterator = bucket.iterator();
        while (iterator.hasNext()) {
            Pair<K, V> entry = iterator.next();

            if (entry.key.equals(key)) {
                // Remove the entry
                value = entry.value;
                iterator.remove();

                if (keys != null) {
                    keys.remove(key);
                }

                // Decrement the count
                count--;

                if (mapListeners != null) {
                    mapListeners.valueRemoved(this, key, value);
                }

                break;
            }
        }

        return value;
    }

    public void clear() {
        if (count > 0) {
            // Remove all entries
            for (LinkedList<Pair<K, V>> bucket : buckets) {
                if (bucket != null) {
                    bucket.clear();
                }
            }

            if (keys != null) {
                keys.clear();
            }

            // Clear the count
            count = 0;

            if (mapListeners != null) {
                mapListeners.mapCleared(this);
            }
        }
    }

    public boolean containsKey(K key) {
        // Locate the entry
        LinkedList<Pair<K, V>> bucket = getBucket(key);

        int i = 0;
        List.ItemIterator<Pair<K, V>> iterator = bucket.iterator();
        while (iterator.hasNext()) {
            Pair<K, V> entry = iterator.next();

            if (entry.key.equals(key)) {
                break;
            }

            i++;
        }

        return (i < bucket.getLength());
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

    private static int rehashCount = 0;
    private static long rehashTime = 0;

    private void rehash(int capacity) {
        ArrayList<LinkedList<Pair<K, V>>> previousBuckets = this.buckets;
        buckets = new ArrayList<LinkedList<Pair<K, V>>>(capacity);

        for (int i = 0; i < capacity; i++) {
            buckets.add(null);
        }

        long t0 = System.currentTimeMillis();

        if (previousBuckets != null) {
            for (LinkedList<Pair<K, V>> bucket : previousBuckets) {
                if (bucket != null) {
                    for (Pair<K, V> entry : bucket) {
                        put(entry.key, entry.value);
                    }
                }
            }
        }

        long t1 = System.currentTimeMillis();

        rehashTime += (t1 - t0);
        rehashCount++;
    }

    public static void clearRehashTime() {
        rehashCount = 0;
        rehashTime = 0;
    }

    public static void dumpRehashTime() {
        System.out.println("Rehash time/count: " + rehashTime + "ms/" + rehashCount);
    }

    private LinkedList<Pair<K, V>> getBucket(K key) {
        int hashCode = key.hashCode();
        int bucketIndex = Math.abs(hashCode % getCapacity());

        LinkedList<Pair<K, V>> bucket = buckets.get(bucketIndex);
        if (bucket == null) {
            bucket = new LinkedList<Pair<K, V>>();
            buckets.update(bucketIndex, bucket);
        }

        return bucket;
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

            if (mapListeners != null) {
                mapListeners.comparatorChanged(this, previousComparator);
            }
        }
    }

    public Iterator<K> iterator() {
        return (keys == null) ? new KeyIterator() : new ImmutableIterator<K>(keys.iterator());
    }

    public ListenerList<MapListener<K, V>> getMapListeners() {
        if (mapListeners == null) {
            mapListeners = new MapListenerList<K, V>();
        }

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
