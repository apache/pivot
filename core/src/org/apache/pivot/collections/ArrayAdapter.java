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
package org.apache.pivot.collections;

import java.io.Serializable;

import org.apache.pivot.collections.Sequence;

/**
 * Implementation of the {@link Sequence} interface that wraps an array.
 */
public class ArrayAdapter<T> implements Sequence<T>, Serializable {
    private static final long serialVersionUID = 1143706808122308239L;

    private T[] array;

    public ArrayAdapter(T... array) {
        if (array == null) {
            throw new IllegalArgumentException();
        }

        this.array = array;
    }

    @Override
    public int add(T item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insert(T item, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T update(int index, T item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int remove(T item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Sequence<T> remove(int index, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get(int index) {
        return array[index];
    }

    @Override
    public int indexOf(T item) {
        int index = 0;

        while (index < array.length) {
            if (item == null) {
                if (array[index] == null) {
                    break;
                }
            } else {
                if (item.equals(array[index])) {
                    break;
                }
            }

            index++;
        }

        if (index == array.length) {
            index = -1;
        }

        return index;
    }

    @Override
    public int getLength() {
        return array.length;
    }
}
