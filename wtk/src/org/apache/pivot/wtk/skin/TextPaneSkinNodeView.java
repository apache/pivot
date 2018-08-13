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

import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.TextPane;
import org.apache.pivot.wtk.text.BulletedList;
import org.apache.pivot.wtk.text.ComponentNode;
import org.apache.pivot.wtk.text.Document;
import org.apache.pivot.wtk.text.Element;
import org.apache.pivot.wtk.text.ImageNode;
import org.apache.pivot.wtk.text.List;
import org.apache.pivot.wtk.text.Node;
import org.apache.pivot.wtk.text.NodeListener;
import org.apache.pivot.wtk.text.NumberedList;
import org.apache.pivot.wtk.text.Paragraph;
import org.apache.pivot.wtk.text.TextNode;
import org.apache.pivot.wtk.text.TextSpan;

/**
 * Abstract base class for node views.
 */
abstract class TextPaneSkinNodeView implements NodeListener {
    protected final TextPaneSkin textPaneSkin;
    private Node node = null;
    private TextPaneSkinElementView parent = null;

    private int width = 0;
    private int height = 0;
    private int x = 0;
    private int y = 0;
    private int previousBreakWidth = -1;

    private boolean valid = false;

    public TextPaneSkinNodeView(final TextPaneSkin textPaneSkin, final Node node) {
        this.textPaneSkin = textPaneSkin;
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public TextPaneSkinElementView getParent() {
        return parent;
    }

    protected void setParent(final TextPaneSkinElementView parent) {
        this.parent = parent;
    }

    protected TextPaneSkin getTextPaneSkin() {
        return textPaneSkin;
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

    public void setSize(final int width, final int height) {
        assert (width >= 0);
        assert (height >= 0);

        // Redraw the region formerly occupied by this view
        repaint();

        this.width = width;
        this.height = height;

        // Redraw the region currently occupied by this view
        repaint();
    }

    public void setSize(final Dimensions size) {
        Utils.checkNull(size, "size");
        setSize(size.width, size.height);
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

    protected void setLocation(final int x, final int y) {
        // Redraw the region formerly occupied by this view
        repaint();

        this.x = x;
        this.y = y;

        // Redraw the region currently occupied by this view
        repaint();
    }

    /**
     * Set location of the NodeView relative to the skin component. This is
     * needed by the ComponentViewNode to correctly position child Component's.
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

    public void repaint(final int xArg, final int yArg, final int widthArg, final int heightArg) {
        assert (widthArg >= 0);
        assert (heightArg >= 0);

        if (parent != null) {
            parent.repaint(xArg + this.x, yArg + this.y, widthArg, heightArg);
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

    public final void layout(final int breakWidth) {
        // reduce the number of layout calculations we need to do by only
        // redoing them if necessary
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

    public abstract int getNextInsertionPoint(int xArgument, int from,
        TextPane.ScrollDirection direction);

    public abstract int getRowAt(int offset);

    public abstract int getRowCount();

    public abstract Bounds getCharacterBounds(int offset);

    @Override
    public void parentChanged(final Node nodeArgument, final Element previousParent) {
        // No-op
    }

    @Override
    public void offsetChanged(final Node nodeArgument, final int previousOffset) {
        // No-op
    }

    @Override
    public void rangeInserted(final Node nodeArgument, final int offset, final int span) {
        // No-op
    }

    @Override
    public void rangeRemoved(final Node nodeArgument, final int offset, final int characterCount,
        final CharSequence removedChars) {
        // No-op
    }

    @Override
    public void nodesRemoved(final Node nodeArgument, final Sequence<Node> removed, final int offset) {
        // No-op
    }

    @Override
    public void nodeInserted(final Node nodeArgument, final int offset) {
        // No-op
    }

    /**
     * In order to avoid the overhead of reflection to create the node view
     * objects given the node model objects, we will implement this interface
     * for each node type, which will just create the appropriate view object.
     */
    @FunctionalInterface
    private interface NodeCreator {
        TextPaneSkinNodeView create(TextPaneSkin textPaneSkin, Node node);
    }

    private static HashMap<Class<? extends Node>, NodeCreator> nodeViewCreatorMap = new HashMap<>();
    static {
        nodeViewCreatorMap.put(Document.class, (textPaneSkin, node) ->
            new TextPaneSkinDocumentView(textPaneSkin, (Document) node));
        nodeViewCreatorMap.put(Paragraph.class, (textPaneSkin, node) ->
            new TextPaneSkinParagraphView(textPaneSkin, (Paragraph) node));
        nodeViewCreatorMap.put(TextNode.class, (textPaneSkin, node) ->
            new TextPaneSkinTextNodeView(textPaneSkin, (TextNode) node));
        nodeViewCreatorMap.put(ImageNode.class, (textPaneSkin, node) ->
            new TextPaneSkinImageNodeView(textPaneSkin, (ImageNode) node));
        nodeViewCreatorMap.put(ComponentNode.class, (textPaneSkin, node) ->
            new TextPaneSkinComponentNodeView(textPaneSkin, (ComponentNode) node));
        nodeViewCreatorMap.put(TextSpan.class, (textPaneSkin, node) ->
            new TextPaneSkinSpanView(textPaneSkin, (TextSpan) node));
        nodeViewCreatorMap.put(NumberedList.class, (textPaneSkin, node) ->
            new TextPaneSkinNumberedListView(textPaneSkin, (NumberedList) node));
        nodeViewCreatorMap.put(BulletedList.class, (textPaneSkin, node) ->
            new TextPaneSkinBulletedListView(textPaneSkin, (BulletedList) node));
        nodeViewCreatorMap.put(List.Item.class, (textPaneSkin, node) ->
            new TextPaneSkinListItemView(textPaneSkin, (List.Item) node));
    }

    /**
     * Create a node view for the given model node attached to this <tt>TextPaneSkin</tt>.
     *
     * @param textPaneSkin The overall skin of this <tt>TextPane</tt>.
     * @param node The data node we are creating the view for.
     * @return The corresponding view node.
     */
    public static TextPaneSkinNodeView createNodeView(final TextPaneSkin textPaneSkin, final Node node) {
        TextPaneSkinNodeView nodeView = null;

        NodeCreator creator = nodeViewCreatorMap.get(node.getClass());
        if (creator != null) {
            return creator.create(textPaneSkin, node);
        } else {
            throw new IllegalArgumentException("Unsupported node type: " + node.getClass().getName());
        }
    }

}
