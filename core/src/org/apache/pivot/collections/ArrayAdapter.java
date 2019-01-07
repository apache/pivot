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

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.util.Utils;

/**
 * A read-only implementation of the {@link Sequence} interface that wraps an array.
 */
public class ArrayAdapter<T> implements Sequence<T>, Serializable {
    private static final long serialVersionUID = 1143706808122308239L;

    private static final String ERROR_MSG = "An Array Adapter is immutable.";

    private T[] array;

    @SuppressWarnings({ "unchecked" })
    public ArrayAdapter(T... array) {
        Utils.checkNull(array, "array");

        this.array = array;
    }

    @Override
    @UnsupportedOperation
    public int add(T item) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    @UnsupportedOperation
    public void insert(T item, int index) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    @UnsupportedOperation
    public T update(int index, T item) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    @UnsupportedOperation
    public int remove(T item) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    @UnsupportedOperation
    public Sequence<T> remove(int index, int count) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public T get(int index) {
        return array[index];
    }

    @Override
    public int indexOf(T item) {
        for (int index = 0; index < array.length; index++) {
            if ((item == null && array[index] == null) || item.equals(array[index])) {
                return index;
            }
        }

        return -1;
    }

    @Override
    public int getLength() {
        return array.length;
    }
}
