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

import org.apache.pivot.util.Utils;

/**
 * A read-only implementation of the {@link Sequence} interface that wraps an array.
 * <p> Used to interface between a Java array and a Pivot control that expects a
 * {@link Sequence} of objects (such as a {@code TablePane}, {@code Menu}, {@code ListView}
 * or similar).
 *
 * @param <T> The underlying type of the array objects.
 */
public class ArrayAdapter<T> extends ReadOnlySequence<T> implements Serializable {
    private static final long serialVersionUID = 1143706808122308239L;

    private T[] array;

    @SuppressWarnings({ "unchecked" })
    public ArrayAdapter(final T... array) {
        Utils.checkNull(array, "array");

        this.array = array;
    }

    @Override
    public T get(final int index) {
        return array[index];
    }

    @Override
    public int indexOf(final T item) {
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
