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

/**
 * <p>Abstract base class for "tasks". A task is an asynchronous operation that
 * may optionally return a value.</p>
 *
 * @param V
 * The type of the value returned by the operation. May be {@link Void} to
 * indicate that the task does not return a value.
 *
 * @author gbrown
 */
public abstract class Task<V> {
    /**
     * <p>Task execution callback that is posted to the dispatcher queue.</p>
     */
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

    /**
     * Synchronously executes the task.
     *
     * @return
     * The result of the task's execution.
     *
     * @throws TaskExecutionException
     * If an error occurs while executing the task.
     */
    public abstract V execute() throws TaskExecutionException;

    /**
     * Asynchronously executes the task. The caller is notified of the task's
     * completion via the listener argument.
     *
     * @param taskListener
     * The listener to be notified when the task completes.
     */
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

    /**
     * Returns the dispatcher used to execute this task.
     */
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

    /**
     * Returns the fault that occurred while executing the task.
     *
     * @return
     * The task fault, or <tt>null</tt> if the task is still executing or
     * has succeeded. Callers should call {@link #isPending()} to distinguish
     * between these cases.
     */
    public synchronized Exception getFault() {
        return fault;
    }

    /**
     * Returns the pending state of the task.
     *
     * @return
     * <tt>true</tt> if the task is awaiting execution or currently executing;
     * <tt>false</tt>, otherwise.
     */
    public synchronized boolean isPending() {
        return (taskListener != null);
    }


    /**
     * Returns the timeout value for this task.
     *
     * @see #setTimeout(long)
     */
    public synchronized long getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout value for this task. It is the responsibility of the
     * implementing class to respect this value.
     *
     * @param timeout
     * The time by which the task must complete execution. If the timeout is
     * exceeded, a {@link TimeoutException} will be thrown.
     */
    public synchronized void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Sets the abort flag for this task to <tt>true</tt>. It is the
     * responsibility of the implementing class to respect this value and
     * throw a {@link AbortException}.
     */
    public synchronized void abort() {
        if (taskListener == null) {
            throw new IllegalStateException("Task is not currently pending.");
        }

        abort = true;
    }
}
