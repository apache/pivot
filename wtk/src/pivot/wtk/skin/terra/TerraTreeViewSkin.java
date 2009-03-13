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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.collections.Sequence;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.Bounds;
import pivot.wtk.Theme;
import pivot.wtk.TreeView;
import pivot.wtk.TreeViewListener;
import pivot.wtk.TreeViewBranchListener;
import pivot.wtk.TreeViewNodeListener;
import pivot.wtk.TreeViewNodeStateListener;
import pivot.wtk.TreeViewSelectionListener;
import pivot.wtk.skin.ComponentSkin;

/**
 * Tree view skin.
 *
 * @author tvolkert
 */
public class TerraTreeViewSkin extends ComponentSkin implements TreeView.Skin,
    TreeViewListener, TreeViewBranchListener, TreeViewNodeListener,
    TreeViewNodeStateListener, TreeViewSelectionListener{

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
        public Sequence<NodeInfo> children = null;
        public boolean expanded = false;

        public BranchInfo(BranchInfo parent, List<Object> data) {
            super(parent, data);
        }

        @SuppressWarnings("unchecked")
        public void loadChildren() {
            if (children == null) {
                List<Object> data = (List<Object>)this.data;
                int count = data.getLength();

                children = new ArrayList<NodeInfo>(count);

                for (int i = 0; i < count; i++) {
                    Object nodeData = data.get(i);
                    children.add(NodeInfo.createNew(this, nodeData));
                }
            }
        }
    }

    private static final int BRANCH_CONTROL_IMAGE_WIDTH = 8;
    private static final int BRANCH_CONTROL_IMAGE_HEIGHT = 8;
    private static final int VERTICAL_SPACING = 1;

    private BranchInfo rootBranchInfo = null;
    private List<NodeInfo> visibleNodes = new ArrayList<NodeInfo>();

    private NodeInfo highlightedNode = null;

    private Font font;
    private Color color;
    private Color disabledColor;
    private Color backgroundColor;
    private Color selectionColor;
    private Color selectionBackgroundColor;
    private Color inactiveSelectionColor;
    private Color inactiveSelectionBackgroundColor;
    private Color highlightColor;
    private Color highlightBackgroundColor;
    private int spacing;
    private int indent;
    private boolean showHighlight;
    private boolean showBranchControls;
    private Color branchControlColor;
    private Color branchControlDisabledColor;
    private Color branchControlSelectionColor;
    private Color branchControlInactiveSelectionColor;
    private Color gridColor;
    private boolean showGridLines;

    public TerraTreeViewSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();

        font = theme.getFont();
        color = theme.getColor(1);
        disabledColor = theme.getColor(7);
        backgroundColor = theme.getColor(4);
        selectionColor = theme.getColor(4);
        selectionBackgroundColor = theme.getColor(19);
        inactiveSelectionColor = theme.getColor(1);
        inactiveSelectionBackgroundColor = theme.getColor(10);
        highlightColor = theme.getColor(1);
        highlightBackgroundColor = theme.getColor(10);
        spacing = 6;
        indent = 16;
        showHighlight = true;
        showBranchControls = true;
        branchControlColor = theme.getColor(18);
        branchControlDisabledColor = theme.getColor(19);
        branchControlSelectionColor = theme.getColor(4);
        branchControlInactiveSelectionColor = theme.getColor(19);
        gridColor = theme.getColor(11);
        showGridLines = false;
    }

    public void install(Component component) {
        super.install(component);

        TreeView treeView = (TreeView)component;
        treeView.getTreeViewListeners().add(this);
        treeView.getTreeViewBranchListeners().add(this);
        treeView.getTreeViewNodeListeners().add(this);
        treeView.getTreeViewNodeStateListeners().add(this);
        treeView.getTreeViewSelectionListeners().add(this);

        treeDataChanged(treeView, null);
    }

    public void uninstall() {
        TreeView treeView = (TreeView)getComponent();
        treeView.getTreeViewListeners().remove(this);
        treeView.getTreeViewBranchListeners().remove(this);
        treeView.getTreeViewNodeListeners().remove(this);
        treeView.getTreeViewNodeStateListeners().remove(this);
        treeView.getTreeViewSelectionListeners().remove(this);

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

            if (treeView.getCheckmarksEnabled()) {
                nodeWidth += indent + spacing;
            }

            nodeRenderer.render(nodeInfo.data, treeView, false, false,
                TreeView.NodeCheckState.UNCHECKED, false, false);
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

        int nodeHeight = getNodeHeight();

        // Paint the background
        graphics.setPaint(backgroundColor);
        graphics.fillRect(0, 0, width, height);

        int nodeStart = 0;
        int nodeEnd = visibleNodes.getLength() - 1;

        // Ensure that we only paint items that are visible
        Rectangle clipBounds = graphics.getClipBounds();
        if (clipBounds != null) {
            nodeStart = Math.max(nodeStart, (int)(clipBounds.y
                / (double)(nodeHeight + VERTICAL_SPACING)));
            nodeEnd = Math.min(nodeEnd, (int)((clipBounds.y +
                clipBounds.height) / (double)(nodeHeight + VERTICAL_SPACING)));
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

                    Color branchControlColor;

                    if (treeView.isEnabled()
                        && !disabled) {
                        if (selected) {
                            if (treeView.isFocused()) {
                                branchControlColor = branchControlSelectionColor;
                            } else {
                                branchControlColor = branchControlInactiveSelectionColor;
                            }
                        } else {
                            branchControlColor = this.branchControlColor;
                        }
                    } else {
                        branchControlColor = branchControlDisabledColor;
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

                nodeX += indent + spacing;
            }

            // Paint the checkbox
            if (treeView.getCheckmarksEnabled()) {
                // TODO Paint the checkbox

                nodeX += indent + spacing;
            }

            int nodeWidth = width - nodeX;

            // Paint the node data
            Graphics2D rendererGraphics = (Graphics2D)graphics.create(nodeX, nodeY,
                nodeWidth, nodeHeight);
            nodeRenderer.render(nodeInfo.data, treeView, expanded, selected,
                TreeView.NodeCheckState.UNCHECKED, highlighted, disabled);
            nodeRenderer.setSize(nodeWidth, nodeHeight);
            nodeRenderer.paint(rendererGraphics);
            rendererGraphics.dispose();

            // Paint the grid line
            if (showGridLines) {
                graphics.setStroke(gridStroke);
                graphics.setPaint(gridColor);

                graphics.drawLine(0, nodeY + nodeHeight, width, nodeY + nodeHeight);
            }

            nodeY += nodeHeight + VERTICAL_SPACING;
        }
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        this.font = font;
        invalidateComponent();
    }

    public void setFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Font.decode(font));
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.color = color;
        repaintComponent();
    }

    public void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(decodeColor(color));
    }

    public Color getDisabledColor() {
        return disabledColor;
    }

    public void setDisabledColor(Color disabledColor) {
        if (disabledColor == null) {
            throw new IllegalArgumentException("disabledColor is null.");
        }

        this.disabledColor = disabledColor;
        repaintComponent();
    }

    public void setDisabledColor(String disabledColor) {
        if (disabledColor == null) {
            throw new IllegalArgumentException("disabledColor is null.");
        }

        setDisabledColor(decodeColor(disabledColor));
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    public void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        setBackgroundColor(decodeColor(backgroundColor));
    }

    public Color getSelectionColor() {
        return selectionColor;
    }

    public void setSelectionColor(Color selectionColor) {
        if (selectionColor == null) {
            throw new IllegalArgumentException("selectionColor is null.");
        }

        this.selectionColor = selectionColor;
        repaintComponent();
    }

    public void setSelectionColor(String selectionColor) {
        if (selectionColor == null) {
            throw new IllegalArgumentException("selectionColor is null.");
        }

        setSelectionColor(decodeColor(selectionColor));
    }

    public Color getSelectionBackgroundColor() {
        return selectionBackgroundColor;
    }

    public void setSelectionBackgroundColor(Color selectionBackgroundColor) {
        if (selectionBackgroundColor == null) {
            throw new IllegalArgumentException("selectionBackgroundColor is null.");
        }

        this.selectionBackgroundColor = selectionBackgroundColor;
        repaintComponent();
    }

    public void setSelectionBackgroundColor(String selectionBackgroundColor) {
        if (selectionBackgroundColor == null) {
            throw new IllegalArgumentException("selectionBackgroundColor is null.");
        }

        setSelectionBackgroundColor(decodeColor(selectionBackgroundColor));
    }

    public Color getInactiveSelectionColor() {
        return inactiveSelectionColor;
    }

    public void setInactiveSelectionColor(Color inactiveSelectionColor) {
        if (inactiveSelectionColor == null) {
            throw new IllegalArgumentException("inactiveSelectionColor is null.");
        }

        this.inactiveSelectionColor = inactiveSelectionColor;
        repaintComponent();
    }

    public void setInactiveSelectionColor(String inactiveSelectionColor) {
        if (inactiveSelectionColor == null) {
            throw new IllegalArgumentException("inactiveSelectionColor is null.");
        }

        setInactiveSelectionColor(decodeColor(inactiveSelectionColor));
    }

    public Color getInactiveSelectionBackgroundColor() {
        return inactiveSelectionBackgroundColor;
    }

    public void setInactiveSelectionBackgroundColor(Color inactiveSelectionBackgroundColor) {
        if (inactiveSelectionBackgroundColor == null) {
            throw new IllegalArgumentException("inactiveSelectionBackgroundColor is null.");
        }

        this.inactiveSelectionBackgroundColor = inactiveSelectionBackgroundColor;
        repaintComponent();
    }

    public void setInactiveSelectionBackgroundColor(String inactiveSelectionBackgroundColor) {
        if (inactiveSelectionBackgroundColor == null) {
            throw new IllegalArgumentException("inactiveSelectionBackgroundColor is null.");
        }

        setInactiveSelectionBackgroundColor(decodeColor(inactiveSelectionBackgroundColor));
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(Color highlightColor) {
        if (highlightColor == null) {
            throw new IllegalArgumentException("highlightColor is null.");
        }

        this.highlightColor = highlightColor;
        repaintComponent();
    }

    public void setHighlightColor(String highlightColor) {
        if (highlightColor == null) {
            throw new IllegalArgumentException("highlightColor is null.");
        }

        setHighlightColor(decodeColor(highlightColor));
    }

    public Color getHighlightBackgroundColor() {
        return highlightBackgroundColor;
    }

    public void setHighlightBackgroundColor(Color highlightBackgroundColor) {
        if (highlightBackgroundColor == null) {
            throw new IllegalArgumentException("highlightBackgroundColor is null.");
        }

        this.highlightBackgroundColor = highlightBackgroundColor;
        repaintComponent();
    }

    public void setHighlightBackgroundColor(String highlightBackgroundColor) {
        if (highlightBackgroundColor == null) {
            throw new IllegalArgumentException("highlightBackgroundColor is null.");
        }

        setHighlightBackgroundColor(decodeColor(highlightBackgroundColor));
    }

    public int getSpacing() {
        return spacing;
    }

    public void setSpacing(int spacing) {
        this.spacing = spacing;
        invalidateComponent();
    }

    public void setSpacing(Number spacing) {
        if (spacing == null) {
            throw new IllegalArgumentException("spacing is null.");
        }

        setSpacing(spacing.intValue());
    }

    public int getIndent() {
        return indent;
    }

    public void setIndent(int indent) {
        this.indent = indent;
        invalidateComponent();
    }

    public void setIndent(Number indent) {
        if (indent == null) {
            throw new IllegalArgumentException("indent is null.");
        }

        setIndent(indent.intValue());
    }

    public boolean getShowHighlight() {
        return showHighlight;
    }

    public void setShowHighlight(boolean showHighlight) {
        this.showHighlight = showHighlight;
        repaintComponent();
    }

    public boolean getShowBranchControls() {
        return showBranchControls;
    }

    public void setShowBranchControls(boolean showBranchControls) {
        this.showBranchControls = showBranchControls;
        invalidateComponent();
    }

    public Color getBranchControlColor() {
        return branchControlColor;
    }

    public void setBranchControlColor(Color branchControlColor) {
        if (branchControlColor == null) {
            throw new IllegalArgumentException("branchControlColor is null.");
        }

        this.branchControlColor = branchControlColor;
        repaintComponent();
    }

    public void setBranchControlColor(String branchControlColor) {
        if (branchControlColor == null) {
            throw new IllegalArgumentException("branchControlColor is null.");
        }

        setBranchControlColor(decodeColor(branchControlColor));
    }

    public Color getBranchControlDisabledColor() {
        return branchControlDisabledColor;
    }

    public void setBranchControlDisabledColor(Color branchControlDisabledColor) {
        if (branchControlDisabledColor == null) {
            throw new IllegalArgumentException("branchControlDisabledColor is null.");
        }

        this.branchControlDisabledColor = branchControlDisabledColor;
        repaintComponent();
    }

    public void setBranchControlDisabledColor(String branchControlDisabledColor) {
        if (branchControlDisabledColor == null) {
            throw new IllegalArgumentException("branchControlDisabledColor is null.");
        }

        setBranchControlDisabledColor(decodeColor(branchControlDisabledColor));
    }

    public Color getBranchControlSelectionColor() {
        return branchControlSelectionColor;
    }

    public void setBranchControlSelectionColor(Color branchControlSelectionColor) {
        if (branchControlSelectionColor == null) {
            throw new IllegalArgumentException("branchControlSelectionColor is null.");
        }

        this.branchControlSelectionColor = branchControlSelectionColor;
        repaintComponent();
    }

    public void setBranchControlSelectionColor(String branchControlSelectionColor) {
        if (branchControlSelectionColor == null) {
            throw new IllegalArgumentException("branchControlSelectionColor is null.");
        }

        setBranchControlSelectionColor(decodeColor(branchControlSelectionColor));
    }

    public Color getBranchControlInactiveSelectionColor() {
        return branchControlInactiveSelectionColor;
    }

    public void setBranchControlInactiveSelectionColor(Color branchControlInactiveSelectionColor) {
        if (branchControlInactiveSelectionColor == null) {
            throw new IllegalArgumentException("branchControlInactiveSelectionColor is null.");
        }

        this.branchControlInactiveSelectionColor = branchControlInactiveSelectionColor;
        repaintComponent();
    }

    public void setBranchControlInactiveSelectionColor(String branchControlInactiveSelectionColor) {
        if (branchControlInactiveSelectionColor == null) {
            throw new IllegalArgumentException("branchControlInactiveSelectionColor is null.");
        }

        setBranchControlInactiveSelectionColor(decodeColor(branchControlInactiveSelectionColor));
    }

    public Color getGridColor() {
        return gridColor;
    }

    public void setGridColor(Color gridColor) {
        if (gridColor == null) {
            throw new IllegalArgumentException("gridColor is null.");
        }

        this.gridColor = gridColor;
        repaintComponent();
    }

    public void setGridColor(String gridColor) {
        if (gridColor == null) {
            throw new IllegalArgumentException("gridColor is null.");
        }

        setGridColor(decodeColor(gridColor));
    }

    public boolean getShowGridLines() {
        return showGridLines;
    }

    public void setShowGridLines(boolean showGridLines) {
        this.showGridLines = showGridLines;
        repaintComponent();
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
                branchInfo.loadChildren();
                NodeInfo nodeInfo = branchInfo.children.get(path.get(i));

                assert(nodeInfo instanceof BranchInfo) : "Invalid path";

                branchInfo = (BranchInfo)nodeInfo;
            }

            branchInfo.loadChildren();
            result = branchInfo.children.get(path.get(n - 1));
        }

        return result;
    }

    /**
     * Gets the bounding box defined by the specified node, or <tt>null</tt>
     * if the node is not currently visible.
     */
    protected Bounds getNodeBounds(NodeInfo nodeInfo) {
        Bounds bounds = null;

        int index = visibleNodes.indexOf(nodeInfo);

        if (index >= 0) {
            int nodeHeight = getNodeHeight();
            int nodeY = index * (nodeHeight + VERTICAL_SPACING);

            bounds = new Bounds(0, nodeY, getWidth(), nodeHeight);
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
            parentBranchInfo.loadChildren();
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
                        branchInfo.loadChildren();
                        for (int i = 0, n = branchInfo.children.getLength(); i < n; i++) {
                            nodes.insert(branchInfo.children.get(i), i);
                        }
                    }
                }
            }
        }

        invalidateComponent();
    }

    /**
     * Adds the specified child of the specified branch to the visible node
     * list.
     */
    private void addVisibleNode(BranchInfo parentBranchInfo, int index) {
        parentBranchInfo.loadChildren();

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
        }

        invalidateComponent();
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
        parentBranchInfo.loadChildren();

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
            }
        }

        invalidateComponent();
    }

    /**
     * Repaints the region occupied by the specified node.
     */
    protected void repaintNode(NodeInfo nodeInfo) {
        Bounds bounds = getNodeBounds(nodeInfo);
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
    public boolean mouseMove(Component component, int x, int y) {
        boolean consumed = super.mouseMove(component, x, y);

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
    public void mouseOut(Component component) {
        super.mouseOut(component);

        clearHighlightedNode();
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        if (button == Mouse.Button.LEFT) {
            TreeView treeView = (TreeView)getComponent();
            treeView.requestFocus();

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

                    int nodeX = (branchInfo.depth - 1) * (indent + spacing);

                    if (x >= nodeX
                        && x < nodeX + indent) {
                        treeView.setBranchExpanded(path, !branchInfo.expanded);
                        handled = true;
                    }
                }

                // See if the user clicked on a checkbox. If so, update the
                // check state of the node
                if (!handled
                    && treeView.getCheckmarksEnabled()) {
                    // TODO Update node check state
                }

                // If we haven't handled the event, then proceed to manage the
                // selection state of the node
                if (!handled) {
                    TreeView.SelectMode selectMode = treeView.getSelectMode();

                    if (selectMode == TreeView.SelectMode.SINGLE) {
                        if (!treeView.isNodeSelected(path)) {
                            treeView.setSelectedPath(path);
                        }
                    } else if (selectMode == TreeView.SelectMode.MULTI) {
                        if (Keyboard.isPressed(Keyboard.Modifier.CTRL)) {
                            if (treeView.isNodeSelected(path)) {
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

        return consumed;
    }

    @Override
    public boolean mouseWheel(Component component, Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        boolean consumed = super.mouseWheel(component, scrollType, scrollAmount,
            wheelRotation, x, y);

        clearHighlightedNode();

        return consumed;
    }

    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(component, keyCode, keyLocation);

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
            if (showBranchControls) {
                Sequence<Sequence<Integer>> paths = treeView.getSelectedPaths();

                if (paths != null) {
                    Sequence<Integer> path = paths.get(paths.getLength() - 1);
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
            if (showBranchControls) {
                Sequence<Sequence<Integer>> paths = treeView.getSelectedPaths();

                if (paths != null) {
                    Sequence<Integer> path = paths.get(paths.getLength() - 1);
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

    public Bounds getNodeBounds(Sequence<Integer> path) {
        NodeInfo nodeInfo = getNodeInfoAt(path);
        return getNodeBounds(nodeInfo);
    }

    public int getNodeIndent(int depth) {
        TreeView treeView = (TreeView)getComponent();

        int nodeIndent = (depth - 1) * (indent + spacing);

        if (showBranchControls) {
            nodeIndent += indent + spacing;
        }

        if (treeView.getCheckmarksEnabled()) {
            nodeIndent += indent + spacing;
        }

        return nodeIndent;
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

    public void checkmarksEnabledChanged(TreeView treeView) {
        invalidateComponent();
    }

    public void showMixedCheckmarkStateChanged(TreeView treeView) {
        if (treeView.getCheckmarksEnabled()) {
            // TODO update internal data model?
            repaintComponent();
        }
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
        if (branchInfo.children != null) {
            NodeInfo nodeInfo = NodeInfo.createNew(branchInfo, branchData.get(index));
            branchInfo.children.insert(nodeInfo, index);
        }

        // Add the node to the visible nodes list
        addVisibleNode(branchInfo, index);

        branchInfo.loadChildren();
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
        if (branchInfo.children != null) {
            branchInfo.children.remove(index,
                (count >= 0) ? count : branchInfo.children.getLength());
        }

        branchInfo.loadChildren();
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

        branchInfo.loadChildren();
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

    // TreeViewNodeStateListener methods

    public void nodeDisabledChanged(TreeView treeView, Sequence<Integer> path) {
        NodeInfo nodeInfo = getNodeInfoAt(path);

        nodeInfo.disabled = treeView.isNodeDisabled(path);
        repaintNode(nodeInfo);
    }

    public void nodeCheckStateChanged(TreeView treeView, Sequence<Integer> path,
        TreeView.NodeCheckState previousCheckState) {
        // TODO
    }

    // TreeViewSelectionListener methods

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

    public void selectedPathsChanged(TreeView treeView,
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
