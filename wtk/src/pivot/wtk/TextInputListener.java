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
package pivot.wtk;

import pivot.wtk.text.TextNode;
import pivot.wtk.text.validation.Validator;

/**
 * Text input listener interface.
 *
 * @author gbrown
 */
public interface TextInputListener {
    /**
     * Adapts the <tt>TextInputListener</tt> interface.
     *
     * @author tvolkert
     */
    public static class Adapter implements TextInputListener {
        public void textNodeChanged(TextInput textInput, TextNode previousTextNode) {
        }

        public void textSizeChanged(TextInput textInput, int previousTextSize) {
        }

        public void maximumLengthChanged(TextInput textInput, int previousMaximumLength) {
        }

        public void passwordChanged(TextInput textInput) {
        }

        public void promptChanged(TextInput textInput, String previousPrompt) {
        }

        public void textKeyChanged(TextInput textInput, String previousTextKey) {
        }

        public void textValidChanged(TextInput textInput) {
        }

        public void textValidatorChanged(TextInput textInput, Validator previousValidator) {
        }
    }

    /**
     * Called when a text input's text node has changed.
     * @param textInput
     * @param previousTextNode
     */
    public void textNodeChanged(TextInput textInput, TextNode previousTextNode);

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
     * Called when a text input's text key has changed.
     *
     * @param textInput
     * @param previousTextKey
     */
    public void textKeyChanged(TextInput textInput, String previousTextKey);

    /**
     * Called when the text changes validity.
     *
     * @param textInput
     */
    public void textValidChanged(TextInput textInput);

    /**
     * Called when the validator changes.
     *
     * @param textInput
     * @param previousValidator
     */
    public void textValidatorChanged(TextInput textInput, Validator previousValidator);
}
