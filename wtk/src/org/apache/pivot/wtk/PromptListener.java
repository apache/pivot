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
 * Prompt listener interface.
 */
public interface PromptListener {
    /**
     * Prompt listener adapter.
     */
    public static class Adapter implements PromptListener {
        @Override
        public void messageTypeChanged(Prompt prompt, MessageType previousMessageType) {
            // empty block
        }

        @Override
        public void messageChanged(Prompt prompt, String previousMessage) {
            // empty block
        }

        @Override
        public void bodyChanged(Prompt prompt, Component previousBody) {
            // empty block
        }

        @Override
        public void optionInserted(Prompt prompt, int index) {
            // empty block
        }

        @Override
        public void optionsRemoved(Prompt prompt, int index, Sequence<?> removed) {
            // empty block
        }

        @Override
        public void selectedOptionChanged(Prompt prompt, int previousSelectedOption) {
            // empty block
        }
    }

    /**
     * Called when a prompt's message type has changed.
     *
     * @param prompt
     * @param previousMessageType
     */
    public void messageTypeChanged(Prompt prompt, MessageType previousMessageType);

    /**
     * Called when a prompt's message has changed.
     *
     * @param prompt
     * @param previousMessage
     */
    public void messageChanged(Prompt prompt, String previousMessage);

    /**
     * Called when a prompt's body has changed.
     *
     * @param prompt
     * @param previousBody
     */
    public void bodyChanged(Prompt prompt, Component previousBody);

    /**
     * Called when an option has been inserted into a prompt's option sequence.
     *
     * @param prompt
     * @param index
     */
    public void optionInserted(Prompt prompt, int index);

    /**
     * Called when options have been removed from a prompt's option sequence.
     *
     * @param prompt
     * @param index
     * @param removed
     */
    public void optionsRemoved(Prompt prompt, int index, Sequence<?> removed);

    /**
     * Called when a prompt's selected option has changed.
     *
     * @param prompt
     * @param previousSelectedOption
     */
    public void selectedOptionChanged(Prompt prompt, int previousSelectedOption);
}
