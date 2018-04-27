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
 * Text input binding listener interface.
 */
public interface TextInputBindingListener {
    /**
     * Text input binding listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements TextInputBindingListener {
        @Override
        public void textKeyChanged(TextInput textInput, String previousTextKey) {
            // empty block
        }

        @Override
        public void textBindTypeChanged(TextInput textInput, BindType previousTextBindType) {
            // empty block
        }

        @Override
        public void textBindMappingChanged(TextInput textInput,
            TextInput.TextBindMapping previousTextBindMapping) {
            // empty block
        }
    }

    /**
     * Text input binding listener listeners list.
     */
    public static class Listeners extends ListenerList<TextInputBindingListener>
            implements TextInputBindingListener {
        @Override
        public void textKeyChanged(TextInput textInput, String previousTextKey) {
            forEach(listener -> listener.textKeyChanged(textInput, previousTextKey));
        }

        @Override
        public void textBindTypeChanged(TextInput textInput, BindType previousTextBindType) {
            forEach(listener -> listener.textBindTypeChanged(textInput, previousTextBindType));
        }

        @Override
        public void textBindMappingChanged(TextInput textInput,
            TextInput.TextBindMapping previousTextBindMapping) {
            forEach(listener -> listener.textBindMappingChanged(textInput, previousTextBindMapping));
        }
    }

    /**
     * Called when a text input's text key has changed.
     *
     * @param textInput The source of this event.
     * @param previousTextKey The previous text key for the component.
     */
    default void textKeyChanged(TextInput textInput, String previousTextKey) {
    }

    /**
     * Called when a text input's text bind type has changed.
     *
     * @param textInput The source of this event.
     * @param previousTextBindType The previous bind type for this component.
     */
    default void textBindTypeChanged(TextInput textInput, BindType previousTextBindType) {
    }

    /**
     * Called when a text input's text bind mapping has changed.
     *
     * @param textInput The source of this event.
     * @param previousTextBindMapping The previous bind mapping for this component.
     */
    default void textBindMappingChanged(TextInput textInput,
        TextInput.TextBindMapping previousTextBindMapping) {
    }
}
