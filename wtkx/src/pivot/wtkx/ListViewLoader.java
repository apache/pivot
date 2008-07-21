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
import pivot.collections.List;
import pivot.collections.Map;
import pivot.serialization.JSONSerializer;
import pivot.wtk.Component;
import pivot.wtk.ListView;

class ListViewLoader extends Loader {
    public static final String LIST_VIEW_TAG = "ListView";

    public static final String SELECT_MODE_ATTRIBUTE = "selectMode";
    public static final String SELECTED_INDEX_ATTRIBUTE = "selectedIndex";
    public static final String SELECTED_INDEXES_ATTRIBUTE = "selectedIndexes";
    public static final String DISABLED_ITEMS_ATTRIBUTE = "disabledItems";
    public static final String ITEM_RENDERER_ATTRIBUTE = "itemRenderer";
    public static final String ITEM_RENDERER_STYLES_ATTRIBUTE = "itemRendererStyles";
    public static final String ITEM_RENDERER_PROPERTIES_ATTRIBUTE = "itemRendererProperties";
    public static final String SELECTED_VALUE_KEY_ATTRIBUTE = "selectedValueKey";
    public static final String SELECTED_VALUES_KEY_ATTRIBUTE = "selectedValuesKey";
    public static final String VALUE_MAPPING_ATTRIBUTE = "valueMapping";

    public static final String LIST_DATA_TAG = "listData";

    @Override
    @SuppressWarnings("unchecked")
    protected Component load(Element element, ComponentLoader rootLoader)
        throws LoadException {
        ListView listView = new ListView();

        NodeList childNodes = element.getChildNodes();

        for (int i = 0, n = childNodes.getLength(); i < n; i++) {
            Node childNode = childNodes.item(i);

            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element)childNode;
                String childTagName = childElement.getTagName();

                if (childTagName.equals(LIST_DATA_TAG)) {
                    InlineDataList elementList = InlineDataList.load(childElement, rootLoader);
                    listView.setListData(elementList);
                }
            }
        }

        if (element.hasAttribute(SELECT_MODE_ATTRIBUTE)) {
            String selectModeAttribute = element.getAttribute(SELECT_MODE_ATTRIBUTE);
            ListView.SelectMode selectMode = ListView.SelectMode.decode(selectModeAttribute);
            listView.setSelectMode(selectMode);
        }

        if (element.hasAttribute(SELECTED_INDEX_ATTRIBUTE)) {
            int selectedIndex = Integer.parseInt(element.getAttribute(SELECTED_INDEX_ATTRIBUTE));
            listView.setSelectedIndex(selectedIndex);
        }

        if (element.hasAttribute(SELECTED_INDEXES_ATTRIBUTE)) {
            String selectedIndexesAttribute = element.getAttribute(SELECTED_INDEXES_ATTRIBUTE);

            StringReader selectedIndexesReader = null;
            try {
                selectedIndexesReader = new StringReader(selectedIndexesAttribute);
                JSONSerializer jsonSerializer = new JSONSerializer();
                List<Object> selectedIndexes =
                    (List<Object>)jsonSerializer.readObject(selectedIndexesReader);

                for (Object index : selectedIndexes) {
                    listView.addSelectedIndex((Integer)index);
                }
            } catch(Exception exception) {
                throw new LoadException("Unable to set selected indexes.", exception);
            } finally {
                if (selectedIndexesReader != null) {
                    selectedIndexesReader.close();
                }
            }
        }

        if (element.hasAttribute(DISABLED_ITEMS_ATTRIBUTE)) {
            String disabledItemsAttribute = element.getAttribute(DISABLED_ITEMS_ATTRIBUTE);

            StringReader disabledItemsReader = null;
            try {
                disabledItemsReader = new StringReader(disabledItemsAttribute);
                JSONSerializer jsonSerializer = new JSONSerializer();
                List<Object> disabledItems =
                    (List<Object>)jsonSerializer.readObject(disabledItemsReader);

                for (Object index : disabledItems) {
                    listView.setItemDisabled((Integer)index, true);
                }
            } catch(Exception exception) {
                throw new LoadException("Unable to set disabled items.", exception);
            } finally {
                if (disabledItemsReader != null) {
                    disabledItemsReader.close();
                }
            }
        }

        if (element.hasAttribute(ITEM_RENDERER_ATTRIBUTE)) {
            String itemRendererAttribute = element.getAttribute(ITEM_RENDERER_ATTRIBUTE);

            try {
                Class<?> itemRendererClass = Class.forName(itemRendererAttribute);
                listView.setItemRenderer((ListView.ItemRenderer)itemRendererClass.newInstance());
            } catch(Exception exception) {
                throw new LoadException("Unable to install item renderer.", exception);
            }
        }

        ListView.ItemRenderer itemRenderer = listView.getItemRenderer();

        if (element.hasAttribute(ITEM_RENDERER_STYLES_ATTRIBUTE)) {
            String itemRendererStylesAttribute = element.getAttribute(ITEM_RENDERER_STYLES_ATTRIBUTE);

            StringReader itemRendererStylesReader = null;
            try {
                itemRendererStylesReader = new StringReader(itemRendererStylesAttribute);
                JSONSerializer jsonSerializer = new JSONSerializer();
                Map<String, Object> styles =
                    (Map<String, Object>)jsonSerializer.readObject(itemRendererStylesReader);

                for (String key : styles) {
                    itemRenderer.getStyles().put(key, styles.get(key));
                }
            } catch(Exception exception) {
                throw new LoadException("Unable to apply item renderer styles.", exception);
            } finally {
                if (itemRendererStylesReader != null) {
                    itemRendererStylesReader.close();
                }
            }
        }

        if (element.hasAttribute(ITEM_RENDERER_PROPERTIES_ATTRIBUTE)) {
            String itemRendererPropertiesAttribute =
                element.getAttribute(ITEM_RENDERER_PROPERTIES_ATTRIBUTE);

            StringReader itemRendererPropertiesReader = null;
            try {
                itemRendererPropertiesReader = new StringReader(itemRendererPropertiesAttribute);
                JSONSerializer jsonSerializer = new JSONSerializer();
                Map<String, Object> properties =
                    (Map<String, Object>)jsonSerializer.readObject(itemRendererPropertiesReader);

                for (String key : properties) {
                    itemRenderer.put(key, properties.get(key));
                }
            } catch(Exception exception) {
                throw new LoadException("Unable to apply item renderer properties.", exception);
            } finally {
                if (itemRendererPropertiesReader != null) {
                    itemRendererPropertiesReader.close();
                }
            }
        }

        if (element.hasAttribute(SELECTED_VALUE_KEY_ATTRIBUTE)) {
            String selectedValueKey = element.getAttribute(SELECTED_VALUE_KEY_ATTRIBUTE);
            listView.setSelectedValueKey(selectedValueKey);
        }

        if (element.hasAttribute(SELECTED_VALUES_KEY_ATTRIBUTE)) {
            String selectedValuesKey = element.getAttribute(SELECTED_VALUES_KEY_ATTRIBUTE);
            listView.setSelectedValuesKey(selectedValuesKey);
        }

        if (element.hasAttribute(VALUE_MAPPING_ATTRIBUTE)) {
            String valueMappingAttribute = element.getAttribute(VALUE_MAPPING_ATTRIBUTE);

            try {
                Class<?> valueMappingClass = Class.forName(valueMappingAttribute);
                listView.setValueMapping((ListView.ValueMapping)valueMappingClass.newInstance());
            } catch(Exception exception) {
                throw new LoadException("Unable to install value mapping.", exception);
            }
        }

        return listView;
    }

}
