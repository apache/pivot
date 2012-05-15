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

/**
 * Alert listener interface.
 */
public interface AlertListener {
    /**
     * Alert listener adapter.
     */
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
     * @param alert
     * @param previousMessageType
     */
    public void messageTypeChanged(Alert alert, MessageType previousMessageType);

    /**
     * Called when an alert's message has changed.
     *
     * @param alert
     * @param previousMessage
     */
    public void messageChanged(Alert alert, String previousMessage);

    /**
     * Called when an alert's body has changed.
     *
     * @param alert
     * @param previousBody
     */
    public void bodyChanged(Alert alert, Component previousBody);

    /**
     * Called when an option has been inserted into an alert's option sequence.
     *
     * @param alert
     * @param index
     */
    public void optionInserted(Alert alert, int index);

    /**
     * Called when options have been removed from an alert's option sequence.
     *
     * @param alert
     * @param index
     * @param removed
     */
    public void optionsRemoved(Alert alert, int index, Sequence<?> removed);

    /**
     * Called when an alert's selected option has changed.
     *
     * @param alert
     * @param previousSelectedOption
     */
    public void selectedOptionChanged(Alert alert, int previousSelectedOption);
}
