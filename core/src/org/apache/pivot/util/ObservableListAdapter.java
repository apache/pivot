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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Observable list that is backed by an instance of {@link List}.
 */
public class ObservableListAdapter<E> extends AbstractList<E>
    implements ObservableList<E> {
    private List<E> list;
    private ObservableListListenerList<E> observableListListeners =
        new ObservableListListenerList<E>();

    public ObservableListAdapter(List<E> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }

        this.list = list;
    }

    public List<E> getList() {
        return list;
    }

    @Override
    public void add(int index, E element) {
        list.add(index, element);

        observableListListeners.elementsAdded(this, index, index + 1);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> collection) {
        boolean added = list.addAll(index, collection);

        if (added) {
            observableListListeners.elementsAdded(this, index,
                index + collection.size());
        }

        return added;
    }

    @Override
    public E remove(int index) {
        E element = list.remove(index);
        observableListListeners.elementsRemoved(this, index,
            Collections.singletonList(element));

        return element;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        List<E> subList = list.subList(fromIndex, toIndex);
        List<E> removed = new ArrayList<E>(subList);
        subList.clear();

        observableListListeners.elementsRemoved(this, fromIndex, removed);
    }

    @Override
    public E get(int index) {
        return list.get(index);
    }

    @Override
    public E set(int index, E element) {
        E previousElement = list.set(index, element);
        observableListListeners.elementsUpdated(this, index,
            Collections.singletonList(previousElement));

        return previousElement;
    }

    @Override
    public List<E> setAll(int index, Collection<? extends E> collection) {
        ArrayList<E> previousElements = new ArrayList<E>(collection.size());

        int i = index;
        for (E element : collection) {
            list.set(i++, element);
            previousElements.add(element);
        }

        observableListListeners.elementsUpdated(this, index, previousElements);

        return previousElements;
    }

    @Override
    public void sort(Comparator<E> comparator) {
        Collections.sort(list, comparator);
        observableListListeners.elementsUpdated(this, 0, this);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean equals(Object object) {
        return list.equals(object);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public ListenerList<ObservableListListener<E>> getObservableListListeners() {
        return observableListListeners;
    }

    public static <E> ObservableList<E> observableArrayList() {
        return new ObservableListAdapter<E>(new ArrayList<E>());
    }

    public static <E> ObservableList<E> observableLinkedList() {
        return new ObservableListAdapter<E>(new LinkedList<E>());
    }
}
