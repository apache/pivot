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
package org.apache.pivot.util.concurrent.test;

import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskGroup;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.util.concurrent.TaskSequence;
import org.junit.Test;

public class TaskTest {
    public static class SleepTask extends Task<Void> {
        private long timeoutSleepTask = 0;

        public SleepTask(long timeout) {
            this.timeoutSleepTask = timeout;
        }

        @Override
        public Void execute() {
            System.out.println("Starting task " + this + "...");

            try {
                Thread.sleep(timeoutSleepTask);
            } catch (InterruptedException exception) {
                System.out.println(exception);
            }

            System.out.println("...done");

            return null;
        }

        @Override
        public String toString() {
            return Long.toString(timeoutSleepTask);
        }
    }

    @Test
    public void testTaskSequence() {
        TaskListener<Void> taskListener = new TaskListener<Void>() {
            @Override
            public synchronized void taskExecuted(Task<Void> task) {
                System.out.println("EXECUTED");
                notify();
            }

            @Override
            public synchronized void executeFailed(Task<Void> task) {
                System.out.println("FAILED: " + task.getFault());
                notify();
            }
        };

        TaskSequence taskSequence = new TaskSequence();

        SleepTask task1 = new SleepTask(500);
        taskSequence.add(task1);

        SleepTask task2 = new SleepTask(1000);
        taskSequence.add(task2);

        SleepTask task3 = new SleepTask(2000);
        taskSequence.add(task3);

        synchronized (taskListener) {
            taskSequence.execute(taskListener);

            try {
                taskListener.wait();
            } catch (InterruptedException exception) {
                // empty block
            }
        }
    }

    @Test
    public void testTaskGroup() {
        TaskListener<Void> taskListener = new TaskListener<Void>() {
            @Override
            public synchronized void taskExecuted(Task<Void> task) {
                System.out.println("EXECUTED");
                notify();
            }

            @Override
            public synchronized void executeFailed(Task<Void> task) {
                System.out.println("FAILED: " + task.getFault());
                notify();
            }
        };

        TaskGroup taskGroup = new TaskGroup();

        SleepTask task1 = new SleepTask(500);
        taskGroup.add(task1);

        SleepTask task2 = new SleepTask(1000);
        taskGroup.add(task2);

        SleepTask task3 = new SleepTask(2000);
        taskGroup.add(task3);

        synchronized (taskListener) {
            taskGroup.execute(taskListener);

            try {
                taskListener.wait();
            } catch (InterruptedException exception) {
                // empty block
            }
        }
    }
}
