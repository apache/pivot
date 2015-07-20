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
 * Utility class for additional Option methods.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class OptionCompanion<T> {
    private static final OptionCompanion INSTANCE = new OptionCompanion<>();

    /**
     * Get the static instance.
     * @return the static instance
     */
    public static final <T> OptionCompanion<T> getInstance() {
        return INSTANCE;
    }

    /**
     * Default constructor, not usable from outside.
     */
    private OptionCompanion() {
        // no-op
    }


    /**
     * Utility method to say if the given argument is an Option.
     *
     * @param val the value to test
     * @return true if it's an Option, false otherwise
     */
    public final boolean isOption(final T val) {
        return val instanceof Option;
    }

    /**
     * Utility method to build an Option, depending on the value given.
     * @param val the value to set
     * @return Some(val) or None(), depending on the given value
     */
    public Option<T> fromValue(final T val) {
        if (val != null) {
            return new Some<>(val);
        }

        return None.getInstance();
    }

    /**
     * Utility method to return the value contained in the given Option.
     * @param o the Option
     * @return the value if any, or null
     */
    public T toValue(final Option<T> o) {
        Objects.requireNonNull(o);

        if (!o.hasValue()) {
            return null;
        }

        return o.getValue();
    }

    /**
     * Utility method to return the value contained in the given Option,
     * or an alternate value if not present.
     * @param o the Option
     * @param alternativeValue the alternative value (null could be used here)
     * @return value if set, otherwise alternativeValue
     */
    public T toValueOrElse(final Option<T> o, final T alternativeValue) {
        Objects.requireNonNull(o);

        return o.getValueOrElse(alternativeValue);
    }

    /**
     * Utility method that tell if the given Option has nested Option instances.
     * @param o the Option to test
     * @return true if it contains at least one nested Option, otherwise false
     */
    public boolean hasNestedOptions(final Option<T> o) {
        Objects.requireNonNull(o);

        if (o instanceof None) {
            return false;
        }

        return o.getValue() instanceof Option;
    }

}
