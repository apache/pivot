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
package org.apache.pivot.wtkx;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;

import org.apache.pivot.beans.BeanDictionary;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.serialization.Serializer;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.ThreadUtilities;
import org.apache.pivot.util.Vote;

/**
 * Loads an object hierarchy from an XML document.
 */
public class WTKXSerializer implements Serializer<Object>, Dictionary<String, Object> {
    private class NamedObjectBindings implements Bindings {
        @Override
        public Object get(Object key) {
            return namedObjects.get(key.toString());
        }

        @Override
        public Object put(String key, Object value) {
            return namedObjects.put(key, value);
        }

        @Override
        public void putAll(Map<? extends String, ? extends Object> map) {
            for (String key : map.keySet()) {
                put(key, map.get(key));
            }
        }

        @Override
        public Object remove(Object key) {
            return namedObjects.remove(key.toString());
        }

        @Override
        public void clear() {
            namedObjects.clear();
        }

        @Override
        public boolean containsKey(Object key) {
            return namedObjects.containsKey(key.toString());
        }

        @Override
        public boolean containsValue(Object value) {
            boolean contains = false;
            for (String key : namedObjects) {
                if (namedObjects.get(key).equals(value)) {
                    contains = true;
                    break;
                }
            }

            return contains;
        }

        @Override
        public boolean isEmpty() {
            return namedObjects.isEmpty();
        }

        @Override
        public Set<String> keySet() {
            java.util.HashSet<String> keySet = new java.util.HashSet<String>();
            for (String key : namedObjects) {
                keySet.add(key);
            }

            return keySet;
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            java.util.HashMap<String, Object> hashMap = new java.util.HashMap<String, Object>();
            for (String key : namedObjects) {
                hashMap.put(key, namedObjects.get(key));
            }

            return hashMap.entrySet();
        }

        @Override
        public int size() {
            return namedObjects.getCount();
        }

        @Override
        public Collection<Object> values() {
            java.util.ArrayList<Object> values = new java.util.ArrayList<Object>();
            for (String key : namedObjects) {
                values.add(namedObjects.get(key));
            }

            return values;
        }
    }

    private static class Element  {
        public enum Type {
            DEFINE,
            INSTANCE,
            INCLUDE,
            SCRIPT,
            READ_ONLY_PROPERTY,
            WRITABLE_PROPERTY
        }

        public final Element parent;
        public final Type type;
        public final String id;
        public final String tagName;
        public final int lineNumber;
        public final List<Attribute> attributes;

        public Object value;

        public Element(Element parent, Type type, String id, String tagName, int lineNumber,
            List<Attribute> attributes, Object value) {
            this.parent = parent;
            this.type = type;
            this.id = id;
            this.tagName = tagName;
            this.lineNumber = lineNumber;
            this.attributes = attributes;
            this.value = value;
        }
    }

    private static class Attribute {
        public final String namespaceURI;
        public final String localName;
        public final String value;

        public Attribute(String namespaceURI, String localName, String value) {
            this.namespaceURI = namespaceURI;
            this.localName = localName;
            this.value = value;
        }
    }

    private static class AttributeInvocationHandler implements InvocationHandler {
        private ScriptEngine scriptEngine;
        private String event;
        private String script;

        public AttributeInvocationHandler(ScriptEngine scriptEngine, String event, String script) {
            this.scriptEngine = scriptEngine;
            this.event = event;
            this.script = script;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
            Object result = null;

            String methodName = method.getName();
            if (methodName.equals(event)) {
                try {
                    scriptEngine.eval(script);
                } catch (ScriptException exception) {
                    System.err.println(exception);
                    System.err.println(script);
                }
            }

            // If the function didn't return a value, return the default
            Class<?> returnType = method.getReturnType();
            if (returnType == Vote.class) {
                result = Vote.APPROVE;
            } else if (returnType == Boolean.TYPE) {
                result = false;
            }

            return result;
        }
    }

    private static class ElementInvocationHandler implements InvocationHandler {
        private ScriptEngine scriptEngine;

        public ElementInvocationHandler(ScriptEngine scriptEngine) {
            this.scriptEngine = scriptEngine;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
            Object result = null;

            String methodName = method.getName();
            Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
            if (bindings.containsKey(methodName)) {
                Invocable invocable;
                try {
                    invocable = (Invocable)scriptEngine;
                } catch (ClassCastException exception) {
                    throw new SerializationException(exception);
                }

                result = invocable.invokeFunction(methodName, args);
            }

            // If the function didn't return a value, return the default
            if (result == null) {
                Class<?> returnType = method.getReturnType();
                if (returnType == Vote.class) {
                    result = Vote.APPROVE;
                } else if (returnType == Boolean.TYPE) {
                    result = false;
                }
            }

            return result;
        }
    }

