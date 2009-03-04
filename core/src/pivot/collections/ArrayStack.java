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
 * Implementation of the {@link Stack} interface that is backed by an
 * array.
 *
 * @author gbrown
 */
public class ArrayStack<T> extends ArrayList<T> implements Stack<T> {
    private static final long serialVersionUID = 0;

    public ArrayStack() {
        super();
    }

    public ArrayStack(List<T> items) {
        super(items);
    }

    public void push(T item) {
        insert(item, getComparator() == null ? getLength() : -1);
    }

    public T pop() {
        int length = getLength();
        if (length == 0) {
            throw new IllegalStateException();
        }

        return remove(length - 1, 1).get(0);
    }

    public T peek() {
        int length = getLength();

        return (length == 0) ? null : get(length - 1);
    }

    public T poke(T item) {
        int length = getLength();
        if (length == 0) {
            throw new IllegalStateException();
        }

        return update(length - 1, item);
    }

    public int getRemainingCapacity() {
        return -1;
    }
}
