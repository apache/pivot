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

import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;

import org.apache.pivot.util.Utils;

/**
 * Interface representing a set of key/value pairs.
 */
public interface Dictionary<K, V> {
    /**
     * Class representing a key/value pair.
     */
    public static final class Pair<K, V> implements Serializable {
        private static final long serialVersionUID = 5010958035775950649L;

        public final K key;
        public final V value;

        public Pair(K key, V value) {
            Utils.checkNull(key, "key");

            this.key = key;
            this.value = value;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(Object object) {
            boolean equals = false;

            if (object instanceof Pair<?, ?>) {
                Pair<K, V> pair = (Pair<K, V>) object;
                equals = (key.equals(pair.key) && ((value == null && pair.value == null) || (value != null && value.equals(pair.value))));
            }

            return equals;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public String toString() {
            return "{" + key + ": " + value + "}";
        }
    }

    /**
     * Retrieves the value for the given key.
     *
     * @param key The key whose value is to be returned.
     * @return The value corresponding to <tt>key</tt>, or null if the key does
     * not exist. Will also return null if the key refers to a null value. Use
     * <tt>containsKey()</tt> to distinguish between these two cases.
     */
    V get(K key);

    /**
     * Sets the value of the given key, creating a new entry or replacing the
     * existing value.
     *
     * @param key The key whose value is to be set.
     * @param value The value to be associated with the given key.
     * @return The value previously associated with the key.
     */
    V put(K key, V value);

    /**
     * Removes a key/value pair from the map.
     *
     * @param key The key whose mapping is to be removed.
     * @return The value that was removed.
     */
    V remove(K key);

    /**
     * Tests the existence of a key in the dictionary.
     *
     * @param key The key whose presence in the dictionary is to be tested.
     * @return <tt>true</tt> if the key exists in the dictionary; <tt>false</tt>,
     * otherwise.
     */
    boolean containsKey(K key);

    /**
     * Using the other methods in this interface, retrieve an integer value
     * from this dictionary; returning 0 if the key does not exist.
     *
     * @param key The key for the (supposed) <tt>Integer</tt>
     * value to retrieve (actually any {@link Number} will work).
     * @return The integer value, or 0 if the key is not present.
     */
    default int getInt(K key) {
        return getInt(key, 0);
    }

    /**
     * Using the other methods in this interface, retrieve an integer value
     * from this dictionary; returning the given default if the key does not exist.
     *
     * @param key The key for the (supposed) <tt>Integer</tt>
     * value to retrieve (actually any {@link Number} will work).
     * @param defaultValue The value to return if the key is not present.
     * @return The integer value, or the default value if the key is not present.
     */
    default int getInt(K key, int defaultValue) {
        if (containsKey(key)) {
            return ((Number)get(key)).intValue();
        } else {
            return defaultValue;
        }
    }

    /**
     * Using the other methods in this interface, put an integer value
     * into this dictionary.
     *
     * @param key The key for the <tt>Integer</tt> value to save.
     * @param value The int value to be saved.
     * @return The previous value for this key.
     */
    @SuppressWarnings("unchecked")
    default V putInt(K key, int value) {
        return put(key, (V)Integer.valueOf(value));
    }

    /**
     * Using the other methods in this interface, retrieve a boolean value
     * from this dictionary; returning false if the key does not exist.
     *
     * @param key The key for the (supposed) <tt>Boolean</tt>
     * value to retrieve.
     * @return The boolean value, or false if the key is not present.
     */
    default boolean getBoolean(K key) {
        return getBoolean(key, false);
    }

    /**
     * Using the other methods in this interface, retrieve a boolean value
     * from this dictionary; returning a default value if the key does not exist.
     *
     * @param key The key for the (supposed) <tt>Boolean</tt>
     * value to retrieve.
     * @param defaultValue What to return if the key is not present.
     * @return The boolean value, or the default if the key is not present.
     */
    default boolean getBoolean(K key, boolean defaultValue) {
        if (containsKey(key)) {
            return ((Boolean)get(key)).booleanValue();
        } else {
            return defaultValue;
        }
    }

    /**
     * Using the other methods in this interface, put a boolean value
     * into this dictionary.
     *
     * @param key The key for the <tt>Boolean</tt> value to save.
     * @param value The value to be saved.
     * @return The previous value for this key.
     */
    @SuppressWarnings("unchecked")
    default V putBoolean(K key, boolean value) {
        return put(key, (V)Boolean.valueOf(value));
    }

    /**
     * Using the other methods in this interface, retrieve a {@link Color} value
     * from this dictionary; returning <tt>null</tt> if the key does not exist.
     *
     * @param key The key for the (supposed) <tt>Color</tt>
     * value to retrieve.
     * @return The color value, or <tt>null</tt> if the key is not present.
     */
    default Color getColor(K key) {
        if (containsKey(key)) {
            return (Color)get(key);
        } else {
            return (Color)null;
        }
    }

    /**
     * Using the other methods in this interface, retrieve a {@link Font} value
     * from this dictionary; returning <tt>null</tt> if the key does not exist.
     *
     * @param key The key for the (supposed) <tt>Font</tt>
     * value to retrieve.
     * @return The font value, or <tt>null</tt> if the key is not present.
     */
    default Font getFont(K key) {
        if (containsKey(key)) {
            return (Font)get(key);
        } else {
            return (Font)null;
        }
    }

}
