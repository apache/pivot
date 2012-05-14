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
package org.apache.pivot.wtk;

/**
 * Component data listener interface.
 */
public interface ComponentDataListener {
    /**
     * Component data listener adapter.
     */
    public static class Adapter implements ComponentDataListener {
        @Override
        public void valueAdded(Component component, String key) {
            // empty block
        }

        @Override
        public void valueUpdated(Component component, String key, Object previousValue) {
            // empty block
        }

        @Override
        public void valueRemoved(Component component, String key, Object value) {
            // empty block
        }
    }

    /**
     * Called when a value has been added to a component's user data
     * dictionary.
     *
     * @param component
     * @param key
     */
    public void valueAdded(Component component, String key);

    /**
     * Called when a value has been updated in a component's user data
     * dictionary.
     *
     * @param component
     * @param key
     * @param previousValue
     */
    public void valueUpdated(Component component, String key, Object previousValue);

    /**
     * Called when a value has been removed from a component's user data
     * dictionary.
     *
     * @param component
     * @param key
     * @param value
     */
    public void valueRemoved(Component component, String key, Object value);
}
