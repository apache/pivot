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
 * <p>
 * TODO Add is/setNodeChecked(); methods; add events to
 * TreeViewNodeListener
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
     * appear in a fully expanded tree.
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
    private class BranchHandler implements ListListener<Object> {
        private BranchHandler parentBranch;
        private List<BranchHandler> childBranches = new ArrayList<BranchHandler>();

        // The backing data structure
        private List<?> branchData;

        @SuppressWarnings("unchecked")
        public BranchHandler(BranchHandler parentBranch, List<?> branchData) {
            this.parentBranch = parentBranch;
            this.branchData = branchData;

            ((List<Object>)branchData).getListListeners().add(this);

            for (int i = 0, n = branchData.getLength(); i < n; i++) {
                Object childData = branchData.get(i);

                BranchHandler branchHandler = null;

                if (childData instanceof List) {
                    branchHandler = new BranchHandler(this, (List<?>)childData);
                }

                childBranches.add(branchHandler);
            }
        }

        @SuppressWarnings("unchecked")
        public Sequence<Integer> getPath() {
            Sequence<Integer> path = new ArrayList<Integer>();

            BranchHandler branch = this;

            while (branch.parentBranch != null) {
                int index = ((List<Object>)branch.parentBranch.branchData).indexOf(branch.branchData);
                path.insert(index, 0);

                branch = branch.parentBranch;
            }

            return path;
        }

        /**
         * Unregisters this branch handler's interest in ListListener events. This
         * must be done to release references from the tree data to our
         * internal BranchHandler data structures. Failure to do so would mean
         * that our BranchHandler objects would remain in scope as long as the
         * tree data remained in scope, even if  we were no longer using the
         * BranchHandler objects.
         */
        @SuppressWarnings("unchecked")
        private void unregisterListListeners() {
            ((List<Object>)branchData).getListListeners().remove(this);

            // Recursively have all child branches unregister interest
            for (int i = 0, n = childBranches.getLength(); i < n; i++) {
                BranchHandler branchHandler = childBranches.get(i);
                if (branchHandler != null) {
                    branchHandler.unregisterListListeners();
                }
            }
        }

        public void comparatorChanged(List<Object> list,
            Comparator<Object> previousComparator) {
            if (list.getComparator() != null) {
                Sequence<Integer> path = getPath();

                // Find first descendant in selected paths list, if it exists
                int index = Sequence.Search.binarySearch(selectedPaths, path, PATH_COMPARATOR);
                index = (index < 0 ? -index - 1 : index + 1);

                // Remove all descendants from the selection
                for (int i = index, n = selectedPaths.getLength(); i < n; i++) {
                    Sequence<Integer> selectedPath = selectedPaths.get(index);

                    if (!Sequence.Tree.isDescendant(path, selectedPath)) {
                        break;
                    }

                    selectedPaths.remove(index, 1);
                }

                treeViewNodeListeners.nodesSorted(TreeView.this, path);
            }
        }

        @SuppressWarnings("unchecked")
        public void itemInserted(List<Object> list, int index) {
            Sequence<Integer> path = getPath();

            // Update our children BranchHandler
            BranchHandler branchHandler = null;

            Object childData = list.get(index);
            if (childData instanceof List) {
                branchHandler = new BranchHandler(this, (List<Object>)childData);
            }

            childBranches.insert(branchHandler, index);

            // Calculate the child's path
            Sequence<Integer> childPath = new ArrayList<Integer>(path);
            childPath.add(index);

            pathInserted(expandedPaths, childPath);
            pathInserted(disabledPaths, childPath);
            pathInserted(selectedPaths, childPath);

            treeViewNodeListeners.nodeInserted(TreeView.this, path, index);
        }

        public void itemsRemoved(List<Object> list, int index, Sequence<Object> items) {
            assert(index == 0 || items != null) : "";

            int n = (items == null) ? childBranches.getLength() : items.getLength();
            for (int i = 0; i < n; i++) {
                BranchHandler branchHandler = childBranches.update(index + i, null);

                if (branchHandler != null) {
                    branchHandler.unregisterListListeners();
                }
            }

            treeViewNodeListeners.nodesRemoved(TreeView.this, getPath(), index,
                (items == null) ? -1 : items.getLength());
        }

        public void itemUpdated(List<Object> list, int index, Object previousItem) {
            treeViewNodeListeners.nodeUpdated(TreeView.this, getPath(), index);
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
                expandedPaths.clear();
                selectedPaths.clear();
                disabledPaths.clear();
                checkedPaths.clear();

                rootBranchHandler.unregisterListListeners();
            }

            rootBranchHandler = new BranchHandler(null, treeData);

            // Update the tree data and fire change event
            this.treeData = treeData;
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

        // Update the selection
        this.selectedPaths = new ArrayList<Sequence<Integer>>(PATH_COMPARATOR);

        for (int i = 0, n = selectedPaths.getLength(); i < n; i++) {
            Sequence<Integer> selectedPath = new ArrayList<Integer>(selectedPaths.get(i));
            this.selectedPaths.add(selectedPath);
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

    public void setSelectedPath(Sequence<Integer> path) {
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
        if (selectMode != SelectMode.MULTI) {
            throw new IllegalStateException("Tree view is not in multi-select mode.");
        }

        if (selectedPaths.indexOf(path) < 0) {
            selectedPaths.add(new ArrayList<Integer>(path));

            treeViewSelectionListeners.selectedPathAdded(this, path);
        }
    }

    public void removeSelectedPath(Sequence<Integer> path) {
        if (selectMode != SelectMode.MULTI) {
            throw new IllegalStateException("Tree view is not in multi-select mode.");
        }

        int index = selectedPaths.indexOf(path);

        if (index >= 0) {
            selectedPaths.remove(index, 1);

            treeViewSelectionListeners.selectedPathRemoved(this, path);
        }
    }

    public void clearSelection() {
        if (selectedPaths.getLength() > 0) {
            Sequence<Sequence<Integer>> previousSelectedPaths = selectedPaths;

            selectedPaths = new ArrayList<Sequence<Integer>>(PATH_COMPARATOR);

            treeViewSelectionListeners.selectedPathsChanged(this, previousSelectedPaths);
        }
    }

    public boolean isNodeSelected(Sequence<Integer> path) {
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
        int index = disabledPaths.indexOf(path);

        if ((index < 0 && disabled)
            || (index >= 0 && !disabled)) {
            if (disabled) {
                disabledPaths.add(path);
            } else {
                disabledPaths.remove(index, 1);
            }

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

            // Update the check mode
            this.checkmarksEnabled = checkmarksEnabled;

            // Fire checkmarks enabled change event
            treeViewListeners.checkmarksEnabledChanged(this);
        }
    }

    // TODO Load branch handler
    public void setBranchExpanded(Sequence<Integer> path, boolean expanded) {
        int index = expandedPaths.indexOf(path);

        if (expanded) {
            if (index < 0) {
                expandedPaths.add(new ArrayList<Integer>(path));
                treeViewBranchListeners.branchExpanded(this, path);
            }
        } else {
            if (index >= 0) {
                expandedPaths.remove(index, 1);
                treeViewBranchListeners.branchCollapsed(this, path);
            }
        }
    }

    public boolean isBranchExpanded(Sequence<Integer> path) {
        return (expandedPaths.indexOf(path) >= 0);
    }

    public void expandBranch(Sequence<Integer> path) {
        setBranchExpanded(path, true);
    }

    public void collapseBranch(Sequence<Integer> path) {
        setBranchExpanded(path, false);
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

    /**
     * Updates the paths within the specified sequence in response to a tree
     * data path insertion.  For instance, if <tt>paths</tt> is
     * <tt>[[3, 0], [5, 0]]</tt>, and <tt>path</tt> is <tt>[4]</tt>, then
     * <tt>paths</tt> will be updated to <tt>[[3, 0], [6, 0]]</tt>. No events
     * are fired.
     *
     * @param paths
     * Sequence of paths guaranteed to be sorted according to
     * <tt>TreeViewPathComparator</tt>.
     *
     * @param path
     * The path that was inserted into the tree data.
     *
     * @return
     * <tt>true</tt> if an only if updates were made to <tt>paths</tt>.
     */
    private boolean pathInserted(Sequence<Sequence<Integer>> paths, Sequence<Integer> path) {
        if (path == null || path.getLength() == 0) {
            throw new IllegalArgumentException("path must not be null or empty.");
        }

        boolean result = false;

        // Calculate the parent of the inserted path. Only descendants of this
        // parent path who occur after the inserted path will be affected by
        // the insertion
        Sequence<Integer> parentPath = new ArrayList<Integer>(path);
        parentPath.remove(parentPath.getLength() - 1, 1);

        // Find the inserted path's place in our sorted paths sequence
        int i = Sequence.Search.binarySearch(paths, path, PATH_COMPARATOR);
        if (i < 0) {
            i = -i - 1;
        }

        // Update all affected paths by incrementing the appropriate path element
        for (int depth = path.getLength() - 1, n = paths.getLength(); i < n; i++) {
            Sequence<Integer> affectedPath = paths.get(i);

            if (!Sequence.Tree.isDescendant(parentPath, affectedPath)) {
                // All paths from here forward are guaranteed to be unaffected
                break;
            }

            result = true;

            affectedPath.update(depth, affectedPath.get(depth) + 1);
        }

        return result;
    }

    private boolean pathRemoved(Sequence<Sequence<Integer>> paths, Sequence<Integer> path) {
        // TODO
        return false;
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
