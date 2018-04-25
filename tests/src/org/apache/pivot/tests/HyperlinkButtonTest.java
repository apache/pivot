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
package org.apache.pivot.tests;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.HyperlinkButton;
import org.apache.pivot.wtk.TextPane;
import org.apache.pivot.wtk.text.ComponentNode;
import org.apache.pivot.wtk.text.Document;
import org.apache.pivot.wtk.text.ImageNode;
import org.apache.pivot.wtk.text.Paragraph;
import org.apache.pivot.wtk.text.TextNode;


public class HyperlinkButtonTest implements Application {
    private Frame frame = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        frame = new Frame();
        frame.setTitle("Hyperlink Button Test");
        frame.setPreferredSize(480, 360);

        HyperlinkButton button1 = new HyperlinkButton("http://pivot.apache.org");
        HyperlinkButton button2 = new HyperlinkButton("Apache website", "http://apache.org");
        TextPane textPane = new TextPane();
        Document document = new Document();
        TextNode text1 = new TextNode("Link to the Apache Pivot site: ");
        TextNode text2 = new TextNode("Main Apache Software Foundation website: ");
        ComponentNode compNode1 = new ComponentNode(button1);
        ComponentNode compNode2 = new ComponentNode(button2);
        Paragraph para1 = new Paragraph();
        para1.add(text1);
        document.add(para1);
        document.add(compNode1);
        Paragraph para2 = new Paragraph();
        para2.add(text2);
        document.add(para2);
        document.add(compNode2);
        ImageNode image1 = new ImageNode("/org/apache/pivot/tests/house.png");
        document.add(image1);
        textPane.setDocument(document);

        frame.setContent(textPane);

        frame.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (frame != null) {
            frame.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(HyperlinkButtonTest.class, args);
    }
}
