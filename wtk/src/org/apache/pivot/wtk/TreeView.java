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
package org.apache.pivot.wtk;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.Sequence.Tree.ImmutablePath;
import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.collections.immutable.ImmutableList;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.content.TreeViewNodeRenderer;

/**
 * Class that displays a hierarchical data structure, allowing a user to select
 * one or more paths.
 */
@DefaultProperty("treeData")
public class TreeView extends Component {
    /**
     * Enumeration defining supported selection modes. <tt>TreeView</tt>
     * defaults to single select mode.
     */
    public enum SelectMode {
        /**
         * Selection is disabled.
         */
        NONE,

        /**
         * A single path may be selected at a time.
         */
        SINGLE,

        /**
         * Multiple paths may be concurrently selected.
         */
        MULTI
    }

    /**
     * Enumeration defining node check states. Note that <tt>TreeView</tt> does
     * not involve itself in the propagation of checkmarks (either up or down
     * the tree). Developers who wish to propagate checkmarks may do so by
     * registering a {@link TreeViewNodeStateListener} and setting the desired
     * checkmark states manually.
     */
    public enum NodeCheckState {
        /**
         * The node is checked.
         */
        CHECKED,

        /**
         * The node is unchecked. If <tt>showMixedCheckmarkState</tt> is true,
         * this implies that all of the node's descendants are unchecked as
         * well.
         */
        UNCHECKED,

        /**
         * The node's check state is mixed, meaning that it is not checked,
         * but at least one of its descendants is checked. This state will only
         * be reported if <tt>showMixedCheckmarkState</tt> is true. Otherwise,
         * the node will be reported as {@link #UNCHECKED}.
         */
        MIXED
    }

    /**
     * {@link Renderer} interface to customize the appearance of items in a TreeView.
     */
    public interface NodeRenderer extends Renderer {
        /**
         * Prepares the renderer for layout or paint.
         *
         * @param node
         * The node value to render, or <tt>null</tt> if called to calculate
         * preferred height for skins that assume a fixed renderer height.
         *
         * @param path
         * The path to the node being rendered, or <tt>null</tt> if
         * <tt>node</tt> is <tt>null</tt>.
         *
         * @param rowIndex
         * The row index of the node being rendered, as seen in the current
         * visible nodes list, or <tt>-1</tt> if <tt>node</tt> is <tt>null</tt>.
         *
         * @param treeView
         * The host component.
         *
         * @param expanded
         * <tt>true</tt> if the node is expanded; <tt>false</tt> otherwise.
         *
         * @param selected
         * <tt>true</tt> if the node is selected; <tt>false</tt> otherwise.
         *
         * @param checkState
         * The node's {@linkplain NodeCheckState check state}.
         *
         * @param highlighted
         * <tt>true</tt> if the node is highlighted; <tt>false</tt> otherwise.
         *
         * @param disabled
         * <tt>true</tt> if the node is disabled; <tt>false</tt> otherwise.
         */
        public void render(Object node, Path path, int rowIndex, TreeView treeView,
            boolean expanded, boolean selected, NodeCheckState checkState,
            boolean highlighted, boolean disabled);

        /**
         * Converts a tree node to a string representation.
         *
         * @param node
         *
         * @return
         * The node's string representation, or <tt>null</tt> if the node does not
         * have a string representation.
         * <p>
         * Note that this method may be called often during keyboard navigation, so
         * implementations should avoid unnecessary string allocations.
         */
        public String toString(Object node);
    }

    /**
     * Tree view node editor interface.
     */
    public interface NodeEditor {
        /**
         * Called to begin editing a tree node.
         *
         * @param treeView
         * @param path
         */
        public void beginEdit(TreeView treeView, Path path);

        /**
         * Terminates an edit operation.
         *
         * @param result
         * <tt>true</tt> to perform the edit; <tt>false</tt> to cancel it.
         */
        public void endEdit(boolean result);

        /**
         * Tests whether an edit is currently in progress.
         */
        public boolean isEditing();
    }

    /**
     * Tree view skin interface. Tree view skins must implement this.
     */
    public interface Skin {
        /**
         * Gets the path to the node found at the specified y-coordinate
         * (relative to the tree view).
         *
         * @param y
         * The y-coordinate in pixels.
         *
         * @return
         * The path to the node, or <tt>null</tt> if there is no node being
         * painted at the specified y-coordinate.
         */
        public Path getNodeAt(int y);

        /**
         * Gets the bounds of the node at the specified path relative to the
         * tree view. Note that all nodes are left aligned with the tree; to
         * get the pixel value of a node's indent, use
         * {@link #getNodeIndent(int)}.
         *
         * @param path
         * The path to the node.
         *
         * @return
         * The bounds, or <tt>null</tt> if the node is not currently visible.
         */
        public Bounds getNodeBounds(Path path);

        /**
         * Gets the pixel indent of nodes at the specified depth. Depth is
         * measured in generations away from the tree view's "root" node, which
         * is represented by the {@link #getTreeData() tree data}.
         *
         * @param depth
         * The depth, where the first child of the root has depth 1, the child
         * of that branch has depth 2, etc.
         *
         * @return
         * The indent in pixels to the node's content.
         */
        public int getNodeIndent(int depth);

        /**
         * Gets the row index of the node, as seen in the current visible nodes
         * list. Note that as branches are expanded and collapsed, the row
         * index of any given node in the tree will change.
         *
         * @param path
         * The path to the node.
         *
         * @return
         * The row index of the node, or <tt>-1</tt> if the node is not
         * currently visible.
         */
        public int getRowIndex(Path path);
    }

    /**
     * Tree view listener list.
     */
    private static class TreeViewListenerList extends WTKListenerList<TreeViewListener>
        implements TreeViewListener {

        @Override
        public void treeDataChanged(TreeView treeView, List<?> previousTreeData) {
            for (TreeViewListener listener : this) {
                listener.treeDataChanged(treeView, previousTreeData);
            }
        }

        @Override
        public void nodeRendererChanged(TreeView treeView,
            NodeRenderer previousNodeRenderer) {
            for (TreeViewListener listener : this) {
                listener.nodeRendererChanged(treeView, previousNodeRenderer);
            }
        }

        @Override
        public void nodeEditorChanged(TreeView treeView,
            TreeView.NodeEditor previousNodeEditor) {
            for (TreeViewListener listener : this) {
                listener.nodeEditorChanged(treeView, previousNodeEditor);
            }
        }

        @Override
        public void selectModeChanged(TreeView treeView, SelectMode previousSelectMode) {
            for (TreeViewListener listener : this) {
                listener.selectModeChanged(treeView, previousSelectMode);
            }
        }

        @Override
        public void checkmarksEnabledChanged(TreeView treeView) {
            for (TreeViewListener listener : this) {
                listener.checkmarksEnabledChanged(treeView);
            }
        }

        @Override
        public void showMixedCheckmarkStateChanged(TreeView treeView) {
            for (TreeViewListener listener : this) {
                listener.showMixedCheckmarkStateChanged(treeView);
            }
        }

        @Override
        public void disabledNodeFilterChanged(TreeView treeView,
            Filter<?> previousDisabledNodeFilter) {
            for (TreeViewListener listener : this) {
                listener.disabledNodeFilterChanged(treeView, previousDisabledNodeFilter);
            }
        }

        @Override
        public void disabledCheckmarkFilterChanged(TreeView treeView,
            Filter<?> previousDisabledCheckmarkFilter) {
            for (TreeViewListener listener : this) {
                listener.disabledCheckmarkFilterChanged(treeView, previousDisabledCheckmarkFilter);
            }
        }
    }

