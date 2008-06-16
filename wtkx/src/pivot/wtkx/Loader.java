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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.wtk.Component;

public abstract class Loader {
    protected static class InlineDataList extends ArrayList<InlineDataList>
        implements Dictionary<String, Object> {
        private HashMap<String, Object> properties = new HashMap<String, Object>();
        public Object get(String key) {
            return properties.get(key);
        }

        public Object put(String key, Object value) {
            return properties.put(key, value);
        }

        public Object remove(String key) {
            return properties.remove(key);
        }

        public boolean containsKey(String key) {
            return properties.containsKey(key);
        }

        public boolean isEmpty() {
            return properties.isEmpty();
        }

        public static InlineDataList load(Element element, ComponentLoader rootLoader) {
            InlineDataList inlineDataList = new InlineDataList();

            NamedNodeMap attributes = element.getAttributes();
            for (int i = 0, n = attributes.getLength(); i < n; i++) {
                Node node = attributes.item(i);

                String key = node.getNodeName();
                String value = node.getNodeValue();

                inlineDataList.put(key, rootLoader.resolve(value));
            }

            NodeList childNodes = element.getChildNodes();
            for (int i = 0, n = childNodes.getLength(); i < n; i++) {
                Node childNode = childNodes.item(i);

                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    inlineDataList.add(load((Element)childNode, rootLoader));
                }
            }

            return inlineDataList;
        }
    }

    /**
     * Loads a component from an XML element.
     *
     * @param element
     * The component's root XML element.
     *
     * @param rootLoader
     * The root loader of this component loader.
     *
     * @return
     * The loaded component.
     */
    protected abstract Component load(Element element, ComponentLoader rootLoader)
        throws LoadException;
}
