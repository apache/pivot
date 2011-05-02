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
            if (key == null) {
                throw new IllegalArgumentException("key cannot be null.");
            }

            this.key = key;
            this.value = value;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(Object object) {
           boolean equals = false;

           if (object instanceof Pair<?, ?>) {
              Pair<K, V> pair = (Pair<K, V>)object;
              equals = (key.equals(pair.key)
                  && ((value == null && pair.value == null)
                      || (value != null && value.equals(pair.value))));
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
     * @param key
     * The key whose value is to be returned.
     *
     * @return
     * The value corresponding to <tt>key</tt>, or null if the key does not
     * exist. Will also return null if the key refers to a null value.
     * Use <tt>containsKey()</tt> to distinguish between these two cases.
     */
    public V get(K key);

    /**
     * Sets the value of the given key, creating a new entry or replacing the
     * existing value.
     *
     * @param key
     * The key whose value is to be set.
     *
     * @param value
     * The value to be associated with the given key.
     *
     * @return
     * The value previously associated with the key.
     */
    public V put(K key, V value);

    /**
     * Removes a key/value pair from the map.
     *
     * @param key
     * The key whose mapping is to be removed.
     *
     * @return
     * The value that was removed.
     */
    public V remove(K key);

    /**
     * Tests the existence of a key in the dictionary.
     *
     * @param key
     * The key whose presence in the dictionary is to be tested.
     *
     * @return
     * <tt>true</tt> if the key exists in the dictionary; <tt>false</tt>,
     * otherwise.
     */
    public boolean containsKey(K key);
}
