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

import pivot.collections.Sequence;

/**
 * <p>Class that runs a sequence of tasks in series and notifies listeners
 * when all tasks are complete. Callers can retrieve task results or faults by
 * calling {@link Task#getResult()} and {@link Task#getFault()},
 * respectively.</p>
 *
 * <p>TODO This class is currently incomplete.</p>
 *
 * @author gbrown
 */
public class TaskSequence<V> extends Task<Void> implements Sequence<Task<? extends V>> {
    public TaskSequence(Dispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public Void execute() {
        // TODO Execute all tasks using this task's dispatcher
        return null;
    }

    public int add(Task<? extends V> task) {
        // TODO
        return 0;
    }

    public void insert(Task<? extends V> task, int index) {
        // TODO Auto-generated method stub
    }

    public int remove(Task<? extends V> task) {
        // TODO
        return -1;
    }

    public Sequence<Task<? extends V>> remove(int index, int count) {
        // TODO Auto-generated method stub
        return null;
    }

    public Task<V> get(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    public Task<V> update(int index, Task<? extends V> task) {
        // TODO Auto-generated method stub
        return null;
    }

    public int indexOf(Task<? extends V> task) {
        // TODO
        return -1;
    }

    public int getLength() {
        // TODO Auto-generated method stub
        return 0;
    }
}
