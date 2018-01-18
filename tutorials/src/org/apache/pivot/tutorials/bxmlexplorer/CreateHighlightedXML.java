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
package org.apache.pivot.tutorials.bxmlexplorer;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.TextPane;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.text.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Create a nicely syntax highlighted version of the BXML document.
 */
public class CreateHighlightedXML {

    private static final Color ELEMENT_COLOR = new Color(63, 127, 127);
    private static final Color ATTR_NAME_COLOR = new Color(127, 0, 127);
    private static final Color ATTR_VALUE_COLOR = new Color(42, 0, 255);
    private static final Color ATTR_EQUALS_COLOR = new Color(0, 0, 0);

    private final org.apache.pivot.wtk.text.Document textPaneDocument = new org.apache.pivot.wtk.text.Document();
    private org.apache.pivot.wtk.text.Paragraph currentParagraph = new org.apache.pivot.wtk.text.Paragraph();

    public org.apache.pivot.wtk.text.Document prettyPrint(InputStream reader)
        throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        org.w3c.dom.Document doc = documentBuilder.parse(reader);

        return prettyPrint(doc);
    }

    private org.apache.pivot.wtk.text.Document prettyPrint(final org.w3c.dom.Document domDocument) {
        org.w3c.dom.Element domElement = domDocument.getDocumentElement();
        prettyPrint(domElement, 0);
        textPaneDocument.add(currentParagraph);
        return textPaneDocument;
    }

    private void prettyPrint(org.w3c.dom.Element domElement, int indent) {
        newLine();
        int indentMutable = indent + 1;
        add(createIndent(indentMutable));

        add(ELEMENT_COLOR, "<" + domElement.getNodeName());

        final NamedNodeMap attributes = domElement.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attributes.item(i);
                String attributeName = "";
                String prefix2 = attr.getPrefix();
                if (prefix2 != null && prefix2.length() > 0) {
                    attributeName += prefix2 + ":";
                }
                attributeName += attr.getNodeName();
                add(ATTR_NAME_COLOR, " " + attributeName);
                add(ATTR_EQUALS_COLOR, "=");
                add(ATTR_VALUE_COLOR, "\"" + attr.getValue() + "\"");
            }
        }

        if (!domElement.hasChildNodes()) {
            add(ELEMENT_COLOR, "/>");
        } else {
            add(ELEMENT_COLOR, ">");
            final NodeList childNodes = domElement.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                org.w3c.dom.Node node = childNodes.item(i);
                if (node instanceof org.w3c.dom.Element) {
                    prettyPrint((org.w3c.dom.Element) node, indentMutable);
                }
            }

            newLine();
            add(createIndent(indentMutable));
            add(ELEMENT_COLOR, "</" + domElement.getNodeName() + ">");
        }

        indentMutable--;
    }

    private void newLine() {
        textPaneDocument.add(currentParagraph);
        currentParagraph = new org.apache.pivot.wtk.text.Paragraph();
    }

    private void add(Color color, String buf) {
        final org.apache.pivot.wtk.text.TextSpan textSpan = new org.apache.pivot.wtk.text.TextSpan();
        textSpan.setForegroundColor(color);
        textSpan.add(buf);
        currentParagraph.add(textSpan);
    }

    private void add(String buf) {
        final org.apache.pivot.wtk.text.TextSpan textSpan = new org.apache.pivot.wtk.text.TextSpan();
        textSpan.add(buf);
        currentParagraph.add(textSpan);
    }

    private static String createIndent(int indent) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            buf.append("  ");
        }
        return buf.toString();
    }

    public static final class TestApplication implements Application {
        private Window window = null;

        @Override
        public void startup(Display display, Map<String, String> properties) throws Exception {

            CreateHighlightedXML xml = new CreateHighlightedXML();
            Document doc = xml.prettyPrint(CreateHighlightedXML.class.getResourceAsStream("builder-test1.bxml"));

            BXMLSerializer bxmlSerializer = new BXMLSerializer();
            window = (Window) bxmlSerializer.readObject(BXMLExplorer.class,
                "CreateHighlightedXml.bxml", true);

            TextPane textPane = (TextPane) bxmlSerializer.getNamespace().get("textPane");
            textPane.setDocument(doc);

            window.open(display);
        }

        @Override
        public boolean shutdown(boolean optional) {
            if (window != null) {
                window.close();
            }

            return false;
        }

    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(TestApplication.class, args);
    }

}
