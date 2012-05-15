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
package org.apache.pivot.wtk.skin;

import java.awt.Graphics2D;

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.TextPane;
import org.apache.pivot.wtk.text.Element;
import org.apache.pivot.wtk.text.Node;
import org.apache.pivot.wtk.text.NodeListener;

/**
 * Abstract base class for node views.
 */
abstract class TextPaneSkinNodeView implements NodeListener {
    private Node node = null;
    private TextPaneSkinElementView parent = null;

    private int width = 0;
    private int height = 0;
    private int x = 0;
    private int y = 0;
    private int previousBreakWidth = -1;

    private boolean valid = false;

    public TextPaneSkinNodeView(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public TextPaneSkinElementView getParent() {
        return parent;
    }

    protected void setParent(TextPaneSkinElementView parent) {
        this.parent = parent;
    }

    protected TextPaneSkin getTextPaneSkin() {
        return getParent().getTextPaneSkin();
    }

    protected void attach() {
        node.getNodeListeners().add(this);
    }

    protected void detach() {
        node.getNodeListeners().remove(this);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public abstract int getBaseline();

    public abstract void paint(Graphics2D g);

    public Dimensions getSize() {
        return new Dimensions(width, height);
    }

    protected void setSize(int width, int height) {
        assert(width >= 0);
        assert(height >= 0);

        // Redraw the region formerly occupied by this view
        repaint();

        this.width = width;
        this.height = height;

        // Redraw the region currently occupied by this view
        repaint();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Point getLocation() {
        return new Point(x, y);
    }

    protected void setLocation(int x, int y) {
        // Redraw the region formerly occupied by this view
        repaint();

        this.x = x;
        this.y = y;

        // Redraw the region currently occupied by this view
        repaint();
    }

    /**
     * Set location of the NodeView relative to the skin component.
     * This is needed by the ComponentViewNode to correctly position child Component's.
     *
     * @param skinX the X coordinate in the skin's frame of reference
     * @param skinY the Y coordinate in the skin's frame of reference
     */
    protected abstract void setSkinLocation(int skinX, int skinY);

    public Bounds getBounds() {
        return new Bounds(x, y, width, height);
    }

    public void repaint() {
        repaint(0, 0, width, height);
    }

    public void repaint(int xArgument, int yArgument, int widthArgument, int heightArgument) {
        assert(widthArgument >= 0);
        assert(heightArgument >= 0);

        if (parent != null) {
            parent.repaint(xArgument + this.x, yArgument + this.y, widthArgument, heightArgument);
        }
    }

    public final boolean isValid() {
        return valid;
    }

    public void invalidateUpTree() {
        valid = false;

        if (parent != null) {
            parent.invalidateUpTree();
        }
    }

    public void invalidateDownTree() {
        valid = false;
    }

    public final void layout(int breakWidth) {
        // reduce the number of layout calculations we need to do by only redoing them if necessary
        if (!valid || previousBreakWidth != breakWidth) {
            childLayout(breakWidth);
            valid = true;
            previousBreakWidth = breakWidth;
        }
    }

    public abstract Dimensions getPreferredSize(int breakWidth);

    protected abstract void childLayout(int breakWidth);

    public int getOffset() {
        return node.getOffset();
    }

    public int getDocumentOffset() {
        return (parent == null) ? 0 : parent.getDocumentOffset() + getOffset();
    }

    public int getCharacterCount() {
        return node.getCharacterCount();
    }

    public abstract int getInsertionPoint(int xArgument, int yArgument);
    public abstract int getNextInsertionPoint(int xArgument, int from, TextPane.ScrollDirection direction);
    public abstract int getRowAt(int offset);
    public abstract int getRowCount();
    public abstract Bounds getCharacterBounds(int offset);

    @Override
    public void parentChanged(Node nodeArgument, Element previousParent) {
        // No-op
    }

    @Override
    public void offsetChanged(Node nodeArgument, int previousOffset) {
        // No-op
    }

    @Override
    public void rangeInserted(Node nodeArgument, int offset, int span) {
        // No-op
    }

    @Override
    public void rangeRemoved(Node nodeArgument, int offset, int characterCount) {
        // No-op
    }

    @Override
    public void nodesRemoved(Node nodeArgument, Sequence<Node> removed, int offset) {
        // No-op
    }

    @Override
    public void nodeInserted(Node nodeArgument, int offset) {
        // No-op
    }

}
