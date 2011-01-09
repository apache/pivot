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
package org.apache.pivot.scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.pivot.bxml.DefaultProperty;
import org.apache.pivot.scene.effect.Decorator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.ObservableList;
import org.apache.pivot.util.ObservableListAdapter;

/**
 * Container for a group of nodes. A group is primarily responsible for
 * laying out its child nodes.
 */
@DefaultProperty("nodes")
public class Group extends Node {
    private class NodeList extends ObservableListAdapter<Node> {
        public NodeList() {
            super(new ArrayList<Node>());
        }

        @Override
        public void add(int index, Node node) {
            addNode(node);
            invalidate();

            super.add(node);
        }

        @Override
        public boolean addAll(int index, Collection<? extends Node> nodes) {
            for (Node node : nodes) {
                addNode(node);
            }

            invalidate();

            return super.addAll(index, nodes);
        }

        private void addNode(Node node) {
            if (node == null) {
                throw new IllegalArgumentException();
            }

            if (node.getGroup() != null) {
                throw new IllegalArgumentException();
            }

            node.setGroup(Group.this);
            repaint(node.getDecoratedBounds());
        }

        @Override
        public Node remove(int index) {
            removeNode(get(index));
            invalidate();

            return super.remove(index);
        }

        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            for (int i = fromIndex; i < toIndex; i++) {
                removeNode(get(i));
            }

            invalidate();

            super.removeRange(fromIndex, toIndex);
        }

        private void removeNode(Node node) {
            node.setGroup(null);
            repaint(node.getDecoratedBounds());
        }

