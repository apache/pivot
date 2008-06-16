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
import pivot.wtk.SplitPane;

class SplitPaneLoader extends ContainerLoader {
    public static final String SPLIT_PANE_TAG = "SplitPane";
    public static final String TOP_REGION_TAG = "top";
    public static final String LEFT_REGION_TAG = "left";
    public static final String BOTTOM_REGION_TAG = "bottom";
    public static final String RIGHT_REGION_TAG = "right";

    public static final String ORIENTATION_ATTRIBUTE = "orientation";

    public static final String PRIMARY_REGION_ATTRIBUTE = "primaryRegion";
    public static final String TOP_REGION = "top";
    public static final String LEFT_REGION = "left";
    public static final String BOTTOM_REGION = "bottom";
    public static final String RIGHT_REGION = "right";

    public static final String SPLIT_LOCATION_ATTRIBUTE = "splitLocation";
    public static final String LOCKED_ATTRIBUTE = "locked";

    protected Container createContainer() {
        return new SplitPane();
    }

    protected Component load(Element element, ComponentLoader rootLoader)
        throws LoadException {
        SplitPane splitPane = (SplitPane)super.load(element, rootLoader);

        Orientation orientation = Orientation.HORIZONTAL;
        if (element.hasAttribute(ORIENTATION_ATTRIBUTE)) {
            String orientationAttribute = element.getAttribute(ORIENTATION_ATTRIBUTE);
            orientation = Orientation.decode(orientationAttribute);
        }

        splitPane.setOrientation(orientation);

        if (element.hasAttribute(PRIMARY_REGION_ATTRIBUTE)) {
            String primaryRegionAttribute = element.getAttribute
                (PRIMARY_REGION_ATTRIBUTE);

            if (primaryRegionAttribute.equals(TOP_REGION)) {
                if (orientation != Orientation.VERTICAL) {
                    throw new IllegalArgumentException("Horizontal split panes do not " +
                        "have a top region.");
                }

                splitPane.setPrimaryRegion(SplitPane.Region.TOP_LEFT);
            } else if (primaryRegionAttribute.equals(LEFT_REGION)) {
                if (orientation != Orientation.HORIZONTAL) {
                    throw new IllegalArgumentException("Vertical split panes do not " +
                        "have a left region.");
                }

                splitPane.setPrimaryRegion(SplitPane.Region.TOP_LEFT);
            } else if (primaryRegionAttribute.equals(BOTTOM_REGION)) {
                if (orientation != Orientation.VERTICAL) {
                    throw new IllegalArgumentException("Horizontal split panes do not " +
                        "have a bottom region.");
                }

                splitPane.setPrimaryRegion(SplitPane.Region.BOTTOM_RIGHT);
            } else if (primaryRegionAttribute.equals(RIGHT_REGION)) {
                if (orientation != Orientation.HORIZONTAL) {
                    throw new IllegalArgumentException("Vertical split panes do not " +
                        "have a right region.");
                }

                splitPane.setPrimaryRegion(SplitPane.Region.BOTTOM_RIGHT);
            } else {
                throw new IllegalArgumentException(primaryRegionAttribute +
                    " is not a valid split pane region.");
            }
        }

        if (element.hasAttribute(SPLIT_LOCATION_ATTRIBUTE)) {
            String splitLocationAttribute = element.getAttribute
                (SPLIT_LOCATION_ATTRIBUTE);
            splitPane.setSplitLocation(Integer.parseInt(splitLocationAttribute));
        }

        if (element.hasAttribute(LOCKED_ATTRIBUTE)) {
            String lockedAttribute = element.getAttribute(LOCKED_ATTRIBUTE);
            splitPane.setLocked(Boolean.parseBoolean(lockedAttribute));
        }

        NodeList childNodes = element.getChildNodes();
        int n = childNodes.getLength();

        if (n > 0) {
            ComponentLoader componentLoader = new ComponentLoader();

            for (int i = 0; i < n; i++) {
                Node childNode = childNodes.item(i);

                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element)childNode;
                    String childTagName = childElement.getTagName();

                    if (childTagName.equals(TOP_REGION_TAG)) {
                        if (orientation != Orientation.VERTICAL) {
                            throw new IllegalArgumentException("Horizontal split panes " +
                                "do not have a top region.");
                        }

                        splitPane.setTopLeftComponent(loadFirstComponent(childElement,
                            componentLoader, rootLoader));
                    } else if (childTagName.equals(LEFT_REGION_TAG)) {
                        if (orientation != Orientation.HORIZONTAL) {
                            throw new IllegalArgumentException("Vertical split panes " +
                                "do not have a left region.");
                        }

                        splitPane.setTopLeftComponent(loadFirstComponent(childElement,
                            componentLoader, rootLoader));
                    } else if (childTagName.equals(BOTTOM_REGION_TAG)) {
                        if (orientation != Orientation.VERTICAL) {
                            throw new IllegalArgumentException("Horizontal split panes " +
                                "do not have a bottom region.");
                        }

                        splitPane.setBottomRightComponent(loadFirstComponent(childElement,
                            componentLoader, rootLoader));
                    } else if (childTagName.equals(RIGHT_REGION_TAG)) {
                        if (orientation != Orientation.HORIZONTAL) {
                            throw new IllegalArgumentException("Vertical split panes " +
                                "do not have a right region.");
                        }

                        splitPane.setBottomRightComponent(loadFirstComponent(childElement,
                            componentLoader, rootLoader));
                    }
                }
            }
        }

        return splitPane;
    }

    private Component loadFirstComponent(Element element, ComponentLoader componentLoader,
        ComponentLoader rootLoader) throws LoadException {
        Component firstComponent = null;

        NodeList childNodes = element.getChildNodes();

        for (int i = 0, n = childNodes.getLength(); i < n; i++) {
            Node childNode = childNodes.item(i);

            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                firstComponent = componentLoader.load((Element)childNode, rootLoader);
                break;
            }
        }

        return firstComponent;
    }
}
