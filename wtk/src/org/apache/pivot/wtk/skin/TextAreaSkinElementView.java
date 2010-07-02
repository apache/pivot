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
abstract class TextAreaSkinElementView extends TextAreaSkinNodeView
    implements Sequence<TextAreaSkinNodeView>, Iterable<TextAreaSkinNodeView>, ElementListener {
    private ArrayList<TextAreaSkinNodeView> nodeViews = new ArrayList<TextAreaSkinNodeView>();

    public TextAreaSkinElementView(Element element) {
        super(element);
    }

    @Override
    protected void attach() {
        super.attach();

        Element element = (Element)getNode();
        element.getElementListeners().add(this);

        // NOTE We don't attach child views here because this may not
        // be efficient for all subclasses (e.g. paragraph views need to
        // recreate child views when breaking across multiple lines)
    }

    @Override
    protected void detach() {
        Element element = (Element)getNode();
        element.getElementListeners().remove(this);

        // Detach child node views
        for (TextAreaSkinNodeView nodeView : this) {
            nodeView.detach();
        }

        super.detach();
    }

    @Override
    public int add(TextAreaSkinNodeView nodeView) {
        int index = getLength();
        insert(nodeView, index);

        return index;
    }

    @Override
    public void insert(TextAreaSkinNodeView nodeView, int index) {
        nodeView.setParent(this);
        nodeView.attach();

        nodeViews.insert(nodeView, index);
    }

    @Override
    public TextAreaSkinNodeView update(int index, TextAreaSkinNodeView nodeView) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int remove(TextAreaSkinNodeView nodeView) {
        int index = indexOf(nodeView);
        if (index != -1) {
            remove(index, 1);
        }

        return index;
    }

    @Override
    public Sequence<TextAreaSkinNodeView> remove(int index, int count) {
        Sequence<TextAreaSkinNodeView> removed = nodeViews.remove(index, count);

        for (int i = 0, n = removed.getLength(); i < n; i++) {
            TextAreaSkinNodeView nodeView = removed.get(i);
            nodeView.setParent(null);
            nodeView.detach();
        }

        return removed;
    }

    @Override
    public TextAreaSkinNodeView get(int index) {
        return nodeViews.get(index);
    }

    @Override
    public int indexOf(TextAreaSkinNodeView nodeView) {
        return nodeViews.indexOf(nodeView);
    }

    @Override
    public int getLength() {
        return nodeViews.getLength();
    }

    @Override
    public void paint(Graphics2D graphics) {
        // Determine the paint bounds
        Bounds paintBounds = new Bounds(0, 0, getWidth(), getHeight());
        Rectangle clipBounds = graphics.getClipBounds();
        if (clipBounds != null) {
            paintBounds = paintBounds.intersect(new Bounds(clipBounds));
        }

        for (TextAreaSkinNodeView nodeView : nodeViews) {
            Bounds nodeViewBounds = nodeView.getBounds();

            // Only paint node views that intersect the current clip rectangle
            if (nodeViewBounds.intersects(paintBounds)) {
                // Create a copy of the current graphics context and
                // translate to the node view's coordinate system
                Graphics2D nodeViewGraphics = (Graphics2D)graphics.create();
                nodeViewGraphics.translate(nodeViewBounds.x, nodeViewBounds.y);

                Color styledBackgroundColor = getStyledBackgroundColor();
                if (styledBackgroundColor != null) {
                    nodeViewGraphics.setColor(styledBackgroundColor);
                    nodeViewGraphics.fillRect(nodeViewBounds.x, nodeViewBounds.y, nodeViewBounds.width, nodeViewBounds.height);
                }

                // NOTE We don't clip here because views should generally
                // not overlap and clipping would impose an unnecessary
                // performance penalty

                // Paint the node view
                nodeView.paint(nodeViewGraphics);

                // Dispose of the node views's graphics
                nodeViewGraphics.dispose();
            }
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
            TextAreaSkinNodeView nodeView = nodeViews.get(i);
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
        invalidate();
    }

    @Override
    public void nodesRemoved(Element element, int index, Sequence<Node> nodes) {
        invalidate();
    }

    @Override
    public void fontChanged(Element element, Font previousFont) {
        invalidate();
    }

    @Override
    public void backgroundColorChanged(Element element, Color previousBackgroundColor) {
        invalidate();
    }

    @Override
    public void foregroundColorChanged(Element element, Color previousForegroundColor) {
        invalidate();
    }

    @Override
    public Iterator<TextAreaSkinNodeView> iterator() {
        return new ImmutableIterator<TextAreaSkinNodeView>(nodeViews.iterator());
    }
}