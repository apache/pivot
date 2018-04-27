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
import org.apache.pivot.util.ListenerList;

/**
 * Element listener interface.
 */
public interface ElementListener {
    /**
     * Element listeners.
     */
    public static class Listeners extends ListenerList<ElementListener> implements ElementListener {
        @Override
        public void defaultNamespaceURIChanged(Element element, String previousDefaultNamespaceURI) {
            forEach(listener -> listener.defaultNamespaceURIChanged(element, previousDefaultNamespaceURI));
        }

        @Override
        public void namespaceAdded(Element element, String prefix) {
            forEach(listener -> listener.namespaceAdded(element, prefix));
        }

        @Override
        public void namespaceUpdated(Element element, String prefix, String previousURI) {
            forEach(listener -> listener.namespaceUpdated(element, prefix, previousURI));
        }

        @Override
        public void namespaceRemoved(Element element, String prefix, String uri) {
            forEach(listener -> listener.namespaceRemoved(element, prefix, uri));
        }

        @Override
        public void attributeInserted(Element element, int index) {
            forEach(listener -> listener.attributeInserted(element, index));
        }

        @Override
        public void attributesRemoved(Element element, int index, Sequence<Element.Attribute> attributes) {
            forEach(listener -> listener.attributesRemoved(element, index, attributes));
        }

        @Override
        public void attributeValueChanged(Element.Attribute attribute, String previousValue) {
            forEach(listener -> listener.attributeValueChanged(attribute, previousValue));
        }
    }

    /**
     * Element listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
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
        public void attributesRemoved(Element element, int index,
            Sequence<Element.Attribute> attributes) {
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
     * @param element The element that has changed.
     * @param previousDefaultNamespaceURI The previous value of the default namespace URI.
     */
    default void defaultNamespaceURIChanged(Element element, String previousDefaultNamespaceURI) {
    }

    /**
     * Called when a namespace has been added to an element.
     *
     * @param element The element that has been changed.
     * @param prefix The new namespace prefix that has been set.
     */
    default void namespaceAdded(Element element, String prefix) {
    }

    /**
     * Called when a namespace {@code URI} has been updated.
     *
     * @param element The element that has had the namespace updated.
     * @param prefix The namespace prefix for this element.
     * @param previousURI The previous value of the namespace URI.
     */
    default void namespaceUpdated(Element element, String prefix, String previousURI) {
    }

    /**
     * Called when a namespace has been removed from an element.
     *
     * @param element The element that is changing.
     * @param prefix The namespace prefix that has been removed.
     * @param uri The {@code URI} that was removed.
     */
    default void namespaceRemoved(Element element, String prefix, String uri) {
    }

    /**
     * Called when an attribute has been added to an element.
     *
     * @param element The element that has changed.
     * @param index The index where the new attribute was added.
     */
    default void attributeInserted(Element element, int index) {
    }

    /**
     * Called when attributes have been removed from an element.
     *
     * @param element The element that has changed.
     * @param index Starting index of the attributes that were removed.
     * @param attributes The sequence of removed attributes.
     */
    default void attributesRemoved(Element element, int index, Sequence<Element.Attribute> attributes) {
    }

    /**
     * Called when an attribute's value has changed.
     *
     * @param attribute The attribute whose value has changed.
     * @param previousValue The previous value for this attribute.
     */
    default void attributeValueChanged(Element.Attribute attribute, String previousValue) {
    }
}
