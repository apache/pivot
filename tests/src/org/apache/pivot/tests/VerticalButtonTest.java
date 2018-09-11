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

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TextPane;
import org.apache.pivot.wtk.content.ButtonData;
import org.apache.pivot.wtk.content.VerticalButtonDataRenderer;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.text.ComponentNode;
import org.apache.pivot.wtk.text.Document;
import org.apache.pivot.wtk.text.Paragraph;
import org.apache.pivot.wtk.text.TextNode;


public final class VerticalButtonTest implements Application {
    private Frame frame = null;

    private void browse() {
        try {
            Desktop.getDesktop().browse(new URI("https://pivot.apache.org"));
        } catch (URISyntaxException | IOException ex) {
            Alert.alert(ex.getMessage(), frame);
        }
    }

    @Override
    public void startup(final Display display, final Map<String, String> properties) throws Exception {
        frame = new Frame();
        frame.setTitle("Vertical Button Test");
        frame.setPreferredSize(480, 360);

        Image image = Image.load(getClass().getResource("go-home.png"));
        PushButton button1 = new PushButton(new ButtonData(image, "Home"));
        button1.setDataRenderer(new VerticalButtonDataRenderer());
        button1.getButtonPressListeners().add((button) -> browse());
        TextPane textPane = new TextPane();
        Document document = new Document();
        TextNode text1 = new TextNode("Link to the Apache Pivot site: ");
        ComponentNode compNode1 = new ComponentNode(button1);
        Paragraph para1 = new Paragraph();
        para1.add(text1);
        document.add(para1);
        document.add(compNode1);
        textPane.setDocument(document);

        frame.setContent(textPane);

        frame.open(display);
    }

    @Override
    public boolean shutdown(final boolean optional) {
        if (frame != null) {
            frame.close();
        }

        return false;
    }

    public static void main(final String[] args) {
        DesktopApplicationContext.main(VerticalButtonTest.class, args);
    }
}
