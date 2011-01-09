/*
 * Contains code originally developed for Apache Pivot under the Apache
 * License, Version 2.0:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.apache.pivot.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Node class representing an XML element.
 */
public class Element extends Node {
    /**
     * Class representing an XML namespace.
     */
    public static class Namespace {
        private Element element = null;

        private String prefix;
        private String uri;

        public Namespace(String prefix, String uri) {
            this.prefix = prefix;
            this.uri = uri;
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
         * Returns the prefix associated with this namespace.
         */
        public String getPrefix() {
            return prefix;
        }

        /**
         * Returns the URI associated with this namespace.
         */
        public String getURI() {
            return uri;
        }
    }

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

            this.value = value;
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

    private String namespacePrefix;
    private String localName;

    private String defaultNamespaceURI = null;
    private ArrayList<Namespace> namespaces = new ArrayList<Namespace>() {
        private static final long serialVersionUID = 0;

        @Override
        public boolean add(Namespace namespace) {
            if (namespace.element != null) {
                throw new IllegalArgumentException();
            }

            if (namespaceMap.containsKey(namespace.getPrefix())) {
                throw new IllegalArgumentException();
            }

            namespace.element = Element.this;
            namespaceMap.put(namespace.getPrefix(), namespace);

            return super.add(namespace);
        }

        @Override
        public void add(int index, Namespace namespace) {
            if (namespace.element != null) {
                throw new IllegalArgumentException();
            }

            if (namespaceMap.containsKey(namespace.getPrefix())) {
                throw new IllegalArgumentException();
            }

            namespace.element = Element.this;
            namespaceMap.put(namespace.getPrefix(), namespace);

            super.add(index, namespace);
        }

        @Override
        public Namespace remove(int index) {
            Namespace namespace = super.remove(index);
            namespaceMap.remove(namespace.getPrefix());
            namespace.element = null;

            return namespace;
        }

        @Override
        public void clear() {
            for (Namespace namespace : this) {
                namespace.element = null;
            }

            namespaceMap.clear();

            super.clear();
        }

        @Override
        public Namespace set(int index, Namespace namespace) {
            throw new UnsupportedOperationException();
        }
    };

    private HashMap<String, Namespace> namespaceMap = new HashMap<String, Namespace>();

    private ArrayList<Attribute> attributes = new ArrayList<Attribute>() {
        private static final long serialVersionUID = 0;

        @Override
        public boolean add(Attribute attribute) {
            if (attribute.element != null) {
                throw new IllegalArgumentException();
            }

            if (attributeMap.containsKey(attribute.getName())) {
                throw new IllegalArgumentException();
            }

            attribute.element = Element.this;
            attributeMap.put(attribute.getName(), attribute);

            return super.add(attribute);
        }

        @Override
        public void add(int index, Attribute attribute) {
            if (attribute.element != null) {
                throw new IllegalArgumentException();
            }

            if (attributeMap.containsKey(attribute.getName())) {
                throw new IllegalArgumentException();
            }

            attribute.element = Element.this;
            attributeMap.put(attribute.getName(), attribute);

            super.add(index, attribute);
        }

        @Override
        public Attribute remove(int index) {
            Attribute attribute = super.remove(index);
            attributeMap.remove(attribute.getName());
            attribute.element = null;

            return attribute;
        }

        @Override
        public void clear() {
            for (Attribute attribute : this) {
                attribute.element = null;
            }

            attributeMap.clear();

            super.clear();
        }

        @Override
        public Attribute set(int index, Attribute attribute) {
            throw new UnsupportedOperationException();
        }
    };

    private HashMap<String, Attribute> attributeMap = new HashMap<String, Attribute>();

    private ArrayList<Node> nodes = new ArrayList<Node>() {
        private static final long serialVersionUID = 0;

        @Override
        public boolean add(Node node) {
            if (node.getParent() != null) {
                throw new IllegalArgumentException();
            }

            node.setParent(Element.this);
            return super.add(node);
        }

        @Override
        public void add(int index, Node node) {
            if (node.getParent() != null) {
                throw new IllegalArgumentException();
            }

            node.setParent(Element.this);
            super.add(index, node);
        }

        @Override
        public Node remove(int index) {
            Node node = super.remove(index);
            node.setParent(null);

            return node;
        }

        @Override
        public void clear() {
            for (Node node : this) {
                node.setParent(null);
            }

            super.clear();
        }

        @Override
        public Node set(int index, Node node) {
            throw new UnsupportedOperationException();
        }
    };

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
        this.defaultNamespaceURI = defaultNamespaceURI;
    }

    /**
     * Returns the element's namespace list.
     */
    public List<Namespace> getNamespaces() {
        return namespaces;
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
            if (namespaceMap.containsKey(prefix)) {
                namespaceURI = namespaceMap.get(prefix).getURI();
            } else {
                namespaceURI = parent.getNamespaceURI(prefix);
            }
        }

