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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;

import pivot.beans.BeanDictionary;
import pivot.beans.PropertyNotFoundException;
import pivot.collections.ArrayList;
import pivot.collections.ArrayStack;
import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.collections.List;
import pivot.collections.Sequence;
import pivot.serialization.Serializer;
import pivot.serialization.SerializationException;

/**
 * <p>Loads an object hierarchy from an XML document.</p>
 *
 * <p>TODO Use a linked stack to process nodes instead of an array stack.</p>
 *
 * @author gbrown
 */
public class WTKXSerializer implements Serializer {
    private static class Attribute {
        final String prefix;
        final String namespace;
        final String localName;
        final String value;

        public Attribute(String prefix, String namespace, String localName, String value,
            XMLStreamReader reader) {
            if (value.length() == 0) {
                throw new IllegalArgumentException("Attribute value is empty.");
            }

            this.prefix = prefix;
            this.namespace = (namespace == null) ? reader.getNamespaceURI("") : namespace;
            this.localName = localName;
            this.value = value;
        }

        public String toString() {
            return (prefix == null) ? localName : prefix + ":" + localName;
        }
    }

    private static class Node {
        public final Object value;
        public final List<Attribute> attributes;

        public Node(Object value, List<Attribute> attributes) {
            this.value = value;
            this.attributes = attributes;
        }
    }

    private URL location = null;
    private Dictionary<String, Object> resources = null;

    private HashMap<String, Object> namedObjects = new HashMap<String, Object>();
    private HashMap<String, WTKXSerializer> includeSerializers = new HashMap<String, WTKXSerializer>();

    public static final char URL_PREFIX = '@';
    public static final char RESOURCE_KEY_PREFIX = '%';
    public static final char OBJECT_REFERENCE_PREFIX = '$';

    public static final String WTKX_PREFIX = "wtkx";
    public static final String ID_ATTRIBUTE = "id";

    public static final String INCLUDE_TAG = "include";
    public static final String INCLUDE_SRC_ATTRIBUTE = "src";
    public static final String INCLUDE_NAMESPACE_ATTRIBUTE = "namespace";

    public static final String NULL_TAG = "null";

    public static final String MIME_TYPE = "application/wtkx";

    public WTKXSerializer() {
        this(null);
    }

