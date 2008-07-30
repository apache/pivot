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

import java.io.StringReader;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pivot.beans.BeanDictionary;
import pivot.collections.List;
import pivot.collections.Map;
import pivot.serialization.JSONSerializer;
import pivot.wtk.Component;
import pivot.wtk.TreeView;

class TreeViewLoader extends Loader {
    public static final String TREE_VIEW_TAG = "TreeView";

    public static final String SELECT_MODE_ATTRIBUTE = "selectMode";
    public static final String SELECTED_PATH_ATTRIBUTE = "selectedPath";
    public static final String NODE_RENDERER_ATTRIBUTE = "nodeRenderer";
    public static final String NODE_RENDERER_STYLES_ATTRIBUTE = "nodeRendererStyles";
    public static final String NODE_RENDERER_PROPERTIES_ATTRIBUTE = "nodeRendererProperties";

    public static final String TREE_DATA_TAG = "treeData";

    @Override
    @SuppressWarnings("unchecked")
    protected Component load(Element element, ComponentLoader rootLoader)
        throws LoadException {
        TreeView treeView = new TreeView();

        NodeList childNodes = element.getChildNodes();

        for (int i = 0, n = childNodes.getLength(); i < n; i++) {
            Node childNode = childNodes.item(i);

            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element)childNode;
                String childTagName = childElement.getTagName();

                if (childTagName.equals(TREE_DATA_TAG)) {
                    InlineDataList elementList = InlineDataList.load(childElement, rootLoader);
                    treeView.setTreeData(elementList);
                }
            }
        }

        if (element.hasAttribute(SELECT_MODE_ATTRIBUTE)) {
            String selectModeAttribute = element.getAttribute(SELECT_MODE_ATTRIBUTE);
            TreeView.SelectMode selectMode = TreeView.SelectMode.decode(selectModeAttribute);
            treeView.setSelectMode(selectMode);
        }

        if (element.hasAttribute(SELECTED_PATH_ATTRIBUTE)) {
            String selectedPathAttribute = element.getAttribute(SELECTED_PATH_ATTRIBUTE);

            StringReader selectedPathReader = null;
            try {
                selectedPathReader = new StringReader(selectedPathAttribute);
                JSONSerializer jsonSerializer = new JSONSerializer();
                List<Integer> selectedPath = (List<Integer>)jsonSerializer.readObject(selectedPathReader);

                treeView.setSelectedPath(selectedPath);
            } catch(Exception exception) {
                throw new LoadException("Unable to set selected path.", exception);
            } finally {
                if (selectedPathReader != null) {
                    selectedPathReader.close();
                }
            }
        }

        if (element.hasAttribute(NODE_RENDERER_ATTRIBUTE)) {
            String nodeRendererAttribute = element.getAttribute(NODE_RENDERER_ATTRIBUTE);

            try {
                Class<?> nodeRendererClass = Class.forName(nodeRendererAttribute);
                treeView.setNodeRenderer((TreeView.NodeRenderer)nodeRendererClass.newInstance());
            } catch(Exception exception) {
                throw new LoadException("Unable to install node renderer.", exception);
            }
        }

        TreeView.NodeRenderer nodeRenderer = treeView.getNodeRenderer();

        if (element.hasAttribute(NODE_RENDERER_STYLES_ATTRIBUTE)) {
            String nodeRendererStylesAttribute = element.getAttribute(NODE_RENDERER_STYLES_ATTRIBUTE);

            StringReader nodeRendererStylesReader = null;
            try {
                nodeRendererStylesReader = new StringReader(nodeRendererStylesAttribute);
                JSONSerializer jsonSerializer = new JSONSerializer();
                Map<String, Object> styles =
                    (Map<String, Object>)jsonSerializer.readObject(nodeRendererStylesReader);

                for (String key : styles) {
                    nodeRenderer.getStyles().put(key, styles.get(key));
                }
            } catch(Exception exception) {
                throw new LoadException("Unable to apply node renderer styles.", exception);
            } finally {
                if (nodeRendererStylesReader != null) {
                    nodeRendererStylesReader.close();
                }
            }
        }

        if (element.hasAttribute(NODE_RENDERER_PROPERTIES_ATTRIBUTE)) {
            String nodeRendererPropertiesAttribute =
                element.getAttribute(NODE_RENDERER_PROPERTIES_ATTRIBUTE);

            StringReader nodeRendererPropertiesReader = null;
            try {
                nodeRendererPropertiesReader = new StringReader(nodeRendererPropertiesAttribute);
                JSONSerializer jsonSerializer = new JSONSerializer();
                Map<String, Object> properties =
                    (Map<String, Object>)jsonSerializer.readObject(nodeRendererPropertiesReader);

                BeanDictionary nodeRendererDictionary = new BeanDictionary(nodeRenderer);
                for (String key : properties) {
                    nodeRendererDictionary.put(key, properties.get(key));
                }
            } catch(Exception exception) {
                throw new LoadException("Unable to apply node renderer properties.", exception);
            } finally {
                if (nodeRendererPropertiesReader != null) {
                    nodeRendererPropertiesReader.close();
                }
            }
        }

        return treeView;
    }
}
