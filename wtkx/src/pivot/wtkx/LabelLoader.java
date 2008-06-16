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
import pivot.wtk.Label;

class LabelLoader extends Loader {
    public static final String LABEL_TAG = "Label";
    public static final String TEXT_ATTRIBUTE = "text";
    public static final String TEXT_KEY_ATTRIBUTE = "textKey";

    protected Component load(Element element, ComponentLoader rootLoader)
        throws LoadException {
        Label label = new Label();

        if (element.hasAttribute(TEXT_ATTRIBUTE)) {
            String textAttribute = element.getAttribute(TEXT_ATTRIBUTE);
            label.setText(rootLoader.resolve(textAttribute).toString());
        }

        if (element.hasAttribute(TEXT_KEY_ATTRIBUTE)) {
            String textKey = element.getAttribute(TEXT_KEY_ATTRIBUTE);
            label.setTextKey(textKey);
        }

        return label;
    }
}
