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

import java.util.Objects;

/**
 * Utility class for additional Try methods.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class TryCompanion<T> {
    private static final TryCompanion INSTANCE = new TryCompanion<>();

    /**
     * Get the static instance.
     * @return the static instance
     */
    public static final <T> TryCompanion<T> getInstance() {
        return INSTANCE;
    }

    /**
     * Default constructor, not usable from outside.
     */
    private TryCompanion() {
        // no-op
    }


    /**
     * Utility method to say if the given argument is a Try.
     *
     * @param val the value to test
     * @return true if it's a Try, false otherwise
     */
    public final boolean isTry(final T val) {
        return val instanceof Try;
    }

    /**
     * Utility method to build a Try instance, depending on the value given.
     * @param val the value to set
     * @return Success(val) or Failure(val), depending on the type of the given value
     */
    @SuppressWarnings("unchecked")
    public Try<T> fromValue(final Object val) {
        try {
            // return new Success(val);
            if (val == null || !(val instanceof RuntimeException)) {
                return new Success(val);
            }

            return new Failure((RuntimeException) val);
        } catch (RuntimeException re) {
            return new Failure<>(re);
        }
    }

    /**
     * Utility method to return the value contained.
     * @param t the try instance
     * @return the Success instance containing the value, or the Failure instance containing the exception
     */
    public T toValue(final Try<T> t) {
        Objects.requireNonNull(t);
        return t.getValue();
    }

    /**
     * Utility method to return the value contained,
     * or an alternate value if not present.
     * @param t the Try
     * @param alternativeValue the alternative value (null could be used here)
     * @return value if set, otherwise alternativeValue
     */
    public T toValueOrElse(final Try<T> t, final T alternativeValue) {
        Objects.requireNonNull(t);
        return t.getValueOrElse(alternativeValue);
    }

    /**
     * Utility method that tell if the given Try has nested Try instances.
     * @param t the Try to test
     * @return true if it contains at least one nested Try, otherwise false
     */
    public boolean hasNestedOptions(final Try<T> t) {
        Objects.requireNonNull(t);

        if (t instanceof Failure) {
            return false;
        }

        return t.getValue() instanceof Try;
    }

    /**
     * Utility method to return the value contained in the Try, in a Option instance
     * @param t the Try
     * @return an Option instance: Some(value) if success, None otherwise
     */
    @SuppressWarnings("unchecked")
    public Option<T> toOption(final Try<T> t) {
        Objects.requireNonNull(t);
        if (t.isSuccess()) {
            return (Option<T>) OptionCompanion.getInstance().fromValue(t.getValue());
        }
        return None.getInstance();
    }
}
