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
import java.util.Iterator;

import org.apache.pivot.collections.Map.Pair;
import org.apache.pivot.util.ListenerList;

/**
 * Decorates a {@link Map} to look like a {@link List} of key/value pairs. This
 * facilitates the use of a <tt>Map</tt> as table data in a
 * {@link org.apache.pivot.wtk.TableView}.
 *
 * @author tvolkert
 */
public class MapList<K, V> implements List<Pair<K, V>> {
    private Map<K, V> map;
    private ArrayList<Pair<K, V>> pairs = new ArrayList<Pair<K, V>>();

    private boolean updating = false;

    private ListListenerList<Pair<K, V>> listListeners = new ListListenerList<Pair<K, V>>();

    /**
     * Creates a new map list that decorates the specified map.
     *
     * @param map
     * The map to present as a list
     */
    public MapList(Map<K, V> map) {
        this.map = map;

        for (K key : map) {
            pairs.add(new Pair<K, V>(key, map.get(key)));
        }

        map.getMapListeners().add(new MapListener.Adapter<K, V>() {
            @Override
            public void valueAdded(Map<K, V> map, K key) {
                if (!updating) {
                    int index = pairs.add(new Pair<K, V>(key, map.get(key)));
                    listListeners.itemInserted(MapList.this, index);
                }
            }

            @Override
            public void valueUpdated(Map<K, V> map, K key, V previousValue) {
                if (!updating) {
                    Pair<K, V> pair = new Pair<K, V>(key, map.get(key));
                    Pair<K, V> previousPair = new Pair<K, V>(key, previousValue);

                    // Bypass the pairs comparator to find exact matches only
                    int index = linearSearch(pair);

                    if (index >= 0) {
                        // We disallow duplicate keys in the list, so this means
                        // that the value logically equals the previous value
                        pairs.update(index, pair);
                        listListeners.itemUpdated(MapList.this, index, previousPair);
                    } else {
                        int previousIndex = linearSearch(previousPair);
                        assert (previousIndex >= 0);

                        Sequence<Pair<K, V>> removed = pairs.remove(previousIndex, 1);
                        listListeners.itemsRemoved(MapList.this, previousIndex, removed);

                        index = pairs.add(pair);
                        listListeners.itemInserted(MapList.this, index);
                    }
                }
            }

            @Override
            public void valueRemoved(Map<K, V> map, K key, V value) {
                if (!updating) {
                    Pair<K, V> pair = new Pair<K, V>(key, value);

                    // Bypass the pairs comparator to find exact matches only
                    int index = linearSearch(pair);

                    Sequence<Pair<K, V>> removed = pairs.remove(index, 1);
                    listListeners.itemsRemoved(MapList.this, index, removed);
                }
            }

            @Override
            public void mapCleared(Map<K, V> map) {
                if (!updating) {
                    pairs.clear();
                    listListeners.listCleared(MapList.this);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int add(Pair<K, V> pair) {
        if (pair == null) {
            throw new IllegalArgumentException("Pair is null.");
        }

        if (map.containsKey(pair.key)) {
            throw new IllegalArgumentException("Duplicate keys not allowed.");
        }

        // Update the list
        int index = pairs.add(pair);

        // Update the map
        updating = true;
        try {
            map.put(pair.key, pair.value);
        } finally {
            updating = false;
        }

        // Notify listeners
        listListeners.itemInserted(this, index);

        return index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert(Pair<K, V> pair, int index) {
        if (pair == null) {
            throw new IllegalArgumentException("Pair is null.");
        }

        if (map.containsKey(pair.key)) {
            throw new IllegalArgumentException("Duplicate keys not allowed.");
        }

        // Update the list
        pairs.insert(pair, index);

        // Update the map
        updating = true;
        try {
            map.put(pair.key, pair.value);
        } finally {
            updating = false;
        }

        // Notify listeners
        listListeners.itemInserted(this, index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<K, V> update(int index, Pair<K, V> pair) {
        if (pair == null) {
            throw new IllegalArgumentException("Pair is null.");
        }

        Pair<K, V> previousPair = pairs.get(index);

        if (!pair.key.equals(previousPair.key)
            && map.containsKey(pair.key)) {
            throw new IllegalArgumentException("Duplicate keys not allowed.");
        }

        // Update the list
        pairs.update(index, pair);

        // Update the map
        updating = true;
        try {
            map.remove(previousPair.key);
            map.put(pair.key, pair.value);
        } finally {
            updating = false;
        }

        // Notify listeners
        listListeners.itemUpdated(this, index, previousPair);

        return previousPair;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int remove(Pair<K, V> pair) {
        int index = indexOf(pair);

        if (index >= 0) {
           remove(index, 1);
        }

        return index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sequence<Pair<K, V>> remove(int index, int count) {
        // Update the list
        Sequence<Pair<K, V>> removed = pairs.remove(index, count);

        // Update the map
        updating = true;
        try {
            for (int i = 0, n = removed.getLength(); i < n; i++) {
                Pair<K, V> pair = removed.get(i);
                map.remove(pair.key);
            }
        } finally {
            updating = false;
        }

        // Notify listeners
        listListeners.itemsRemoved(this, index, removed);

        return removed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<K, V> get(int index) {
        return pairs.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(Pair<K, V> pair) {
        return pairs.indexOf(pair);
    }

    /**
     * Finds the specified pair in the pairs list by searching linearly
     * and reporting an exact match only (bypasses the list's
     * comparator).
     *
     * @param pair
     * The pair to search for
     *
     * @return
     * The index of the pair in the list, or <tt>-1</tt> if not found
     */
    private int linearSearch(Pair<K, V> pair) {
        int index = -1;

        for (int i = 0, n = pairs.getLength(); i < n; i++) {
            if (pairs.get(i).equals(pair)) {
                index = i;
                break;
            }
        }

        return index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        // Update the list
        pairs.clear();

        // Update the map
        updating = true;
        try {
            map.clear();
        } finally {
            updating = false;
        }

        // Notify listeners
        listListeners.listCleared(MapList.this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLength() {
        return pairs.getLength();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Comparator<Pair<K, V>> getComparator() {
        return pairs.getComparator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setComparator(Comparator<Pair<K, V>> comparator) {
        Comparator<Pair<K, V>> previousComparator = pairs.getComparator();

        if (previousComparator != comparator) {
            pairs.setComparator(comparator);
            listListeners.comparatorChanged(this, previousComparator);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Pair<K, V>> iterator() {
        return pairs.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListenerList<ListListener<Pair<K, V>>> getListListeners() {
        return listListeners;
    }
}
