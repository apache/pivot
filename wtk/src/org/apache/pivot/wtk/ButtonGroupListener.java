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
 * Button group listener interface.
 */
public interface ButtonGroupListener {
    /**
     * The button group listener listeners list.
     */
    public static class Listeners extends ListenerList<ButtonGroupListener>
        implements ButtonGroupListener {
        @Override
        public void buttonAdded(ButtonGroup buttonGroup, Button button) {
            forEach(listener -> listener.buttonAdded(buttonGroup, button));
        }

        @Override
        public void buttonRemoved(ButtonGroup buttonGroup, Button button) {
            forEach(listener -> listener.buttonRemoved(buttonGroup, button));
        }

        @Override
        public void selectionChanged(ButtonGroup buttonGroup, Button previousSelection) {
            forEach(listener -> listener.selectionChanged(buttonGroup, previousSelection));
        }
    }

    /**
     * Button group listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public class Adapter implements ButtonGroupListener {
        @Override
        public void buttonAdded(ButtonGroup buttonGroup, Button button) {
            // empty block
        }

        @Override
        public void buttonRemoved(ButtonGroup buttonGroup, Button button) {
            // empty block
        }

        @Override
        public void selectionChanged(ButtonGroup buttonGroup, Button previousSelection) {
            // empty block
        }
    }

    /**
     * Called when a button has been added to a button group.
     *
     * @param buttonGroup The button group that has changed.
     * @param button      The button that was added to the group.
     */
    default void buttonAdded(ButtonGroup buttonGroup, Button button) {
    }

    /**
     * Called when a button has been removed from a button group.
     *
     * @param buttonGroup The button group that has changed.
     * @param button      The button that was removed from the group.
     */
    default void buttonRemoved(ButtonGroup buttonGroup, Button button) {
    }

    /**
     * Called when a button group's selection has changed.
     *
     * @param buttonGroup       The button group that changed.
     * @param previousSelection The previously selected button in the group.
     */
    default void selectionChanged(ButtonGroup buttonGroup, Button previousSelection) {
    }
}
