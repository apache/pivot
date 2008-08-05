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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.Iterator;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;

import pivot.beans.BeanDictionary;
import pivot.collections.ArrayList;
import pivot.collections.ArrayStack;
import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.serialization.Serializer;
import pivot.serialization.SerializationException;
import pivot.util.ListenerList;

/**
 * Loads an object hierarchy from XML.
 *
 * TODO Use a linked stack to process nodes.
 *
 * @author gbrown
 */
public class WTKXSerializer implements Serializer {
    public static class Element implements Dictionary<String, Object>, List<Object> {
        private class ItemIterator implements Iterator<Object> {
            Iterator<Object> source = null;

            public ItemIterator(Iterator<Object> source) {
                this.source = source;
            }

            public boolean hasNext() {
                return source.hasNext();
            }

            public Object next() {
                return source.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

        private HashMap<String, Object> dictionary = new HashMap<String, Object>();
        private ArrayList<Object> list = new ArrayList<Object>();

        private ListListenerList<Object> listListeners = new ListListenerList<Object>();

        // Dictionary methods
        public Object get(String key) {
            return dictionary.get(key);
        }

        public Object put(String key, Object value) {
            return dictionary.put(key, value);
        }

        public Object remove(String key) {
            return dictionary.remove(key);
        }

        public boolean containsKey(String key) {
            return dictionary.containsKey(key);
        }

        public boolean isEmpty() {
            return dictionary.isEmpty();
        }

        public Iterator<String> getKeys() {
            return dictionary.iterator();
        }

        // List methods
        public int add(Object item) {
            int index = getLength();
            insert(item, index);

            return index;
        }

        public void insert(Object item, int index) {
            list.insert(item, index);
            listListeners.itemInserted(this, index);
        }

        public int remove(Object item) {
            int index = indexOf(item);
            remove(index, 1);

            return index;
        }

        public Sequence<Object> remove(int index, int count) {
            Sequence<Object> removed = list.remove(index, count);
            listListeners.itemsRemoved(this, index, removed);

            return removed;
        }

        public void clear() {
            list.clear();
            listListeners.itemsRemoved(this, 0, null);
        }

        public Object update(int index, Object item) {
            Object previousItem = list.update(index, item);
            listListeners.itemUpdated(list, index, previousItem);

            return previousItem;
        }

        public Object get(int index) {
            return list.get(index);
        }

        public int indexOf(Object item) {
            return list.indexOf(item);
        }

        public int getLength() {
            return list.getLength();
        }

        public Comparator<Object> getComparator() {
            return list.getComparator();
        }

        public void setComparator(Comparator<Object> comparator) {
            Comparator<Object> previousComparator = list.getComparator();
            list.setComparator(comparator);
            listListeners.comparatorChanged(this, previousComparator);
        }

        public Iterator<Object> iterator() {
            return new ItemIterator(list.iterator());
        }

        public ListenerList<ListListener<Object>> getListListeners() {
            return listListeners;
        }
    }

    private static class Node {
        public final boolean typed;
        public final Object value;
        public final HashMap<String, String> attributes = new HashMap<String, String>();

        public Node(boolean typed, Object value) {
            this.typed = typed;
            this.value = value;
        }
    }

    private URL location = null;
    private Dictionary<String, ?> resources = null;

    private HashMap<String, Object> namedObjects = new HashMap<String, Object>();
    private HashMap<String, WTKXSerializer> includeSerializers = new HashMap<String, WTKXSerializer>();

    public static final String URL_PREFIX = "@";
    public static final String RESOURCE_KEY_PREFIX = "%";
    public static final String OBJECT_REFERENCE_PREFIX = "$";

    public static final String WTKX_PREFIX = "wtkx";
    public static final String ID_ATTRIBUTE = "id";
    public static final String INCLUDE_TAG = "include";
    public static final String INCLUDE_SRC_ATTRIBUTE = "src";
    public static final String INCLUDE_NAMESPACE_ATTRIBUTE = "namespace";

    public static final String MIME_TYPE = "application/wtkx";

    public WTKXSerializer() {
        this(null);
    }

    public WTKXSerializer(Dictionary<String, ?> resources) {
        this.resources = resources;
    }

    public Object readObject(String resourceName) throws IOException,
        SerializationException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL location = classLoader.getResource(resourceName);

        if (location == null) {
            throw new SerializationException("Could not find resource named \""
                + resourceName + "\".");
        }

