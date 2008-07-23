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
import pivot.wtk.Orientation;
import pivot.wtk.TabPane;

class TabPaneLoader extends ContainerLoader {
    public static final String TAB_PANE_TAG = "TabPane";
    public static final String TAB_ORIENTATION_ATTRIBUTE = "tabOrientation";
    public static final String COLLAPSIBLE_ATTRIBUTE = "collapsible";
    public static final String SELETED_INDEX_ATTRIBUTE = "selectedIndex";

    public static final String ICON_URL_ATTRIBUTE = "iconURL";
    public static final String LABEL_ATTRIBUTE = "label";

    protected Container createContainer() {
        return new TabPane();
    }

    @Override
    protected Component load(Element element, ComponentLoader rootLoader)
        throws LoadException {
        TabPane tabPane = (TabPane)super.load(element, rootLoader);

        if (element.hasAttribute(TAB_ORIENTATION_ATTRIBUTE)) {
            String tabOrientationAttribute = element.getAttribute(TAB_ORIENTATION_ATTRIBUTE);
            Orientation tabOrientation = Orientation.decode(tabOrientationAttribute);
            tabPane.setTabOrientation(tabOrientation);
        }

        if (element.hasAttribute(COLLAPSIBLE_ATTRIBUTE)) {
            String collapsibleAttribute = element.getAttribute(COLLAPSIBLE_ATTRIBUTE);
            boolean collapsible = Boolean.parseBoolean(collapsibleAttribute);
            tabPane.setCollapsible(collapsible);
        }

        // Add the tabs
        NodeList childNodes = element.getChildNodes();
        int n = childNodes.getLength();

        if (n > 0) {
            ComponentLoader componentLoader = new ComponentLoader();

            for (int i = 0; i < n; i++) {
                Node childNode = childNodes.item(i);

                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element)childNode;
                    Component component = componentLoader.load(childElement, rootLoader);

                    if (childElement.hasAttribute(ICON_URL_ATTRIBUTE)) {
                        String iconURLAttribute = childElement.getAttribute(ICON_URL_ATTRIBUTE);
                        TabPane.setIcon(component, rootLoader.getResource(iconURLAttribute));
                    }

                    if (childElement.hasAttribute(LABEL_ATTRIBUTE)) {
                        TabPane.setLabel(component, rootLoader.resolve(childElement.getAttribute(LABEL_ATTRIBUTE)).toString());
                    }

                    tabPane.getTabs().add(component);
                }
            }
        }

        // Set the selected tab
        int selectedIndex = -1;
        if (element.hasAttribute(SELETED_INDEX_ATTRIBUTE)) {
            selectedIndex = Integer.parseInt(element.getAttribute(SELETED_INDEX_ATTRIBUTE));
        } else {
            if (tabPane.getTabs().getLength() > 0) {
                selectedIndex = 0;
            }
        }

        tabPane.setSelectedIndex(selectedIndex);

        return tabPane;
    }

}
