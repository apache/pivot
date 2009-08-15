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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;


/**
 * Class that runs a sequence of tasks in series and notifies listeners
 * when all tasks are complete.
 *
 */
public class TaskSequence extends Task<Void>
    implements Sequence<Task<?>>, Iterable<Task<?>> {
    private ArrayList<Task<?>> tasks = new ArrayList<Task<?>>();

    public TaskSequence() {
        super();
    }

    public TaskSequence(Dispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public Void execute() throws TaskExecutionException {
        for (Task<?> task : tasks) {
            task.execute();
        }

        return null;
    }

    public int add(Task<?> task) {
        int index = tasks.getLength();
        insert(task, index);

        return index;
    }

    public void insert(Task<?> task, int index) {
        if (isPending()) {
            throw new IllegalStateException();
        }

        tasks.insert(task, index);
    }

    public Task<?> update(int index, Task<?> task) {
        if (isPending()) {
            throw new IllegalStateException();
        }

        return tasks.update(index, task);
    }

    public int remove(Task<?> task) {
        int index = tasks.indexOf(task);
        if (index != -1) {
            tasks.remove(index, 1);
        }

        return index;
    }

    public Sequence<Task<?>> remove(int index, int count) {
        if (isPending()) {
            throw new IllegalStateException();
        }

        return tasks.remove(index, count);
    }

    public Task<?> get(int index) {
        return tasks.get(index);
    }

    public int indexOf(Task<?> task) {
        return tasks.indexOf(task);
    }

    public int getLength() {
        return tasks.getLength();
    }

    public Iterator<Task<?>> iterator() {
        return new ImmutableIterator<Task<?>>(tasks.iterator());
    }
}
