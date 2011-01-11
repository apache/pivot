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
package org.apache.pivot.bxml;

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
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.io.SerializationException;
import org.apache.pivot.io.Serializer;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.ObservableMap;
import org.apache.pivot.util.ObservableMapListener;
import org.apache.pivot.util.Resources;

/**
 * Loads an object hierarchy from an XML document.
 */
public class BXMLSerializer implements Serializer<Object> {
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
        public final String name;
        public Object value;

        public String id = null;
        public final HashMap<String, String> properties = new HashMap<String, String>();
        public final LinkedList<Attribute> attributes = new LinkedList<Attribute>();

        public Element(Element parent, Type type, String name, Object value) {
            this.parent = parent;
            this.type = type;
            this.name = name;
            this.value = value;
        }
    }

    private static class Attribute {
        public final Element element;
        public final String name;
        public Object value;

        public Attribute(Element element, String name, Object value) {
            this.element = element;
            this.name = name;
            this.value = value;
        }
    }

    private static class ListenerInvocationHandler implements InvocationHandler {
        public final HashMap<String, String> functionMap;
        public final ScriptEngine scriptEngine;

        public ListenerInvocationHandler(HashMap<String, String> functionMap,
            ScriptEngine scriptEngine) {
            this.functionMap = functionMap;
            this.scriptEngine = scriptEngine;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
            String methodName = method.getName();
            String functionName = functionMap.get(methodName);

            Object result = null;
            if (functionName != null) {
                Invocable invocable;
                try {
                    invocable = (Invocable)scriptEngine;
                } catch (ClassCastException exception) {
                    throw new SerializationException(exception);
                }

                result = invocable.invokeFunction(methodName, args);
            }

            return result;
        }
    }

    private static class NamespaceBinding {
        public final Object source;
        public final String sourceKey;
        public final Object target;
        public final String targetKey;
        public final String mappingFunction;
        public final ScriptEngine scriptEngine;

        public NamespaceBinding(Object source, String sourceKey,
            Object target, String targetKey,
            String mappingFunction, ScriptEngine scriptEngine) {
            this.source = source;
            this.sourceKey = sourceKey;
            this.target = target;
            this.targetKey = targetKey;
            this.mappingFunction = mappingFunction;
            this.scriptEngine = scriptEngine;
        }

        public void apply() {
            // Get source value
            Object value = BeanAdapter.get(source, sourceKey);

            // Apply mapping function, if specified
            if (mappingFunction != null) {
                Invocable invocable = (Invocable)scriptEngine;

                try {
                    value = invocable.invokeFunction(mappingFunction, value);
                } catch (NoSuchMethodException exception) {
                    throw new RuntimeException(exception);
                } catch (ScriptException exception) {
                    throw new RuntimeException(exception);
                }
            }

            // Set target value
            BeanAdapter.set(target, targetKey, value);
        }
    };

    private Map<String, Object> namespace;
    private Charset charset;
    private XMLInputFactory xmlInputFactory;
    private ScriptEngineManager scriptEngineManager;

    private URL location = null;
    private Resources resources = null;

    private XMLStreamReader xmlStreamReader = null;
    private Element element = null;

    private Object root = null;
    private String language = null;

    private LinkedList<Attribute> namespaceBindingAttributes = new LinkedList<Attribute>();

    public static final String DEFAULT_CHARSET_NAME = "UTF-8";

    public static final char URL_PREFIX = '@';
    public static final char RESOURCE_KEY_PREFIX = '%';
    public static final char OBJECT_REFERENCE_PREFIX = '$';

    public static final String NAMESPACE_BINDING_PREFIX = OBJECT_REFERENCE_PREFIX + "{";
    public static final String NAMESPACE_BINDING_SUFFIX = "}";
    public static final String MAPPING_FUNCTION_DELIMITER = ":";

    public static final String LANGUAGE_PROCESSING_INSTRUCTION = "language";

    public static final String BXML_PREFIX = "bxml";
    public static final String BXML_EXTENSION = "bxml";
    public static final String ID_ATTRIBUTE = "id";

    public static final String INCLUDE_TAG = "include";
    public static final String INCLUDE_SRC_ATTRIBUTE = "src";
    public static final String INCLUDE_RESOURCES_ATTRIBUTE = "resources";
    public static final String INCLUDE_CHARSET_ATTRIBUTE = "charset";
    public static final String INCLUDE_INLINE_ATTRIBUTE = "inline";

    public static final String SCRIPT_TAG = "script";
    public static final String SCRIPT_SRC_ATTRIBUTE = "src";

    public static final String DEFINE_TAG = "define";

    public static final String REFERENCE_TAG = "reference";
    public static final String REFERENCE_ID_ATTRIBUTE = "id";

    public static final String DEFAULT_LANGUAGE = "javascript";

    public static final String MIME_TYPE = "application/bxml";

    public BXMLSerializer() {
        this(Charset.forName(DEFAULT_CHARSET_NAME));
    }

    public BXMLSerializer(Charset charset) {
        this(new HashMap<String, Object>(), charset);
    }

    public BXMLSerializer(Map<String, Object> namespace, Charset charset) {
        if (namespace == null) {
            throw new IllegalArgumentException();
        }

        if (charset == null) {
            throw new IllegalArgumentException();
        }

        this.namespace = namespace;
        this.charset = charset;

        xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty("javax.xml.stream.isCoalescing", true);

        scriptEngineManager = new javax.script.ScriptEngineManager();
        scriptEngineManager.setBindings(new SimpleBindings(namespace));
    }

    /**
     * Deserializes an object hierarchy from a BXML resource.
     * <p>
     * This version of the method does not support location or resource resolution.
     *
     * @param inputStream
     * An input stream containing the BXML data to deserialize.
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
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset);
                xmlStreamReader = xmlInputFactory.createXMLStreamReader(inputStreamReader);

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
        } finally {
            xmlStreamReader = null;
        }

        // Apply the namespace bindings
        applyNamespaceBindings();

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
     * <p>
     * This version of the method does not support resource resolution.
     *
     * @param location
     * The location of the BXML resource.
     *
     * @see #readObject(URL, Resources)
     */
    public final Object readObject(URL location)
        throws IOException, SerializationException {
        return readObject(location, null);
    }

    /**
     * Deserializes an object hierarchy from a BXML resource.
     *
     * @param location
     * The location of the BXML resource.
     *
     * @param resources
     * The resources that will be used to localize the deserialized resource.
     *
     * #see readObject(InputStream)
     */
    public final Object readObject(URL location, Resources resources)
        throws IOException, SerializationException {
        if (location == null) {
            throw new IllegalArgumentException("location is null.");
        }

        this.location = location;
        this.resources = resources;

        InputStream inputStream = new BufferedInputStream(location.openStream());

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
                throw new SerializationException("Language already set.");
            }

            language = piData;
        }
    }

    private void processCharacters() throws SerializationException {
        if (!xmlStreamReader.isWhiteSpace()) {
            // Process the text
            String text = xmlStreamReader.getText();

            switch (element.type) {
                case INSTANCE: {
                    // TODO If the parent element has a default property, set
                    // its value to text using BeanAdapter; otherwise, throw
                    // an exception

                    break;
                }

                case WRITABLE_PROPERTY:
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
                // The element represents a typed object
                if (namespaceURI == null) {
                    throw new SerializationException("No XML namespace specified for "
                        + localName + " tag.");
                }

                elementType = Element.Type.INSTANCE;
                name = "<" + ((prefix == null) ? "" : prefix + ":") + localName + ">";

                String className = namespaceURI + "." + localName.replace('.', '$');

                try {
                    Class<?> type = Class.forName(className);
                    value = newTypedObject(type);
                } catch (ClassNotFoundException exception) {
                    throw new SerializationException(exception);
                } catch (InstantiationException exception) {
                    throw new SerializationException(exception);
                } catch (IllegalAccessException exception) {
                    throw new SerializationException(exception);
                }
            } else {
                // The element represents a property
                if (prefix != null) {
                    throw new SerializationException("Property elements cannot have a namespace prefix.");
                }

                if (element.value instanceof Map<?, ?>) {
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
        element = new Element(element, elementType, name, value);
        processAttributes();

        if (elementType == Element.Type.INCLUDE) {
            // Load the include
            if (!element.properties.containsKey(INCLUDE_SRC_ATTRIBUTE)) {
                throw new SerializationException(INCLUDE_SRC_ATTRIBUTE
                    + " attribute is required for " + BXML_PREFIX + ":" + INCLUDE_TAG
                    + " tag.");
            }

            String src = element.properties.get(INCLUDE_SRC_ATTRIBUTE);

            Resources resources = this.resources;
            if (element.properties.containsKey(INCLUDE_RESOURCES_ATTRIBUTE)) {
                resources = new Resources(resources,
                    element.properties.get(INCLUDE_RESOURCES_ATTRIBUTE));
            }

            boolean inline = false;
            if (element.properties.containsKey(INCLUDE_INLINE_ATTRIBUTE)) {
                inline = Boolean.parseBoolean(element.properties.get(INCLUDE_INLINE_ATTRIBUTE));
            }

            Charset charset = this.charset;
            if (element.properties.containsKey(INCLUDE_CHARSET_ATTRIBUTE)) {
                charset = Charset.forName(element.properties.get(INCLUDE_CHARSET_ATTRIBUTE));
            }

            // Create a serializer for the include
            BXMLSerializer bxmlSerializer = (inline) ?
                new BXMLSerializer(namespace, charset) : new BXMLSerializer(charset);

            // Determine location from src attribute
            URL location;
            if (src.charAt(0) == '/') {
                ClassLoader classLoader = getClass().getClassLoader();
                location = classLoader.getResource(src.substring(1));
            } else {
                location = new URL(this.location, src);
            }

            element.value = bxmlSerializer.readObject(location, resources);
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
                            || localName.equals(INCLUDE_INLINE_ATTRIBUTE));
                        break;
                    }

                    case SCRIPT: {
                        property = (localName.equals(SCRIPT_SRC_ATTRIBUTE));
                        break;
                    }

                    case REFERENCE: {
                        property = (localName.equals(REFERENCE_ID_ATTRIBUTE));
                    }
                }

                if (property) {
                    element.properties.put(localName, value);
                } else {
                    // The attribute represents an instance property
                    if (value.startsWith(NAMESPACE_BINDING_PREFIX)
                        && value.endsWith(NAMESPACE_BINDING_SUFFIX)) {
                        // The attribute represents a namespace binding
                        namespaceBindingAttributes.add(new Attribute(element, localName,
                            value.substring(2, value.length() - 1)));
                    } else {
                        // Resolve the attribute value
                        Attribute attribute = new Attribute(element, localName, value);

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
                                            && BeanAdapter.isDefined(resources, value)) {
                                            attribute.value = BeanAdapter.get(resources, value);
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
                                            if (!BeanAdapter.isDefined(namespace, value)) {
                                                throw new SerializationException("Value \"" + value + "\" is not defined.");
                                            }

                                            attribute.value = BeanAdapter.get(namespace, value);
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
        switch (element.type) {
            case INSTANCE:
            case INCLUDE:
            case REFERENCE: {
                // Apply attributes
                for (Attribute attribute : element.attributes) {
                    Map<String, Object> map;
                    if (element.value instanceof Map<?, ?>) {
                        map = (Map<String, Object>)element.value;
                    } else {
                        map = new BeanAdapter(element.value);
                    }

                    map.put(attribute.name, attribute.value);
                }

                if (element.parent != null) {
                    if (element.parent.type == Element.Type.WRITABLE_PROPERTY) {
                        // Set this as the property value; it will be applied later in the
                        // parent's closing tag
                        element.parent.value = element.value;
                    } else if (element.parent.value != null) {
                        // If the parent element has a default property, use it; otherwise, if the
                        // parent is a list, add the element to it
                        Class<?> parentType = element.parent.value.getClass();
                        DefaultProperty defaultProperty = parentType.getAnnotation(DefaultProperty.class);

                        if (defaultProperty == null) {
                            if (element.parent.value instanceof List<?>) {
                                List<Object> list = (List<Object>)element.parent.value;
                                list.add(element.value);
                            } else {
                                throw new SerializationException(element.parent.value.getClass()
                                    + " is not a list.");
                            }
                        } else {
                            String defaultPropertyName = defaultProperty.value();
                            BeanAdapter beanAdapter = new BeanAdapter(element.parent.value);
                            Object defaultPropertyValue = beanAdapter.get(defaultPropertyName);

                            if (defaultPropertyValue instanceof List<?>) {
                                List<Object> list = (List<Object>)defaultPropertyValue;
                                list.add(element.value);
                            } else {
                                beanAdapter.put(defaultPropertyName, element.value);
                            }
                        }
                    }
                }

                break;
            }

            case READ_ONLY_PROPERTY: {
                Map<String, Object> map;
                if (element.value instanceof Map<?, ?>) {
                    map = (Map<String, Object>)element.value;
                } else {
                    map = new BeanAdapter(element.value);
                }

                // Process attributes looking for instance property setters
                for (Attribute attribute : element.attributes) {
                    map.put(attribute.name, attribute.value);
                }

                break;
            }

            case WRITABLE_PROPERTY: {
                Map<String, Object> map;
                if (element.parent.value instanceof Map) {
                    map = (Map<String, Object>)element.parent.value;
                } else {
                    map = new BeanAdapter(element.parent.value);
                }

                map.put(element.name, element.value);

                break;
            }

            case LISTENER_LIST_PROPERTY: {
                // Create the listener invocation handler
                HashMap<String, String> functionMap = new HashMap<String, String>();
                for (Attribute attribute : element.attributes) {
                    functionMap.put(attribute.name, (String)attribute.value);
                }

                ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(language);
                ListenerInvocationHandler handler = new ListenerInvocationHandler(functionMap, scriptEngine);

                // Get the type of the listener interface and create the listener proxy
                BeanAdapter beanAdapter = new BeanAdapter(element.parent.value);
                ListenerList<?> listenerList = (ListenerList<?>)beanAdapter.get(element.name);
                Class<?> listenerListClass = listenerList.getClass();

                java.lang.reflect.Type[] genericInterfaces = listenerListClass.getGenericInterfaces();
                Class<?> listenerClass = (Class<?>)genericInterfaces[0];

                Object listener = Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[]{listenerClass}, handler);

                // Add the listener
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
                            ClassLoader classLoader = getClass().getClassLoader();
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
            }
        }

        // Move up the stack
        if (element.parent == null) {
            root = element.value;
        }

        element = element.parent;
    }

    @SuppressWarnings("unchecked")
    private void applyNamespaceBindings() throws SerializationException {
        for (Attribute attribute : namespaceBindingAttributes) {
            // Determine source object, key, and mapping function
            Element element = attribute.element;
            String sourcePath = (String)attribute.value;

            String mappingFunction;
            int i = sourcePath.indexOf(MAPPING_FUNCTION_DELIMITER);
            if (i == -1) {
                mappingFunction = null;
            } else {
                mappingFunction = sourcePath.substring(0, i);
                sourcePath = sourcePath.substring(i + 1);
            }

            List<String> sourceKeys = BeanAdapter.parsePath(sourcePath);
            String sourceKey = sourceKeys.remove(sourceKeys.size() - 1);
            Object source = BeanAdapter.get(namespace, sourceKeys);

            // Determine target object and key
            Object target;
            String targetKey;
            switch (element.type) {
                case INSTANCE:
                case INCLUDE:
                case READ_ONLY_PROPERTY: {
                    target = element.value;
                    targetKey = attribute.name;
                    break;
                }

                default: {
                    throw new RuntimeException("Unsupported element type in namespace binding.");
                }
            }

            ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(language);

            final NamespaceBinding namespaceBinding = new NamespaceBinding(source, sourceKey,
                target, targetKey, mappingFunction, scriptEngine);

            // Perform the initial binding
            namespaceBinding.apply();

            // Listen for subsequent bind events
            ObservableMap<String, Object> map = (source instanceof ObservableMap<?, ?>) ?
                (ObservableMap<String, Object>)source : new BeanAdapter(source);

            ObservableMapListener<String, Object> listener = new ObservableMapListener.Adapter<String, Object>() {
                @Override
                public void valueUpdated(ObservableMap<String, Object> map, String key, Object previousValue) {
                    if (key.equals(namespaceBinding.sourceKey)) {
                        namespaceBinding.apply();
                    }
                }
            };

            map.getObservableMapListeners().add(listener);
        }

        namespaceBindingAttributes.clear();
    }

    private void logException(Exception exception) {
        Location streamReaderlocation = xmlStreamReader.getLocation();
        String message = "An error occurred at line number " + streamReaderlocation.getLineNumber();

        if (location != null) {
            message += " in file " + location.getPath();
        }

        message += ":\n" + exception.getMessage();

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
     * Creates a new typed object as part of the deserialization process.
     * The base implementation simply calls {@link Class#newInstance()}.
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
}
