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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pivot.wtk.Alert;
import pivot.wtk.Component;
import pivot.wtk.Container;
import pivot.wtk.Form;

class FormLoader extends ContainerLoader {
    public static final String FORM_TAG = "Form";

    public static final String LABEL_ATTRIBUTE = "label";
    public static final String FLAG_TYPE_ATTRIBUTE = "flagType";
    public static final String FLAG_MESSAGE_ATTRIBUTE = "flagMessage";

    protected Container createContainer() {
        return new Form();
    }

    @Override
    protected Component load(Element element, ComponentLoader rootLoader)
        throws LoadException {
        Form form = (Form)super.load(element, rootLoader);

        NodeList childNodes = element.getChildNodes();
        int n = childNodes.getLength();

        if (n > 0) {
            ComponentLoader componentLoader = new ComponentLoader();

            for (int i = 0; i < n; i++) {
                Node childNode = childNodes.item(i);

                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element)childNode;
                    Component component = componentLoader.load(childElement, rootLoader);
                    form.getFields().add(component);

                    if (childElement.hasAttribute(LABEL_ATTRIBUTE)) {
                        String labelAttribute = childElement.getAttribute(LABEL_ATTRIBUTE);
                        Form.setLabel(component, rootLoader.resolve(labelAttribute).toString());
                    }

                    if (childElement.hasAttribute(FLAG_TYPE_ATTRIBUTE)) {
                        String flagTypeAttribute = childElement.getAttribute(FLAG_TYPE_ATTRIBUTE);
                        Alert.Type flagType = Alert.Type.decode(flagTypeAttribute);

                        String flagMessage = null;
                        if (childElement.hasAttribute(FLAG_MESSAGE_ATTRIBUTE)) {
                            flagMessage = childElement.getAttribute(FLAG_MESSAGE_ATTRIBUTE);
                        }

                        Form.Flag flag = new Form.Flag(flagType, flagMessage);
                        Form.setFlag(component, flag);
                    }
                }
            }
        }

        return form;
    }

}