        return readObject(location);
    }

    public Object readObject(URL location) throws IOException, SerializationException {
        this.location = location;

        return readObject(new BufferedInputStream(location.openStream()));
    }

    @SuppressWarnings("unchecked")
    public Object readObject(InputStream inputStream) throws IOException,
        SerializationException {
        // Clear any previous named objects and include serializers
        namedObjects.clear();
        includeSerializers.clear();

        Object object = null;
        ArrayStack<Node> nodeStack = new ArrayStack<Node>();

        XMLInputFactory factory = XMLInputFactory.newInstance();

        try {
            XMLStreamReader reader = factory.createXMLStreamReader(inputStream);

            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.START_ELEMENT: {
                        Node node = null;
                        String prefix = reader.getPrefix();
                        String localName = reader.getLocalName();

                        if (prefix != null
                            && prefix.equals(WTKX_PREFIX)) {
                            if (localName.equals(INCLUDE_TAG)) {
                                // The element represents an include
                                node = new Node(true, null);
                            } else {
                                throw new SerializationException("<" + WTKX_PREFIX + ":"
                                    + localName + "> is not a valid tag.");
                            }
                        } else if (Character.isLowerCase(localName.charAt(0))) {
                            // The element represents a property
                            Object value = null;
                            Node parentNode = nodeStack.peek();

                            if (parentNode.typed) {
                                BeanDictionary beanDictionary = new BeanDictionary(parentNode.value);
                                Class<?> propertyType = beanDictionary.getType(localName);

                                // If the property is a read-only sequence, use the
                                // sequence as the node value; otherwise, create a
                                // new Element instance for the node value
                                if (Sequence.class.isAssignableFrom(propertyType)
                                    && beanDictionary.isReadOnly(localName)) {
                                    value = beanDictionary.get(localName);
                                    assert (value != null) :
                                        "Read-only sequence properties cannot be null.";
                                } else {
                                    value = new Element();
                                }
                            } else {
                                // Create an Element to represent the node value
                                value = new Element();
                            }

                            node = new Node(false, value);
                        } else {
                            // The element represents a typed object
                            String namespaceURI = reader.getNamespaceURI();
                            String className = namespaceURI + "."
                                + localName.replaceAll("\\.", "$");

                            Object value = null;
                            try {
                                Class<?> type = Class.forName(className);

                                // TODO If type represents a (non-static) inner class,
                                // walk up the node list to find an instance of the
                                // enclosing class
                                value = type.newInstance();
                            } catch(Exception exception) {
                                throw new SerializationException(exception);
                            }

                            node = new Node(true, value);
                        }

                        // Retrieve the attributes, which will be applied
                        // while processing the closing tag
                        for (int i = 0, n = reader.getAttributeCount(); i < n; i++) {
                            String attributePrefix = reader.getAttributePrefix(i);
                            String attributeLocalName = reader.getAttributeLocalName(i);
                            String attributeValue = reader.getAttributeValue(i);

                            if (attributePrefix != null
                                && attributePrefix.equals(WTKX_PREFIX)) {
                                if (attributeLocalName.equals(ID_ATTRIBUTE)) {
                                    namedObjects.put(attributeValue, node.value);
                                } else {
                                    throw new SerializationException(WTKX_PREFIX + ":"
                                        + attributeLocalName + " is not a valid attribute.");
                                }
                            } else {
                                node.attributes.put(attributeLocalName, attributeValue);
                            }
                        }

                        // Push the node onto the stack
                        nodeStack.push(node);

                        break;
                    }

                    case XMLStreamConstants.END_ELEMENT: {
                        String localName = reader.getLocalName();

                        // Get the node for this element
                        Node node = nodeStack.pop();

                        if (node.typed
                            && node.value == null) {
                            // This node represents an include; remove the src
                            // and namespace attributes so we don't try to set
                            // them as properties later
                            String src = node.attributes.remove(INCLUDE_SRC_ATTRIBUTE);
                            String namespace = node.attributes.remove(INCLUDE_NAMESPACE_ATTRIBUTE);

                            // Process the include
                            WTKXSerializer serializer =
                                new WTKXSerializer((Dictionary<String, Object>)resources.get(namespace));
                            includeSerializers.put(namespace, serializer);

                            node = new Node(true, serializer.readObject(new URL(location, src)));
                        }

                        if (nodeStack.getLength() == 0) {
                            // This is the last element; return the node value
                            object = node.value;
                        } else {
                            // If the parent node is typed and this is a property
                            // element (beginning with a lowercase letter), set
                            // the property value; otherwise, add the node value
                            // to the untyped parent sequence
                            Node parentNode = nodeStack.peek();

                            if (parentNode.typed) {
                                // If this is a property element, try to set the
                                // property on the parent node; otherwise, just
                                // ignore the instantiated object (it may be
                                // referenced elsewhere via ID)
                                if (Character.isLowerCase(localName.charAt(0))) {
                                    BeanDictionary beanDictionary =
                                        new BeanDictionary(parentNode.value);
                                    Class<?> propertyType = beanDictionary.getType(localName);

                                    if (Sequence.class.isAssignableFrom(propertyType)) {
                                        if (!beanDictionary.isReadOnly(localName)) {
                                            // The property refers to a writable sequence;
                                            // set the property value to this node's value
                                            beanDictionary.put(localName, node.value);
                                        }
                                    } else {
                                        // The property is not a sequence; set the property
                                        // value to the first sub-item of this element
                                        assert(node.value instanceof Element) :
                                            "Node value is not an element.";

                                        Element element = (Element)node.value;
                                        beanDictionary.put(localName, element.get(0));
                                    }
                                }
                            } else {
                                // Append the value to the parent sequence; it is the caller's
                                // responsibility to ensure that the value is of the correct
                                // type for the sequence
                                Sequence<Object> sequence = (Sequence<Object>)parentNode.value;
                                sequence.add(node.value);
                            }
                        }

                        // Set the properties
                        if (!node.attributes.isEmpty()) {
                            if (node.typed) {
                                BeanDictionary beanDictionary = new BeanDictionary(node.value);

                                for (String attributeLocalName : node.attributes) {
                                    String attributeValue = node.attributes.get(attributeLocalName);

                                    if (Character.isUpperCase(attributeLocalName.charAt(0))) {
                                        setStaticProperty(attributeLocalName, attributeValue, node.value);
                                    } else {
                                        Class<?> propertyType = beanDictionary.getType(attributeLocalName);
                                        Object propertyValue = resolve(attributeValue, propertyType);
                                        beanDictionary.put(attributeLocalName, propertyValue);
                                    }
                                }
                            } else {
                                Dictionary<String, Object> dictionary =
                                    (Dictionary<String, Object>)node.value;

                                for (String attributeLocalName : node.attributes) {
                                    String attributeValue = node.attributes.get(attributeLocalName);
                                    dictionary.put(attributeLocalName,
                                        resolve(attributeValue, Object.class));
                                }
                            }
                        }

                        break;
                    }
                }
            }

