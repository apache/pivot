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

import org.apache.pivot.util.EmptyIterator;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;

/**
 * Provides a filtered view of a list that can be sorted independently, but
 * remains backed by the original data. Modifications to the filtered list
 * are propagated to the source.
 */
public class FilteredList<T> implements List<T> {
    private static class FilteredListListenerList<T> extends ListenerList<FilteredListListener<T>>
        implements FilteredListListener<T> {
        public void sourceChanged(FilteredList<T> filteredList, List<T> previousSource) {
            for (FilteredListListener<T> listener : this) {
                listener.sourceChanged(filteredList, previousSource);
            }
        }

        public void filterChanged(FilteredList<T> filteredList, Filter<T> previousFilter) {
            for (FilteredListListener<T> listener : this) {
                listener.filterChanged(filteredList, previousFilter);
            }
        }
    }

    private List<T> source = null;
    private Filter<T> filter = null;

    private ArrayList<T> view = null;

    private boolean updating = false;

    private ListListener<T> listListener = new ListListener<T>() {
        public void itemInserted(List<T> list, int index) {
            if (!updating) {
                T item = list.get(index);

                if (filter == null
                    || filter.include(item)) {
                    // Add the item to the view
                    int viewIndex = view.add(item);
                    listListeners.itemInserted(FilteredList.this, viewIndex);
                }
            }
        }

        public void itemsRemoved(List<T> list, int index, Sequence<T> items) {
            if (!updating) {
                // Remove the items from the view
                for (int i = 0, n = items.getLength(); i < n; i++) {
                    T item = items.get(i);

                    int viewIndex = view.indexOf(item);

                    if (viewIndex != -1) {
                        Sequence<T> removed = view.remove(viewIndex, 1);
                        listListeners.itemsRemoved(FilteredList.this, viewIndex, removed);
                    }
                }
            }
        }

        public void itemUpdated(List<T> list, int index, T previousItem) {
            if (!updating) {
                T item = list.get(index);

                int viewIndex = view.indexOf(previousItem);

                if (filter == null
                    || filter.include(item)) {
                    if (viewIndex == -1) {
                        // Add the item to the view
                        viewIndex = view.add(item);
                        listListeners.itemInserted(FilteredList.this, viewIndex);
                    } else {
                        // Update the item in the view
                        Comparator<T> comparator = view.getComparator();

                        if (comparator == null) {
                            // Add the item to the view
                            viewIndex = view.add(item);
                            listListeners.itemInserted(FilteredList.this, viewIndex);
                        } else {
                            int previousViewIndex = ArrayList.binarySearch(view, item, comparator);

                            if (previousViewIndex == viewIndex) {
                                // Update the item in the view
                                view.update(viewIndex, item);
                            } else {
                                // Remove the item from the view
                                Sequence<T> removed = view.remove(previousViewIndex, 1);
                                listListeners.itemsRemoved(FilteredList.this, previousViewIndex, removed);

                                // Re-add the item to the view
                                viewIndex = view.add(item);
                                listListeners.itemInserted(FilteredList.this, viewIndex);
                            }
                        }
                    }
                } else {
                    if (viewIndex != -1) {
                        // Remove the item from the view
                        Sequence<T> removed = view.remove(viewIndex, 1);
                        listListeners.itemsRemoved(FilteredList.this, viewIndex, removed);
                    }
                }
            }
        }

        public void listCleared(List<T> list) {
            if (!updating) {
                // Remove all items from the view
                view.clear();
                listListeners.listCleared(FilteredList.this);
            }
        }

        public void comparatorChanged(List<T> list, Comparator<T> previousComparator) {
            // No-op
        }
    };

    private ListListenerList<T> listListeners = new ListListenerList<T>();
    private FilteredListListenerList<T> filteredListListeners = new FilteredListListenerList<T>();

    public FilteredList() {
        this(null);
    }

    public FilteredList(List<T> source) {
        setSource(source);
    }

    /**
     * Returns the source list.
     *
     * @return
     * The source list, or <tt>null</tt> if no source is set.
     */
    public List<T> getSource() {
        return source;
    }

    /**
     * Sets the source list.
     *
     * @param source
     * The source list, or <tt>null</tt> to clear the source.
     */
    public void setSource(List<T> source) {
        List<T> previousSource = this.source;

        if (previousSource != source) {
            // Clear any existing view
            if (view != null) {
                view.clear();
                listListeners.listCleared(this);
            }

            // Attach/detach list listeners
            if (previousSource != null) {
                previousSource.getListListeners().remove(listListener);
            }

            if (source != null) {
                source.getListListeners().add(listListener);
            }

            // Update source
            this.source = source;
            filteredListListeners.sourceChanged(this, previousSource);

            // Refresh the view
            if (source == null) {
                view = null;
            } else {
                view = new ArrayList<T>();

                for (T item : source) {
                    if (filter == null
                        || filter.include(item)) {
                        int index = view.add(item);
                        listListeners.itemInserted(this, index);
                    }
                }
            }
        }
    }

    /**
     * Returns the filter.
     *
     * @return
     * The current filter, or <tt>null</tt> if no filter is applied.
     */
    public Filter<T> getFilter() {
        return filter;
    }

