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

import pivot.wtk.Border;
import pivot.wtk.Component;
import pivot.wtk.Container;

class BorderLoader extends ContainerLoader {
    public static final String BORDER_TAG = "Border";
    public static final String TITLE_ATTRIBUTE = "title";

    protected Container createContainer() {
        return new Border();
    }

    @Override
    protected Component load(Element element, ComponentLoader rootLoader)
        throws LoadException {
        Border border = (Border)super.load(element, rootLoader);

        if (element.hasAttribute(TITLE_ATTRIBUTE)) {
            border.setTitle(element.getAttribute(TITLE_ATTRIBUTE));
        }

        NodeList childNodes = element.getChildNodes();
        int n = childNodes.getLength();

        if (n > 0) {
            ComponentLoader componentLoader = new ComponentLoader();

            for (int i = 0; i < n; i++) {
                Node childNode = childNodes.item(i);

                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    Component component = componentLoader.load((Element)childNode, rootLoader);
                    border.setContent(component);
                    break;
                }
            }
        }

        return border;
    }
}