            reader.close();
        } catch(XMLStreamException exception) {
            throw new SerializationException(exception);
        }

        // Clear the location so the previous value won't be re-used in a
        // subsequent call to this method
        location = null;

        return object;
    }

    public void writeObject(Object object, OutputStream outputStream) throws IOException,
        SerializationException {
        throw new UnsupportedOperationException();
    }

    public String getMIMEType() {
        return MIME_TYPE;
    }

    /**
     * Retrieves a named object.
     *
     * @param name
     * The name of the object, relative to this loader. The values's name is the
     * concatentation of its parent namespaces and its ID, separated by periods
     * (e.g. "foo.bar.baz").
     *
     * @return The named object, or <tt>null</tt> if an object with the given
     * name does not exist.
     */
    public Object getObjectByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null.");
        }

        Object object = null;
        WTKXSerializer serializer = this;
        String[] namespacePath = name.split("\\.");

        int i = 0;
        int n = namespacePath.length - 1;
        while (i < n && serializer != null) {
            String namespace = namespacePath[i++];
            serializer = serializer.includeSerializers.get(namespace);
        }

        if (serializer != null) {
            object = serializer.namedObjects.get(namespacePath[i]);
        }

        return object;
    }

    private Object resolve(String attributeValue, Class<?> propertyType)
        throws MalformedURLException {
        Object resolvedValue = null;

        if (propertyType == Boolean.class
            || propertyType == Boolean.TYPE) {
            resolvedValue = Boolean.parseBoolean(attributeValue);
        } else if (propertyType == Character.class
            || propertyType == Character.TYPE) {
            resolvedValue = attributeValue.charAt(0);
        } else if (propertyType == Byte.class
            || propertyType == Byte.TYPE) {
            resolvedValue = Byte.parseByte(attributeValue);
        } else if (propertyType == Short.class
            || propertyType == Short.TYPE) {
            resolvedValue = Short.parseShort(attributeValue);
        } else if (propertyType == Integer.class
            || propertyType == Integer.TYPE) {
            resolvedValue = Integer.parseInt(attributeValue);
        } else if (propertyType == Long.class
            || propertyType == Long.TYPE) {
            resolvedValue = Long.parseLong(attributeValue);
        } else if (propertyType == Float.class
            || propertyType == Float.TYPE) {
            resolvedValue = Float.parseFloat(attributeValue);
        } else if (propertyType == Double.class
            || propertyType == Double.TYPE) {
            resolvedValue = Double.parseDouble(attributeValue);
        } else {
            if (attributeValue.startsWith(URL_PREFIX)) {
                if (location == null) {
                    throw new IllegalStateException("Base location is undefined.");
                }

                resolvedValue = new URL(location, attributeValue.substring(1));
            } else if (attributeValue.startsWith(RESOURCE_KEY_PREFIX)) {
                if (resources == null) {
                    throw new IllegalStateException("Resource dictionary is undefined.");
                }

                resolvedValue = resources.get(attributeValue.substring(1));
            } else if (attributeValue.startsWith(OBJECT_REFERENCE_PREFIX)) {
                resolvedValue = namedObjects.get(attributeValue.substring(1));
            } else {
                resolvedValue = attributeValue;
            }
        }

        return resolvedValue;
    }

    private void setStaticProperty(String attributeLocalName, String attributeValue, Object object)
        throws SerializationException, MalformedURLException {
        String propertyName =
            attributeLocalName.substring(attributeLocalName.lastIndexOf("."));
        propertyName = Character.toUpperCase(propertyName.charAt(0)) +
            propertyName.substring(1);

        String propertyClassName = attributeLocalName.substring(0, propertyName.length());

        Class<?> propertyClass = null;
        try {
            propertyClass = Class.forName(propertyClassName);
        } catch(ClassNotFoundException exception) {
            throw new SerializationException(exception);
        }

        Class<?> objectType = object.getClass();

        // Determine the property type from the getter method
        Method getterMethod = null;
        try {
            getterMethod = propertyClass.getMethod(BeanDictionary.GET_PREFIX
                + propertyName, new Class<?>[] {objectType});
        } catch(NoSuchMethodException exception) {
            // No-op
        }

        if (getterMethod == null) {
            try {
                getterMethod = propertyClass.getMethod(BeanDictionary.IS_PREFIX
                    + propertyName, new Class<?>[] {objectType});
            } catch(NoSuchMethodException exception) {
                // No-op
            }
        }

        if (getterMethod == null) {
            throw new SerializationException("Unable to determine type of "
                + " static property \"" + attributeLocalName + "\".");
        }

        // Resolve the attribute value
        Class<?> propertyType = getterMethod.getReturnType();
        Object propertyValue = resolve(attributeValue, propertyType);

        final String setterMethodName = BeanDictionary.SET_PREFIX + propertyName;
        Class<?> propertyValueType = propertyValue.getClass();

        Method setterMethod = null;
        try {
            setterMethod = propertyClass.getMethod(setterMethodName,
                new Class<?>[] {objectType, propertyValueType});
        } catch(NoSuchMethodException exception) {
            // No-op
        }

        if (setterMethod == null) {
            // If value type is a primitive wrapper, look for a method
            // signature with the corresponding primitive type
            try {
                Field primitiveTypeField = propertyValueType.getField("TYPE");
                Class<?> primitivePropertyValueType = (Class<?>)primitiveTypeField.get(this);

                try {
                    setterMethod = propertyClass.getMethod(setterMethodName,
                        new Class<?>[] {objectType, primitivePropertyValueType});
                } catch(NoSuchMethodException exception) {
                    // No-op
                }
            } catch(NoSuchFieldException exception) {
                // No-op; not a wrapper type
            } catch(IllegalAccessException exception) {
                // No-op
            }
        }

        if (setterMethod == null) {
            throw new SerializationException("Static property type \""
                + attributeLocalName + "\" does not exist or is read-only.");
        }

        // Invoke the setter
        try {
            setterMethod.invoke(null, new Object[] {object, propertyValue});
        } catch(Exception exception) {
            throw new SerializationException(exception);
        }
    }
}
