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

import pivot.collections.Group;
import pivot.collections.HashMap;
import pivot.util.ImmutableIterator;

/**
 * Class that runs a group of tasks in parallel and notifies listeners
 * when all tasks are complete.
 *
 * @author tvolkert
 * @author gbrown
 */
public class TaskGroup extends Task<Void>
    implements Group<Task<?>>, Iterable<Task<?>> {
    private class TaskHandler implements TaskListener<Object> {
        public void taskExecuted(Task<Object> task) {
            synchronized (TaskGroup.this) {
                tasks.put(task, Boolean.TRUE);
                TaskGroup.this.notify();
            }
        }

        public void executeFailed(Task<Object> task) {
            synchronized (TaskGroup.this) {
                tasks.put(task, Boolean.TRUE);
                TaskGroup.this.notify();
            }
        }
    }

    private HashMap<Task<?>, Boolean> tasks = new HashMap<Task<?>, Boolean>();
    private boolean executing = false;

    public TaskGroup() {
        super();
    }

    public TaskGroup(Dispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized Void execute() throws TaskExecutionException {
        executing = true;

        try {
            TaskHandler taskHandler = new TaskHandler();

            for (Task<?> task : tasks) {
                tasks.put(task, Boolean.FALSE);
                ((Task<Object>)task).execute(taskHandler);
            }

            boolean complete = false;

            while (!complete) {
                try {
                    wait();
                } catch (InterruptedException exception) {
                    throw new TaskExecutionException(exception);
                }

                complete = true;
                for (Task<?> task : tasks) {
                    if (!tasks.get(task)) {
                        complete = false;
                        break;
                    }
                }
            }
        } finally {
            executing = false;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public synchronized void add(Task<?> element) {
        if (executing) {
            throw new IllegalStateException("Task group is executing.");
        }

        tasks.put((Task<Object>)element, Boolean.FALSE);
    }

    @SuppressWarnings("unchecked")
    public synchronized void remove(Task<?> element) {
        if (executing) {
            throw new IllegalStateException("Task group is executing.");
        }

        tasks.remove((Task<Object>)element);
    }

    @SuppressWarnings("unchecked")
    public boolean contains(Task<?> element) {
        return tasks.containsKey((Task<Object>)element);
    }

    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    public Iterator<Task<?>> iterator() {
        return new ImmutableIterator<Task<?>>(tasks.iterator());
    }
}