    /**
     * Tree view branch listener list.
     */
    private static class TreeViewBranchListenerList extends WTKListenerList<TreeViewBranchListener>
        implements TreeViewBranchListener {
        @Override
        public void branchExpanded(TreeView treeView, Path path) {
            for (TreeViewBranchListener listener : this) {
                listener.branchExpanded(treeView, path);
            }
        }

        @Override
        public void branchCollapsed(TreeView treeView, Path path) {
            for (TreeViewBranchListener listener : this) {
                listener.branchCollapsed(treeView, path);
            }
        }
    }

    /**
     * Tree view node listener list.
     */
    private static class TreeViewNodeListenerList extends WTKListenerList<TreeViewNodeListener>
        implements TreeViewNodeListener {
        @Override
        public void nodeInserted(TreeView treeView, Path path, int index) {
            for (TreeViewNodeListener listener : this) {
                listener.nodeInserted(treeView, path, index);
            }
        }

        @Override
        public void nodesRemoved(TreeView treeView, Path path, int index,
            int count) {
            for (TreeViewNodeListener listener : this) {
                listener.nodesRemoved(treeView, path, index, count);
            }
        }

        @Override
        public void nodeUpdated(TreeView treeView, Path path, int index) {
            for (TreeViewNodeListener listener : this) {
                listener.nodeUpdated(treeView, path, index);
            }
        }

        @Override
        public void nodesCleared(TreeView treeView, Path path) {
            for (TreeViewNodeListener listener : this) {
                listener.nodesCleared(treeView, path);
            }
        }

        @Override
        public void nodesSorted(TreeView treeView, Path path) {
            for (TreeViewNodeListener listener : this) {
                listener.nodesSorted(treeView, path);
            }
        }
    }

    /**
     * Tree view node state listener list.
     */
    private static class TreeViewNodeStateListenerList
        extends WTKListenerList<TreeViewNodeStateListener>
        implements TreeViewNodeStateListener {
        @Override
        public void nodeCheckStateChanged(TreeView treeView, Path path,
            TreeView.NodeCheckState previousCheckState) {
            for (TreeViewNodeStateListener listener : this) {
                listener.nodeCheckStateChanged(treeView, path, previousCheckState);
            }
        }
    }

    /**
     * Tree view selection listener list.
     */
    private static class TreeViewSelectionListenerList
        extends WTKListenerList<TreeViewSelectionListener>
        implements TreeViewSelectionListener {
        @Override
        public void selectedPathAdded(TreeView treeView, Path path) {
            for (TreeViewSelectionListener listener : this) {
                listener.selectedPathAdded(treeView, path);
            }
        }

        @Override
        public void selectedPathRemoved(TreeView treeView, Path path) {
            for (TreeViewSelectionListener listener : this) {
                listener.selectedPathRemoved(treeView, path);
            }
        }

        @Override
        public void selectedPathsChanged(TreeView treeView,
            Sequence<Path> previousSelectedPaths) {
            for (TreeViewSelectionListener listener : this) {
                listener.selectedPathsChanged(treeView, previousSelectedPaths);
            }
        }

        @Override
        public void selectedNodeChanged(TreeView treeView, Object previousSelectedNode) {
            for (TreeViewSelectionListener listener : this) {
                listener.selectedNodeChanged(treeView, previousSelectedNode);
            }
        }
    }

    /**
     * A comparator that sorts paths by the order in which they would visually
     * appear in a fully expanded tree, otherwise known as their "row order".
     */
    public static final class PathComparator implements Comparator<Path>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(Path path1, Path path2) {
            int path1Length = path1.getLength();
            int path2Length = path2.getLength();

            for (int i = 0, n = Math.min(path1Length, path2Length); i < n; i++) {
                int pathElement1 = path1.get(i);
                int pathElement2 = path2.get(i);

                if (pathElement1 != pathElement2) {
                    return pathElement1 - pathElement2;
                }
            }

