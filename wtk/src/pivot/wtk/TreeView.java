/*
 * Copyright (c) 2008 VMware, Inc.
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
package pivot.wtk;

import java.util.Comparator;

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.util.ListenerList;
import pivot.wtk.content.TreeViewNodeRenderer;

/**
 * Class that displays a hierarchical data structure, allowing a user to select
 * one or more paths.
 *
 * @author gbrown
 * @author tvolkert
 */
public class TreeView extends Component {
    /**
     * Enumeration defining supported selection modes.
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
        MULTI;

        public static SelectMode decode(String value) {
            return valueOf(value.toUpperCase());
        }
    }

    /**
     * Enumeration defining node check states.
     */
    public enum NodeCheckState {
        /**
         * The node is checked, meaning that all of its descendants are
         * also checked.
         */
        CHECKED,

        /**
         * The node is unchecked, meaning that all of its descendants are also
         * unchecked.
         */
        UNCHECKED,

        /**
         * The node's check state is mixed, meaning that some of its
         * descendants are checked and some are not.
         */
        MIXED;

        public static NodeCheckState decode(String value) {
            return valueOf(value.toUpperCase());
        }
    }

    /**
     * Tree view node renderer interface.
     *
     * @author tvolkert
     */
    public interface NodeRenderer extends Renderer {
        public void render(Object node, TreeView treeView, boolean expanded,
            boolean selected, NodeCheckState checkState, boolean highlighted,
            boolean disabled);
    }

    /**
     * Tree view skin interface. Tree view skins must implement this.
     *
     * @author tvolkert
     */
    public interface Skin {
        public Sequence<Integer> getNodeAt(int y);
        public Bounds getNodeBounds(Sequence<Integer> path);
        public int getNodeOffset(Sequence<Integer> path);
    }

    /**
     * Tree view listener list.
     *
     * @author tvolkert
     */
    private static class TreeViewListenerList extends ListenerList<TreeViewListener>
        implements TreeViewListener {

        public void treeDataChanged(TreeView treeView, List<?> previousTreeData) {
            for (TreeViewListener listener : this) {
                listener.treeDataChanged(treeView, previousTreeData);
            }
        }

        public void nodeRendererChanged(TreeView treeView,
            NodeRenderer previousNodeRenderer) {
            for (TreeViewListener listener : this) {
                listener.nodeRendererChanged(treeView, previousNodeRenderer);
            }
        }

        public void selectModeChanged(TreeView treeView, SelectMode previousSelectMode) {
            for (TreeViewListener listener : this) {
                listener.selectModeChanged(treeView, previousSelectMode);
            }
        }

        public void checkmarksEnabledChanged(TreeView treeView) {
            for (TreeViewListener listener : this) {
                listener.checkmarksEnabledChanged(treeView);
            }
        }

        public void showMixedCheckmarkStateChanged(TreeView treeView) {
            for (TreeViewListener listener : this) {
                listener.showMixedCheckmarkStateChanged(treeView);
            }
        }
    }

    /**
     * Tree view branch listener list.
     *
     * @author tvolkert
     */
    private static class TreeViewBranchListenerList extends ListenerList<TreeViewBranchListener>
        implements TreeViewBranchListener {
        public void branchExpanded(TreeView treeView, Sequence<Integer> path) {
            for (TreeViewBranchListener listener : this) {
                listener.branchExpanded(treeView, path);
            }
        }

        public void branchCollapsed(TreeView treeView, Sequence<Integer> path) {
            for (TreeViewBranchListener listener : this) {
                listener.branchCollapsed(treeView, path);
            }
        }
    }

