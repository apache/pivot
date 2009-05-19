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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URI;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;

import pivot.beans.BeanDictionary;
import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.collections.Sequence;
import pivot.util.ListenerList;

/**
 * Translates WTKX documents into compilable Java file objects.
 *
 * @author tvolkert
 */
public class Translator {
    /**
     * A generated Java file object.
     *
     * @author tvolkert
     */
    private static class JavaFile implements JavaFileObject {
        private final File file;

        public JavaFile() {
            try {
                file = File.createTempFile(WTKX_PREFIX, JAVA_SUFFIX);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }

        public URI toUri() {
            return file.toURI();
        }

        public String getName() {
            return file.getPath();
        }

        public InputStream openInputStream() throws IOException {
            return new FileInputStream(file);
        }

        public OutputStream openOutputStream() throws IOException {
            return new FileOutputStream(file);
        }

        public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
            return new FileReader(file);
        }

        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            String result;

            Reader reader = openReader(ignoreEncodingErrors);
            try {
                char[] buf = new char[(int)file.length()];
                int n = reader.read(buf, 0, buf.length);
                result = new String(buf, 0, n);
            } finally {
                reader.close();
            }

            return result;
        }

        public Writer openWriter() throws IOException {
            return new FileWriter(file);
        }

        public long getLastModified() {
            return file.lastModified();
        }

        public boolean delete() {
            return file.delete();
        }

        public Kind getKind() {
            return Kind.SOURCE;
        }

        public boolean isNameCompatible(String simpleName, Kind kind) {
            return (kind == Kind.SOURCE);
        }

        public NestingKind getNestingKind() {
            return NestingKind.TOP_LEVEL;
        }

