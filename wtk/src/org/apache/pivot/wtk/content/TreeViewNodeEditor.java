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
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.ComponentListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.TreeViewListener;
import org.apache.pivot.wtk.TreeViewNodeListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;
import org.apache.pivot.wtk.content.TreeNode;
import org.apache.pivot.wtk.content.TreeViewNodeRenderer;

/**
 * Default tree view node editor, which allows the user to edit the text of a
 * tree node in a <tt>TextInput</tt>. It is only intended to work with
 * {@link TreeNode} data and {@link TreeViewNodeRenderer} renderers.
 */
public class TreeViewNodeEditor implements TreeView.NodeEditor {
    /**
     * Responsible for repositioning the popup when the table view's size changes.
     */
    private ComponentListener componentListener = new ComponentListener.Adapter() {
        @Override
        public void sizeChanged(Component component, int previousWidth, int previousHeight) {
            ApplicationContext.queueCallback(new Runnable() {
                @Override
                public void run() {
                    reposition();
                }
            });
        }

        @Override
        public void locationChanged(Component component, int previousX, int previousY) {
            ApplicationContext.queueCallback(new Runnable() {
                @Override
                public void run() {
                    reposition();
                }
            });
        }
    };

    /**
     * Responsible for "edit initialization" and "edit finalization" tasks when
     * the edit popup is opened and closed, respectively.
     */
    private WindowStateListener popupStateHandler = new WindowStateListener.Adapter() {
        @Override
        public void windowOpened(Window window) {
            window.setOwner(treeView.getWindow());

            Display display = window.getDisplay();
            display.getContainerMouseListeners().add(displayMouseHandler);

            treeView.getComponentListeners().add(componentListener);
            treeView.getTreeViewListeners().add(treeViewHandler);
            treeView.getTreeViewNodeListeners().add(treeViewNodeHandler);
        }

        @Override
        public void windowClosed(Window window, Display display) {
            // Clean up
            display.getContainerMouseListeners().remove(displayMouseHandler);

            treeView.getComponentListeners().remove(componentListener);
            treeView.getTreeViewListeners().remove(treeViewHandler);
            treeView.getTreeViewNodeListeners().remove(treeViewNodeHandler);

            // Move the owner to front
            window.getOwner().moveToFront();
            window.setOwner(null);

            // Free memory
            treeView = null;
            path = null;
            textInput = null;
            popup = null;
        }
    };