    /**
     * Tree view node listener list.
     *
     * @author tvolkert
     */
    private static class TreeViewNodeListenerList extends ListenerList<TreeViewNodeListener>
        implements TreeViewNodeListener {
        public void nodeInserted(TreeView treeView, Sequence<Integer> path, int index) {
            for (TreeViewNodeListener listener : this) {
                listener.nodeInserted(treeView, path, index);
            }
        }

        public void nodesRemoved(TreeView treeView, Sequence<Integer> path, int index,
            int count) {
            for (TreeViewNodeListener listener : this) {
                listener.nodesRemoved(treeView, path, index, count);
            }
        }

        public void nodeUpdated(TreeView treeView, Sequence<Integer> path, int index) {
            for (TreeViewNodeListener listener : this) {
                listener.nodeUpdated(treeView, path, index);
            }
        }

        public void nodesSorted(TreeView treeView, Sequence<Integer> path) {
            for (TreeViewNodeListener listener : this) {
                listener.nodesSorted(treeView, path);
            }
        }
    }

    /**
     * Tree view node state listener list.
     *
     * @author tvolkert
     */
    private static class TreeViewNodeStateListenerList
        extends ListenerList<TreeViewNodeStateListener>
        implements TreeViewNodeStateListener {
        public void nodeDisabledChanged(TreeView treeView, Sequence<Integer> path) {
            for (TreeViewNodeStateListener listener : this) {
                listener.nodeDisabledChanged(treeView, path);
            }
        }

        public void nodeCheckStateChanged(TreeView treeView, Sequence<Integer> path,
            TreeView.NodeCheckState previousCheckState) {
            for (TreeViewNodeStateListener listener : this) {
                listener.nodeCheckStateChanged(treeView, path, previousCheckState);
            }
        }
    }

    /**
     * Tree view selection listener list.
     *
     * @author tvolkert
     */
    private static class TreeViewSelectionListenerList
        extends ListenerList<TreeViewSelectionListener>
        implements TreeViewSelectionListener {
        public void selectedPathAdded(TreeView treeView, Sequence<Integer> path) {
            for (TreeViewSelectionListener listener : this) {
                listener.selectedPathAdded(treeView, path);
            }
        }

        public void selectedPathRemoved(TreeView treeView, Sequence<Integer> path) {
            for (TreeViewSelectionListener listener : this) {
                listener.selectedPathRemoved(treeView, path);
            }
        }

        public void selectedPathsChanged(TreeView treeView,
            Sequence<Sequence<Integer>> previousSelectedPaths) {
            for (TreeViewSelectionListener listener : this) {
                listener.selectedPathsChanged(treeView, previousSelectedPaths);
            }
        }
    }

