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

import pivot.wtk.Container;
import pivot.wtk.Expander;
import pivot.wtk.Component;

class ExpanderLoader extends ContainerLoader {
    public static final String EXPANDER_TAG = "Expander";
    public static final String TITLE_ATTRIBUTE = "title";
    public static final String EXPANDED_ATTRIBUTE = "expanded";

    protected Container createContainer() {
        return new Expander();
    }

    @Override
    protected Component load(Element element, ComponentLoader rootLoader)
        throws LoadException {
        Expander expander = (Expander)super.load(element, rootLoader);

        if (element.hasAttribute(TITLE_ATTRIBUTE)) {
            expander.setTitle(element.getAttribute(TITLE_ATTRIBUTE));
        }

        if (element.hasAttribute(EXPANDED_ATTRIBUTE)) {
            String expandedAttribute = element.getAttribute(EXPANDED_ATTRIBUTE);
            boolean expanded = Boolean.parseBoolean(expandedAttribute);
            expander.setExpanded(expanded);
        }

        NodeList childNodes = element.getChildNodes();
        int n = childNodes.getLength();

        if (n > 0) {
            ComponentLoader componentLoader = new ComponentLoader();

            for (int i = 0; i < n; i++) {
                Node childNode = childNodes.item(i);

                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    Component component = componentLoader.load((Element)childNode, rootLoader);
                    expander.setContent(component);
                    break;
                }
            }
        }

        return expander;
    }
}
