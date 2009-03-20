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
import pivot.wtk.Popup;
import pivot.wtk.TextInput;
import pivot.wtk.TreeView;
import pivot.wtk.TreeViewListener;
import pivot.wtk.TreeViewBranchListener;
import pivot.wtk.TreeViewNodeListener;
import pivot.wtk.TreeViewNodeStateListener;
import pivot.wtk.TreeViewSelectionListener;
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
            // Add this as a container mouse listener on display
            Display display = window.getDisplay();
            display.getContainerMouseListeners().add(displayMouseHandler);

            treeView.getTreeViewListeners().add(treeViewHandler);
            treeView.getTreeViewBranchListeners().add(treeViewHandler);
            treeView.getTreeViewNodeListeners().add(treeViewHandler);
            treeView.getTreeViewNodeStateListeners().add(treeViewHandler);
            treeView.getTreeViewSelectionListeners().add(treeViewHandler);
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
            treeView.getTreeViewBranchListeners().remove(treeViewHandler);
            treeView.getTreeViewNodeListeners().remove(treeViewHandler);
            treeView.getTreeViewNodeStateListeners().remove(treeViewHandler);
            treeView.getTreeViewSelectionListeners().remove(treeViewHandler);

            // Restore focus to the tree view
            treeView.requestFocus();

            // Free memory
            treeView = null;
            editPath = null;
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
        @SuppressWarnings("unchecked")
        public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            if (keyCode == Keyboard.KeyCode.ENTER) {
                List<?> treeData = treeView.getTreeData();
                TreeNode nodeData = (TreeNode)Sequence.Tree.get(treeData, editPath);

                // Update the node data
                TextInput textInput = (TextInput)component;
                String text = textInput.getText();
                nodeData.setText(text);

                // Notify the tree branch that the child was updated. Our
                // treeViewHandler will be notified of this edit and close
                // the popup for us.
                int n = editPath.getLength();
                if (n == 1) {
                    // Base case
                    int index = editPath.get(0);
                    ((List<TreeNode>)treeData).update(index, nodeData);
                } else {
                    Sequence<Integer> parentPath = new ArrayList<Integer>(editPath, 0, n - 1);
                    TreeBranch parentData = (TreeBranch)Sequence.Tree.get(treeData, parentPath);
                    int index = editPath.get(n - 1);
                    parentData.update(index, nodeData);
                }
            } else if (keyCode == Keyboard.KeyCode.ESCAPE) {
                // Close the popup without saving (cancel)
                popup.close();
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
        public void mouseMove(Container container, int x, int y) {
            // No-op
        }

        public void mouseDown(Container container, Mouse.Button button, int x, int y) {
            // If the event did not occur within a window that is owned by
            // this popup, close the popup
            Display display = (Display)container;
            Window window = (Window)display.getComponentAt(x, y);

            if (window == null
                || !popup.isOwningAncestorOf(window)) {
                popup.close();
            }
        }

        public void mouseUp(Container container, Mouse.Button button, int x, int y) {
            // No-op
        }

        public void mouseWheel(Container container, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            // No-op
        }
    };

    /**
     * Responsible for cancelling the edit if any relevant changes are made to
     * the tree view while we're editing.
     */
    private class TreeViewHandler implements
        TreeViewListener, TreeViewBranchListener, TreeViewNodeListener,
        TreeViewNodeStateListener, TreeViewSelectionListener {
        private TreeView.PathComparator pathComparator = new TreeView.PathComparator();

        public void treeDataChanged(TreeView treeView, List<?> previousTreeData) {
            popup.close();
        }

        public void nodeRendererChanged(TreeView treeView, TreeView.NodeRenderer previousNodeRenderer) {
            popup.close();
        }

        public void nodeEditorChanged(TreeView treeView, TreeView.NodeEditor previousNodeEditor) {
            popup.close();
        }

        public void selectModeChanged(TreeView treeView, TreeView.SelectMode previousSelectMode) {
            popup.close();
        }

        public void checkmarksEnabledChanged(TreeView treeView) {
            popup.close();
        }

        public void showMixedCheckmarkStateChanged(TreeView treeView) {
        }

        public void branchExpanded(TreeView treeView, Sequence<Integer> path) {
            if (pathComparator.compare(path, editPath) < 0) {
                popup.close();
            }
        }

        public void branchCollapsed(TreeView treeView, Sequence<Integer> path) {
            if (pathComparator.compare(path, editPath) < 0) {
                popup.close();
            }
        }

        public void nodeInserted(TreeView treeView, Sequence<Integer> path, int index) {
            Sequence<Integer> childPath = new ArrayList<Integer>(path);
            childPath.add(index);

            if (pathComparator.compare(childPath, editPath) <= 0) {
                popup.close();
            }
        }

        public void nodesRemoved(TreeView treeView, Sequence<Integer> path, int index, int count) {
            Sequence<Integer> childPath = new ArrayList<Integer>(path);
            childPath.add(index);

            if (pathComparator.compare(childPath, editPath) <= 0) {
                popup.close();
            }
        }

        public void nodeUpdated(TreeView treeView, Sequence<Integer> path, int index) {
            Sequence<Integer> childPath = new ArrayList<Integer>(path);
            childPath.add(index);

            if (pathComparator.compare(childPath, editPath) <= 0) {
                popup.close();
            }
        }

        public void nodesSorted(TreeView treeView, Sequence<Integer> path) {
            if (Sequence.Tree.isDescendant(path, editPath)) {
                popup.close();
            }
        }

        public void nodeDisabledChanged(TreeView treeView, Sequence<Integer> path) {
            if (pathComparator.compare(path, editPath) == 0) {
                popup.close();
            }
        }

        public void nodeCheckStateChanged(TreeView treeView, Sequence<Integer> path,
            TreeView.NodeCheckState previousCheckState) {
        }

        public void selectedPathAdded(TreeView treeView, Sequence<Integer> path) {
        }

        public void selectedPathRemoved(TreeView treeView, Sequence<Integer> path) {
            if (pathComparator.compare(path, editPath) == 0) {
                popup.close();
            }
        }

        public void selectedPathsChanged(TreeView treeView,
            Sequence<Sequence<Integer>> previousSelectedPaths) {
            popup.close();
        }
    }

    private TreeView treeView = null;
    private Sequence<Integer> editPath = null;
    private Popup popup = null;

    private TreeViewHandler treeViewHandler = new TreeViewHandler();

    public void edit(TreeView treeView, Sequence<Integer> path) {
        if (this.treeView != null) {
            throw new IllegalStateException("Currently editing.");
        }

        this.treeView = treeView;
        this.editPath = path;

        TreeViewNodeRenderer nodeRenderer = (TreeViewNodeRenderer)treeView.getNodeRenderer();

        // Get the data being edited
        List<?> treeData = treeView.getTreeData();
        TreeNode nodeData = (TreeNode)Sequence.Tree.get(treeData, path);

        // Calculate the indent to the node's text
        int textIndent = treeView.getNodeIndent(path.getLength());
        textIndent += ((Insets)nodeRenderer.getStyles().get("padding")).left;
        if (nodeRenderer.getShowIcon()) {
            textIndent += nodeRenderer.getIconWidth();
            textIndent += (Integer)nodeRenderer.getStyles().get("spacing");
        }

        // Calculate the bounds of the node's text
        Bounds textBounds = treeView.getNodeBounds(path);
        textBounds.x += textIndent;
        textBounds.width -= textIndent;

        // Constrain the bounds by what is visible through Viewport ancestors
        treeView.constrainToViewportBounds(textBounds);

        TextInput textInput = new TextInput();
        textInput.setText(nodeData.getText());
        textInput.setPreferredWidth(textBounds.width);
        textInput.getComponentKeyListeners().add(textInputKeyHandler);

        popup = new Popup(textInput);
        Point displayCoordinates = treeView.mapPointToAncestor(treeView.getDisplay(), 0, 0);
        popup.setLocation(displayCoordinates.x + textBounds.x, displayCoordinates.y +
            textBounds.y + (textBounds.height - textInput.getPreferredHeight(-1)) / 2);
        popup.getWindowStateListeners().add(popupStateHandler);
        popup.open(treeView);

        textInput.requestFocus();
    }
}
