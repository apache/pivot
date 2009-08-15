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
 */
public class MapList<K, V> implements List<Pair<K, V>> {
    /**
     * Map list listener list.
     * 
     */
    private static class MapListListenerList<K, V> extends ListenerList<MapListListener<K, V>>
        implements MapListListener<K, V> {
        public void sourceChanged(MapList<K, V> mapList, Map<K, V> previousSource) {
            for (MapListListener<K, V> listener : this) {
                listener.sourceChanged(mapList, previousSource);
            }
        }
    }

    private Map<K, V> source;

    private ArrayList<Pair<K, V>> view = null;

    // this flag is used to prevent recursion if the source is updated
    // externally
    private boolean updating = false;

    private ListListenerList<Pair<K, V>> listListeners = new ListListenerList<Pair<K, V>>();
    private MapListListenerList<K, V> mapListListeners = new MapListListenerList<K, V>();

    private MapListener<K, V> mapHandler = new MapListener.Adapter<K, V>() {
        @Override
        public void valueAdded(Map<K, V> map, K key) {
            if (!updating) {
                int index = view.add(new Pair<K, V>(key, map.get(key)));
                listListeners.itemInserted(MapList.this, index);
            }
        }

        @Override
        public void valueUpdated(Map<K, V> map, K key, V previousValue) {
            if (!updating) {
                Pair<K, V> pair = new Pair<K, V>(key, map.get(key));
                Pair<K, V> previousPair = new Pair<K, V>(key, previousValue);

                // Bypass the view comparator to find exact matches only
                int index = linearSearch(pair);

                if (index >= 0) {
                    // We disallow duplicate keys in the list, so this means
                    // that the value logically equals the previous value
                    view.update(index, pair);
                    listListeners.itemUpdated(MapList.this, index, previousPair);
                } else {
                    int previousIndex = linearSearch(previousPair);
                    assert (previousIndex >= 0);

                    Sequence<Pair<K, V>> removed = view.remove(previousIndex, 1);
                    listListeners.itemsRemoved(MapList.this, previousIndex, removed);

                    index = view.add(pair);
                    listListeners.itemInserted(MapList.this, index);
                }
            }
        }

        @Override
        public void valueRemoved(Map<K, V> map, K key, V value) {
            if (!updating) {
                Pair<K, V> pair = new Pair<K, V>(key, value);

                // Bypass the view comparator to find exact matches only
                int index = linearSearch(pair);

                Sequence<Pair<K, V>> removed = view.remove(index, 1);
                listListeners.itemsRemoved(MapList.this, index, removed);
            }
        }

        @Override
        public void mapCleared(Map<K, V> map) {
            if (!updating) {
                view.clear();
                listListeners.listCleared(MapList.this);
            }
        }
    };

    /**
     * Creates a new map list with no source map.
     */
    public MapList() {
        this(null);
    }

    /**
     * Creates a new map list that decorates the specified source map.
     * 
     * @param source
     *            The map to present as a list
     */
    public MapList(Map<K, V> source) {
        setSource(source);
    }

    /**
     * Gets the source map.
     * 
     * @return The source map, or <tt>null</tt> if no source is set
     */
    public Map<K, V> getSource() {
        return source;
    }

    /**
     * Sets the source map.
     * 
     * @param source
     *            The source map, or <tt>null</tt> to clear the source
     */
    public void setSource(Map<K, V> source) {
        if (source == null) {
            source = new HashMap<K, V>();
        }

        Map<K, V> previousSource = this.source;

        if (previousSource != source) {
            // Clear any existing view
            if (view != null) {
                view.clear();
                listListeners.listCleared(this);
            }

            // Attach/detach list listeners
            if (previousSource != null) {
                previousSource.getMapListeners().remove(mapHandler);
            }

            if (source != null) {
                source.getMapListeners().add(mapHandler);
            }

            // Update source
            this.source = source;
            mapListListeners.sourceChanged(this, previousSource);

            // Refresh the view
            view = new ArrayList<Pair<K, V>>(source.count());

            for (K key : source) {
                listListeners.itemInserted(this, view.add(new Pair<K, V>(key, source.get(key))));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int add(Pair<K, V> pair) {
        if (pair == null) {
            throw new IllegalArgumentException("Pair is null.");
        }

        if (source.containsKey(pair.key)) {
            throw new IllegalArgumentException("Duplicate keys not allowed.");
        }

        // Update the list
        int index = view.add(pair);

        // Update the source
        updating = true;
        try {
            source.put(pair.key, pair.value);
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

        if (source.containsKey(pair.key)) {
            throw new IllegalArgumentException("Duplicate keys not allowed.");
        }

        // Update the list
        view.insert(pair, index);

        // Update the source
        updating = true;
        try {
            source.put(pair.key, pair.value);
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

        Pair<K, V> previousPair = view.get(index);

        if (!pair.key.equals(previousPair.key) && source.containsKey(pair.key)) {
            throw new IllegalArgumentException("Duplicate keys not allowed.");
        }

        // Update the list
        view.update(index, pair);

        // Update the source
        updating = true;
        try {
            source.put(pair.key, pair.value);
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
        Sequence<Pair<K, V>> removed = view.remove(index, count);

        // Update the source
        updating = true;
        try {
            for (int i = 0, n = removed.getLength(); i < n; i++) {
                Pair<K, V> pair = removed.get(i);
                source.remove(pair.key);
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
        return view.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(Pair<K, V> pair) {
        return view.indexOf(pair);
    }

    /**
     * Finds the specified pair in the view list by searching linearly and
     * reporting an exact match only (bypasses the list's comparator).
     * 
     * @param pair
     *            The pair to search for
     * 
     * @return The index of the pair in the list, or <tt>-1</tt> if not found
     */
    private int linearSearch(Pair<K, V> pair) {
        int index = -1;

        for (int i = 0, n = view.getLength(); i < n; i++) {
            if (view.get(i).equals(pair)) {
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
        view.clear();

        // Update the source
        updating = true;
        try {
            source.clear();
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
        return view.getLength();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Comparator<Pair<K, V>> getComparator() {
        return view.getComparator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setComparator(Comparator<Pair<K, V>> comparator) {
        Comparator<Pair<K, V>> previousComparator = view.getComparator();

        if (previousComparator != comparator) {
            view.setComparator(comparator);
            listListeners.comparatorChanged(this, previousComparator);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Pair<K, V>> iterator() {
        return view.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListenerList<ListListener<Pair<K, V>>> getListListeners() {
        return listListeners;
    }

    /**
     * Gets the map list listeners.
     */
    public ListenerList<MapListListener<K, V>> getMapListListeners() {
        return mapListListeners;
    }
}