    /**
     * Sets the filter.
     *
     * @param filter
     * The filter to apply, or <tt>null</tt> to clear the filter.
     */
    public void setFilter(Filter<T> filter) {
        Filter<T> previousFilter = this.filter;

        if (previousFilter != filter) {
            // Clear any existing view
            if (view != null) {
                view.clear();
                listListeners.listCleared(this);
            }

            // Update the filter
            this.filter = filter;
            filteredListListeners.filterChanged(this, previousFilter);

            // Refresh the view
            if (view != null) {
                for (T item : source) {
                    if (filter == null
                        || filter.include(item)) {
                        int index = view.add(item);
                        listListeners.itemInserted(this, index);
                    }
                }
            }
        }
    }

    /**
     * Adds an item to the view and the backing list.
     *
     * @param item
     * The item to add.
     */
    @Override
    public int add(T item) {
        if (view == null) {
            throw new IllegalStateException();
        }

        int index = -1;

        updating = true;
        try {
            // If it passes the filter, add to the view
            if (filter != null
                && filter.include(item)) {
                index = view.add(item);

                listListeners.itemInserted(this, index);
            }

            // Add to source list
            source.add(item);
        } finally {
            updating = false;
        }

        return index;
    }

    /**
     * Inserts an item into the view and adds the item to the backing list.
     *
     * @param item
     * The item to insert.
     *
     * @param index
     * The index at which the item should be inserted.
     */
    @Override
    public void insert(T item, int index) {
        if (view == null) {
            throw new IllegalStateException();
        }

        updating = true;
        try {
            // If it passes the filter, insert it into the view
            if (filter != null
                && filter.include(item)) {
                view.insert(item, index);

                listListeners.itemInserted(this, index);
            }

            // Add to source list
            source.add(item);
        } finally {
            updating = false;
        }
    }

    /**
     * Updates an item in the view and in the backing list.
     *
     * @param index
     * The index of the item to update.
     *
     * @param item
     * The item that will replace any existing value at the given index. The
     * updated item must also exist in the view.
     */
    @Override
    public T update(int index, T item) {
        if (view == null) {
            throw new IllegalStateException();
        }

        if (filter != null
            && !filter.include(item)) {
            throw new IllegalArgumentException();
        }

        T previousItem;

        updating = true;
        try {
            // Update the item in the view
            previousItem = view.update(index, item);
            listListeners.itemUpdated(this, index, previousItem);

            // Update the item in the source
            source.update(source.indexOf(previousItem), item);
        } finally {
            updating = false;
        }

        return previousItem;
    }

    /**
     * Removes an item from the view and the backing list.
     *
     * @param item
     * The item to remove.
     */
    @Override
    public int remove(T item) {
        if (view == null) {
            throw new IllegalStateException();
        }

        int index;

        updating = true;
        try {
            // Remove the item from the view
            index = view.indexOf(item);
            if (index != -1) {
                Sequence<T> removed = view.remove(index, 1);
                listListeners.itemsRemoved(this, index, removed);
            }

            // Remove the item from the source
            source.remove(item);
        } finally {
            updating = false;
        }

        return index;
    }

    /**
     * Removes one or more items from view and the backing list.
     *
     * @param index
     * The starting index to remove.
     *
     * @param count
     * The number of items to remove, beginning with <tt>index</tt>.
     */
    @Override
    public Sequence<T> remove(int index, int count) {
        if (view == null) {
            throw new IllegalStateException();
        }

        Sequence<T> removed;

        updating = true;
        try {
            // Remove the items from the view
            removed = view.remove(index, count);
            listListeners.itemsRemoved(this, index, removed);

            // Remove the items from the source
            for (int i = 0, n = removed.getLength(); i < n; i++) {
                source.remove(removed.get(i));
            }
        } finally {
            updating = false;
        }

        return removed;
    }

    /**
     * Clears the view and removes the cleared items from the backing list.
     */
    @Override
    public void clear() {
        if (view == null) {
            throw new IllegalStateException();
        }

        updating = true;
        try {
            // Remove all items from the view
            Sequence<T> removed = view.remove(0, view.getLength());
            listListeners.listCleared(this);

            // Remove the items from the source
            for (int i = 0, n = removed.getLength(); i < n; i++) {
                source.remove(removed.get(i));
            }
        } finally {
            updating = false;
        }
    }

    @Override
    public T get(int index) {
        return (view == null) ? null : view.get(index);
    }

    @Override
    public int indexOf(T item) {
        return (view == null) ? -1 : view.indexOf(item);
    }

    @Override
    public int getLength() {
        return (view == null) ? 0 : view.getLength();
    }

    @Override
    public Comparator<T> getComparator() {
        return (view == null) ? null : view.getComparator();
    }

    @Override
    public void setComparator(Comparator<T> comparator) {
        if (view == null) {
            throw new IllegalStateException();
        }

        Comparator<T> previousComparator = view.getComparator();
        if (previousComparator != comparator) {
            view.setComparator(comparator);
            listListeners.comparatorChanged(this, previousComparator);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return (view == null) ? new EmptyIterator<T>() : new ImmutableIterator<T>(view.iterator());
    }

    @Override
    public ListenerList<ListListener<T>> getListListeners() {
        return listListeners;
    }

    public ListenerList<FilteredListListener<T>> getFilteredListListeners() {
        return filteredListListeners;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getClass().getName());
        sb.append(" [");

        for (int i = 0; i < getLength(); i++) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(get(i));
        }

        sb.append("]");

        return sb.toString();
    }
}