    private Resources resources;
    private WTKXSerializer namespaceOwner;
    private HashMap<String, Object> namedObjects;
    private HashMap<String, WTKXSerializer> namedSerializers;

    private XMLInputFactory xmlInputFactory;
    private ScriptEngineManager scriptEngineManager;

    private URL location = null;
    private Element element = null;
    private Object root = null;

    private String language = DEFAULT_LANGUAGE;

    public static final char URL_PREFIX = '@';
    public static final char RESOURCE_KEY_PREFIX = '%';
    public static final char OBJECT_REFERENCE_PREFIX = '$';

    public static final String LANGUAGE_PROCESSING_INSTRUCTION = "language";

    public static final String WTKX_PREFIX = "wtkx";
    public static final String ID_ATTRIBUTE = "id";

    public static final String INCLUDE_TAG = "include";
    public static final String INCLUDE_SRC_ATTRIBUTE = "src";
    public static final String INCLUDE_RESOURCES_ATTRIBUTE = "resources";

    public static final String SCRIPT_TAG = "script";
    public static final String SCRIPT_SRC_ATTRIBUTE = "src";
    public static final String SCRIPT_LANGUAGE_ATTRIBUTE = "language";

    public static final String DEFINE_TAG = "define";

    public static final String DEFAULT_LANGUAGE = "javascript";

    public static final String MIME_TYPE = "application/wtkx";

    public WTKXSerializer() {
        this(null, null);
    }

    public WTKXSerializer(Resources resources) {
        this(resources, null);
    }

    private WTKXSerializer(Resources resources, WTKXSerializer namespaceOwner) {
        this.resources = resources;
        this.namespaceOwner = namespaceOwner;

        if (namespaceOwner == null) {
            namedObjects = new HashMap<String, Object>();
            namedSerializers = new HashMap<String, WTKXSerializer>();
        } else {
            namedObjects = namespaceOwner.namedObjects;
            namedSerializers = namespaceOwner.namedSerializers;
        }

        xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty("javax.xml.stream.isCoalescing", true);

        scriptEngineManager = new javax.script.ScriptEngineManager();
        scriptEngineManager.setBindings(new NamedObjectBindings());
    }

    public Resources getResources() {
        return resources;
    }

    public Object readObject(String resourceName)
        throws IOException, SerializationException {
        if (resourceName == null) {
            throw new IllegalArgumentException("resourceName is null.");
        }

        ClassLoader classLoader = ThreadUtilities.getClassLoader();
        URL location = classLoader.getResource(resourceName);

        if (location == null) {
            throw new SerializationException("Could not find resource named \""
                + resourceName + "\".");
        }

        return readObject(location);
    }

    public Object readObject(Object baseObject, String resourceName)
        throws IOException, SerializationException {
        if (baseObject == null) {
            throw new IllegalArgumentException("baseObject is null.");
        }

        if (resourceName == null) {
            throw new IllegalArgumentException("resourceName is null.");
        }

        return readObject(baseObject.getClass(), resourceName);
    }

    public Object readObject(Class<?> baseType, String resourceName)
        throws IOException, SerializationException {
        if (baseType == null) {
            throw new IllegalArgumentException("baseType is null.");
        }

        if (resourceName == null) {
            throw new IllegalArgumentException("resourceName is null.");
        }

        return readObject(baseType.getResource(resourceName));
    }

    public Object readObject(URL location)
        throws IOException, SerializationException {
        if (location == null) {
            throw new IllegalArgumentException("location is null.");
        }

        this.location = location;

        InputStream inputStream = new BufferedInputStream(location.openStream());
        try {
            return readObject(inputStream);
        } finally {
            inputStream.close();
        }
    }

