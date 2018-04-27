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
 * Button listener interface.
 */
public interface ButtonListener {
    /**
     * Button listeners.
     */
    public static class Listeners extends ListenerList<ButtonListener> implements ButtonListener {
        @Override
        public void buttonDataChanged(Button button, Object previousButtonData) {
            forEach(listener -> listener.buttonDataChanged(button, previousButtonData));
        }

        @Override
        public void dataRendererChanged(Button button, Button.DataRenderer previousDataRenderer) {
            forEach(listener -> listener.dataRendererChanged(button, previousDataRenderer));
        }

        @Override
        public void actionChanged(Button button, Action previousAction) {
            forEach(listener -> listener.actionChanged(button, previousAction));
        }

        @Override
        public void toggleButtonChanged(Button button) {
            forEach(listener -> listener.toggleButtonChanged(button));
        }

        @Override
        public void triStateChanged(Button button) {
            forEach(listener -> listener.triStateChanged(button));
        }

        @Override
        public void buttonGroupChanged(Button button, ButtonGroup previousButtonGroup) {
            forEach(listener -> listener.buttonGroupChanged(button, previousButtonGroup));
        }
    }

    /**
     * Button listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements ButtonListener {
        @Override
        public void buttonDataChanged(Button button, Object previousButtonData) {
            // empty block
        }

        @Override
        public void dataRendererChanged(Button button, Button.DataRenderer previousDataRenderer) {
            // empty block
        }

        @Override
        public void actionChanged(Button button, Action previousAction) {
            // empty block
        }

        @Override
        public void toggleButtonChanged(Button button) {
            // empty block
        }

        @Override
        public void triStateChanged(Button button) {
            // empty block
        }

        @Override
        public void buttonGroupChanged(Button button, ButtonGroup previousButtonGroup) {
            // empty block
        }
    }

    /**
     * Called when a button's data has changed.
     *
     * @param button             The button that changed.
     * @param previousButtonData The previous value of the button data.
     */
    default void buttonDataChanged(Button button, Object previousButtonData) {
    }

    /**
     * Called when a button's data renderer has changed.
     *
     * @param button               The button that changed.
     * @param previousDataRenderer The previous data renderer for the button.
     */
    default void dataRendererChanged(Button button, Button.DataRenderer previousDataRenderer) {
    }

    /**
     * Called when a button's action has changed.
     *
     * @param button         The button that changed.
     * @param previousAction The previous action that was assigned to the button.
     */
    default void actionChanged(Button button, Action previousAction) {
    }

    /**
     * Called when a button's toggle button flag has changed.
     *
     * @param button The button that changed.
     */
    default void toggleButtonChanged(Button button) {
    }

    /**
     * Called when a button's tri-state flag has changed.
     *
     * @param button The button that changed.
     */
    default void triStateChanged(Button button) {
    }

    /**
     * Called when a button's button group has changed.
     *
     * @param button              The button whose group changed.
     * @param previousButtonGroup The button group the button used to belong to (can be {@code null}).
     */
    default void buttonGroupChanged(Button button, ButtonGroup previousButtonGroup) {
    }
}
