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

import java.util.Iterator;

import pivot.util.ListenerList;

/**
 * Abstract base class for synchronized listener lists.
 *
 * @author gbrown
 */
public abstract class SynchronizedListenerList<T> extends ListenerList<T> {
    public synchronized void add(T listener) {
        super.add(listener);
    }

    public synchronized void remove(T listener) {
        super.remove(listener);
    }

    public synchronized int getCount() {
        return super.getCount();
    }

    /**
     * NOTE Callers must manually synchronize on the SynchronizedListenerList
     * instance to ensure thread safety during iteration.
     */
    public Iterator<T> iterator() {
        return super.iterator();
    }
}
