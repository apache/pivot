/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
public class TaskSequence<V> extends Task<Void>
    implements Sequence<Task<V>>, Iterable<Task<V>> {
    private ArrayList<Task<V>> tasks = new ArrayList<Task<V>>();
    private int activeTaskIndex = -1;

    public TaskSequence() {
        super();
    }

    public TaskSequence(Dispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public synchronized Void execute() throws TaskExecutionException {
        TaskListener<V> taskListener = new TaskListener<V>() {
            public void taskExecuted(Task<V> task) {
                synchronized (TaskSequence.this) {
                    TaskSequence.this.notify();
                }
            }

            public void executeFailed(Task<V> task) {
                synchronized (TaskSequence.this) {
                    TaskSequence.this.notify();
                }
            }
        };

        activeTaskIndex = 0;

        while (activeTaskIndex < tasks.getLength()) {
            Task<V> activeTask = tasks.get(activeTaskIndex);
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

    public int add(Task<V> task) {
        int index = tasks.getLength();
        insert(task, index);

        return index;
    }

    public synchronized void insert(Task<V> task, int index) {
        if (activeTaskIndex != -1) {
            throw new IllegalStateException();
        }

        tasks.insert(task, index);
    }

    public synchronized Task<V> update(int index, Task<V> task) {
        if (activeTaskIndex != -1) {
            throw new IllegalStateException();
        }

        return tasks.update(index, task);
    }

    public int remove(Task<V> task) {
        int index = tasks.indexOf(task);
        if (index != -1) {
            tasks.remove(index, 1);
        }

        return index;
    }

    public synchronized Sequence<Task<V>> remove(int index, int count) {
        if (activeTaskIndex != -1) {
            throw new IllegalStateException();
        }

        return tasks.remove(index, count);
    }

    public Task<V> get(int index) {
        return tasks.get(index);
    }


    public int indexOf(Task<V> task) {
        return tasks.indexOf(task);
    }

    public int getLength() {
        return tasks.getLength();
    }

    public Iterator<Task<V>> iterator() {
        return new ImmutableIterator<Task<V>>(tasks.iterator());
    }
}
