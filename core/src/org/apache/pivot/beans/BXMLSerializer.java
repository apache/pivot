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
package org.apache.pivot.beans;

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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.LinkedList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.json.JSON;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.BinarySerializer;
import org.apache.pivot.serialization.ByteArraySerializer;
import org.apache.pivot.serialization.CSVSerializer;
import org.apache.pivot.serialization.PropertiesSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.serialization.Serializer;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.Vote;

/**
 * Loads an object hierarchy from an XML document.
 */
public class BXMLSerializer implements Serializer<Object>, Resolvable {
    private static class Element  {
        public enum Type {
            INSTANCE,
            READ_ONLY_PROPERTY,
            WRITABLE_PROPERTY,
            LISTENER_LIST_PROPERTY,
            INCLUDE,
            SCRIPT,
            DEFINE,
            REFERENCE
        }

        public final Element parent;
        public final Type type;
        public final Class<?> propertyClass;
        public final String name;
        public Object value;

        public String id = null;
        public final HashMap<String, String> properties = new HashMap<String, String>();
        public final LinkedList<Attribute> attributes = new LinkedList<Attribute>();

        public Element(Element parent, Type type, String name, Class<?> propertyClass, Object value) {
            this.parent = parent;
            this.type = type;
            this.name = name;
            this.propertyClass = propertyClass;
            this.value = value;
        }
    }

    private static class Attribute {
        public final Element element;
        public final String name;
        public final Class<?> propertyClass;
        public Object value;

        public Attribute(Element element, String name, Class<?> propertyClass, Object value) {
            this.element = element;
            this.name = name;
            this.propertyClass = propertyClass;
            this.value = value;
        }
    }

    private static class AttributeInvocationHandler implements InvocationHandler {
        private ScriptEngine scriptEngine;
        private String event;
        private String script;

        private static final String ARGUMENTS_KEY = "arguments";

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
                    SimpleBindings bindings = new SimpleBindings();
                    bindings.put(ARGUMENTS_KEY, args);
                    scriptEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
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

    private static class ScriptBindMapping implements NamespaceBinding.BindMapping {
        private ScriptEngine scriptEngine;
        private String functionName;

        public ScriptBindMapping(ScriptEngine scriptEngine, String functionName) {
            this.scriptEngine = scriptEngine;
            this.functionName = functionName;
        }

        @Override
        public Object evaluate(final Object value) {
            Object result = value;
            Bindings bindings = scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE);
            if (bindings.containsKey(functionName)) {
                Invocable invocable;
                try {
                    invocable = (Invocable)scriptEngine;
                } catch (ClassCastException exception) {
                    throw new RuntimeException(exception);
                }

                try {
                    result = invocable.invokeFunction(functionName, result);
                } catch (NoSuchMethodException exception) {
                    throw new RuntimeException(exception);
                } catch (ScriptException exception) {
                    throw new RuntimeException(exception);
                }
            } else {
                throw new RuntimeException("Mapping function \"" + functionName + "\" is not defined.");
            }

            return result;
        }
    }

    private XMLInputFactory xmlInputFactory;
    private ScriptEngineManager scriptEngineManager;

    private Map<String, Object> namespace = new HashMap<String, Object>();
    private URL location = null;
    private Resources resources = null;

    private XMLStreamReader xmlStreamReader = null;
    private Element element = null;

    private Object root = null;
    private String language = null;
    private int nextID = 0;

    private LinkedList<Attribute> namespaceBindingAttributes = new LinkedList<Attribute>();

    private static HashMap<String, String> fileExtensions = new HashMap<String, String>();
    private static HashMap<String, Class<? extends Serializer<?>>> mimeTypes =
        new HashMap<String, Class<? extends Serializer<?>>>();

    public static final char URL_PREFIX = '@';
    public static final char RESOURCE_KEY_PREFIX = '%';
    public static final char OBJECT_REFERENCE_PREFIX = '$';

    public static final String NAMESPACE_BINDING_PREFIX = OBJECT_REFERENCE_PREFIX + "{";
    public static final String NAMESPACE_BINDING_SUFFIX = "}";
    public static final String BIND_MAPPING_DELIMITER = ":";
    public static final String INTERNAL_ID_PREFIX = "$";

    public static final String LANGUAGE_PROCESSING_INSTRUCTION = "language";

    public static final String BXML_PREFIX = "bxml";
    public static final String BXML_EXTENSION = "bxml";
    public static final String ID_ATTRIBUTE = "id";

    public static final String INCLUDE_TAG = "include";
    public static final String INCLUDE_SRC_ATTRIBUTE = "src";
    public static final String INCLUDE_RESOURCES_ATTRIBUTE = "resources";
    public static final String INCLUDE_MIME_TYPE_ATTRIBUTE = "mimeType";
    public static final String INCLUDE_INLINE_ATTRIBUTE = "inline";

    public static final String SCRIPT_TAG = "script";
    public static final String SCRIPT_SRC_ATTRIBUTE = "src";

    public static final String DEFINE_TAG = "define";

    public static final String REFERENCE_TAG = "reference";
    public static final String REFERENCE_ID_ATTRIBUTE = "id";

    public static final String DEFAULT_LANGUAGE = "javascript";

    public static final String MIME_TYPE = "application/bxml";

