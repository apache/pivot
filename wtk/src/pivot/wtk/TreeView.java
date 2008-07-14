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
 * TODO Add is/setNodeChecked(); is/setNodeDisabled() methods; add events to
 * TreeViewNodeListener
 *
 * This class will listen for list events fired by its branch node. It will
 * notify the tree by calling insertNode(), removeNodes(), and invalidateNode()
 * (when the node is updated; the tree view itself will call invalidateNode()
 * when the node is selected - should this class provide a method to add/remove
 * selected indexes? If so, it may also call invalidateNode() in this case).
 *
 * It will also update the index values in its nested branch state and selected
 * index lists (incrementing when sub-nodes are added, and removing/decrementing
 * when nodes are removed).
 *
 * This structure must be constructed when tree data is set, and modified as
 * tree data is updated.
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
     * Tree view listener list.
     *
     * @author tvolkert
     */
    private class TreeViewListenerList extends ListenerList<TreeViewListener>
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

        public void checkEnabledChanged(TreeView treeView) {
            for (TreeViewListener listener : this) {
                listener.checkEnabledChanged(treeView);
            }
        }
    }

    /**
     * Tree view branch listener list.
     *
     * @author tvolkert
     */
    private class TreeViewBranchListenerList extends ListenerList<TreeViewBranchListener>
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
    private class TreeViewNodeListenerList extends ListenerList<TreeViewNodeListener>
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
    private class TreeViewNodeStateListenerList
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
    private class TreeViewSelectionListenerList
        extends ListenerList<TreeViewSelectionListener>
        implements TreeViewSelectionListener {
        public void selectionChanged(TreeView treeView) {
            for (TreeViewSelectionListener listener : this) {
                listener.selectionChanged(treeView);
            }
        }
    }

    /**
     * Tree view selection detail listener list.
     *
     * @author tvolkert
     */
    private class TreeViewSelectionDetailListenerList
        extends ListenerList<TreeViewSelectionDetailListener>
        implements TreeViewSelectionDetailListener {
        public void selectedPathAdded(TreeView treeView, Sequence<Integer> path) {
            for (TreeViewSelectionDetailListener listener : this) {
                listener.selectedPathAdded(treeView, path);
            }
        }

        public void selectedPathRemoved(TreeView treeView, Sequence<Integer> path) {
            for (TreeViewSelectionDetailListener listener : this) {
                listener.selectedPathRemoved(treeView, path);
            }
        }

        public void selectionReset(TreeView treeView,
            Sequence<Sequence<Integer>> previousSelectedPaths) {
            for (TreeViewSelectionDetailListener listener : this) {
                listener.selectionReset(treeView, previousSelectedPaths);
            }
        }
    }

    public interface NodeRenderer extends Renderer {
        public void render(Object node, TreeView treeView, boolean expanded,
            boolean selected, boolean highlighted, boolean disabled);
    }

    public interface Skin extends pivot.wtk.Skin {
        public Sequence<Integer> getNodeAt(int y);
        public Rectangle getNodeBounds(Sequence<Integer> path);
        public int getNodeOffset(Sequence<Integer> path);
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
     * Nested class that tracks node check state and notifies the tree of
     * nested <tt>ListListener</tt> events that occur on the tree data.
     *
     * @author tvolkert
     */
    private class BranchInfo implements ListListener<Object> {
        private BranchInfo parent;
        private List<BranchInfo> childBranches = new ArrayList<BranchInfo>();
        private List<?> branchData;
        // TODO Add check state data structure

        @SuppressWarnings("unchecked")
        public BranchInfo(BranchInfo parent, List<?> branchData) {
            this.parent = parent;
            this.branchData = branchData;

            ((List<Object>)branchData).getListListeners().add(this);

            for (int i = 0, n = branchData.getLength(); i < n; i++) {
                Object childData = branchData.get(i);

                BranchInfo branchInfo = null;

                if (childData instanceof List) {
                    branchInfo = new BranchInfo(this, (List<?>)childData);
                }

                childBranches.add(branchInfo);
            }
        }

        @SuppressWarnings("unchecked")
        public Sequence<Integer> getPath() {
            Sequence<Integer> path = new ArrayList<Integer>();

            BranchInfo branch = this;

            while (branch.parent != null) {
                int index = ((List<Object>)branch.parent.branchData).indexOf(branch.branchData);
                path.insert(index, 0);

                branch = branch.parent;
            }

            return path;
        }

        public Object getBranchData(int index) {
            return branchData.get(index);
        }

        /**
         * Unregisters this branch info's interest in ListListener events. This
         * must be done to release references from the tree data to our
         * internal BranchInfo data structures. Failure to do so would mean
         * that our BranchInfo objects would remain in scope as long as the
         * tree data remained in scope, even if  we were no longer using the
         * BranchInfo objects.
         */
        @SuppressWarnings("unchecked")
        private void unregisterListListeners() {
            ((List<Object>)branchData).getListListeners().remove(this);

            // Recursively have all child branches unregister interest
            for (int i = 0, n = childBranches.getLength(); i < n; i++) {
                BranchInfo branchInfo = childBranches.get(i);
                if (branchInfo != null) {
                    branchInfo.unregisterListListeners();
                }
            }
        }

        public void comparatorChanged(List<Object> list,
            Comparator<Object> previousComparator) {
            // TODO Remove selected child nodes

            treeViewNodeListeners.nodesSorted(TreeView.this, getPath());
        }

        @SuppressWarnings("unchecked")
        public void itemInserted(List<Object> list, int index) {
            Object childData = list.get(index);

            BranchInfo branchInfo = null;

            if (childData instanceof List) {
                branchInfo = new BranchInfo(this, (List<Object>)childData);
            }

            childBranches.insert(branchInfo, index);

            insertNode(getPath(), index);
        }

        public void itemsRemoved(List<Object> list, int index, Sequence<Object> items) {
            assert(index == 0 || items != null) : "";

            int n = (items == null) ? childBranches.getLength() : items.getLength();
            for (int i = 0; i < n; i++) {
                BranchInfo branchInfo = childBranches.update(index + i, null);

                if (branchInfo != null) {
                    branchInfo.unregisterListListeners();
                }
            }

            removeNodes(getPath(), index, (items == null) ? -1 : items.getLength());
        }

        public void itemUpdated(List<Object> list, int index, Object previousItem) {
            invalidateNode(getPath(), index);
        }
    }

    private static final Comparator<Sequence<Integer>> PATH_COMPARATOR =
        new TreeViewPathComparator();

    /**
     * Tree data.
     */
    private List<?> treeData = null;

    private List<Sequence<Integer>> expandedPaths =
        new ArrayList<Sequence<Integer>>(PATH_COMPARATOR);
    private List<Sequence<Integer>> selectedPaths =
        new ArrayList<Sequence<Integer>>(PATH_COMPARATOR);
    private List<Sequence<Integer>> disabledPaths =
        new ArrayList<Sequence<Integer>>(PATH_COMPARATOR);

    private SelectMode selectMode = SelectMode.SINGLE;
    private boolean checkEnabled = false;

    private BranchInfo rootBranchInfo;

    private NodeRenderer nodeRenderer = new TreeViewNodeRenderer();

    private TreeViewListenerList treeViewListeners = new TreeViewListenerList();
    private TreeViewBranchListenerList treeViewBranchListeners =
        new TreeViewBranchListenerList();
    private TreeViewNodeListenerList treeViewNodeListeners =
        new TreeViewNodeListenerList();
    private TreeViewNodeStateListenerList treeViewNodeStateListeners =
        new TreeViewNodeStateListenerList();
    private TreeViewSelectionListenerList treeViewSelectionListeners =
        new TreeViewSelectionListenerList();
    private TreeViewSelectionDetailListenerList treeViewSelectionDetailListeners =
        new TreeViewSelectionDetailListenerList();

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
    public void setSkinClass(Class<? extends pivot.wtk.Skin> skinClass) {
        if (!TreeView.Skin.class.isAssignableFrom(skinClass)) {
            throw new IllegalArgumentException("Skin class must implement "
                + TreeView.Skin.class.getName());
        }

        super.setSkinClass(skinClass);
    }

    /**
     * Returns the tree data.
     */
    public List<?> getTreeData() {
        return this.treeData;
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
                // Clear any existing selection
                clearSelection();

                rootBranchInfo.unregisterListListeners();
            }

            rootBranchInfo = new BranchInfo(null, treeData);

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
            clearSelection();

            // Update the selection mode
            this.selectMode = selectMode;

            // Fire select mode change event
            treeViewListeners.selectModeChanged(this, previousSelectMode);
        }
    }

    public Sequence<Sequence<Integer>> getSelectedPaths() {
        Sequence<Sequence<Integer>> selectedPaths = new ArrayList<Sequence<Integer>>();

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
        treeViewSelectionDetailListeners.selectionReset(this, previousSelectedPaths);
        treeViewSelectionListeners.selectionChanged(this);
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
        Sequence<Sequence<Integer>> selectedPaths = new ArrayList<Sequence<Integer>>(0);
        selectedPaths.add(path);

        setSelectedPaths(selectedPaths);
    }

    public void addSelectedPath(Sequence<Integer> path) {
        if (selectMode != SelectMode.MULTI) {
            throw new IllegalStateException("Tree view is not in multi-select mode.");
        }

        selectedPaths.add(new ArrayList<Integer>(path));

        treeViewSelectionDetailListeners.selectedPathAdded(this, path);
    }

    public void removeSelectedPath(Sequence<Integer> path) {
        if (selectMode != SelectMode.MULTI) {
            throw new IllegalStateException("Tree view is not in multi-select mode.");
        }

        selectedPaths.remove(path);

        treeViewSelectionDetailListeners.selectedPathRemoved(this, path);
    }

    public void clearSelection() {
        if (selectedPaths.getLength() > 0) {
            Sequence<Sequence<Integer>> previousSelectedPaths = selectedPaths;

            selectedPaths = new ArrayList<Sequence<Integer>>(PATH_COMPARATOR);

            treeViewSelectionDetailListeners.selectionReset(this, previousSelectedPaths);
            treeViewSelectionListeners.selectionChanged(this);
        }
    }

    public boolean isPathSelected(Sequence<Integer> path) {
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
        Sequence<Sequence<Integer>> disabledPaths = new ArrayList<Sequence<Integer>>();

        // Deep copy the disabled paths into a new list
        for (int i = 0, n = this.disabledPaths.getLength(); i < n; i++) {
            Sequence<Integer> disabledPath = new ArrayList<Integer>(this.disabledPaths.get(i));
            disabledPaths.add(disabledPath);
        }

        return disabledPaths;
    }

    public boolean isCheckEnabled() {
        return checkEnabled;
    }

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

    public Rectangle getNodeBounds(Sequence<Integer> path) {
        TreeView.Skin treeViewSkin = (TreeView.Skin)getSkin();
        return treeViewSkin.getNodeBounds(path);
    }

    public int getNodeOffset(Sequence<Integer> path) {
        TreeView.Skin treeViewSkin = (TreeView.Skin)getSkin();
        return treeViewSkin.getNodeOffset(path);
    }

    protected void insertNode(Sequence<Integer> path, int index) {
        treeViewNodeListeners.nodeInserted(this, path, index);
    }

    protected void removeNodes(Sequence<Integer> path, int index, int count) {
        treeViewNodeListeners.nodesRemoved(this, path, index, count);
    }

    protected void invalidateNode(Sequence<Integer> path, int index) {
        treeViewNodeListeners.nodeUpdated(this, path, index);
    }

    public ListenerList<TreeViewListener> getTreeViewListeners() {
        return treeViewListeners;
    }

    public ListenerList<TreeViewBranchListener> getTreeViewBranchListeners() {
        return treeViewBranchListeners;
    }

    public ListenerList<TreeViewNodeListener> getTreeViewNodeListeners() {
        return treeViewNodeListeners;
    }

    public ListenerList<TreeViewNodeStateListener> getTreeViewNodeStateListeners() {
        return treeViewNodeStateListeners;
    }

    public ListenerList<TreeViewSelectionListener> getTreeViewSelectionListeners() {
        return treeViewSelectionListeners;
    }

    public ListenerList<TreeViewSelectionDetailListener> getTreeViewSelectionDetailListeners() {
        return treeViewSelectionDetailListeners;
    }
}
