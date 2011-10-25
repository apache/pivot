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
package org.apache.pivot.util.concurrent;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;


/**
 * {@link Task} that runs a sequence of tasks in series and notifies listeners
 * when all tasks are complete.
 */
public class TaskSequence extends Task<Void>
    implements Sequence<Task<?>>, Iterable<Task<?>> {
    private ArrayList<Task<?>> tasks = new ArrayList<Task<?>>();

    public TaskSequence() {
        this(DEFAULT_EXECUTOR_SERVICE);
    }

    public TaskSequence(ExecutorService executorService) {
        super(executorService);
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized Void execute() throws TaskExecutionException {
        TaskListener<Object> taskListener = new TaskListener<Object>() {
            @Override
            public void taskExecuted(Task<Object> task) {
                synchronized (TaskSequence.this) {
                    TaskSequence.this.notify();
                }
            }

            @Override
            public void executeFailed(Task<Object> task) {
                synchronized (TaskSequence.this) {
                    TaskSequence.this.notify();
                }
            }
        };

        for (Task<?> task : tasks) {
            if (abort) {
                throw new AbortException();
            }

            ((Task<Object>)task).execute(taskListener);

            try {
                wait();
            } catch (InterruptedException exception) {
                throw new TaskExecutionException(exception);
            }
        }

        return null;
    }

    @Override
    public synchronized int add(Task<?> task) {
        int index = tasks.getLength();
        insert(task, index);

        return index;
    }

    @Override
    public synchronized void insert(Task<?> task, int index) {
        if (isPending()) {
            throw new IllegalStateException();
        }

        tasks.insert(task, index);
    }

    @Override
    public synchronized Task<?> update(int index, Task<?> task) {
        if (isPending()) {
            throw new IllegalStateException();
        }

        return tasks.update(index, task);
    }

    @Override
    public synchronized int remove(Task<?> task) {
        int index = tasks.indexOf(task);
        if (index != -1) {
            tasks.remove(index, 1);
        }

        return index;
    }

    @Override
    public synchronized Sequence<Task<?>> remove(int index, int count) {
        if (isPending()) {
            throw new IllegalStateException();
        }

        return tasks.remove(index, count);
    }

    @Override
    public synchronized Task<?> get(int index) {
        return tasks.get(index);
    }

    @Override
    public synchronized int indexOf(Task<?> task) {
        return tasks.indexOf(task);
    }

    @Override
    public synchronized int getLength() {
        return tasks.getLength();
    }

    @Override
    public Iterator<Task<?>> iterator() {
        return new ImmutableIterator<Task<?>>(tasks.iterator());
    }
}
