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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;

import pivot.util.ListenerList;

/**
 * Implementation of the {@link List} interface that is backed by a linked
 * list.
 * <p>
 * TODO This class is currently incomplete.
 */
public class LinkedList<T> implements List<T>, Serializable {
    private static final long serialVersionUID = 0;

    public int add(T item) {
        // TODO
        return 0;
    }

    public void insert(T item, int index) {
        // TODO Auto-generated method stub
    }

    public T update(int index, T item) {
        // TODO Auto-generated method stub
        return null;
    }

    public int remove (T item) {
        // TODO
        return -1;
    }

    public Sequence<T> remove(int index, int count) {
        // TODO Auto-generated method stub
        return null;
    }

    public void clear() {
        // TODO
    }

    public T get(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    public int indexOf(T item) {
        // TODO
        return -1;
    }

    public int getLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    public Comparator<T> getComparator() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setComparator(Comparator<T> comparator) {
        // TODO Auto-generated method stub

    }

    public Iterator<T> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    public ListenerList<ListListener<T>> getListListeners() {
        // TODO Auto-generated method stub
        return null;
    }
}
