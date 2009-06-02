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
package pivot.util.concurrent;

import java.util.Iterator;

import pivot.collections.ArrayList;
import pivot.collections.Sequence;
import pivot.util.ImmutableIterator;

/**
 * Class that runs a sequence of tasks in series and notifies listeners
 * when all tasks are complete. Callers can retrieve task results or faults by
 * calling {@link Task#getResult()} and {@link Task#getFault()},
 * respectively.
 *
 * @author gbrown
 */
public class TaskSequence extends Task<Void>
    implements Sequence<Task<?>>, Iterable<Task<?>> {
    private ArrayList<Task<?>> tasks = new ArrayList<Task<?>>();
    private int activeTaskIndex = -1;

    public TaskSequence() {
        super();
    }

    public TaskSequence(Dispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized Void execute() throws TaskExecutionException {
        TaskListener<Object> taskListener = new TaskListener<Object>() {
            public void taskExecuted(Task<Object> task) {
                synchronized (TaskSequence.this) {
                    TaskSequence.this.notify();
                }
            }

            public void executeFailed(Task<Object> task) {
                synchronized (TaskSequence.this) {
                    TaskSequence.this.notify();
                }
            }
        };

        activeTaskIndex = 0;

        while (activeTaskIndex < tasks.getLength()) {
            Task<Object> activeTask = (Task<Object>)tasks.get(activeTaskIndex);
            activeTask.execute(taskListener);

            try {
                wait();
            } catch (InterruptedException exception) {
                throw new TaskExecutionException(exception);
            }

            activeTaskIndex++;
        }

        activeTaskIndex = -1;

        return null;
    }

    public int add(Task<?> task) {
        int index = tasks.getLength();
        insert(task, index);

        return index;
    }

    public synchronized void insert(Task<?> task, int index) {
        if (activeTaskIndex != -1) {
            throw new IllegalStateException();
        }

        tasks.insert(task, index);
    }

    public synchronized Task<?> update(int index, Task<?> task) {
        if (activeTaskIndex != -1) {
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

    public synchronized Sequence<Task<?>> remove(int index, int count) {
        if (activeTaskIndex != -1) {
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
