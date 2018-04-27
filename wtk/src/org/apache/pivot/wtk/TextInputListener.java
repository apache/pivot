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
import org.apache.pivot.wtk.validation.Validator;

/**
 * Text input listener interface.
 */
public interface TextInputListener {
    /**
     * Text input listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements TextInputListener {
        @Override
        public void textSizeChanged(TextInput textInput, int previousTextSize) {
            // empty block
        }

        @Override
        public void maximumLengthChanged(TextInput textInput, int previousMaximumLength) {
            // empty block
        }

        @Override
        public void passwordChanged(TextInput textInput) {
            // empty block
        }

        @Override
        public void promptChanged(TextInput textInput, String previousPrompt) {
            // empty block
        }

        @Override
        public void textValidatorChanged(TextInput textInput, Validator previousValidator) {
            // empty block
        }

        @Override
        public void strictValidationChanged(TextInput textInput) {
            // empty block
        }

        @Override
        public void textValidChanged(TextInput textInput) {
            // empty block
        }

        @Override
        public void editableChanged(TextInput textInput) {
            // empty block
        }
    }

    /**
     * Text input listener listeners list.
     */
    public static class Listeners extends ListenerList<TextInputListener> implements TextInputListener {
        @Override
        public void textSizeChanged(TextInput textInput, int previousTextSize) {
            forEach(listener -> listener.textSizeChanged(textInput, previousTextSize));
        }

        @Override
        public void maximumLengthChanged(TextInput textInput, int previousMaximumLength) {
            forEach(listener -> listener.maximumLengthChanged(textInput, previousMaximumLength));
        }

        @Override
        public void passwordChanged(TextInput textInput) {
            forEach(listener -> listener.passwordChanged(textInput));
        }

        @Override
        public void promptChanged(TextInput textInput, String previousPrompt) {
            forEach(listener -> listener.promptChanged(textInput, previousPrompt));
        }

        @Override
        public void textValidatorChanged(TextInput textInput, Validator previousValidator) {
            forEach(listener -> listener.textValidatorChanged(textInput, previousValidator));
        }

        @Override
        public void strictValidationChanged(TextInput textInput) {
            forEach(listener -> listener.strictValidationChanged(textInput));
        }

        @Override
        public void textValidChanged(TextInput textInput) {
            forEach(listener -> listener.textValidChanged(textInput));
        }

        @Override
        public void editableChanged(TextInput textInput) {
            forEach(listener -> listener.editableChanged(textInput));
        }
    }

    /**
     * Called when a text input's text size has changed.
     *
     * @param textInput        The source of this event.
     * @param previousTextSize The previous text size for the control
     */
    default void textSizeChanged(TextInput textInput, int previousTextSize) {
    }

    /**
     * Called when a text input's maximum length has changed.
     *
     * @param textInput             The source of this event.
     * @param previousMaximumLength The previous maximum text length.
     */
    default void maximumLengthChanged(TextInput textInput, int previousMaximumLength) {
    }

    /**
     * Called when a text input's password flag has changed.
     *
     * @param textInput The source of this event.
     */
    default void passwordChanged(TextInput textInput) {
    }

    /**
     * Called when a text input's prompt has changed.
     *
     * @param textInput      The source of this event.
     * @param previousPrompt The previous prompt string.
     */
    default void promptChanged(TextInput textInput, String previousPrompt) {
    }

    /**
     * Called when the validator changes.
     *
     * @param textInput         The source of this event.
     * @param previousValidator The previous validator for the text.
     */
    default void textValidatorChanged(TextInput textInput, Validator previousValidator) {
    }

    /**
     * Called when a text input's strict validation flag has changed.
     *
     * @param textInput The text input that has changed.
     */
    default void strictValidationChanged(TextInput textInput) {
    }

    /**
     * Called when the text changes validity.
     *
     * @param textInput The text input that has been changed.
     */
    default void textValidChanged(TextInput textInput) {
    }

    /**
     * Called when the editable state has changed.
     *
     * @param textInput The text input whose state has changed.
     */
    default void editableChanged(TextInput textInput) {
    }

}
