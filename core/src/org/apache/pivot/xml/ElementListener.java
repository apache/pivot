/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.xml;

import org.apache.pivot.collections.Sequence;

/**
 * Element listener interface.
 */
public interface ElementListener {
    /**
     * Element listener adapter.
     */
    public static class Adapter implements ElementListener {
        @Override
        public void defaultNamespaceURIChanged(Element element, String previousDefaultNamespaceURI) {
            // empty block
        }

        @Override
        public void namespaceAdded(Element element, String prefix) {
            // empty block
        }

        @Override
        public void namespaceUpdated(Element element, String prefix, String previousURI) {
            // empty block
        }

        @Override
        public void namespaceRemoved(Element element, String prefix, String uri) {
            // empty block
        }

        @Override
        public void attributeInserted(Element element, int index) {
            // empty block
        }

        @Override
        public void attributesRemoved(Element element, int index, Sequence<Element.Attribute> attributes) {
            // empty block
        }

        @Override
        public void attributeValueChanged(Element.Attribute attribute, String previousValue) {
            // empty block
        }
    }

    /**
     * Called when an element's default namespace URI has changed.
     *
     * @param element
     * @param previousDefaultNamespaceURI
     */
    public void defaultNamespaceURIChanged(Element element, String previousDefaultNamespaceURI);

    /**
     * Called when a namespace has been added to an element.
     *
     * @param element
     * @param prefix
     */
    public void namespaceAdded(Element element, String prefix);

    /**
     * Called when an element attribute has been updated.
     *
     * @param element
     * @param prefix
     * @param previousURI
     */
    public void namespaceUpdated(Element element, String prefix, String previousURI);

    /**
     * Called when a namespace has been removed from an element.
     *
     * @param element
     * @param prefix
     * @param uri
     */
    public void namespaceRemoved(Element element, String prefix, String uri);

    /**
     * Called when an attribute has been added to an element.
     *
     * @param element
     * @param index
     */
    public void attributeInserted(Element element, int index);

    /**
     * Called when attributes have been removed from an element.
     *
     * @param element
     * @param index
     * @param attributes
     */
    public void attributesRemoved(Element element, int index, Sequence<Element.Attribute> attributes);

    /**
     * Called when an attribute's value has changed.
     *
     * @param attribute
     * @param previousValue
     */
    public void attributeValueChanged(Element.Attribute attribute, String previousValue);
}
