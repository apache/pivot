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

/**
 * Definition of a generic Option container, to hold an invariant value (derived from Monad).
 */
public abstract class Option<T> extends Monad<T> {

    /**
     * Default constructor, do not use.
     */
    Option() {
        super(null);
        // throw new IllegalArgumentException("Option must have a value in the constructor");
    }

    /**
     * Constructor with a value to set in the Option
     *
     * @param val the value to set in the Option
     */
    Option(final T val) {
        super(val);
    }

    /**
     * Tell if the value has been set in the Option.
     * @return true if set, otherwise false
     */
    public abstract boolean hasValue();

    /**
     * Return the value contained in the Option.
     * @return the value (if set)
     */
    public abstract T getValue();

    /**
     *
     * Return the value contained in the Option, or an alternative value if not set.
     * @param alternativeValue the value to return as alternative (if value wasn't set in the Option)
     * @return value if set, otherwise alternativeValue
     */
    public T getValueOrElse(final T alternativeValue) {
        return (hasValue() == true) ? getValue() : alternativeValue;
    }

// TODO: check if implement map, mapFlatten, etc ...

}
