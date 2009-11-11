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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.serialization.Serializer;

/**
 * Reads and writes XML data.
 */
public class XMLSerializer implements Serializer<Element> {
    public static final String MIME_TYPE = "text/xml";

    private XMLInputFactory xmlInputFactory;

    private Element element = null;

    public static final String XMLNS_ATTRIBUTE_PREFIX = "xmlns";

    public XMLSerializer() {
        this(true);
    }

    public XMLSerializer(boolean coalesceText) {
        xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty("javax.xml.stream.isCoalescing", coalesceText);
    }

    @Override
    public Element readObject(InputStream inputStream) throws IOException, SerializationException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is null.");
        }

        // Parse the XML stream
        element = null;

        try {
            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(inputStream);

            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.CHARACTERS: {
                        element.add(new TextNode(reader.getText()));
                        break;
                    }

                    case XMLStreamConstants.START_ELEMENT: {
                        // Create the element
                        String prefix = reader.getPrefix();
                        String localName = reader.getLocalName();

                        Element element = new Element(prefix, localName);

                        for (int i = 0, n = reader.getAttributeCount(); i < n; i++) {
                            String attributePrefix = reader.getAttributePrefix(i);
                            String attributeLocalName = reader.getAttributeLocalName(i);
                            String attributeValue = reader.getAttributeValue(i);

                            if (attributePrefix != null) {
                                if (!attributePrefix.equals(XMLNS_ATTRIBUTE_PREFIX)) {
                                    throw new SerializationException("Attribute namespaces are not supported.");
                                }

                                element.getNamespaces().put(attributeLocalName, attributeValue);
                            } else {
                                element.put(attributeLocalName, attributeValue);
                            }
                        }

                        if (this.element == null) {
                            this.element = element;
                        } else {
                            this.element.add(element);
                        }

                        break;
                    }

                    case XMLStreamConstants.END_ELEMENT: {
                        // Move up the stack
                        Element parent = element.getParent();
                        if (parent != null) {
                            element = parent;
                        }

                        break;
                    }
                }
            }

            reader.close();
        } catch (XMLStreamException exception) {
            throw new SerializationException(exception);
        }

        return element;
    }

    @Override
    public void writeObject(Element object, OutputStream outputStream)
        throws IOException, SerializationException {
        // TODO (note that we'll need to check for valid namespace prefixes here,
        // since we don't do it in Element)
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMIMEType(Element object) {
        return MIME_TYPE;
    }
}