        return namespaceURI;
    }

    /**
     * Returns the element's attribute list.
     */
    public List<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * Returns the element's attribute dictionary.
     */
    public String getAttributeValue(String attributeName) {
        Attribute attribute = attributeMap.get(attributeName);
        return (attribute == null) ? null : attribute.getValue();
    }

    /**
     * Returns the element's node list.
     */
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * Returns a descendant element matching a given path.
     *
     * @param path
     * A path of the form:
     * <pre>
     * tag[n]/tag[n]/...
     * </pre>
     * The bracketed index values are optional and refer to the <i>n</i>th
     * occurrence of the given tag name within its parent element. If
     * omitted, the path refers to the first occurrence of the named
     * element (i.e. the element at index 0).
     *
     * @return
     * The matching element, or <tt>null</tt> if no such element exists.
     */
    public Element getElement(String path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        if (path.length() == 0) {
            throw new IllegalArgumentException("path is empty.");
        }

        List<String> pathComponents = Arrays.asList(path.split("/"));
        Element current = this;

        for (int i = 0, n = pathComponents.size(); i < n; i++) {
            String pathComponent = pathComponents.get(i);

            String tagName;
            int index;
            int leadingBracketIndex = pathComponent.indexOf('[');
            if (leadingBracketIndex == -1) {
                tagName = pathComponent;
                index = 0;
            } else {
                tagName = pathComponent.substring(0, leadingBracketIndex);

                int trailingBracketIndex = pathComponent.lastIndexOf(']');
                if (trailingBracketIndex == -1) {
                    throw new IllegalArgumentException("Unterminated index identifier.");
                }

                index = Integer.parseInt(pathComponent.substring(leadingBracketIndex + 1,
                    trailingBracketIndex));
            }


            int j = 0;
            int k = 0;
            for (Node node : current.getNodes()) {
                if (node instanceof Element) {
                    Element element = (Element)node;

                    if (element.getName().equals(tagName)) {
                        if (k == index) {
                            break;
                        }

                        k++;
                    }
                }

                j++;
            }

            if (j < current.getNodes().size()) {
                current = (Element)current.getNodes().get(j);
            } else {
                current = null;
                break;
            }
        }

        return current;
    }

    /**
     * Returns the sub-elements of of this element whose tag names match the
     * given name.
     *
     * @param name
     * The tag name to match.
     *
     * @return
     * A sequence containing the matching elements. The sequence will be empty
     * if no elements matched the given tag name.
     */
    public List<Element> getElements(String name) {
        List<Element> elements = new ArrayList<Element>();

        for (Node node : nodes) {
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
     * Returns the sub-elements of a descendant element whose tag names match
     * the given name.
     *
     * @param path
     * The path to the descendant, relative to this element.
     *
     * @param name
     * The tag name to match.
     *
     * @return
     * The matching elements, or <tt>null</tt> if no such descendant exists.
     *
     * @see #getElement(Element, String)
     * @see #getElements(String)
     */
    public List<Element> getElements(String path, String name) {
        Element element = getElement(path);
        return (element == null) ? null : element.getElements(name);
    }

    /**
     * Returns the text content of this element. An element is defined to
     * contain text when it contains a single child that is an instance of
     * {@link TextNode}.
     *
     * @return
     * The text content of the element, or <tt>null</tt> if this element does
     * not contain text.
     */
    public String getText() {
        String text = null;

        if (nodes.size() == 1) {
            Node node = nodes.get(0);

            if (node instanceof TextNode) {
                TextNode textNode = (TextNode)node;
                text = textNode.getText();
            }
        }

        return text;
    }

    /**
     * Returns the text content of a descendant element.
     *
     * @param path
     * The path to the descendant, relative to this element.
     *
     * @return
     * The text of the descendant, or <tt>null</tt> if no such descendant
     * exists.
     *
     * @see #getElement(Element, String)
     * @see #getText()
     */
    public String getText(String path) {
        Element element = getElement(path);
        return (element == null) ? null : element.getText();
    }

    @Override
    public boolean equals(Object o) {
        boolean equals = false;

        if (this == o) {
            equals = true;
        } else if (o instanceof Element) {
            Element element = (Element)o;
            if (namespacePrefix == null) {
                equals = (element.namespacePrefix == null);
            } else {
                equals = (namespacePrefix.equals(element.namespacePrefix));
            }

            equals &= (attributes.equals(element.attributes)
                && nodes.equals(element.nodes));
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
        result = prime * result + namespaces.hashCode();
        result = prime * result + attributes.hashCode();
        result = prime * result + nodes.hashCode();
        return result;
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
}
