/*
 * Copyright (c) 2009 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk.text.test;

import pivot.collections.Dictionary;
import pivot.collections.Sequence;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.Frame;
import pivot.wtk.Label;
import pivot.wtk.TextArea;
import pivot.wtk.TextAreaSelectionListener;
import pivot.wtk.TreeView;
import pivot.wtk.TreeViewSelectionListener;
import pivot.wtk.text.Element;
import pivot.wtk.text.Node;
import pivot.wtk.text.NodeListener;
import pivot.wtkx.WTKXSerializer;

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
        public void parentChanged(Node node, Element previousParent) {
            // No-op
        }

        public void offsetChanged(Node node, int previousOffset) {
            updateSelectedNodeData();
        }

        public void rangeInserted(Node node, int offset, int span) {
            updateSelectedNodeData();
        }

        public void rangeRemoved(Node node, int offset, int span) {
            updateSelectedNodeData();
        }
    };

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        frame = new Frame((Component)wtkxSerializer.readObject(getClass().getResource("text_area_test.wtkx")));
        frame.setTitle("TextArea Test");
        frame.setPreferredSize(640, 480);
        frame.open(display);

        textArea = (TextArea)wtkxSerializer.getObjectByName("textArea");
        textArea.getTextAreaSelectionListeners().add(new TextAreaSelectionListener() {
            public void selectionChanged(TextArea textArea,
                int previousSelectionStart, int previousSelectionLength) {
                selectionStartLabel.setText(Integer.toString(textArea.getSelectionStart()));
                selectionLengthLabel.setText(Integer.toString(textArea.getSelectionLength()));
            }
        });

        selectionStartLabel = (Label)wtkxSerializer.getObjectByName("selectionStartLabel");
        selectionLengthLabel = (Label)wtkxSerializer.getObjectByName("selectionLengthLabel");

        treeView = (TreeView)wtkxSerializer.getObjectByName("treeView");
        treeView.getTreeViewSelectionListeners().add(new TreeViewSelectionListener() {
            public void selectionChanged(TreeView treeView) {
                Sequence<Integer> selectedPath = treeView.getSelectedPath();

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

        offsetLabel = (Label)wtkxSerializer.getObjectByName("offsetLabel");
        charactersLabel = (Label)wtkxSerializer.getObjectByName("charactersLabel");

        documentAdapter = new DocumentAdapter(textArea.getDocument());
        treeView.setTreeData(documentAdapter);
    }

    public boolean shutdown(boolean optional) {
        frame.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
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
}
