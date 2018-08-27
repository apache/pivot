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
package org.apache.pivot.util;

/**
 * Utility class for dealing with classes.
 */
public final class ClassUtils {
    public static final String UNKNOWN_CALLER = "<unknown caller>";

    private ClassUtils() {
    }

    /**
     * Return the description (name, location) of a caller of this method.
     *
     * @param       level   0 = the caller of this method, 1 = its caller, etc.
     * @return      The {@link #UNKNOWN_CALLER} string if the level is
     *              out of range or a formatted string that describes the
     *              caller at the given level.
     * @throws      IllegalArgumentException if the level value is negative.
     */
    public static String getCallingMethod(final int level) {
        Utils.checkNonNegative(level, "level");

        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        // level + 2 because 0 = inside "getStackTrace", 1 = inside here, so
        // 2 is the caller of this method, etc.
        if (elements == null || level + 2 >= elements.length) {
            return UNKNOWN_CALLER;
        }
        return elements[level + 2].toString();
    }

    /**
     * Get the default value of {@link Object#toString} for any given object.
     *
     * @param obj Any object.
     * @return The result of what {@link Object#toString} would return without
     * any alternative implementation of <tt>toString()</tt> that may be implemented
     * in the class or any intervening superclass.
     */
    public static String defaultToString(final Object obj) {
        return obj.getClass().getName() + "@"
            + Integer.toHexString(System.identityHashCode(obj));
    }

    /**
     * Get the (simple) default value of {@link Object#toString} for any given object.
     *
     * @param obj Any object.
     * @return The result of what {@link Object#toString} would return without
     * any alternative implementation of <tt>toString()</tt> that may be implemented
     * in the class or any intervening superclass, except that the simple name
     * of the class is used (without any package designation).
     */
    public static String simpleToString(final Object obj) {
        return obj.getClass().getSimpleName() + "@"
            + Integer.toHexString(System.identityHashCode(obj));
    }

    /**
     * Multi-class version of the <tt>instanceof</tt> operator.
     * <p> Specifically checks if the given object is an instance of any of the
     * given classes.
     * @param obj The (non-null) object to test.
     * @param classes The list of classes.
     * @return <tt>true</tt> if the object is non-null and an instance of ANY of
     * the given classes, <tt>false</tt> otherwise.
     */
    public static boolean instanceOf(final Object obj, final Class<?>... classes) {
        for (Class<?> cls : classes) {
            if (cls.isInstance(obj)) {
                return true;
            }
        }
        return false;
    }

}
