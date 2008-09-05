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
package pivot.collections;

/**
 * <p>Implementation of the {@link Queue} interface that is backed by an
 * array.</p>
 *
 * <p>TODO The current implementation is not optimal, since it requires shifting
 * all elements on every call to enqueue(). Use an approach that maintains
 * rotating headIndex and tailIndex values and override List methods to use
 * these as offsets from the current capacity value. When the capacity needs
 * to increase, we'll copy the elements in a contiguous block to the new array
 * (we may want to make this operation a protected method so ArrayList can call
 * it polymorphically).</p>
 *
 * @author gbrown
 */
public class ArrayQueue<T> extends ArrayList<T> implements Queue<T> {
    public static final long serialVersionUID = 0;

    public ArrayQueue() {
        super();
    }

    public ArrayQueue(List<T> items) {
        super(items);
    }

    public void enqueue(T item) {
        insert(item, getComparator() == null ? 0 : -1);
    }

    public T dequeue() {
        // TODO Throw if empty
        return remove(getLength() - 1, 1).get(0);
    }

    public T peek() {
        // TODO Return null if empty
        return get(0);
    }
}
