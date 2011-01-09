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

import org.apache.pivot.scene.Keyboard.KeyLocation;
import org.apache.pivot.scene.Mouse.Button;
import org.apache.pivot.scene.effect.Decorator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.ObservableList;
import org.apache.pivot.util.ObservableListAdapter;

/**
 * Abstract base class for nodes.
 */
public abstract class Node implements Visual {
    private static class NodeListenerList extends ListenerList<NodeListener>
        implements NodeListener {
        @Override
        public void locationChanged(Node node, int previousX, int previousY) {
            for (NodeListener listener : listeners()) {
                listener.locationChanged(node, previousX, previousY);
            }
        }

        @Override
        public void sizeChanged(Node node, int previousWidth, int previousHeight) {
            for (NodeListener listener : listeners()) {
                listener.sizeChanged(node, previousWidth, previousHeight);
            }
        }

        @Override
        public void visibleChanged(Node node) {
            for (NodeListener listener : listeners()) {
                listener.visibleChanged(node);
            }
        }

        @Override
        public void clipChanged(Node node) {
            for (NodeListener listener : listeners()) {
                listener.clipChanged(node);
            }
        }
    }

    private static class NodeStateListenerList extends ListenerList<NodeStateListener>
        implements NodeStateListener {
        @Override
        public void enabledChanged(Node node) {
            for (NodeStateListener listener : listeners()) {
                listener.enabledChanged(node);
            }
        }

        @Override
        public void focusedChanged(Node node, Node obverseNode) {
            for (NodeStateListener listener : listeners()) {
                listener.focusedChanged(node, obverseNode);
            }
        }
    }

    private static class NodeMouseListenerList extends ListenerList<NodeMouseListener>
        implements NodeMouseListener {
        @Override
        public void mouseEntered(Node node) {
            for (NodeMouseListener listener : listeners()) {
                listener.mouseEntered(node);
            }
        }

        @Override
        public void mouseExited(Node node) {
            for (NodeMouseListener listener : listeners()) {
                listener.mouseExited(node);
            }
        }

        @Override
        public boolean mouseMoved(Node node, int x, int y, boolean captured) {
            boolean consumed = false;

            for (NodeMouseListener listener : listeners()) {
                consumed |= listener.mouseMoved(node, x, y, captured);
            }

            return consumed;
        }
    }

    private static class NodeMouseButtonListenerList extends ListenerList<NodeMouseButtonListener>
        implements NodeMouseButtonListener {
        @Override
        public boolean mousePressed(Node node, Button button, int x, int y) {
            boolean consumed = false;

            for (NodeMouseButtonListener listener : listeners()) {
                consumed |= listener.mousePressed(node, button, x, y);
            }

            return consumed;
        }

        @Override
        public boolean mouseReleased(Node node, Button button, int x, int y) {
            boolean consumed = false;

            for (NodeMouseButtonListener listener : listeners()) {
                consumed |= listener.mouseReleased(node, button, x, y);
            }

            return consumed;
        }

        @Override
        public boolean mouseClicked(Node node, Button button, int x, int y, int count) {
            boolean consumed = false;

            for (NodeMouseButtonListener listener : listeners()) {
                consumed |= listener.mouseClicked(node, button, x, y, count);
            }

            return consumed;
        }
    }

    private static class NodeMouseWheelListenerList extends ListenerList<NodeMouseWheelListener>
        implements NodeMouseWheelListener {
        @Override
        public boolean mouseWheelScrolled(Node node, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            boolean consumed = false;

            for (NodeMouseWheelListener listener : listeners()) {
                consumed |= listener.mouseWheelScrolled(node, scrollType, scrollAmount,
                    wheelRotation, x, y);
            }

            return consumed;
        }
    }

