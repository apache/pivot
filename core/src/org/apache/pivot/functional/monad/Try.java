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
package org.apache.pivot.functional.monad;

import java.util.Iterator;

/**
 * Definition of a generic Monad.
 */
public abstract class Try<T> extends Monad<T> implements Iterable<T> {

    /** Default constructor */
    protected Try() {
        // no-op
    }

    /**
     * Tell if it contains a successful value.
     * @return true if it is s Success, otherwise false (it's a Failure)
     */
    public abstract boolean isSuccess();

    /**
     * Return the value contained.
     * @return the value (if set)
     */
    public abstract T getValue();

    /**
     * Return the value contained in the Try if it is a successful value, or an alternative value.
     * @param alternativeValue the value to return as alternative
     * @return value if it is a successful value, otherwise alternativeValue
     */
    public T getValueOrElse(final T alternativeValue) {
        return (isSuccess() == true) ? getValue() : alternativeValue;
    }

    /**
     * Return the value contained in the Try, or null if it hasn't a value set.
     * @return value if it is a successful value, otherwise null
     */
    public T getValueOrNull() {
        return getValueOrElse(null);
    }

    @Override
    public String toString() {
        return "Try()";
    }

    /**
     * Return an Iterator
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<T> iterator() {
        return new TryIterator();
    }


    /**
     * Immutable iterator on the value contained in the Try (if any).
     */
    private class TryIterator implements Iterator<T> {
        private int cursor = 0;

        @Override
        public boolean hasNext() {
            return (isSuccess() && cursor == 0);
        }

        @Override
        public T next() {
            cursor++;
            return getValue();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
