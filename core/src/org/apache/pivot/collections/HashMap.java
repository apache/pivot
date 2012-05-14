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
    private static final long serialVersionUID = -7079717428744528670L;

    private class KeyIterator implements Iterator<K> {
        private int bucketIndex;
        private Iterator<Pair<K, V>> entryIterator;
        private int countLocal;

        private Pair<K, V> entry = null;

        public KeyIterator() {
            bucketIndex = 0;
            entryIterator = getBucketIterator(bucketIndex);

            countLocal = HashMap.this.count;
        }

        @Override
        public boolean hasNext() {
            if (countLocal != HashMap.this.count) {
                throw new ConcurrentModificationException();
            }

            // Move to the next bucket
            while (entryIterator != null
                && !entryIterator.hasNext()) {
                entryIterator = (++bucketIndex < buckets.getLength()) ?
                    getBucketIterator(bucketIndex) : null;
            }

            return (entryIterator != null);
        }

        @Override
        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            entry = entryIterator.next();
            return entry.key;
        }

        @Override
        public void remove() {
            if (entry == null
                || entryIterator == null) {
                throw new IllegalStateException();
            }

            entryIterator.remove();
            countLocal--;
            HashMap.this.count--;

            if (mapListeners != null) {
                mapListeners.valueRemoved(HashMap.this, entry.key, entry.value);
            }

            entry = null;
        }

        private Iterator<Pair<K, V>> getBucketIterator(int bucketIndexArgument) {
            LinkedList<Pair<K, V>> bucket = buckets.get(bucketIndexArgument);

            return (bucket == null) ? new EmptyIterator<Pair<K,V>>() : bucket.iterator();
        }
    }

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
        this(Math.max((int)(entries.length / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_CAPACITY));

        for (int i = 0; i < entries.length; i++) {
            Pair<K, V> entry = entries[i];
            put(entry.key, entry.value);
        }
    }

    public HashMap(Map<K, V> map) {
        this(Math.max((int)(map.getCount() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_CAPACITY));

        for (K key : map) {
            put(key, map.get(key));
        }
    }

    public HashMap(Comparator<K> comparator) {
        this();

        setComparator(comparator);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException
     * If {@code key} is {@literal null}.
     */
    @Override
    public V get(K key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null.");
        }

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

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException
     * If {@code key} is {@literal null}.
     */
    @Override
    public V put(K key, V value) {
        return put(key, value, true);
    }

    private V put(K key, V value, boolean notifyListeners) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null.");
        }

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

                if (mapListeners != null
                    && notifyListeners) {
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
            if (count > (int)(capacity * loadFactor)) {
                rehash(capacity * 2);
            }

            if (mapListeners != null
                && notifyListeners) {
                mapListeners.valueAdded(this, key);
            }
        }

        return previousValue;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException
     * If {@code key} is {@literal null}.
     */
    @Override
    public V remove(K key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null.");
        }

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

    @Override
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

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException
     * If {@code key} is {@literal null}.
     */
    @Override
    public boolean containsKey(K key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null.");
        }

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

    @Override
    public boolean isEmpty() {
        return (count == 0);
    }

    @Override
    public int getCount() {
        return count;
    }

    public int getCapacity() {
        return buckets.getLength();
    }

    private void rehash(int capacity) {
        ArrayList<LinkedList<Pair<K, V>>> previousBuckets = this.buckets;
        buckets = new ArrayList<LinkedList<Pair<K, V>>>(capacity);

        for (int i = 0; i < capacity; i++) {
            buckets.add(null);
        }

        if (previousBuckets != null) {
            count = 0;

            if (keys != null) {
                keys.clear();
            }

            for (LinkedList<Pair<K, V>> bucket : previousBuckets) {
                if (bucket != null) {
                    for (Pair<K, V> entry : bucket) {
                        put(entry.key, entry.value, false);
                    }
                }
            }
        }
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

    @Override
    public Comparator<K> getComparator() {
        return (keys == null) ? null : keys.getComparator();
    }

    @Override
    public void setComparator(Comparator<K> comparator) {
        Comparator<K> previousComparator = getComparator();

        if (comparator == null) {
            keys = null;
        } else {
            if (keys == null) {
                // Populate key list
                ArrayList<K> keysLocal = new ArrayList<K>((int)(getCapacity() * loadFactor));
                for (K key : this) {
                    keysLocal.add(key);
                }

                this.keys = keysLocal;
            }

            keys.setComparator(comparator);
        }

        if (mapListeners != null) {
            mapListeners.comparatorChanged(this, previousComparator);
        }
    }

    @Override
    public Iterator<K> iterator() {
        return (keys == null) ? new KeyIterator() : new ImmutableIterator<K>(keys.iterator());
    }

    @Override
    public ListenerList<MapListener<K, V>> getMapListeners() {
        if (mapListeners == null) {
            mapListeners = new MapListenerList<K, V>();
        }

        return mapListeners;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        boolean equals = false;

        if (this == o) {
            equals = true;
        } else if (o instanceof Map<?, ?>) {
            Map<K, V> map = (Map<K, V>)o;

            if (count == map.getCount()) {
                for (K key : this) {
                    V value = get(key);

                    if (value == null) {
                        equals = (map.containsKey(key)
                            && map.get(key) == null);
                    } else {
                        equals = value.equals(map.get(key));
                    }

                    if (!equals) {
                        break;
                    }
                }
            }
        }

        return equals;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;

        for (K key : this) {
            hashCode = 31 * hashCode + key.hashCode();
        }

        return hashCode;
    }

    @Override
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
