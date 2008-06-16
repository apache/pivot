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
import pivot.wtk.Component;
import pivot.wtk.Container;
import pivot.wtk.ScrollPane;

class ScrollPaneLoader extends ContainerLoader {
    public static final String SCROLL_PANE_TAG = "ScrollPane";
    public static final String VIEW_TAG = "view";
    public static final String ROW_HEADER_TAG = "rowHeader";
    public static final String COLUMN_HEADER_TAG = "columnHeader";
    public static final String CORNER_TAG = "corner";

    public static final String HORIZONTAL_SCROLL_BAR_POLICY_ATTRIBUTE = "horizontalScrollBarPolicy";
    public static final String VERTICAL_SCROLL_BAR_POLICY_ATTRIBUTE = "verticalScrollBarPolicy";

    protected Container createContainer() {
        return new ScrollPane();
    }

    protected Component load(Element element, ComponentLoader rootLoader)
        throws LoadException {
        ScrollPane scrollPane = (ScrollPane)super.load(element, rootLoader);

        ScrollPane.ScrollBarPolicy horizontalScrollBarPolicy = ScrollPane.ScrollBarPolicy.AUTO;
        if (element.hasAttribute(HORIZONTAL_SCROLL_BAR_POLICY_ATTRIBUTE)) {
            String horizontalScrollBarPolicyAttribute =
                element.getAttribute(HORIZONTAL_SCROLL_BAR_POLICY_ATTRIBUTE);
            horizontalScrollBarPolicy =
                ScrollPane.ScrollBarPolicy.decode(horizontalScrollBarPolicyAttribute);
        }

        scrollPane.setHorizontalPolicy(horizontalScrollBarPolicy);

        ScrollPane.ScrollBarPolicy verticalScrollBarPolicy = ScrollPane.ScrollBarPolicy.AUTO;
        if (element.hasAttribute(VERTICAL_SCROLL_BAR_POLICY_ATTRIBUTE)) {
            String verticalScrollBarPolicyAttribute =
                element.getAttribute(VERTICAL_SCROLL_BAR_POLICY_ATTRIBUTE);
            verticalScrollBarPolicy =
                ScrollPane.ScrollBarPolicy.decode(verticalScrollBarPolicyAttribute);
        }

        scrollPane.setVerticalPolicy(verticalScrollBarPolicy);

        NodeList childNodes = element.getChildNodes();
        int n = childNodes.getLength();

        if (n > 0) {
            ComponentLoader componentLoader = new ComponentLoader();

            for (int i = 0; i < n; i++) {
                Node childNode = childNodes.item(i);

                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element)childNode;
                    String childTagName = childElement.getTagName();

                    Component component = null;

                    Element firstChildElement = getFirstChildElement(childElement);
                    if (firstChildElement != null) {
                       component = componentLoader.load(firstChildElement, rootLoader);
                    }

                    if (childTagName.equals(VIEW_TAG)) {
                        scrollPane.setView(component);
                    } else if (childTagName.equals(ROW_HEADER_TAG)) {
                        scrollPane.setRowHeader(component);
                    } else if (childTagName.equals(COLUMN_HEADER_TAG)) {
                        scrollPane.setColumnHeader(component);
                    } else if (childTagName.equals(CORNER_TAG)) {
                        scrollPane.setCorner(component);
                    }
                }
            }
        }

        return scrollPane;
    }

    private Element getFirstChildElement(Element element) {
        Element firstChildElement = null;

        NodeList childNodes = element.getChildNodes();

        for (int i = 0, n = childNodes.getLength(); i < n; i++) {
            Node childNode = childNodes.item(i);

            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                firstChildElement = (Element)childNode;
                break;
            }
        }

        return firstChildElement;
    }
}
