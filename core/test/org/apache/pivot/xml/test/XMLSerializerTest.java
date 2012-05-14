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
package org.apache.pivot.xml.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.apache.pivot.collections.List;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.xml.Element;
import org.apache.pivot.xml.TextNode;
import org.apache.pivot.xml.XML;
import org.apache.pivot.xml.XMLSerializer;
import org.apache.pivot.xml.XMLSerializerListener;
import org.junit.Test;

public class XMLSerializerTest {
    @Test
    public void basicTest() throws IOException, SerializationException {
        XMLSerializer xmlSerializer = new XMLSerializer();

        Element root = xmlSerializer.readObject(getClass().getResourceAsStream("sample.xml"));

        assertEquals(root.getName(), "root");

        Element a = XML.getElement(root, "a");
        assertEquals(a.getName(), "a");
        assertEquals(a.getElementDictionary().get("id"), "x");

        Element b = XML.getElement(root, "a/b");
        assertEquals(b.getName(), "b");
        assertEquals(b.getElementDictionary().get("id"), "y");

        b = XML.getElement(a, "b");
        assertEquals(b.getName(), "b");
        assertEquals(b.getElementDictionary().get("id"), "y");

        List<Element> cs = XML.getElements(root, "a/b", "c");
        assertEquals(cs.getLength(), 1);

        List<Element> fs = XML.getElements(root, "d/e", "f");
        assertEquals(fs.getLength(), 4);

        Element e = XML.getElement(root, "d/e");
        Element f = XML.getElement(e, "f");
        assertEquals(f.getName(), "f");

        Element g = XML.getElement(e, "g");
        assertEquals(g.getName(), "g");

        String ft = XML.getText(root, "d/e/f");
        assertEquals(ft, "1");

        String gt = XML.getText(root, "d/e/g");
        assertEquals(gt, "4");

        assertNull(XML.getElement(root, "a/b/n"));
        assertNull(XML.getText(root, "a/b/n"));

        assertEquals(XML.getElements(root, "a/b", "n").getLength(), 0);

        assertEquals(XML.getText(root, "d/foo:h"), "Hello");

        List<Element> is = XML.getElements(e, "is", "i");
        assertEquals(is.getLength(), 3);

        assertEquals(XML.getText(root, "d[0]/e[0]/f[2]"), "3");
        assertEquals(XML.getText(root, "d[0]/e[1]"), null);
    }

    @Test
    public void equalsTest() throws IOException, SerializationException {
        XMLSerializer xmlSerializer = new XMLSerializer();
        XMLSerializerListener xmlSerializerListener = new XMLSerializerListener() {
            @Override
            public void beginElement(XMLSerializer xmlSerializerArgument, Element element) {
                System.out.println("Begin element: " + element);
            }

            @Override
            public void endElement(XMLSerializer xmlSerializerArgument) {
                System.out.println("End element");
            }

            @Override
            public void readTextNode(XMLSerializer xmlSerializerArgument, TextNode textNode) {
                System.out.println("Read text node: " + textNode);
            }
        };

        xmlSerializer.getXMLSerializerListeners().add(xmlSerializerListener);
        Element root1 = xmlSerializer.readObject(getClass().getResourceAsStream("sample.xml"));
        assertNotNull(root1);

        xmlSerializer.getXMLSerializerListeners().remove(xmlSerializerListener);
        Element root2 = xmlSerializer.readObject(getClass().getResourceAsStream("sample.xml"));
        assertNotNull(root2);
    }
}
