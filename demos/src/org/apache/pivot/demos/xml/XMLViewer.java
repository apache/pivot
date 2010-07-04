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
package org.apache.pivot.demos.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.io.FileList;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.CardPane;
import org.apache.pivot.wtk.Clipboard;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.DropAction;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Manifest;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.effects.OverlayDecorator;
import org.apache.pivot.xml.Element;
import org.apache.pivot.xml.Node;
import org.apache.pivot.xml.TextNode;
import org.apache.pivot.xml.XMLSerializer;

/**
 * Utility application that allows the user to browse an XML DOM using
 * a tree view component.
 */
public class XMLViewer implements Application {
    private Window window = null;

    @BXML private TreeView treeView = null;
    @BXML private CardPane propertiesCardPane = null;
    @BXML private TableView namespacesTableView = null;
    @BXML private TableView attributesTableView = null;
    @BXML private TextArea textArea = null;

    private OverlayDecorator promptDecorator = new OverlayDecorator();

    public static final String APPLICATION_KEY = "application";
    public static final String WINDOW_TITLE = "XML Viewer";

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        bxmlSerializer.getNamespace().put(APPLICATION_KEY, this);

        window = (Window)bxmlSerializer.readObject(this, "xml_viewer.bxml");
        bxmlSerializer.bind(this);

        Label prompt = new Label("Drag or paste XML here");
        prompt.getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
        prompt.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        promptDecorator.setOverlay(prompt);
        treeView.getDecorators().add(promptDecorator);

        window.setTitle(WINDOW_TITLE);
        window.open(display);
        window.requestFocus();
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public void paste() {
        Manifest clipboardContent = Clipboard.getContent();

        if (clipboardContent != null
            && clipboardContent.containsText()) {
            String xml = null;
            XMLSerializer xmlSerializer = new XMLSerializer();
            try {
                xml = clipboardContent.getText();
                setDocument(xmlSerializer.readObject(new StringReader(xml)));
            } catch (Exception exception) {
                Prompt.prompt(exception.getMessage(), window);
            }

            window.setTitle(WINDOW_TITLE);
        }
    }

    public DropAction drop(Manifest dragContent) {
        DropAction dropAction = null;

        try {
            FileList fileList = dragContent.getFileList();
            if (fileList.getLength() == 1) {
                File file = fileList.get(0);

                XMLSerializer xmlSerializer = new XMLSerializer();
                FileInputStream fileInputStream = null;
                try {
                    try {
                        fileInputStream = new FileInputStream(file);
                        setDocument(xmlSerializer.readObject(fileInputStream));
                    } finally {
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                    }
                } catch (Exception exception) {
                    Prompt.prompt(exception.getMessage(), window);
                }

                window.setTitle(WINDOW_TITLE + " - " + file.getName());

                dropAction = DropAction.COPY;
            } else {
                Prompt.prompt("Multiple files not supported.", window);
            }
        } catch(IOException exception) {
            Prompt.prompt(exception.getMessage(), window);
        }

        return dropAction;
    }

    public void updateProperties() {
        Node node = (Node)treeView.getSelectedNode();

        if (node instanceof TextNode) {
            TextNode textNode = (TextNode)node;
            textArea.setText(textNode.getText());
            propertiesCardPane.setSelectedIndex(1);
        } else if (node instanceof Element) {
            Element element = (Element)node;

            // Populate the namespaces table
            ArrayList<HashMap<String, String>> namespacesTableData =
                new ArrayList<HashMap<String, String>>();

            String defaultNamespaceURI = element.getDefaultNamespaceURI();
            if (defaultNamespaceURI != null) {
                HashMap<String, String> row = new HashMap<String, String>();
                row.put("prefix", "(default)");
                row.put("uri", defaultNamespaceURI);
                namespacesTableData.add(row);
            }

            Element.NamespaceDictionary namespaceDictionary = element.getNamespaces();
            for (String prefix : namespaceDictionary) {
                HashMap<String, String> row = new HashMap<String, String>();
                row.put("prefix", prefix);
                row.put("uri", namespaceDictionary.get(prefix));
                namespacesTableData.add(row);
            }

            namespacesTableView.setTableData(namespacesTableData);

            // Populate the attributes table
            ArrayList<HashMap<String, String>> attributesTableData =
                new ArrayList<HashMap<String, String>>();

            for (Element.Attribute attribute : element.getAttributes()) {
                HashMap<String, String> row = new HashMap<String, String>();

                String attributeName = attribute.getName();
                row.put("name", attributeName);
                row.put("value", element.get(attributeName));
                attributesTableData.add(row);
            }

            attributesTableView.setTableData(attributesTableData);

            propertiesCardPane.setSelectedIndex(0);
        } else {
            throw new IllegalStateException();
        }
    }

    private void setDocument(Element document) {
        // Remove prompt decorator
        if (promptDecorator != null) {
            treeView.getDecorators().remove(promptDecorator);
            promptDecorator = null;
        }

        ArrayList<Element> treeData = new ArrayList<Element>();
        treeData.add(document);
        treeView.setTreeData(treeData);

        Sequence.Tree.Path path = new Sequence.Tree.Path(0);
        treeView.expandBranch(path);
        treeView.setSelectedPath(path);
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(XMLViewer.class, args);
    }
}