    /**
     * A comparator that sorts paths by the order in which they would visually
     * appear in a fully expanded tree, otherwise known as their "row order".
     *
     * @author tvolkert
     */
    private static class TreeViewPathComparator
        implements Comparator<Sequence<Integer>> {
        public int compare(Sequence<Integer> path1, Sequence<Integer> path2) {
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
     *
     * @author tvolkert
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
         * tree data remained in scope, even if  we were no longer using the
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
        private Sequence<Integer> getPath() {
            Sequence<Integer> path = new ArrayList<Integer>();

            BranchHandler handler = this;

            while (handler.parent != null) {
                int index = ((List<Object>)handler.parent.branchData).indexOf(handler.branchData);
                path.insert(index, 0);

                handler = handler.parent;
            }

            return path;
        }

        public void itemInserted(List<Object> list, int index) {
            Sequence<Integer> path = getPath();

            // Insert child handler placeholder (lazily loaded)
            insert(null, index);

            // Update our data structures
            incrementPaths(expandedPaths, path, index);
            incrementPaths(selectedPaths, path, index);
            incrementPaths(disabledPaths, path, index);
            incrementPaths(checkedPaths, path, index);

            // Notify listeners
            treeViewNodeListeners.nodeInserted(TreeView.this, path, index);
        }

        public void itemsRemoved(List<Object> list, int index, Sequence<Object> items) {
            Sequence<Integer> path = getPath();

            // Remove child handlers
            int count = (items == null) ? getLength() : items.getLength();
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
            clearAndDecrementPaths(selectedPaths, path, index, count);
            clearAndDecrementPaths(disabledPaths, path, index, count);
            clearAndDecrementPaths(checkedPaths, path, index, count);

            // Notify listeners
            treeViewNodeListeners.nodesRemoved(TreeView.this, getPath(), index,
                (items == null) ? -1 : items.getLength());
        }

        public void itemUpdated(List<Object> list, int index, Object previousItem) {
            Sequence<Integer> path = getPath();

            // TODO update child handler and tree view data structures

            treeViewNodeListeners.nodeUpdated(TreeView.this, path, index);
        }

        public void comparatorChanged(List<Object> list,
            Comparator<Object> previousComparator) {
            if (list.getComparator() != null) {
                Sequence<Integer> path = getPath();

                // Update our data structures
                clearPaths(expandedPaths, path);
                clearPaths(selectedPaths, path);
                clearPaths(disabledPaths, path);
                clearPaths(checkedPaths, path);

                // Notify listeners
                treeViewNodeListeners.nodesSorted(TreeView.this, path);
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
        private void incrementPaths(Sequence<Sequence<Integer>> paths,
            Sequence<Integer> basePath, int index) {
            // Calculate the child's path
            Sequence<Integer> childPath = new ArrayList<Integer>(basePath);
            childPath.add(index);

            // Find the child path's place in our sorted paths sequence
            int i = Sequence.Search.binarySearch(paths, childPath, PATH_COMPARATOR);
            if (i < 0) {
                i = -i - 1;
            }

            // Update all affected paths by incrementing the appropriate path element
            for (int depth = basePath.getLength(), n = paths.getLength(); i < n; i++) {
                Sequence<Integer> affectedPath = paths.get(i);

                if (!Sequence.Tree.isDescendant(basePath, affectedPath)) {
                    // All paths from here forward are guaranteed to be unaffected
                    break;
                }

                affectedPath.update(depth, affectedPath.get(depth) + 1);
            }
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
         * The path to the parent of the inserted item.
         */
        private void clearAndDecrementPaths(Sequence<Sequence<Integer>> paths,
            Sequence<Integer> basePath, int index, int count) {
            // TODO
        }

        /**
         * Removes affected paths from within the specified sequence in response
         * to a base path having been sorted.  For instance, if <tt>paths</tt>
         * is <tt>[[3, 0], [3, 1], [5, 0]]</tt> and <tt>basePath</tt> is
         * <tt>[3]</tt>, then <tt>paths</tt> will be updated to
         * <tt>[[5, 0]]</tt>. No events are fired.
         *
         * @param paths
         * Sequence of paths guaranteed to be sorted by "row order".
         *
         * @param basePath
         * The path to the parent of the inserted item.
         */
        private void clearPaths(Sequence<Sequence<Integer>> paths, Sequence<Integer> basePath) {
            // Find first descendant in paths list, if it exists
            int index = Sequence.Search.binarySearch(paths, basePath, PATH_COMPARATOR);
            index = (index < 0 ? -index - 1 : index + 1);

            // Remove all descendants from the paths list
            for (int i = index, n = paths.getLength(); i < n; i++) {
                Sequence<Integer> affectedPath = paths.get(index);

                if (!Sequence.Tree.isDescendant(basePath, affectedPath)) {
                    break;
                }

                paths.remove(index, 1);
            }
        }
    }

    private List<?> treeData = null;

    private ArrayList<Sequence<Integer>> expandedPaths =
        new ArrayList<Sequence<Integer>>(PATH_COMPARATOR);
    private ArrayList<Sequence<Integer>> selectedPaths =
        new ArrayList<Sequence<Integer>>(PATH_COMPARATOR);
    private ArrayList<Sequence<Integer>> disabledPaths =
        new ArrayList<Sequence<Integer>>(PATH_COMPARATOR);
    private ArrayList<Sequence<Integer>> checkedPaths =
        new ArrayList<Sequence<Integer>>(PATH_COMPARATOR);

    private SelectMode selectMode = SelectMode.SINGLE;
    private boolean checkmarksEnabled = false;
    private boolean showMixedCheckmarkState = false;

    private BranchHandler rootBranchHandler;

    private NodeRenderer nodeRenderer = DEFAULT_NODE_RENDERER;

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

    private static final Comparator<Sequence<Integer>> PATH_COMPARATOR =
        new TreeViewPathComparator();

    public TreeView() {
        this(new ArrayList<Object>());
    }

    /**
     * TreeView constructor.
     *
     * @param treeData
     * Default data set to be used with the tree. This list represents the root
     * set of items displayed by the tree. Sub-items that also implement the
     * <tt>List</tt> interface are considered branches; other items are
     * considered leaves.
     *
     * @see #setTreeData(List)
     */
    public TreeView(List<?> treeData) {
        setTreeData(treeData);
        installSkin(TreeView.class);
    }

    @Override
    protected void setSkin(pivot.wtk.Skin skin) {
        if (!(skin instanceof TreeView.Skin)) {
            throw new IllegalArgumentException("Skin class must implement "
                + TreeView.Skin.class.getName());
        }

        super.setSkin(skin);
    }

    /**
     * Returns the tree data.
     */
    public List<?> getTreeData() {
        return treeData;
    }

    /**
     * Sets the tree data.
     *
     * @param treeData
     * The data to be presented by the tree. It is the responsibility of the
     * caller to ensure that the current tree node renderer is capable of
     * displaying the contents of the tree structure.
     */
    public void setTreeData(List<?> treeData) {
        if (treeData == null) {
            throw new IllegalArgumentException("treeData is null.");
        }

        List<?> previousTreeData = this.treeData;

        if (previousTreeData != treeData) {
            if (previousTreeData != null) {
                // Reset our data models
                expandedPaths.clear();
                selectedPaths.clear();
                disabledPaths.clear();
                checkedPaths.clear();

                // Release our existing branch handlers
                rootBranchHandler.release();
            }

            // Update our root branch handler
            rootBranchHandler = new BranchHandler(null, treeData);

            // Update the tree data
            this.treeData = treeData;

            // Notify listeners
            treeViewListeners.treeDataChanged(this, previousTreeData);
        }
    }

    public NodeRenderer getNodeRenderer() {
        return nodeRenderer;
    }

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
     * Returns the current selection mode.
     */
    public SelectMode getSelectMode() {
        return selectMode;
    }

    /**
     * Sets the selection mode. Clears the selection if the mode has changed.
     *
     * @param selectMode
     * The new selection mode.
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

    public void setSelectMode(String selectMode) {
        if (selectMode == null) {
            throw new IllegalArgumentException("selectMode is null.");
        }

        setSelectMode(SelectMode.decode(selectMode));
    }

    public Sequence<Sequence<Integer>> getSelectedPaths() {
        Sequence<Sequence<Integer>> selectedPaths =
            new ArrayList<Sequence<Integer>>(this.selectedPaths.getLength());

        // Deep copy the selected paths into a new list
        for (int i = 0, n = this.selectedPaths.getLength(); i < n; i++) {
            Sequence<Integer> selectedPath = new ArrayList<Integer>(this.selectedPaths.get(i));
            selectedPaths.add(selectedPath);
        }

        return selectedPaths;
    }

    public void setSelectedPaths(Sequence<Sequence<Integer>> selectedPaths) {
        if (selectedPaths == null) {
            throw new IllegalArgumentException("selectedPaths is null.");
        }

        if (selectMode == SelectMode.NONE) {
            throw new IllegalArgumentException("Selection is not enabled.");
        }

        if (selectMode == SelectMode.SINGLE
            && selectedPaths.getLength() > 1) {
            throw new IllegalArgumentException("Selection length is greater than 1.");
        }

        Sequence<Sequence<Integer>> previousSelectedPaths = this.selectedPaths;

        this.selectedPaths = new ArrayList<Sequence<Integer>>(PATH_COMPARATOR);

        for (int i = 0, n = selectedPaths.getLength(); i < n; i++) {
            Sequence<Integer> path = selectedPaths.get(i);

            // Monitor the path's parent
            monitorBranch(new ArrayList<Integer>(path, 0, path.getLength() - 1));

            // Update the selection
            this.selectedPaths.add(new ArrayList<Integer>(path));
        }

        // Notify listeners
        treeViewSelectionListeners.selectedPathsChanged(this, previousSelectedPaths);
    }

    /**
     * Returns the first selected path, as it would appear in a fully expanded
     * tree.
     *
     * @return
     * The first selected path, or <tt>null</tt> if nothing is selected.
     */
    public Sequence<Integer> getFirstSelectedPath() {
        Sequence<Integer> selectedPath = null;

        if (selectedPaths.getLength() > 0) {
            selectedPath = new ArrayList<Integer>(selectedPaths.get(0));
        }

        return selectedPath;
    }

    /**
     * Returns the last selected path, as it would appear in a fully expanded
     * tree.
     *
     * @return
     * The last selected path, or <tt>null</tt> if nothing is selected.
     */
    public Sequence<Integer> getLastSelectedPath() {
        Sequence<Integer> selectedPath = null;

        if (selectedPaths.getLength() > 0) {
            selectedPath = new ArrayList<Integer>
                (selectedPaths.get(selectedPaths.getLength() - 1));
        }

        return selectedPath;
    }

    public Sequence<Integer> getSelectedPath() {
        if (selectMode != SelectMode.SINGLE) {
            throw new IllegalStateException("Tree view is not in single-select mode.");
        }

        Sequence<Integer> selectedPath = null;

        if (selectedPaths.getLength() > 0) {
            selectedPath = new ArrayList<Integer>(selectedPaths.get(0));
        }

        return selectedPath;
    }

    public final void setSelectedPath(Sequence<Integer> path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        Sequence<Sequence<Integer>> selectedPaths = new ArrayList<Sequence<Integer>>(1);
        selectedPaths.add(new ArrayList<Integer>(path));

        setSelectedPaths(selectedPaths);
    }

    public Object getSelectedNode() {
        Sequence<Integer> path = getSelectedPath();
        Object node = null;

        if (path != null) {
            node = Sequence.Tree.get(treeData, path);
        }

        return node;
    }

    public void addSelectedPath(Sequence<Integer> path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        if (selectMode != SelectMode.MULTI) {
            throw new IllegalStateException("Tree view is not in multi-select mode.");
        }

        if (selectedPaths.indexOf(path) < 0) {
            // Monitor the path's parent
            monitorBranch(new ArrayList<Integer>(path, 0, path.getLength() - 1));

            // Update the selection
            selectedPaths.add(new ArrayList<Integer>(path));

            // Notify listeners
            treeViewSelectionListeners.selectedPathAdded(this, path);
        }
    }

    public void removeSelectedPath(Sequence<Integer> path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
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
        }
    }

    public void clearSelection() {
        if (selectedPaths.getLength() > 0) {
            Sequence<Sequence<Integer>> previousSelectedPaths = selectedPaths;

            // Update the selection
            selectedPaths = new ArrayList<Sequence<Integer>>(PATH_COMPARATOR);

            // Notify listeners
            treeViewSelectionListeners.selectedPathsChanged(this, previousSelectedPaths);
        }
    }

    public boolean isNodeSelected(Sequence<Integer> path) {
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
    public boolean isNodeDisabled(Sequence<Integer> path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        return (disabledPaths.indexOf(path) >= 0);
    }

    /**
     * Sets the disabled state of a node.
     *
     * @param path
     * The path to the node whose disabled state is to be set
     *
     * @param disabled
     * <tt>true</tt> to disable the node; <tt>false</tt>, otherwise
     */
    public void setNodeDisabled(Sequence<Integer> path, boolean disabled) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        int index = disabledPaths.indexOf(path);

        if ((index < 0 && disabled)
            || (index >= 0 && !disabled)) {
            if (disabled) {
                // Monitor the path's parent
                monitorBranch(new ArrayList<Integer>(path, 0, path.getLength() - 1));

                // Update the disabled paths
                disabledPaths.add(new ArrayList<Integer>(path));
            } else {
                // Update the disabled paths
                disabledPaths.remove(index, 1);
            }

            // Notify listeners
            treeViewNodeStateListeners.nodeDisabledChanged(this, path);
        }
    }

    public Sequence<Sequence<Integer>> getDisabledPaths() {
        Sequence<Sequence<Integer>> disabledPaths =
            new ArrayList<Sequence<Integer>>(this.disabledPaths.getLength());

        // Deep copy the disabled paths into a new list
        for (int i = 0, n = this.disabledPaths.getLength(); i < n; i++) {
            Sequence<Integer> disabledPath = new ArrayList<Integer>(this.disabledPaths.get(i));
            disabledPaths.add(disabledPath);
        }

        return disabledPaths;
    }

    public boolean getCheckmarksEnabled() {
        return checkmarksEnabled;
    }

    /**
     * Enables or disabled checkmarks. Clears the check state if the check
     * mode has changed (but does not fire any check state change events).
     *
     * @param checkmarksEnabled
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

    public boolean getShowMixedCheckmarkState() {
        return showMixedCheckmarkState;
    }

    public void setShowMixedCheckmarkState(boolean showMixedCheckmarkState) {
        if (this.showMixedCheckmarkState != showMixedCheckmarkState) {
            // Update the flag
            this.showMixedCheckmarkState = showMixedCheckmarkState;

            // Notify listeners
            treeViewListeners.showMixedCheckmarkStateChanged(this);
        }
    }

    public boolean isNodeChecked(Sequence<Integer> path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        return (checkedPaths.indexOf(path) >= 0);
    }

    public NodeCheckState getNodeCheckState(Sequence<Integer> path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        NodeCheckState checkState = NodeCheckState.UNCHECKED;

        int index = Sequence.Search.binarySearch(checkedPaths, path, PATH_COMPARATOR);

        if (index >= 0) {
            checkState = NodeCheckState.CHECKED;
        } else if (showMixedCheckmarkState) {
            // Translate to the insertion index
            index = -index - 1;

            if (index < checkedPaths.getLength()) {
                Sequence<Integer> nextCheckedPath = checkedPaths.get(index);

                if (Sequence.Tree.isDescendant(path, nextCheckedPath)) {
                    checkState = NodeCheckState.MIXED;
                }
            }
        }

        return checkState;
    }

    public void setNodeChecked(Sequence<Integer> path, boolean checked) {
        if (!checkmarksEnabled) {
            throw new IllegalStateException("Checkmarks are not enabled.");
        }

        int index = Sequence.Search.binarySearch(checkedPaths, path, PATH_COMPARATOR);

        if ((index < 0 && checked)
            || (index >= 0 && !checked)) {
            NodeCheckState previousCheckState = getNodeCheckState(path);

            if (checked) {
                // Monitor the path's parent
                monitorBranch(new ArrayList<Integer>(path, 0, path.getLength() - 1));

                // Update the checked paths
                checkedPaths.add(new ArrayList<Integer>(path));
            } else {
                // Update the checked paths
                checkedPaths.remove(index, 1);
            }

            // Notify listeners
            treeViewNodeStateListeners.nodeCheckStateChanged(this, path, previousCheckState);

            if (showMixedCheckmarkState) {
                // TODO scan up hierarchy, seeing if this changed any of our
                // ancestors' check state
            }
        }
    }

    public Sequence<Sequence<Integer>> getCheckedPaths() {
        Sequence<Sequence<Integer>> checkedPaths =
            new ArrayList<Sequence<Integer>>(this.checkedPaths.getLength());

        // Deep copy the checked paths into a new list
        for (int i = 0, n = this.checkedPaths.getLength(); i < n; i++) {
            Sequence<Integer> checkedPath = new ArrayList<Integer>(this.checkedPaths.get(i));
            checkedPaths.add(checkedPath);
        }

        return checkedPaths;
    }

    public void setBranchExpanded(Sequence<Integer> path, boolean expanded) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        int index = expandedPaths.indexOf(path);

        if (expanded) {
            if (index < 0) {
                // Monitor the branch
                monitorBranch(path);

                // Update the expanded paths
                expandedPaths.add(new ArrayList<Integer>(path));

                // Notify listeners
                treeViewBranchListeners.branchExpanded(this, path);
            }
        } else {
            if (index >= 0) {
                // Update the expanded paths
                expandedPaths.remove(index, 1);

                // Notify listeners
                treeViewBranchListeners.branchCollapsed(this, path);
            }
        }
    }

    public boolean isBranchExpanded(Sequence<Integer> path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }

        return (expandedPaths.indexOf(path) >= 0);
    }

    public final void expandBranch(Sequence<Integer> path) {
        setBranchExpanded(path, true);
    }

    public final void collapseBranch(Sequence<Integer> path) {
        setBranchExpanded(path, false);
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
    @SuppressWarnings("unchecked")
    private void monitorBranch(Sequence<Integer> path) {
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

                if (!(childData instanceof List)) {
                    throw new IllegalArgumentException
                        ("Unexpected leaf in branch path: " + path);
                }

                child = new BranchHandler(parent, (List<?>)childData);
                parent.update(index, child);
            }

            parent = child;
        }
    }

    public Sequence<Integer> getNodeAt(int y) {
        TreeView.Skin treeViewSkin = (TreeView.Skin)getSkin();
        return treeViewSkin.getNodeAt(y);
    }

    public Bounds getNodeBounds(Sequence<Integer> path) {
        TreeView.Skin treeViewSkin = (TreeView.Skin)getSkin();
        return treeViewSkin.getNodeBounds(path);
    }

    public int getNodeOffset(Sequence<Integer> path) {
        TreeView.Skin treeViewSkin = (TreeView.Skin)getSkin();
        return treeViewSkin.getNodeOffset(path);
    }

    public ListenerList<TreeViewListener> getTreeViewListeners() {
        return treeViewListeners;
    }

    public void setTreeViewListener(TreeViewListener listener) {
        treeViewListeners.add(listener);
    }

    public ListenerList<TreeViewBranchListener> getTreeViewBranchListeners() {
        return treeViewBranchListeners;
    }

    public void setTreeViewBranchListeners(TreeViewBranchListener listener) {
        treeViewBranchListeners.add(listener);
    }

    public ListenerList<TreeViewNodeListener> getTreeViewNodeListeners() {
        return treeViewNodeListeners;
    }

    public void setTreeViewNodeListener(TreeViewNodeListener listener) {
        treeViewNodeListeners.add(listener);
    }

    public ListenerList<TreeViewNodeStateListener> getTreeViewNodeStateListeners() {
        return treeViewNodeStateListeners;
    }

    public void setTreeViewNodeStateListener(TreeViewNodeStateListener listener) {
        treeViewNodeStateListeners.add(listener);
    }

    public ListenerList<TreeViewSelectionListener> getTreeViewSelectionListeners() {
        return treeViewSelectionListeners;
    }

    public void setTreeViewSelectionListener(TreeViewSelectionListener listener) {
        treeViewSelectionListeners.add(listener);
    }
}
