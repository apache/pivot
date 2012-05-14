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

import org.apache.pivot.util.ListenerList;

/**
 * Interface representing a first-in, first-out (FIFO) queue when unsorted, and
 * a priority queue when sorted.
 */
public interface Queue<T> extends Collection<T> {
    /**
     * Queue listener list.
     */
    public static class QueueListenerList<T> extends ListenerList<QueueListener<T>>
        implements QueueListener<T> {
        @Override
        public void itemEnqueued(Queue<T> queue, T item) {
            for (QueueListener<T> listener : this) {
                listener.itemEnqueued(queue, item);
            }
        }

        @Override
        public void itemDequeued(Queue<T> queue, T item) {
            for (QueueListener<T> listener : this) {
                listener.itemDequeued(queue, item);
            }
        }

        @Override
        public void queueCleared(Queue<T> queue) {
            for (QueueListener<T> listener : this) {
                listener.queueCleared(queue);
            }
        }

        @Override
        public void comparatorChanged(Queue<T> queue, Comparator<T> previousComparator) {
            for (QueueListener<T> listener : this) {
                listener.comparatorChanged(queue, previousComparator);
            }
        }
    }

    /**
     * Enqueues an item. If the queue is unsorted, the item is added at the
     * tail of the queue (index <tt>0</tt>). Otherwise, it is inserted at the
     * appropriate index.
     *
     * @param item
     * The item to add to the queue.
     */
    public void enqueue(T item);

    /**
     * Removes the item from the head of the queue and returns it. Calling this
     * method should have the same effect as:
     *
     * <code>remove(getLength() - 1, 1);</code>
     */
    public T dequeue();

    /**
     * Returns the item at the head of the queue without removing it from the
     * queue. Returns null if the queue contains no items. Will also return null
     * if the head item in the queue is null. <tt>isEmpty()</tt> can be used
     * to distinguish between these two cases.
     */
    public T peek();

    /**
     * Tests the emptiness of the queue.
     *
     * @return
     * <tt>true</tt> if the queue contains no items; <tt>false</tt>,
     * otherwise.
     */
    @Override
    public boolean isEmpty();

    /**
     * Returns the length of the queue.
     */
    public int getLength();

    /**
     * Returns the queue listener list.
     */
    public ListenerList<QueueListener<T>> getQueueListeners();
}
