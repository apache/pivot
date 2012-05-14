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

import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;

/**
 * Node class representing an XML element.
 */
public class Element extends Node implements List<Node> {
    /**
     * Class representing an XML attribute.
     */
    public static class Attribute {
        private Element element = null;

        private String namespacePrefix;
        private String localName;
        private String value;

        public Attribute(String localName, String value) {
            this(null, localName, value);
        }

        public Attribute(String namespacePrefix, String localName, String value) {
            validateName(namespacePrefix, localName);

            this.namespacePrefix = namespacePrefix;
            this.localName = localName;

            setValue(value);
        }

        /**
         * Returns the element to which this attribute belongs.
         *
         * @return
         * This attribute's element, or <tt>null</tt> if the attribute does not
         * belong to an element.
         */
        public Element getElement() {
            return element;
        }

        /**
         * Returns the attribute's namespace prefix.
         *
         * @return
         * The attribute's namespace prefix, or <tt>null</tt> if the attribute belongs to the
         * default namespace.
         */
        public String getNamespacePrefix() {
            return namespacePrefix;
        }

        /**
         * Returns the attribute's local name.
         */
        public String getLocalName() {
            return localName;
        }

        /**
         * Returns the fully-qualified name of the attribute.
         */
        public String getName() {
            String name;
            if (namespacePrefix == null) {
                name = localName;
            } else {
                name = namespacePrefix + ":" + localName;
            }

            return name;
        }

