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

import org.apache.pivot.collections.Group;
import org.apache.pivot.collections.HashSet;
import org.apache.pivot.util.ImmutableIterator;


/**
 * Class that runs a group of tasks in parallel and notifies listeners
 * when all tasks are complete.
 */
public class TaskGroup extends Task<Void>
    implements Group<Task<?>>, Iterable<Task<?>> {
    private HashSet<Task<?>> tasks = new HashSet<Task<?>>();
    private int count = 0;
    private int complete = 0;

    public TaskGroup() {
        super();
    }

    public TaskGroup(Dispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized Void execute() throws TaskExecutionException {
        TaskListener<Object> taskListener = new TaskListener<Object>() {
            @Override
            public void taskExecuted(Task<Object> task) {
                synchronized(TaskGroup.this) {
                    complete++;
                    TaskGroup.this.notify();
                }
            }

            @Override
            public void executeFailed(Task<Object> task) {
                synchronized(TaskGroup.this) {
                    complete++;
                    TaskGroup.this.notify();
                }
            }
        };

        complete = 0;
        for (Task<?> task : tasks) {
            ((Task<Object>)task).execute(taskListener);
        }

        while (complete < count) {
            try {
                wait();
            } catch (InterruptedException exception) {
                throw new TaskExecutionException(exception);
            }
        }

        return null;
    }

    @Override
    public boolean add(Task<?> element) {
        if (isPending()) {
            throw new IllegalStateException();
        }

        boolean added = tasks.add(element);
        if (added) {
            count++;
        }

        return added;
    }

    @Override
    public boolean remove(Task<?> element) {
        if (isPending()) {
            throw new IllegalStateException();
        }

        boolean removed = tasks.remove(element);
        if (removed) {
            count--;
        }

        return removed;
    }

    @Override
    public boolean contains(Task<?> element) {
        return tasks.contains(element);
    }

    @Override
    public Iterator<Task<?>> iterator() {
        return new ImmutableIterator<Task<?>>(tasks.iterator());
    }
}
