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

import org.apache.pivot.util.Utils;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;

/**
 * Class that forwards task events to the UI thread.
 */
public class TaskAdapter<T> implements TaskListener<T> {
    /** The TaskListener that we're adapting. */
    private TaskListener<T> taskListener;

    /**
     * Creates a new <tt>TaskAdapter</tt> that wraps the specified task listener.
     *
     * @param taskListener The task listener that will be notified on the UI thread.
     */
    public TaskAdapter(TaskListener<T> taskListener) {
        Utils.checkNull(taskListener, "Task listener");

        this.taskListener = taskListener;
    }

    // TaskListener methods

    @Override
    public void taskExecuted(final Task<T> task) {
        ApplicationContext.queueCallback(() -> taskListener.taskExecuted(task));
    }

    @Override
    public void executeFailed(final Task<T> task) {
        ApplicationContext.queueCallback(() -> taskListener.executeFailed(task));
    }
}
