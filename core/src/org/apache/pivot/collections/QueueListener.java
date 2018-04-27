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
 * Queue listener interface.
 */
public interface QueueListener<T> {
    /**
     * Queue listeners.
     */
    public static class Listeners<T> extends ListenerList<QueueListener<T>> implements
        QueueListener<T> {
        @Override
        public void itemEnqueued(Queue<T> queue, T item) {
            forEach(listener -> listener.itemEnqueued(queue, item));
        }

        @Override
        public void itemDequeued(Queue<T> queue, T item) {
            forEach(listener -> listener.itemDequeued(queue, item));
        }

        @Override
        public void queueCleared(Queue<T> queue) {
            forEach(listener -> listener.queueCleared(queue));
        }

        @Override
        public void comparatorChanged(Queue<T> queue, Comparator<T> previousComparator) {
            forEach(listener -> listener.comparatorChanged(queue, previousComparator));
        }
    }

    /**
     * QueueListener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter<T> implements QueueListener<T> {
        @Override
        public void itemEnqueued(Queue<T> queue, T item) {
            // empty block
        }

        @Override
        public void itemDequeued(Queue<T> queue, T item) {
            // empty block
        }

        @Override
        public void queueCleared(Queue<T> queue) {
            // empty block
        }

        @Override
        public void comparatorChanged(Queue<T> queue, Comparator<T> previousComparator) {
            // empty block
        }
    }

    /**
     * Called when an item has been inserted into the tail of a queue.
     *
     * @param queue The queue that has been modified.
     * @param item The item that was just added to the queue.
     */
    default void itemEnqueued(Queue<T> queue, T item) {
    }

    /**
     * Called when an item has been removed from the head of a queue.
     *
     * @param queue The queue in question.
     * @param item The item that was just removed from the head of the queue.
     */
    default void itemDequeued(Queue<T> queue, T item) {
    }

    /**
     * Called when a queue has been cleared.
     *
     * @param queue The newly cleared queue object.
     */
    default void queueCleared(Queue<T> queue) {
    }

    /**
     * Called when a queue's comparator has changed.
     *
     * @param queue The queue that changed.
     * @param previousComparator Previous value of the queue's comparator (if any).
     */
    default void comparatorChanged(Queue<T> queue, Comparator<T> previousComparator) {
    }
}
