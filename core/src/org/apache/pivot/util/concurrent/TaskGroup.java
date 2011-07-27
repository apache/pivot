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

import org.apache.pivot.collections.Group;
import org.apache.pivot.collections.HashSet;
import org.apache.pivot.util.ImmutableIterator;


/**
 * {@link Task} that runs a group of tasks in parallel and notifies listeners
 * when all tasks are complete.
 */
public class TaskGroup extends Task<Void>
    implements Group<Task<?>>, Iterable<Task<?>> {
    private HashSet<Task<?>> tasks = new HashSet<Task<?>>();
    private int complete = 0;

    public TaskGroup() {
        this(DEFAULT_EXECUTOR_SERVICE);
    }

    public TaskGroup(ExecutorService executorService) {
        super(executorService);
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized Void execute() throws TaskExecutionException {
        TaskListener<Object> taskListener = new TaskListener<Object>() {
            @Override
            public void taskExecuted(Task<Object> task) {
                synchronized (TaskGroup.this) {
                    complete++;
                    TaskGroup.this.notify();
                }
            }

            @Override
            public void executeFailed(Task<Object> task) {
                synchronized (TaskGroup.this) {
                    complete++;
                    TaskGroup.this.notify();
                }
            }
        };

        complete = 0;
        for (Task<?> task : tasks) {
            ((Task<Object>)task).execute(taskListener);
        }

        while (complete < getCount()) {
            try {
                wait();
            } catch (InterruptedException exception) {
                throw new TaskExecutionException(exception);
            }
        }

        return null;
    }

    /**
     * Aborts all tasks in this group.
     */
    @Override
    public synchronized void abort() {
        for (Task<?> task : this) {
            synchronized (task) {
                if (task.isPending()) {
                    task.abort();
                }
            }
        }

        super.abort();
    }

    @Override
    public synchronized boolean add(Task<?> element) {
        if (isPending()) {
            throw new IllegalStateException();
        }

        return tasks.add(element);
    }

    @Override
    public synchronized boolean remove(Task<?> element) {
        if (isPending()) {
            throw new IllegalStateException();
        }

        return tasks.remove(element);
    }

    @Override
    public synchronized boolean contains(Task<?> element) {
        return tasks.contains(element);
    }

    public synchronized int getCount() {
        return tasks.getCount();
    }

    @Override
    public Iterator<Task<?>> iterator() {
        return new ImmutableIterator<Task<?>>(tasks.iterator());
    }
}
