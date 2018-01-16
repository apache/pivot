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
package org.apache.pivot.collections.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;

import org.apache.pivot.collections.ArrayQueue;
import org.apache.pivot.collections.LinkedQueue;
import org.apache.pivot.collections.Queue;
import org.apache.pivot.collections.QueueListener;
import org.junit.Test;

public class QueueTest {
    @Test
    public void queueTest() {
        testQueue(new ArrayQueue<String>(5));
        testQueue(new LinkedQueue<String>());
        testSortedQueue(new ArrayQueue<String>(5));
        testSortedQueue(new LinkedQueue<String>());
        testMaxLengthQueue(new ArrayQueue<String>(5, 5));
        testMaxLengthQueue(new LinkedQueue<String>(5));
    }

    private static void testQueue(Queue<String> queue) {
        int i = 0;
        while (i < 5) {
            char c = 'A';
            c += i;
            queue.enqueue(Character.toString(c));
            i++;
        }

        i = 0;
        while (!queue.isEmpty()) {
            String s = queue.dequeue();
            char c = s.charAt(0);
            c -= i;
            assertTrue(c == 'A');
            i++;
        }
    }

    private static void testSortedQueue(Queue<String> queue) {
        queue.setComparator(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        });

        int i = 0;
        while (i < 5) {
            char c = 'A';
            c += i;
            queue.enqueue(Character.toString(c));
            i++;
        }

        i = 4;
        while (!queue.isEmpty()) {
            String s = queue.dequeue();
            char c = s.charAt(0);
            c -= i;
            assertTrue(c == 'A');
            i--;
        }
    }

    private static class Listener implements QueueListener<String> {
        public int queueCount = 0;

        @Override
        public void itemEnqueued(Queue<String> queue, String item) {
            queueCount++;
        }
    }

    private static void testMaxLengthQueue(Queue<String> queue) {
        Listener listener = new Listener();
        queue.getQueueListeners().add(listener);

        for (int i = 0; i < queue.getMaxLength() + 2; i++) {
            queue.enqueue("This is a test!");
        }

        assertEquals(listener.queueCount, queue.getMaxLength());
    }
}
