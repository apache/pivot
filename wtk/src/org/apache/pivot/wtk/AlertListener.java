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

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;

/**
 * Alert listener interface.
 */
public interface AlertListener {
    /**
     * Alert listeners.
     */
    public static class Listeners extends ListenerList<AlertListener> implements AlertListener {
        @Override
        public void messageTypeChanged(Alert alert, MessageType previousMessageType) {
            forEach(listener -> listener.messageTypeChanged(alert, previousMessageType));
        }

        @Override
        public void messageChanged(Alert alert, String previousMessage) {
            forEach(listener -> listener.messageChanged(alert, previousMessage));
        }

        @Override
        public void bodyChanged(Alert alert, Component previousBody) {
            forEach(listener -> listener.bodyChanged(alert, previousBody));
        }

        @Override
        public void optionInserted(Alert alert, int index) {
            forEach(listener -> listener.optionInserted(alert, index));
        }

        @Override
        public void optionsRemoved(Alert alert, int index, Sequence<?> removed) {
            forEach(listener -> listener.optionsRemoved(alert, index, removed));
        }

        @Override
        public void selectedOptionChanged(Alert alert, int previousSelectedOption) {
            forEach(listener -> listener.selectedOptionChanged(alert, previousSelectedOption));
        }
    }

    /**
     * Alert listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements AlertListener {
        @Override
        public void messageTypeChanged(Alert alert, MessageType previousMessageType) {
            // empty block
        }

        @Override
        public void messageChanged(Alert alert, String previousMessage) {
            // empty block
        }

        @Override
        public void bodyChanged(Alert alert, Component previousBody) {
            // empty block
        }

        @Override
        public void optionInserted(Alert alert, int index) {
            // empty block
        }

        @Override
        public void optionsRemoved(Alert alert, int index, Sequence<?> removed) {
            // empty block
        }

        @Override
        public void selectedOptionChanged(Alert alert, int previousSelectedOption) {
            // empty block
        }
    }

    /**
     * Called when an alert's message type has changed.
     *
     * @param alert               The alert that has changed.
     * @param previousMessageType The previous message type for the alert.
     */
    default void messageTypeChanged(Alert alert, MessageType previousMessageType) {
    }

    /**
     * Called when an alert's message has changed.
     *
     * @param alert           The alert that has changed.
     * @param previousMessage The previous message for this alert.
     */
    default void messageChanged(Alert alert, String previousMessage) {
    }

    /**
     * Called when an alert's body has changed.
     *
     * @param alert        The alert that has changed.
     * @param previousBody The previous body for this alert.
     */
    default void bodyChanged(Alert alert, Component previousBody) {
    }

    /**
     * Called when an option has been inserted into an alert's option sequence.
     *
     * @param alert The alert that has changed.
     * @param index The index where the new option was inserted.
     */
    default void optionInserted(Alert alert, int index) {
    }

    /**
     * Called when options have been removed from an alert's option sequence.
     *
     * @param alert    The alert that has changed.
     * @param index    The starting index where the options were removed.
     * @param removed  The actual sequence of the options that were removed.
     */
    default void optionsRemoved(Alert alert, int index, Sequence<?> removed) {
    }

    /**
     * Called when an alert's selected option has changed.
     *
     * @param alert                  The alert that has changed.
     * @param previousSelectedOption The index of the previously selected option.
     */
    default void selectedOptionChanged(Alert alert, int previousSelectedOption) {
    }
}