    @Override
    public Object readObject(InputStream inputStream)
        throws IOException, SerializationException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is null.");
        }

        // Reset the serializer
        if (namespaceOwner == null) {
            namedObjects.clear();
            namedSerializers.clear();
        }

        root = null;
        language = DEFAULT_LANGUAGE;

        // Parse the XML stream
        element = null;

        try {
            try {
                XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(inputStream);

                while (xmlStreamReader.hasNext()) {
                    int event = xmlStreamReader.next();

                    switch (event) {
                        case XMLStreamConstants.PROCESSING_INSTRUCTION: {
                            processProcessingInstruction(xmlStreamReader);
                            break;
                        }

                        case XMLStreamConstants.CHARACTERS: {
                            processCharacters(xmlStreamReader);
                            break;
                        }

                        case XMLStreamConstants.START_ELEMENT: {
                            processStartElement(xmlStreamReader);
                            break;
                        }

                        case XMLStreamConstants.END_ELEMENT: {
                            processEndElement(xmlStreamReader);
                            break;
                        }
                    }
                }
            } catch (XMLStreamException exception) {
                throw new SerializationException(exception);
            }
        } catch (IOException exception) {
            logException(exception);
            throw exception;
        } catch (SerializationException exception) {
            logException(exception);
            throw exception;
        } catch (RuntimeException exception) {
            logException(exception);
            throw exception;
        }

        if (root instanceof Bindable) {
            bind(root);

            Bindable bindable = (Bindable)root;
            bindable.initialize(resources);
        }

        // Clear the location
        location = null;

        return root;
    }

    private void processProcessingInstruction(XMLStreamReader xmlStreamReader) {
        String piTarget = xmlStreamReader.getPITarget();
        String piData = xmlStreamReader.getPIData();

        if (piTarget.equals(LANGUAGE_PROCESSING_INSTRUCTION)) {
            language = piData;
        }
    }

    @SuppressWarnings("unchecked")
    private void processCharacters(XMLStreamReader xmlStreamReader) throws SerializationException {
        if (!xmlStreamReader.isWhiteSpace()) {
            // Process the text
            String text = xmlStreamReader.getText();

            switch (element.type) {
                case INSTANCE: {
                    if (element.value instanceof Sequence<?>) {
                        Sequence<Object> sequence = (Sequence<Object>)element.value;

                        try {
                            Method addMethod = sequence.getClass().getMethod("add", String.class);
                            addMethod.invoke(sequence, text);
                        } catch (NoSuchMethodException exception) {
                            throw new SerializationException("Text content cannot be added to "
                                + sequence.getClass().getName() + ".", exception);
                        } catch (InvocationTargetException exception) {
                            throw new SerializationException(exception);
                        } catch (IllegalAccessException exception) {
                            throw new SerializationException(exception);
                        }
                    }

                    break;
                }

                case SCRIPT:
                case WRITABLE_PROPERTY: {
                    element.value = text;
                    break;
                }

                default: {
                    throw new SerializationException("Unexpected characters in "
                        + element.type + " element.");
                }
            }
        }
    }

    private void processStartElement(XMLStreamReader xmlStreamReader) throws SerializationException {
        // Get element properties
        String namespaceURI = xmlStreamReader.getNamespaceURI();
        String prefix = xmlStreamReader.getPrefix();
        String localName = xmlStreamReader.getLocalName();

        // Build attribute list; these will be processed in the close tag
        String id = null;
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();

        for (int i = 0, n = xmlStreamReader.getAttributeCount(); i < n; i++) {
            String attributePrefix = xmlStreamReader.getAttributePrefix(i);
            String attributeLocalName = xmlStreamReader.getAttributeLocalName(i);
            String attributeValue = xmlStreamReader.getAttributeValue(i);

            if (attributePrefix != null
                && attributePrefix.equals(WTKX_PREFIX)) {
                if (attributeLocalName.equals(ID_ATTRIBUTE)) {
                    if (attributeValue.length() == 0) {
                        throw new IllegalArgumentException(WTKX_PREFIX + ":" + ID_ATTRIBUTE
                            + " must not be empty.");
                    }

                    id = attributeValue;
                } else {
                    throw new SerializationException(WTKX_PREFIX + ":" + attributeLocalName
                        + " is not a valid attribute.");
                }
            } else {
                String attributeNamespaceURI = xmlStreamReader.getAttributeNamespace(i);
                if (attributeNamespaceURI == null) {
                    attributeNamespaceURI = xmlStreamReader.getNamespaceURI("");
                }

                attributes.add(new Attribute(attributeNamespaceURI, attributeLocalName,
                    attributeValue));
            }
        }

        // Determine the type and value of this element
        Element.Type elementType = null;
        Object value = null;

        if (prefix != null
            && prefix.equals(WTKX_PREFIX)) {
            // The element represents a WTKX operation
            if (element == null) {
                throw new SerializationException(prefix + ":" + localName
                    + " is not a valid root element.");
            }

            if (localName.equals(INCLUDE_TAG)) {
                elementType = Element.Type.INCLUDE;
            } else if (localName.equals(SCRIPT_TAG)) {
                elementType = Element.Type.SCRIPT;
            } else if (localName.equals(DEFINE_TAG)) {
                if (attributes.getLength() > 0) {
                    throw new SerializationException(WTKX_PREFIX + ":" + DEFINE_TAG
                        + " cannot have attributes.");
                }

                elementType = Element.Type.DEFINE;
            } else {
                throw new SerializationException(prefix + ":" + localName
                    + " is not a valid element.");
            }
        } else {
            if (Character.isUpperCase(localName.charAt(0))) {
                // The element represents a typed object
                if (namespaceURI == null) {
                    throw new SerializationException("No XML namespace specified for "
                        + localName + " tag.");
                }

                String className = namespaceURI + "." + localName.replace('.', '$');

                try {
                    Class<?> type = Class.forName(className);
                    elementType = Element.Type.INSTANCE;
                    value = type.newInstance();
                } catch (Exception exception) {
                    throw new SerializationException(exception);
                }
            } else {
                // The element represents a property
                if (element == null
                    || element.type != Element.Type.INSTANCE) {
                    throw new SerializationException("Parent element must be a typed object.");
                }

                if (prefix != null
                    && prefix.length() > 0) {
                    throw new SerializationException("Property elements cannot have a namespace prefix.");
                }

                BeanDictionary beanDictionary = new BeanDictionary(element.value);

                if (beanDictionary.isReadOnly(localName)) {
                    elementType = Element.Type.READ_ONLY_PROPERTY;
                    value = beanDictionary.get(localName);
                    assert (value != null) : "Read-only properties cannot be null.";

                    if (attributes.getLength() > 0
                        && !(value instanceof Dictionary<?, ?>)) {
                        throw new SerializationException("Only read-only dictionaries can have attributes.");
                    }
                } else {
                    if (attributes.getLength() > 0) {
                        throw new SerializationException("Writable property elements cannot have attributes.");
                    }

                    elementType = Element.Type.WRITABLE_PROPERTY;
                }
            }
        }

        // Set the current element
        String tagName = localName;
        if (prefix != null
            && prefix.length() > 0) {
            tagName = prefix + ":" + tagName;
        }

        Location xmlStreamLocation = xmlStreamReader.getLocation();
        element = new Element(element, elementType, id, tagName, xmlStreamLocation.getLineNumber(),
            attributes, value);

        // If this is the root, set it
        if (element.parent == null) {
            root = element.value;
        }
    }

    @SuppressWarnings("unchecked")
    private void processEndElement(XMLStreamReader xmlStreamReader)
        throws SerializationException, IOException {
        String localName = xmlStreamReader.getLocalName();

        switch (element.type) {
            case INSTANCE:
            case INCLUDE: {
                ArrayList<Attribute> instancePropertyAttributes = new ArrayList<Attribute>();
                ArrayList<Attribute> staticPropertyAttributes = new ArrayList<Attribute>();

                if (element.type == Element.Type.INCLUDE) {
                    // Process attributes looking for src, resources, and static property
                    // setters only
                    String src = null;
                    Resources resources = this.resources;

                    for (Attribute attribute : element.attributes) {
                        if (attribute.localName.equals(INCLUDE_SRC_ATTRIBUTE)) {
                            src = attribute.value;
                        } else if (attribute.localName.equals(INCLUDE_RESOURCES_ATTRIBUTE)) {
                            resources = new Resources(resources, attribute.value);
                        } else {
                            if (!Character.isUpperCase(attribute.localName.charAt(0))) {
                                throw new SerializationException("Instance property setters are not"
                                    + " supported for " + WTKX_PREFIX + ":" + INCLUDE_TAG
                                    + " " + " tag.");
                            }

                            staticPropertyAttributes.add(attribute);
                        }
                    }

                    if (src == null) {
                        throw new SerializationException(INCLUDE_SRC_ATTRIBUTE
                            + " attribute is required for " + WTKX_PREFIX + ":" + INCLUDE_TAG
                            + " tag.");
                    }

                    // Read the object
                    WTKXSerializer serializer;
                    if (element.id == null) {
                        serializer = new WTKXSerializer(resources, this);
                    } else {
                        serializer = new WTKXSerializer(resources);
                        namedSerializers.put(element.id, serializer);
                    }

                    if (src.charAt(0) == '/') {
                        element.value = serializer.readObject(src.substring(1));
                    } else {
                        element.value = serializer.readObject(new URL(location, src));
                    }

                    if (element.id == null
                        && !serializer.isEmpty()
                        && serializer.scriptEngineManager == null) {
                        System.err.println("Include \"" + src + "\" defines unreachable objects.");
                    }
                } else {
                    // Process attributes looking for all property setters
                    for (Attribute attribute : element.attributes) {
                        if (Character.isUpperCase(attribute.localName.charAt(0))) {
                            staticPropertyAttributes.add(attribute);
                        } else {
                            instancePropertyAttributes.add(attribute);
                        }
                    }
                }

                // Add the value to the named objects map
                if (element.id != null) {
                    namedObjects.put(element.id, element.value);
                }

                // Apply instance attributes
                Dictionary<String, Object> dictionary;
                if (element.value instanceof Dictionary<?, ?>) {
                    dictionary = (Dictionary<String, Object>)element.value;
                } else {
                    dictionary = new BeanDictionary(element.value);
                }

                for (Attribute attribute : instancePropertyAttributes) {
                    dictionary.put(attribute.localName, resolve(attribute.value));
                }

                // If the element's parent is a sequence or a listener list, add
                // the element value to it
                if (element.parent != null) {
                    if (element.parent.value instanceof Sequence<?>) {
                        Sequence<Object> sequence = (Sequence<Object>)element.parent.value;
                        sequence.add(element.value);
                    } else {
                        if (element.parent.value instanceof ListenerList<?>) {
                            ListenerList<Object> listenerList = (ListenerList<Object>)element.parent.value;
                            listenerList.add(element.value);
                        }
                    }
                }

                // Apply static attributes
                if (element.value instanceof Dictionary<?, ?>) {
                    if (staticPropertyAttributes.getLength() > 0) {
                        throw new SerializationException("Static setters are only supported"
                            + " for typed objects.");
                    }
                } else {
                    for (Attribute attribute : staticPropertyAttributes) {
                        // Determine the type of the attribute
                        String propertyClassName = attribute.namespaceURI + "."
                            + attribute.localName.substring(0, attribute.localName.lastIndexOf("."));

                        Class<?> propertyClass = null;
                        try {
                            propertyClass = Class.forName(propertyClassName);
                        } catch (ClassNotFoundException exception) {
                            throw new SerializationException(exception);
                        }

                        if (propertyClass.isInterface()) {
                            // The attribute represents an event listener
                            String listenerClassName = propertyClassName.substring(propertyClassName.lastIndexOf('.') + 1);
                            String getListenerListMethodName = "get" + Character.toUpperCase(listenerClassName.charAt(0))
                                + listenerClassName.substring(1) + "s";

                            // Get the listener list
                            Method getListenerListMethod;
                            try {
                                Class<?> type = element.value.getClass();
                                getListenerListMethod = type.getMethod(getListenerListMethodName);
                            } catch (NoSuchMethodException exception) {
                                throw new SerializationException(exception);
                            }

                            Object listenerList;
                            try {
                                listenerList = getListenerListMethod.invoke(element.value);
                            } catch (InvocationTargetException exception) {
                                throw new SerializationException(exception);
                            } catch (IllegalAccessException exception) {
                                throw new SerializationException(exception);
                            }

                            // Don't pollute the engine namespace with the listener functions
                            ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(language);
                            scriptEngine.setBindings(new SimpleBindings(), ScriptContext.ENGINE_SCOPE);

                            // Create an invocation handler for this listener
                            AttributeInvocationHandler handler =
                                new AttributeInvocationHandler(scriptEngine,
                                    attribute.localName.substring(attribute.localName.lastIndexOf(".") + 1),
                                    attribute.value);

                            Object listener = Proxy.newProxyInstance(ThreadUtilities.getClassLoader(),
                                new Class<?>[]{propertyClass}, handler);

                            // Add the listener
                            Class<?> listenerListClass = listenerList.getClass();
                            Method addMethod;
                            try {
                                addMethod = listenerListClass.getMethod("add", Object.class);
                            } catch (NoSuchMethodException exception) {
                                throw new RuntimeException(exception);
                            }

                            try {
                                addMethod.invoke(listenerList, listener);
                            } catch (IllegalAccessException exception) {
                                throw new SerializationException(exception);
                            } catch (InvocationTargetException exception) {
                                throw new SerializationException(exception);
                            }
                        } else {
                            // The attribute reprsents a static setter
                            Object value = resolve(attribute.value);

                            Class<?> objectType = element.value.getClass();

                            String propertyName = attribute.localName.substring(attribute.localName.lastIndexOf(".") + 1);
                            propertyName = Character.toUpperCase(propertyName.charAt(0)) +
                            propertyName.substring(1);

                            Method setterMethod = null;
                            if (value != null) {
                                setterMethod = getStaticSetterMethod(propertyClass, propertyName,
                                    objectType, value.getClass());
                            }

                            if (setterMethod == null) {
                                Method getterMethod = getStaticGetterMethod(propertyClass, propertyName, objectType);

                                if (getterMethod != null) {
                                    Class<?> propertyType = getterMethod.getReturnType();
                                    setterMethod = getStaticSetterMethod(propertyClass, propertyName,
                                        objectType, propertyType);

                                    if (value instanceof String) {
                                        value = BeanDictionary.coerce((String)value, propertyType);
                                    }
                                }
                            }

                            if (setterMethod == null) {
                                throw new SerializationException(attribute.localName + " is not valid static property.");
                            }

                            // Invoke the setter
                            try {
                                setterMethod.invoke(null, element.value, value);
                            } catch (Exception exception) {
                                throw new SerializationException(exception);
                            }
                        }
                    }
                }

                // If the parent element is a writable property, set this as its
                // value; it will be applied later in the parent's closing tag
                if (element.parent != null
                    && element.parent.type == Element.Type.WRITABLE_PROPERTY) {
                    element.parent.value = element.value;
                }

                break;
            }

            case READ_ONLY_PROPERTY: {
                if (element.value instanceof Dictionary<?, ?>) {
                    // Process attributes looking for instance property setters
                    for (Attribute attribute : element.attributes) {
                        if (Character.isUpperCase(attribute.localName.charAt(0))) {
                            throw new SerializationException("Static setters are not supported"
                                + " for read-only properties.");
                        }

                        Dictionary<String, Object> dictionary =
                            (Dictionary<String, Object>)element.value;
                        dictionary.put(attribute.localName, resolve(attribute.value));
                    }
                }

                break;
            }

            case WRITABLE_PROPERTY: {
                BeanDictionary beanDictionary = new BeanDictionary(element.parent.value);
                beanDictionary.put(localName, element.value);
                break;
            }

            case SCRIPT: {
                // Process attributes looking for src and language
                String src = null;
                String language = this.language;
                for (Attribute attribute : element.attributes) {
                    if (attribute.localName.equals(SCRIPT_SRC_ATTRIBUTE)) {
                        src = attribute.value;
                    } else if (attribute.localName.equals(SCRIPT_LANGUAGE_ATTRIBUTE)) {
                        language = attribute.value;
                    } else {
                        throw new SerializationException(attribute.localName + " is not a valid"
                            + " attribute for the " + WTKX_PREFIX + ":" + SCRIPT_TAG + " tag.");
                    }
                }

                Bindings bindings;
                if (element.parent.value instanceof ListenerList<?>) {
                    // Don't pollute the engine namespace with the listener functions
                    bindings = new SimpleBindings();
                } else {
                    bindings = scriptEngineManager.getBindings();
                }

                // Execute script
                final ScriptEngine scriptEngine;

                if (src != null) {
                    // The script is located in an external file
                    int i = src.lastIndexOf(".");
                    if (i == -1) {
                        throw new SerializationException("Cannot determine type of script \""
                            + src + "\".");
                    }

                    String extension = src.substring(i + 1);
                    scriptEngine = scriptEngineManager.getEngineByExtension(extension);

                    if (scriptEngine == null) {
                        throw new SerializationException("Unable to find scripting engine for"
                            + " extension " + extension + ".");
                    }

                    scriptEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

                    try {
                        URL scriptLocation;
                        if (src.charAt(0) == '/') {
                            ClassLoader classLoader = ThreadUtilities.getClassLoader();
                            scriptLocation = classLoader.getResource(src);
                        } else {
                            scriptLocation = new URL(location, src);
                        }

                        BufferedReader scriptReader = null;
                        try {
                            scriptReader = new BufferedReader(new InputStreamReader(scriptLocation.openStream()));
                            scriptEngine.eval(scriptReader);
                        } catch(ScriptException exception) {
                            exception.printStackTrace();
                        } finally {
                            if (scriptReader != null) {
                                scriptReader.close();
                            }
                        }
                    } catch (IOException exception) {
                        throw new SerializationException(exception);
                    }
                } else {
                    // The script is inline
                    scriptEngine = scriptEngineManager.getEngineByName(language);

                    if (scriptEngine == null) {
                        throw new SerializationException("Unable to find scripting engine for"
                            + " language \"" + language + "\".");
                    }

                    scriptEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

                    String script = (String)element.value;
                    if (script != null) {
                        try {
                            scriptEngine.eval(script);
                        } catch (ScriptException exception) {
                            System.err.println(exception);
                            System.err.println(script);
                        }
                    }
                }

                if (element.parent.value instanceof ListenerList<?>) {
                    // Create the listener and add it to the list
                    Class<?> listenerListClass = element.parent.value.getClass();

                    java.lang.reflect.Type[] genericInterfaces = listenerListClass.getGenericInterfaces();
                    Class<?> listenerClass = (Class<?>)genericInterfaces[0];

                    ElementInvocationHandler handler = new ElementInvocationHandler(scriptEngine);

                    Method addMethod;
                    try {
                        addMethod = listenerListClass.getMethod("add", Object.class);
                    } catch (NoSuchMethodException exception) {
                        throw new RuntimeException(exception);
                    }

                    Object listener = Proxy.newProxyInstance(ThreadUtilities.getClassLoader(),
                        new Class<?>[]{listenerClass}, handler);

                    try {
                        addMethod.invoke(element.parent.value, listener);
                    } catch (IllegalAccessException exception) {
                        throw new SerializationException(exception);
                    } catch (InvocationTargetException exception) {
                        throw new SerializationException(exception);
                    }
                }

                break;
            }

            case DEFINE: {
                // No-op
            }
        }

        // Move up the stack
        if (element.parent != null) {
            element = element.parent;
        }
    }

    private void logException(Exception exception) {
        String message = "An error occurred while processing ";

        if (element == null) {
            message += " the root element";
        } else {
            message += " element <" + element.tagName + ">"
                + " starting at line number " + element.lineNumber;
        }

        if (location != null) {
            message += " in file " + location.getPath();
        }

        message += ":";

        System.err.println(message);
        exception.printStackTrace();
    }

    @Override
    public void writeObject(Object object, OutputStream outputStream) throws IOException,
        SerializationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMIMEType(Object object) {
        return MIME_TYPE;
    }

    /**
     * Retrieves a named object.
     *
     * @param name
     * The name of the object, relative to this loader. The object's name is
     * the concatenation of its parent IDs and its ID, separated by periods
     * (e.g. "foo.bar.baz").
     *
     * @return The named object, or <tt>null</tt> if an object with the given
     * name does not exist. Use {@link #containsKey(String)} to distinguish
     * between the two cases.
     */
    @Override
    public Object get(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null.");
        }

        Object value = null;

        int i = name.lastIndexOf('.');
        if (i == -1) {
            value = namedObjects.get(name);
        } else {
            String serializerName = name.substring(0, name.lastIndexOf('.'));
            String id = name.substring(serializerName.length() + 1);
            WTKXSerializer serializer = getSerializer(serializerName);

            if (serializer != null) {
                value = serializer.get(id);
            }
        }

        return value;
    }

    /**
     * Provides typed access to named objects. Delegates to {@link #get(String)} and casts
     * the return value to <tt>T</tt>.
     *
     * @param name
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(String name) {
        return (T)get(name);
    }

    @Override
    public Object put(String name, Object value) {
        if (name == null) {
            throw new IllegalArgumentException("name is null.");
        }

        Object previousValue;

        int i = name.lastIndexOf('.');
        if (i == -1) {
            previousValue = namedObjects.put(name, value);
        } else {
            String serializerName = name.substring(0, name.lastIndexOf('.'));
            String id = name.substring(serializerName.length() + 1);
            WTKXSerializer serializer = getSerializer(serializerName);
            previousValue = serializer.put(id, value);
        }

        return previousValue;
    }

    @Override
    public Object remove(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null.");
        }

        Object previousValue;

        int i = name.lastIndexOf('.');
        if (i == -1) {
            previousValue = namedObjects.remove(name);
        } else {
            String serializerName = name.substring(0, name.lastIndexOf('.'));
            String id = name.substring(serializerName.length() + 1);
            WTKXSerializer serializer = getSerializer(serializerName);
            previousValue = serializer.remove(id);
        }

        return previousValue;
    }

    @Override
    public boolean containsKey(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null.");
        }

        boolean containsKey = false;

        int i = name.lastIndexOf('.');
        if (i == -1) {
            containsKey = namedObjects.containsKey(name);
        } else {
            String serializerName = name.substring(0, name.lastIndexOf('.'));
            String id = name.substring(serializerName.length() + 1);
            WTKXSerializer serializer = getSerializer(serializerName);

            if (serializer != null) {
                containsKey = serializer.containsKey(id);
            }
        }

        return containsKey;
    }

    public boolean isEmpty() {
        return namedObjects.isEmpty()
            && namedSerializers.isEmpty();
    }

    /**
     * Retrieves the root of the object hierarchy most recently processed by
     * this serializer.
     *
     * @return
     * The root object, or <tt>null</tt> if this serializer has not yet read an
     * object from an input stream.
     */
    public Object getRoot() {
        return root;
    }

    /**
     * Retrieves a nested serializer.
     *
     * @param name
     * The name of the serializer, relative to this loader. The serializer's name
     * is the concatentation of its parent IDs and its ID, separated by periods
     * (e.g. "foo.bar.baz").
     *
     * @return The named serializer, or <tt>null</tt> if a serializer with the
     * given name does not exist.
     */
    public WTKXSerializer getSerializer(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null.");
        }

        WTKXSerializer serializer = this;
        String[] path = name.split("\\.");

        int i = 0;
        int n = path.length;
        while (i < n && serializer != null) {
            String id = path[i++];
            serializer = serializer.namedSerializers.get(id);
        }

        return serializer;
    }

    /**
     * Returns the location of the WTKX most recently processed by this
     * serializer.
     *
     * @return
     * The location of the WTKX, or <tt>null</tt> if this serializer has not
     * yet read an object from a URL.
     */
    public URL getLocation() {
        return location;
    }

    /**
     * Applies WTKX binding annotations to an object.
     *
     * @param object
     *
     * @see #bind(Object, Class)
     */
    public void bind(Object object) {
        if (object == null) {
            throw new IllegalArgumentException();
        }

        bind(object, object.getClass());
    }

    /**
     * Applies WTKX binding annotations to an object.
     * <p>
     * NOTE This method uses reflection to set internal member variables. As
     * a result, it may only be called from trusted code.
     *
     * @param object
     * @param type
     *
     * @throws BindException
     * If an error occurs during binding
     */
    public void bind(Object object, Class<?> type) throws BindException {
        if (object == null) {
            throw new IllegalArgumentException();
        }

        if (!type.isAssignableFrom(object.getClass())) {
            throw new IllegalArgumentException();
        }

        Field[] fields = type.getDeclaredFields();

        // Process bind annotations
        for (int j = 0, n = fields.length; j < n; j++) {
            Field field = fields[j];
            String fieldName = field.getName();
            int fieldModifiers = field.getModifiers();

            WTKX wtkxAnnotation = field.getAnnotation(WTKX.class);
            if (wtkxAnnotation != null) {
                // Ensure that we can write to the field
                if ((fieldModifiers & Modifier.FINAL) > 0) {
                    throw new BindException(fieldName + " is final.");
                }

                if ((fieldModifiers & Modifier.PUBLIC) == 0) {
                    try {
                        field.setAccessible(true);
                    } catch (SecurityException exception) {
                        throw new BindException(fieldName + " is not accessible.");
                    }
                }

                String id = wtkxAnnotation.id();
                if (id.equals("\0")) {
                    id = field.getName();
                }

                if (containsKey(id)) {
                    // Set the value into the field
                    Object value = get(id);
                    try {
                        field.set(object, value);
                    } catch (IllegalAccessException exception) {
                        throw new BindException(exception);
                    }
                }
            }
        }
    }

    /**
     * Resolves an attribute value as either a URL, resource value, or
     * object reference, depending on the value's prefix. If the value can't
     * or doesn't need to be resolved, the original attribute value is
     * returned.
     *
     * @param attributeValue
     * The attribute value to resolve.
     *
     * @return
     * The resolved value.
     */
    private Object resolve(String attributeValue)
        throws MalformedURLException {
        Object resolvedValue = null;

        if (attributeValue.length() > 0) {
            if (attributeValue.charAt(0) == URL_PREFIX) {
                if (attributeValue.length() > 1) {
                    if (attributeValue.charAt(1) == URL_PREFIX) {
                        resolvedValue = attributeValue.substring(1);
                    } else {
                        if (location == null) {
                            throw new IllegalStateException("Base location is undefined.");
                        }

                        resolvedValue = new URL(location, attributeValue.substring(1));
                    }
                }
            } else if (attributeValue.charAt(0) == RESOURCE_KEY_PREFIX) {
                if (attributeValue.length() > 1) {
                    if (attributeValue.charAt(1) == RESOURCE_KEY_PREFIX) {
                        resolvedValue = attributeValue.substring(1);
                    } else {
                        if (resources == null) {
                            throw new IllegalStateException("Resources is null.");
                        }

                        resolvedValue = resources.get(attributeValue.substring(1));

                        if (resolvedValue == null) {
                            resolvedValue = attributeValue;
                        }
                    }
                }
            } else if (attributeValue.charAt(0) == OBJECT_REFERENCE_PREFIX) {
                if (attributeValue.length() > 1) {
                    if (attributeValue.charAt(1) == OBJECT_REFERENCE_PREFIX) {
                        resolvedValue = attributeValue.substring(1);
                    } else {
                        resolvedValue = get(attributeValue.substring(1));

                        if (resolvedValue == null) {
                            resolvedValue = attributeValue;
                        }
                    }
                }
            } else {
                resolvedValue = attributeValue;
            }
        } else {
            resolvedValue = attributeValue;
        }

        return resolvedValue;
    }

    private Method getStaticGetterMethod(Class<?> propertyClass, String propertyName,
        Class<?> objectType) {
        Method method = null;

        if (objectType != null) {
            try {
                method = propertyClass.getMethod(BeanDictionary.GET_PREFIX
                    + propertyName, objectType);
            } catch (NoSuchMethodException exception) {
                // No-op
            }

            if (method == null) {
                try {
                    method = propertyClass.getMethod(BeanDictionary.IS_PREFIX
                        + propertyName, objectType);
                } catch (NoSuchMethodException exception) {
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
                method = propertyClass.getMethod(methodName, objectType, propertyValueType);
            } catch (NoSuchMethodException exception) {
                // No-op
            }

            if (method == null) {
                // If value type is a primitive wrapper, look for a method
                // signature with the corresponding primitive type
                try {
                    Field primitiveTypeField = propertyValueType.getField("TYPE");
                    Class<?> primitivePropertyValueType = (Class<?>)primitiveTypeField.get(null);

                    try {
                        method = propertyClass.getMethod(methodName,
                            objectType, primitivePropertyValueType);
                    } catch (NoSuchMethodException exception) {
                        // No-op
                    }
                } catch (NoSuchFieldException exception) {
                    // No-op; not a wrapper type
                } catch (IllegalAccessException exception) {
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
