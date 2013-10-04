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

import org.apache.pivot.wtk.validation.Validator;

/**
 * Text input listener interface.
 */
public interface TextInputListener {
    /**
     * Text input listener adapter.
     */
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
     * Called when a text input's text size has changed.
     *
     * @param textInput
     * @param previousTextSize
     */
    public void textSizeChanged(TextInput textInput, int previousTextSize);

    /**
     * Called when a text input's maximum length has changed.
     *
     * @param textInput
     * @param previousMaximumLength
     */
    public void maximumLengthChanged(TextInput textInput, int previousMaximumLength);

    /**
     * Called when a text input's password flag has changed.
     *
     * @param textInput
     */
    public void passwordChanged(TextInput textInput);

    /**
     * Called when a text input's prompt has changed.
     *
     * @param textInput
     * @param previousPrompt
     */
    public void promptChanged(TextInput textInput, String previousPrompt);

    /**
     * Called when the validator changes.
     *
     * @param textInput
     * @param previousValidator
     */
    public void textValidatorChanged(TextInput textInput, Validator previousValidator);

    /**
     * Called when a text input's strict validation flag has changed.
     *
     * @param textInput
     */
    public void strictValidationChanged(TextInput textInput);

    /**
     * Called when the text changes validity.
     *
     * @param textInput
     */
    public void textValidChanged(TextInput textInput);

    /**
     * Called when the editable state has changed.
     *
     * @param textInput
     */
    public void editableChanged(TextInput textInput);

}
