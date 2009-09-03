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
package org.apache.pivot.wtk;

import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;

/**
 * Class that forwards task events to the UI thread.
 */
public class TaskAdapter<T> implements TaskListener<T> {
    /**
     * Callback that gets posted to the UI thread when our task has been
     * executed.
     */
    private class TaskExecutedCallback implements Runnable {
        private Task<T> task;

        public TaskExecutedCallback(Task<T> task) {
            this.task = task;
        }

        @Override
        public void run() {
            taskListener.taskExecuted(task);
        }
    }

    /**
     * Callback that gets posted to the UI thread when our task execution has
     * failed.
     */
    private class ExecuteFailedCallback implements Runnable {
        private Task<T> task;

        public ExecuteFailedCallback(Task<T> task) {
            this.task = task;
        }

        @Override
        public void run() {
            taskListener.executeFailed(task);
        }
    }


    // The TaskListener that we're adapting
    private TaskListener<T> taskListener;


    /**
     * Creates a new <tt>TaskAdapter</tt> that wraps the specified task
     * listener.
     *
     * @param taskListener
     * The task listener that will be notified on the UI thread
     */
    public TaskAdapter(TaskListener<T> taskListener) {
        if (taskListener == null) {
            throw new IllegalArgumentException("taskListener cannot be null");
        }

        this.taskListener = taskListener;
    }

    // TaskListener methods

    @Override
    public void taskExecuted(Task<T> task) {
        ApplicationContext.queueCallback(new TaskExecutedCallback(task));
    }

    @Override
    public void executeFailed(Task<T> task) {
        ApplicationContext.queueCallback(new ExecuteFailedCallback(task));
    }
}