        public Modifier getAccessLevel() {
            return Modifier.PUBLIC;
        }
    }

    /**
     * A parsed XML element.
     *
     * @author gbrown
     */
    private static class Element  {
        public enum Type {
            INSTANCE,
            INCLUDE,
            READ_ONLY_PROPERTY,
            WRITABLE_PROPERTY
        }

        public final Element parent;
        public final Type type;
        public final List<Attribute> attributes;

        public Class<?> clazz;
        public int ref;

        public static int counter = -1;

        public Element(Element parent, Type type, List<Attribute> attributes, Class<?> clazz, int ref) {
            this.parent = parent;
            this.type = type;
            this.attributes = attributes;
            this.clazz = clazz;
            this.ref = ref;
        }
    }

    /**
     * A parsed XML attribute.
     *
     * @author gbrown
     */
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

    private XMLInputFactory xmlInputFactory;

    private static final String WTKX_PREFIX = "WTKX";
    private static final String JAVA_SUFFIX = ".java";

    private static final String SPACE = "";

    public Translator() {
        xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty("javax.xml.stream.isCoalescing", true);
    }

    /**
     * Reads WTKX input stream into a compilable file object. The file object
     * will hold a class that implements the {@link Bindable.ObjectHierarchy}
     * interface.
     *
     * @param inputStream
     * The data stream from which the WTKX will be read
     *
     * @param className
     * The fully qualified class name of the class to generate.
     *
     * @return
     * The compilable java file object represented by the WTKX
     */
    public JavaFileObject translate(InputStream inputStream, String className)
        throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is null.");
        }

        if (className == null) {
            throw new IllegalArgumentException("className is null.");
        }

        JavaFile javaFile = new JavaFile();
        String packageName = null;

        // Separate the package name from the un-qualified class name
        int classDeliminatorIndex = className.lastIndexOf('.');
        if (classDeliminatorIndex != -1) {
            if (classDeliminatorIndex == 0) {
                throw new IllegalArgumentException(className + " is not a valid class name.");
            }

            packageName = className.substring(0, classDeliminatorIndex);
            className = className.substring(classDeliminatorIndex + 1);
        }

        Writer writer = javaFile.openWriter();
        try {
            if (packageName != null) {
                writer.write("package " + packageName + ";\n\n");
            }

            writer.write(String.format(
                "import pivot.collections.HashMap;\n" +
                "import pivot.wtkx.Bindable;\n" +
                "\n" +
                "public class %2$s implements Bindable.ObjectHierarchy {\n" +
                "%1$4sprivate HashMap<String, Object> namedObjects = new HashMap<String, Object>();\n" +
                "\n" +
                "%1$4s@SuppressWarnings(\"unchecked\")\n" +
                "%1$4spublic <T> T getObjectByID(String id) {\n" +
                "%1$8sObject object = namedObjects.get(id);\n" +
                "%1$8sreturn (T)object;\n" +
                "%1$4s}\n" +
                "\n" +
                "%1$4s@SuppressWarnings({\"unchecked\", \"cast\"})\n" +
                "%1$4spublic <T> T getRootObject() {\n" +
                "%1$8sObject result = null;\n",
                SPACE, className));

            // Parse the XML stream
            Element element = null;
            try {
                XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(inputStream);

                while (reader.hasNext()) {
                    int event = reader.next();

                    switch (event) {
                    case XMLStreamConstants.CHARACTERS: {
                        // TODO
                        break;
                    }

                    case XMLStreamConstants.START_ELEMENT: {
                        String namespaceURI = reader.getNamespaceURI();
                        String prefix = reader.getPrefix();
                        String localName = reader.getLocalName();

                        String id = null;

                        if (prefix != null
                            && prefix.equals(WTKXSerializer.WTKX_PREFIX)) {
                            if (element == null) {
                                throw new IOException(prefix + ":" + localName
                                    + " is not a valid root element.");
                            }

                            if (localName.equals(WTKXSerializer.INCLUDE_TAG)) {
                                // TODO
                                throw new IOException(prefix + ":" + localName
                                    + " compilation is not yet implemented.");
                            } else if (localName.equals(WTKXSerializer.SCRIPT_TAG)) {
                                throw new IOException(prefix + ":" + localName
                                    + " tags may not be compiled.");
                            } else {
                                throw new IOException(prefix + ":" + localName
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
                                    && attributePrefix.equals(WTKXSerializer.WTKX_PREFIX)) {
                                    if (attributeLocalName.equals(WTKXSerializer.ID_ATTRIBUTE)) {
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
                                    throw new IOException("No XML namespace specified for "
                                        + localName + " tag.");
                                }

                                String elementClassName = namespaceURI + "." + localName;
                                writer.write(String.format("%8s%s o%d = new %s();\n",
                                    SPACE, elementClassName, ++Element.counter, elementClassName));

                                try {
                                    elementClassName = namespaceURI + "." + localName.replace('.', '$');
                                    Class<?> type = Class.forName(elementClassName,
                                        false, getClass().getClassLoader());
                                    element = new Element(element, Element.Type.INSTANCE,
                                        attributes, type, Element.counter);
                                } catch(Exception exception) {
                                    throw new IOException(exception);
                                }
                            } else {
                                // The element represents a property
                                if (element == null) {
                                    throw new IOException
                                        ("Root node must represent a typed object.");
                                }

                                if (element.type != Element.Type.INSTANCE) {
                                    throw new IOException
                                        ("Property elements must apply to typed objects.");
                                }

                                Class<?> type = (Class<?>)element.clazz;

                                if (BeanDictionary.isReadOnly(type, localName)) {
                                    Class<?> valueType = BeanDictionary.getType(type, localName);
                                    Method getterMethod = BeanDictionary.getGetterMethod(type, localName);

                                    // Instantiate the property so we have a reference to it
                                    writer.write(String.format
                                        ("%8s%s o%d = o%d.%s();\n", SPACE, valueType.getName().replace('$', '.'),
                                        ++Element.counter, element.ref, getterMethod.getName()));
                                    writer.write(String.format
                                        ("%8sassert (o%d != null) : \"Read-only properties cannot be null.\";\n",
                                         SPACE, Element.counter));

                                    element = new Element(element, Element.Type.READ_ONLY_PROPERTY,
                                        attributes, valueType, Element.counter);
                                } else {
                                    if (attributes.getLength() > 0) {
                                        throw new IOException
                                            ("Writable property elements cannot have attributes.");
                                    }

                                    element = new Element(element, Element.Type.WRITABLE_PROPERTY,
                                        null, null, -1);
                                }
                            }
                        }

                        switch (element.type) {
                        case INCLUDE:
                        case INSTANCE: {
                            // If the element's parent is a sequence or a listener list, add
                            // the element value to it
                            if (element.parent != null) {
                                Class<?> parentType = (Class<?>)element.parent.clazz;

                                if (parentType != null
                                    && (Sequence.class.isAssignableFrom(parentType)
                                    || ListenerList.class.isAssignableFrom(parentType))) {
                                    writer.write(String.format("%8so%d.add(o%d);\n",
                                        SPACE, element.parent.ref, element.ref));
                                }
                            }

                            // If an ID was specified, add the value to the named object map
                            if (id != null) {
                                if (id.length() == 0) {
                                    throw new IllegalArgumentException(WTKXSerializer.WTKX_PREFIX
                                        + ":" + WTKXSerializer.ID_ATTRIBUTE
                                        + " must not be null.");
                                }

                                writer.write(String.format
                                    ("%8snamedObjects.put(\"%s\", o%d);\n", SPACE, id, element.ref));
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
                            Class<?> type = (Class<?>)element.clazz;
                            Class<?> parentType = (Class<?>)element.parent.clazz;
                            Method setterMethod = BeanDictionary.getSetterMethod(parentType, localName, type);

                            writer.write(String.format("%8so%d.%s(o%d);\n",
                                SPACE, element.parent.ref, setterMethod.getName(), element.ref));

                            break;
                        }

                        default: {
                            Class<?> type = (Class<?>)element.clazz;

                            if (type != null && Dictionary.class.isAssignableFrom(type)) {
                                // The element is an untyped object
                                for (Attribute attribute : element.attributes) {
                                    if (Character.isUpperCase(attribute.localName.charAt(0))) {
                                        throw new IOException
                                            ("Static setters are only supported for typed instances.");
                                    }

                                    // Resolve and apply the attribute
                                    writer.write(String.format("%8so%d.put(\"%s\", %s);\n", SPACE, element.ref,
                                        attribute.localName, resolve(attribute.value, null)));
                                }
                            } else {
                                // The element represents a typed object; apply the attributes
                                for (Attribute attribute : element.attributes) {
                                    Class<?> attributeType = BeanDictionary.getType(type, attribute.localName);

                                    if (Character.isUpperCase(attribute.localName.charAt(0))) {
                                        // The property represents an attached value
                                        String propertyName = attribute.localName.substring
                                            (attribute.localName.lastIndexOf(".") + 1);
                                        propertyName = Character.toUpperCase(propertyName.charAt(0)) +
                                            propertyName.substring(1);
                                        String setterMethodName = BeanDictionary.SET_PREFIX + propertyName;

                                        String propertyClassName = attribute.namespaceURI + "."
                                            + attribute.localName.substring(0, attribute.localName.length()
                                            - (propertyName.length() + 1));

                                        writer.write(String.format("%8s%s.%s(o%d, %s);\n",
                                            SPACE, propertyClassName, setterMethodName, element.ref,
                                            resolve(attribute.value, attributeType)));
                                    } else {
                                        if (attributeType != null
                                            && ListenerList.class.isAssignableFrom(attributeType)) {
                                            // The property represents a listener list
                                            /*
                                            ListenerList<Object> listenerList = (ListenerList<Object>)
                                                valueDictionary.get(attribute.localName);

                                            // The attribute value is a comma-separated list of listener IDs
                                            String[] listenerIDs = attribute.value.split(",");

                                            for (int i = 0, n = listenerIDs.length; i < n; i++) {
                                                String listenerID = listenerIDs[i].trim();

                                                if (listenerID.length() > 0) {
                                                    listenerID = listenerID.substring(1);

                                                    if (listenerID.length() > 0) {
                                                        listenerList.add(namedObjectDictionary.get(listenerID));
                                                    }
                                                }
                                            }
                                            */
                                        } else {
                                            String key = Character.toUpperCase(attribute.localName.charAt(0)) +
                                                attribute.localName.substring(1);
                                            String setterMethodName = BeanDictionary.SET_PREFIX + key;

                                            writer.write(String.format("%8so%d.%s(%s);\n", SPACE, element.ref,
                                                setterMethodName, resolve(attribute.value, attributeType)));
                                        }
                                    }
                                }
                            }

                            // If the parent element is a writable property, set this as its value; it
                            // will be applied later in the parent's closing tag
                            if (element.parent != null
                                && element.parent.type == Element.Type.WRITABLE_PROPERTY) {
                                element.parent.clazz = element.clazz;
                                element.parent.ref = element.ref;
                            }
                        }
                        }

                        // If this is the top of the stack, return this element's value;
                        // otherwise, move up the stack
                        if (element.parent == null) {
                            writer.write(String.format("%8sresult = o%d;\n", SPACE, element.ref));
                        } else {
                            element = element.parent;
                        }

                        break;
                    }
                    }
                }

                reader.close();
            } catch(XMLStreamException exception) {
                throw new IOException(exception);
            }

            // Close method declaration
            writer.write(String.format("%8sreturn (T)result;\n", SPACE));
            writer.write(String.format("%4s}\n", SPACE));

            // Close class declaration
            writer.write("}\n");
        } finally {
            writer.close();
        }

        return javaFile;
    }

    private String resolve(String attributeValue, Class<?> propertyType)
        throws IOException {
        String result = null;

        if (propertyType == Boolean.class
            || propertyType == Boolean.TYPE) {
            result = String.valueOf(Boolean.parseBoolean(attributeValue));
        } else if (propertyType == Character.class
            || propertyType == Character.TYPE) {
            if (attributeValue.length() > 0) {
                result = String.format("'%c'", attributeValue.charAt(0));
            }
        } else if (propertyType == Byte.class
            || propertyType == Byte.TYPE) {
            try {
                result = String.format("(byte)%d", Byte.parseByte(attributeValue));
            } catch(NumberFormatException exception) {
                result = String.format("\"%s\"", attributeValue);
            }
        } else if (propertyType == Short.class
            || propertyType == Short.TYPE) {
            try {
                result = String.format("(short)%d", Short.parseShort(attributeValue));
            } catch(NumberFormatException exception) {
                result = String.format("\"%s\"", attributeValue);
            }
        } else if (propertyType == Integer.class
            || propertyType == Integer.TYPE) {
            try {
                result = String.format("(int)%d", Integer.parseInt(attributeValue));
            } catch(NumberFormatException exception) {
                result = String.format("\"%s\"", attributeValue);
            }
        } else if (propertyType == Long.class
            || propertyType == Long.TYPE) {
            try {
                result = String.format("(long)%d", Long.parseLong(attributeValue));
            } catch(NumberFormatException exception) {
                result = String.format("\"%s\"", attributeValue);
            }
        } else if (propertyType == Float.class
            || propertyType == Float.TYPE) {
            try {
                result = String.format("(float)%f", Float.parseFloat(attributeValue));
            } catch(NumberFormatException exception) {
                result = String.format("\"%s\"", attributeValue);
            }
        } else if (propertyType == Double.class
            || propertyType == Double.TYPE) {
            try {
                result = String.format("(double)%f", Double.parseDouble(attributeValue));
            } catch(NumberFormatException exception) {
                result = String.format("\"%s\"", attributeValue);
            }
        } else {
            if (attributeValue.length() > 0) {
                if (attributeValue.charAt(0) == WTKXSerializer.URL_PREFIX) {
                    if (attributeValue.length() > 1) {
                        if (attributeValue.charAt(1) == WTKXSerializer.URL_PREFIX) {
                            result = String.format("\"%s\"", attributeValue.substring(1));
                        } else {
                            result = String.format("getClass().getResource(\"%s\")", attributeValue.substring(1));
                        }
                    }
                } else if (attributeValue.charAt(0) == WTKXSerializer.RESOURCE_KEY_PREFIX) {
                    if (attributeValue.length() > 1) {
                        if (attributeValue.charAt(1) == WTKXSerializer.RESOURCE_KEY_PREFIX) {
                            result = String.format("\"%s\"", attributeValue.substring(1));
                        } else {
                            // TODO
                            result = String.format("\"%s\"", attributeValue);
                        }
                    }
                } else if (attributeValue.charAt(0) == WTKXSerializer.OBJECT_REFERENCE_PREFIX) {
                    if (attributeValue.length() > 1) {
                        if (attributeValue.charAt(1) == WTKXSerializer.OBJECT_REFERENCE_PREFIX) {
                            result = String.format("\"%s\"", attributeValue.substring(1));
                        } else {
                            String className = propertyType == null ?
                                "Object" : propertyType.getName();
                            result = String.format("(%s)namedObjects.get(\"%s\")",
                                className, attributeValue.substring(1));
                        }
                    }
                } else {
                    result = String.format("\"%s\"", attributeValue);
                }
            } else {
                result = "\"\"";
            }
        }

        if (result == null) {
            // Fall-through case
            result = "null";
        }

        return result;
    }
}
