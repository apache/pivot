/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtkx;

import org.w3c.dom.Element;
import pivot.wtk.Component;
import pivot.wtk.TextInput;

class TextInputLoader extends Loader {
    public static final String TEXT_INPUT_TAG = "TextInput";
    public static final String MAXIMUM_LENGTH_ATTRIBUTE = "maximumLength";
    public static final String TEXT_ATTRIBUTE = "text";
    public static final String TEXT_SIZE_ATTRIBUTE = "textSize";
    public static final String PASSWORD_ATTRIBUTE = "password";
    public static final String TEXT_KEY_ATTRIBUTE = "textKey";

    @Override
    protected Component load(Element element, ComponentLoader rootLoader)
        throws LoadException {
        TextInput textInput = new TextInput();

        if (element.hasAttribute(MAXIMUM_LENGTH_ATTRIBUTE)) {
            int maximumLength = Integer.parseInt(element.getAttribute(MAXIMUM_LENGTH_ATTRIBUTE));
            textInput.setMaximumLength(maximumLength);
        }

        if (element.hasAttribute(TEXT_ATTRIBUTE)) {
            textInput.setText(element.getAttribute(TEXT_ATTRIBUTE));
        }

        if (element.hasAttribute(TEXT_SIZE_ATTRIBUTE)) {
            int textSize = Integer.parseInt(element.getAttribute(TEXT_SIZE_ATTRIBUTE));
            textInput.setTextSize(textSize);
        }

        if (element.hasAttribute(PASSWORD_ATTRIBUTE)) {
            boolean password = Boolean.parseBoolean(element.getAttribute(PASSWORD_ATTRIBUTE));
            textInput.setPassword(password);
        }

        if (element.hasAttribute(TEXT_KEY_ATTRIBUTE)) {
            String textKey = element.getAttribute(TEXT_KEY_ATTRIBUTE);
            textInput.setTextKey(textKey);
        }

        return textInput;
    }

}
