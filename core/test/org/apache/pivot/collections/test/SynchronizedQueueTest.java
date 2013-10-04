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

import static org.junit.Assert.assertTrue;

import org.apache.pivot.collections.ArrayQueue;
import org.apache.pivot.collections.LinkedQueue;
import org.apache.pivot.collections.Queue;
import org.apache.pivot.collections.concurrent.SynchronizedQueue;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.junit.Test;

public class SynchronizedQueueTest {
    @Test
    public void synchronizedQueueTest() {
        testSynchronizedQueue(new ArrayQueue<String>());
        testSynchronizedQueue(new LinkedQueue<String>());
    }

    private static void testSynchronizedQueue(Queue<String> queue) {
        final SynchronizedQueue<String> synchronizedQueue = new SynchronizedQueue<>(queue);

        Task<Void> testTask = new Task<Void>() {
            @Override
            public Void execute() {
                int i = 0;
                while (i < 5) {
                    synchronizedQueue.dequeue();
                    i++;
                }

                return null;
            }
        };

        testTask.execute(new TaskListener<Void>() {
            @Override
            public synchronized void taskExecuted(Task<Void> task) {
                notify();
            }

            @Override
            public synchronized void executeFailed(Task<Void> task) {
                notify();
            }
        });

        int i = 0;
        while (i < 5) {
            char c = 'A';
            c += i;
            synchronizedQueue.enqueue(Character.toString(c));

            try {
                Thread.sleep(500);
            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            }

            i++;
        }

        synchronized (testTask) {
            if (testTask.isPending()) {
                try {
                    testTask.wait(10000);
                } catch (InterruptedException exception) {
                    throw new RuntimeException(exception);
                }
            }
        }

        assertTrue(testTask.getFault() == null);
    }
}
