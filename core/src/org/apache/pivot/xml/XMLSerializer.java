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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.serialization.Serializer;
import org.apache.pivot.util.ListenerList;

/**
 * Reads and writes XML data.
 */
public class XMLSerializer implements Serializer<Element> {
    private static class XMLSerializerListenerList extends ListenerList<XMLSerializerListener>
        implements XMLSerializerListener {
        @Override
        public void beginElement(XMLSerializer xmlSerializer, Element element) {
            for (XMLSerializerListener listener : this) {
                listener.beginElement(xmlSerializer, element);
            }
        }

        @Override
        public void endElement(XMLSerializer xmlSerializer) {
            for (XMLSerializerListener listener : this) {
                listener.endElement(xmlSerializer);
            }
        }

        @Override
        public void readTextNode(XMLSerializer xmlSerializer, TextNode textNode) {
            for (XMLSerializerListener listener : this) {
                listener.readTextNode(xmlSerializer, textNode);
            }
        }
    }

    private Charset charset = null;

    private XMLSerializerListenerList xmlSerializerListeners = null;

    public static final String XMLNS_ATTRIBUTE_PREFIX = "xmlns";

    public static final String DEFAULT_CHARSET_NAME = "UTF-8";
    public static final String XML_EXTENSION = "xml";
    public static final String MIME_TYPE = "text/xml";
    public static final int BUFFER_SIZE = 2048;

    public XMLSerializer() {
        this(Charset.forName(DEFAULT_CHARSET_NAME));
    }

    public XMLSerializer(Charset charset) {
        if (charset == null) {
            throw new IllegalArgumentException("charset is null.");
        }

        this.charset = charset;
    }

    public Charset getCharset() {
        return charset;
    }

