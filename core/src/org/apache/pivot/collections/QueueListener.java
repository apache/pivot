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

/**
 * Queue listener interface.
 */
public interface QueueListener<T> {
    /**
     * Called when an item has been inserted into the tail of a queue.
     *
     * @param queue
     * @param item
     */
    public void itemEnqueued(Queue<T> queue, T item);

    /**
     * Called when an item has been removed from the head of a queue.
     *
     * @param queue
     * @param item
     */
    public void itemDequeued(Queue<T> queue, T item);
}