        /**
         * Returns the attribute's value.
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the attribute's value.
         *
         * @param value
         */
        public void setValue(String value) {
            if (value == null) {
                throw new IllegalArgumentException();
            }

            String previousValue = this.value;
            if (previousValue != value) {
                this.value = value;

                if (element != null) {
                    element.elementListeners.attributeValueChanged(this, previousValue);
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            boolean equals = false;

            if (this == o) {
                equals = true;
            } else if (o instanceof Attribute) {
                Attribute attribute = (Attribute)o;
                if (namespacePrefix == null) {
                    equals = (attribute.namespacePrefix == null);
                } else {
                    equals = (namespacePrefix.equals(attribute.namespacePrefix));
                }

                equals &= (localName.equals(attribute.localName)
                    && value.equals(attribute.value));
            }

            return equals;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            if (namespacePrefix != null) {
                result = 31 * result + namespacePrefix.hashCode();
            }
            result = prime * result + localName.hashCode();
            return result;
        }

        @Override
        public String toString() {
            String string = "";
            if (namespacePrefix != null) {
                string += namespacePrefix + ":";
            }

            string += localName + "=\"" + value + "\"";

            return string;
        }
    }

    /**
     * Sequence representing the attributes declared by this element.
     */
    public class AttributeSequence implements Sequence<Attribute>, Iterable<Attribute> {
        private AttributeSequence() {
        }

        /**
         * Adds an attribute to the sequence.
         *
         * @param attribute
         */
        @Override
        public int add(Attribute attribute) {
            int index = getLength();
            insert(attribute, index);

            return index;
        }

        /**
         * Inserts an attribute into the sequence at a specific location.
         *
         * @param attribute
         * @param index
         */
        @Override
        public void insert(Attribute attribute, int index) {
            if (attribute == null) {
                throw new IllegalArgumentException();
            }

            if (attribute.getElement() != null) {
                throw new IllegalArgumentException();
            }

            String attributeName = attribute.getName();
            if (attributeMap.containsKey(attributeName)) {
                throw new IllegalArgumentException("Attribute \"" + attributeName + "\" already exists.");
            }

            attributes.insert(attribute, index);
            attributeMap.put(attributeName, attribute);
            attribute.element = Element.this;

            elementListeners.attributeInserted(Element.this, index);
        }

        /**
         * @throws UnsupportedOperationException
         * This method is not supported. Use {@link Attribute#setValue(String)}
         * instead.
         */
        @Override
        public Attribute update(int index, Attribute item) {
            throw new UnsupportedOperationException();
        }

        /**
         * Removes an attribute from the sequence.
         *
         * @param attribute
         */
        @Override
        public int remove(Attribute attribute) {
            int index = indexOf(attribute);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        /**
         * Removes a range of attributes from the sequence.
         *
         * @param index
         * @param count
         */
        @Override
        public Sequence<Attribute> remove(int index, int count) {
            Sequence<Attribute> removed = attributes.remove(index, count);
            if (count > 0) {
                for (int i = 0, n = removed.getLength(); i < n; i++ ) {
                    Attribute attribute = removed.get(i);
                    String attributeName = attribute.getName();
                    attributeMap.remove(attributeName);
                    attribute.element = null;
                }

                elementListeners.attributesRemoved(Element.this, index, removed);
            }

            return removed;
        }

        /**
         * Returns the attribute at a given index.
         *
         * @param index
         */
        @Override
        public Attribute get(int index) {
            return attributes.get(index);
        }

        /**
         * Determines the index of an attribute.
         *
         * @param attribute
         *
         * @return
         * The index of the attribute, if found; otherwise, <tt>-1</tt>.
         */
        @Override
        public int indexOf(Attribute attribute) {
            return attributes.indexOf(attribute);
        }

        /**
         * Returns the number of attributes in the sequence.
         */
        @Override
        public int getLength() {
            return attributes.getLength();
        }

        /**
         * Returns an iterator over the attribute sequence.
         */
        @Override
        public Iterator<Attribute> iterator() {
            return new ImmutableIterator<Attribute>(attributes.iterator());
        }
    }

    /**
     * Dictionary representing the namespaces declared by this element.
     */
    public class NamespaceDictionary implements Dictionary<String, String>, Iterable<String> {
        private NamespaceDictionary() {
        }

        /**
         * Returns the URI of a namespace declared by this element.
         *
         * @param prefix
         * The namespace prefix.
         *
         * @return
         * The declared namespace, or <tt>null</tt> if no such namespace exists.
         */
        @Override
        public String get(String prefix) {
            return namespaces.get(prefix);
        }

        /**
         * Sets the URI of a namespace declared by this element.
         *
         * @param prefix
         * The namespace prefix.
         *
         * @param uri
         * The namespace URI.
         *
         * @return
         * The URI previously associated with the given prefix.
         */
        @Override
        public String put(String prefix, String uri) {
            if (uri == null) {
                throw new IllegalArgumentException("uri is null.");
            }

            boolean update = containsKey(prefix);
            String previousURI = namespaces.put(prefix, uri);

            if (update) {
                elementListeners.namespaceUpdated(Element.this, prefix, previousURI);
            } else {
                elementListeners.namespaceAdded(Element.this, prefix);
            }

            return previousURI;
        }

        /**
         * Removes a namespace from this element's declared namespaces.
         *
         * @param prefix
         * The namespace prefix.
         *
         * @return
         * The URI previously associated with the given prefix.
         */
        @Override
        public String remove(String prefix) {
            String uri = null;

            if (containsKey(prefix)) {
                uri = namespaces.remove(prefix);
                elementListeners.namespaceRemoved(Element.this, prefix, uri);
            }

            return uri;
        }

        /**
         * Tests for the existence of a namespace declared by this element.
         *
         * @param prefix
         *
         * @return
         * <tt>true</tt> if this element declares a namespace with the given prefix;
         * <tt>false</tt>, otherwise.
         */
        @Override
        public boolean containsKey(String prefix) {
            return namespaces.containsKey(prefix);
        }

        /**
         * Returns an iterator over the element's namespace prefixes.
         */
        @Override
        public Iterator<String> iterator() {
            return new ImmutableIterator<String>(namespaces.iterator());
        }
    }

    private static class ElementListenerList extends ListenerList<ElementListener>
        implements ElementListener {
        @Override
        public void defaultNamespaceURIChanged(Element element, String previousDefaultNamespaceURI) {
            for (ElementListener listener : this) {
                listener.defaultNamespaceURIChanged(element, previousDefaultNamespaceURI);
            }
        }

        @Override
        public void namespaceAdded(Element element, String prefix) {
            for (ElementListener listener : this) {
                listener.namespaceAdded(element, prefix);
            }
        }

        @Override
        public void namespaceUpdated(Element element, String prefix, String previousURI) {
            for (ElementListener listener : this) {
                listener.namespaceUpdated(element, prefix, previousURI);
            }
        }

        @Override
        public void namespaceRemoved(Element element, String prefix, String uri) {
            for (ElementListener listener : this) {
                listener.namespaceRemoved(element, prefix, uri);
            }
        }

        @Override
        public void attributeInserted(Element element, int index) {
            for (ElementListener listener : this) {
                listener.attributeInserted(element, index);
            }
        }

        @Override
        public void attributesRemoved(Element element, int index, Sequence<Attribute> attributes) {
            for (ElementListener listener : this) {
                listener.attributesRemoved(element, index, attributes);
            }
        }

        @Override
        public void attributeValueChanged(Attribute attribute, String previousValue) {
            for (ElementListener listener : this) {
                listener.attributeValueChanged(attribute, previousValue);
            }
        }
    }

    /**

     * Dictionary representing the attributes declared by this element.
     */
    public class ElementDictionary implements Dictionary<String, String> {
        private ElementDictionary() {
        }

        /**
         * Returns an attribute value.
         *
         * @param attributeName
         *
         * @return
         * The value associated with the given attribute, or <tt>null</tt>
         */
        @Override
        public String get(String attributeName) {
            Attribute attribute = attributeMap.get(attributeName);
            return (attribute == null) ? null : attribute.getValue();
        }

        /**
         * Sets an attribute value.
         *
         * @param attributeName
         * @param value
         *
         * @return
         * The value previously associated with the given attribute, or <tt>null</tt>
         * if the attribute did not previously exist.
         */
        @Override
        public String put(String attributeName, String value) {
            String previousValue;

            Attribute attribute = attributeMap.get(attributeName);
            if (attribute == null) {
                previousValue = null;

                String namespacePrefixElementDictionary;
                String localNameElementDictionary;
                int i = attributeName.indexOf(':');
                if (i == -1) {
                    namespacePrefixElementDictionary = null;
                    localNameElementDictionary = attributeName;
                } else {
                    namespacePrefixElementDictionary = attributeName.substring(0, i);
                    localNameElementDictionary = attributeName.substring(i + 1);
                }

                attributeSequence.add(new Attribute(namespacePrefixElementDictionary, localNameElementDictionary, value));
            } else {
                previousValue = attribute.getValue();
                attribute.setValue(value);
            }

            return previousValue;
        }

        /**
         * Removes an attribute.
         *
         * @param attributeName
         *
         * @return
         * The value previously associated with the given attribute.
         */
        @Override
        public String remove(String attributeName) {
            Attribute attribute = attributeMap.get(attributeName);
            if (attribute != null) {
                attributeSequence.remove(attribute);
            }

            return (attribute == null) ? null : attribute.getValue();
        }

        /**
         * Tests for the existence of an attribute.
         *
         * @param attributeName
         *
         * @return
         * <tt>true</tt> if this element defines the given attribute; <tt>false<tt>,
         * otherwise.
         */
        @Override
        public boolean containsKey(String attributeName) {
            return attributeMap.containsKey(attributeName);
        }

    }


    private String namespacePrefix;
    private String localName;

    private String defaultNamespaceURI = null;
    private HashMap<String, String> namespaces = new HashMap<String, String>();
    private NamespaceDictionary namespaceDictionary = new NamespaceDictionary();
    private ElementDictionary elementDictionary = new ElementDictionary();

    private ArrayList<Attribute> attributes = new ArrayList<Attribute>();
    private AttributeSequence attributeSequence = new AttributeSequence();
    private HashMap<String, Attribute> attributeMap = new HashMap<String, Attribute>();

    private ArrayList<Node> nodes = new ArrayList<Node>();

    private ListListenerList<Node> listListeners = new ListListenerList<Node>();
    private ElementListenerList elementListeners = new ElementListenerList();

    public Element(String localName) {
        this(null, localName);
    }

    public Element(String namespacePrefix, String localName) {
        validateName(namespacePrefix, localName);

        this.namespacePrefix = namespacePrefix;
        this.localName = localName;
    }

    /**
     * Returns the element's namespace prefix.
     *
     * @return
     * The element's namespace prefix, or <tt>null</tt> if the element belongs to the
     * default namespace.
     */
    public String getNamespacePrefix() {
        return namespacePrefix;
    }

    /**
     * Returns the element's local name.
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Returns the fully-qualified name of the element.
     */
    public String getName() {
        String name;
        if (namespacePrefix == null) {
            name = localName;
        } else {
            name = namespacePrefix + ":" + localName;
        }

        return name;
    }

    /**
     * Returns the element's default namespace URI.
     *
     * @return
     * The default namespace URI declared by this element, or <tt>null</tt> if
     * this element does not declare a default namespace.
     */
    public String getDefaultNamespaceURI() {
        return defaultNamespaceURI;
    }

    /**
     * Sets the element's default namespace URI.
     *
     * @param defaultNamespaceURI
     * The default namespace URI declared by this element, or <tt>null</tt> if
     * this element does not declare a default namespace.
     */
    public void setDefaultNamespaceURI(String defaultNamespaceURI) {
        String previousDefaultNamespaceURI = this.defaultNamespaceURI;

        if (previousDefaultNamespaceURI != defaultNamespaceURI) {
            this.defaultNamespaceURI = defaultNamespaceURI;
            elementListeners.defaultNamespaceURIChanged(this, previousDefaultNamespaceURI);
        }
    }

    /**
     * Returns the element's namespace dictionary.
     */
    public NamespaceDictionary getNamespaces() {
        return namespaceDictionary;
    }

    /**
     * Determines the namespace URI corresponding to the given prefix by traversing
     * the element's ancestry.
     *
     * @param prefix
     * The namespace prefix to look up, or <tt>null</tt> to determine the default
     * namespace for this element.
     *
     * @return
     * The namespace URI corresponding to the given prefix, or <tt>null</tt> if a
     * URI could not be found.
     */
    public String getNamespaceURI(String prefix) {
        String namespaceURI;

        Element parent = getParent();
        if (prefix == null) {
            if (defaultNamespaceURI == null) {
                namespaceURI = parent.getDefaultNamespaceURI();
            } else {
                namespaceURI = defaultNamespaceURI;
            }
        } else {
            if (namespaces.containsKey(prefix)) {
                namespaceURI = namespaces.get(prefix);
            } else {
                namespaceURI = parent.getNamespaceURI(prefix);
            }
        }

        return namespaceURI;
    }

    /**
     * Returns the element's element dictionary.
     */
    public ElementDictionary getElementDictionary() {
        return elementDictionary;
    }

    /**
     * Returns the element's attribute dictionary.
     */
    public AttributeSequence getAttributes() {
        return attributeSequence;
    }

    /**
     * Adds a node to this element.
     *
     * @param node
     *
     * @return
     * The index at which the node was added.
     */
    @Override
    public int add(Node node) {
        if (node.getParent() != null) {
            throw new IllegalArgumentException();
        }

        int index = getLength();
        insert(node, index);

        return index;
    }

    /**
     * Inserts a node at a specific location within this element.
     *
     * @param node
     * @param index
     */
    @Override
    public void insert(Node node, int index) {
        if (node.getParent() != null) {
            throw new IllegalArgumentException();
        }

        nodes.insert(node, index);
        node.setParent(this);
        listListeners.itemInserted(this, index);
    }

    /**
     * @throws UnsupportedOperationException
     * This method is not supported.
     */
    @Override
    public Node update(int index, Node node) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes a node from this element.
     *
     * @param node
     */
    @Override
    public int remove(Node node) {
        int index = indexOf(node);
        if (index != -1) {
            remove(index, 1);
        }

        return index;
    }

    /**
     * Removes a range of nodes from this element.
     *
     * @param index
     * @param count
     *
     * @return
     * The removed nodes.
     */
    @Override
    public Sequence<Node> remove(int index, int count) {
        Sequence<Node> removed = nodes.remove(index, count);
        if (count > 0) {
            for (int i = 0, n = removed.getLength(); i < n; i++ ) {
                Node node = removed.get(i);
                node.setParent(null);
            }

            listListeners.itemsRemoved(this, index, removed);
        }

        return removed;
    }

    /**
     * Removes all nodes from this element.
     */
    @Override
    public void clear() {
        if (getLength() > 0) {
            for (int i = 0, n = nodes.getLength(); i < n; i++) {
                Node node = nodes.get(i);
                node.setParent(null);
            }

            nodes.clear();
            listListeners.listCleared(this);
        }
    }

    /**
     * Returns the node at the given index.
     *
     * @param index
     */
    @Override
    public Node get(int index) {
        return nodes.get(index);
    }

    /**
     * Determines the index of the given node within this element.
     *
     * @return
     * The index of the node, or <tt>-1</tt> if the node does not exist in
     * this element.
     */
    @Override
    public int indexOf(Node node) {
        return nodes.indexOf(node);
    }

    /**
     * Returns the number of nodes contained by this element.
     */
    @Override
    public int getLength() {
        return nodes.getLength();
    }

    /**
     * @return
     * <tt>null</tt>; elements cannot be sorted.
     */
    @Override
    public Comparator<Node> getComparator() {
        return null;
    }

    /**
     * @throws UnsupportedOperationException
     * Elements cannot be sorted.
     */
    @Override
    public void setComparator(Comparator<Node> comparator) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns an iterator over this elements child nodes.
     */
    @Override
    public Iterator<Node> iterator() {
        return new ImmutableIterator<Node>(nodes.iterator());
    }

    /**
     * Determines if this element defines any attributes.
     *
     * @return
     * <tt>true</tt> if this element does not define any attributes;
     * <tt>false</tt>, otherwise.
     */
    @Override
    public boolean isEmpty() {
        return attributeMap.isEmpty();
    }

    /**
     * Returns the sub-elements of of this element whose tag names match the
     * given name.
     *
     * @param name
     * The tag name to match.
     *
     * @return
     * A list containing the matching elements. The list will be empty if no
     * elements matched the given tag name.
     */
    public List<Element> getElements(String name) {
        ArrayList<Element> elements = new ArrayList<Element>();

        for (int i = 0, n = getLength(); i < n; i++) {
            Node node = get(i);

            if (node instanceof Element) {
                Element element = (Element)node;

                if (element.getName().equals(name)) {
                    elements.add(element);
                }
            }
        }

        return elements;
    }

    /**
     * Returns the text content of this element. An element is defined to
     * contain text when it contains a single child that is an instance of
     * {@link TextNode}.
     *
     * @return
     * The text content of the element, or {@code null} if this element does
     * not contain text.
     */
    public String getText() {
        String text = null;

        if (getLength() == 1) {
            Node node = get(0);

            if (node instanceof TextNode) {
                TextNode textNode = (TextNode)node;
                text = textNode.getText();
            }
        }

        return text;
    }

    /**
     * Returns the element's listener list.
     */
    @Override
    public ListenerList<ListListener<Node>> getListListeners() {
        return listListeners;
    }

    /**
     * Returns the element listener list.
     */
    public ListenerList<ElementListener> getElementListeners() {
        return elementListeners;
    }

    private static void validateName(String namespacePrefix, String localName) {
        // Validate prefix
        if (namespacePrefix != null) {
            if (namespacePrefix.length() == 0) {
                throw new IllegalArgumentException("Namespace prefix is empty.");
            }

            char c = namespacePrefix.charAt(0);
            if (!Character.isLetter(c)) {
                throw new IllegalArgumentException("'" + c + "' is not a valid start"
                    + " character for a namespace prefix.");
            }

            for (int i = 1, n = namespacePrefix.length(); i < n; i++) {
                c = namespacePrefix.charAt(i);

                if (!Character.isLetterOrDigit(c)
                    && c != '-'
                    && c != '_'
                    && c != '.') {
                    throw new IllegalArgumentException("'" + c + "' is not a valid character"
                        + " for a namespace prefix.");
                }
            }
        }

        // Validate local name
        if (localName == null) {
            throw new IllegalArgumentException();
        }

        if (localName.length() == 0) {
            throw new IllegalArgumentException("Local name is empty.");
        }

        char c = localName.charAt(0);
        if (!Character.isLetter(c)
            && c != '_') {
            throw new IllegalArgumentException("'" + c + "' is not a valid start"
                + " character for a local name.");
        }

        for (int i = 1, n = localName.length(); i < n; i++) {
            c = localName.charAt(i);

            if (!Character.isLetterOrDigit(c)
                && c != '-'
                && c != '_'
                && c != '.') {
                throw new IllegalArgumentException("'" + c + "' is not a valid character"
                    + " for a local name.");
            }
        }
    }

    @Override
    public String toString() {
        String string = "<";
        if (namespacePrefix != null) {
            string += namespacePrefix + ":";
        }

        string += localName + ">";

        return string;
    }
}