    public WTKXSerializer(Dictionary<String, Object> resources) {
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

        // Parse the XML stream
        Object object = null;
        ArrayStack<Node> nodeStack = new ArrayStack<Node>();

        XMLInputFactory factory = XMLInputFactory.newInstance();

        try {
            XMLStreamReader reader = factory.createXMLStreamReader(inputStream);

            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.START_ELEMENT: {
                        Object nodeValue = null;
                        ArrayList<Attribute> nodeAttributes = new ArrayList<Attribute>();

                        Node parentNode = (nodeStack.getLength() > 0) ? nodeStack.peek() : null;

                        if (parentNode != null
                            && parentNode.value == null) {
                            throw new SerializationException("Child elements are not allowed for "
                                + WTKX_PREFIX + ":" + NULL_TAG + " tag.");
                        }

                        BeanDictionary parentBeanDictionary = null;

                        String prefix = reader.getPrefix();
                        String localName = reader.getLocalName();
                        String tagName = (prefix == null ? "" : prefix + ":") + localName;

                        String id = null;

                        if (prefix != null
                            && prefix.equals(WTKX_PREFIX)) {
                            if (localName.equals(INCLUDE_TAG)) {
                                // The element represents an include
                                String src = null;
                                String namespace = null;

                                // Walk the attribute list and extract src and namespace;
                                // append all other attributes to the attributes list
                                for (int i = 0, n = reader.getAttributeCount(); i < n; i++) {
                                    Attribute attribute = new Attribute(reader.getAttributePrefix(i),
                                        reader.getAttributeNamespace(i),
                                        reader.getAttributeLocalName(i),
                                        reader.getAttributeValue(i), reader);

                                    if (attribute.localName.equals(INCLUDE_SRC_ATTRIBUTE)) {
                                        src = attribute.value;
                                    } else if (attribute.localName.equals(INCLUDE_NAMESPACE_ATTRIBUTE)) {
                                        namespace = attribute.value;
                                    } else if (attribute.localName.equals(ID_ATTRIBUTE)) {
                                        id = attribute.value;
                                    } else {
                                        nodeAttributes.add(attribute);
                                    }
                                }

                                if (src == null) {
                                    throw new SerializationException(INCLUDE_SRC_ATTRIBUTE
                                        + " attribute is required for " + WTKX_PREFIX + ":" + INCLUDE_TAG
                                        + " tag.");
                                }

                                // Process the include
                                Dictionary<String, Object> includeResources = resources;
                                if (includeResources != null
                                    && includeResources.containsKey(namespace)) {
                                    includeResources = (Dictionary<String, Object>)includeResources.get(namespace);
                                }

                                WTKXSerializer serializer = new WTKXSerializer(includeResources);
                                if (namespace != null) {
                                    includeSerializers.put(namespace, serializer);
                                }

                                nodeValue = serializer.readObject(new URL(location, src));
                            } else if (localName.equals(NULL_TAG)) {
                                // The element represents a null value
                                if (nodeAttributes.getLength() > 0) {
                                    throw new SerializationException("Attributes are not allowed for "
                                        + WTKX_PREFIX + ":" + NULL_TAG + " tag.");
                                }
                            } else {
                                throw new SerializationException("<" + WTKX_PREFIX + ":"
                                    + localName + "> is not a valid tag.");
                            }
                        } else {
                            for (int i = 0, n = reader.getAttributeCount(); i < n; i++) {
                                Attribute attribute = new Attribute(reader.getAttributePrefix(i),
                                    reader.getAttributeNamespace(i),
                                    reader.getAttributeLocalName(i),
                                    reader.getAttributeValue(i),
                                    reader);

                                if (attribute.prefix != null
                                    && attribute.prefix.equals(WTKX_PREFIX)) {
                                    if (attribute.localName.equals(ID_ATTRIBUTE)) {
                                        id = attribute.value;
                                    }
                                } else {
                                    nodeAttributes.add(attribute);
                                }
                            }

                            if (Character.isUpperCase(localName.charAt(0))) {
                                // The element represents a typed object
                                String namespaceURI = reader.getNamespaceURI();
                                if (namespaceURI == null) {
                                    throw new SerializationException("No XML namespace specified for "
                                        + localName + " tag.");
                                }

                                String className = namespaceURI + "."
                                    + localName.replace('.', '$');

                                try {
                                    Class<?> type = Class.forName(className);
                                    Class<?> enclosingClass = type.getEnclosingClass();

                                    if (enclosingClass == null
                                        || (type.getModifiers() & Modifier.STATIC) > 0) {
                                        nodeValue = type.newInstance();
                                    } else {
                                        // The type represents an inner class; walk up the node
                                        // list to find an instance of the enclosing class
                                        Object outer = null;

                                        int i = nodeStack.getLength() - 1;
                                        while (i >= 0
                                            && outer == null) {
                                            Node ancestorNode = nodeStack.get(i--);
                                            if (enclosingClass.isAssignableFrom(ancestorNode.value.getClass())) {
                                                outer = ancestorNode.value;
                                            }
                                        }

                                        if (outer == null) {
                                            throw new SerializationException("No enclosing instance of type "
                                                + enclosingClass.getName() + " was found.");
                                        }

                                        // Get a reference to a constructor for this type that takes
                                        // an argument of the type represented by enclosingClass and
                                        // invoke it
                                        Constructor<?> constructor =
                                            type.getConstructor(new Class<?>[] {enclosingClass});
                                        nodeValue = constructor.newInstance(outer);
                                    }
                                } catch(Exception exception) {
                                    throw new SerializationException(exception);
                                }
                            } else {
                                if (parentNode == null) {
                                    throw new SerializationException("Root node must represent a typed object.");
                                }

                                if (parentNode.value instanceof Element) {
                                    // The element represents a nested untyped object
                                    nodeValue = new Element(tagName);
                                } else {
                                    // The element represents a property of a typed parent object
                                    parentBeanDictionary = new BeanDictionary(parentNode.value);
                                    Class<?> propertyType = parentBeanDictionary.getType(localName);

                                    if (Sequence.class.isAssignableFrom(propertyType)
                                        && parentBeanDictionary.isReadOnly(localName)) {
                                        // The element represents a read-only sequence property
                                        nodeValue = parentBeanDictionary.get(localName);
                                        assert (nodeValue != null) :
                                            "Read-only sequence properties cannot be null.";
                                    } else {
                                        // The element represents a writable property; the property
                                        // value will be set when the closing tag is processed
                                        nodeValue = new Element(tagName);
                                    }
                                }
                            }

                        }

                        // If the node has an ID, add its value to the named
                        // object map
                        if (id != null) {
                            namedObjects.put(id, nodeValue);
                        }

                        // If the element does not represent a property and the parent node
                        // is a sequence, append the value to the parent
                        if (parentBeanDictionary == null
                            && parentNode != null
                            && parentNode.value instanceof Sequence) {
                            // NOTE It is the caller's responsibility to ensure that the value
                            // is of the correct type for the sequence
                            Sequence<Object> sequence = (Sequence<Object>)parentNode.value;
                            sequence.add(nodeValue);
                        }

                        // Append the node to the node stack
                        nodeStack.push(new Node(nodeValue, nodeAttributes));

                        break;
                    }

                    case XMLStreamConstants.END_ELEMENT: {
                        String prefix = reader.getPrefix();
                        String localName = reader.getLocalName();
                        String tagName = (prefix == null ? "" : prefix + ":") + localName;

                        // Get the node for this element
                        Node node = nodeStack.pop();
                        Node parentNode = (nodeStack.getLength() > 0) ? nodeStack.peek() : null;

                        // If the node represents a writable property of a typed parent
                        // element and the property type matches the node value type, set
                        // the property to the node value; otherwise, set the property to
                        // the first child of the node value (which is assumed to be an
                        // instance of Element)
                        if (parentNode != null
                            && !(parentNode.value instanceof Element)
                            && (prefix == null
                                || !prefix.equals(WTKX_PREFIX))
                            && Character.isLowerCase(localName.charAt(0))) {
                            BeanDictionary parentBeanDictionary = new BeanDictionary(parentNode.value);
                            Class<?> propertyType = parentBeanDictionary.getType(localName);

                            if (node.value == null
                                || propertyType.isAssignableFrom(node.value.getClass())) {
                                if (!parentBeanDictionary.isReadOnly(localName)) {
                                    parentBeanDictionary.put(localName, node.value);
                                }
                            } else {
                                assert(node.value instanceof Element) : "Node value is not an element.";

                                Element element = (Element)node.value;
                                if (element.getLength() > 0) {
                                    parentBeanDictionary.put(localName, element.get(0));
                                }
                            }
                        }

                        // Apply the attributes
                        if (node.value != null) {
                            if (node.value instanceof Dictionary) {
                                Dictionary<String, Object> nodeDictionary = (Dictionary<String, Object>)node.value;

                                for (Attribute attribute : node.attributes) {
                                    Object propertyValue = resolve(attribute.value, Object.class);
                                    nodeDictionary.put(attribute.localName, propertyValue);
                                }
                            } else {
                                BeanDictionary beanDictionary = new BeanDictionary(node.value);

                                for (Attribute attribute : node.attributes) {
                                    try {
                                        if (Character.isUpperCase(attribute.localName.charAt(0))) {
                                            setStaticProperty(attribute, node.value);
                                        } else {
                                            Class<?> propertyType;
                                            propertyType = beanDictionary.getType(attribute.localName);

                                            Object propertyValue = resolve(attribute.value, propertyType);
                                            beanDictionary.put(attribute.localName, propertyValue);
                                        }
                                    } catch(PropertyNotFoundException exception) {
                                        System.out.println(attribute + " is not a valid attribute for the "
                                            + tagName + " tag.");
                                    }
                                }
                            }
                        }

                        // If the stack is empty, return the node value
                        if (nodeStack.getLength() == 0) {
                            object = node.value;
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
     * Retrieves a included serializer by its namespace.
     *
     * @param name
     * The name of the serializer, relative to this loader. The values's name
     * is the concatentation of its parent namespaces and its namespace,
     * separated by periods (e.g. "foo.bar.baz").
     *
     * @return The named serializer, or <tt>null</tt> if a serializer with the
     * given name does not exist.
     */
    public WTKXSerializer getSerializerByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null.");
        }

        WTKXSerializer serializer = this;
        String[] namespacePath = name.split("\\.");

        int i = 0;
        int n = namespacePath.length;
        while (i < n && serializer != null) {
            String namespace = namespacePath[i++];
            serializer = serializer.includeSerializers.get(namespace);
        }

        return serializer;
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

    /**
     * Resolves an attribute value. If the property type is a primitive or
     * primitive wrapper, converts the string value to the primitive type.
     * Otherwise, resolves the value as either a URL, resource value, or
     * object reference, depending on the value's prefix. If the value can't
     * or doesn't need to be resolved, the original attribute value is
     * returned.
     *
     * @param attributeValue
     * The attribute value to resolve.
     *
     * @param propertyType
     * The property type.
     *
     * @return
     * The resolved value.
     */
    private Object resolve(String attributeValue, Class<?> propertyType)
        throws MalformedURLException {
        Object resolvedValue = null;

        if (propertyType == Boolean.class
            || propertyType == Boolean.TYPE) {
            try {
                resolvedValue = Boolean.parseBoolean(attributeValue);
            } catch(NumberFormatException exception) {
                resolvedValue = attributeValue;
            }
        } else if (propertyType == Character.class
            || propertyType == Character.TYPE) {
            if (attributeValue.length() > 0) {
                resolvedValue = attributeValue.charAt(0);
            }
        } else if (propertyType == Byte.class
            || propertyType == Byte.TYPE) {
            try {
                resolvedValue = Byte.parseByte(attributeValue);
            } catch(NumberFormatException exception) {
                resolvedValue = attributeValue;
            }
        } else if (propertyType == Short.class
            || propertyType == Short.TYPE) {
            try {
                resolvedValue = Short.parseShort(attributeValue);
            } catch(NumberFormatException exception) {
                resolvedValue = attributeValue;
            }
        } else if (propertyType == Integer.class
            || propertyType == Integer.TYPE) {
            try {
                resolvedValue = Integer.parseInt(attributeValue);
            } catch(NumberFormatException exception) {
                resolvedValue = attributeValue;
            }
        } else if (propertyType == Long.class
            || propertyType == Long.TYPE) {
            try {
                resolvedValue = Long.parseLong(attributeValue);
            } catch(NumberFormatException exception) {
                resolvedValue = attributeValue;
            }
        } else if (propertyType == Float.class
            || propertyType == Float.TYPE) {
            try {
                resolvedValue = Float.parseFloat(attributeValue);
            } catch(NumberFormatException exception) {
                resolvedValue = attributeValue;
            }
        } else if (propertyType == Double.class
            || propertyType == Double.TYPE) {
            try {
                resolvedValue = Double.parseDouble(attributeValue);
            } catch(NumberFormatException exception) {
                resolvedValue = attributeValue;
            }
        } else {
            if (attributeValue.charAt(0) == URL_PREFIX) {
                if (location == null) {
                    throw new IllegalStateException("Base location is undefined.");
                }

                if (attributeValue.length() > 1) {
                    if (attributeValue.charAt(1) == URL_PREFIX) {
                        resolvedValue = attributeValue.substring(1);
                    } else {
                        resolvedValue = new URL(location, attributeValue.substring(1));
                    }
                }
            } else if (attributeValue.charAt(0) == RESOURCE_KEY_PREFIX) {
                if (resources == null) {
                    throw new IllegalStateException("Resource dictionary is undefined.");
                }

                if (attributeValue.length() > 1) {
                    if (attributeValue.charAt(1) == RESOURCE_KEY_PREFIX) {
                        resolvedValue = attributeValue.substring(1);
                    } else {
                        resolvedValue = resources.get(attributeValue.substring(1));
                    }
                }
            } else if (attributeValue.charAt(0) == OBJECT_REFERENCE_PREFIX) {
                if (attributeValue.length() > 1) {
                    if (attributeValue.charAt(1) == OBJECT_REFERENCE_PREFIX) {
                        resolvedValue = attributeValue.substring(1);
                    } else {
                        resolvedValue = namedObjects.get(attributeValue.substring(1));
                    }
                }
            } else {
                resolvedValue = attributeValue;
            }
        }

        return resolvedValue;
    }

    /**
     * Invokes a static property setter.
     *
     * @param attributeLocalName
     * The attribute name, in the form "ClassName.propertyName".
     *
     * @param attributeValue
     * The attribute value.
     *
     * @param object
     * The object on which to invoke the static setter.
     */
    private void setStaticProperty(Attribute attribute, Object object)
        throws SerializationException, MalformedURLException {
        String propertyName =
            attribute.localName.substring(attribute.localName.lastIndexOf(".") + 1);
        propertyName = Character.toUpperCase(propertyName.charAt(0)) +
            propertyName.substring(1);

        String propertyClassName = attribute.namespace + "."
            + attribute.localName.substring(0, attribute.localName.length()
                - (propertyName.length() + 1));

        Class<?> propertyClass = null;
        try {
            propertyClass = Class.forName(propertyClassName);
        } catch(ClassNotFoundException exception) {
            throw new SerializationException(exception);
        }

        Class<?> objectType = object.getClass();

        // Determine the property type from the getter method
        Method getterMethod = getStaticGetterMethod(propertyClass, propertyName, objectType);
        if (getterMethod == null) {
            throw new PropertyNotFoundException("Static property \"" + attribute
                + "\" does not exist.");
        }

        // Resolve the attribute value
        Class<?> propertyType = getterMethod.getReturnType();
        Object propertyValue = resolve(attribute.value, propertyType);
        Class<?> propertyValueType = (propertyValue == null) ?
            getterMethod.getReturnType() : propertyValue.getClass();

        Method setterMethod = getStaticSetterMethod(propertyClass, propertyName,
            objectType, propertyValueType);

        if (setterMethod == null) {
            throw new SerializationException("Unable to determine type for "
                + " static property \"" + attribute + "\".");
        }

        // Invoke the setter
        try {
            setterMethod.invoke(null, new Object[] {object, propertyValue});
        } catch(Exception exception) {
            throw new SerializationException(exception);
        }
    }

    private Method getStaticGetterMethod(Class<?> propertyClass, String propertyName,
        Class<?> objectType) {
        Method method = null;

        if (objectType != null) {
            try {
                method = propertyClass.getMethod(BeanDictionary.GET_PREFIX
                    + propertyName, new Class<?>[] {objectType});
            } catch(NoSuchMethodException exception) {
                // No-op
            }

            if (method == null) {
                try {
                    method = propertyClass.getMethod(BeanDictionary.IS_PREFIX
                        + propertyName, new Class<?>[] {objectType});
                } catch(NoSuchMethodException exception) {
                    // No-op
                }
            }

            if (method == null) {
                method = getStaticGetterMethod(propertyClass, propertyName,
                    objectType.getSuperclass());
            }
        }

        return method;
    }

    private Method getStaticSetterMethod(Class<?> propertyClass, String propertyName,
        Class<?> objectType, Class<?> propertyValueType) {
        Method method = null;

        if (objectType != null) {
            final String methodName = BeanDictionary.SET_PREFIX + propertyName;

            try {
                method = propertyClass.getMethod(methodName,
                    new Class<?>[] {objectType, propertyValueType});
            } catch(NoSuchMethodException exception) {
                // No-op
            }

            if (method == null) {
                // If value type is a primitive wrapper, look for a method
                // signature with the corresponding primitive type
                try {
                    Field primitiveTypeField = propertyValueType.getField("TYPE");
                    Class<?> primitivePropertyValueType = (Class<?>)primitiveTypeField.get(this);

                    try {
                        method = propertyClass.getMethod(methodName,
                            new Class<?>[] {objectType, primitivePropertyValueType});
                    } catch(NoSuchMethodException exception) {
                        // No-op
                    }
                } catch(NoSuchFieldException exception) {
                    // No-op; not a wrapper type
                } catch(IllegalAccessException exception) {
                    // No-op; not a wrapper type
                }
            }

            if (method == null) {
                method = getStaticSetterMethod(propertyClass, propertyName,
                    objectType.getSuperclass(), propertyValueType);
            }
        }

        return method;
    }
}
