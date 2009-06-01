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
        add(item);

        // TODO Fire push event
    }

    public T pop() {
        int length = getLength();
        if (length == 0) {
            throw new IllegalStateException();
        }

        T item = remove(length - 1, 1).get(0);

        // TODO Fire pop event

        return item;
    }

    public T peek() {
        T item = null;
        int length = getLength();
        if (length > 0) {
            item = get(length - 1);
        }

        return item;
    }

    public T poke(T item) {
        int length = getLength();
        if (length == 0) {
            throw new IllegalStateException();
        }

        return update(length - 1, item);
    }
}