    static {
        mimeTypes.put(MIME_TYPE, BXMLSerializer.class);

        mimeTypes.put(BinarySerializer.MIME_TYPE, BinarySerializer.class);
        mimeTypes.put(ByteArraySerializer.MIME_TYPE, ByteArraySerializer.class);
        mimeTypes.put(CSVSerializer.MIME_TYPE, CSVSerializer.class);
        mimeTypes.put(JSONSerializer.MIME_TYPE, JSONSerializer.class);
        mimeTypes.put(PropertiesSerializer.MIME_TYPE, PropertiesSerializer.class);

        fileExtensions.put(BXML_EXTENSION, MIME_TYPE);

        fileExtensions.put(CSVSerializer.CSV_EXTENSION, CSVSerializer.MIME_TYPE);
        fileExtensions.put(JSONSerializer.JSON_EXTENSION, JSONSerializer.MIME_TYPE);
        fileExtensions.put(PropertiesSerializer.PROPERTIES_EXTENSION, PropertiesSerializer.MIME_TYPE);
    }

    public BXMLSerializer() {
        xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty("javax.xml.stream.isCoalescing", true);

        scriptEngineManager = new javax.script.ScriptEngineManager();
        scriptEngineManager.setBindings(new Bindings() {
            @Override
            public Object get(Object key) {
                return namespace.get(key.toString());
            }

            @Override
            public Object put(String key, Object value) {
                return namespace.put(key, value);
            }

            @Override
            public void putAll(java.util.Map<? extends String, ? extends Object> map) {
                for (String key : map.keySet()) {
                    put(key, map.get(key));
                }
            }

            @Override
            public Object remove(Object key) {
                return namespace.remove(key.toString());
            }

            @Override
            public void clear() {
                namespace.clear();
            }

            @Override
            public boolean containsKey(Object key) {
                return namespace.containsKey(key.toString());
            }

            @Override
            public boolean containsValue(Object value) {
                boolean contains = false;
                for (String key : namespace) {
                    if (namespace.get(key).equals(value)) {
                        contains = true;
                        break;
                    }
                }

                return contains;
            }

            @Override
            public boolean isEmpty() {
                return namespace.isEmpty();
            }

            @Override
            public java.util.Set<String> keySet() {
                java.util.HashSet<String> keySet = new java.util.HashSet<String>();
                for (String key : namespace) {
                    keySet.add(key);
                }

                return keySet;
            }

            @Override
            public java.util.Set<Entry<String, Object>> entrySet() {
                java.util.HashMap<String, Object> hashMap = new java.util.HashMap<String, Object>();
                for (String key : namespace) {
                    hashMap.put(key, namespace.get(key));
                }

                return hashMap.entrySet();
            }

            @Override
            public int size() {
                return namespace.getCount();
            }

            @Override
            public Collection<Object> values() {
                java.util.ArrayList<Object> values = new java.util.ArrayList<Object>();
                for (String key : namespace) {
                    values.add(namespace.get(key));
                }

                return values;
            }
        });
    }

    /** DO NOT USE. see https://issues.apache.org/jira/browse/PIVOT-742 */
    @Deprecated
    public BXMLSerializer(@SuppressWarnings("unused") final ClassLoader classLoader) {
        throw new UnsupportedOperationException("https://issues.apache.org/jira/browse/PIVOT-742");
    }

