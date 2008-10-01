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
 * Task listener interface.
 *
 * @param <V>
 * The return type of the task.
 *
 * @author gbrown
 */
public interface TaskListener<V> {
    /**
     * Called when the task has completed successfully.
     *
     * @param task
     * The source of the task event.
     */
    public void taskExecuted(Task<V> task);

    /**
     * Called when task execution has failed.
     *
     * @param task
     * The source of the task event.
     */
    public void executeFailed(Task<V> task);
}
