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

public abstract class Task<V> {
    private class ExecuteCallback implements Runnable {
        public void run() {
            V result = null;
            Exception fault = null;

            try {
                result = execute();
            }
            catch(Exception exception) {
                fault = exception;
            }

            synchronized(Task.this) {
                Task.this.result = result;
                Task.this.fault = fault;

                if (fault == null) {
                    taskListener.taskExecuted(Task.this);
                }
                else {
                    taskListener.executeFailed(Task.this);
                }

                abort = false;
                taskListener = null;
            }

        }
    }

    private Dispatcher dispatcher = null;

    private V result = null;
    private Exception fault = null;
    private TaskListener<V> taskListener = null;

    protected volatile long timeout = Long.MAX_VALUE;
    protected volatile boolean abort = false;

    private ExecuteCallback executeCallback = null;

    private static Dispatcher DEFAULT_DISPATCHER = new Dispatcher();

    public Task() {
        this(DEFAULT_DISPATCHER);
    }

    public Task(Dispatcher dispatcher) {
        if (dispatcher == null) {
            throw new IllegalArgumentException("dispatcher is null.");
        }

        this.dispatcher = dispatcher;
    }

    public abstract V execute() throws TaskExecutionException;

    public synchronized void execute(TaskListener<V> taskListener) {
        if (taskListener == null) {
            throw new IllegalArgumentException("taskListener is null.");
        }

        if (this.taskListener != null) {
            throw new IllegalThreadStateException("Task is already pending.");
        }

        this.taskListener = taskListener;

        result = null;
        fault = null;
        abort = false;

        // Create a new execute callback and post it to the dispatcher
        executeCallback = new ExecuteCallback();
        dispatcher.getPendingQueue().enqueue(executeCallback);
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    /**
     * Returns the result of executing the task.
     *
     * @return
     * The task result, or <tt>null</tt> if the task is still executing or
     * has failed. The result itself may also be <tt>null</tt>; callers should
     * call {@link #isPending()} and {@link #getFault()} to distinguish
     * between these cases.
     */
    public synchronized V getResult() {
        return result;
    }

    public synchronized Exception getFault() {
        return fault;
    }

    public synchronized TaskListener<V> getTaskListener() {
        return taskListener;
    }

    public synchronized long getTimeout() {
        return timeout;
    }

    public synchronized void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public synchronized void abort() {
        if (taskListener == null) {
            throw new IllegalStateException("Task is not currently pending.");
        }

        abort = true;
    }
}