    private static class NodeKeyListenerList extends ListenerList<NodeKeyListener>
        implements NodeKeyListener {
        @Override
        public boolean keyPressed(Node node, int keyCode, KeyLocation keyLocation) {
            boolean consumed = false;

            for (NodeKeyListener listener : listeners()) {
                consumed |= listener.keyPressed(node, keyCode, keyLocation);
            }

            return consumed;
        }

        @Override
        public boolean keyReleased(Node node, int keyCode, KeyLocation keyLocation) {
            boolean consumed = false;

            for (NodeKeyListener listener : listeners()) {
                consumed |= listener.keyReleased(node, keyCode, keyLocation);
            }

            return consumed;
        }

        @Override
        public boolean keyTyped(Node node, char character) {
            boolean consumed = false;

            for (NodeKeyListener listener : listeners()) {
                consumed |= listener.keyTyped(node, character);
            }

            return consumed;
        }
    }

    private static class NodeClassListenerList extends ListenerList<NodeClassListener>
        implements NodeClassListener {
        @Override
        public void focusedNodeChanged(Node previousFocusedNode) {
            for (NodeClassListener listener : listeners()) {
                listener.focusedNodeChanged(previousFocusedNode);
            }
        }
    }

    // The node's parent, or null if the node does not have a parent
    private Group group = null;

    // The node's location, relative to the parent's origin
    private int x = 0;
    private int y = 0;

    // The node's size
    private int width = 0;
    private int height = 0;

    // The node's visible flag
    private boolean visible = false;

    // The node's clip flag
    private boolean clip = false;

    // The node's valid state
    private boolean valid = false;

    // The node's decorators
    // TODO Use an inner class that will call repaint() as needed
    private ObservableList<Decorator> decorators = ObservableListAdapter.observableArrayList();

    // The node's enabled flag
    private boolean enabled = true;

    // The current mouse location
    private Point mouseLocation = null;

    // Instance listener lists
    private NodeListenerList nodeListeners = new NodeListenerList();
    private NodeStateListenerList nodeStateListeners = new NodeStateListenerList();
    private NodeMouseListenerList nodeMouseListeners = new NodeMouseListenerList();
    private NodeMouseButtonListenerList nodeMouseButtonListeners = new NodeMouseButtonListenerList();
    private NodeMouseWheelListenerList nodeMouseWheelListeners = new NodeMouseWheelListenerList();
    private NodeKeyListenerList nodeKeyListeners = new NodeKeyListenerList();

    // The currently focused node
    private static Node focusedNode = null;

    // Class listener list
    private static NodeClassListenerList nodeClassListeners = new NodeClassListenerList();

    /**
     * Returns the group that contains this node.
     *
     * @return
     * The group that contains the node, or <tt>null</tt> if this node is
     * not currently attached to a group.
     */
    public Group getGroup() {
        return group;
    }

    /**
     * Sets the group that will contain this node.
     *
     * @param group
     * The group that will contain the node, or <tt>null</tt> to remove
     * the node from a group.
     */
    protected void setGroup(Group group) {
        // If this node is being removed from the scene graph
        // and is currently focused, clear the focus
        if (group == null
            && isFocused()) {
            clearFocus();
        }

        Group previousGroup = this.group;
        this.group = group;

        if (previousGroup != null) {
            previousGroup.descendantRemoved(this);
        }

        if (group != null) {
            group.descendantAdded(this);
        }
    }

    /**
     * Returns the stage that is hosting this node.
     *
     * @return
     * The stage that is hosting the node, or <tt>null</tt> if this node
     * is not currently attached to a stage.
     */
    public Stage getStage() {
        return (Stage)getAncestor(Stage.class);
    }

    public Group getAncestor(Class<? extends Group> ancestorType) {
        Node node = this;

        while (node != null
            && !(ancestorType.isInstance(node))) {
            node = node.getGroup();
        }

        return (Group)node;
    }

    @SuppressWarnings("unchecked")
    public Group getAncestor(String ancestorTypeName) throws ClassNotFoundException {
        if (ancestorTypeName == null) {
            throw new IllegalArgumentException();
        }

        return getAncestor((Class<? extends Group>)Class.forName(ancestorTypeName));
    }

    /**
     * Returns the node's x-coordinate.
     *
     * @return
     * The node's horizontal position relative to the origin of the
     * parent group.
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the node's x-coordinate.
     *
     * @param x
     * The node's horizontal position relative to the origin of the
     * parent group.
     */
    public void setX(int x) {
        setLocation(x, getY());
    }

