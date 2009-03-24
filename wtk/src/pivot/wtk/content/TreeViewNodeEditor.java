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
package pivot.wtk.content;

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.collections.Sequence;
import pivot.util.Vote;
import pivot.wtk.Bounds;
import pivot.wtk.Component;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.Container;
import pivot.wtk.ContainerMouseListener;
import pivot.wtk.Display;
import pivot.wtk.Insets;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.Point;
import pivot.wtk.TextInput;
import pivot.wtk.TreeView;
import pivot.wtk.TreeViewListener;
import pivot.wtk.TreeViewNodeListener;
import pivot.wtk.Window;
import pivot.wtk.WindowStateListener;
import pivot.wtk.content.TreeNode;
import pivot.wtk.content.TreeViewNodeRenderer;

/**
 * Default tree view node editor, which allows the user to edit the text of a
 * tree node in a <tt>TextInput</tt>. It is only intended to work with
 * {@link TreeNode} data and {@link TreeViewNodeRenderer} renderers.
 *
 * @author tvolkert
 */
public class TreeViewNodeEditor implements TreeView.NodeEditor {
    /**
     * Responsible for "edit initialization" and "edit finalization" tasks when
     * the edit popup is opened and closed, respectively.
     *
     * @author tvolkert
     */
    private WindowStateListener popupStateHandler = new WindowStateListener() {
        public Vote previewWindowOpen(Window window, Display display) {
            return Vote.APPROVE;
        }

        public void windowOpenVetoed(Window window, Vote reason) {
        }

        public void windowOpened(Window window) {
            Display display = window.getDisplay();
            display.getContainerMouseListeners().add(displayMouseHandler);

            treeView.getTreeViewListeners().add(treeViewHandler);
            treeView.getTreeViewNodeListeners().add(treeViewHandler);
        }

        public Vote previewWindowClose(Window window) {
            return Vote.APPROVE;
        }

        public void windowCloseVetoed(Window window, Vote reason) {
        }

        public void windowClosed(Window window, Display display) {
            // Clean up
            display.getContainerMouseListeners().remove(displayMouseHandler);

            treeView.getTreeViewListeners().remove(treeViewHandler);
            treeView.getTreeViewNodeListeners().remove(treeViewHandler);

            // Restore focus to the tree view
            treeView.requestFocus();

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
     *
     * @author tvolkert
     */
    private ComponentKeyListener textInputKeyHandler = new ComponentKeyListener() {
        public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            if (keyCode == Keyboard.KeyCode.ENTER) {
                save();
            } else if (keyCode == Keyboard.KeyCode.ESCAPE) {
                cancel();
            }

            return false;
        }

        public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            return false;
        }

        public boolean keyTyped(Component component, char character) {
            return false;
        }
    };

    /**
     * Responsible for closing the popup whenever the user clicks outside the
     * bounds of the popup.
     *
     * @author tvolkert
     */
    private ContainerMouseListener displayMouseHandler = new ContainerMouseListener() {
        public boolean mouseMove(Container container, int x, int y) {
            return false;
        }

        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            // If the event did not occur within a window that is owned by
            // this popup, close the popup
            Display display = (Display)container;
            Window window = (Window)display.getComponentAt(x, y);

            if (popup != window) {
                save();
            }

            return false;
        }

        public boolean mouseUp(Container container, Mouse.Button button, int x, int y) {
            return false;
        }

