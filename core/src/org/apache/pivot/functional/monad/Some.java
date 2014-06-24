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
 * Container for an immutable value, derived from Option.
 */
public final class Some<T> extends Option<T> {

    /**
     * Default constructor, do not use here or an IllegalArgumentException will
     * be thrown.
     */
    Some() {
        throw new IllegalArgumentException("Some must have a value in the constructor");
    }

    /**
     * Constructor with a value to set here
     *
     * @param val the value to set
     */
    Some(T val) {
        super(val);
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Some(" + value.toString() + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || other.getClass() != Some.class) {
            return false;
        }
        Some<?> otherAsSome = (Some<?>) other;
        Object otherAsSomeValue = otherAsSome.getValue();
        return value.equals(otherAsSomeValue);
    }

    @Override
    public int hashCode() {
        return 31 * ((value != null) ? value.hashCode() : 0);
    }

}