    /**
     * Returns the node's y-coordinate.
     *
     * @return
     * The node's vertical position relative to the origin of the
     * parent group.
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the node's y-coordinate.
     *
     * @param y
     * The node's vertical position relative to the origin of the
     * parent group.
     */
    public void setY(int y) {
        setLocation(getX(), y);
    }

    /**
     * Returns the node's location.
     *
     * @return
     * A point value containing the node's horizontal and vertical
     * position relative to the origin of the parent group.
     */
    public Point getLocation() {
        return new Point(getX(), getY());
    }

    /**
     * Sets the node's location.
     *
     * @param x
     * The node's horizontal position relative to the origin of the
     * parent group.
     *
     * @param y
     * The node's vertical position relative to the origin of the
     * parent group.
     */
    public void setLocation(int x, int y) {
        int previousX = this.x;
        int previousY = this.y;

        if (previousX != x
            || previousY != y) {
            // Redraw the region formerly occupied by this node
            if (group != null) {
                group.repaint(getDecoratedBounds());
            }

            // Set the new coordinates
            this.x = x;
            this.y = y;

            // Redraw the region currently occupied by this node
            if (group != null) {
                group.repaint(getDecoratedBounds());
            }

            nodeListeners.locationChanged(this, previousX, previousY);
        }
    }

    /**
     * Sets the node's location.
     *
     * @param location
     * A point value containing the node's horizontal and vertical
     * position relative to the origin of the parent group.
     *
     * @see #setLocation(int, int)
     */
    public final void setLocation(Point location) {
        if (location == null) {
            throw new IllegalArgumentException("location cannot be null.");
        }

        setLocation(location.x, location.y);
    }

