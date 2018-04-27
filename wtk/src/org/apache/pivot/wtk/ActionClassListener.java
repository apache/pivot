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
 * Action class listener interface.
 */
public interface ActionClassListener {
    /**
     * Action class listener listeners list.
     */
    public static class Listeners extends ListenerList<ActionClassListener>
        implements ActionClassListener {
        @Override
        public void actionAdded(String id) {
            forEach(listener -> listener.actionAdded(id));
        }

        @Override
        public void actionUpdated(String id, Action previousAction) {
            forEach(listener -> listener.actionUpdated(id, previousAction));
        }

        @Override
        public void actionRemoved(String id, Action action) {
            forEach(listener -> listener.actionRemoved(id, action));
        }
    }

    /**
     * Action class listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public class Adapter implements ActionClassListener {
        @Override
        public void actionAdded(String id) {
            // empty block
        }

        @Override
        public void actionUpdated(String id, Action previousAction) {
            // empty block
        }

        @Override
        public void actionRemoved(String id, Action action) {
            // empty block
        }
    }

    /**
     * Called when an action has been added to the named action dictionary.
     *
     * @param id The identifier for the newly added action.
     */
    default void actionAdded(String id) {
    }

    /**
     * Called when an action has been updated in the named action dictionary.
     *
     * @param id             The identifier for the updated action.
     * @param previousAction The previous {@link Action} associated with this identifier.
     */
    default void actionUpdated(String id, Action previousAction) {
    }

    /**
     * Called when an action has been removed from the named action dictionary.
     *
     * @param id     Identifier for the action that was removed.
     * @param action The removed action.
     */
    default void actionRemoved(String id, Action action) {
    }
}
