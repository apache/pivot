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
 * Observable map listener interface.
 */
public interface ObservableMapListener<K, V> {
    /**
     * Observable map listener adapter.
     */
    public static class Adapter<K, V> implements ObservableMapListener<K, V> {
        @Override
        public void valueAdded(ObservableMap<K, V> map, K key) {
        }

        @Override
        public void valueUpdated(ObservableMap<K, V> map, K key, V previousValue) {
        }

        @Override
        public void valueRemoved(ObservableMap<K, V> map, Object key, V value) {
        }
    }

    public void valueAdded(ObservableMap<K, V> map, K key);
    public void valueUpdated(ObservableMap<K, V> map, K key, V previousValue);
    public void valueRemoved(ObservableMap<K, V> map, Object key, V value);
}
