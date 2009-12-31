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
package org.apache.pivot.tests.text;

import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TextAreaCharacterListener;
import org.apache.pivot.wtk.TextAreaSelectionListener;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.TreeViewSelectionListener;
import org.apache.pivot.wtk.text.Element;
import org.apache.pivot.wtk.text.Node;
import org.apache.pivot.wtk.text.NodeListener;
import org.apache.pivot.wtkx.WTKXSerializer;

public class TextAreaTest implements Application {
    private Frame frame = null;

    private TextArea textArea = null;
    private Label selectionStartLabel = null;
    private Label selectionLengthLabel = null;

    private TreeView treeView = null;
    private Label offsetLabel = null;
    private Label charactersLabel = null;

    private DocumentAdapter documentAdapter = null;
    private Node selectedNode = null;

    private NodeListener selectedNodeListener = new NodeListener() {
        @Override
        public void parentChanged(Node node, Element previousParent) {
            // No-op
        }

        @Override
        public void offsetChanged(Node node, int previousOffset) {
            updateSelectedNodeData();
        }

        @Override
        public void rangeInserted(Node node, int offset, int span) {
            updateSelectedNodeData();
        }

        @Override
        public void rangeRemoved(Node node, int offset, int span) {
            updateSelectedNodeData();
        }
    };

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        frame = new Frame((Component)wtkxSerializer.readObject(getClass().getResource("text_area_test.wtkx")));
        frame.setTitle("TextArea Test");
        frame.setPreferredSize(640, 480);
        frame.setLocation(160, 120);
        frame.open(display);

        textArea = (TextArea)wtkxSerializer.get("textArea");
        textArea.getTextAreaSelectionListeners().add(new TextAreaSelectionListener() {
            @Override
            public void selectionChanged(TextArea textArea,
                int previousSelectionStart, int previousSelectionLength) {
                updateSelection();
            }
        });

        textArea.getTextAreaCharacterListeners().add(new TextAreaCharacterListener() {
            @Override
            public void charactersRemoved(TextArea textArea, int index, int count) {
                updateSelection();
            }

            @Override
            public void charactersInserted(TextArea textArea, int index, int count) {
                updateSelection();
            }
        });

        selectionStartLabel = (Label)wtkxSerializer.get("selectionStartLabel");
        selectionLengthLabel = (Label)wtkxSerializer.get("selectionLengthLabel");

        treeView = (TreeView)wtkxSerializer.get("treeView");
        treeView.getTreeViewSelectionListeners().add(new TreeViewSelectionListener() {
            @Override
            public void selectedPathAdded(TreeView treeView, Path path) {
                // No-op
            }

            @Override
            public void selectedPathRemoved(TreeView treeView, Path path) {
                // No-op
            }

            @Override
            public void selectedPathsChanged(TreeView treeView,
                Sequence<Path> previousSelectedPaths) {
                Path selectedPath = treeView.getSelectedPath();

                if (selectedNode != null) {
                    selectedNode.getNodeListeners().remove(selectedNodeListener);
                }

                if (selectedPath == null) {
                    selectedNode = null;
                } else {
                    NodeAdapter nodeAdapter = Sequence.Tree.get(documentAdapter, selectedPath);
                    selectedNode = nodeAdapter.getNode();
                }

                if (selectedNode != null) {
                    selectedNode.getNodeListeners().add(selectedNodeListener);
                }

                updateSelectedNodeData();
            }
        });

        offsetLabel = (Label)wtkxSerializer.get("offsetLabel");
        charactersLabel = (Label)wtkxSerializer.get("charactersLabel");

        documentAdapter = new DocumentAdapter(textArea.getDocument());
        treeView.setTreeData(documentAdapter);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (frame != null) {
            frame.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    private void updateSelection() {
        selectionStartLabel.setText(Integer.toString(textArea.getSelectionStart()));
        selectionLengthLabel.setText(Integer.toString(textArea.getSelectionLength()));
    }

    private void updateSelectedNodeData() {
        if (selectedNode == null) {
            offsetLabel.setText(null);
            charactersLabel.setText(null);
        } else {
            offsetLabel.setText(Integer.toString(selectedNode.getDocumentOffset()));
            charactersLabel.setText(Integer.toString(selectedNode.getCharacterCount()));
        }
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(TextAreaTest.class, args);
    }
}
