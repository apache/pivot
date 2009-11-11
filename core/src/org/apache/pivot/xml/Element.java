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
         * Determines if this element declares any namespaces.
         *
         * @return
         * <tt>true</tt> if this element does not declare any namespaces;
         * <tt>false</tt> if the element declares at least one namespace.
         */
        @Override
        public boolean isEmpty() {
            return namespaces.isEmpty();
        }

        /**
         * Returns an iterator over the element's namespace prefixes.
         */
        @Override
        public Iterator<String> iterator() {
            return new ImmutableIterator<String>(namespaces.iterator());
        }
    }

    /**
     * Dictionary representing the attributes declared by this element.
     */
    public class AttributeDictionary implements Dictionary<String, String>, Iterable<String> {
        private AttributeDictionary() {
        }

        /**
         * Returns an attribute value.
         */
        @Override
        public String get(String attribute) {
            return attributes.get(attribute);
        }

        /**
         * Sets an attribute value.
         *
         * @param attribute
         * @param value
         *
         * @return
         * The value previously associated with the given attribute.
         */
        @Override
        public String put(String attribute, String value) {
            if (value == null) {
                throw new IllegalArgumentException("value is null.");
            }

            boolean update = containsKey(attribute);
            String previousValue = attributes.put(attribute, value);

            if (update) {
                elementListeners.attributeUpdated(Element.this, attribute, previousValue);
            } else {
                elementListeners.attributeAdded(Element.this, attribute);
            }

            return previousValue;
        }

        /**
         * Removes an attribute value.
         *
         * @param attribute
         *
         * @return
         * The value previously associated with the given attribute.
         */
        @Override
        public String remove(String attribute) {
            String value = null;

            if (containsKey(attribute)) {
                value = attributes.remove(attribute);
                elementListeners.namespaceRemoved(Element.this, attribute, value);
            }

            return value;
        }

        /**
         * Tests for the existence of an attribute.
         *
         * @param attribute
         *
         * @return
         * <tt>true</tt> if this element defines the given attribute; <tt>false<tt>,
         * otherwise.
         */
        @Override
        public boolean containsKey(String attribute) {
            return attributes.containsKey(attribute);
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
            return attributes.isEmpty();
        }

        /**
         * Returns an iterator over the element's attributes.
         */
        @Override
        public Iterator<String> iterator() {
            return new ImmutableIterator<String>(attributes.iterator());
        }
    }

    private static class ElementListenerList extends ListenerList<ElementListener>
        implements ElementListener {
        @Override
        public void namespacePrefixChanged(Element element, String previousNamespacePrefix) {
            for (ElementListener listener : this) {
                listener.namespacePrefixChanged(element, previousNamespacePrefix);
            }
        }

        @Override
        public void localNameChanged(Element element, String previousLocalName) {
            for (ElementListener listener : this) {
                listener.localNameChanged(element, previousLocalName);
            }
        }

        @Override
        public void attributeAdded(Element element, String attribute) {
            for (ElementListener listener : this) {
                listener.attributeAdded(element, attribute);
            }
        }

        @Override
        public void attributeUpdated(Element element, String attribute, String previousValue) {
            for (ElementListener listener : this) {
                listener.attributeUpdated(element, attribute, previousValue);
            }
        }

        @Override
        public void attributeRemoved(Element element, String attribute, String value) {
            for (ElementListener listener : this) {
                listener.attributeRemoved(element, attribute, value);
            }
        }

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
    }

    private String namespacePrefix;
    private String localName;

    private HashMap<String, String> namespaces = new HashMap<String, String>();
    private NamespaceDictionary namespaceDictionary = new NamespaceDictionary();
    private String defaultNamespaceURI = null;

    private HashMap<String, String> attributes = new HashMap<String, String>();
    private AttributeDictionary attributeDictionary = new AttributeDictionary();

    private ArrayList<Node> nodes = new ArrayList<Node>();

    private ListListenerList<Node> listListeners = new ListListenerList<Node>();
    private ElementListenerList elementListeners = new ElementListenerList();

    public Element(String localName) {
        this(null, localName);
    }

    public Element(String namespacePrefix, String localName) {
        setNamespacePrefix(namespacePrefix);
        setLocalName(localName);
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
     * Sets the element's namespace prefix.
     * <p>
     * Note that this method does not ensure that the namespace specified by the
     * prefix actually exists. It only verifies that the prefix does not contain
     * invalid characters.
     *
     * @param namespacePrefix
     * The element's namespace prefix, or <tt>null</tt> to use the default namespace.
     */
    public void setNamespacePrefix(String namespacePrefix) {
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
                    && c != '.') {
                    throw new IllegalArgumentException("'" + c + "' is not a valid character"
                        + " for a namespace prefix.");
                }
            }
        }

        String previousNamespacePrefix = this.namespacePrefix;

        if (previousNamespacePrefix != namespacePrefix) {
            this.namespacePrefix = namespacePrefix;
            elementListeners.namespacePrefixChanged(this, previousNamespacePrefix);
        }
    }

    /**
     * Returns the element's local name.
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Sets the element's local name.
     *
     * @param localName
     */
    public void setLocalName(String localName) {
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
                && c != '.') {
                throw new IllegalArgumentException("'" + c + "' is not a valid character"
                    + " for a local name.");
            }
        }

        String previousLocalName = this.localName;

        if (previousLocalName != localName) {
            this.localName = localName;
            elementListeners.localNameChanged(this, previousLocalName);
        }
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
     * Returns the element's namespace dictionary.
     */
    public NamespaceDictionary getNamespaces() {
        return namespaceDictionary;
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
     * @return
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
     * Returns the element's attribute dictionary.
     */
    public AttributeDictionary getAttributes() {
        return attributeDictionary;
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

        int index = nodes.add(node);
        node.setParent(this);
        listListeners.itemInserted(this, index);

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
     * @param
     */
    @Override
    public int remove(Node node) {
        int index = nodes.indexOf(node);
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
}
