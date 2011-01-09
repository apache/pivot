/*
 * Contains code originally developed for Apache Pivot under the Apache
 * License, Version 2.0:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.apache.pivot.xml.test;

import java.io.IOException;
import java.util.List;


import org.apache.pivot.io.SerializationException;
import org.apache.pivot.xml.Element;
import org.apache.pivot.xml.TextNode;
import org.apache.pivot.xml.XMLSerializer;
import org.apache.pivot.xml.XMLSerializerListener;
import org.junit.Test;

import static org.junit.Assert.*;

public class XMLSerializerTest {
    @Test
    public void basicTest() throws IOException, SerializationException {
        XMLSerializer xmlSerializer = new XMLSerializer();

        Element root = xmlSerializer.readObject(getClass().getResourceAsStream("sample.xml"));

        assertEquals(root.getName(), "root");

        Element a = root.getElement("a");
        assertEquals(a.getName(), "a");
        assertEquals(a.getAttributeValue("id"), "x");

        Element b = root.getElement("a/b");
        assertEquals(b.getName(), "b");
        assertEquals(b.getAttributeValue("id"), "y");

        b = a.getElement("b");
        assertEquals(b.getName(), "b");
        assertEquals(b.getAttributeValue("id"), "y");

        List<Element> cs = root.getElements("a/b", "c");
        assertEquals(cs.size(), 1);

        List<Element> fs = root.getElements("d/e", "f");
        assertEquals(fs.size(), 4);

        Element e = root.getElement("d/e");
        Element f = e.getElement("f");
        assertEquals(f.getName(), "f");

        Element g = e.getElement("g");
        assertEquals(g.getName(), "g");

        String ft = root.getText("d/e/f");
        assertEquals(ft, "1");

        String gt = root.getText("d/e/g");
        assertEquals(gt, "4");

        assertNull(root.getElement("a/b/n"));
        assertNull(root.getText("a/b/n"));

        assertEquals(root.getElements("a/b", "n").size(), 0);

        assertEquals(root.getText("d/foo:h"), "Hello");

        List<Element> is = e.getElements("is", "i");
        assertEquals(is.size(), 3);

        assertEquals(root.getText("d[0]/e[0]/f[2]"), "3");
        assertEquals(root.getText("d[0]/e[1]"), null);
    }

    @Test
    public void equalsTest() throws IOException, SerializationException {
        XMLSerializer xmlSerializer = new XMLSerializer();
        XMLSerializerListener xmlSerializerListener = new XMLSerializerListener() {
            @Override
            public void beginElement(XMLSerializer xmlSerializer, Element element) {
                System.out.println("Begin element: " + element);
            }

            @Override
            public void endElement(XMLSerializer xmlSerializer) {
                System.out.println("End element");
            }

            @Override
            public void readTextNode(XMLSerializer xmlSerializer, TextNode textNode) {
                System.out.println("Read text node: " + textNode);
            }
        };

        xmlSerializer.getXMLSerializerListeners().add(xmlSerializerListener);
        Element root1 = xmlSerializer.readObject(getClass().getResourceAsStream("sample.xml"));

        xmlSerializer.getXMLSerializerListeners().remove(xmlSerializerListener);
        Element root2 = xmlSerializer.readObject(getClass().getResourceAsStream("sample.xml"));

        assertTrue(root1.equals(root2));

        Element a = root2.getElement("a");
        a.getAttributes().remove(0);

        assertFalse(root1.equals(root2));
    }
}