        @Override
        public Node set(int index, Node node) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Node> setAll(int index, Collection<? extends Node> nodes) {
            throw new UnsupportedOperationException();
        }
    }

    private static class GroupListenerList extends ListenerList<GroupListener>
        implements GroupListener {
        @Override
        public void preferredSizeChanged(Group group, int previousPreferredWidth,
            int previousPreferredHeight) {
            for (GroupListener listener : listeners()) {
                listener.preferredSizeChanged(group, previousPreferredWidth, previousPreferredHeight);
            }
        }

        @Override
        public void widthLimitsChanged(Group group, int previousMinimumWidth,
            int previousMaximumWidth) {
            for (GroupListener listener : listeners()) {
                listener.widthLimitsChanged(group, previousMinimumWidth, previousMaximumWidth);
            }
        }

        @Override
        public void heightLimitsChanged(Group group, int previousMinimumHeight,
            int previousMaximumHeight) {
            for (GroupListener listener : listeners()) {
                listener.heightLimitsChanged(group, previousMinimumHeight, previousMaximumHeight);
            }
        }

        @Override
        public void layoutChanged(Group group, Layout previousLayout) {
            for (GroupListener listener : listeners()) {
                listener.layoutChanged(group, previousLayout);
            }
        }

        @Override
        public void focusTraversalPolicyChanged(Group group,
            FocusTraversalPolicy previousFocusTraversalPolicy) {
            for (GroupListener listener : listeners()) {
                listener.focusTraversalPolicyChanged(group, previousFocusTraversalPolicy);
            }
        }
    }

    private static class GroupMouseListenerList extends ListenerList<GroupMouseListener>
        implements GroupMouseListener {
        @Override
        public boolean mouseMoved(Group group, int x, int y, boolean captured) {
            for (GroupMouseListener listener : listeners()) {
                listener.mouseMoved(group, x, y, captured);
            }

            return false;
        }

        @Override
        public boolean mousePressed(Group group, Mouse.Button button, int x, int y) {
            for (GroupMouseListener listener : listeners()) {
                listener.mousePressed(group, button, x, y);
            }

            return false;
        }

        @Override
        public boolean mouseReleased(Group group, Mouse.Button button, int x, int y) {
            for (GroupMouseListener listener : listeners()) {
                listener.mouseReleased(group, button, x, y);
            }

            return false;
        }

        @Override
        public boolean mouseWheelScrolled(Group group, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            for (GroupMouseListener listener : listeners()) {
                listener.mouseWheelScrolled(group, scrollType, scrollAmount, wheelRotation, x, y);
            }

            return false;
        }
    }

    private ObservableList<Node> nodes = new NodeList();

    private Layout layout = null;
    private FocusTraversalPolicy focusTraversalPolicy = null;

    // Preferred width and height values explicitly set by the user
    private int preferredWidth = -1;
    private int preferredHeight = -1;

    // Bounds on preferred size
    private int minimumWidth = 0;
    private int maximumWidth = Integer.MAX_VALUE;
    private int minimumHeight = 0;
    private int maximumHeight = Integer.MAX_VALUE;

    // Calculated preferred size value
    private Dimensions preferredSize = null;

    // Calculated baseline for current size
    private int baseline = -1;

    private Node mouseOverNode = null;
    private boolean mouseDown = false;
    private Node mouseDownNode = null;
    private long mouseDownTime = 0;
    private int mouseClickCount = 0;
    private boolean mouseClickConsumed = false;

    // Listener lists
    private GroupListenerList groupListeners = new GroupListenerList();
    private GroupMouseListenerList groupMouseListeners = new GroupMouseListenerList();

    public Group() {
        this(null);
    }

    public Group(Layout layout) {
        setLayout(layout);
    }

    /**
     * Returns the list of this group's child nodes.
     */
    public ObservableList<Node> getNodes() {
        return nodes;
    }

    @Override
    protected void setGroup(Group group) {
        // If this group is being removed from the node hierarchy
        // and contains the focused node, clear the focus
        if (group == null
            && containsFocus()) {
            clearFocus();
        }

        super.setGroup(group);
    }

    public Node getNodeAt(int x, int y) {
        Node node = null;

        int i = nodes.size() - 1;
        while (i >= 0) {
            node = nodes.get(i);
            if (node.isVisible()) {
                Bounds bounds = node.getBounds();
                if (bounds.contains(x, y)) {
                    break;
                }
            }

            i--;
        }

        if (i < 0) {
            node = null;
        }

        return node;
    }

    public Node getDescendantAt(int x, int y) {
        Node descendant = getNodeAt(x, y);

        if (descendant instanceof Group) {
            Group group = (Group)descendant;
            descendant = group.getDescendantAt(x - group.getX(), y - group.getY());
        }

        if (descendant == null) {
            descendant = this;
        }

        return descendant;
    }

    /**
     * Tests if this group is an ancestor of a given node. A group
     * is considered to be its own ancestor.
     *
     * @param node
     * The node to test.
     *
     * @return
     * <tt>true</tt> if this group is an ancestor of <tt>node</tt>;
     * <tt>false</tt> otherwise.
     */
    public boolean isAncestor(Node node) {
        boolean ancestor = false;

        while (node != null) {
           if (node == this) {
              ancestor = true;
              break;
           }

           node = node.getGroup();
        }

        return ancestor;
    }

    @Override
    public void setVisible(boolean visible) {
        if (!visible
            && containsFocus()) {
            clearFocus();
        }

        super.setVisible(visible);
    }

    /**
     * Returns the layout the group uses to arrange its children.
     */
    public Layout getLayout() {
        return layout;
    }

    /**
     * Sets the layout the group uses to arrange its children.
     *
     * @param layout
     * The layout the group will use to arrange its children, or <tt>null</tt>
     * for no layout.
     */
    public void setLayout(Layout layout) {
        Layout previousLayout = this.layout;
        if (previousLayout != layout) {
            this.layout = layout;
            invalidate();

            groupListeners.layoutChanged(this, previousLayout);
        }
    }

    @Override
    public Extents getExtents() {
        int minimumX = 0;
        int maximumX = 0;
        int minimumY = 0;
        int maximumY = 0;

        for (int i = 0, n = nodes.size(); i < n; i++) {
            Node node = nodes.get(i);

            if (node.isVisible()) {
                Extents extents = node.getExtents();

                minimumX = Math.min(extents.minimumX, minimumX);
                maximumX = Math.max(extents.maximumX, maximumX);
                minimumY = Math.min(extents.minimumY, minimumY);
                maximumY = Math.max(extents.maximumY, maximumY);
            }
        }

        return new Extents(minimumX, maximumX, minimumY, maximumY);
    }

    @Override
    public boolean contains(int x, int y) {
        // TODO Find node at x, y and call contains() on it

        // TODO What should we return here? Does a group always contain the point
        // if it is within the extents? IMPORTANT - we may need to cache extents
        // in this class, since we may need to use it here.

        // TODO How does contains() affect mouse notifications?

        return false;
    }

    @Override
    public boolean isFocusable() {
        return false;
    }

    /**
     * Returns the group's unconstrained preferred width.
     */
    public int getPreferredWidth() {
        return getPreferredWidth(-1);
    }

    /**
     * Returnsgrouponent's constrained preferred width.
     *
     * @param height
     * The height value by which the preferred width should be constrained, or
     * <tt>-1</tt> for no constraint.
     *
     * @return
     * The constrained preferred width.
     */
    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth;

        if (this.preferredWidth == -1) {
            if (height == -1) {
                preferredWidth = getPreferredSize().width;
            } else {
                if (preferredSize != null
                    && preferredSize.height == height) {
                    preferredWidth = preferredSize.width;
                } else {
                    preferredWidth = (layout == null) ? 0 : layout.getPreferredWidth(this, height);

                    Limits widthLimits = getWidthLimits();
                    preferredWidth = widthLimits.constrain(preferredWidth);
                }
            }
        } else {
            preferredWidth = this.preferredWidth;
        }

        return preferredWidth;
    }

    /**
     * Sets the group's preferred width.
     *
     * @param preferredWidth
     * The preferred width value, or <tt>-1</tt> to use the default
     * value determined by the skin.
     */
    public void setPreferredWidth(int preferredWidth) {
        setPreferredSize(preferredWidth, preferredHeight);
    }

    /**
     * Returns a flag indicating whether the preferred width was explicitly
     * set by the caller or is the default value determined by the skin.
     *
     * @return
     * <tt>true</tt> if the preferred width was explicitly set; <tt>false</tt>,
     * otherwise.
     */
    public boolean isPreferredWidthSet() {
        return (preferredWidth != -1);
    }

    /**
     * Returns the group's unconstrained preferred height.
     */
    public int getPreferredHeight() {
        return getPreferredHeight(-1);
    }

    /**
     * Returns the group's constrained preferred height.
     *
     * @param width
     * The width value by which the preferred height should be constrained, or
     * <tt>-1</tt> for no constraint.
     *
     * @return
     * The constrained preferred height.
     */
    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight;

        if (this.preferredHeight == -1) {
            if (width == -1) {
                preferredHeight = getPreferredSize().height;
            } else {
                if (preferredSize != null
                    && preferredSize.width == width) {
                    preferredHeight = preferredSize.height;
                } else {
                    preferredHeight = (layout == null) ? 0 : layout.getPreferredHeight(this, width);

                    Limits heightLimits = getHeightLimits();
                    preferredHeight = heightLimits.constrain(preferredHeight);
                }
            }
        } else {
            preferredHeight = this.preferredHeight;
        }

        return preferredHeight;
    }

    /**
     * Sets the group's preferred height.
     *
     * @param preferredHeight
     * The preferred height value, or <tt>-1</tt> to use the default
     * value determined by the skin.
     */
    public void setPreferredHeight(int preferredHeight) {
        setPreferredSize(preferredWidth, preferredHeight);
    }

    /**
     * Returns a flag indicating whether the preferred height was explicitly
     * set by the caller or is the default value determined by the skin.
     *
     * @return
     * <tt>true</tt> if the preferred height was explicitly set; <tt>false</tt>,
     * otherwise.
     */
    public boolean isPreferredHeightSet() {
        return (preferredHeight != -1);
    }

    /**
     * Gets the group's unconstrained preferred size.
     */
    public Dimensions getPreferredSize() {
        if (preferredSize == null) {
            if (layout == null) {
                preferredSize = new Dimensions(0, 0);
            } else {
                Dimensions preferredSize;

                if (preferredWidth == -1
                    && preferredHeight == -1) {
                    preferredSize = new Dimensions(layout.getPreferredWidth(this, preferredHeight),
                        layout.getPreferredHeight(this, preferredWidth));
                } else if (preferredWidth == -1) {
                    preferredSize = new Dimensions(layout.getPreferredWidth(this, preferredHeight),
                        preferredHeight);
                } else if (preferredHeight == -1) {
                    preferredSize = new Dimensions(preferredWidth, layout.getPreferredHeight(this,
                        preferredWidth));
                } else {
                    preferredSize = new Dimensions(preferredWidth, preferredHeight);
                }

                Limits widthLimits = getWidthLimits();
                Limits heightLimits = getHeightLimits();

                int preferredWidth = widthLimits.constrain(preferredSize.width);
                int preferredHeight = heightLimits.constrain(preferredSize.height);

                if (preferredSize.width > preferredWidth) {
                    preferredHeight = heightLimits.constrain(layout.getPreferredHeight(this, preferredWidth));
                }

                if (preferredSize.height > preferredHeight) {
                    preferredWidth = widthLimits.constrain(layout.getPreferredWidth(this, preferredHeight));
                }

                this.preferredSize = new Dimensions(preferredWidth, preferredHeight);
            }
        }

        return preferredSize;
    }

    public final void setPreferredSize(Dimensions preferredSize) {
        if (preferredSize == null) {
            throw new IllegalArgumentException("preferredSize is null.");
        }

        setPreferredSize(preferredSize.width, preferredSize.height);
    }

    /**
     * Sets the group's preferred size.
     *
     * @param preferredWidth
     * The preferred width value, or <tt>-1</tt> to use the default
     * value determined by the skin.
     *
     * @param preferredHeight
     * The preferred height value, or <tt>-1</tt> to use the default
     * value determined by the skin.
     */
    public void setPreferredSize(int preferredWidth, int preferredHeight) {
        if (preferredWidth < -1) {
            throw new IllegalArgumentException(preferredWidth
                + " is not a valid value for preferredWidth.");
        }

        if (preferredHeight < -1) {
            throw new IllegalArgumentException(preferredHeight
                + " is not a valid value for preferredHeight.");
        }

        int previousPreferredWidth = this.preferredWidth;
        int previousPreferredHeight = this.preferredHeight;

        if (previousPreferredWidth != preferredWidth
            || previousPreferredHeight != preferredHeight) {
            this.preferredWidth = preferredWidth;
            this.preferredHeight = preferredHeight;

            invalidate();

            groupListeners.preferredSizeChanged(this, previousPreferredWidth,
                previousPreferredHeight);
        }
    }

    /**
     * Returns a flag indicating whether the preferred size was explicitly
     * set by the caller or is the default value determined by the skin.
     *
     * @return
     * <tt>true</tt> if the preferred size was explicitly set; <tt>false</tt>,
     * otherwise.
     */
    public boolean isPreferredSizeSet() {
        return isPreferredWidthSet()
            && isPreferredHeightSet();
    }

    /**
     * Returns the minimum width of this group.
     */
    public int getMinimumWidth() {
        return minimumWidth;
    }

    /**
     * Sets the minimum width of this group.
     *
     * @param minimumWidth
     */
    public void setMinimumWidth(int minimumWidth) {
        setWidthLimits(minimumWidth, getMaximumWidth());
    }

    /**
     * Returns the maximum width of this group.
     */
    public int getMaximumWidth() {
        return maximumWidth;
    }

    /**
     * Sets the maximum width of this group.
     *
     * @param maximumWidth
     */
    public void setMaximumWidth(int maximumWidth) {
        setWidthLimits(getMinimumWidth(), maximumWidth);
    }

    /**
     * Returns the width limits for this group.
     */
    public Limits getWidthLimits() {
        return new Limits(minimumWidth, maximumWidth);
    }

    /**
     * Sets the width limits for this group.
     *
     * @param minimumWidth
     * @param maximumWidth
     */
    public void setWidthLimits(int minimumWidth, int maximumWidth) {
        int previousMinimumWidth = this.minimumWidth;
        int previousMaximumWidth = this.maximumWidth;

        if (previousMinimumWidth != minimumWidth
            || previousMaximumWidth != maximumWidth) {
            if (minimumWidth < 0) {
                throw new IllegalArgumentException("minimumWidth is negative.");
            }

            if (minimumWidth > maximumWidth) {
                throw new IllegalArgumentException("minimumWidth is greater than maximumWidth.");
            }

            this.minimumWidth = minimumWidth;
            this.maximumWidth = maximumWidth;

            invalidate();

            groupListeners.widthLimitsChanged(this, previousMinimumWidth, previousMaximumWidth);
        }
    }

    /**
     * Sets the width limits for this group.
     *
     * @param widthLimits
     */
    public final void setWidthLimits(Limits widthLimits) {
        if (widthLimits == null) {
            throw new IllegalArgumentException("widthLimits is null.");
        }

        setWidthLimits(widthLimits.minimum, widthLimits.maximum);
    }

    /**
     * Returns the minimum height of this group.
     */
    public int getMinimumHeight() {
        return minimumHeight;
    }

    /**
     * Sets the minimum height of this group.
     *
     * @param minimumHeight
     */
    public void setMinimumHeight(int minimumHeight) {
        setHeightLimits(minimumHeight, getMaximumHeight());
    }

    /**
     * Returns the maximum height of this group.
     */
    public int getMaximumHeight() {
        return maximumHeight;
    }

    /**
     * Sets the maximum height of this group.
     *
     * @param maximumHeight
     */
    public void setMaximumHeight(int maximumHeight) {
        setHeightLimits(getMinimumHeight(), maximumHeight);
    }

    /**
     * Returns the height limits for this group.
     */
    public Limits getHeightLimits() {
        return new Limits(minimumHeight, maximumHeight);
    }

    /**
     * Sets the height limits for this group.
     *
     * @param minimumHeight
     * @param maximumHeight
     */
    public void setHeightLimits(int minimumHeight, int maximumHeight) {
        int previousMinimumHeight = this.minimumHeight;
        int previousMaximumHeight = this.maximumHeight;

        if (previousMinimumHeight != minimumHeight
            || previousMaximumHeight != maximumHeight) {
            if (minimumHeight < 0) {
                throw new IllegalArgumentException("minimumHeight is negative.");
            }

            if (minimumHeight > maximumHeight) {
                throw new IllegalArgumentException("minimumHeight is greater than maximumHeight.");
            }

            this.minimumHeight = minimumHeight;
            this.maximumHeight = maximumHeight;

            invalidate();

            groupListeners.heightLimitsChanged(this, previousMinimumHeight, previousMaximumHeight);
        }
    }

    /**
     * Sets the height limits for this group.
     *
     * @param heightLimits
     */
    public final void setHeightLimits(Limits heightLimits) {
        if (heightLimits == null) {
            throw new IllegalArgumentException("heightLimits is null.");
        }

        setHeightLimits(heightLimits.minimum, heightLimits.maximum);
    }

    @Override
    public int getBaseline(int width, int height) {
        return (layout == null) ? -1 : layout.getBaseline(this, width, height);
    }

    /**
     * Returns the group's baseline.
     *
     * @return
     * The baseline relative to the origin of this group, or <tt>-1</tt> if
     * this group does not have a baseline.
     */
    public int getBaseline() {
        if (baseline == -1) {
            baseline = getBaseline(getWidth(), getHeight());
        }

        return baseline;
    }

    // ----------------------------------------

    @Override
    public void layout() {
        if (layout != null) {
            layout.layout(this);
        }

        for (int i = 0, n = nodes.size(); i < n; i++) {
            Node node = nodes.get(i);
            node.validate();
        }
    }

    @Override
    public void invalidate() {
        // Clear the preferred size and baseline
        preferredSize = null;
        baseline = -1;

        super.invalidate();
    }

    @Override
    public void paint(Graphics graphics) {
        Bounds clipBounds = graphics.getClipBounds();

        for (int i = 0, n = nodes.size(); i < n; i++) {
            Node node = nodes.get(i);

            // Only paint nodes that are visible and intersect the
            // current clip rectangle
            if (node.isVisible()
                && node.getDecoratedBounds().intersects(clipBounds)) {
                paintNode(graphics, node);
            }
        }
    }

    private void paintNode(Graphics graphics, Node node) {
        // TODO Transform graphics before passing to node, etc.

        Bounds nodeBounds = node.getBounds();

        // Create a copy of the current graphics context and
        // translate to the node's coordinate system
        Graphics decoratedGraphics = graphics.create();
        decoratedGraphics.translate(nodeBounds.x, nodeBounds.y);

        // Prepare the decorators
        List<Decorator> decorators = node.getDecorators();
        int n = decorators.size();

        for (int j = n - 1; j >= 0; j--) {
            Decorator decorator = decorators.get(j);
            decoratedGraphics = decorator.prepare(node, decoratedGraphics);
        }

        // Paint the node
        Graphics nodeGraphics = decoratedGraphics.create();
        if (node.getClip()) {
            nodeGraphics.clip(0, 0, nodeBounds.width, nodeBounds.height);
        }

        node.paint(nodeGraphics);
        nodeGraphics.dispose();

        // Update the decorators
        for (int j = 0; j < n; j++) {
            Decorator decorator = decorators.get(j);
            decorator.update();
        }
    }

    /**
     * Requests that focus be given to this group. If this group is not
     * focusable, this requests that focus be set to the first focusable
     * descendant in this group.
     *
     * @return
     * The node that got the focus, or <tt>null</tt> if the focus request
     * was denied
     */
    @Override
    public boolean requestFocus() {
        boolean focused = false;

        if (isFocusable()) {
            focused = super.requestFocus();
        } else {
            if (focusTraversalPolicy != null) {
                Node first = focusTraversalPolicy.getNextNode(this, null, FocusTraversalDirection.FORWARD);

                Node node = first;
                while (node != null
                    && !node.requestFocus()) {
                    node = focusTraversalPolicy.getNextNode(this, node, FocusTraversalDirection.FORWARD);

                    // Ensure that we don't get into an infinite loop
                    if (node == first) {
                        break;
                    }
                }

                focused = (node != null);
            }
        }

        return focused;
    }

    /**
     * Transfers focus to the next focusable node.
     *
     * @param node
     * The node from which focus will be transferred.
     *
     * @param direction
     * The direction in which to transfer focus.
     */
    public Node transferFocus(Node node, FocusTraversalDirection direction) {
        if (focusTraversalPolicy == null) {
            // The group has no traversal policy; move up a level
            node = transferFocus(direction);
        } else {
            do {
                node = focusTraversalPolicy.getNextNode(this, node, direction);

                if (node != null) {
                    if (node.isFocusable()) {
                        node.requestFocus();
                    } else {
                        if (node instanceof Group) {
                            Group group = (Group)node;
                            node = group.transferFocus(null, direction);
                        }
                    }
                }
            } while (node != null
                && !node.isFocused());

            if (node == null) {
                // We are at the end of the traversal
                node = transferFocus(direction);
            }
        }

        return node;
    }

    /**
     * Returns this group's focus traversal policy.
     */
    public FocusTraversalPolicy getFocusTraversalPolicy() {
        return this.focusTraversalPolicy;
    }

    /**
     * Sets this group's focus traversal policy.
     *
     * @param focusTraversalPolicy
     * The focus traversal policy to use with this group.
     */
    public void setFocusTraversalPolicy(FocusTraversalPolicy focusTraversalPolicy) {
        FocusTraversalPolicy previousFocusTraversalPolicy = this.focusTraversalPolicy;

        if (previousFocusTraversalPolicy != focusTraversalPolicy) {
            this.focusTraversalPolicy = focusTraversalPolicy;
            groupListeners.focusTraversalPolicyChanged(this, previousFocusTraversalPolicy);
        }
    }

    /**
     * Tests whether this group is an ancestor of the currently focused
     * node.
     *
     * @return
     * <tt>true</tt> if a node is focused and this group is an
     * ancestor of the node; <tt>false</tt>, otherwise.
     */
    public boolean containsFocus() {
        Node focusedNode = getFocusedNode();
        return (focusedNode != null
            && isAncestor(focusedNode));
    }

    protected void descendantAdded(Node descendant) {
        Group group = getGroup();

        if (group != null) {
            group.descendantAdded(descendant);
        }
    }

    protected void descendantRemoved(Node descendant) {
        Group group = getGroup();

        if (group != null) {
            group.descendantRemoved(descendant);
        }
    }

    protected void descendantGainedFocus(Node descendant) {
        Group group = getGroup();

        if (group != null) {
            group.descendantGainedFocus(descendant);
        }
    }

    protected void descendantLostFocus(Node descendant) {
        Group group = getGroup();

        if (group != null) {
            group.descendantLostFocus(descendant);
        }
    }

    @Override
    protected boolean mouseMoved(int x, int y, boolean captured) {
        boolean consumed = false;

        // Clear the mouse over node if its mouse-over state has
        // changed (e.g. if its enabled or visible properties have
        // changed)
        if (mouseOverNode != null
            && !mouseOverNode.isMouseOver()) {
            mouseOverNode = null;
        }

        if (isEnabled()) {
            // Synthesize mouse over/out events
            Node node = getNodeAt(x, y);

            if (mouseOverNode != node) {
                if (mouseOverNode != null) {
                    mouseOverNode.mouseExited();
                }

                mouseOverNode = null;
            }

            // Notify group listeners
            consumed = groupMouseListeners.mouseMoved(this, x, y, captured);

            if (!consumed) {
                if (mouseOverNode != node) {
                    mouseOverNode = node;

                    if (mouseOverNode!= null) {
                        mouseOverNode.mouseEntered();
                    }
                }

                // Propagate event to subnodes
                if (node != null) {
                    // TODO Transform coordinates before passing to node
                    consumed = node.mouseMoved(x - node.getX(), y - node.getY(), captured);
                }

                // Notify the base class
                if (!consumed) {
                    consumed = super.mouseMoved(x, y, captured);
                }
            }
        }

        return consumed;
    }

    @Override
    protected void mouseExited() {
        // Ensure that mouse out is called on descendant nodes
        if (mouseOverNode != null
            && mouseOverNode.isMouseOver()) {
            mouseOverNode.mouseExited();
        }

        mouseOverNode = null;

        super.mouseExited();
    }

    @Override
    protected boolean mousePressed(Mouse.Button button, int x, int y) {
        boolean consumed = false;

        mouseDown = true;

        if (isEnabled()) {
            // Notify group listeners
            consumed = groupMouseListeners.mousePressed(this, button, x, y);

            if (!consumed) {
                // Synthesize mouse click event
                Node node = getNodeAt(x, y);

                long currentTime = System.currentTimeMillis();
                int multiClickInterval = Mouse.getMultiClickInterval();
                if (mouseDownNode == node
                    && currentTime - mouseDownTime < multiClickInterval) {
                    mouseClickCount++;
                } else {
                    mouseDownTime = System.currentTimeMillis();
                    mouseClickCount = 1;
                }

                mouseDownNode = node;

                // Propagate event to subnodes
                if (node != null) {
                    // Ensure that mouse over is called
                    if (!node.isMouseOver()) {
                        node.mouseEntered();
                    }

                    // TODO Transform coordinates before passing to node
                    consumed = node.mousePressed(button, x - node.getX(), y - node.getY());
                }

                // Notify the base class
                if (!consumed) {
                    consumed = super.mousePressed(button, x, y);
                }
            }
        }

        return consumed;
    }

    @Override
    protected boolean mouseReleased(Mouse.Button button, int x, int y) {
        boolean consumed = false;

        if (isEnabled()) {
            // Notify group listeners
            consumed = groupMouseListeners.mouseReleased(this, button, x, y);

            if (!consumed) {
                // Propagate event to subnodes
                Node node = getNodeAt(x, y);

                if (node != null) {
                    // Ensure that mouse over is called
                    if (!node.isMouseOver()) {
                        node.mouseEntered();
                    }

                    // TODO Transform coordinates before passing to node
                    consumed = node.mouseReleased(button, x - node.getX(), y - node.getY());
                }

                // Notify the base class
                if (!consumed) {
                    consumed = super.mouseReleased(button, x, y);
                }

                // Synthesize mouse click event
                if (mouseDown
                    && node != null
                    && node == mouseDownNode
                    && node.isEnabled()
                    && node.isVisible()) {

                    // TODO Transform coordinates before passing to node (consolidate with
                    // above?)
                    mouseClickConsumed = node.mouseClicked(button, x - node.getX(),
                        y - node.getY(), mouseClickCount);
                }
            }
        }

        mouseDown = false;

        return consumed;
    }

    @Override
    protected boolean mouseClicked(Mouse.Button button, int x, int y, int count) {
        if (isEnabled()) {
            if (!mouseClickConsumed) {
                // Allow the event to propagate
                mouseClickConsumed = super.mouseClicked(button, x, y, count);
            }
        }

        return mouseClickConsumed;
    }

    @Override
    protected boolean mouseWheelScrolled(Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        boolean consumed = false;

        if (isEnabled()) {
            // Notify group listeners
            consumed = groupMouseListeners.mouseWheelScrolled(this, scrollType, scrollAmount,
                wheelRotation, x, y);

            if (!consumed) {
                // Propagate event to subnodes
                Node node = getNodeAt(x, y);

                if (node != null) {
                    // Ensure that mouse over is called
                    if (!node.isMouseOver()) {
                        node.mouseEntered();
                    }

                    // TODO Transform coordinates before passing to node
                    consumed = node.mouseWheelScrolled(scrollType, scrollAmount, wheelRotation,
                        x - node.getX(), y - node.getY());
                }

                // Notify the base class
                if (!consumed) {
                    consumed = super.mouseWheelScrolled(scrollType, scrollAmount,
                        wheelRotation, x, y);
                }
            }
        }

        return consumed;
    }

    public ListenerList<GroupListener> getGroupListeners() {
        return groupListeners;
    }

    public ListenerList<GroupMouseListener> getGroupMouseListeners() {
        return groupMouseListeners;
    }
}
