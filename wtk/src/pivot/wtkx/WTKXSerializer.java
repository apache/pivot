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
package pivot.wtkx;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;

import pivot.beans.BeanDictionary;
import pivot.beans.PropertyNotFoundException;
import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.collections.List;
import pivot.collections.Sequence;
import pivot.serialization.Serializer;
import pivot.serialization.SerializationException;
import pivot.util.ListenerList;
import pivot.util.Resources;

/**
 * Loads an object hierarchy from an XML document.
 *
 * @author gbrown
 */
public class WTKXSerializer implements Serializer<Object> {
    private static class Element  {
        public enum Type {
            INSTANCE,
            INCLUDE,
            SCRIPT,
            READ_ONLY_PROPERTY,
            WRITABLE_PROPERTY
        }

        public final Element parent;
        public final String tagName;
        public final Type type;
        public final List<Attribute> attributes;

        public Object value;

        public Element(Element parent, String tagName, Type type, List<Attribute> attributes, Object value) {
            this.parent = parent;
            this.tagName = tagName;
            this.type = type;
            this.attributes = attributes;
            this.value = value;
        }
    }

    private static class Attribute {
        public final String namespaceURI;
        public final String prefix;
        public final String localName;
        public final String value;

        public Attribute(String namespaceURI, String prefix, String localName, String value) {
            this.namespaceURI = namespaceURI;
            this.prefix = prefix;
            this.localName = localName;
            this.value = value;
        }
    }

    private URL location = null;
    private Resources resources = null;

    private HashMap<String, Object> namedObjects = new HashMap<String, Object>();
    private HashMap<String, WTKXSerializer> includeSerializers = new HashMap<String, WTKXSerializer>();

    private XMLInputFactory xmlInputFactory;
    private Object scriptEngineManager;
    private Class<?> scriptEngineManagerClass;

    public static final char URL_PREFIX = '@';
    public static final char RESOURCE_KEY_PREFIX = '%';
    public static final char OBJECT_REFERENCE_PREFIX = '$';

    public static final String WTKX_PREFIX = "wtkx";
    public static final String ID_ATTRIBUTE = "id";

    public static final String INCLUDE_TAG = "include";
    public static final String INCLUDE_SRC_ATTRIBUTE = "src";
    public static final String INCLUDE_NAMESPACE_ATTRIBUTE = "namespace";
    public static final String INCLUDE_RESOURCES_ATTRIBUTE = "resources";

    public static final String SCRIPT_TAG = "script";
    public static final String SCRIPT_SRC_ATTRIBUTE = "src";

    public static final String MIME_TYPE = "application/wtkx";

    public WTKXSerializer() {
        this(null);
    }

    public WTKXSerializer(Resources resources) {
        this.resources = resources;

        xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty("javax.xml.stream.isCoalescing", true);

        try {
        	scriptEngineManagerClass = Class.forName("javax.script.ScriptEngineManager");
        	scriptEngineManager = scriptEngineManagerClass.newInstance();
        } catch(Exception exception) {
        	scriptEngineManagerClass = null;
        	scriptEngineManager = null;
        }
    }

    public Resources getResources() {
        return resources;
    }

    public Object readObject(String resourceName) throws IOException,
        SerializationException {
        if (resourceName == null) {
            throw new IllegalArgumentException("resourceName is null.");
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL location = classLoader.getResource(resourceName);

        if (location == null) {
            throw new SerializationException("Could not find resource named \""
                + resourceName + "\".");
        }

        return readObject(location);
    }

    public Object readObject(URL location) throws IOException, SerializationException {
        if (location == null) {
            throw new IllegalArgumentException("location is null.");
        }

        this.location = location;
        return readObject(new BufferedInputStream(location.openStream()));
    }

    @SuppressWarnings("unchecked")
    public Object readObject(InputStream inputStream) throws IOException,
        SerializationException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is null.");
        }

        Object object = null;

        // Clear any previous named objects and include serializers
        namedObjects.clear();
        includeSerializers.clear();

