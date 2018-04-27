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

import org.apache.pivot.util.ListenerList;

/**
 * Component data listener interface.
 */
public interface ComponentDataListener {
    /**
     * Component data listeners.
     */
    public static class Listeners extends ListenerList<ComponentDataListener>
        implements ComponentDataListener {
        @Override
        public void valueAdded(Component component, String key) {
            forEach(listener -> listener.valueAdded(component, key));
        }

        @Override
        public void valueUpdated(Component component, String key, Object previousValue) {
            forEach(listener -> listener.valueUpdated(component, key, previousValue));
        }

        @Override
        public void valueRemoved(Component component, String key, Object value) {
            forEach(listener -> listener.valueRemoved(component, key, value));
        }
    }

    /**
     * Component data listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
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
     * Called when a value has been added to a component's user data dictionary.
     *
     * @param component The component that has changed.
     * @param key       The key for the key/value pair that was added to this component's data dictionary.
     */
    default void valueAdded(Component component, String key) {
    }

    /**
     * Called when a value has been updated in a component's user data
     * dictionary.
     *
     * @param component     The component that has changed.
     * @param key           The key for the value that has been updated.
     * @param previousValue The previous value for this key.
     */
    default void valueUpdated(Component component, String key, Object previousValue) {
    }

    /**
     * Called when a value has been removed from a component's user data
     * dictionary.
     *
     * @param component The component that has changed.
     * @param key       The key for the key/value pair that has been removed.
     * @param value     The corresponding value that was removed.
     */
    default void valueRemoved(Component component, String key, Object value) {
    }
}
