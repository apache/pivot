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
package pivot.collections.adapter;

import java.util.Comparator;
import java.util.Iterator;
import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.util.ListenerList;

public final class ListAdapter<T> implements List<T> {
    private java.util.List<T> list = null;
    private ListListenerList<T> listListeners = new ListListenerList<T>();

    public ListAdapter(java.util.List<T> list) {
        if (list == null) {
            throw new IllegalArgumentException("list is null.");
        }

        this.list = list;
    }

    public int add(T item) {
        int index = list.size();
        list.add(index, item);
        listListeners.itemInserted(this, index);

        return index;
    }

    public void insert(T item, int index) {
        list.add(index, item);
        listListeners.itemInserted(this, index);
    }

    public T update(int index, T item) {
        T previousItem = list.set(index, item);
        listListeners.itemUpdated(this, index, previousItem);

        return previousItem;
    }

    /**
     * NOTE This method is not supported because it cannot be efficiently
     * implemented for all list types.
     */
    public int remove(T item) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public Sequence<T> remove(int index, int count) {
        java.util.List<T> removedList = null;
        try {
            removedList = (java.util.List<T>)list.getClass().newInstance();
        } catch(IllegalAccessException exception) {
        } catch(InstantiationException exception) {
        }

        for (int i = count - 1; i >= 0; i--) {
            removedList.add(0, list.remove(index + i));
        }

        // Fire event
        List<T> removed = new ListAdapter<T>(removedList);
        listListeners.itemsRemoved(this, index, removed);

        return removed;
    }

    public void clear() {
        list.clear();
        listListeners.itemsRemoved(this, 0, null);
    }

    public T get(int index) {
        return list.get(index);
    }

    public int indexOf(T item) {
        return list.indexOf(item);
    }

    public int getLength() {
        return list.size();
    }

    public Comparator<T> getComparator() {
        return null;
    }

    /**
     * NOTE This method is not supported because it cannot be efficiently
     * implemented for all list types.
     */
    public void setComparator(Comparator<T> comparator) {
        throw new UnsupportedOperationException();
    }

    public Iterator<T> iterator() {
        return list.iterator();
    }

    public ListenerList<ListListener<T>> getListListeners() {
        return listListeners;
    }
}