    @Override
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        setSize(width, getHeight());
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        setSize(getWidth(), height);
    }

    public Dimensions getSize() {
        return new Dimensions(this.getWidth(), this.getHeight());
    }

    public final void setSize(Dimensions size) {
        if (size == null) {
            throw new IllegalArgumentException("size is null.");
        }

        setSize(size.width, size.height);
    }

    @Override
    public void setSize(int width, int height) {
        if (width < 0) {
            throw new IllegalArgumentException("width is negative.");
        }

        if (height < 0) {
            throw new IllegalArgumentException("height is negative.");
        }

        int previousWidth = getWidth();
        int previousHeight = getHeight();

        if (width != previousWidth
            || height != previousHeight) {
            // This node's size changed, most likely as a result
            // of being laid out; it must be flagged as invalid to ensure
            // that layout is propagated downward when validate() is
            // called on it
            invalidate();

            // Redraw the region formerly occupied by this node
            if (group != null) {
                group.repaint(getDecoratedBounds());
            }

            // Set the size
            this.width = width;
            this.height = height;

            // Redraw the region currently occupied by this node
            if (group != null) {
                group.repaint(getDecoratedBounds());
            }

            nodeListeners.sizeChanged(this, previousWidth, previousHeight);
        }
    }


    /**
     * Returns the node's bounding area.
     *
     * @return
     * The node's bounding area. The <tt>x</tt> and <tt>y</tt> values are
     * relative to the parent group.
     */
    public Bounds getBounds() {
        // TODO This is the transformed bounds of the node, in the parent's
        // coordinate space. It is determined by transforming the node's
        // extents and mapping to parent coordinates. If clip is true, it
        // will also be constrained to the untransformed bounds of the node.

        // TODO If null, recalculate (set to null in invalidate())

        return new Bounds(x, y, getWidth(), getHeight());
    }

    /**
     * Returns the node's decorated bounding area.
     *
     * @return
     * TODO
     */
    public Bounds getDecoratedBounds() {
        // TODO
        return getBounds();
    }

    /**
     * Returns the node's extents. These are the maximum and minimum x and y values
     * in the node's coordinate space.
     * <p>
     * This method will only be called as needed, so implementations do not need to
     * cache the value.
     */
    public abstract Extents getExtents();

    /**
     * Determines if the node contains a given location. This method facilitates
     * mouse interaction with non-rectangular nodes.
     *
     * @param x
     * @param y
     *
     * @return
     * <tt>true</tt> if the node's shape contains the given location; <tt>false</tt>,
     * otherwise.
     *
     * @throws UnsupportedOperationException
     * This method is not currently implemented.
     */
    public abstract boolean contains(int x, int y);

    /**
     * Returns the node's visibility.
     *
     * @return
     * <tt>true</tt> if the node will be painted; <tt>false</tt>,
     * otherwise.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets the node's visibility.
     *
     * @param visible
     * <tt>true</tt> if the node should be painted; <tt>false</tt>,
     * otherwise.
     */
    public void setVisible(boolean visible) {
        if (this.visible != visible) {
            // If this node is being hidden and has the focus, clear
            // the focus
            if (!visible) {
                if (isFocused()) {
                    clearFocus();
                }

                // Ensure that the mouse out event is processed
                if (isMouseOver()) {
                    mouseExited();
                }
            }

            // Redraw the region formerly occupied by this node
            if (group != null) {
                group.repaint(getDecoratedBounds());
            }

            this.visible = visible;

            // Redraw the region currently occupied by this node
            if (group != null) {
                group.repaint(getDecoratedBounds());
            }

            // Ensure the layout is valid
            if (visible
                && !valid) {
                validate();
            }

            // Invalidate the parent
            if (group != null) {
                group.invalidate();
            }

            nodeListeners.visibleChanged(this);
        }
    }

    /**
     * Determines if this node is showing. To be showing, the node
     * and all of its ancestors must be visible and attached to a display.
     *
     * @return
     * <tt>true</tt> if this node is showing; <tt>false</tt> otherwise.
     */
    public boolean isShowing() {
        Node node = this;

        while (node != null
            && node.isVisible()
            && !(node instanceof Stage)) {
            node = node.getGroup();
        }

        return (node != null
            && node.isVisible());
    }

    /**
     * Returns the node's clip flag.
     *
     * @return
     * <tt>true</tt> if the node should be clipped when painted;
     * <tt>false</tt>, otherwise.
     */
    public boolean getClip() {
        return clip;
    }

    /**
     * Sets the node's clip flag.
     *
     * @param clip
     * <tt>true</tt> if the node should be clipped when painted;
     * <tt>false</tt>, otherwise.
     */
    public void setClip(boolean clip) {
        if (this.clip != clip) {
            this.clip = clip;
            invalidate();

            nodeListeners.clipChanged(this);
        }
    }

    /**
     * Returns the node's decorator list.
     */
    public ObservableList<Decorator> getDecorators() {
        return decorators;
    }

    /**
     * Returns the node's enabled state.
     *
     * @return
     * <tt>true</tt> if the node is enabled; <tt>false</tt>, otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the node's enabled state. Enabled nodes respond to user
     * input events; disabled nodes do not.
     *
     * @param enabled
     * <tt>true</tt> if the node is enabled; <tt>false</tt>, otherwise.
     */
    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            if (!enabled) {
                // If this node has the focus, clear it
                if (isFocused()) {
                    clearFocus();
                }

                // Ensure that the mouse out event is processed
                if (isMouseOver()) {
                    mouseExited();
                }
            }

            this.enabled = enabled;

            nodeStateListeners.enabledChanged(this);
        }
    }

    /**
     * Determines if this node is blocked. A node is blocked if the
     * node or any of its ancestors is disabled.
     *
     * @return
     * <tt>true</tt> if the node is blocked; <tt>false</tt>, otherwise.
     */
    public boolean isBlocked() {
        boolean blocked = false;

        Node node = this;

        while (node != null
            && !blocked) {
            blocked = !node.isEnabled();
            node = node.getGroup();
        }

        return blocked;
    }

    /**
     * Determines if the mouse is positioned over this node.
     *
     * @return
     * <tt>true</tt> if the mouse is currently located over this node;
     * <tt>false</tt>, otherwise.
     */
    public boolean isMouseOver() {
        return (mouseLocation != null);
    }

    /**
     * Returns the current mouse location in the node's coordinate space.
     *
     * @return
     * The current mouse location, or <tt>null</tt> if the mouse is not
     * currently positioned over this node.
     */
    public Point getMouseLocation() {
        return mouseLocation;
    }

    /**
     * Maps a point in this node's coordinate system to the specified
     * ancestor's coordinate space.
     *
     * @param x
     * The x-coordinate in this node's coordinate space
     *
     * @param y
     * The y-coordinate in this node's coordinate space
     *
     * @return
     * A point containing the translated coordinates, or <tt>null</tt> if the
     * node is not a descendant of the specified ancestor.
     */
    public Point mapPointToAncestor(Group ancestor, int x, int y) {
        if (ancestor == null) {
            throw new IllegalArgumentException("ancestor is null");
        }

        Point coordinates = null;

        Node node = this;

        while (node != null
            && coordinates == null) {
            if (node == ancestor) {
                coordinates = new Point(x, y);
            } else {
                x += node.x;
                y += node.y;

                node = node.getGroup();
            }
        }

        return coordinates;
    }

    public Point mapPointToAncestor(Group ancestor, Point location) {
        if (location == null) {
            throw new IllegalArgumentException();
        }

        return mapPointToAncestor(ancestor, location.x, location.y);
    }

    /**
     * Maps a point in the specified ancestor's coordinate space to this
     * node's coordinate system.
     *
     * @param x
     * The x-coordinate in the ancestors's coordinate space.
     *
     * @param y
     * The y-coordinate in the ancestor's coordinate space.
     *
     * @return
     * A point containing the translated coordinates, or <tt>null</tt> if the
     * node is not a descendant of the specified ancestor.
     */
    public Point mapPointFromAncestor(Group ancestor, int x, int y) {
        if (ancestor == null) {
            throw new IllegalArgumentException("ancestor is null");
        }

        Point coordinates = null;

        Node node = this;

        while (node != null
            && coordinates == null) {
            if (node == ancestor) {
                coordinates = new Point(x, y);
            } else {
                x -= node.x;
                y -= node.y;

                node = node.getGroup();
            }
        }

        return coordinates;
    }

    public Point mapPointFromAncestor(Group ancestor, Point location) {
        if (location == null) {
            throw new IllegalArgumentException();
        }

        return mapPointFromAncestor(ancestor, location.x, location.y);
    }

    /**
     * Returns this node's focusability.
     *
     * @return
     * <tt>true</tt> if the node is capable of receiving the focus;
     * <tt>false</tt>, otherwise.
     */
    public abstract boolean isFocusable();

    /**
     * Returns the node's focused state.
     *
     * @return
     * <tt>true</tt> if the node has the input focus; <tt>false</tt>
     * otherwise.
     */
    public boolean isFocused() {
        return (focusedNode == this);
    }

    /**
     * Called to notify a node that its focus state has changed.
     *
     * @param focused
     * <tt>true</tt> if the node has received the input focus;
     * <tt>false</tt> if the node has lost the focus.
     *
     * @param obverseComponent
     * If <tt>focused</tt> is true, the node that has lost the focus;
     * otherwise, the node that has gained the focus.
     */
    protected void setFocused(boolean focused, Node obverseNode) {
        if (focused) {
            group.descendantGainedFocus(this);
        } else {
            group.descendantLostFocus(this);
        }

        nodeStateListeners.focusedChanged(this, obverseNode);
    }

    /**
     * Requests that focus be given to this node. In order to receive the focus,
     * the node must be focusable, showing, and unblocked.
     *
     * @return
     * <tt>true</tt> if the node gained the focus; <tt>false</tt>
     * otherwise.
     */
    public boolean requestFocus() {
        boolean focusable = isFocusable();

        if (focusable) {
            Node node = this;

            while (focusable
                && node != null
                && !(node instanceof Stage)) {
                focusable = node.isVisible()
                    && isEnabled();

                node = node.getGroup();
                focusable &= node != null;
            }
        }

        if (focusable) {
            setFocusedNode(this);
            getStage().requestNativeFocus();
        }

        return isFocused();
    }

    /**
     * Transfers focus to the next focusable node.
     *
     * @param direction
     * The direction in which to transfer focus.
     */
    public Node transferFocus(FocusTraversalDirection direction) {
        Node node = null;

        Group group = getGroup();
        if (group != null) {
            node = group.transferFocus(this, direction);
        }

        return node;
    }

    /**
     * Lays out the node's contents.
     */
    public abstract void layout();

    /**
     * Returns the node's valid state.
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Repaints the node and flags the node's hierarchy as invalid.
     */
    public void invalidate() {
        valid = false;

        // Repaint the area this node previously occupied
        repaint();

        if (group != null) {
            group.invalidate();
        }
    }

    /**
     * Repaints and lays out the node.
     */
    public void validate() {
        if (!valid
            && visible) {
            // Repaint the area this node currently occupies
            repaint();

            layout();
            valid = true;
        }
    }

    /**
     * Flags the entire node as needing to be repainted.
     */
    public final void repaint() {
        repaint(false);
    }

    /**
     * Flags the entire node as needing to be repainted.
     *
     * @param immediate
     */
    public final void repaint(boolean immediate) {
        repaint(getBounds(), immediate);
    }

    /**
     * Flags an area as needing to be repainted.
     *
     * @param area
     */
    public final void repaint(Bounds area) {
        repaint(area, false);
    }

    /**
     * Flags an area as needing to be repainted or repaints the rectangle
     * immediately.
     *
     * @param area
     * @param immediate
     */
    public final void repaint(Bounds area, boolean immediate) {
        if (area == null) {
            throw new IllegalArgumentException("area is null.");
        }

        repaint(area.x, area.y, area.width, area.height, immediate);
    }

    /**
     * Flags an area as needing to be repainted.
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public final void repaint(int x, int y, int width, int height) {
        repaint(x, y, width, height, false);
    }

    /**
     * Flags an area as needing to be repainted.
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @param immediate
     */
    public void repaint(int x, int y, int width, int height, boolean immediate) {
        if (group != null) {
            // Constrain the repaint area to this node's bounds
            // TODO Only constrain if clipped
            int top = y;
            int left = x;
            int bottom = top + height - 1;
            int right = left + width - 1;

            x = Math.max(left, 0);
            y = Math.max(top, 0);
            width = Math.min(right, getWidth() - 1) - x + 1;
            height = Math.min(bottom, getHeight() - 1) - y + 1;

            if (width > 0
                && height > 0) {
                // Notify the parent that the region needs updating
                // TODO Apply node's transform before propagating to parent
                group.repaint(x + this.x, y + this.y, width, height, immediate);

                // Repaint any affected decorators
                for (Decorator decorator : decorators) {
                    Transform transform = decorator.getTransform(this);

                    if (!transform.isIdentity()) {
                        // TODO
                    }
                }
            }
        }
    }

    /**
     * Creates a graphics context for this node. This graphics context
     * will not be double buffered. In other words, drawing operations on it
     * will operate directly on the video RAM.
     *
     * @return
     * A graphics context for this node, or <tt>null</tt> if this
     * node is not showing.
     *
     * @see #isShowing()
     */
    public Graphics getGraphics() {
        // TODO Get host graphics and translate to the bounds of this node;
        // clip if necessary
        return null;
    }

    /**
     * If the mouse is currently over the node, causes the node to
     * fire <tt>mouseOut()</tt> and a <tt>mouseMove()</tt> at the current mouse
     * location.
     * <p>
     * This method is primarily useful when consuming group mouse motion
     * events, since it allows a caller to reset the mouse state based on the
     * event consumption logic.
     */
    public void reenterMouse() {
        if (isMouseOver()) {
            mouseExited();

            Stage stage = getStage();
            Point location = stage.getMouseLocation();
            location = mapPointFromAncestor(stage, x, y);
            mouseMoved(location.x, location.y, false);
        }
    }

    protected void mouseEntered() {
        if (isEnabled()) {
            mouseLocation = new Point(-1, -1);
            nodeMouseListeners.mouseEntered(this);
        }
    }

    protected void mouseExited() {
        if (isEnabled()) {
            mouseLocation = null;
            nodeMouseListeners.mouseExited(this);
        }
    }

    protected boolean mouseMoved(int x, int y, boolean captured) {
        boolean consumed = false;

        if (isEnabled()) {
            mouseLocation = new Point(x, y);
            consumed = nodeMouseListeners.mouseMoved(this, x, y, captured);
        }

        return consumed;
    }

    protected boolean mousePressed(Mouse.Button button, int x, int y) {
        boolean consumed = false;

        if (isEnabled()) {
            consumed = nodeMouseButtonListeners.mousePressed(this, button, x, y);
        }

        return consumed;
    }

    protected boolean mouseReleased(Mouse.Button button, int x, int y) {
        boolean consumed = false;

        if (isEnabled()) {
            consumed = nodeMouseButtonListeners.mouseReleased(this, button, x, y);
        }

        return consumed;
    }

    protected boolean mouseClicked(Mouse.Button button, int x, int y, int count) {
        boolean consumed = false;

        if (isEnabled()) {
            consumed = nodeMouseButtonListeners.mouseClicked(this, button, x, y, count);
        }

        return consumed;
    }

    protected boolean mouseWheelScrolled(Mouse.ScrollType scrollType,
        int scrollAmount, int wheelRotation, int x, int y) {
        boolean consumed = false;

        if (isEnabled()) {
            consumed = nodeMouseWheelListeners.mouseWheelScrolled(this, scrollType, scrollAmount,
                wheelRotation, x, y);
        }

        return consumed;
    }

    protected boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (isEnabled()) {
            consumed = nodeKeyListeners.keyPressed(this, keyCode, keyLocation);

            if (!consumed && group != null) {
                consumed = group.keyPressed(keyCode, keyLocation);
            }
        }

        return consumed;
    }

    protected boolean keyReleased(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (isEnabled()) {
            consumed = nodeKeyListeners.keyReleased(this, keyCode, keyLocation);

            if (!consumed && group != null) {
                consumed = group.keyReleased(keyCode, keyLocation);
            }
        }

        return consumed;
    }

    protected boolean keyTyped(char character) {
        boolean consumed = false;

        if (isEnabled()) {
            consumed = nodeKeyListeners.keyTyped(this, character);

            if (!consumed && group != null) {
                consumed = group.keyTyped(character);
            }
        }

        return consumed;
    }

    @Override
    public String toString() {
        return getClass().getName();
    }

    public ListenerList<NodeListener> getNodeListeners() {
        return nodeListeners;
    }

    public ListenerList<NodeStateListener> getNodeStateListeners() {
        return nodeStateListeners;
    }

    public ListenerList<NodeMouseListener> getNodeMouseListeners() {
        return nodeMouseListeners;
    }

    public ListenerList<NodeMouseButtonListener> getNodeMouseButtonListeners() {
        return nodeMouseButtonListeners;
    }

    public ListenerList<NodeMouseWheelListener> getNodeMouseWheelListeners() {
        return nodeMouseWheelListeners;
    }

    public ListenerList<NodeKeyListener> getNodeKeyListeners() {
        return nodeKeyListeners;
    }

    public static ListenerList<NodeClassListener> getNodeClassListeners() {
        return nodeClassListeners;
    }

    /**
     * Returns the currently focused node.
     *
     * @return
     * The node that currently has the focus, or <tt>null</tt> if no
     * node is focused.
     */
    public static Node getFocusedNode() {
        return focusedNode;
    }

    /**
     * Sets the focused node.
     *
     * @param focusedComponent
     * The node to focus, or <tt>null</tt> to clear the focus.
     */
    private static void setFocusedNode(Node focusedNode) {
        Node previousFocusedNode = Node.focusedNode;

        if (previousFocusedNode != focusedNode) {
            Node.focusedNode = focusedNode;

            if (previousFocusedNode != null) {
                previousFocusedNode.setFocused(false, focusedNode);
            }

            if (focusedNode != null) {
                focusedNode.setFocused(true, previousFocusedNode);
            }

            nodeClassListeners.focusedNodeChanged(previousFocusedNode);
        }
    }

    /**
     * Clears the focus.
     */
    public static void clearFocus() {
        setFocusedNode(null);
    }
}
