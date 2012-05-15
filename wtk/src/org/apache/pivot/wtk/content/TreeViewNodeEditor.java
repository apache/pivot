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
package org.apache.pivot.wtk.content;

import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.Window;

/**
 * Default tree view node editor.
 */
public class TreeViewNodeEditor extends Window implements TreeView.NodeEditor {
    private TreeView treeView = null;
    private Path path = null;

    private TextInput textInput = new TextInput();

    private ContainerMouseListener displayMouseHandler = new ContainerMouseListener.Adapter() {
        @Override
        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            Display display = (Display)container;
            Window window = (Window)display.getComponentAt(x, y);

            if (window != TreeViewNodeEditor.this) {
                endEdit(true);
            }

            return false;
        }

        @Override
        public boolean mouseWheel(Container container, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            Display display = (Display)container;
            Window window = (Window)display.getComponentAt(x, y);

            return (window != TreeViewNodeEditor.this);
        }
    };

    public TreeViewNodeEditor() {
        setContent(textInput);
    }

    public TreeView getTreeView() {
        return treeView;
    }

    public Path getPath() {
        return path;
    }

    public TextInput getTextInput() {
        return textInput;
    }

    @Override
    public void beginEdit(TreeView treeViewArgument, Path pathArgument) {
        this.treeView = treeViewArgument;
        this.path = pathArgument;

        // Get the data being edited
        List<?> treeData = treeViewArgument.getTreeData();
        TreeNode treeNode = (TreeNode)Sequence.Tree.get(treeData, pathArgument);

        textInput.setText(treeNode.getText());
        textInput.selectAll();

        // Get the node bounds
        Bounds nodeBounds = treeViewArgument.getNodeBounds(pathArgument);
        int nodeIndent = treeViewArgument.getNodeIndent(pathArgument.getLength());
        nodeBounds = new Bounds(nodeBounds.x + nodeIndent, nodeBounds.y,
            nodeBounds.width - nodeIndent, nodeBounds.height);

        // Render the node data
        TreeViewNodeRenderer nodeRenderer = (TreeViewNodeRenderer)treeViewArgument.getNodeRenderer();
        nodeRenderer.render(treeNode, pathArgument, treeViewArgument.getRowIndex(pathArgument), treeViewArgument, false, false,
            TreeView.NodeCheckState.UNCHECKED, false, false);
        nodeRenderer.setSize(nodeBounds.width, nodeBounds.height);

        // Get the text bounds
        Bounds textBounds = nodeRenderer.getTextBounds();

        // Calculate the bounds of what is being edited
        Insets padding = (Insets)textInput.getStyles().get("padding");
        Bounds editBounds = new Bounds(nodeBounds.x + textBounds.x - (padding.left + 1),
            nodeBounds.y, nodeBounds.width - textBounds.x + (padding.left + 1),
            nodeBounds.height);

        // Scroll to make the node as visible as possible
        treeViewArgument.scrollAreaToVisible(editBounds.x, editBounds.y,
            textBounds.width + padding.left + 1, editBounds.height);

        // Constrain the bounds by what is visible through viewport ancestors
        editBounds = treeViewArgument.getVisibleArea(editBounds);
        Point location = treeViewArgument.mapPointToAncestor(treeViewArgument.getDisplay(), editBounds.x, editBounds.y);

        textInput.setPreferredWidth(editBounds.width);
        setLocation(location.x, location.y + (editBounds.height - getPreferredHeight(-1)) / 2);

        // Open the editor
        open(treeViewArgument.getWindow());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void endEdit(boolean result) {
        if (result) {
            // Update the node data
            String text = textInput.getText();

            List<?> treeData = treeView.getTreeData();
            TreeNode treeNode = (TreeNode)Sequence.Tree.get(treeData, path);
            treeNode.setText(text);

            // Get a reference to node's parent
            int n = path.getLength();
            List<TreeNode> parentData;

            if (n == 1) {
                parentData = (List<TreeNode>)treeData;
            } else {
                Path parentPath = new Path(path, n - 1);
                parentData = (List<TreeNode>)Sequence.Tree.get(treeData, parentPath);
            }

            if (parentData.getComparator() == null) {
                int index = path.get(n - 1);
                parentData.remove(index, 1);
                parentData.insert(treeNode, index);
            } else {
                parentData.remove(path.get(n - 1), 1);
                parentData.add(treeNode);

                // Re-select the node, and make sure it's visible
                path = new Path(path, n - 1);
                path.add(parentData.indexOf(treeNode));
                treeView.setSelectedPath(path);
                treeView.scrollAreaToVisible(treeView.getNodeBounds(path));
            }
        }

        getOwner().moveToFront();
        treeView.requestFocus();

        treeView = null;
        path = null;

        close();
    }

    @Override
    public boolean isEditing() {
        return (treeView != null);
    }

    @Override
    public void open(Display display, Window owner) {
        if (treeView == null) {
            throw new IllegalStateException();
        }

        super.open(display, owner);
        display.getContainerMouseListeners().add(displayMouseHandler);

        requestFocus();
    }

    @Override
    public void close() {
        Display display = getDisplay();
        display.getContainerMouseListeners().remove(displayMouseHandler);

        super.close();
    }

    @Override
    public boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed;

        if (keyCode == Keyboard.KeyCode.ENTER) {
            endEdit(true);
            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.ESCAPE) {
            endEdit(false);
            consumed = true;
        } else {
            consumed = false;
        }

        return consumed;
    }
}
