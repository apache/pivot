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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.text.Element;
import org.apache.pivot.wtk.text.ElementListener;
import org.apache.pivot.wtk.text.Node;

/**
 * Abstract base class for element views.
 */
abstract class TextPaneSkinElementView extends TextPaneSkinNodeView
    implements Sequence<TextPaneSkinNodeView>, Iterable<TextPaneSkinNodeView>, ElementListener {
    private ArrayList<TextPaneSkinNodeView> nodeViews = new ArrayList<TextPaneSkinNodeView>();
    private int skinX = 0;
    private int skinY = 0;

    public TextPaneSkinElementView(Element element) {
        super(element);
    }

    @Override
    protected void attach() {
        super.attach();

        Element element = (Element)getNode();
        element.getElementListeners().add(this);

        // Attach child node views
        for (Node node : element) {
            add(getTextPaneSkin().createNodeView(node));
        }
    }

    @Override
    protected void detach() {
        Element element = (Element)getNode();
        element.getElementListeners().remove(this);

        // Detach child node views
        for (TextPaneSkinNodeView nodeView : this) {
            nodeView.detach();
        }

        super.detach();
    }

    @Override
    public void invalidateDownTree() {
        super.invalidateDownTree();
        for (TextPaneSkinNodeView child : this) {
            child.invalidateDownTree();
        }
    }

    @Override
    public int add(TextPaneSkinNodeView nodeView) {
        int index = getLength();
        insert(nodeView, index);

        return index;
    }

    @Override
    public void insert(TextPaneSkinNodeView nodeView, int index) {
        nodeView.setParent(this);
        nodeView.attach();

        nodeViews.insert(nodeView, index);
    }

    @Override
    public TextPaneSkinNodeView update(int index, TextPaneSkinNodeView nodeView) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int remove(TextPaneSkinNodeView nodeView) {
        int index = indexOf(nodeView);
        if (index != -1) {
            remove(index, 1);
        }

        return index;
    }

    @Override
    public Sequence<TextPaneSkinNodeView> remove(int index, int count) {
        Sequence<TextPaneSkinNodeView> removed = nodeViews.remove(index, count);

        for (int i = 0, n = removed.getLength(); i < n; i++) {
            TextPaneSkinNodeView nodeView = removed.get(i);
            nodeView.setParent(null);
            nodeView.detach();
        }

        return removed;
    }

    @Override
    public TextPaneSkinNodeView get(int index) {
        return nodeViews.get(index);
    }

    @Override
    public int indexOf(TextPaneSkinNodeView nodeView) {
        return nodeViews.indexOf(nodeView);
    }

    @Override
    public int getLength() {
        return nodeViews.getLength();
    }

    @Override
    public int getBaseline() {
        int baseline = -1;
        for (TextPaneSkinNodeView nodeView : nodeViews) {
            baseline = Math.max(baseline, nodeView.getBaseline());

        }
        return baseline;
    }

    @Override
    protected void setSkinLocation(int skinX, int skinY) {
        this.skinX = skinX;
        this.skinY = skinY;
    }

    @Override
    public void paint(Graphics2D graphics) {
        // Determine the paint bounds
        Bounds paintBounds = new Bounds(0, 0, getWidth(), getHeight());
        Rectangle clipBounds = graphics.getClipBounds();
        if (clipBounds != null) {
            paintBounds = paintBounds.intersect(clipBounds);
        }

        for (TextPaneSkinNodeView nodeView : nodeViews) {
            paintChild(graphics, paintBounds, nodeView);
        }
    }

    protected final void paintChild(Graphics2D graphics, Bounds paintBounds, TextPaneSkinNodeView nodeView) {
        Bounds nodeViewBounds = nodeView.getBounds();

        // Only paint node views that intersect the current clip rectangle
        if (nodeViewBounds.intersects(paintBounds)) {
            // Create a copy of the current graphics context and
            // translate to the node view's coordinate system
            Graphics2D nodeViewGraphics = (Graphics2D)graphics.create();

            Color styledBackgroundColor = getStyledBackgroundColor();
            if (styledBackgroundColor != null) {
                // don't paint over the selection background
                Area selection = getTextPaneSkin().getSelection();
                if (selection != null) {
                    Area fillArea = new Area(new Rectangle(nodeViewBounds.x, nodeViewBounds.y, nodeViewBounds.width, nodeViewBounds.height));
                    selection = selection.createTransformedArea(AffineTransform.getTranslateInstance(-skinX, -skinY));
                    fillArea.subtract(selection);
                    nodeViewGraphics.setColor(styledBackgroundColor);
                    nodeViewGraphics.fill(fillArea);
                } else {
                    nodeViewGraphics.setColor(styledBackgroundColor);
                    nodeViewGraphics.fillRect(nodeViewBounds.x, nodeViewBounds.y, nodeViewBounds.width, nodeViewBounds.height);
                }
            }
            nodeViewGraphics.translate(nodeViewBounds.x, nodeViewBounds.y);

            // NOTE We don't clip here because views should generally
            // not overlap and clipping would impose an unnecessary
            // performance penalty

            // Paint the node view
            nodeView.paint(nodeViewGraphics);

            // Dispose of the node views's graphics
            nodeViewGraphics.dispose();
        }
    }

    private Color getStyledBackgroundColor() {
        Color backgroundColor = null;
        Node node = getNode();
        // run up the tree until we find a Element's style to apply
        while (node != null) {
            if (node instanceof Element) {
                backgroundColor = ((Element) node).getBackgroundColor();
                if (backgroundColor != null) {
                    break;
                }
            }
            node = node.getParent();
        }
        return backgroundColor;
    }

    @Override
    public Bounds getCharacterBounds(int offset) {
        Bounds characterBounds = null;

        for (int i = 0, n = nodeViews.getLength(); i < n; i++) {
            TextPaneSkinNodeView nodeView = nodeViews.get(i);
            int nodeViewOffset = nodeView.getOffset();
            int characterCount = nodeView.getCharacterCount();

            if (offset >= nodeViewOffset
                && offset < nodeViewOffset + characterCount) {
                characterBounds = nodeView.getCharacterBounds(offset - nodeViewOffset);

                if (characterBounds != null) {
                    characterBounds = characterBounds.translate(nodeView.getX(), nodeView.getY());
                }

                break;
            }
        }

        if (characterBounds != null) {
            characterBounds = characterBounds.intersect(0, 0, getWidth(), getHeight());
        }

        return characterBounds;
    }

    @Override
    public void nodeInserted(Element element, int index) {
        insert(getTextPaneSkin().createNodeView(element.get(index)), index);
        invalidateUpTree();
    }

    @Override
    public void nodesRemoved(Element element, int index, Sequence<Node> nodes) {
        remove(index, nodes.getLength());
        invalidateUpTree();
    }

    @Override
    public void fontChanged(Element element, Font previousFont) {
        // because children may depend on parents for their style information, we need to invalidate the whole tree
        // TODO, we don't need to invalidate the whole tree, just the sub-tree from here down
        getTextPaneSkin().invalidateNodeViewTree();
    }

    @Override
    public void backgroundColorChanged(Element element, Color previousBackgroundColor) {
        repaint();
    }

    @Override
    public void foregroundColorChanged(Element element, Color previousForegroundColor) {
        // Because children may depend on parents for their style information, we need to invalidate the whole tree.
        // TODO we don't need to invalidate the whole tree, just the sub-tree from here down.
        getTextPaneSkin().invalidateNodeViewTree();
    }

    @Override
    public void underlineChanged(Element element) {
        // Because children may depend on parents for their style information, we need to invalidate the whole tree.
        // TODO we don't need to invalidate the whole tree, just the sub-tree from here down.
        getTextPaneSkin().invalidateNodeViewTree();
    }

    @Override
    public void strikethroughChanged(Element element) {
        // Because children may depend on parents for their style information, we need to invalidate the whole tree.
        // TODO we don't need to invalidate the whole tree, just the sub-tree from here down.
        getTextPaneSkin().invalidateNodeViewTree();
    }

    @Override
    public Iterator<TextPaneSkinNodeView> iterator() {
        return new ImmutableIterator<TextPaneSkinNodeView>(nodeViews.iterator());
    }
}