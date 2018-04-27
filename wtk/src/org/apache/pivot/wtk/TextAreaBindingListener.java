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
 * Text area binding listener interface.
 */
public interface TextAreaBindingListener {
    /**
     * Text area binding listeners.
     */
    public static class Listeners extends ListenerList<TextAreaBindingListener>
        implements TextAreaBindingListener {
        @Override
        public void textKeyChanged(TextArea textArea, String previousTextKey) {
            forEach(listener -> listener.textKeyChanged(textArea, previousTextKey));
        }

        @Override
        public void textBindTypeChanged(TextArea textArea, BindType previousTextBindType) {
            forEach(listener -> listener.textBindTypeChanged(textArea, previousTextBindType));
        }

        @Override
        public void textBindMappingChanged(TextArea textArea,
            TextArea.TextBindMapping previousTextBindMapping) {
            forEach(listener -> listener.textBindMappingChanged(textArea, previousTextBindMapping));
        }
    }

    /**
     * Text area binding listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements TextAreaBindingListener {
        @Override
        public void textKeyChanged(TextArea textArea, String previousTextKey) {
            // empty block
        }

        @Override
        public void textBindTypeChanged(TextArea textArea, BindType previousTextBindType) {
            // empty block
        }

        @Override
        public void textBindMappingChanged(TextArea textArea,
            TextArea.TextBindMapping previousTextBindMapping) {
            // empty block
        }
    }

    /**
     * Called when a text area's text key has changed.
     *
     * @param textArea The component that has changed.
     * @param previousTextKey What the text key used to be for this component.
     */
    default void textKeyChanged(TextArea textArea, String previousTextKey) {
    }

    /**
     * Called when a text area's text bind type has changed.
     *
     * @param textArea The source of this event.
     * @param previousTextBindType The previous bind type for this component.
     */
    default void textBindTypeChanged(TextArea textArea, BindType previousTextBindType) {
    }

    /**
     * Called when a text area's text bind mapping has changed.
     *
     * @param textArea The source of this event.
     * @param previousTextBindMapping The previous bind mapping for this component.
     */
    default void textBindMappingChanged(TextArea textArea,
        TextArea.TextBindMapping previousTextBindMapping) {
    }
}
