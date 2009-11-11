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

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;

/**
 * Node class representing an XML element.
 */
public class Element extends Node implements List<Node>, Dictionary<String, String> {
    /**
     * Dictionary representing the namespaces declared by this element.
     */
    public class NamespaceDictionary implements Dictionary<String, String> {
        private NamespaceDictionary() {
        }

        @Override
        public String get(String prefix) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String put(String prefix, String uri) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String remove(String prefix) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean containsKey(String prefix) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isEmpty() {
            // TODO Auto-generated method stub
            return false;
        }
    }

    private static class ElementListenerList extends ListenerList<ElementListener>
        implements ElementListener {
        public void namespacePrefixChanged(Element element, String previousNamespacePrefix) {
            for (ElementListener listener : this) {
                listener.namespacePrefixChanged(element, previousNamespacePrefix);
            }
        }

        public void localNameChanged(Element element, String previousLocalName) {
            for (ElementListener listener : this) {
                listener.localNameChanged(element, previousLocalName);
            }
        }

        public void attributeAdded(Element element, String attribute) {
            for (ElementListener listener : this) {
                listener.attributeAdded(element, attribute);
            }
        }

        public void attributeUpdated(Element element, String attribute, String previousValue) {
            for (ElementListener listener : this) {
                listener.attributeUpdated(element, attribute, previousValue);
            }
        }

        public void attributeRemoved(Element element, String attribute) {
            for (ElementListener listener : this) {
                listener.attributeRemoved(element, attribute);
            }
        }

        public void namespaceAdded(Element element, String prefix) {
            for (ElementListener listener : this) {
                listener.namespaceAdded(element, prefix);
            }
        }

        public void namespaceUpdated(Element element, String prefix, String previousURI) {
            for (ElementListener listener : this) {
                listener.namespaceUpdated(element, prefix, previousURI);
            }
        }

        public void namespaceRemoved(Element element, String prefix) {
            for (ElementListener listener : this) {
                listener.namespaceRemoved(element, prefix);
            }
        }
    }

    private String namespacePrefix;
    private String localName;

    private ElementListenerList elementListeners = new ElementListenerList();

    public Element(String localName) {
        this(null, localName);
    }

    public Element(String namespacePrefix, String localName) {
        setNamespacePrefix(namespacePrefix);
        setLocalName(localName);
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

    public String getNamespacePrefix() {
        return namespacePrefix;
    }

    public void setNamespacePrefix(String namespacePrefix) {
        if (namespacePrefix != null) {
            // TODO Validate name

            if (getNamespaceURI(namespacePrefix) == null) {
                throw new IllegalArgumentException("Namespace \"" + namespacePrefix + "\" does not exist.");
            }
        }

        String previousNamespacePrefix = this.namespacePrefix;

        if (previousNamespacePrefix != namespacePrefix) {
            this.namespacePrefix = namespacePrefix;
            elementListeners.namespacePrefixChanged(this, previousNamespacePrefix);
        }
    }

    public String getNamespaceURI() {
        return (namespacePrefix == null) ? null : getNamespaceURI(namespacePrefix);
    }

    public String getNamespaceURI(String prefix) {
        // TODO Walk up parent tree looking for namespace prefix
        return null;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        if (localName == null) {
            throw new IllegalArgumentException();
        }

        // TODO Validate name

        String previousLocalName = this.localName;

        if (previousLocalName != localName) {
            this.localName = localName;
            elementListeners.localNameChanged(this, previousLocalName);
        }
    }

    @Override
    public String get(String attribute) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String put(String attribute, String value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String remove(String attribute) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean containsKey(String attribute) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int add(Node item) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void insert(Node item, int index) {
        // TODO Auto-generated method stub

    }

    @Override
    public Node update(int index, Node item) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int remove(Node item) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Sequence<Node> remove(int index, int count) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

    @Override
    public Node get(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int indexOf(Node item) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Comparator<Node> getComparator() {
        return null;
    }

    @Override
    public void setComparator(Comparator<Node> comparator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Node> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ListenerList<ListListener<Node>> getListListeners() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns the element listener list.
     */
    public ListenerList<ElementListener> getElementListeners() {
        return elementListeners;
    }
}