        public boolean mouseWheel(Container container, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            return true;
        }
    };

    /**
     * Responsible for cancelling the edit if any relevant changes are made to
     * the tree view while we're editing.
     */
    private class TreeViewHandler implements TreeViewListener, TreeViewNodeListener {
        public void treeDataChanged(TreeView treeView, List<?> previousTreeData) {
            cancel();
        }

        public void nodeRendererChanged(TreeView treeView, TreeView.NodeRenderer previousNodeRenderer) {
        }

        public void nodeEditorChanged(TreeView treeView, TreeView.NodeEditor previousNodeEditor) {
            cancel();
        }

        public void selectModeChanged(TreeView treeView, TreeView.SelectMode previousSelectMode) {
        }

        public void checkmarksEnabledChanged(TreeView treeView) {
        }

        public void showMixedCheckmarkStateChanged(TreeView treeView) {
        }

        public void nodeInserted(TreeView treeView, Sequence<Integer> path, int index) {
            cancel();
        }

        public void nodesRemoved(TreeView treeView, Sequence<Integer> path, int index, int count) {
            cancel();
        }

        public void nodeUpdated(TreeView treeView, Sequence<Integer> path, int index) {
            cancel();
        }

        public void nodesSorted(TreeView treeView, Sequence<Integer> path) {
            cancel();
        }
    }

    private TreeView treeView = null;
    private Sequence<Integer> path = null;

    private Window popup = null;
    private TextInput textInput = null;

    private TreeViewHandler treeViewHandler = new TreeViewHandler();

    public void edit(TreeView treeView, Sequence<Integer> path) {
        if (isEditing()) {
            throw new IllegalStateException();
        }

        this.treeView = treeView;
        this.path = path;

        // Get the data being edited
        List<?> treeData = treeView.getTreeData();
        TreeNode nodeData = (TreeNode)Sequence.Tree.get(treeData, path);

        // Get the node bounds
        Bounds nodeBounds = treeView.getNodeBounds(path);
        int nodeIndent = treeView.getNodeIndent(path.getLength());
        nodeBounds.x += nodeIndent;
        nodeBounds.width -= nodeIndent;

        // Render the node data
        TreeViewNodeRenderer nodeRenderer = (TreeViewNodeRenderer)treeView.getNodeRenderer();
        nodeRenderer.render(nodeData, treeView, false, false,
            TreeView.NodeCheckState.UNCHECKED, false, false);
        nodeRenderer.setSize(nodeBounds.width, nodeBounds.height);

        // Get the text bounds
        Bounds textBounds = nodeRenderer.getTextBounds();

        if (textBounds != null) {
            textInput = new TextInput();
            Insets padding = (Insets)textInput.getStyles().get("padding");

            // Calculate the bounds of what we're editing
            Bounds editBounds = new Bounds(nodeBounds);
            editBounds.x += textBounds.x - (padding.left + 1);
            editBounds.width -= textBounds.x;
            editBounds.width += (padding.left + 1);

            // Scroll to make the text as visible as possible
            treeView.scrollAreaToVisible(editBounds.x, editBounds.y,
                textBounds.width, editBounds.height);

            // Constrain the bounds by what is visible through Viewport ancestors
            treeView.constrainToViewportBounds(editBounds);

            textInput.setText(nodeData.getText());
            textInput.setPreferredWidth(editBounds.width);
            textInput.getComponentKeyListeners().add(textInputKeyHandler);

            popup = new Window(textInput);
            popup.getWindowStateListeners().add(popupStateHandler);

            Point location = treeView.mapPointToAncestor(treeView.getDisplay(), 0, 0);
            popup.setLocation(location.x + editBounds.x, location.y +
                editBounds.y + (editBounds.height - textInput.getPreferredHeight(-1)) / 2);
            popup.open(treeView.getWindow());

            textInput.requestFocus();
        }
    }

    public boolean isEditing() {
        return (treeView != null);
    }

    @SuppressWarnings("unchecked")
    public void save() {
        if (!isEditing()) {
            throw new IllegalStateException();
        }

        List<?> treeData = treeView.getTreeData();
        TreeNode nodeData = (TreeNode)Sequence.Tree.get(treeData, path);

        // Update the node data
        String text = textInput.getText();
        nodeData.setText(text);

        // Notifying the parent will close the popup
        int n = path.getLength();
        if (n == 1) {
            // Base case
            int index = path.get(0);
            ((List<TreeNode>)treeData).update(index, nodeData);
        } else {
            Sequence<Integer> parentPath = new ArrayList<Integer>(path, 0, n - 1);
            TreeBranch parentData = (TreeBranch)Sequence.Tree.get(treeData, parentPath);
            int index = path.get(n - 1);
            parentData.update(index, nodeData);
        }
    }

    public void cancel() {
        if (!isEditing()) {
            throw new IllegalStateException();
        }

        popup.close();
    }
}
