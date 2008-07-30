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
package pivot.wtk.skin.terra;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.collections.Sequence;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.Rectangle;
import pivot.wtk.TreeView;
import pivot.wtk.TreeViewListener;
import pivot.wtk.TreeViewBranchListener;
import pivot.wtk.TreeViewNodeListener;
import pivot.wtk.TreeViewNodeStateListener;
import pivot.wtk.TreeViewSelectionDetailListener;
import pivot.wtk.skin.ComponentSkin;

/**
 *
 * @author tvolkert
 */
public class TreeViewSkin extends ComponentSkin implements TreeView.Skin,
    TreeViewListener, TreeViewBranchListener, TreeViewNodeListener,
    TreeViewNodeStateListener, TreeViewSelectionDetailListener{

    /**
     * An internal data structure that keeps track of skin-related metadata
     * for a tree node.
     * <p>
     * NOTE some of this data is duplicated from <tt>TreeView</tt> to provide
     * optimizations during painting and user input.
     *
     * @author tvolkert
     */
    protected static class NodeInfo {
        public BranchInfo parent;
        public Object data;
        public int depth;
        public boolean selected = false;
        public boolean highlighted = false;
        public boolean disabled = false;

        public NodeInfo(BranchInfo parent, Object data) {
            this.parent = parent;
            this.data = data;

            depth = (parent == null) ? 0 : parent.depth + 1;
        }

        @SuppressWarnings("unchecked")
        public static NodeInfo createNew(BranchInfo parent, Object data) {
            NodeInfo nodeInfo = null;

            if (data instanceof List) {
                nodeInfo = new BranchInfo(parent, (List<Object>)data);
            } else {
                nodeInfo = new NodeInfo(parent, data);
            }

            return nodeInfo;
        }

        @SuppressWarnings("unchecked")
        public Sequence<Integer> getPath() {
            Sequence<Integer> path = new ArrayList<Integer>(depth);

            NodeInfo nodeInfo = this;

            while (nodeInfo.parent != null) {
                List<Object> parentData = (List<Object>)nodeInfo.parent.data;
                int index = parentData.indexOf(nodeInfo.data);
                path.insert(index, 0);

                nodeInfo = nodeInfo.parent;
            }

            return path;
        }
    }

    /**
     * An internal data structure that keeps track of skin-related metadata
     * for a tree branch.
     *
     * @author tvolkert
     */
    protected static class BranchInfo extends NodeInfo {
        public Sequence<NodeInfo> children = new ArrayList<NodeInfo>();
        public boolean expanded = false;

        public BranchInfo(BranchInfo parent, List<Object> data) {
            super(parent, data);

            for (int i = 0, n = data.getLength(); i <  n; i++) {
                Object nodeData = data.get(i);
                children.add(NodeInfo.createNew(this, nodeData));
            }
        }
    }

    private static final int BRANCH_CONTROL_IMAGE_WIDTH = 8;
    private static final int BRANCH_CONTROL_IMAGE_HEIGHT = 8;
    private static final int VERTICAL_SPACING = 1;

    private BranchInfo rootBranchInfo = null;
    private List<NodeInfo> visibleNodes = new ArrayList<NodeInfo>();

    private NodeInfo highlightedNode = null;

    private Font font = new Font("Verdana", Font.PLAIN, 11);
    private Color color = Color.BLACK;
    private Color disabledColor = new Color(0x99, 0x99, 0x99);
    private Color backgroundColor = Color.WHITE;
    private Color selectionColor = Color.WHITE;
    private Color selectionBackgroundColor = new Color(0x3C, 0x77, 0xB2);
    private Color inactiveSelectionColor = Color.BLACK;
    private Color inactiveSelectionBackgroundColor = new Color(0xE6, 0xE3, 0xDA);
    private Color highlightColor = Color.BLACK;
    private Color highlightBackgroundColor = new Color(0xE6, 0xE3, 0xDA);
    private int spacing = 6;
    private int indent = 16;
    private boolean showHighlight = true;
    private boolean showBranchControls = true;
    private Color branchControlColor = new Color(0x33, 0x66, 0x99);
    private Color branchControlDisabledColor = new Color(0x3C, 0x77, 0xB2);
    private Color branchControlSelectionColor = Color.WHITE;
    private Color branchControlInactiveSelectionColor = new Color(0x33, 0x66, 0x99);
    private Color gridColor = new Color(0xF7, 0xF5, 0xEB);
    private boolean showGridLines = false;

    public void install(Component component) {
        validateComponentType(component, TreeView.class);

        super.install(component);

        TreeView treeView = (TreeView)component;
        treeView.getTreeViewListeners().add(this);
        treeView.getTreeViewBranchListeners().add(this);
        treeView.getTreeViewNodeListeners().add(this);
        treeView.getTreeViewNodeStateListeners().add(this);
        treeView.getTreeViewSelectionDetailListeners().add(this);

        treeDataChanged(treeView, null);
    }

    public void uninstall() {
        TreeView treeView = (TreeView)getComponent();
        treeView.getTreeViewListeners().remove(this);
        treeView.getTreeViewBranchListeners().remove(this);
        treeView.getTreeViewNodeListeners().remove(this);
        treeView.getTreeViewNodeStateListeners().remove(this);
        treeView.getTreeViewSelectionDetailListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        TreeView treeView = (TreeView)getComponent();
        TreeView.NodeRenderer nodeRenderer = treeView.getNodeRenderer();

        int preferredWidth = 0;

        for (int i = 0, n = visibleNodes.getLength(); i < n; i++) {
            NodeInfo nodeInfo = visibleNodes.get(i);

            int nodeWidth = (nodeInfo.depth - 1) * (indent + spacing);

            if (showBranchControls) {
                nodeWidth += indent + spacing;
            }

            if (treeView.isCheckEnabled()) {
                nodeWidth += indent + spacing;
            }

            nodeRenderer.render(nodeInfo.data, treeView, false, false, false, false);
            nodeWidth += nodeRenderer.getPreferredWidth(-1);

            preferredWidth = Math.max(preferredWidth, nodeWidth);
        }

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        TreeView treeView = (TreeView)getComponent();
        TreeView.NodeRenderer nodeRenderer = treeView.getNodeRenderer();

        int preferredHeight = 0;

        int nodeHeight = nodeRenderer.getPreferredHeight(-1);
        int visibleNodeCount = visibleNodes.getLength();

        preferredHeight = nodeHeight * visibleNodeCount;

        if (visibleNodeCount > 1) {
            preferredHeight += VERTICAL_SPACING * (visibleNodeCount - 1);
        }

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    public void layout() {
        // No-op
    }

    public void paint(Graphics2D graphics) {
        TreeView treeView = (TreeView)getComponent();
        TreeView.NodeRenderer nodeRenderer = treeView.getNodeRenderer();

        int width = getWidth();
        int height = getHeight();

        Shape clip = graphics.getClip();

        int nodeHeight = getNodeHeight();

        // Paint the background
        graphics.setPaint(backgroundColor);
        graphics.fillRect(0, 0, width, height);

        int nodeStart = 0;
        int nodeEnd = visibleNodes.getLength() - 1;

        // Ensure that we only paint items that are visible
        if (clip != null) {
            Rectangle2D clipBounds = clip.getBounds();

            nodeStart = Math.max(0,
                (int)(clipBounds.getY() / (double)(nodeHeight + VERTICAL_SPACING)));
            nodeEnd = Math.min(nodeEnd, (int)((clipBounds.getY() +
                clipBounds.getHeight()) / (double)(nodeHeight + VERTICAL_SPACING)));
        }

        BasicStroke gridStroke = new BasicStroke();

        int nodeY = nodeStart * (nodeHeight + VERTICAL_SPACING);

        for (int i = nodeStart; i <= nodeEnd; i++) {
            NodeInfo nodeInfo = visibleNodes.get(i);

            boolean expanded = false;
            boolean highlighted = nodeInfo.highlighted;
            boolean selected = nodeInfo.selected;
            boolean disabled = nodeInfo.disabled;

            int nodeX = (nodeInfo.depth - 1) * (indent + spacing);

            if (treeView.isEnabled()) {
                if (selected) {
                    // Paint the selection state
                    Color selectionBackgroundColor = treeView.isFocused() ?
                        this.selectionBackgroundColor : inactiveSelectionBackgroundColor;
                    graphics.setPaint(selectionBackgroundColor);
                    graphics.fillRect(0, nodeY, width, nodeHeight);
                } else if (highlighted && !disabled) {
                    // Paint the highlight state
                    graphics.setPaint(highlightBackgroundColor);
                    graphics.fillRect(0, nodeY, width, nodeHeight);
                }
            }

            // Paint the expand/collapse control
            if (showBranchControls) {
                if (nodeInfo instanceof BranchInfo) {
                    BranchInfo branchInfo = (BranchInfo)nodeInfo;

                    expanded = branchInfo.expanded;

                    if (branchInfo.children.getLength() > 0) {
                        Color branchControlColor;

                        if (treeView.isEnabled()
                            && !disabled) {
                            if (selected) {
                                if (treeView.isFocused()) {
                                    branchControlColor = this.branchControlSelectionColor;
                                } else {
                                    branchControlColor = this.branchControlInactiveSelectionColor;
                                }
                            } else {
                                branchControlColor = this.branchControlColor;
                            }
                        } else {
                            branchControlColor = this.branchControlDisabledColor;
                        }

                        GeneralPath shape = new GeneralPath();

                        int imageX = nodeX + (indent - BRANCH_CONTROL_IMAGE_WIDTH) / 2;
                        int imageY = nodeY + (nodeHeight - BRANCH_CONTROL_IMAGE_HEIGHT) / 2;

                        if (expanded) {
                            shape.moveTo(imageX, imageY + 1);
                            shape.lineTo(imageX + 8, imageY + 1);
                            shape.lineTo(imageX + 4, imageY + 7);
                        } else {
                            shape.moveTo(imageX + 1, imageY);
                            shape.lineTo(imageX + 7, imageY + 4);
                            shape.lineTo(imageX + 1, imageY + 8);
                        }

                        shape.closePath();

                        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                        graphics.setPaint(branchControlColor);

                        graphics.fill(shape);
                    }
                }

                nodeX += indent + spacing;
            }

            // Paint the checkbox
            if (treeView.isCheckEnabled()) {
                // TODO Paint the checkbox

                nodeX += indent + spacing;
            }

            int nodeWidth = width - nodeX;

            // Paint the node data
            Graphics2D rendererGraphics = (Graphics2D)graphics.create(nodeX, nodeY,
                nodeWidth, nodeHeight);
            nodeRenderer.render(nodeInfo.data, treeView, expanded, selected,
                highlighted, disabled);
            nodeRenderer.setSize(nodeWidth, nodeHeight);
            nodeRenderer.paint(rendererGraphics);

            // Paint the grid line
            if (showGridLines) {
                graphics.setStroke(gridStroke);
                graphics.setPaint(gridColor);

                graphics.drawLine(0, nodeY + nodeHeight, width, nodeY + nodeHeight);
            }

            nodeY += nodeHeight + VERTICAL_SPACING;
        }
    }

    /**
     * Gets the fixed node height of this skin.
     */
    protected int getNodeHeight() {
        TreeView treeView = (TreeView)getComponent();
        TreeView.NodeRenderer nodeRenderer = treeView.getNodeRenderer();

        return nodeRenderer.getPreferredHeight(-1);
    }

    /**
     * Gets the metadata associated with the node found at the specified
     * y-coordinate, or <tt>null</tt> if there is no node at that location.
     */
    protected NodeInfo getNodeInfoAt(int y) {
        NodeInfo nodeInfo = null;

        int nodeHeight = getNodeHeight();
        int index = y / (nodeHeight + VERTICAL_SPACING);

        if (index >= 0
            && index < visibleNodes.getLength()) {
            nodeInfo = visibleNodes.get(index);
        }

        return nodeInfo;
    }

    /**
     * Gets the metadata associated with the node at the specified path.
     */
    protected NodeInfo getNodeInfoAt(Sequence<Integer> path) {
        assert(path != null) : "Path is null";

        NodeInfo result = null;

        int n = path.getLength();

        if (n == 0) {
            result = rootBranchInfo;
        } else {
            BranchInfo branchInfo = rootBranchInfo;

            for (int i = 0; i < n - 1; i++) {
                NodeInfo nodeInfo = branchInfo.children.get(path.get(i));

                assert(nodeInfo instanceof BranchInfo) : "Invalid path";

                branchInfo = (BranchInfo)nodeInfo;
            }

            result = branchInfo.children.get(path.get(n - 1));
        }

        return result;
    }

    /**
     * Gets the bounding box defined by the specified node, or <tt>null</tt>
     * if the node is not currently visible.
     */
    protected Rectangle getNodeBounds(NodeInfo nodeInfo) {
        Rectangle bounds = null;

        int index = visibleNodes.indexOf(nodeInfo);

        if (index >= 0) {
            int nodeHeight = getNodeHeight();
            int nodeY = index * (nodeHeight + VERTICAL_SPACING);

            bounds = new Rectangle(0, nodeY, getWidth(), nodeHeight);
        }

        return bounds;
    }

    /**
     * Adds all children of the specified branch to the visible node list.
     */
    private void addVisibleNodes(BranchInfo parentBranchInfo) {
        int insertIndex = -1;

        if (parentBranchInfo == rootBranchInfo) {
            // Bootstrap case since the root branch is implicitly expanded
            insertIndex = 0;
        } else {
            int branchIndex = visibleNodes.indexOf(parentBranchInfo);
            if (branchIndex >= 0) {
                insertIndex = branchIndex + 1;
            }
        }

        if (insertIndex >= 0) {
            Sequence<NodeInfo> nodes = new ArrayList<NodeInfo>();

            // The parent branch's children are the baseline nodes to make
            // visible
            for (int i = 0, n = parentBranchInfo.children.getLength(); i < n; i++) {
                nodes.add(parentBranchInfo.children.get(i));
            }

            while (nodes.getLength() > 0) {
                NodeInfo nodeInfo = nodes.get(0);
                nodes.remove(nodeInfo);

                visibleNodes.insert(nodeInfo, insertIndex++);

                // If we encounter an expanded branch, we add that branch's
                // children to our list of nodes that are to become visible
                if (nodeInfo instanceof BranchInfo) {
                    BranchInfo branchInfo = (BranchInfo)nodeInfo;

                    if (branchInfo.expanded) {
                        for (int i = 0, n = branchInfo.children.getLength(); i < n; i++) {
                            nodes.insert(branchInfo.children.get(i), i);
                        }
                    }
                }
            }

            invalidateComponent();
        }
    }

    /**
     * Adds the specified child of the specified branch to the visible node
     * list.
     */
    private void addVisibleNode(BranchInfo parentBranchInfo, int index) {
        assert(index >= 0) : "Index is too small";
        assert(index < parentBranchInfo.children.getLength()) : "Index is too large";

        int branchIndex = visibleNodes.indexOf(parentBranchInfo);

        if ((branchIndex >= 0 && parentBranchInfo.expanded)
            || parentBranchInfo == rootBranchInfo) {

            NodeInfo nodeInfo = parentBranchInfo.children.get(index);

            int insertIndex = branchIndex + index + 1;

            if (index > 0) {
                // Siblings of the node that lie before it may be expanded
                // branches, thus adding their own children to the
                // visible nodes list and pushing down our insert index
                NodeInfo youngerSibling = parentBranchInfo.children.get(index - 1);

                // Try to insert after our younger sibling
                insertIndex = visibleNodes.indexOf(youngerSibling) + 1;

                // Continue looking as long as the node at our insert index
                // has a greater depth than we do, which means that it's a
                // descendant of our younger sibling
                for (int n = visibleNodes.getLength(), nodeDepth = youngerSibling.depth;
                    insertIndex < n && visibleNodes.get(insertIndex).depth > nodeDepth;
                    insertIndex++);
            }

            visibleNodes.insert(nodeInfo, insertIndex);

            invalidateComponent();
        }
    }

    /**
     * Removes the specified children of the specified branch from the visible
     * node list if necessary. If they are not already in the visible node
     * list, nothing happens.
     *
     * @param parentBranchInfo
     * The branch info of the parent node
     *
     * @param index
     * The index of the first child node to remove from the visible nodes
     * sequence
     *
     * @param count
     * The number of child nodes to remove, or <tt>-1</tt> to remove all
     * child nodes from the visible nodes sequence
     */
    private void removeVisibleNodes(BranchInfo parentBranchInfo, int index, int count) {
        if (count == -1) {
            assert(index == 0) : "Non-zero index with 'remove all' count";
            count = parentBranchInfo.children.getLength();
        }

        assert(index + count <= parentBranchInfo.children.getLength()) : "Value too big";

        if (count > 0) {
            NodeInfo first = parentBranchInfo.children.get(index);
            NodeInfo last = parentBranchInfo.children.get(index + count - 1);

            int rangeStart = visibleNodes.indexOf(first);

            if (rangeStart >= 0) {
                int rangeEnd = visibleNodes.indexOf(last) + 1;

                assert(rangeEnd > rangeStart) : "Invalid visible node structure";

                // Continue looking as long as the node at our endpoint has a
                // greater depth than the last child node, which means that
                // it's a descendant of the last child node
                for (int n = visibleNodes.getLength(), nodeDepth = last.depth;
                    rangeEnd < n && visibleNodes.get(rangeEnd).depth > nodeDepth;
                    rangeEnd++);

                visibleNodes.remove(rangeStart, rangeEnd - rangeStart);

                invalidateComponent();
            }
        }
    }

    /**
     * Repaints the region occupied by the specified node.
     */
    protected void repaintNode(NodeInfo nodeInfo) {
        Rectangle bounds = getNodeBounds(nodeInfo);
        if (bounds != null) {
            repaintComponent(bounds);
        }
    }

    /**
     * Clears the highlighted node if one exists.
     */
    protected void clearHighlightedNode() {
        if (highlightedNode != null) {
            highlightedNode.highlighted = false;
            repaintNode(highlightedNode);

            highlightedNode = null;
        }
    }

    @Override
    public boolean mouseMove(int x, int y) {
        boolean consumed = super.mouseMove(x, y);

        TreeView treeView = (TreeView)getComponent();

        if (showHighlight
            && treeView.getSelectMode() != TreeView.SelectMode.NONE) {
            NodeInfo previousHighlightedNode = this.highlightedNode;
            highlightedNode = getNodeInfoAt(y);

            if (highlightedNode != previousHighlightedNode) {
                if (previousHighlightedNode != null) {
                    previousHighlightedNode.highlighted = false;
                    repaintNode(previousHighlightedNode);
                }

                if (highlightedNode != null) {
                    highlightedNode.highlighted = true;
                    repaintNode(highlightedNode);
                }
            }
        }

        return consumed;
    }

    @Override
    public void mouseOut() {
        super.mouseOut();

        clearHighlightedNode();
    }

    @Override
    public void mouseClick(Mouse.Button button, int x, int y, int count) {
        super.mouseClick(button, x, y, count);

        if (button == Mouse.Button.LEFT) {
            TreeView treeView = (TreeView)getComponent();

            Component.setFocusedComponent(treeView);

            NodeInfo nodeInfo = getNodeInfoAt(y);

            if (nodeInfo != null
                && !nodeInfo.disabled) {
                boolean handled = false;

                Sequence<Integer> path = nodeInfo.getPath();

                // See if the user clicked on an expand/collapse control of a
                // branch. If so, expand/collapse the branch
                if (showBranchControls
                    && nodeInfo instanceof BranchInfo) {
                    BranchInfo branchInfo = (BranchInfo)nodeInfo;

                    if (branchInfo.children.getLength() > 0) {
                        int nodeX = (branchInfo.depth - 1) * (indent + spacing);

                        if (x >= nodeX
                            && x < nodeX + indent) {
                            treeView.setBranchExpanded(path, !branchInfo.expanded);

                            handled = true;
                        }
                    }
                }

                // See if the user clicked on a checkbox. If so, update the
                // check state of the node
                if (!handled
                    && treeView.isCheckEnabled()) {
                    // TODO Update node check state
                }

                // If we haven't handled the event, then proceed to manage the
                // selection state of the node
                if (!handled) {
                    TreeView.SelectMode selectMode = treeView.getSelectMode();

                    if (selectMode == TreeView.SelectMode.SINGLE) {
                        if (!treeView.isPathSelected(path)) {
                            treeView.setSelectedPath(path);
                        }
                    } else if (selectMode == TreeView.SelectMode.MULTI) {
                        if ((Keyboard.getModifiers()
                            & Keyboard.Modifier.CTRL.getMask()) > 0) {
                            if (treeView.isPathSelected(path)) {
                                treeView.removeSelectedPath(path);
                            } else {
                                treeView.addSelectedPath(path);
                            }
                        } else {
                            // Replace the selection
                            treeView.setSelectedPath(path);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean mouseWheel(Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        boolean consumed = super.mouseWheel(scrollType, scrollAmount,
            wheelRotation, x, y);

        clearHighlightedNode();

        return consumed;
    }

    @Override
    public boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(keyCode, keyLocation);

        TreeView treeView = (TreeView)getComponent();
        TreeView.SelectMode selectMode = treeView.getSelectMode();

        switch (keyCode) {
        case Keyboard.KeyCode.UP: {
            if (selectMode != TreeView.SelectMode.NONE) {
                Sequence<Integer> firstSelectedPath = treeView.getFirstSelectedPath();

                int index;
                if (firstSelectedPath != null) {
                    NodeInfo previousSelectedNode = getNodeInfoAt(firstSelectedPath);
                    index = visibleNodes.indexOf(previousSelectedNode);
                } else {
                    // Select the last visible node
                    index = visibleNodes.getLength();
                }

                NodeInfo newSelectedNode = null;
                do {
                    newSelectedNode = (--index >= 0) ? visibleNodes.get(index) : null;
                } while (newSelectedNode != null
                    && newSelectedNode.disabled);

                if (newSelectedNode != null) {
                    treeView.setSelectedPath(newSelectedNode.getPath());
                    treeView.scrollAreaToVisible(getNodeBounds(newSelectedNode));
                    consumed = true;
                }
            }

            break;
        }

        case Keyboard.KeyCode.DOWN: {
            if (selectMode != TreeView.SelectMode.NONE) {
                Sequence<Integer> lastSelectedPath = treeView.getLastSelectedPath();

                int index;
                if (lastSelectedPath != null) {
                    NodeInfo previousSelectedNode = getNodeInfoAt(lastSelectedPath);
                    index = visibleNodes.indexOf(previousSelectedNode);
                } else {
                    // Select the first visible node
                    index = -1;
                }

                NodeInfo newSelectedNode = null;
                int n = visibleNodes.getLength();
                do {
                    newSelectedNode = (++index <= n - 1) ? visibleNodes.get(index) : null;
                } while (newSelectedNode != null
                    && newSelectedNode.disabled);

                if (newSelectedNode != null) {
                    treeView.setSelectedPath(newSelectedNode.getPath());
                    treeView.scrollAreaToVisible(getNodeBounds(newSelectedNode));
                    consumed = true;
                }
            }

            break;
        }

        case Keyboard.KeyCode.LEFT: {
            if (treeView.getSelectMode() == TreeView.SelectMode.SINGLE
                && showBranchControls) {
                Sequence<Integer> path = treeView.getSelectedPath();

                if (path != null) {
                    NodeInfo nodeInfo = getNodeInfoAt(path);

                    if (nodeInfo instanceof BranchInfo) {
                        BranchInfo branchInfo = (BranchInfo)nodeInfo;

                        if (branchInfo.expanded) {
                            treeView.collapseBranch(branchInfo.getPath());
                        }
                    }

                    consumed = true;
                }
            }

            break;
        }

        case Keyboard.KeyCode.RIGHT: {
            if (treeView.getSelectMode() == TreeView.SelectMode.SINGLE
                && showBranchControls) {
                Sequence<Integer> path = treeView.getSelectedPath();

                if (path != null) {
                    NodeInfo nodeInfo = getNodeInfoAt(path);

                    if (nodeInfo instanceof BranchInfo) {
                        BranchInfo branchInfo = (BranchInfo)nodeInfo;

                        if (!branchInfo.expanded) {
                            treeView.expandBranch(branchInfo.getPath());
                        }
                    }

                    consumed = true;
                }
            }

            break;
        }
        }

        clearHighlightedNode();

        return consumed;
    }

    // Component state events

    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        repaintComponent();
    }

    @Override
    public void focusedChanged(Component component, boolean temporary) {
        super.focusedChanged(component, temporary);

        repaintComponent();
    }

    // TreeView.Skin methods

    public Sequence<Integer> getNodeAt(int y) {
        Sequence<Integer> path = null;

        NodeInfo nodeInfo = getNodeInfoAt(y);

        if (nodeInfo != null) {
            path = nodeInfo.getPath();
        }

        return path;
    }

    public Rectangle getNodeBounds(Sequence<Integer> path) {
        NodeInfo nodeInfo = getNodeInfoAt(path);
        return getNodeBounds(nodeInfo);
    }

    public int getNodeOffset(Sequence<Integer> path) {
        TreeView treeView = (TreeView)getComponent();

        NodeInfo nodeInfo = getNodeInfoAt(path);

        int nodeOffset = (nodeInfo.depth - 1) * (indent + spacing);

        if (showBranchControls) {
            nodeOffset += indent + spacing;
        }

        if (treeView.isCheckEnabled()) {
            nodeOffset += indent + spacing;
        }

        return nodeOffset;
    }

    // TreeViewListener methods
    @SuppressWarnings("unchecked")
    public void treeDataChanged(TreeView treeView, List<?> previousTreeData) {
        List<Object> treeData = (List<Object>)treeView.getTreeData();

        visibleNodes.clear();

        if (treeData == null) {
            rootBranchInfo = null;
        } else {
            rootBranchInfo = new BranchInfo(null, treeData);
            addVisibleNodes(rootBranchInfo);
        }

        invalidateComponent();
    }

    public void nodeRendererChanged(TreeView treeView,
        TreeView.NodeRenderer previousNodeRenderer) {
        invalidateComponent();
    }

    public void selectModeChanged(TreeView treeView,
        TreeView.SelectMode previousSelectMode) {
        // No-op
    }

    public void checkEnabledChanged(TreeView treeView) {
        invalidateComponent();
    }

    // TreeViewBranchListener methods

    public void branchExpanded(TreeView treeView, Sequence<Integer> path) {
        BranchInfo branchInfo = (BranchInfo)getNodeInfoAt(path);

        branchInfo.expanded = true;
        addVisibleNodes(branchInfo);
    }

    public void branchCollapsed(TreeView treeView, Sequence<Integer> path) {
        BranchInfo branchInfo = (BranchInfo)getNodeInfoAt(path);

        branchInfo.expanded = false;
        removeVisibleNodes(branchInfo, 0, -1);
    }

    // TreeViewNodeListener methods

    @SuppressWarnings("unchecked")
    public void nodeInserted(TreeView treeView, Sequence<Integer> path, int index) {
        BranchInfo branchInfo = (BranchInfo)getNodeInfoAt(path);
        List<Object> branchData = (List<Object>)branchInfo.data;

        // Update our internal branch info
        NodeInfo nodeInfo = NodeInfo.createNew(branchInfo, branchData.get(index));
        branchInfo.children.insert(nodeInfo, index);

        // Add the node to the visible nodes list
        addVisibleNode(branchInfo, index);

        if (showBranchControls
            && branchInfo.children.getLength() == 1) {
            // The branch went from having no children to having one
            repaintNode(branchInfo);
        }
    }

    public void nodesRemoved(TreeView treeView, Sequence<Integer> path, int index,
        int count) {
        BranchInfo branchInfo = (BranchInfo)getNodeInfoAt(path);

        // Remove the node from the visible nodes list
        removeVisibleNodes(branchInfo, index, count);

        // Update our internal branch info
        branchInfo.children.remove(index,
            (count >= 0) ? count : branchInfo.children.getLength());

        if (showBranchControls
            && branchInfo.children.getLength() == 0) {
            // The branch went from having children to having none
            repaintNode(branchInfo);
        }
    }

    @SuppressWarnings("unchecked")
    public void nodeUpdated(TreeView treeView, Sequence<Integer> path, int index) {
        BranchInfo branchInfo = (BranchInfo)getNodeInfoAt(path);
        List<Object> branchData = (List<Object>)branchInfo.data;

        NodeInfo nodeInfo = branchInfo.children.get(index);

        Object previousNodeData = nodeInfo.data;
        Object nodeData = branchData.get(index);

        if (previousNodeData != nodeData) {
            // Remove the old node from the visible nodes list
            removeVisibleNodes(branchInfo, index, 1);

            // Update our internal branch info
            nodeInfo = NodeInfo.createNew(branchInfo, nodeData);
            branchInfo.children.update(index, nodeInfo);

            // Add the new node to the visible nodes list
            addVisibleNode(branchInfo, index);
        } else if (visibleNodes.indexOf(nodeInfo) >= 0) {
            // The updated node data might affect our preferred width
            invalidateComponent();
        }
    }

    public void nodesSorted(TreeView treeView, Sequence<Integer> path) {
        // TODO
    }

    // TreeViewNodeListener methods

    public void nodeDisabledChanged(TreeView treeView, Sequence<Integer> path) {
        NodeInfo nodeInfo = getNodeInfoAt(path);

        nodeInfo.disabled = treeView.isNodeDisabled(path);
        repaintNode(nodeInfo);
    }

    // TreeViewSelectionDetailListener methods

    public void selectedPathAdded(TreeView treeView, Sequence<Integer> path) {
        NodeInfo nodeInfo = getNodeInfoAt(path);

        nodeInfo.selected = true;
        repaintNode(nodeInfo);
    }

    public void selectedPathRemoved(TreeView treeView, Sequence<Integer> path) {
        NodeInfo nodeInfo = getNodeInfoAt(path);

        nodeInfo.selected = false;
        repaintNode(nodeInfo);
    }

    public void selectionReset(TreeView treeView,
        Sequence<Sequence<Integer>> previousSelectedPaths) {

        // Un-select the previous selected paths
        for (int i = 0, n = previousSelectedPaths.getLength(); i < n; i++) {
            NodeInfo previousSelectedNode = getNodeInfoAt(previousSelectedPaths.get(i));

            previousSelectedNode.selected = false;
            repaintNode(previousSelectedNode);
        }

        Sequence<Sequence<Integer>> selectedPaths = treeView.getSelectedPaths();

        // Select the current selected paths
        for (int i = 0, n = selectedPaths.getLength(); i < n; i++) {
            NodeInfo selectedNode = getNodeInfoAt(selectedPaths.get(i));

            selectedNode.selected = true;
            repaintNode(selectedNode);
        }
    }
}
