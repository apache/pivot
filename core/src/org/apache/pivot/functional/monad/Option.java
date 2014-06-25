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
    final T value;

    /**
     * Default constructor, do not use because it set null as invariant value to hold.
     */
    Option() {
        this(null);
    }

    /**
     * Constructor with a value to set in the Option
     *
     * @param val the value to set in the Option
     */
    Option(final T val) {
        this.value = val;
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

    @Override
    public String toString() {
        return "Monad(" + ((value != null) ? value.toString() : "null") + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Option)) {
            return false;
        }
        Option other = (Option) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

// TODO: check if implement map, flatten, etc ...

}
