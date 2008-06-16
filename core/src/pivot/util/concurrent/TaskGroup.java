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

/**
 * TODO Class that runs a group of tasks in parallel and notifies listeners
 * when all tasks are complete.
 *
 * NOTE Callers can retrieve task results or faults by calling
 * {@link Task#getResult()} and {@link Task#getFault()}, respectively.
 *
 * @author gbrown
 */
public class TaskGroup<V> extends Task<Void> implements Group<Task<? extends V>> {
    public TaskGroup(Dispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public Void execute() {
        // TODO Execute all tasks using this task's dispatcher
        return null;
    }

    public void add(Task<? extends V> element) {
        // TODO Auto-generated method stub

    }

    public void remove(Task<? extends V> element) {
        // TODO Auto-generated method stub

    }

    public boolean contains(Task<? extends V> element) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }
}
