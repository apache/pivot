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

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import org.apache.pivot.util.Utils;

/**
 * Abstract base class for "tasks". A task is an asynchronous operation that may
 * optionally return a value.
 *
 * @param <V> The type of the value returned by the operation. May be
 * {@link Void} to indicate that the task does not return a value.
 */
public abstract class Task<V> {
    /**
     * Task execution callback that is posted to the executor service.
     */
    private class ExecuteCallback implements Runnable {
        @Override
        public void run() {
            V resultLocal = null;
            Throwable faultLocal = null;

            synchronized (Task.this) {
                Task.this.taskThread = new WeakReference<Thread>(Thread.currentThread());
            }

            try {
                resultLocal = execute();
            } catch (Throwable throwable) {
                faultLocal = throwable;
            }

            TaskListener<V> taskListenerLocal;
            synchronized (Task.this) {
                Task.this.result = resultLocal;
                Task.this.fault = faultLocal;

                abort = false;

                taskListenerLocal = Task.this.taskListener;
                Task.this.taskListener = null;
            }

            if (faultLocal == null) {
                taskListenerLocal.taskExecuted(Task.this);
            } else {
                taskListenerLocal.executeFailed(Task.this);
            }
        }
    }

    private ExecutorService executorService;

    private V result = null;
    private Throwable fault = null;
    private TaskListener<V> taskListener = null;
    private WeakReference<Thread> taskThread = null;

    protected volatile long timeout = Long.MAX_VALUE;
    protected volatile boolean abort = false;

    public static final ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    public Task() {
        this(DEFAULT_EXECUTOR_SERVICE);
    }

    public Task(ExecutorService executorService) {
        Utils.checkNull(executorService, "executorService");

        this.executorService = executorService;
    }

    /**
     * Synchronously executes the task.
     *
     * @return The result of the task's execution.
     * @throws TaskExecutionException If an error occurs while executing the
     * task.
     */
    public abstract V execute() throws TaskExecutionException;

    /**
     * Asynchronously executes the task. The caller is notified of the task's
     * completion via the listener argument. Note that the listener will be
     * notified on the task's worker thread, not on the thread that executed the
     * task.
     *
     * @param taskListenerArgument The listener to be notified when the task
     * completes.
     */
    public synchronized void execute(TaskListener<V> taskListenerArgument) {
        execute(taskListenerArgument, executorService);
    }

    /**
     * Asynchronously executes the task. The caller is notified of the task's
     * completion via the listener argument. Note that the listener will be
     * notified on the task's worker thread, not on the thread that executed the
     * task.
     *
     * @param taskListenerArgument The listener to be notified when the task
     * completes.
     * @param executorServiceArgument The service to submit the task to,
     * overriding the Task's own ExecutorService.
     */
    public synchronized void execute(TaskListener<V> taskListenerArgument,
        ExecutorService executorServiceArgument) {
        Utils.checkNull(taskListenerArgument, "taskListener");
        Utils.checkNull(executorServiceArgument, "executorService");

        if (this.taskListener != null) {
            throw new IllegalThreadStateException("Task is already pending.");
        }

        this.taskListener = taskListenerArgument;

        result = null;
        fault = null;
        taskThread = null;
        abort = false;

        // Create a new execute callback and post it to the executor service
        ExecuteCallback executeCallback = new ExecuteCallback();
        executorServiceArgument.submit(executeCallback);
    }

    /**
     * @return The executor service used to execute this task.
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Returns the result of executing the task.
     *
     * @return The task result, or <tt>null</tt> if the task is still executing
     * or has failed. The result itself may also be <tt>null</tt>; callers
     * should call {@link #isPending()} and {@link #getFault()} to distinguish
     * between these cases.
     */
    public synchronized V getResult() {
        return result;
    }

    /**
     * Returns the fault that occurred while executing the task.
     *
     * @return The task fault, or <tt>null</tt> if the task is still executing
     * or has succeeded. Callers should call {@link #isPending()} to distinguish
     * between these cases.
     */
    public synchronized Throwable getFault() {
        return fault;
    }

    /**
     * Returns the thread that was used to execute this task in the background.
     *
     * @return The background thread or <tt>null</tt> if the weak reference was
     * already cleared or if the thread hasn't started yet.
     */
    public synchronized Thread getBackgroundThread() {
        return taskThread == null ? null : taskThread.get();
    }

    /**
     * Returns the pending state of the task.
     *
     * @return <tt>true</tt> if the task is awaiting execution or currently
     * executing; <tt>false</tt>, otherwise.
     */
    public synchronized boolean isPending() {
        return (taskListener != null);
    }

    /**
     * Return the timeout value for this task.
     *
     * @return The timeout value.
     * @see #setTimeout(long)
     */
    public synchronized long getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout value for this task. It is the responsibility of the
     * implementing class to respect this value.
     *
     * @param timeout The time by which the task must complete execution. If the
     * timeout is exceeded, a {@link TimeoutException} will be thrown.
     */
    public synchronized void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Sets the abort flag for this task to <tt>true</tt>. It is the
     * responsibility of the implementing class to respect this value and throw
     * a {@link AbortException}.
     */
    public synchronized void abort() {
        abort = true;
    }
}