            return path1Length - path2Length;
        }
    }

    /**
     * Notifies the tree of nested <tt>ListListener</tt> events that occur on
     * the tree data.
     */
    private class BranchHandler extends ArrayList<BranchHandler> implements ListListener<Object> {
        private static final long serialVersionUID = -6132480635507615071L;

        // Reference to its parent allows for the construction of its path
        private BranchHandler parent;

        // The backing data structure
        private List<?> branchData;

        /**
         * Creates a new <tt>BranchHandler</tt> tied to the specified parent
         * and listening to events from the specified branch data.
         */
        @SuppressWarnings("unchecked")
        public BranchHandler(BranchHandler parent, List<?> branchData) {
            super(branchData.getLength());

            this.parent = parent;
            this.branchData = branchData;

            ((List<Object>)branchData).getListListeners().add(this);

            // Create placeholder child entries, to be loaded lazily
            for (int i = 0, n = branchData.getLength(); i < n; i++) {
                add(null);
            }
        }

        /**
         * Gets the branch data that this handler is monitoring.
         */
        public List<?> getBranchData() {
            return branchData;
        }

        /**
         * Unregisters this branch handler's interest in ListListener events.
         * This must be done to release references from the tree data to our
         * internal BranchHandler data structures. Failure to do so would mean
         * that our BranchHandler objects would remain in scope as long as the
         * tree data remained in scope, even if we were no longer using the
         * BranchHandler objects.
         */
        @SuppressWarnings("unchecked")
        public void release() {
            ((List<Object>)branchData).getListListeners().remove(this);

            // Recursively have all child branches unregister interest
            for (int i = 0, n = getLength(); i < n; i++) {
                BranchHandler branchHandler = get(i);

                if (branchHandler != null) {
                    branchHandler.release();
                }
            }
        }

        /**
         * Gets the path that leads from the root of the tree data to this
         * branch. Note: <tt>rootBranchHandler.getPath()</tt> will return and
         * empty sequence.
         */
        @SuppressWarnings("unchecked")
        private Path getPath() {
            Path path = new Path();

            BranchHandler handler = this;

            while (handler.parent != null) {
                int index = ((List<Object>)handler.parent.branchData).indexOf(handler.branchData);
                path.insert(index, 0);

                handler = handler.parent;
            }

            return path;
        }

        @Override
        public void itemInserted(List<Object> list, int index) {
            Path path = getPath();

            // Insert child handler placeholder (lazily loaded)
            insert(null, index);

            // Update our data structures
            incrementPaths(expandedPaths, path, index);
            int updated = incrementPaths(selectedPaths, path, index);
            incrementPaths(checkedPaths, path, index);

            // Notify listeners
            treeViewNodeListeners.nodeInserted(TreeView.this, path, index);

            if (updated > 0) {
                treeViewSelectionListeners.selectedPathsChanged(TreeView.this, getSelectedPaths());
            }
        }

        @Override
        public void itemsRemoved(List<Object> list, int index, Sequence<Object> items) {
            Path path = getPath();

            Path previousSelectedPath;
            if (selectMode == SelectMode.SINGLE
                && selectedPaths.getLength() > 0) {
                previousSelectedPath = selectedPaths.get(0);
            } else {
                previousSelectedPath = null;
            }

            // Remove child handlers
            int count = items.getLength();
            Sequence<BranchHandler> removed = remove(index, count);

            // Release each child handler that was removed
            for (int i = 0, n = removed.getLength(); i < n; i++) {
                BranchHandler handler = removed.get(i);

                if (handler != null) {
                    handler.release();
                }
            }

            // Update our data structures
            clearAndDecrementPaths(expandedPaths, path, index, count);
            int updated = clearAndDecrementPaths(selectedPaths, path, index, count);
            clearAndDecrementPaths(checkedPaths, path, index, count);

            // Notify listeners
            treeViewNodeListeners.nodesRemoved(TreeView.this, path, index, count);

            if (updated > 0) {
                treeViewSelectionListeners.selectedPathsChanged(TreeView.this, getSelectedPaths());

                if (selectMode == SelectMode.SINGLE
                    && !getSelectedPath().equals(previousSelectedPath)) {
                    treeViewSelectionListeners.selectedNodeChanged(TreeView.this, null);
                }
            }
        }

        @Override
        public void itemUpdated(List<Object> list, int index, Object previousItem) {
            Path path = getPath();

            if (list.get(index) != previousItem) {
                // Release child handler
                BranchHandler handler = update(index, null);

                if (handler != null) {
                    handler.release();
                }

                // Update our data structures
                clearPaths(expandedPaths, path, index);
                clearPaths(selectedPaths, path, index);
                clearPaths(checkedPaths, path, index);
            }

            // Notify listeners
            treeViewNodeListeners.nodeUpdated(TreeView.this, path, index);
        }

        @Override
        public void listCleared(List<Object> list) {
            Path path = getPath();

            // Release each child handler
            for (int i = 0, n = getLength(); i < n; i++) {
                BranchHandler handler = get(i);

                if (handler != null) {
                    handler.release();
                }
            }

            // Remove child handlers
            clear();

            // Update our data structures
            clearPaths(expandedPaths, path);
            int cleared = clearPaths(selectedPaths, path);
            clearPaths(checkedPaths, path);

            // Notify listeners
            treeViewNodeListeners.nodesCleared(TreeView.this, path);

            if (cleared > 0) {
                treeViewSelectionListeners.selectedPathsChanged(TreeView.this, getSelectedPaths());

                if (selectMode == SelectMode.SINGLE) {
                    treeViewSelectionListeners.selectedNodeChanged(TreeView.this, null);
                }
            }
        }

        @Override
        public void comparatorChanged(List<Object> list,
            Comparator<Object> previousComparator) {
            if (list.getComparator() != null) {
                Path path = getPath();

                // Release all child handlers. This is safe because of the
                // calls to clearPaths(). Failure to do this would result in
                // the indices of our child handlers not matching those of the
                // backing data structures, which would yield very hard to find
                // bugs
                for (int i = 0, n = getLength(); i < n; i++) {
                    BranchHandler handler = update(i, null);

                    if (handler != null) {
                        handler.release();
                    }
                }

                // Update our data structures
                clearPaths(expandedPaths, path);
                int cleared = clearPaths(selectedPaths, path);
                clearPaths(checkedPaths, path);

                // Notify listeners
                treeViewNodeListeners.nodesSorted(TreeView.this, path);

                if (cleared > 0) {
                    treeViewSelectionListeners.selectedPathsChanged(TreeView.this, getSelectedPaths());

                    if (selectMode == SelectMode.SINGLE) {
                        treeViewSelectionListeners.selectedNodeChanged(TreeView.this, null);
                    }
                }
            }
        }

        /**
         * Updates the paths within the specified sequence in response to a tree
         * data path insertion.  For instance, if <tt>paths</tt> is
         * <tt>[[3, 0], [5, 0]]</tt>, <tt>basePath</tt> is <tt>[]</tt>, and
         * <tt>index</tt> is <tt>4</tt>, then <tt>paths</tt> will be updated to
         * <tt>[[3, 0], [6, 0]]</tt>. No events are fired.
         *
         * @param paths
         * Sequence of paths guaranteed to be sorted by "row order".
         *
         * @param basePath
         * The path to the parent of the inserted item.
         *
         * @param index
         * The index of the inserted item within its parent.
         */
        private int incrementPaths(ArrayList<Path> paths, Path basePath, int index) {
            // Calculate the child's path
            Path childPath = new Path(basePath);
            childPath.add(index);

            // Find the child path's place in our sorted paths sequence
            int i = ArrayList.binarySearch(paths, childPath, PATH_COMPARATOR);
            if (i < 0) {
                i = -(i + 1);
            }

            // Temporarily clear the comparator while we update the list
            paths.setComparator(null);

            int n = paths.getLength();
            try {
                // Update all affected paths by incrementing the appropriate path element
                for (int depth = basePath.getLength(); i < n; i++) {
                    Path affectedPath = paths.get(i);

                    if (!Sequence.Tree.isDescendant(basePath, affectedPath)) {
                        // All paths from here forward are guaranteed to be unaffected
                        break;
                    }

                    Integer[] elements = affectedPath.toArray();
                    elements[depth]++;
                    paths.update(i, new ImmutablePath(elements));
                }
            } finally {
                // Restore the comparator
                paths.setComparator(PATH_COMPARATOR);
            }

            return (n - i);
        }

        /**
         * Updates the paths within the specified sequence in response to items
         * having been removed from the base path. For instance, if
         * <tt>paths</tt> is <tt>[[3, 0], [3, 1], [6, 0]]</tt>,
         * <tt>basePath</tt> is <tt>[]</tt>, <tt>index</tt> is <tt>3</tt>, and
         * <tt>count</tt> is <tt>2</tt>, then <tt>paths</tt> will be updated to
         * <tt>[[4, 0]]</tt>. No events are fired.
         *
         * @param paths
         * Sequence of paths guaranteed to be sorted by "row order".
         *
         * @param basePath
         * The path to the parent of the removed items.
         *
         * @param index
         * The index of the first removed item within the base.
         *
         * @param count
         * The number of items removed.
         */
        private int clearAndDecrementPaths(ArrayList<Path> paths, Path basePath, int index,
            int count) {
            int depth = basePath.getLength();

            // Find the index of the first path to clear (inclusive)
            Path testPath = new Path(basePath);
            testPath.add(index);

            int start = ArrayList.binarySearch(paths, testPath, PATH_COMPARATOR);
            if (start < 0) {
                start = -(start + 1);
            }

            // Find the index of the last path to clear (exclusive)
            testPath.update(depth, index + count);

            int end = ArrayList.binarySearch(paths, testPath, PATH_COMPARATOR);
            if (end < 0) {
                end = -(end + 1);
            }

            // Clear affected paths
            if (end > start) {
                paths.remove(start, end - start);
            }

            // Decrement paths as necessary
            int n = paths.getLength();
            for (int i = start; i < n; i++) {
                Path affectedPath = paths.get(i);

                if (!Sequence.Tree.isDescendant(basePath, affectedPath)) {
                    // All paths from here forward are guaranteed to be unaffected
                    break;
                }

                Integer[] elements = affectedPath.toArray();
                elements[depth] -= count;
                paths.update(i, new ImmutablePath(elements));
            }

            return (n - start);
        }

        /**
         * Removes affected paths from within the specified sequence in response
         * to an item having been updated in the base path.  For instance, if
         * <tt>paths</tt> is <tt>[[3], [3, 0], [3, 1], [5, 0]]</tt>,
         * <tt>basePath</tt> is <tt>[3]</tt>, and <tt>index</tt> is <tt>0</tt>,
         * then <tt>paths</tt> will be updated to
         * <tt>[[3], [3, 1], [5, 0]]</tt>. No events are fired.
         *
         * @param paths
         * Sequence of paths guaranteed to be sorted by "row order".
         *
         * @param basePath
         * The path to the parent of the updated item.
         *
         * @param index
         * The index of the updated item within its parent.
         */
        private void clearPaths(ArrayList<Path> paths, Path basePath, int index) {
            // Calculate the child's path
            Path childPath = new Path(basePath);
            childPath.add(index);

            // Find the child path's place in our sorted paths sequence
            int clearIndex = ArrayList.binarySearch(paths, childPath, PATH_COMPARATOR);
            if (clearIndex < 0) {
                clearIndex = -(clearIndex + 1);
            }

            // Remove the child and all descendants from the paths list
            for (int i = clearIndex, n = paths.getLength(); i < n; i++) {
                Path affectedPath = paths.get(clearIndex);

                if (!Sequence.Tree.isDescendant(childPath, affectedPath)) {
                    break;
                }

                paths.remove(clearIndex, 1);
            }
        }

        /**
         * Removes affected paths from within the specified sequence in response
         * to a base path having been sorted.  For instance, if <tt>paths</tt>
         * is <tt>[[3], [3, 0], [3, 1], [5, 0]]</tt> and <tt>basePath</tt> is
         * <tt>[3]</tt>, then <tt>paths</tt> will be updated to
         * <tt>[[3], [5, 0]]</tt>. No events are fired.
         *
         * @param paths
         * Sequence of paths guaranteed to be sorted by "row order".
         *
         * @param basePath
         * The path whose children were sorted.
         */
        private int clearPaths(ArrayList<Path> paths, Path basePath) {
            // Find first descendant in paths list, if it exists
            int index = ArrayList.binarySearch(paths, basePath, PATH_COMPARATOR);
            index = (index < 0 ? -(index + 1) : index + 1);

            // Remove all descendants from the paths list
            int n = paths.getLength();
            for (int i = index; i < n; i++) {
                Path affectedPath = paths.get(index);

                if (!Sequence.Tree.isDescendant(basePath, affectedPath)) {
                    break;
                }

                paths.remove(index, 1);
            }

            return (n - index);
        }
    }

    // Core data model
    private List<?> treeData = null;

    // Ancillary data models
    private ArrayList<Path> expandedPaths = new ArrayList<Path>(PATH_COMPARATOR);
    private ArrayList<Path> selectedPaths = new ArrayList<Path>(PATH_COMPARATOR);
    private ArrayList<Path> checkedPaths = new ArrayList<Path>(PATH_COMPARATOR);

    // Properties
    private SelectMode selectMode = SelectMode.SINGLE;
    private boolean checkmarksEnabled = false;
    private boolean showMixedCheckmarkState = false;

    // Filters
    private Filter<?> disabledNodeFilter = null;
    private Filter<?> disabledCheckmarkFilter = null;

    // Handlers
    private BranchHandler rootBranchHandler;

    // Renderer & editor
    private NodeRenderer nodeRenderer = DEFAULT_NODE_RENDERER;
    private NodeEditor nodeEditor = null;

    // Listener lists
    private TreeViewListenerList treeViewListeners = new TreeViewListenerList();
    private TreeViewBranchListenerList treeViewBranchListeners =
        new TreeViewBranchListenerList();
    private TreeViewNodeListenerList treeViewNodeListeners =
        new TreeViewNodeListenerList();
    private TreeViewNodeStateListenerList treeViewNodeStateListeners =
        new TreeViewNodeStateListenerList();
    private TreeViewSelectionListenerList treeViewSelectionListeners =
        new TreeViewSelectionListenerList();

    private static final NodeRenderer DEFAULT_NODE_RENDERER = new TreeViewNodeRenderer();

    private static final Comparator<Path> PATH_COMPARATOR = new PathComparator();

    /**
     * Creates a new <tt>TreeView</tt> with empty tree data.
     */
    public TreeView() {
        this(new ArrayList<Object>());
    }

    /**
     * Creates a new <tt>TreeView</tt> with the specified tree data.
     *
     * @param treeData
     * Default data set to be used with the tree. This list represents the root
     * set of items displayed by the tree and will never itself be painted.
     * Sub-items that also implement the <tt>List</tt> interface are considered
     * branches; other items are considered leaves.
     *
     * @see #setTreeData(List)
     */
    public TreeView(List<?> treeData) {
        setTreeData(treeData);
        installSkin(TreeView.class);
    }

    /**
     * Sets the skin, replacing any previous skin. This ensures that the skin
     * being set implements the {@link TreeView.Skin} interface.
     *
     * @param skin
     * The new skin.
     */
    @Override
    protected void setSkin(org.apache.pivot.wtk.Skin skin) {
        if (!(skin instanceof TreeView.Skin)) {
            throw new IllegalArgumentException("Skin class must implement "
                + TreeView.Skin.class.getName());
        }

        super.setSkin(skin);
    }

    /**
     * Returns the tree view's data model. This list represents the root
     * set of items displayed by the tree and will never itself be painted.
     * Sub-items that also implement the <tt>List</tt> interface are considered
     * branches; other items are considered leaves.
     * <p>
     * For instance, a tree view that displays a single root branch would be
     * backed by list with one child (also a list).
     *
     * @return
     * The tree view's data model.
     */
    public List<?> getTreeData() {
        return treeData;
    }

    /**
     * Sets the tree data. Note that it is the responsibility of the
     * caller to ensure that the current tree node renderer is capable of
     * displaying the contents of the tree structure. By default, an instance
     * of {@link TreeViewNodeRenderer} is used.
     * <p>
     * When the tree data is changed, the state of all nodes (expansion,
     * selection, and checked) will be cleared since the nodes themselves are
     * being replaced. Note that corresponding events will <b>not</b> be fired,
     * since these actions are implied by the
     * {@link TreeViewListener#treeDataChanged(TreeView,List) treeDataChanged}
     * event.
     *
     * @param treeData
     * The data to be presented by the tree.
     */
    public void setTreeData(List<?> treeData) {
        if (treeData == null) {
            throw new IllegalArgumentException("treeData is null.");
        }

        List<?> previousTreeData = this.treeData;

        if (previousTreeData != treeData) {
            int cleared;
            if (previousTreeData != null) {
                // Reset our data models
                expandedPaths.clear();
                cleared = selectedPaths.getLength();
                selectedPaths.clear();
                checkedPaths.clear();

                // Release our existing branch handlers
                rootBranchHandler.release();
            } else {
                cleared = 0;
            }

            // Update our root branch handler
            rootBranchHandler = new BranchHandler(null, treeData);

            // Update the tree data
            this.treeData = treeData;

            // Notify listeners
            treeViewListeners.treeDataChanged(this, previousTreeData);

            if (cleared > 0) {
                treeViewSelectionListeners.selectedPathsChanged(TreeView.this, getSelectedPaths());

                if (selectMode == SelectMode.SINGLE) {
                    treeViewSelectionListeners.selectedNodeChanged(TreeView.this, null);
                }
            }
        }
    }

    /**
     * Gets the tree view's node renderer, which is responsible for the
     * appearance of the node data. As such, note that there is an implied
     * coordination between the node renderer and the data model. The default
     * node renderer used is an instance of <tt>TreeViewNodeRenderer</tt>.
     *
     * @return
     * The current node renderer.
     *
     * @see TreeViewNodeRenderer
     */
    public NodeRenderer getNodeRenderer() {
        return nodeRenderer;
    }

    /**
     * Sets the tree view's node renderer, which is responsible for the
     * appearance of the node data.
     *
     * @param nodeRenderer
     * The new node renderer.
     */
    public void setNodeRenderer(NodeRenderer nodeRenderer) {
        if (nodeRenderer == null) {
            throw new IllegalArgumentException("nodeRenderer is null.");
        }

        NodeRenderer previousNodeRenderer = this.nodeRenderer;

        if (previousNodeRenderer != nodeRenderer) {
            this.nodeRenderer = nodeRenderer;
            treeViewListeners.nodeRendererChanged(this, previousNodeRenderer);
        }
    }

    /**
     * Returns the editor used to edit nodes in this tree.
     *
     * @return
     * The node editor, or <tt>null</tt> if no editor is installed.
     */
    public NodeEditor getNodeEditor() {
        return nodeEditor;
    }

    /**
     * Sets the editor used to edit nodes in this tree.
     *
     * @param nodeEditor
     * The node editor for the tree.
     */
    public void setNodeEditor(NodeEditor nodeEditor) {
        NodeEditor previousNodeEditor = this.nodeEditor;

        if (previousNodeEditor != nodeEditor) {
            this.nodeEditor = nodeEditor;
            treeViewListeners.nodeEditorChanged(this, previousNodeEditor);
        }
    }

    /**
     * Returns the current selection mode.
     *
     * @return
     * The current selection mode.
     */
    public SelectMode getSelectMode() {
        return selectMode;
    }

    /**
     * Sets the selection mode. Clears the selection if the mode has changed.
     * Note that if the selection is cleared, selection listeners will not
     * be notified, as the clearing of the selection is implied by the
     * {@link TreeViewListener#selectModeChanged(TreeView,TreeView.SelectMode)
     * selectModeChanged} event.
     *
     * @param selectMode
     * The new selection mode.
     *
     * @see
     * TreeViewListener
     *
     * @see
     * TreeViewSelectionListener
     */
    public void setSelectMode(SelectMode selectMode) {
        if (selectMode == null) {
            throw new IllegalArgumentException("selectMode is null");
        }

        SelectMode previousSelectMode = this.selectMode;

        if (selectMode != previousSelectMode) {
            // Clear any current selection
            selectedPaths.clear();

            // Update the selection mode
            this.selectMode = selectMode;

            // Fire select mode change event
            treeViewListeners.selectModeChanged(this, previousSelectMode);
        }
    }

    /**
     * Returns the currently selected paths.
     *
     * @return
     * An immutable list containing the currently selected paths. Note that the returned
     * list is a wrapper around the actual selection, not a copy. Any changes made to the
     * selection state will be reflected in the list, but events will not be fired.
     */
    public ImmutableList<Path> getSelectedPaths() {
        return new ImmutableList<Path>(selectedPaths);
    }

    /**
     *
     *
     * @throws IllegalStateException
     * If selection has been disabled (select mode <tt>NONE</tt>).
     */
    public Sequence<Path> setSelectedPaths(Sequence<Path> selectedPaths) {
        if (selectedPaths == null) {
            throw new IllegalArgumentException("selectedPaths is null.");
        }

        if (selectMode == SelectMode.NONE) {
            throw new IllegalStateException("Selection is not enabled.");
        }

        if (selectMode == SelectMode.SINGLE
            && selectedPaths.getLength() > 1) {
            throw new IllegalArgumentException("Selection length is greater than 1.");
        }

        Sequence<Path> previousSelectedPaths = this.selectedPaths;
        Object previousSelectedNode = (selectMode == SelectMode.SINGLE) ? getSelectedNode() : null;

        // TODO Only add and monitor non-duplicates

        if (selectedPaths != previousSelectedPaths) {
            this.selectedPaths = new ArrayList<Path>(PATH_COMPARATOR);

            for (int i = 0, n = selectedPaths.getLength(); i < n; i++) {
                Path path = selectedPaths.get(i);

                // Monitor the path's parent
                monitorBranch(new Path(path, path.getLength() - 1));

                // Update the selection
                this.selectedPaths.add(new ImmutablePath(path));
            }

            // Notify listeners
            treeViewSelectionListeners.selectedPathsChanged(this, previousSelectedPaths);

            if (selectMode == SelectMode.SINGLE) {
                treeViewSelectionListeners.selectedNodeChanged(TreeView.this, previousSelectedNode);
            }
        }

        return getSelectedPaths();
    }

    /**
     * Returns the first selected path, as it would appear in a fully expanded
     * tree.
     *
     * @return
     * The first selected path, or <tt>null</tt> if nothing is selected.
     */
    public Path getFirstSelectedPath() {
        return (selectedPaths.getLength() > 0 ? selectedPaths.get(0) : null);
    }

    /**
     * Returns the last selected path, as it would appear in a fully expanded
     * tree.
     *
     * @return
     * The last selected path, or <tt>null</tt> if nothing is selected.
     */
    public Path getLastSelectedPath() {
        return (selectedPaths.getLength() > 0
            ? selectedPaths.get(selectedPaths.getLength() - 1) : null);
    }

    /**
     * Returns the currently selected index, even when in multi-select mode.
     *
     * @return
     * The selected path, or <tt>null</tt> if nothing is selected.
     */
    public Path getSelectedPath() {
        return getFirstSelectedPath();
    }

    /**
     */
    public void setSelectedPath(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        if (path.getLength() == 0) {
            throw new IllegalArgumentException("path is empty.");
        }

        setSelectedPaths(new ArrayList<Path>(path));
    }

    /**
     *
     *
     * @return
     * The selected object, or <tt>null</tt> if nothing is selected. Note that
     * technically, the selected path could be backed by a <tt>null</tt> data
     * value. If the caller wishes to distinguish between these cases, they can
     * use <tt>getSelectedPath()</tt> instead.
     */
    public Object getSelectedNode() {
        Path path = getSelectedPath();
        Object node = null;

        if (path != null) {
            node = Sequence.Tree.get(treeData, path);
        }

        return node;
    }

    /**
     * Adds a path to the selection.
     *
     * @param path
     *
     * @return
     * <tt>true</tt> if the path was added to the selection; <tt>false</tt>,
     * otherwise.
     *
     * @throws IllegalStateException
     * If multi-select is not enabled.
     */
    public boolean addSelectedPath(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        if (path.getLength() == 0) {
            throw new IllegalArgumentException("path is empty.");
        }

        if (selectMode != SelectMode.MULTI) {
            throw new IllegalStateException("Tree view is not in multi-select mode.");
        }

        int index = selectedPaths.indexOf(path);
        if (index < 0) {
            // Monitor the path's parent
            monitorBranch(new Path(path, path.getLength() - 1));

            // Update the selection
            selectedPaths.add(new ImmutablePath(path));

            // Notify listeners
            treeViewSelectionListeners.selectedPathAdded(this, path);
            treeViewSelectionListeners.selectedPathsChanged(this, null);
        }

        return (index < 0);
    }

    /**
     * Removes a path from the selection.
     *
     * @param path
     *
     * @return
     * <tt>true</tt> if the path was added to the selection; <tt>false</tt>,
     * otherwise.
     *
     * @throws IllegalStateException
     * If multi-select is not enabled.
     */
    public boolean removeSelectedPath(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        if (path.getLength() == 0) {
            throw new IllegalArgumentException("path is empty.");
        }

        if (selectMode != SelectMode.MULTI) {
            throw new IllegalStateException("Tree view is not in multi-select mode.");
        }

        int index = selectedPaths.indexOf(path);
        if (index >= 0) {
            // Update the selection
            selectedPaths.remove(index, 1);

            // Notify listeners
            treeViewSelectionListeners.selectedPathRemoved(this, path);
            treeViewSelectionListeners.selectedPathsChanged(this, null);
        }

        return (index >= 0);
    }

    /**
     */
    public void clearSelection() {
        if (selectedPaths.getLength() > 0) {
            Sequence<Path> previousSelectedPaths = selectedPaths;

            // Update the selection
            selectedPaths = new ArrayList<Path>(PATH_COMPARATOR);

            // Notify listeners
            treeViewSelectionListeners.selectedPathsChanged(this, previousSelectedPaths);
        }
    }

    /**
     */
    public boolean isNodeSelected(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        return (selectedPaths.indexOf(path) >= 0);
    }

    /**
     * Returns the disabled state of a given node.
     *
     * @param path
     * The path to the node whose disabled state is to be tested
     *
     * @return
     * <tt>true</tt> if the node is disabled; <tt>false</tt>,
     * otherwise
     */
    @SuppressWarnings("unchecked")
    public boolean isNodeDisabled(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        boolean disabled = false;

        if (disabledNodeFilter != null) {
            Object node = Sequence.Tree.get(treeData, path);
            disabled = ((Filter<Object>)disabledNodeFilter).include(node);
        }

        return disabled;
    }

    /**
     * Returns the disabled node filter, which determines the disabled state of
     * all nodes. Disabled nodes are not interactive to the user. Note, however,
     * that disabled nodes may still be expanded, selected, and checked
     * <i>programatically</i>. A disabled node may have enabled children.
     * <p>
     * If the disabled node filter is set to <tt>null</tt>, all nodes are
     * enabled.
     *
     * @return
     * The disabled node filter, or <tt>null</tt> if no disabled node filter is
     * set
     */
    public Filter<?> getDisabledNodeFilter() {
        return disabledNodeFilter;
    }

    /**
     * Sets the disabled node filter, which determines the disabled state of
     * all nodes. Disabled nodes are not interactive to the user. Note, however,
     * that disabled nodes may still be expanded, selected, and checked
     * <i>programatically</i>. A disabled node may have enabled children.
     * <p>
     * If the disabled node filter is set to <tt>null</tt>, all nodes are
     * enabled.
     *
     * @param disabledNodeFilter
     * The disabled node filter, or <tt>null</tt> for no disabled node filter
     */
    public void setDisabledNodeFilter(Filter<?> disabledNodeFilter) {
        Filter<?> previousDisabledNodeFilter = this.disabledNodeFilter;

        if (previousDisabledNodeFilter != disabledNodeFilter) {
            this.disabledNodeFilter = disabledNodeFilter;
            treeViewListeners.disabledNodeFilterChanged(this, previousDisabledNodeFilter);
        }
    }

    /**
     */
    public boolean getCheckmarksEnabled() {
        return checkmarksEnabled;
    }

    /**
     * Enables or disables checkmarks. If checkmarks are being disabled, all
     * checked nodes will be automatically unchecked. Note that the
     * corresponding event will <b>not</b> be fired, since the clearing of
     * existing checkmarks is implied by the
     * {@link TreeViewListener#checkmarksEnabledChanged(TreeView)
     * checkmarksEnabledChanged} event.
     *
     * @param checkmarksEnabled
     * <tt>true</tt> to enable checkmarks; <tt>false</tt> to disable them.
     */
    public void setCheckmarksEnabled(boolean checkmarksEnabled) {
        if (this.checkmarksEnabled != checkmarksEnabled) {
            // Clear any current check state
            checkedPaths.clear();

            // Update the checkmark mode
            this.checkmarksEnabled = checkmarksEnabled;

            // Fire checkmarks enabled change event
            treeViewListeners.checkmarksEnabledChanged(this);
        }
    }

    /**
     * Tells whether or not the mixed check state will be reported by this
     * tree view. This state is a derived state meaning "the node is not
     * checked, but one or more of its descendants are." When this state is
     * configured to not be shown, such nodes will simply be reported as
     * unchecked.
     *
     * @return
     * <tt>true</tt> if the tree view will report so-called mixed nodes as
     * mixed; <tt>false</tt> if it will report them as unchecked.
     *
     * @see
     * NodeCheckState#MIXED
     */
    public boolean getShowMixedCheckmarkState() {
        return showMixedCheckmarkState;
    }

    /**
     * Sets whether or not the "mixed" check state will be reported by this
     * tree view. This state is a derived state meaning "the node is not
     * checked, but one or more of its descendants are." When this state is
     * configured to not be shown, such nodes will simply be reported as
     * unchecked.
     * <p>
     * Changing this flag may result in some nodes changing their reported
     * check state. Note that the corresponding <tt>nodeCheckStateChanged</tt>
     * events will <b>not</b> be fired, since the possibility of such a change
     * in check state is implied by the
     * {@link TreeViewListener#showMixedCheckmarkStateChanged(TreeView)
     * showMixedCheckmarkStateChanged} event.
     *
     * @param showMixedCheckmarkState
     * <tt>true</tt> to show the derived mixed state; <tt>false</tt> to report
     * so-called "mixed" nodes as unchecked.
     *
     * @see
     * NodeCheckState#MIXED
     */
    public void setShowMixedCheckmarkState(boolean showMixedCheckmarkState) {
        if (this.showMixedCheckmarkState != showMixedCheckmarkState) {
            // Update the flag
            this.showMixedCheckmarkState = showMixedCheckmarkState;

            // Notify listeners
            treeViewListeners.showMixedCheckmarkStateChanged(this);
        }
    }

    /**
     * Tells whether or not the node at the specified path is checked. If
     * checkmarks are not enabled, this is guaranteed to be <tt>false</tt>. So
     * called mixed nodes will always be reported as unchecked in this method.
     *
     * @param path
     * The path to the node.
     *
     * @return
     * <tt>true</tt> if the node is explicitly checked; <tt>false</tt> otherwise.
     *
     * @see
     * #getCheckmarksEnabled()
     */
    public boolean isNodeChecked(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        return (checkedPaths.indexOf(path) >= 0);
    }

    /**
     * Returns the checkmark state of the node at the specified path. If
     * checkmarks are not enabled, this is guaranteed to be <tt>UNCHECKED</tt>.
     * <p>
     * Note that the <tt>MIXED</tt> check state (meaning "the node is not
     * checked, but one or more of its descendants are") is only reported when
     * the tree view is configured as such. Otherwise, such nodes will be
     * reported as <tt>UNCHECKED</tt>.
     *
     * @param path
     * The path to the node.
     *
     * @return
     * The checkmark state of the specified node.
     *
     * @see
     * #getCheckmarksEnabled()
     *
     * @see
     * #setShowMixedCheckmarkState(boolean)
     */
    public NodeCheckState getNodeCheckState(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        NodeCheckState checkState = NodeCheckState.UNCHECKED;

        if (checkmarksEnabled) {
            int index = ArrayList.binarySearch(checkedPaths, path, PATH_COMPARATOR);

            if (index >= 0) {
                checkState = NodeCheckState.CHECKED;
            } else if (showMixedCheckmarkState) {
                // Translate to the insertion index
                index = -(index + 1);

                if (index < checkedPaths.getLength()) {
                    Path nextCheckedPath = checkedPaths.get(index);

                    if (Sequence.Tree.isDescendant(path, nextCheckedPath)) {
                        checkState = NodeCheckState.MIXED;
                    }
                }
            }
        }

        return checkState;
    }

    /**
     * Sets the check state of the node at the specified path. If the node
     * already has the specified check state, nothing happens.
     * <p>
     * Note that it is impossible to set the check state of a node to
     * <tt>MIXED</tt>. This is because the mixed check state is a derived state
     * meaning "the node is not checked, but one or more of its descendants
     * are."
     *
     * @param path
     * The path to the node.
     *
     * @param checked
     * <tt>true</tt> to check the node; <tt>false</tt> to uncheck it.
     *
     * @throws IllegalStateException
     * If checkmarks are not enabled (see {@link #getCheckmarksEnabled()}).
     *
     * @see
     * NodeCheckState#MIXED
     */
    public void setNodeChecked(Path path, boolean checked) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        if (path.getLength() == 0) {
            throw new IllegalArgumentException("path is empty.");
        }

        if (!checkmarksEnabled) {
            throw new IllegalStateException("Checkmarks are not enabled.");
        }

        int index = checkedPaths.indexOf(path);

        if ((index < 0 && checked)
            || (index >= 0 && !checked)) {
            NodeCheckState previousCheckState = getNodeCheckState(path);

            Sequence<NodeCheckState> ancestorCheckStates = null;

            if (showMixedCheckmarkState) {
                // Record the check states of our ancestors before we change
                // anything so we know which events to fire after we're done
                ancestorCheckStates = new ArrayList<NodeCheckState>(path.getLength() - 1);

                Path ancestorPath = new Path(path, path.getLength() - 1);

                for (int i = ancestorPath.getLength() - 1; i >= 0; i--) {
                    ancestorCheckStates.insert(getNodeCheckState(ancestorPath), 0);

                    ancestorPath.remove(i, 1);
                }
            }

            if (checked) {
                // Monitor the path's parent
                monitorBranch(new Path(path, path.getLength() - 1));

                // Update the checked paths
                checkedPaths.add(new ImmutablePath(path));
            } else {
                // Update the checked paths
                checkedPaths.remove(index, 1);
            }

            // Notify listeners
            treeViewNodeStateListeners.nodeCheckStateChanged(this, path, previousCheckState);

            if (showMixedCheckmarkState) {
                // Notify listeners of any changes to our ancestors' check states
                Path ancestorPath = new Path(path, path.getLength() - 1);

                for (int i = ancestorPath.getLength() - 1; i >= 0; i--) {
                    @SuppressWarnings("null")  // false warning from eclipse
                    NodeCheckState ancestorPreviousCheckState = ancestorCheckStates.get(i);
                    NodeCheckState ancestorCheckState = getNodeCheckState(ancestorPath);

                    if (ancestorCheckState != ancestorPreviousCheckState) {
                        treeViewNodeStateListeners.nodeCheckStateChanged
                            (this, ancestorPath, ancestorPreviousCheckState);
                    }

                    ancestorPath.remove(i, 1);
                }
            }
        }
    }

    /**
     * Gets the sequence of node paths that are checked. If checkmarks are not
     * enabled (see {@link #getCheckmarksEnabled()}), this is guaranteed to
     * return an empty sequence.
     * <p>
     * Note that if the tree view is configured to show mixed checkmark states
     * (see {@link #getShowMixedCheckmarkState()}), this will still only return
     * the nodes that are fully checked.
     *
     * @return
     * The paths to the checked nodes in the tree, guaranteed to be
     * non-<tt>null</tt>.
     */
    public Sequence<Path> getCheckedPaths() {
        return new ImmutableList<Path>(checkedPaths);
    }

    /**
     * Returns the disabled checkmark filter, which determines which checkboxes
     * are interactive and which are not. Note that this filter only affects
     * user interaction; nodes may still be checked programatically despite
     * their inclusion in this filter. If this filter is set to <tt>null</tt>,
     * all checkboxes will be interactive.
     * <p>
     * <b>Note:</b> this filter is only relavent if
     * {@link #setCheckmarksEnabled(boolean) checkmarksEnabled} is set to true.
     *
     * @return
     * The disabled checkmark filter, or <tt>null</tt> if no disabled checkmark
     * filter is set
     */
    public Filter<?> getDisabledCheckmarkFilter() {
        return disabledCheckmarkFilter;
    }

    /**
     * Sets the disabled checkmark filter, which determines which checkboxes
     * are interactive and which are not. Note that this filter only affects
     * user interaction; nodes may still be checked programatically despite
     * their inclusion in this filter. If this filter is set to <tt>null</tt>,
     * all checkboxes will be interactive.
     * <p>
     * <b>Note:</b> this filter is only relavent if
     * {@link #setCheckmarksEnabled(boolean) checkmarksEnabled} is set to true.
     * enabled.
     *
     * @param disabledCheckmarkFilter
     * The disabled checkmark filter, or <tt>null</tt> for no disabled
     * checkmark filter
     */
    public void setDisabledCheckmarkFilter(Filter<?> disabledCheckmarkFilter) {
        Filter<?> previousDisabledCheckmarkFilter = this.disabledCheckmarkFilter;

        if (previousDisabledCheckmarkFilter !=disabledCheckmarkFilter ) {
            this.disabledCheckmarkFilter = disabledCheckmarkFilter;
            treeViewListeners.disabledCheckmarkFilterChanged(this, previousDisabledCheckmarkFilter);
        }
    }

    /**
     * Sets the expansion state of the specified branch. If the branch already
     * has the specified expansion state, nothing happens.
     *
     * @param path
     * The path to the branch node.
     *
     * @param expanded
     * <tt>true</tt> to expand the branch; <tt>false</tt> to collapse it.
     */
    public void setBranchExpanded(Path path, boolean expanded) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        if (path.getLength() == 0) {
            throw new IllegalArgumentException("path is empty.");
        }

        int index = expandedPaths.indexOf(path);

        if (expanded && index < 0) {
            // Monitor the branch
            monitorBranch(path);

            // Update the expanded paths
            expandedPaths.add(new ImmutablePath(path));

            // Notify listeners
            treeViewBranchListeners.branchExpanded(this, path);
        } else if (!expanded && index >= 0) {
            // Update the expanded paths
            expandedPaths.remove(index, 1);

            // Notify listeners
            treeViewBranchListeners.branchCollapsed(this, path);
        }
    }

    /**
     * Tells whether or not the specified branch is expanded.
     *
     * @param path
     * The path to the branch node.
     *
     * @return
     * <tt>true</tt> if the branch is expanded; <tt>false</tt> otherwise.
     */
    public boolean isBranchExpanded(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        return (expandedPaths.indexOf(path) >= 0);
    }

    /**
     * Expands the branch at the specified path. If the branch is already
     * expanded, nothing happens.
     *
     * @param path
     * The path to the branch node.
     */
    public final void expandBranch(Path path) {
        setBranchExpanded(path, true);
    }

    /**
     * Expands all branches in the tree view.
     */
    @SuppressWarnings("unchecked")
    public final void expandAll() {
        Sequence.Tree.ItemIterator<Object> itemIterator =
            Sequence.Tree.depthFirstIterator((List<Object>)treeData);

        while (itemIterator.hasNext()) {
            Object node = itemIterator.next();

            if (node instanceof List<?>) {
                Path path = itemIterator.getPath();

                if (path.getLength() > 0) {
                    expandBranch(path);
                }
            }
        }
    }

    /**
     * Collapses the branch at the specified path. If the branch is already
     * collapsed, nothing happens.
     *
     * @param path
     * The path to the branch node.
     */
    public final void collapseBranch(Path path) {
        setBranchExpanded(path, false);
    }

    /**
     * Collapses all branches in the tree view.
     */
    @SuppressWarnings("unchecked")
    public final void collapseAll() {
        Sequence.Tree.ItemIterator<Object> itemIterator =
            Sequence.Tree.depthFirstIterator((List<Object>)treeData);

        while (itemIterator.hasNext()) {
            Object node = itemIterator.next();

            if (node instanceof List<?>) {
                Path path = itemIterator.getPath();

                if (path.getLength() > 0) {
                    collapseBranch(path);
                }
            }
        }
    }

    /**
     * Ensures that this tree view is listening for list events on every branch
     * node along the specified path.
     *
     * @param path
     * A path leading to a nested branch node.
     *
     * @throws IndexOutOfBoundsException
     * If a path element is out of bounds.
     *
     * @throws IllegalArgumentException
     * If the path contains any leaf nodes.
     */
    private void monitorBranch(Path path) {
        BranchHandler parent = rootBranchHandler;

        for (int i = 0, n = path.getLength(); i < n; i++) {
            int index = path.get(i);
            if (index < 0
                || index >= parent.getLength()) {
                throw new IndexOutOfBoundsException
                    ("Branch path out of bounds: " + path);
            }

            BranchHandler child = parent.get(index);

            if (child == null) {
                List<?> parentBranchData = parent.getBranchData();
                Object childData = parentBranchData.get(index);

                if (!(childData instanceof List<?>)) {
                    throw new IllegalArgumentException
                        ("Unexpected leaf in branch path: " + path);
                }

                child = new BranchHandler(parent, (List<?>)childData);
                parent.update(index, child);
            }

            parent = child;
        }
    }

    /**
     * Gets the path to the node found at the specified y-coordinate
     * (relative to the tree view).
     *
     * @param y
     * The y-coordinate in pixels.
     *
     * @return
     * The path to the node, or <tt>null</tt> if there is no node being
     * painted at the specified y-coordinate.
     */
    public Path getNodeAt(int y) {
        TreeView.Skin treeViewSkin = (TreeView.Skin)getSkin();
        return treeViewSkin.getNodeAt(y);
    }

    /**
     * Gets the bounds of the node at the specified path relative to the
     * tree view. Note that all nodes are left aligned with the tree; to
     * get the pixel value of a node's indent, use
     * {@link #getNodeIndent(int)}.
     *
     * @param path
     * The path to the node.
     *
     * @return
     * The bounds, or <tt>null</tt> if the node is not currently visible.
     */
    public Bounds getNodeBounds(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        if (path.getLength() == 0) {
            throw new IllegalArgumentException("path is empty.");
        }

        TreeView.Skin treeViewSkin = (TreeView.Skin)getSkin();
        return treeViewSkin.getNodeBounds(path);
    }

    /**
     * Gets the pixel indent of nodes at the specified depth. Depth is measured
     * in generations away from the tree view's "root" node, which is
     * represented by the {@link #getTreeData() tree data}.
     *
     * @param depth
     * The depth, where the first child of the root has depth 1, the child of
     * that branch has depth 2, etc.
     *
     * @return
     * The indent in pixels.
     */
    public int getNodeIndent(int depth) {
        TreeView.Skin treeViewSkin = (TreeView.Skin)getSkin();
        return treeViewSkin.getNodeIndent(depth);
    }

    /**
     * Gets the row index of the node, as seen in the current visible nodes
     * list. Note that as branches are expanded and collapsed, the row
     * index of any given node in the tree will change.
     *
     * @param path
     * The path to the node.
     *
     * @return
     * The row index of the node, or <tt>-1</tt> if the node is not
     * currently visible.
     */
    public int getRowIndex(Path path) {
        TreeView.Skin treeViewSkin = (TreeView.Skin)getSkin();
        return treeViewSkin.getRowIndex(path);
    }

    /**
     * Gets the <tt>TreeViewListener</tt>s. Developers interested in these
     * events can register for notification on these events by adding
     * themselves to the listener list.
     *
     * @return
     * The tree view listeners.
     */
    public ListenerList<TreeViewListener> getTreeViewListeners() {
        return treeViewListeners;
    }

    /**
     * Gets the <tt>TreeViewBranchListener</tt>s. Developers interested in
     * these events can register for notification on these events by adding
     * themselves to the listener list.
     *
     * @return
     * The tree view branch listeners.
     */
    public ListenerList<TreeViewBranchListener> getTreeViewBranchListeners() {
        return treeViewBranchListeners;
    }

    /**
     * Gets the <tt>TreeViewNodeListener</tt>s. Developers interested in these
     * events can register for notification on these events by adding
     * themselves to the listener list.
     *
     * @return
     * The tree view node listeners.
     */
    public ListenerList<TreeViewNodeListener> getTreeViewNodeListeners() {
        return treeViewNodeListeners;
    }

    /**
     * Gets the <tt>TreeViewNodeStateListener</tt>s. Developers interested in
     * these events can register for notification on these events by adding
     * themselves to the listener list.
     *
     * @return
     * The tree view node state listeners.
     */
    public ListenerList<TreeViewNodeStateListener> getTreeViewNodeStateListeners() {
        return treeViewNodeStateListeners;
    }

    /**
     * Gets the <tt>TreeViewSelectionListener</tt>s. Developers interested in
     * these events can register for notification on these events by adding
     * themselves to the listener list.
     *
     * @return
     * The tree view selection listeners.
     */
    public ListenerList<TreeViewSelectionListener> getTreeViewSelectionListeners() {
        return treeViewSelectionListeners;
    }
}
