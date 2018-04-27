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
 * Prompt listener interface.
 */
public interface PromptListener {
    /**
     * Prompt listeners.
     */
    public static class Listeners extends ListenerList<PromptListener> implements PromptListener {
        @Override
        public void messageTypeChanged(Prompt prompt, MessageType previousMessageType) {
            forEach(listener -> listener.messageTypeChanged(prompt, previousMessageType));
        }

        @Override
        public void messageChanged(Prompt prompt, String previousMessage) {
            forEach(listener -> listener.messageChanged(prompt, previousMessage));
        }

        @Override
        public void bodyChanged(Prompt prompt, Component previousBody) {
            forEach(listener -> listener.bodyChanged(prompt, previousBody));
        }

        @Override
        public void optionInserted(Prompt prompt, int index) {
            forEach(listener -> listener.optionInserted(prompt, index));
        }

        @Override
        public void optionsRemoved(Prompt prompt, int index, Sequence<?> removed) {
            forEach(listener -> listener.optionsRemoved(prompt, index, removed));
        }

        @Override
        public void selectedOptionChanged(Prompt prompt, int previousSelectedOption) {
            forEach(listener -> listener.selectedOptionChanged(prompt, previousSelectedOption));
        }
    }

    /**
     * Prompt listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
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
     * @param prompt The prompt object that has been changed.
     * @param previousMessageType The message type before the change.
     */
    default void messageTypeChanged(Prompt prompt, MessageType previousMessageType) {
    }

    /**
     * Called when a prompt's message has changed.
     *
     * @param prompt The prompt whose message has changed.
     * @param previousMessage What the message used to be.
     */
    default void messageChanged(Prompt prompt, String previousMessage) {
    }

    /**
     * Called when a prompt's body has changed.
     *
     * @param prompt The prompt that has changed.
     * @param previousBody What the body of this prompt used to be.
     */
    default void bodyChanged(Prompt prompt, Component previousBody) {
    }

    /**
     * Called when an option has been inserted into a prompt's option sequence.
     *
     * @param prompt The prompt whose options have changed.
     * @param index The location where the new option was inserted.
     */
    default void optionInserted(Prompt prompt, int index) {
    }

    /**
     * Called when options have been removed from a prompt's option sequence.
     *
     * @param prompt The prompt whose options have changed.
     * @param index The starting location of the removed options.
     * @param removed The actual sequence of options removed.
     */
    default void optionsRemoved(Prompt prompt, int index, Sequence<?> removed) {
    }

    /**
     * Called when a prompt's selected option has changed.
     *
     * @param prompt The prompt that changed.
     * @param previousSelectedOption The option that used to be the selected one.
     */
    default void selectedOptionChanged(Prompt prompt, int previousSelectedOption) {
    }
}
