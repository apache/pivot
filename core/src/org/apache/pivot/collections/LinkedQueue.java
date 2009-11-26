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
import java.util.Iterator;

import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;


/**
 * Implementation of the {@link Queue} interface that is backed by a linked
 * list.
 */
public class LinkedQueue<T> implements Queue<T>, Serializable {
    private static final long serialVersionUID = 1598074020226109253L;

    private LinkedList<T> linkedList = new LinkedList<T>();
    private transient QueueListenerList<T> queueListeners = new QueueListenerList<T>();

    public LinkedQueue() {
        this(null);
    }

    public LinkedQueue(Comparator<T> comparator) {
        setComparator(comparator);
    }

    @Override
    public void enqueue(T item) {
        if (getComparator() == null) {
            linkedList.insert(item, 0);
        } else {
            linkedList.add(item);
        }

        queueListeners.itemEnqueued(this, item);
    }

    @Override
    public T dequeue() {
        int length = linkedList.getLength();
        if (length == 0) {
            throw new IllegalStateException("queue is empty");
        }

        T item = linkedList.remove(length - 1, 1).get(0);
        queueListeners.itemDequeued(this, item);

        return item;
    }

    @Override
    public T peek() {
        T item = null;
        int length = linkedList.getLength();
        if (length > 0) {
            item = linkedList.get(length - 1);
        }

        return item;
    }

    @Override
    public void clear() {
        if (linkedList.getLength() > 0) {
            linkedList.clear();
            queueListeners.queueCleared(this);
        }
    }

    @Override
    public boolean isEmpty() {
        return (linkedList.getLength() == 0);
    }

    @Override
    public int getLength() {
        return linkedList.getLength();
    }

    @Override
    public Comparator<T> getComparator() {
        return linkedList.getComparator();
    }

    @Override
    public void setComparator(Comparator<T> comparator) {
        Comparator<T> previousComparator = getComparator();
        linkedList.setComparator(comparator);

        queueListeners.comparatorChanged(this, previousComparator);
    }

    @Override
    public Iterator<T> iterator() {
        return new ImmutableIterator<T>(linkedList.iterator());
    }

    @Override
    public ListenerList<QueueListener<T>> getQueueListeners() {
        return queueListeners;
    }
}