        // Parse the XML stream
        Element element = null;
        try {
            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(inputStream);

            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.CHARACTERS: {
                        if (!reader.isWhiteSpace()) {
                            String text = reader.getText();

                            if (text.length() > 0) {
                                switch (element.type) {
                                    case INSTANCE: {
                                        if (element.value instanceof Sequence) {
                                            Sequence<Object> sequence = (Sequence<Object>)element.value;

                                            try {
                                                Method addMethod = sequence.getClass().getMethod("add",
                                                    new Class<?>[] {String.class});
                                                addMethod.invoke(sequence, new Object[] {text});
                                            } catch(NoSuchMethodException exception) {
                                                throw new SerializationException("Text content cannot be added to "
                                                    + sequence.getClass().getName() + ".", exception);
                                            } catch(InvocationTargetException exception) {
                                                throw new SerializationException(exception);
                                            } catch(IllegalAccessException exception) {
                                                throw new SerializationException(exception);
                                            }
                                        }

                                        break;
                                    }

                                    case WRITABLE_PROPERTY: {
                                        element.value = text;
                                        break;
                                    }
                                }
                            }
                        }

                        break;
                    }

                    case XMLStreamConstants.START_ELEMENT: {
                        String namespaceURI = reader.getNamespaceURI();
                        String prefix = reader.getPrefix();
                        String localName = reader.getLocalName();

                        String id = null;

                        if (prefix != null
                            && prefix.equals(WTKX_PREFIX)) {
                            if (element == null) {
                                throw new SerializationException(prefix + ":" + localName
                                    + " is not a valid root element.");
                            }

                            if (localName.equals(INCLUDE_TAG)) {
                                // The element represents an include
                                String src = null;
                                String namespace = null;

                                Resources includeResources = resources;

                                ArrayList<Attribute> attributes = new ArrayList<Attribute>();

                                for (int i = 0, n = reader.getAttributeCount(); i < n; i++) {
                                    String attributeNamespaceURI = reader.getAttributeNamespace(i);
                                    if (attributeNamespaceURI == null) {
                                        attributeNamespaceURI = reader.getNamespaceURI("");
                                    }

                                    String attributePrefix = reader.getAttributePrefix(i);
                                    String attributeLocalName = reader.getAttributeLocalName(i);
                                    String attributeValue = reader.getAttributeValue(i);

                                    if (attributePrefix != null
                                        && attributePrefix.equals(WTKX_PREFIX)) {
                                        if (attributeLocalName.equals(ID_ATTRIBUTE)) {
                                            id = attributeValue;
                                        }
                                    } else {
                                        if (attributeLocalName.equals(INCLUDE_SRC_ATTRIBUTE)) {
                                            src = attributeValue;
                                        } else if (attributeLocalName.equals(INCLUDE_NAMESPACE_ATTRIBUTE)) {
                                            namespace = attributeValue;
                                        } else if (attributeLocalName.equals(INCLUDE_RESOURCES_ATTRIBUTE)) {
                                            includeResources = new Resources(attributeValue);
                                        } else {
                                            attributes.add(new Attribute(attributeNamespaceURI,
                                                attributePrefix, attributeLocalName, attributeValue));
                                        }
                                    }
                                }

                                if (src == null) {
                                    throw new SerializationException(INCLUDE_SRC_ATTRIBUTE
                                        + " attribute is required for " + WTKX_PREFIX + ":" + INCLUDE_TAG
                                        + " tag.");
                                }

                                // Process the include
                                WTKXSerializer serializer = new WTKXSerializer(includeResources);
                                if (namespace != null) {
                                    includeSerializers.put(namespace, serializer);
                                }

                                Object value;
                                if (src.charAt(0) == '/') {
                                    value = serializer.readObject(src.substring(1));
                                } else {
                                    value = serializer.readObject(new URL(location, src));
                                }

                                element = new Element(element, localName, Element.Type.INCLUDE, attributes, value);
                            } else if (localName.equals(SCRIPT_TAG)) {
                            	if (scriptEngineManagerClass == null) {
                            		throw new SerializationException("Scripting is not supported on this platform.");
                            	}

                                // The element represents a script
                                String src = null;

                                for (int i = 0, n = reader.getAttributeCount(); i < n; i++) {
                                    String attributeLocalName = reader.getAttributeLocalName(i);
                                    String attributeValue = reader.getAttributeValue(i);

                                    if (attributeLocalName.equals(SCRIPT_SRC_ATTRIBUTE)) {
                                        src = attributeValue;
                                    } else {
                                    	throw new SerializationException(attributeLocalName + " is not a valid "
                                			+ " attribute for the " + WTKX_PREFIX + ":" + SCRIPT_TAG + ".");
                                    }
                                }

                                if (src == null) {
                                    throw new SerializationException(INCLUDE_SRC_ATTRIBUTE
                                        + " attribute is required for " + WTKX_PREFIX + ":" + SCRIPT_TAG
                                        + " tag.");
                                }

                            	int i = src.lastIndexOf(".");
                            	if (i == -1) {
                            		throw new SerializationException("Cannot determine type of script \""
                        				+ src + "\".");
                            	}

                            	String extension = src.substring(i + 1);

                            	Object scriptEngine = null;
                            	try {
                                	Method getEngineByExtensionMethod =
                                		scriptEngineManagerClass.getMethod("getEngineByExtension", new Class<?>[] {String.class});

                                    scriptEngine = getEngineByExtensionMethod.invoke(scriptEngineManager, new Object[] {extension});
                            	} catch(Exception exception) {
                            		throw new RuntimeException(exception);
                            	}

                            	if (scriptEngine == null) {
                                	throw new SerializationException("Unable to find scripting engine for "
                            			+ " extension " + extension + ".");
                                }

                                try {
                                    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                                	URL scriptLocation;

                                	if (src.charAt(0) == '/') {
                                		scriptLocation = classLoader.getResource(src);
                                    } else {
                                    	scriptLocation = new URL(location, src);
                                    }

                                	Class<?> bindingsClass = Class.forName("javax.script.Bindings");
                                	Method evalMethod = scriptEngine.getClass().getMethod("eval",
                                			new Class<?>[] {Reader.class, bindingsClass});

                                	Method getBindingsMethod =
                                		scriptEngineManagerClass.getMethod("getBindings", new Class<?>[] {});

                                	Object bindings = getBindingsMethod.invoke(scriptEngineManager, new Object[] {});
                                	Reader scriptReader = new BufferedReader(new InputStreamReader(scriptLocation.openStream()));
                                	evalMethod.invoke(scriptEngine, new Object[] {scriptReader, bindings});
                                } catch(Exception exception) {
                                	throw new SerializationException(exception);
                                }

                                element = new Element(element, localName, Element.Type.SCRIPT, null, null);
                            } else {
                                throw new SerializationException(prefix + ":" + localName
                                    + " is not a valid tag.");
                            }
                        } else {
                            ArrayList<Attribute> attributes = new ArrayList<Attribute>();

                            for (int i = 0, n = reader.getAttributeCount(); i < n; i++) {
                                String attributeNamespaceURI = reader.getAttributeNamespace(i);
                                if (attributeNamespaceURI == null) {
                                    attributeNamespaceURI = reader.getNamespaceURI("");
                                }

                                String attributePrefix = reader.getAttributePrefix(i);
                                String attributeLocalName = reader.getAttributeLocalName(i);
                                String attributeValue = reader.getAttributeValue(i);

                                if (attributePrefix != null
                                    && attributePrefix.equals(WTKX_PREFIX)) {
                                    if (attributeLocalName.equals(ID_ATTRIBUTE)) {
                                        id = attributeValue;
                                    }
                                } else {
                                    attributes.add(new Attribute(attributeNamespaceURI,
                                        attributePrefix, attributeLocalName, attributeValue));
                                }
                            }

                            if (Character.isUpperCase(localName.charAt(0))) {
                                // The element represents a typed object
                                if (namespaceURI == null) {
                                    throw new SerializationException("No XML namespace specified for "
                                        + localName + " tag.");
                                }

                                String className = namespaceURI + "." + localName.replace('.', '$');

                                try {
                                    Class<?> type = Class.forName(className);
                                    element = new Element(element, localName, Element.Type.INSTANCE, attributes, type.newInstance());
                                } catch(Exception exception) {
                                    throw new SerializationException(exception);
                                }
                            } else {
                                // This element represents a property
                                if (element == null) {
                                    throw new SerializationException("Root node must represent a typed object.");
                                }

                                BeanDictionary propertyDictionary = new BeanDictionary(element.value);

                                if (propertyDictionary.isReadOnly(localName)) {
                                    Object value = propertyDictionary.get(localName);
                                    assert (value != null) : "Read-only properties cannot be null.";
                                    element = new Element(element, localName, Element.Type.READ_ONLY_PROPERTY, attributes, value);
                                } else {
                                    if (attributes.getLength() > 0) {
                                        throw new SerializationException("Writable property elements cannot have attributes.");
                                    }

                                    element = new Element(element, localName, Element.Type.WRITABLE_PROPERTY, null, null);
                                }
                            }
                        }

                        switch (element.type) {
                            case INCLUDE:
                            case INSTANCE: {
                                // If the element's parent is a sequence or a listener list, add
                                // the element value to it
                                if (element.parent != null) {
                                    if (element.parent.value instanceof Sequence) {
                                        Sequence<Object> sequence = (Sequence<Object>)element.parent.value;
                                        sequence.add(element.value);
                                    } else {
                                        if (element.parent.value instanceof ListenerList) {
                                            ListenerList<Object> listenerList = (ListenerList<Object>)element.parent.value;
                                            listenerList.add(element.value);
                                        }
                                    }
                                }

                                // If an ID was specified, add the value to the named object map
                                if (id != null) {
                                    if (id.length() == 0) {
                                        throw new IllegalArgumentException(WTKX_PREFIX + ":" + ID_ATTRIBUTE
                                            + " must not be null.");
                                    }

                                    namedObjects.put(id, element.value);

                                    if (scriptEngineManager != null) {
                                    	try {
	                                    	Method putMethod = scriptEngineManagerClass.getMethod("put",
                                    			new Class<?>[] {String.class, Object.class});
	                                    	putMethod.invoke(scriptEngineManager, new Object[] {id, namedObjects.get(id)});
                                    	} catch(Exception exception) {
                                    		throw new RuntimeException(exception);
                                    	}
                                	}
                                }

                                break;
                            }
                        }

                        break;
                    }

                    case XMLStreamConstants.END_ELEMENT: {
                        String localName = reader.getLocalName();

                        switch (element.type) {
                            case WRITABLE_PROPERTY: {
                                BeanDictionary propertyDictionary = new BeanDictionary(element.parent.value);
                                propertyDictionary.put(localName, element.value);
                                break;
                            }

                            case SCRIPT: {
                            	break;
                            }

                            default: {
                                if (element.value instanceof Dictionary) {
                                    // The element is an untyped object
                                    Dictionary<String, Object> dictionary = (Dictionary<String, Object>)element.value;

                                    for (Attribute attribute : element.attributes) {
                                        if (Character.isUpperCase(attribute.localName.charAt(0))) {
                                            throw new SerializationException("Static setters are only supported for typed instances.");
                                        }

                                        // Resolve and apply the attribute
                                        dictionary.put(attribute.localName, resolve(attribute.value, null));
                                    }
                                } else {
                                    // The element represents a typed object; apply the attributes
                                    BeanDictionary valueDictionary = new BeanDictionary(element.value);

                                    for (Attribute attribute : element.attributes) {
                                        if (Character.isUpperCase(attribute.localName.charAt(0))) {
                                            // The property represents an attached value
                                            setStaticProperty(attribute, element.value);
                                        } else {
                                            Class<?> type = valueDictionary.getType(attribute.localName);

                                            if (type != null
                                                && ListenerList.class.isAssignableFrom(type)) {
                                                // The property represents a listener list
                                                ListenerList<Object> listenerList = (ListenerList<Object>)valueDictionary.get(attribute.localName);

                                                // The attribute value is a comma-separated list of listener IDs
                                                String[] listenerIDs = attribute.value.split(",");

                                                for (int i = 0, n = listenerIDs.length; i < n; i++) {
                                                    String listenerID = listenerIDs[i].trim();

                                                    if (listenerID.length() > 0) {
                                                        listenerID = listenerID.substring(1);

                                                        if (listenerID.length() > 0) {
                                                            listenerList.add(getObjectByID(listenerID));
                                                        }
                                                    }
                                                }
                                            } else {
                                                valueDictionary.put(attribute.localName,
                                                    resolve(attribute.value, valueDictionary.getType(attribute.localName)));
                                            }
                                        }
                                    }
                                }

                                // If the parent element is a writable property, set this as its value; it
                                // will be applied later in the parent's closing tag
                                if (element.parent != null
                                    && element.parent.type == Element.Type.WRITABLE_PROPERTY) {
                                    element.parent.value = element.value;
                                }
                            }
                        }

                        // If this is the top of the stack, return this element's value;
                        // otherwise, move up the stack
                        if (element.parent == null) {
                            object = element.value;
                        } else {
                            element = element.parent;
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

    public String getMIMEType(Object object) {
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
        	object = serializer.getObjectByID(namespacePath[i]);
        }

        return object;
    }

    private Object getObjectByID(String id) {
    	Object object = null;

    	if (namedObjects.containsKey(id)) {
    		object = namedObjects.get(id);
    	} else {
    		if (scriptEngineManager != null) {
            	try {
                	Method getMethod = scriptEngineManagerClass.getMethod("get", new Class<?>[] {String.class});
                	object = getMethod.invoke(scriptEngineManager, new Object[] {id});
            	} catch(Exception exception) {
            		throw new RuntimeException(exception);
            	}
    		}
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
     * The property type, or <tt>null</tt> if the type is not known.
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
                                throw new IllegalStateException("Resource dictionary is undefined.");
                            }

                            resolvedValue = resources.get(attributeValue.substring(1));
                        }
                    }
                } else if (attributeValue.charAt(0) == OBJECT_REFERENCE_PREFIX) {
                    if (attributeValue.length() > 1) {
                        if (attributeValue.charAt(1) == OBJECT_REFERENCE_PREFIX) {
                            resolvedValue = attributeValue.substring(1);
                        } else {
                            resolvedValue = getObjectByID(attributeValue.substring(1));
                        }
                    }
                } else {
                    resolvedValue = attributeValue;
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
     * @param attribute
     * The attribute whose corresponding static setter is to be invoked.
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

        String propertyClassName = attribute.namespaceURI + "."
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
                    Class<?> primitivePropertyValueType = (Class<?>)primitiveTypeField.get(null);

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