    /**
     * Responsible for saving or cancelling the edit based on the user pressing
     * the <tt>ENTER</tt> key or the <tt>ESCAPE</tt> key, respectively.
     */
    private ComponentKeyListener textInputKeyHandler = new ComponentKeyListener.Adapter() {
        @Override
        public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            if (keyCode == Keyboard.KeyCode.ENTER) {
                save();
            } else if (keyCode == Keyboard.KeyCode.ESCAPE) {
                cancel();
            }

            return false;
        }
    };

    /**
     * Responsible for closing the popup whenever the user clicks outside the
     * bounds of the popup.
     */
    private ContainerMouseListener displayMouseHandler = new ContainerMouseListener.Adapter() {
        @Override
        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            Display display = (Display)container;
            Window window = (Window)display.getComponentAt(x, y);

            if (popup != window) {
                save();
            }

            return false;
        }

        @Override
        public boolean mouseWheel(Container container, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            return true;
        }
    };

    /**
     * Responsible for cancelling the edit if any relevant changes are made to
     * the tree view while we're editing.
     */
    private TreeViewListener treeViewHandler = new TreeViewListener.Adapter() {
        @Override
        public void treeDataChanged(TreeView treeView, List<?> previousTreeData) {
            cancel();
        }

        @Override
        public void nodeEditorChanged(TreeView treeView, TreeView.NodeEditor previousNodeEditor) {
            cancel();
        }
    };

    /**
     * Responsible for cancelling the edit if any changes are made to
     * the tree data while we're editing.
     */
    private TreeViewNodeListener treeViewNodeHandler = new TreeViewNodeListener.Adapter() {
        @Override
        public void nodeInserted(TreeView treeView, Path path, int index) {
            cancel();
        }

        @Override
        public void nodesRemoved(TreeView treeView, Path path, int index, int count) {
            cancel();
        }

        @Override
        public void nodeUpdated(TreeView treeView, Path path, int index) {
            cancel();
        }

        @Override
        public void nodesSorted(TreeView treeView, Path path) {
            cancel();
        }
    };

    private TreeView treeView = null;
    private Path path = null;

    private Window popup = null;
    private TextInput textInput = null;

    /**
     * Gets the text input that serves as the editor component. This component
     * will only be non-<tt>null</tt> while editing.
     *
     * @return
     * This editor's component, or <tt>null</tt> if an edit is not in progress
     *
     * @see #isEditing()
     */
    protected final TextInput getEditor() {
        return textInput;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void edit(TreeView treeView, Path path) {
        if (isEditing()) {
            throw new IllegalStateException();
        }

        this.treeView = treeView;
        this.path = path;

        // Get the data being edited
        List<?> treeData = treeView.getTreeData();
        TreeNode nodeData = (TreeNode)Sequence.Tree.get(treeData, path);

        textInput = new TextInput();
        textInput.setText(nodeData.getText());
        textInput.getComponentKeyListeners().add(textInputKeyHandler);

        popup = new Window(textInput);
        popup.getWindowStateListeners().add(popupStateHandler);
        popup.open(treeView.getDisplay());
        reposition();

        textInput.selectAll();
        textInput.requestFocus();
    }

    /**
     * Repositions the popup to be located over the node being edited.
     */
    private void reposition() {
        // Get the data being edited
        List<?> treeData = treeView.getTreeData();
        TreeNode nodeData = (TreeNode)Sequence.Tree.get(treeData, path);

        // Get the node bounds
        Bounds nodeBounds = treeView.getNodeBounds(path);
        int nodeIndent = treeView.getNodeIndent(path.getLength());
        nodeBounds = new Bounds(nodeBounds.x + nodeIndent, nodeBounds.y,
            nodeBounds.width - nodeIndent, nodeBounds.height);

        // Render the node data
        TreeViewNodeRenderer nodeRenderer = (TreeViewNodeRenderer)treeView.getNodeRenderer();
        nodeRenderer.render(nodeData, treeView, false, false,
            TreeView.NodeCheckState.UNCHECKED, false, false);
        nodeRenderer.setSize(nodeBounds.width, nodeBounds.height);

        // Get the text bounds
        Bounds textBounds = nodeRenderer.getTextBounds();

        // Calculate the bounds of what we're editing
        Insets padding = (Insets)textInput.getStyles().get("padding");
        Bounds editBounds = new Bounds(nodeBounds.x + textBounds.x - (padding.left + 1),
            nodeBounds.y, nodeBounds.width - textBounds.x + (padding.left + 1),
            nodeBounds.height);

        // Scroll to make the text as visible as possible
        treeView.scrollAreaToVisible(editBounds.x, editBounds.y,
            textBounds.width + padding.left + 1, editBounds.height);

        // Constrain the bounds by what is visible through Viewport ancestors
        editBounds = treeView.getVisibleArea(editBounds);

        textInput.setPreferredWidth(editBounds.width);
        popup.setLocation(editBounds.x, editBounds.y
            + (editBounds.height - textInput.getPreferredHeight(-1)) / 2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEditing() {
        return (treeView != null);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void save() {
        if (!isEditing()) {
            throw new IllegalStateException();
        }

        List<?> treeData = treeView.getTreeData();
        TreeNode treeNode = (TreeNode)Sequence.Tree.get(treeData, path);

        // Update the node data
        String text = textInput.getText();
        treeNode.setText(text);

        // Get a reference to the parent of the node data
        int n = path.getLength();
        List<TreeNode> parentData;

        if (n == 1) {
            parentData = (List<TreeNode>)treeData;
        } else {
            Path parentPath = new Path(path, n - 1);
            parentData = (List<TreeNode>)Sequence.Tree.get(treeData, parentPath);
        }

        // Notifying the parent will close the popup
        if (parentData.getComparator() == null) {
            parentData.update(path.get(n - 1), treeNode);
        } else {
            // Save local reference to members variables before they get cleared
            TreeView treeView = this.treeView;
            Path path = this.path;

            parentData.remove(path.get(n - 1), 1);
            parentData.add(treeNode);

            // Re-select the node, and make sure it's visible
            Path newPath = new Path(path, n - 1);
            newPath.add(parentData.indexOf(treeNode));
            treeView.setSelectedPath(newPath);
            treeView.scrollAreaToVisible(treeView.getNodeBounds(newPath));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancel() {
        if (!isEditing()) {
            throw new IllegalStateException();
        }

        popup.close();
    }
}
