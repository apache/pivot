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

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Visual;
import org.apache.pivot.wtk.text.Element;
import org.apache.pivot.wtk.text.Node;
import org.apache.pivot.wtk.text.NodeListener;

/**
 * Abstract base class for node views.
 */
abstract class TextAreaSkinNodeView implements Visual, NodeListener {
    private Node node = null;
    private TextAreaSkinElementView parent = null;

    private int width = 0;
    private int height = 0;
    private int x = 0;
    private int y = 0;

    private int breakWidth = -1;

    private boolean valid = false;

    public TextAreaSkinNodeView(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public TextAreaSkinElementView getParent() {
        return parent;
    }

    protected void setParent(TextAreaSkinElementView parent) {
        this.parent = parent;
    }

    protected void attach() {
        node.getNodeListeners().add(this);
    }

    protected void detach() {
        node.getNodeListeners().remove(this);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getBaseline() {
        return -1;
    }

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

    public void repaint(int x, int y, int width, int height) {
        assert(width >= 0);
        assert(height >= 0);

        if (parent != null) {
            parent.repaint(x + this.x, y + this.y, width, height);
        }
    }

    public boolean isValid() {
        return valid;
    }

    public void invalidate() {
        valid = false;

        if (parent != null) {
            parent.invalidate();
        }
    }

    public void validate() {
        valid = true;
    }

    public int getBreakWidth() {
        return breakWidth;
    }

    public void setBreakWidth(int breakWidth) {
        int previousBreakWidth = this.breakWidth;

        if (previousBreakWidth != breakWidth) {
            this.breakWidth = breakWidth;

            // NOTE We can't call invalidate() here because it would ultimately
            // trigger a call to invalidateComponent(), which we don't want; this method
            // is called during preferred size calculations as well as layout, neither
            // of which should ever trigger an invalidate.
            valid = false;
        }
    }

    public int getOffset() {
        return node.getOffset();
    }

    public int getDocumentOffset() {
        return (parent == null) ? 0 : parent.getDocumentOffset() + getOffset();
    }

    public int getCharacterCount() {
        return node.getCharacterCount();
    }

    /**
     * Used by TextAreaSkinParagraphView when it breaks child nodes into multiple views.
     */
    public abstract TextAreaSkinNodeView getNext();

    public abstract int getInsertionPoint(int x, int y);
    public abstract int getNextInsertionPoint(int x, int from, FocusTraversalDirection direction);
    public abstract int getRowAt(int offset);
    public abstract int getRowCount();
    public abstract Bounds getCharacterBounds(int offset);

    @Override
    public void parentChanged(Node node, Element previousParent) {
        // No-op
    }

    @Override
    public void offsetChanged(Node node, int previousOffset) {
        // No-op
    }

    @Override
    public void rangeInserted(Node node, int offset, int span) {
        // No-op
    }

    @Override
    public void rangeRemoved(Node node, int offset, int characterCount) {
        // No-op
    }

    @Override
    public void nodesRemoved(Node node, Sequence<Node> removed, int offset) {
        // No-op
    }

    @Override
    public void nodeInserted(Node node, int offset) {
        // No-op
    }
}