    /**
     * Deserializes an object hierarchy from a BXML resource.
     * <p>
     * This is the base version of the method. It does not set the "location" or "resources"
     * properties. Callers that wish to use this version of the method to load BXML that uses
     * location or resource resolution must manually set these properties via a call to
     * {@link #setLocation(URL)} or {@link #setResources(Resources)}, respectively, before calling
     * this method.
     *
     * @return
     * The deserialized object hierarchy.
     */
    @Override
    public Object readObject(InputStream inputStream)
        throws IOException, SerializationException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is null.");
        }

        root = null;
        language = null;

        // Parse the XML stream
        try {
            try {
                xmlStreamReader = xmlInputFactory.createXMLStreamReader(inputStream);

                while (xmlStreamReader.hasNext()) {
                    int event = xmlStreamReader.next();

                    switch (event) {
                        case XMLStreamConstants.PROCESSING_INSTRUCTION: {
                            processProcessingInstruction();
                            break;
                        }

                        case XMLStreamConstants.CHARACTERS: {
                            processCharacters();
                            break;
                        }

                        case XMLStreamConstants.START_ELEMENT: {
                            processStartElement();
                            break;
                        }

                        case XMLStreamConstants.END_ELEMENT: {
                            processEndElement();
                            break;
                        }

                        default: {
                            break;
                        }
                    }
                }
            } catch (XMLStreamException exception) {
                throw new SerializationException(exception);
            }
        } catch (IOException exception) {
            logException();
            throw exception;
        } catch (SerializationException exception) {
            logException();
            throw exception;
        } catch (RuntimeException exception) {
            logException();
            throw exception;
        }

        xmlStreamReader = null;

        // Apply the namespace bindings
        for (Attribute attribute : namespaceBindingAttributes) {
            Element elementLocal = attribute.element;
            String sourcePath = (String)attribute.value;

            NamespaceBinding.BindMapping bindMapping;
            int i = sourcePath.indexOf(BIND_MAPPING_DELIMITER);
            if (i == -1) {
                bindMapping = null;
            } else {
                String bindFunction = sourcePath.substring(0, i);
                sourcePath = sourcePath.substring(i + 1);
                bindMapping = new ScriptBindMapping(scriptEngineManager.getEngineByName(language), bindFunction);
            }

            switch (elementLocal.type) {
                case INSTANCE:
                case INCLUDE: {
                    // Bind to <element ID>.<attribute name>
                    if (elementLocal.id == null) {
                        elementLocal.id = INTERNAL_ID_PREFIX + Integer.toString(nextID++);
                        namespace.put(elementLocal.id, elementLocal.value);
                    }

                    String targetPath = elementLocal.id + "." + attribute.name;
                    NamespaceBinding namespaceBinding = new NamespaceBinding(namespace, sourcePath, targetPath,
                        bindMapping);
                    namespaceBinding.bind();

                    break;
                }

                case READ_ONLY_PROPERTY: {
                    // Bind to <parent element ID>.<element name>.<attribute name>
                    if (elementLocal.parent.id == null) {
                        elementLocal.parent.id = INTERNAL_ID_PREFIX + Integer.toString(nextID++);
                        namespace.put(elementLocal.parent.id, elementLocal.parent.value);
                    }

                    String targetPath = elementLocal.parent.id + "." + elementLocal.name + "." + attribute.name;
                    NamespaceBinding namespaceBinding = new NamespaceBinding(namespace, sourcePath, targetPath,
                        bindMapping);
                    namespaceBinding.bind();

                    break;
                }

                default: {
                    break;
                }
            }
        }

        namespaceBindingAttributes.clear();

        // Bind the root to the namespace
        if (root instanceof Bindable) {
            Class<?> type = root.getClass();
            while (Bindable.class.isAssignableFrom(type)) {
                bind(root, type);
                type = type.getSuperclass();
            }

            Bindable bindable = (Bindable)root;
            bindable.initialize(namespace, location, resources);
        }

        return root;
    }

    /**
     * Deserializes an object hierarchy from a BXML resource.
     *
     * @see #readObject(Class, String, boolean)
     */
    public final Object readObject(Class<?> baseType, String resourceName)
        throws IOException, SerializationException {
        return readObject(baseType, resourceName, false);
    }

    /**
     * Deserializes an object hierarchy from a BXML resource.
     * <p>
     * The location of the resource is determined by a call to
     * {@link Class#getResource(String)} on the given base type, passing the given
     * resource name as an argument. If the resources is localized, the base type
     * is also used as the base name of the resource bundle.
     *
     * @param baseType
     * The base type.
     *
     * @param resourceName
     * The name of the BXML resource.
     *
     * @param localize
     * If <tt>true</tt>, the deserialized resource will be localized using the resource
     * bundle specified by the base type. Otherwise, it will not be localized, and any
     * use of the resource resolution operator will result in a serialization exception.
     *
     * @see #readObject(URL, Resources)
     */
    public final Object readObject(Class<?> baseType, String resourceName, boolean localize)
        throws IOException, SerializationException {
        if (baseType == null) {
            throw new IllegalArgumentException("baseType is null.");
        }

        if (resourceName == null) {
            throw new IllegalArgumentException("resourceName is null.");
        }

        // throw a nice error so the user knows which resource did not load
        URL locationLocal = baseType.getResource(resourceName);
        if (locationLocal == null) {
            throw new IllegalArgumentException("could not find resource \"" + resourceName + "\".");
        }
        return readObject(locationLocal, localize ? new Resources(baseType.getName()) : null);
    }

    /**
     * Deserializes an object hierarchy from a BXML resource.
     * <p>
     * This version of the method does not set the "resources" property. Callers
     * that wish to use this version of the method to load BXML that uses resource
     * resolution must manually set this property via a call to
     * {@link #setResources(Resources)} before calling this method.
     *
     * @param locationArgument
     * The location of the BXML resource.
     *
     * @see #readObject(URL, Resources)
     */
    public final Object readObject(URL locationArgument)
        throws IOException, SerializationException {
        return readObject(locationArgument, null);
    }

    /**
     * Deserializes an object hierarchy from a BXML resource.
     *
     * @param locationArgument
     * The location of the BXML resource.
     *
     * @param resourcesArgument
     * The resources that will be used to localize the deserialized resource.
     *
     * #see readObject(InputStream)
     */
    public final Object readObject(URL locationArgument, Resources resourcesArgument)
        throws IOException, SerializationException {
        if (locationArgument == null) {
            throw new IllegalArgumentException("location is null.");
        }

        this.location = locationArgument;
        this.resources = resourcesArgument;

        InputStream inputStream = new BufferedInputStream(locationArgument.openStream());

        Object object;
        try {
            object = readObject(inputStream);
        } finally {
            inputStream.close();
        }

        this.location = null;
        this.resources = null;

        return object;
    }

    private void processProcessingInstruction() throws SerializationException {
        String piTarget = xmlStreamReader.getPITarget();
        String piData = xmlStreamReader.getPIData();

        if (piTarget.equals(LANGUAGE_PROCESSING_INSTRUCTION)) {
            if (language != null) {
                throw new SerializationException("language already set.");
            }

            language = piData;
        }
    }

    @SuppressWarnings("unchecked")
    private void processCharacters() throws SerializationException {
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
                                + sequence.getClass().getName() + ": \"" + text + "\"", exception);
                        } catch (InvocationTargetException exception) {
                            throw new SerializationException(exception);
                        } catch (IllegalAccessException exception) {
                            throw new SerializationException(exception);
                        }
                    }

                    break;
                }

                case WRITABLE_PROPERTY:
                case LISTENER_LIST_PROPERTY:
                case SCRIPT: {
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

    private void processStartElement() throws IOException, SerializationException {

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // Initialize the page language
        if (language == null) {
            language = DEFAULT_LANGUAGE;
        }

        // Get element properties
        String namespaceURI = xmlStreamReader.getNamespaceURI();
        String prefix = xmlStreamReader.getPrefix();

        // Some stream readers incorrectly report an empty string as the prefix
        // for the default namespace
        if (prefix != null
            && prefix.length() == 0) {
            prefix = null;
        }

        String localName = xmlStreamReader.getLocalName();

        // Determine the type and value of this element
        Element.Type elementType;
        String name;
        Class<?> propertyClass = null;
        Object value = null;

        if (prefix != null
            && prefix.equals(BXML_PREFIX)) {
            // The element represents a BXML operation
            if (element == null) {
                throw new SerializationException("Invalid root element.");
            }

            if (localName.equals(INCLUDE_TAG)) {
                elementType = Element.Type.INCLUDE;
            } else if (localName.equals(SCRIPT_TAG)) {
                elementType = Element.Type.SCRIPT;
            } else if (localName.equals(DEFINE_TAG)) {
                elementType = Element.Type.DEFINE;
            } else if (localName.equals(REFERENCE_TAG)) {
                elementType = Element.Type.REFERENCE;
            } else {
                throw new SerializationException("Invalid element.");
            }

            name = "<" + prefix + ":" + localName + ">";
        } else {
            if (Character.isUpperCase(localName.charAt(0))) {
                int i = localName.indexOf('.');
                if (i != -1
                    && Character.isLowerCase(localName.charAt(i + 1))) {
                    // The element represents an attached property
                    elementType = Element.Type.WRITABLE_PROPERTY;
                    name = localName.substring(i + 1);

                    String propertyClassName = namespaceURI + "." + localName.substring(0, i);
                    try {
                        propertyClass = Class.forName(propertyClassName, true, classLoader);
                    } catch (Throwable exception) {
                        throw new SerializationException(exception);
                    }
                } else {
                    // The element represents a typed object
                    if (namespaceURI == null) {
                        throw new SerializationException("No XML namespace specified for "
                            + localName + " tag.");
                    }

                    elementType = Element.Type.INSTANCE;
                    name = "<" + ((prefix == null) ? "" : prefix + ":") + localName + ">";

                    String className = namespaceURI + "." + localName.replace('.', '$');

                    try {
                        Class<?> type = Class.forName(className, true, classLoader);
                        value = newTypedObject(type);
                    } catch (Throwable exception) {
                        throw new SerializationException(exception);
                    }
                }
            } else {
                // The element represents a property
                if (prefix != null) {
                    throw new SerializationException("Property elements cannot have a namespace prefix.");
                }

                if (element.value instanceof Dictionary<?, ?>) {
                    elementType = Element.Type.WRITABLE_PROPERTY;
                } else {
                    BeanAdapter beanAdapter = new BeanAdapter(element.value);

                    if (beanAdapter.isReadOnly(localName)) {
                        Class<?> propertyType = beanAdapter.getType(localName);
                        if (propertyType == null) {
                            throw new SerializationException("\"" + localName
                                + "\" is not a valid property of element "
                                + element.name + ".");
                        }

                        if (ListenerList.class.isAssignableFrom(propertyType)) {
                            elementType = Element.Type.LISTENER_LIST_PROPERTY;
                        } else {
                            elementType = Element.Type.READ_ONLY_PROPERTY;
                            value = beanAdapter.get(localName);
                            assert (value != null) : "Read-only properties cannot be null.";
                        }
                    } else {
                        elementType = Element.Type.WRITABLE_PROPERTY;
                    }
                }

                name = localName;
            }
        }

        // Create the element and process the attributes
        element = new Element(element, elementType, name, propertyClass, value);
        processAttributes();

        if (elementType == Element.Type.INCLUDE) {
            // Load the include
            if (!element.properties.containsKey(INCLUDE_SRC_ATTRIBUTE)) {
                throw new SerializationException(INCLUDE_SRC_ATTRIBUTE
                    + " attribute is required for " + BXML_PREFIX + ":" + INCLUDE_TAG
                    + " tag.");
            }

            String src = element.properties.get(INCLUDE_SRC_ATTRIBUTE);
            if (src.charAt(0) == OBJECT_REFERENCE_PREFIX) {
                src = src.substring(1);
                if (src.length() > 0) {
                    if (!JSON.containsKey(namespace, src)) {
                        throw new SerializationException("Value \"" + src + "\" is not defined.");
                    }
                    String variableValue = JSON.get(namespace, src);
                    src = variableValue;
                }
            }

            Resources resourcesLocal = this.resources;
            if (element.properties.containsKey(INCLUDE_RESOURCES_ATTRIBUTE)) {
                resourcesLocal = new Resources(resourcesLocal,
                    element.properties.get(INCLUDE_RESOURCES_ATTRIBUTE));
            }

            String mimeType = null;
            if (element.properties.containsKey(INCLUDE_MIME_TYPE_ATTRIBUTE)) {
                mimeType = element.properties.get(INCLUDE_MIME_TYPE_ATTRIBUTE);
            }

            if (mimeType == null) {
                // Get the file extension
                int i = src.lastIndexOf(".");
                if (i != -1) {
                    String extension = src.substring(i + 1);
                    mimeType = fileExtensions.get(extension);
                }
            }

            if (mimeType == null) {
                throw new SerializationException("Cannot determine MIME type of include \""
                    + src + "\".");
            }

            boolean inline = false;
            if (element.properties.containsKey(INCLUDE_INLINE_ATTRIBUTE)) {
                inline = Boolean.parseBoolean(element.properties.get(INCLUDE_INLINE_ATTRIBUTE));
            }

            // Determine an appropriate serializer to use for the include
            Class<? extends Serializer<?>> serializerClass = mimeTypes.get(mimeType);

            if (serializerClass == null) {
                throw new SerializationException("No serializer associated with MIME type "
                    + mimeType + ".");
            }

            Serializer<?> serializer;
            try {
                serializer = newIncludeSerializer(serializerClass);
            } catch (InstantiationException exception) {
                throw new SerializationException(exception);
            } catch (IllegalAccessException exception) {
                throw new SerializationException(exception);
            }

            // Determine location from src attribute
            URL locationLocal;
            if (src.charAt(0) == '/') {
                locationLocal = classLoader.getResource(src.substring(1));
            } else {
                locationLocal = new URL(this.location, src);
            }

            // Set optional resolution properties
            if (serializer instanceof Resolvable) {
                Resolvable resolvable = (Resolvable)serializer;
                if (inline) {
                    resolvable.setNamespace(namespace);
                }

                resolvable.setLocation(locationLocal);
                resolvable.setResources(resourcesLocal);
            }

            // Read the object
            InputStream inputStream = new BufferedInputStream(locationLocal.openStream());
            try {
                element.value = serializer.readObject(inputStream);
            } finally {
                inputStream.close();
            }
        } else if (element.type == Element.Type.REFERENCE) {
            // Dereference the value
            if (!element.properties.containsKey(REFERENCE_ID_ATTRIBUTE)) {
                throw new SerializationException(REFERENCE_ID_ATTRIBUTE
                    + " attribute is required for " + BXML_PREFIX + ":" + REFERENCE_TAG
                    + " tag.");
            }

            String id = element.properties.get(REFERENCE_ID_ATTRIBUTE);
            if (!namespace.containsKey(id)) {
                throw new SerializationException("A value with ID \"" + id + "\" does not exist.");
            }

            element.value = namespace.get(id);
        }

        // If the element has an ID, add the value to the namespace
        if (element.id != null) {
            namespace.put(element.id, element.value);

            // If the type has an ID property, use it
            Class<?> type = element.value.getClass();
            IDProperty idProperty = type.getAnnotation(IDProperty.class);

            if (idProperty != null) {
                BeanAdapter beanAdapter = new BeanAdapter(element.value);
                beanAdapter.put(idProperty.value(), element.id);
            }
        }
    }

    private void processAttributes() throws SerializationException {

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        for (int i = 0, n = xmlStreamReader.getAttributeCount(); i < n; i++) {
            String prefix = xmlStreamReader.getAttributePrefix(i);
            String localName = xmlStreamReader.getAttributeLocalName(i);
            String value = xmlStreamReader.getAttributeValue(i);

            if (prefix != null
                && prefix.equals(BXML_PREFIX)) {
                // The attribute represents an internal value
                if (localName.equals(ID_ATTRIBUTE)) {
                    if (value.length() == 0
                        || value.contains(".")) {
                        throw new IllegalArgumentException("\"" + value + "\" is not a valid ID value.");
                    }

                    if (namespace.containsKey(value)) {
                        throw new SerializationException("ID " + value + " is already in use.");
                    }

                    if (element.type != Element.Type.INSTANCE
                        && element.type != Element.Type.INCLUDE) {
                        throw new SerializationException("An ID cannot be assigned to this element.");
                    }

                    element.id = value;
                } else {
                    throw new SerializationException(BXML_PREFIX + ":" + localName
                        + " is not a valid attribute.");
                }
            } else {
                boolean property = false;

                switch (element.type) {
                    case INCLUDE: {
                        property = (localName.equals(INCLUDE_SRC_ATTRIBUTE)
                            || localName.equals(INCLUDE_RESOURCES_ATTRIBUTE)
                            || localName.equals(INCLUDE_MIME_TYPE_ATTRIBUTE)
                            || localName.equals(INCLUDE_INLINE_ATTRIBUTE));
                        break;
                    }

                    case SCRIPT: {
                        property = (localName.equals(SCRIPT_SRC_ATTRIBUTE));
                        break;
                    }

                    case REFERENCE: {
                        property = (localName.equals(REFERENCE_ID_ATTRIBUTE));
                        break;
                    }

                    default: {
                        break;
                    }
                }

                if (property) {
                    element.properties.put(localName, value);
                } else {
                    String name;
                    Class<?> propertyClass = null;

                    if (Character.isUpperCase(localName.charAt(0))) {
                        // The attribute represents a static property or listener list
                        int j = localName.indexOf('.');
                        name = localName.substring(j + 1);

                        String namespaceURI = xmlStreamReader.getAttributeNamespace(i);
                        if (namespaceURI == null || namespaceURI.isEmpty()) {
                            namespaceURI = xmlStreamReader.getNamespaceURI("");
                        }

                        String propertyClassName = namespaceURI + "." + localName.substring(0, j);
                        try {
                            propertyClass = Class.forName(propertyClassName, true, classLoader);
                        } catch (Throwable exception) {
                            throw new SerializationException(exception);
                        }
                    } else {
                        // The attribute represents an instance property
                        name = localName;
                    }

                    if (value.startsWith(NAMESPACE_BINDING_PREFIX)
                        && value.endsWith(NAMESPACE_BINDING_SUFFIX)) {
                        // The attribute represents a namespace binding
                        if (propertyClass != null) {
                            throw new SerializationException("Namespace binding is not supported for static properties.");
                        }

                        namespaceBindingAttributes.add(new Attribute(element, name, propertyClass,
                            value.substring(2, value.length() - 1)));
                    } else {
                        // Resolve the attribute value
                        Attribute attribute = new Attribute(element, name, propertyClass, value);

                        if (value.length() > 0) {
                            if (value.charAt(0) == URL_PREFIX) {
                                value = value.substring(1);

                                if (value.length() > 0) {
                                    if (value.charAt(0) == URL_PREFIX) {
                                        attribute.value = value;
                                    } else {
                                        if (location == null) {
                                            throw new IllegalStateException("Base location is undefined.");
                                        }

                                        try {
                                            attribute.value = new URL(location, value);
                                        } catch (MalformedURLException exception) {
                                            throw new SerializationException(exception);
                                        }
                                    }
                                } else {
                                    throw new SerializationException("Invalid URL resolution argument.");
                                }
                            } else if (value.charAt(0) == RESOURCE_KEY_PREFIX) {
                                value = value.substring(1);

                                if (value.length() > 0) {
                                    if (value.charAt(0) == RESOURCE_KEY_PREFIX) {
                                        attribute.value = value;
                                    } else {
                                        if (resources != null
                                            && JSON.containsKey(resources, value)) {
                                            attribute.value = JSON.get(resources, value);
                                        } else {
                                            attribute.value = value;
                                        }
                                    }
                                } else {
                                    throw new SerializationException("Invalid resource resolution argument.");
                                }
                            } else if (value.charAt(0) == OBJECT_REFERENCE_PREFIX) {
                                value = value.substring(1);

                                if (value.length() > 0) {
                                    if (value.charAt(0) == OBJECT_REFERENCE_PREFIX) {
                                        attribute.value = value;
                                    } else {
                                        if (value.equals(BXML_PREFIX + ":" + null)) {
                                            attribute.value = null;
                                        } else {
                                            if (!JSON.containsKey(namespace, value)) {
                                                throw new SerializationException("Value \"" + value + "\" is not defined.");
                                            }

                                            attribute.value = JSON.get(namespace, value);
                                        }
                                    }
                                } else {
                                    throw new SerializationException("Invalid object resolution argument.");
                                }
                            }
                        }

                        element.attributes.add(attribute);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processEndElement() throws SerializationException {

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        switch (element.type) {
            case INSTANCE:
            case INCLUDE:
            case REFERENCE: {
                // Apply attributes
                for (Attribute attribute : element.attributes) {
                    if (attribute.propertyClass == null) {
                        Dictionary<String, Object> dictionary;
                        if (element.value instanceof Dictionary<?, ?>) {
                            dictionary = (Dictionary<String, Object>)element.value;
                        } else {
                            dictionary = new BeanAdapter(element.value);
                        }

                        dictionary.put(attribute.name, attribute.value);
                    } else {
                        if (attribute.propertyClass.isInterface()) {
                            // The attribute represents an event listener
                            String listenerClassName = attribute.propertyClass.getName();
                            listenerClassName = listenerClassName.substring(listenerClassName.lastIndexOf('.') + 1);
                            String getListenerListMethodName = "get"
                                + Character.toUpperCase(listenerClassName.charAt(0))
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

                            // Create an invocation handler for this listener
                            ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(language);
                            AttributeInvocationHandler handler =
                                new AttributeInvocationHandler(scriptEngine, attribute.name,
                                    (String)attribute.value);

                            Object listener = Proxy.newProxyInstance(classLoader,
                                new Class<?>[]{attribute.propertyClass}, handler);

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
                            // The attribute represents a static setter
                            setStaticProperty(element.value, attribute.propertyClass,
                                attribute.name, attribute.value);
                        }
                    }
                }

                if (element.parent != null) {
                    if (element.parent.type == Element.Type.WRITABLE_PROPERTY) {
                        // Set this as the property value; it will be applied later in the
                        // parent's closing tag
                        element.parent.value = element.value;
                    } else if (element.parent.value != null) {
                        // If the parent element has a default property, use it; otherwise, if the
                        // parent is a sequence, add the element to it
                        Class<?> parentType = element.parent.value.getClass();
                        DefaultProperty defaultProperty = parentType.getAnnotation(DefaultProperty.class);

                        if (defaultProperty == null) {
                            if (element.parent.value instanceof Sequence<?>) {
                                Sequence<Object> sequence = (Sequence<Object>)element.parent.value;
                                sequence.add(element.value);
                            } else {
                                throw new SerializationException(element.parent.value.getClass()
                                    + " is not a sequence.");
                            }
                        } else {
                            String defaultPropertyName = defaultProperty.value();
                            BeanAdapter beanAdapter = new BeanAdapter(element.parent.value);
                            Object defaultPropertyValue = beanAdapter.get(defaultPropertyName);

                            if (defaultPropertyValue instanceof Sequence<?>) {
                                Sequence<Object> sequence = (Sequence<Object>)defaultPropertyValue;
                                try {
                                    sequence.add(element.value);
                                } catch (UnsupportedOperationException uoe) {
                                    beanAdapter.put(defaultPropertyName, element.value);
                                }
                            } else {
                                beanAdapter.put(defaultPropertyName, element.value);
                            }
                        }
                    }
                }

                break;
            }

            case READ_ONLY_PROPERTY: {
                Dictionary<String, Object> dictionary;
                if (element.value instanceof Dictionary<?, ?>) {
                    dictionary = (Dictionary<String, Object>)element.value;
                } else {
                    dictionary = new BeanAdapter(element.value);
                }

                // Process attributes looking for instance property setters
                for (Attribute attribute : element.attributes) {
                    if (attribute.propertyClass != null) {
                        throw new SerializationException("Static setters are not supported"
                            + " for read-only properties.");
                    }

                    dictionary.put(attribute.name, attribute.value);
                }

                break;
            }

            case WRITABLE_PROPERTY: {
                if (element.propertyClass == null) {
                    Dictionary<String, Object> dictionary;
                    if (element.parent.value instanceof Dictionary) {
                        dictionary = (Dictionary<String, Object>)element.parent.value;
                    } else {
                        dictionary = new BeanAdapter(element.parent.value);
                    }

                    dictionary.put(element.name, element.value);
                } else {
                    if (element.parent == null) {
                        throw new SerializationException("Element does not have a parent.");
                    }

                    if (element.parent.value == null) {
                        throw new SerializationException("Parent value is null.");
                    }

                    setStaticProperty(element.parent.value, element.propertyClass,
                        element.name, element.value);
                }

                break;
            }

            case LISTENER_LIST_PROPERTY: {
                // Evaluate the script
                String script = (String)element.value;
                ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(language);
                if (scriptEngine == null) {
                    throw new SerializationException("Script engine for \"" + language + "\" not found.");
                }

                // Don't pollute the engine namespace with the listener functions
                scriptEngine.setBindings(new SimpleBindings(), ScriptContext.ENGINE_SCOPE);

                try {
                    scriptEngine.eval(script);
                } catch (ScriptException exception) {
                    System.err.println(exception);
                    break;
                }

                // Create the listener and add it to the list
                BeanAdapter beanAdapter = new BeanAdapter(element.parent.value);
                ListenerList<?> listenerList = (ListenerList<?>)beanAdapter.get(element.name);
                Class<?> listenerListClass = listenerList.getClass();

                java.lang.reflect.Type[] genericInterfaces = listenerListClass.getGenericInterfaces();
                Class<?> listenerClass = (Class<?>)genericInterfaces[0];

                ElementInvocationHandler handler = new ElementInvocationHandler(scriptEngine);

                Method addMethod;
                try {
                    addMethod = listenerListClass.getMethod("add", Object.class);
                } catch (NoSuchMethodException exception) {
                    throw new RuntimeException(exception);
                }

                Object listener = Proxy.newProxyInstance(classLoader,
                    new Class<?>[]{listenerClass}, handler);

                try {
                    addMethod.invoke(listenerList, listener);
                } catch (IllegalAccessException exception) {
                    throw new SerializationException(exception);
                } catch (InvocationTargetException exception) {
                    throw new SerializationException(exception);
                }

                break;
            }

            case SCRIPT: {
                String src = null;
                if (element.properties.containsKey(INCLUDE_SRC_ATTRIBUTE)) {
                    src = element.properties.get(INCLUDE_SRC_ATTRIBUTE);
                }

                if (src != null) {
                    int i = src.lastIndexOf(".");
                    if (i == -1) {
                        throw new SerializationException("Cannot determine type of script \""
                            + src + "\".");
                    }

                    String extension = src.substring(i + 1);
                    ScriptEngine scriptEngine = scriptEngineManager.getEngineByExtension(extension);

                    if (scriptEngine == null) {
                        throw new SerializationException("Unable to find scripting engine for"
                            + " extension " + extension + ".");
                    }

                    scriptEngine.setBindings(scriptEngineManager.getBindings(), ScriptContext.ENGINE_SCOPE);

                    try {
                        URL scriptLocation;
                        if (src.charAt(0) == '/') {
                            scriptLocation = classLoader.getResource(src.substring(1));
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
                }

                if (element.value != null) {
                    // Evaluate the script
                    String script = (String)element.value;
                    ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(language);

                    if (scriptEngine == null) {
                        throw new SerializationException("Unable to find scripting engine for"
                            + " language \"" + language + "\".");
                    }

                    scriptEngine.setBindings(scriptEngineManager.getBindings(), ScriptContext.ENGINE_SCOPE);

                    try {
                        scriptEngine.eval(script);
                    } catch (ScriptException exception) {
                        System.err.println(exception);
                        System.err.println(script);
                    }
                }

                break;
            }

            case DEFINE: {
                // No-op
                break;
            }

            default: {
                break;
            }
        }

        // Move up the stack
        if (element.parent == null) {
            root = element.value;
        }

        element = element.parent;
    }

    /**
     * Return the current location of the XML parser.  Useful to ascertain the
     * location where an error occurred (if the error was not an XMLStreamException,
     * which has its own {@link XMLStreamException#getLocation} method).
     */
    public Location getCurrentLocation() {
        return xmlStreamReader.getLocation();
    }

    private void logException() {
        Location streamReaderlocation = xmlStreamReader.getLocation();
        String message = "An error occurred at line number " + streamReaderlocation.getLineNumber();

        if (location != null) {
            message += " in file " + location.getPath();
        }

        message += ":";

        System.err.println(message);
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

    @Override
    public Map<String, Object> getNamespace() {
        return namespace;
    }

    @Override
    public void setNamespace(Map<String, Object> namespace) {
        if (namespace == null) {
            throw new IllegalArgumentException();
        }

        this.namespace = namespace;
    }

    @Override
    public URL getLocation() {
        return location;
    }

    @Override
    public void setLocation(URL location) {
        this.location = location;
    }

    @Override
    public Resources getResources() {
        return resources;
    }

    @Override
    public void setResources(Resources resources) {
        this.resources = resources;
    }

    /**
     * Applies BXML binding annotations to an object.
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
     * Applies BXML binding annotations to an object.
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

            BXML bindingAnnotation = field.getAnnotation(BXML.class);

            if (bindingAnnotation != null) {
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

                String id = bindingAnnotation.id();
                if (id.equals("\0")) {
                    id = field.getName();
                }

                if (namespace.containsKey(id)) {
                    // Set the value into the field
                    Object value = namespace.get(id);
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
     * Creates a new serializer to be used on a nested include. The base
     * implementation simply calls {@code Class.newInstance()}. Subclasses may
     * override this method to provide an alternate instantiation mechanism,
     * such as dependency-injected construction.
     *
     * @param type
     * The type of serializer being requested.
     */
    protected Serializer<?> newIncludeSerializer(Class<? extends Serializer<?>> type)
        throws InstantiationException, IllegalAccessException {
        return type.newInstance();
    }

    /**
     * Creates a new typed object as part of the deserialization process.
     * The base implementation simply calls {@code Class.newInstance()}.
     * Subclasses may override this method to provide an alternate instantiation
     * mechanism, such as dependency-injected construction.
     *
     * @param type
     * The type of object being requested.
     */
    protected Object newTypedObject(Class<?> type)
        throws InstantiationException, IllegalAccessException {
        return type.newInstance();
    }

    /**
     * Gets a read-only version of the XML stream reader that's being used by
     * this serializer. Subclasses can use this to access information about the
     * current event.
     */
    protected final XMLStreamReader getXMLStreamReader() {
        return new StreamReaderDelegate(xmlStreamReader) {
            @Override
            public void close() {
                throw new UnsupportedOperationException();
            }

            @Override
            public int next() {
                throw new UnsupportedOperationException();
            }

            @Override
            public int nextTag() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Returns the file extension/MIME type map. This map associates file
     * extensions with MIME types, which are used to automatically determine
     * an appropriate serializer to use for an include based on file extension.
     *
     * @see #getMimeTypes()
     */
    public static Map<String, String> getFileExtensions() {
        return fileExtensions;
    }

    /**
     * Returns the MIME type/serializer class map. This map associates MIME types
     * with serializer classes. The serializer for a given MIME type will be used
     * to deserialize the data for an include that references the MIME type.
     */
    public static Map<String, Class<? extends Serializer<?>>> getMimeTypes() {
        return mimeTypes;
    }

    private static Method getStaticGetterMethod(Class<?> propertyClass, String propertyName,
        Class<?> objectType) {
        Method method = null;

        if (objectType != null) {
            try {
                method = propertyClass.getMethod(BeanAdapter.GET_PREFIX
                    + propertyName, objectType);
            } catch (NoSuchMethodException exception) {
                // No-op
            }

            if (method == null) {
                try {
                    method = propertyClass.getMethod(BeanAdapter.IS_PREFIX
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

    private static Method getStaticSetterMethod(Class<?> propertyClass, String propertyName,
        Class<?> objectType, Class<?> propertyValueType) {
        Method method = null;

        if (objectType != null) {
            final String methodName = BeanAdapter.SET_PREFIX + propertyName;

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

    private static void setStaticProperty(Object object, Class<?> propertyClass,
        final String propertyName, final Object value)
        throws SerializationException {
        Class<?> objectType = object.getClass();
        String propertyNameUpdated =
                Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        Object valueToAssign = value;

        Method setterMethod = null;
        if (valueToAssign != null) {
            setterMethod = getStaticSetterMethod(propertyClass, propertyNameUpdated,
                objectType, valueToAssign.getClass());
        }

        if (setterMethod == null) {
            Method getterMethod = getStaticGetterMethod(propertyClass, propertyNameUpdated, objectType);

            if (getterMethod != null) {
                Class<?> propertyType = getterMethod.getReturnType();
                setterMethod = getStaticSetterMethod(propertyClass, propertyNameUpdated,
                    objectType, propertyType);

                if (valueToAssign instanceof String) {
                    valueToAssign = BeanAdapter.coerce((String)valueToAssign, propertyType);
                }
            }
        }

        if (setterMethod == null) {
            throw new SerializationException(propertyClass.getName()
                + "." + propertyNameUpdated
                + " is not valid static property."
            );
        }

        // Invoke the setter
        try {
            setterMethod.invoke(null, object, valueToAssign);
        } catch (Exception exception) {
            throw new SerializationException(exception);
        }
    }
}
