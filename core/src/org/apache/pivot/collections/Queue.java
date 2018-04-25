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

import org.apache.pivot.util.ListenerList;

/**
 * Interface representing a first-in, first-out (FIFO) queue when unsorted, and
 * a priority queue when sorted.
 */
public interface Queue<T> extends Collection<T> {
    /**
     * Enqueues an item. If the queue is unsorted, the item is added at the tail
     * of the queue (index <tt>0</tt>). Otherwise, it is inserted at the
     * appropriate index.
     * <p> If there is a maximum queue length defined and the queue is already at
     * the maximum length this new item will not be queued.
     *
     * @param item The item to add to the queue.
     */
    public void enqueue(T item);

    /**
     * Removes the item from the head of the queue and returns it. Calling this
     * method should have the same effect as: <code>remove(getLength() - 1,
     * 1);</code>
     * @return The (removed) object at the head of the queue.
     */
    public T dequeue();

    /**
     * Returns the item at the head of the queue without removing it from the
     * queue. Returns null if the queue contains no items. Will also return null
     * if the head item in the queue is null. <tt>isEmpty()</tt> can be used to
     * distinguish between these two cases.
     * @return The object at the head of the queue (not removed from the queue).
     */
    public T peek();

    /**
     * Tests the emptiness of the queue.
     *
     * @return <tt>true</tt> if the queue contains no items; <tt>false</tt>,
     * otherwise.
     */
    @Override
    public boolean isEmpty();

    /**
     * @return The length of the queue.
     */
    public int getLength();

    /**
     * @return The maximum queue length allowed (0 means unlimited).
     */
    public int getMaxLength();

    /**
     * Set the maximum allowed queue length (0 means unlimited).
     *
     * @param maxLength The maximum allowed length.
     */
    public void setMaxLength(int maxLength);

    /**
     * @return The queue listener list.
     */
    public ListenerList<QueueListener<T>> getQueueListeners();
}
