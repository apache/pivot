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
 * Utility class for additional Option methods.
 */
public final class OptionCompanion<T> {
    @SuppressWarnings("rawtypes")
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
        // return new None<>();
        return None.getInstance();
    }

    /**
     * Utility method to return the value contained in the given Option.
     * @param o the Option
     * @return the value (if any)
     */
    public T toValue(final Option<T> o) {
        if (o == null) {
            throw new IllegalArgumentException("option is null.");
        }

        return o.getValue();
        // TODO: make generic (even to handle hasValue, etc) ...
    }

    /**
     * Utility method to return the value contained in the given Option,
     * or an alternate value if not present.
     * @param o the Option
     * @param alternativeValue the alternative value
     * @return value if set, otherwise alternativeValue
     */
    public T toValueOrElse(final Option<T> o, final T alternativeValue) {
        if (o == null) {
            throw new IllegalArgumentException("option is null.");
        }

        return o.getValueOrElse(alternativeValue);
    }

    /**
     * Utility method that tell if the given Option has nested Option instances.
     * @param o the Option to test
     * @return true if it contains at least one nested Option, otherwise false
     */
    public boolean hasNestedOptions(final Option<T> o) {
        if (o == null) {
            throw new IllegalArgumentException("option is null.");
        }

        return o.getValue() instanceof Option;
        // TODO: make generic (even to handle hasValue, etc) ...
    }

/*
// TODO: temp ...
    // flatten the given Option, and return a transformed copy of it
    public Option<T> flatten(final Option<T> o) {
        if (o == null) {
            throw new IllegalArgumentException("option is null.");
        }

        T val = o.getValue();
        Option child;
        boolean valueIsOption = isOption(val);
        if (!isOption(val))
            return fromValue(val);
        // else ...
        child = (Option) val;

int i = 0  // temp
        while (isOption(child) && !child.isNone()) {
            val = child.getValue();
            if (!isOption(val))
                return fromValue(val);
            // else ...
            child = (Option) val;

// temp, to avoid infinite loops with wrong implementation here (during development) ...
if (i > maxDepth) {
    System.err.println("Force loop exiting, infinite loop found ...")
    break
    }
i++
// temp
        }

        val = child.getValue();
        valueIsOption = isOption(val);
        if (valueIsOption) {
            return (Option) val;
        }
        return fromValue(val);
    }

    public Option<T> fromValueFlatten(final T val) {
        if (!isOption(val)) {
            return fromValue(val);
        }
        return flatten((Option) val);
    }

    public T toValueFlatten(final Option<T> o) {
        if (o == null) {
            throw new IllegalArgumentException("option is null.");
        }

        Option flat = flatten(o);
        return flat.getValue();
    }
 */

}