    @Override
    public Element readObject(InputStream inputStream)
        throws IOException, SerializationException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is null.");
        }

        Reader reader = new BufferedReader(new InputStreamReader(inputStream, charset), BUFFER_SIZE);
        Element element = readObject(reader);

        return element;
    }

    public Element readObject(Reader reader) throws SerializationException {
        if (reader == null) {
            throw new IllegalArgumentException("reader is null.");
        }

        // Parse the XML stream
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty("javax.xml.stream.isCoalescing", true);

        Element document = null;

        try {
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(reader);

            Element current = null;

            while (xmlStreamReader.hasNext()) {
                int event = xmlStreamReader.next();

                switch (event) {
                    case XMLStreamConstants.CHARACTERS: {
                        if (!xmlStreamReader.isWhiteSpace()) {
                            TextNode textNode = new TextNode(xmlStreamReader.getText());

                            // Notify listeners
                            if (xmlSerializerListeners != null) {
                                xmlSerializerListeners.readTextNode(this, textNode);
                            }

                            if (current != null) {
                                current.add(textNode);
                            }
                        }

                        break;
                    }

                    case XMLStreamConstants.START_ELEMENT: {
                        // Create the element
                        String prefix = xmlStreamReader.getPrefix();
                        if (prefix != null
                            && prefix.length() == 0) {
                            prefix = null;
                        }

                        String localName = xmlStreamReader.getLocalName();

                        Element element = new Element(prefix, localName);

                        // Get the element's namespaces
                        for (int i = 0, n = xmlStreamReader.getNamespaceCount(); i < n; i++) {
                            String namespacePrefix = xmlStreamReader.getNamespacePrefix(i);
                            String namespaceURI = xmlStreamReader.getNamespaceURI(i);

                            if (namespacePrefix == null) {
                                element.setDefaultNamespaceURI(namespaceURI);
                            } else {
                                element.getNamespaces().put(namespacePrefix, namespaceURI);
                            }
                        }

                        // Get the element's attributes
                        for (int i = 0, n = xmlStreamReader.getAttributeCount(); i < n; i++) {
                            String attributePrefix = xmlStreamReader.getAttributePrefix(i);
                            if (attributePrefix != null
                                && attributePrefix.length() == 0) {
                                attributePrefix = null;
                            }

                            String attributeLocalName = xmlStreamReader.getAttributeLocalName(i);
                            String attributeValue = xmlStreamReader.getAttributeValue(i);

                            element.getAttributes().add(new Element.Attribute(attributePrefix,
                                attributeLocalName, attributeValue));
                        }

                        if (current == null) {
                            document = element;
                        } else {
                            current.add(element);
                        }

                        // Notify listeners
                        if (xmlSerializerListeners != null) {
                            xmlSerializerListeners.beginElement(this, element);
                        }

                        current = element;

                        break;
                    }

                    case XMLStreamConstants.END_ELEMENT: {
                        // Notify listeners
                        if (xmlSerializerListeners != null) {
                            xmlSerializerListeners.endElement(this);
                        }

                        // Move up the stack
                        if (current != null) {
                            current = current.getParent();
                        }

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

        return document;
    }

    @Override
    public void writeObject(Element element, OutputStream outputStream)
        throws IOException, SerializationException {
        if (outputStream == null) {
            throw new IllegalArgumentException("outputStream is null.");
        }

        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset),
            BUFFER_SIZE);
        writeObject(element, writer);
        writer.flush();
    }

    public void writeObject(Element element, Writer writer) throws SerializationException {
        if (writer == null) {
            throw new IllegalArgumentException("writer is null.");
        }

        if (element == null) {
            throw new IllegalArgumentException("element is null.");
        }

        XMLOutputFactory output = XMLOutputFactory.newInstance();

        try {
            XMLStreamWriter xmlStreamWriter = output.createXMLStreamWriter(writer);
            xmlStreamWriter.writeStartDocument();
            writeElement(element, xmlStreamWriter);
            xmlStreamWriter.writeEndDocument();
        } catch (XMLStreamException exception) {
            throw new SerializationException(exception);
        }
    }

    private void writeElement(Element element, XMLStreamWriter xmlStreamWriter)
        throws XMLStreamException, SerializationException {
        String namespacePrefix = element.getNamespacePrefix();
        String localName = element.getLocalName();

        if (namespacePrefix == null) {
            if (element.getLength() == 0) {
                xmlStreamWriter.writeEmptyElement(localName);
            } else {
                xmlStreamWriter.writeStartElement(localName);
            }
        } else {
            String namespaceURI = element.getNamespaceURI(namespacePrefix);

            if (element.getLength() == 0) {
                xmlStreamWriter.writeEmptyElement(namespacePrefix, localName, namespaceURI);
            } else {
                xmlStreamWriter.writeStartElement(namespacePrefix, localName, namespaceURI);
            }
        }

        // Write out the declared namespaces
        String defaultNamespaceURI = element.getDefaultNamespaceURI();
        if (defaultNamespaceURI != null) {
            xmlStreamWriter.writeDefaultNamespace(defaultNamespaceURI);
        }

        Element.NamespaceDictionary namespaces = element.getNamespaces();
        for (String declaredNamespacePrefix : namespaces) {
            String declaredNamespaceURI = namespaces.get(declaredNamespacePrefix);
            xmlStreamWriter.writeNamespace(declaredNamespacePrefix, declaredNamespaceURI);
        }

        // Write out the attributes
        for (Element.Attribute attribute : element.getAttributes()) {
            String attributeNamespacePrefix = attribute.getNamespacePrefix();
            String attributeLocalName = attribute.getLocalName();
            String attributeValue = attribute.getValue();

            if (attributeNamespacePrefix == null) {
                xmlStreamWriter.writeAttribute(attributeLocalName, attributeValue);
            } else {
                String attributeNamespaceURI = element.getNamespaceURI(attributeNamespacePrefix);

                xmlStreamWriter.writeAttribute(attributeNamespacePrefix, attributeNamespaceURI,
                    attributeLocalName, attributeValue);
            }
        }

        // Write out the child nodes
        for (Node node : element) {
            if (node instanceof Element) {
                writeElement((Element)node, xmlStreamWriter);
            } else if (node instanceof TextNode) {
                writeTextNode((TextNode)node, xmlStreamWriter);
            } else {
                throw new SerializationException("Unsupported node type: "
                    + node.getClass().getName());
            }
        }

        if (element.getLength() > 0) {
            xmlStreamWriter.writeEndElement();
        }
    }

    private static void writeTextNode(TextNode textNode, XMLStreamWriter xmlStreamWriter)
        throws XMLStreamException {
        xmlStreamWriter.writeCharacters(textNode.getText());
    }

    @Override
    public String getMIMEType(Element object) {
        return MIME_TYPE;
    }

    public ListenerList<XMLSerializerListener> getXMLSerializerListeners() {
        if (xmlSerializerListeners == null) {
            xmlSerializerListeners = new XMLSerializerListenerList();
        }

        return xmlSerializerListeners;
    }
}
