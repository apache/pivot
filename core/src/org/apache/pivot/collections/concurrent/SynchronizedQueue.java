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
package org.apache.pivot.collections.concurrent;

import org.apache.pivot.collections.Queue;
import org.apache.pivot.collections.QueueListener;
import org.apache.pivot.util.ListenerList;


/**
 * Synchronized implementation of the {@link Queue} interface.
 *
 * @author gbrown
 */
public class SynchronizedQueue<T> extends SynchronizedCollection<T>
    implements Queue<T> {
    private static class SynchronizedQueueListenerList<T>
        extends QueueListenerList<T> {
        @Override
        public synchronized void add(QueueListener<T> listener) {
            super.add(listener);
        }

        @Override
        public synchronized void remove(QueueListener<T> listener) {
            super.remove(listener);
        }

        @Override
        public synchronized void itemEnqueued(Queue<T> queue, T item) {
            super.itemEnqueued(queue, item);
        }

        @Override
        public synchronized void itemDequeued(Queue<T> queue, T item) {
            super.itemDequeued(queue, item);
        }
    }

    private SynchronizedQueueListenerList<T> queueListeners = new SynchronizedQueueListenerList<T>();

    public SynchronizedQueue(Queue<T> queue) {
        super(queue);
    }

    public synchronized void enqueue(T item) {
        ((Queue<T>)collection).enqueue(item);
        queueListeners.itemEnqueued(this, item);

        notify();
    }

    public synchronized T dequeue() {
        T item = null;
        try {
            while (isEmpty()) {
                wait();
            }

            item = ((Queue<T>)collection).dequeue();
            queueListeners.itemDequeued(this, item);
        } catch(InterruptedException exception) {
        }

        return item;
    }

    public synchronized T peek() {
        return ((Queue<T>)collection).peek();
    }

    public synchronized boolean isEmpty() {
        return ((Queue<T>)collection).isEmpty();
    }

    public ListenerList<QueueListener<T>> getQueueListeners() {
        return queueListeners;
    }
}
