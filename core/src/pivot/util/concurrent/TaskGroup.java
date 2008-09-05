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

import pivot.collections.Group;
import pivot.collections.HashMap;

/**
 * <p>Class that runs a group of tasks in parallel and notifies listeners
 * when all tasks are complete. Callers can retrieve task results or faults by
 * calling {@link Task#getResult()} and {@link Task#getFault()},
 * respectively.</p>
 *
 * @author tvolkert
 * @author gbrown
 */
public class TaskGroup<V> extends Task<Void> implements Group<Task<? extends V>> {
    private class TaskHandler implements TaskListener<Object> {
        public void taskExecuted(Task<Object> task) {
            synchronized (TaskGroup.this) {
                tasks.put(task, Boolean.TRUE);
                TaskGroup.this.notify();
            }
        }

        public void executeFailed(Task<Object> task) {
            synchronized (TaskGroup.this) {
                exception = task.getFault();
                TaskGroup.this.notify();
            }
        }
    }

    private HashMap<Task<Object>, Boolean> tasks = new HashMap<Task<Object>, Boolean>();
    private boolean executing = false;
    private Exception exception;

    public TaskGroup() {
        super();
    }

    public TaskGroup(Dispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public synchronized Void execute() throws TaskExecutionException {
        executing = true;

        try {
            TaskHandler taskHandler = new TaskHandler();

            for (Task<Object> task : tasks) {
                tasks.put(task, Boolean.FALSE);
                task.execute(taskHandler);
            }

            boolean complete = false;

            while (!complete) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    throw new TaskExecutionException(ex);
                }

                if (exception != null) {
                    throw new TaskExecutionException(exception);
                }

                complete = true;
                for (Task<Object> task : tasks) {
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
    public synchronized void add(Task<? extends V> element) {
        if (executing) {
            throw new IllegalStateException("Task group is executing.");
        }

        tasks.put((Task<Object>)element, Boolean.FALSE);
    }

    @SuppressWarnings("unchecked")
    public synchronized void remove(Task<? extends V> element) {
        if (executing) {
            throw new IllegalStateException("Task group is executing.");
        }

        tasks.remove((Task<Object>)element);
    }

    @SuppressWarnings("unchecked")
    public boolean contains(Task<? extends V> element) {
        return tasks.containsKey((Task<Object>)element);
    }

    public boolean isEmpty() {
        return tasks.isEmpty();
    }
}
