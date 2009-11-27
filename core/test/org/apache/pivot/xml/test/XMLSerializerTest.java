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

import java.io.IOException;

import org.apache.pivot.collections.List;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.xml.Element;
import org.apache.pivot.xml.XMLSerializer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class XMLSerializerTest {
    @Test
    public void basicTest() throws IOException, SerializationException {
        XMLSerializer xmlSerializer = new XMLSerializer();

        Element root = xmlSerializer.readObject(getClass().getResourceAsStream("sample.xml"));

        assertEquals(root.getName(), "root");

        Element a = XMLSerializer.getElement(root, "a");
        assertEquals(a.getName(), "a");
        assertEquals(a.get("id"), "x");

        Element b = XMLSerializer.getElement(root, "a/b");
        assertEquals(b.getName(), "b");
        assertEquals(b.get("id"), "y");

        b = XMLSerializer.getElement(a, "b");
        assertEquals(b.getName(), "b");
        assertEquals(b.get("id"), "y");

        List<Element> cs = XMLSerializer.getElements(root, "a/b/c");
        assertEquals(cs.getLength(), 1);

        List<Element> fs = XMLSerializer.getElements(root, "d/e/f");
        assertEquals(fs.getLength(), 4);

        Element e = XMLSerializer.getElement(root, "d/e");
        Element f = XMLSerializer.getElement(e, "f");
        assertEquals(f.getName(), "f");

        Element g = XMLSerializer.getElement(e, "g");
        assertEquals(g.getName(), "g");

        String ft = XMLSerializer.getText(root, "d/e/f");
        assertEquals(ft, "1");

        String gt = XMLSerializer.getText(root, "d/e/g");
        assertEquals(gt, "4");

        assertNull(XMLSerializer.getElement(root, "a/b/n"));
        assertNull(XMLSerializer.getText(root, "a/b/n"));

        assertEquals(XMLSerializer.getElements(root, "a/b/n").getLength(), 0);

        assertEquals(XMLSerializer.getText(root, "d/foo:h"), "Hello");
    }
}
