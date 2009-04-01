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
package pivot.collections.concurrent;

import pivot.collections.Queue;

/**
 * Synchronized implementation of the {@link Queue} interface.
 *
 * @author gbrown
 */
public class SynchronizedQueue<T> extends SynchronizedList<T>
    implements Queue<T> {
    public SynchronizedQueue(Queue<T> queue) {
        super(queue);
    }

    public synchronized void enqueue(T item) {
        ((Queue<T>)collection).enqueue(item);

        notify();
    }

    /**
     * Removes an item from the head of the queue, blocking if the queue is
     * empty.
     *
     * @return
     * The item at the head of the queue, or null if the removing thread
     * was interrupted.
     */
    public synchronized T dequeue() {
        T item = null;

        try {
            while (getLength() == 0) {
                wait();
            }

            item = ((Queue<T>)collection).dequeue();
        } catch(InterruptedException exception) {
        }

        return item;
    }

    public synchronized T peek() {
        return ((Queue<T>)collection).peek();
    }

    @Override
    public synchronized int add(T item) {
        int index = super.add(item);

        notify();

        return index;
    }

    @Override
    public synchronized void insert(T item, int index) {
        super.insert(item, index);

        notify();
    }
}
