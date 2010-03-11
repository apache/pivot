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
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.text.CharacterIterator;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.BindType;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Cursor;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TextAreaListener;
import org.apache.pivot.wtk.TextAreaSelectionListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Visual;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.media.ImageListener;
import org.apache.pivot.wtk.text.Document;
import org.apache.pivot.wtk.text.Element;
import org.apache.pivot.wtk.text.ElementListener;
import org.apache.pivot.wtk.text.ImageNode;
import org.apache.pivot.wtk.text.ImageNodeListener;
import org.apache.pivot.wtk.text.Node;
import org.apache.pivot.wtk.text.NodeListener;
import org.apache.pivot.wtk.text.Paragraph;
import org.apache.pivot.wtk.text.TextNode;
import org.apache.pivot.wtk.text.TextNodeListener;

/**
 * Text area skin.
 */
public class TextAreaSkin extends ComponentSkin implements TextArea.Skin,
    TextAreaListener, TextAreaSelectionListener {
    /**
     * Abstract base class for node views.
     */
    public abstract class NodeView implements Visual, NodeListener {
        private Node node = null;
        private ElementView parent = null;

        private int width = 0;
        private int height = 0;
        private int x = 0;
        private int y = 0;

        private int breakWidth = -1;

        private boolean valid = false;

        public NodeView(Node node) {
            this.node = node;
        }

        public Node getNode() {
            return node;
        }

        public ElementView getParent() {
            return parent;
        }

        protected void setParent(ElementView parent) {
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

        public abstract NodeView getNext();
        public abstract int getInsertionPoint(int x, int y);
        public abstract int getNextInsertionPoint(int x, int from, FocusTraversalDirection direction);
        public abstract int getRowIndex(int offset);
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
        public void rangeRemoved(Node node, int offset, int span) {
            // No-op
        }
    }

    /**
     * Abstract base class for element views.
     */
    public abstract class ElementView extends NodeView
        implements Sequence<NodeView>, Iterable<NodeView>, ElementListener {
        private ArrayList<NodeView> nodeViews = new ArrayList<NodeView>();

        public ElementView(Element element) {
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
            for (NodeView nodeView : this) {
                nodeView.detach();
            }

            super.detach();
        }

        @Override
        public int add(NodeView nodeView) {
            int index = getLength();
            insert(nodeView, index);

            return index;
        }

        @Override
        public void insert(NodeView nodeView, int index) {
            nodeView.setParent(this);
            nodeView.attach();

            nodeViews.insert(nodeView, index);
        }

        @Override
        public NodeView update(int index, NodeView nodeView) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int remove(NodeView nodeView) {
            int index = indexOf(nodeView);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<NodeView> remove(int index, int count) {
            Sequence<NodeView> removed = nodeViews.remove(index, count);

            for (int i = 0, n = removed.getLength(); i < n; i++) {
                NodeView nodeView = removed.get(i);
                nodeView.setParent(null);
                nodeView.detach();
            }

            return removed;
        }

        @Override
        public NodeView get(int index) {
            return nodeViews.get(index);
        }

        @Override
        public int indexOf(NodeView nodeView) {
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

            for (NodeView nodeView : nodeViews) {
                Bounds nodeViewBounds = nodeView.getBounds();

                // Only paint node views that intersect the current clip rectangle
                if (nodeViewBounds.intersects(paintBounds)) {
                    // Create a copy of the current graphics context and
                    // translate to the node view's coordinate system
                    Graphics2D nodeViewGraphics = (Graphics2D)graphics.create();
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
        }

        @Override
        public Bounds getCharacterBounds(int offset) {
            Bounds characterBounds = null;

            for (int i = 0, n = nodeViews.getLength(); i < n; i++) {
                NodeView nodeView = nodeViews.get(i);
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
        public Iterator<NodeView> iterator() {
            return new ImmutableIterator<NodeView>(nodeViews.iterator());
        }
    }

    /**
     * Document view.
     */
    public class DocumentView extends ElementView {
        public DocumentView(Document document) {
            super(document);
        }

        @Override
        public void attach() {
            super.attach();

            // Attach child node views
            Document document = (Document)getNode();
            for (Node node : document) {
                add(createNodeView(node));
            }
        }

        @Override
        public void repaint(int x, int y, int width, int height) {
            super.repaint(x, y, width, height);

            repaintComponent(x, y, width, height);
        }

        @Override
        public void invalidate() {
            super.invalidate();
            invalidateComponent();
        }

        @Override
        public void validate() {
            // TODO At some point, we may want to optimize this method by deferring layout of
            // non-visible views. If so, we should not recycle views but rather recreate them
            // (as is done in ParagraphView). This way, we avoid thread contention over the
            // existing views (e.g. trying to paint one while modifying its size/location, etc.).
            // Any invalid node views are simply replaced (in the queued callback, when the
            // thread has finished processing the new ones). This allows the definition of
            // validate() to remain as-is. Of course, if we redefine NodeView to implement
            // ConstrainedVisual, this may no longer be an issue.
            // Note that, if anything happens to invalidate the existence of the new views before
            // they are added to the document view, we need to make sure they are disposed (i.e.
            // detached).

            if (!isValid()) {
                int breakWidth = getBreakWidth();

                int width = 0;
                int height = 0;

                int i = 0;
                int n = getLength();

                while (i < n) {
                    NodeView nodeView = get(i++);
                    nodeView.setBreakWidth(breakWidth);
                    nodeView.validate();

                    nodeView.setLocation(0, height);

                    width = Math.max(width, nodeView.getWidth());
                    height += nodeView.getHeight();
                }

                setSize(width, height);

                super.validate();
            }
        }

        @Override
        public int getInsertionPoint(int x, int y) {
            int offset = -1;

            for (int i = 0, n = getLength(); i < n; i++) {
                NodeView nodeView = get(i);
                Bounds nodeViewBounds = nodeView.getBounds();

                if (y >= nodeViewBounds.y
                    && y < nodeViewBounds.y + nodeViewBounds.height) {
                    offset = nodeView.getInsertionPoint(x - nodeView.getX(), y - nodeView.getY())
                        + nodeView.getOffset();
                    break;
                }
            }

            return offset;
        }

        @Override
        public int getNextInsertionPoint(int x, int from, FocusTraversalDirection direction) {
            int offset = -1;

            if (getLength() > 0) {
                if (from == -1) {
                    int i = (direction == FocusTraversalDirection.FORWARD) ? 0 : getLength() - 1;
                    NodeView nodeView = get(i);
                    offset = nodeView.getNextInsertionPoint(x - nodeView.getX(), -1, direction);

                    if (offset != -1) {
                        offset += nodeView.getOffset();
                    }
                } else {
                    // Find the node view that contains the offset
                    int n = getLength();
                    int i = 0;

                    while (i < n) {
                        NodeView nodeView = get(i);
                        int nodeViewOffset = nodeView.getOffset();
                        int characterCount = nodeView.getCharacterCount();

                        if (from >= nodeViewOffset
                            && from < nodeViewOffset + characterCount) {
                            break;
                        }

                        i++;
                    }

                    if (i < n) {
                        NodeView nodeView = get(i);
                        offset = nodeView.getNextInsertionPoint(x - nodeView.getX(),
                            from - nodeView.getOffset(), direction);

                        if (offset == -1) {
                            // Move to the next or previous node view
                            if (direction == FocusTraversalDirection.FORWARD) {
                                nodeView = (i < n - 1) ? get(i + 1) : null;
                            } else {
                                nodeView = (i > 0) ? get(i - 1) : null;
                            }

                            if (nodeView != null) {
                                offset = nodeView.getNextInsertionPoint(x - nodeView.getX(), -1, direction);
                            }
                        }

                        if (offset != -1) {
                            offset += nodeView.getOffset();
                        }
                    }
                }
            }

            return offset;
        }

        @Override
        public int getRowIndex(int offset) {
            int rowIndex = 0;

            for (NodeView nodeView : this) {
                int nodeViewOffset = nodeView.getOffset();
                int characterCount = nodeView.getCharacterCount();

                if (offset >= nodeViewOffset
                    && offset < nodeViewOffset + characterCount) {
                    rowIndex += nodeView.getRowIndex(offset - nodeView.getOffset());
                    break;
                }

                rowIndex += nodeView.getRowCount();
            }

            return rowIndex;
        }

        @Override
        public int getRowCount() {
            int rowCount = 0;

            for (NodeView nodeView : this) {
                rowCount += nodeView.getRowCount();
            }

            return rowCount;
        }

        @Override
        public NodeView getNext() {
            return null;
        }

        @Override
        public void nodeInserted(Element element, int index) {
            super.nodeInserted(element, index);

            Document document = (Document)getNode();
            insert(createNodeView(document.get(index)), index);
        }

        @Override
        public void nodesRemoved(Element element, int index, Sequence<Node> nodes) {
            remove(index, nodes.getLength());

            super.nodesRemoved(element, index, nodes);
        }
    }

    public class ParagraphView extends ElementView {
        private class Row {
            public int x = 0;
            public int y = 0;
            public int width = 0;
            public int height = 0;
            public ArrayList<NodeView> nodeViews = new ArrayList<NodeView>();
        }

        private ArrayList<Row> rows = null;
        private Bounds terminatorBounds = new Bounds(0, 0, 0, 0);

        public ParagraphView(Paragraph paragraph) {
            super(paragraph);
        }

        @Override
        public void invalidate() {
            super.invalidate();
            terminatorBounds = null;
        }

        @Override
        public void validate() {
            if (!isValid()) {
                // Break the views into multiple rows
                int breakWidth = getBreakWidth();

                Paragraph paragraph = (Paragraph)getNode();
                rows = new ArrayList<Row>();

                Row row = new Row();
                for (Node node : paragraph) {
                    NodeView nodeView = createNodeView(node);

                    nodeView.setBreakWidth(Math.max(breakWidth - (row.width
                        + PARAGRAPH_TERMINATOR_WIDTH), 0));
                    nodeView.validate();

                    int nodeViewWidth = nodeView.getWidth();

                    if (row.width + nodeViewWidth > breakWidth
                        && row.width > 0) {
                        // The view is too big to fit in the remaining space,
                        // and it is not the only view in this row
                        rows.add(row);
                        row = new Row();
                        row.width = 0;
                    }

                    // Add the view to the row
                    row.nodeViews.add(nodeView);
                    row.width += nodeViewWidth;

                    // If the view was split into multiple views, add them to
                    // their own rows
                    nodeView = nodeView.getNext();
                    while (nodeView != null) {
                        rows.add(row);
                        row = new Row();

                        nodeView.setBreakWidth(breakWidth);
                        nodeView.validate();

                        row.nodeViews.add(nodeView);
                        row.width = nodeView.getWidth();

                        nodeView = nodeView.getNext();
                    }
                }

                // Add the last row
                if (row.nodeViews.getLength() > 0) {
                    rows.add(row);
                }

                // Clear all existing views
                remove(0, getLength());

                // Add the row views to this view, lay out, and calculate height
                int x = 0;
                int width = 0;
                int height = 0;
                for (int i = 0, n = rows.getLength(); i < n; i++) {
                    row = rows.get(i);
                    row.y = height;

                    width = Math.max(width, row.width);

                    // Determine the row height
                    for (NodeView nodeView : row.nodeViews) {
                        row.height = Math.max(row.height, nodeView.getHeight());
                    }

                    // TODO Align horizontally when Elements support a horizontal
                    // alignment property
                    x = 0;
                    for (NodeView nodeView : row.nodeViews) {
                        // TODO Align to baseline
                        int y = row.height - nodeView.getHeight();

                        nodeView.setLocation(x, y + height);
                        x += nodeView.getWidth();

                        add(nodeView);
                    }

                    height += row.height;
                }

                // Recalculate terminator bounds
                LineMetrics lm = font.getLineMetrics("", 0, 0, FONT_RENDER_CONTEXT);
                int terminatorHeight = (int)Math.ceil(lm.getHeight());

                int terminatorY;
                if (getCharacterCount() == 1) {
                    // The terminator is the only character in this paragraph
                    terminatorY = 0;
                } else {
                    terminatorY = height - terminatorHeight;
                }

                terminatorBounds = new Bounds(x, terminatorY,
                    PARAGRAPH_TERMINATOR_WIDTH, terminatorHeight);

                // Ensure that the paragraph is visible even when empty
                width += terminatorBounds.width;
                height = Math.max(height, terminatorBounds.height);

                setSize(width, height);
            }

            super.validate();
        }

        @Override
        public NodeView getNext() {
            return null;
        }

        @Override
        public int getInsertionPoint(int x, int y) {
            int offset = -1;

            int n = rows.getLength();
            if (n > 0) {
                for (int i = 0; i < n; i++) {
                    Row row = rows.get(i);

                    if (y >= row.y
                        && y < row.y + row.height) {
                        if (x < row.x) {
                            NodeView firstNodeView = row.nodeViews.get(0);
                            offset = firstNodeView.getOffset();
                        } else if (x > row.x + row.width - 1) {
                            NodeView lastNodeView = row.nodeViews.get(row.nodeViews.getLength() - 1);
                            offset = lastNodeView.getOffset() + lastNodeView.getCharacterCount();

                            if (offset < getCharacterCount() - 1) {
                                offset--;
                            }
                        } else {
                            for (NodeView nodeView : row.nodeViews) {
                                Bounds nodeViewBounds = nodeView.getBounds();

                                if (nodeViewBounds.contains(x, y)) {
                                    offset = nodeView.getInsertionPoint(x - nodeView.getX(), y - nodeView.getY())
                                        + nodeView.getOffset();
                                    break;
                                }
                            }
                        }
                    }

                    if (offset != -1) {
                        break;
                    }
                }
            } else {
                offset = getCharacterCount() - 1;
            }

            return offset;
        }

        @Override
        public int getNextInsertionPoint(int x, int from, FocusTraversalDirection direction) {
            int offset = -1;

            int n = rows.getLength();
            if (n == 0
                && from == -1) {
                // There are no rows; select the terminator character
                offset = 0;
            } else {
                int i;
                if (from == -1) {
                    i = (direction == FocusTraversalDirection.FORWARD) ? -1 : rows.getLength();
                } else {
                    // Find the row that contains offset
                    if (from == getCharacterCount() - 1) {
                        i = rows.getLength() - 1;
                    } else {
                        i = 0;
                        while (i < n) {
                            Row row = rows.get(i);
                            NodeView firstNodeView = row.nodeViews.get(0);
                            NodeView lastNodeView = row.nodeViews.get(row.nodeViews.getLength() - 1);
                            if (from >= firstNodeView.getOffset()
                                && from < lastNodeView.getOffset() + lastNodeView.getCharacterCount()) {
                                break;
                            }

                            i++;
                        }
                    }
                }

                // Move to the next or previous row
                if (direction == FocusTraversalDirection.FORWARD) {
                    i++;
                } else {
                    i--;
                }

                if (i >= 0
                    && i < n) {
                    // Find the node view that contains x and get the insertion point from it
                    Row row = rows.get(i);

                    for (NodeView nodeView : row.nodeViews) {
                        Bounds bounds = nodeView.getBounds();
                        if (x >= bounds.x
                            && x < bounds.x + bounds.width) {
                            offset = nodeView.getNextInsertionPoint(x - nodeView.getX(), -1, direction)
                                + nodeView.getOffset();
                            break;
                        }
                    }

                    if (offset == -1) {
                        // No node view contained the x position; move to the end of the row
                        NodeView lastNodeView = row.nodeViews.get(row.nodeViews.getLength() - 1);
                        offset = lastNodeView.getOffset() + lastNodeView.getCharacterCount();

                        if (offset < getCharacterCount() - 1) {
                            offset--;
                        }
                    }
                }
            }

            return offset;
        }

        @Override
        public int getRowIndex(int offset) {
            int rowIndex = -1;

            if (offset == getCharacterCount() - 1) {
                rowIndex = (rows.getLength() == 0) ? 0 : rows.getLength() - 1;
            } else {
                for (int i = 0, n = rows.getLength(); i < n; i++) {
                    Row row = rows.get(i);
                    NodeView firstNodeView = row.nodeViews.get(0);
                    NodeView lastNodeView = row.nodeViews.get(row.nodeViews.getLength() - 1);

                    if (offset >= firstNodeView.getOffset()
                        && offset < lastNodeView.getOffset() + lastNodeView.getCharacterCount()) {
                        rowIndex = i;
                        break;
                    }
                }
            }

            return rowIndex;
        }

        @Override
        public int getRowCount() {
            return Math.max(rows.getLength(), 1);
        }

        @Override
        public Bounds getCharacterBounds(int offset) {
            Bounds bounds;

            if (offset == getCharacterCount() - 1) {
                bounds = terminatorBounds;
            } else {
                bounds = super.getCharacterBounds(offset);
            }

            return bounds;
        }
    }

    /**
     * Text node view.
     */
    public class TextNodeView extends NodeView implements TextNodeListener {
        private int start;

        private int length = 0;
        private GlyphVector glyphVector = null;
        private TextNodeView next = null;

        public TextNodeView(TextNode textNode) {
            this(textNode, 0);
        }

        public TextNodeView(TextNode textNode, int start) {
            super(textNode);
            this.start = start;
        }

        @Override
        protected void attach() {
            super.attach();

            TextNode textNode = (TextNode)getNode();
            textNode.getTextNodeListeners().add(this);
        }

        @Override
        protected void detach() {
            super.detach();

            TextNode textNode = (TextNode)getNode();
            textNode.getTextNodeListeners().remove(this);
        }

        @Override
        public void invalidate() {
            length = 0;
            next = null;
            glyphVector = null;

            super.invalidate();
        }

        @Override
        public void validate() {
            if (!isValid()) {
                TextNode textNode = (TextNode)getNode();

                int breakWidth = getBreakWidth();
                CharacterIterator ci = textNode.getCharacterIterator(start);

                float lineWidth = 0;
                int lastWhitespaceIndex = -1;

                char c = ci.first();
                while (c != CharacterIterator.DONE
                    && lineWidth < breakWidth) {
                    if (Character.isWhitespace(c)) {
                        lastWhitespaceIndex = ci.getIndex();
                    }

                    int i = ci.getIndex();
                    Rectangle2D characterBounds = font.getStringBounds(ci, i, i + 1,
                        FONT_RENDER_CONTEXT);
                    lineWidth += characterBounds.getWidth();

                    c = ci.current();
                }

                int end;
                if (lineWidth <= breakWidth) {
                    end = ci.getEndIndex();
                } else {
                    if (lastWhitespaceIndex == -1) {
                        end = ci.getIndex() - 1;
                        if (end <= start) {
                            end = start + 1;
                        }
                    } else {
                        end = lastWhitespaceIndex + 1;
                    }
                }

                glyphVector = font.createGlyphVector(FONT_RENDER_CONTEXT,
                    textNode.getCharacterIterator(start, end));

                if (end < ci.getEndIndex()) {
                    length = end - start;
                    next = new TextNodeView(textNode, end);
                } else {
                    length = ci.getEndIndex() - start;
                }

                Rectangle2D textBounds = glyphVector.getLogicalBounds();
                setSize((int)Math.ceil(textBounds.getWidth()),
                    (int)Math.ceil(textBounds.getHeight()));
            }

            super.validate();
        }

        @Override
        public void paint(Graphics2D graphics) {
            if (glyphVector != null) {
                TextArea textArea = (TextArea)getComponent();

                if (FONT_RENDER_CONTEXT.isAntiAliased()) {
                    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        Platform.getTextAntialiasingHint());
                }

                if (FONT_RENDER_CONTEXT.usesFractionalMetrics()) {
                    graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                        RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                }

                LineMetrics lm = font.getLineMetrics("", FONT_RENDER_CONTEXT);
                float ascent = lm.getAscent();

                graphics.setFont(font);

                int selectionStart = textArea.getSelectionStart();
                int selectionLength = textArea.getSelectionLength();
                Span selectionRange = new Span(selectionStart, selectionStart + selectionLength - 1);

                int documentOffset = getDocumentOffset();
                Span characterRange = new Span(documentOffset, documentOffset + getCharacterCount() - 1);

                if (selectionLength > 0
                    && characterRange.intersects(selectionRange)) {
                    // Determine the selection bounds
                    int width = getWidth();
                    int height = getHeight();

                    int x0;
                    if (selectionRange.start > characterRange.start) {
                        Bounds leadingSelectionBounds = getCharacterBounds(selectionRange.start - documentOffset);
                        x0 = leadingSelectionBounds.x;
                    } else {
                        x0 = 0;
                    }

                    int x1;
                    if (selectionRange.end < characterRange.end) {
                        Bounds trailingSelectionBounds = getCharacterBounds(selectionRange.end - documentOffset);
                        x1 = trailingSelectionBounds.x + trailingSelectionBounds.width;
                    } else {
                        x1 = width;
                    }

                    Rectangle selection = new Rectangle(x0, 0, x1 - x0, height);

                    // Paint the unselected text
                    Area unselectedArea = new Area();
                    unselectedArea.add(new Area(new Rectangle(0, 0, width, height)));
                    unselectedArea.subtract(new Area(selection));

                    Graphics2D textGraphics = (Graphics2D)graphics.create();
                    textGraphics.setColor(color);
                    textGraphics.clip(unselectedArea);
                    textGraphics.drawGlyphVector(glyphVector, 0, ascent);
                    textGraphics.dispose();

                    // Paint the selection
                    Color selectionColor;
                    if (textArea.isFocused()) {
                        selectionColor = TextAreaSkin.this.selectionColor;
                    } else {
                        selectionColor = inactiveSelectionColor;
                    }

                    Graphics2D selectedTextGraphics = (Graphics2D)graphics.create();
                    selectedTextGraphics.setColor(textArea.isFocused() &&
                        textArea.isEditable() ? selectionColor : inactiveSelectionColor);
                    selectedTextGraphics.clip(selection.getBounds());
                    selectedTextGraphics.drawGlyphVector(glyphVector, 0, ascent);
                    selectedTextGraphics.dispose();
                } else {
                    // Draw the text
                    graphics.setColor(color);
                    graphics.drawGlyphVector(glyphVector, 0, ascent);
                }
            }
        }

        @Override
        public int getOffset() {
            return super.getOffset() + start;
        }

        @Override
        public int getCharacterCount() {
            return length;
        }

        @Override
        public NodeView getNext() {
            return next;
        }

        @Override
        public int getInsertionPoint(int x, int y) {
            LineMetrics lm = font.getLineMetrics("", FONT_RENDER_CONTEXT);
            float ascent = lm.getAscent();

            int n = glyphVector.getNumGlyphs();
            int i = 0;

            while (i < n) {
                Shape glyphBounds = glyphVector.getGlyphLogicalBounds(i);

                if (glyphBounds.contains(x, y - ascent)) {
                    Rectangle2D glyphBounds2D = glyphBounds.getBounds2D();

                    if (x - glyphBounds2D.getX() > glyphBounds2D.getWidth() / 2
                        && i < n - 1) {
                        // The user clicked on the right half of the character; select
                        // the next character
                        i++;
                    }

                    break;
                }

                i++;
            }

            return i;
        }

        @Override
        public int getNextInsertionPoint(int x, int from, FocusTraversalDirection direction) {
            int offset = -1;

            if (from == -1) {
                int n = glyphVector.getNumGlyphs();
                int i = 0;

                while (i < n) {
                    Shape glyphBounds = glyphVector.getGlyphLogicalBounds(i);
                    Rectangle2D glyphBounds2D = glyphBounds.getBounds2D();

                    float glyphX = (float)glyphBounds2D.getX();
                    float glyphWidth = (float)glyphBounds2D.getWidth();

                    if (x >= glyphX && x < glyphX + glyphWidth) {
                        if (x - glyphX > glyphWidth / 2
                            && i < n - 1) {
                            // The x position falls within the right half of the character;
                            // select the next character
                            i++;
                        }

                        offset = i;
                        break;
                    }

                    i++;
                }
            }

            return offset;
        }

        @Override
        public int getRowIndex(int offset) {
            return -1;
        }

        @Override
        public int getRowCount() {
            return 0;
        }

        @Override
        public Bounds getCharacterBounds(int offset) {
            Shape glyphBounds = glyphVector.getGlyphLogicalBounds(offset);
            Rectangle2D glyphBounds2D = glyphBounds.getBounds2D();

            return new Bounds((int)Math.floor(glyphBounds2D.getX()), 0,
                (int)Math.ceil(glyphBounds2D.getWidth()), getHeight());
        }

        @Override
        public void charactersInserted(TextNode textNode, int index, int count) {
            invalidate();
        }

        @Override
        public void charactersRemoved(TextNode textNode, int index, String characters) {
            invalidate();
        }

        @Override
        public String toString() {
            TextNode textNode = (TextNode)getNode();
            String text = textNode.getText();
            return "[" + text.substring(start, start + length) + "]";
        }
    }

    public class ImageNodeView extends NodeView implements ImageNodeListener, ImageListener {
        public ImageNodeView(ImageNode imageNode) {
            super(imageNode);
        }

        @Override
        protected void attach() {
            super.attach();

            ImageNode imageNode = (ImageNode)getNode();
            imageNode.getImageNodeListeners().add(this);

            Image image = imageNode.getImage();
            if (image != null) {
                image.getImageListeners().add(this);
            }
        }

        @Override
        protected void detach() {
            super.detach();

            ImageNode imageNode = (ImageNode)getNode();
            imageNode.getImageNodeListeners().remove(this);
        }

        @Override
        public void validate() {
            if (!isValid()) {
                ImageNode imageNode = (ImageNode)getNode();
                Image image = imageNode.getImage();

                if (image == null) {
                    setSize(0, 0);
                } else {
                    setSize(image.getWidth(), image.getHeight());
                }

                super.validate();
            }
        }

        @Override
        public void paint(Graphics2D graphics) {
            ImageNode imageNode = (ImageNode)getNode();
            Image image = imageNode.getImage();

            if (image != null) {
                image.paint(graphics);
            }
        }

        @Override
        public NodeView getNext() {
            return null;
        }

        @Override
        public int getInsertionPoint(int x, int y) {
            return 0;
        }

        @Override
        public int getNextInsertionPoint(int x, int from, FocusTraversalDirection direction) {
            return (from == -1) ? 0 : -1;
        }

        @Override
        public int getRowIndex(int offset) {
            return -1;
        }

        @Override
        public int getRowCount() {
            return 0;
        }

        @Override
        public Bounds getCharacterBounds(int offset) {
            return new Bounds(0, 0, getWidth(), getHeight());
        }

        @Override
        public void imageChanged(ImageNode imageNode, Image previousImage) {
            invalidate();

            Image image = imageNode.getImage();
            if (image != null) {
                image.getImageListeners().add(this);
            }

            if (previousImage != null) {
                previousImage.getImageListeners().remove(this);
            }
        }

        @Override
        public void sizeChanged(Image image, int previousWidth, int previousHeight) {
            invalidate();
        }

        @Override
        public void baselineChanged(Image image, int previousBaseline) {
            // TODO Invalidate once baseline alignment of node view is supported
        }

        @Override
        public void regionUpdated(Image image, int x, int y, int width, int height) {
            // TODO Repaint the corresponding area of the component (add a repaint()
            // method to NodeView to facilitate this as well as paint-only updates
            // such as color changes)
        }
    }

    private class BlinkCaretCallback implements Runnable {
        @Override
        public void run() {
            caretOn = !caretOn;

            if (selection == null) {
                TextArea textArea = (TextArea)getComponent();
                textArea.repaint(caret.x, caret.y, caret.width, caret.height, true);
            }
        }
    }

    private class ScrollSelectionCallback implements Runnable {
        @Override
        public void run() {
            TextArea textArea = (TextArea)getComponent();
            int selectionStart = textArea.getSelectionStart();
            int selectionLength = textArea.getSelectionLength();
            int selectionEnd = selectionStart + selectionLength - 1;

            switch (scrollDirection) {
                case FORWARD: {
                    // Get next offset
                    int offset = getNextInsertionPoint(mouseX, selectionEnd, scrollDirection);

                    if (offset != -1) {
                        // If the next character is a paragraph terminator and is not the
                        // final terminator character, increment the selection
                        Document document = textArea.getDocument();
                        if (document.getCharacterAt(offset) == '\n'
                            && offset < documentView.getCharacterCount() - 1) {
                            offset++;
                        }

                        textArea.setSelection(selectionStart, offset - selectionStart);
                        scrollCharacterToVisible(offset - 1);
                    }

                    break;
                }

                case BACKWARD: {
                    // Get previous offset
                    int offset = getNextInsertionPoint(mouseX, selectionStart, scrollDirection);

                    if (offset != -1) {
                        textArea.setSelection(offset, selectionEnd - offset + 1);
                        scrollCharacterToVisible(offset + 1);
                    }

                    break;
                }

                default: {
                    throw new RuntimeException();
                }
            }
        }
    }

    private DocumentView documentView = null;

    private int caretX = 0;
    private Rectangle caret = new Rectangle();
    private Area selection = null;

    private boolean caretOn = false;

    private int anchor = -1;
    private FocusTraversalDirection scrollDirection = null;
    private int mouseX = -1;

    private BlinkCaretCallback blinkCaretCallback = new BlinkCaretCallback();
    private ApplicationContext.ScheduledCallback scheduledBlinkCaretCallback = null;

    private ScrollSelectionCallback scrollSelectionCallback = new ScrollSelectionCallback();
    private ApplicationContext.ScheduledCallback scheduledScrollSelectionCallback = null;

    private Font font;
    private Color color;
    private Color inactiveColor;
    private Color backgroundColor;
    private Color selectionColor;
    private Color selectionBackgroundColor;
    private Color inactiveSelectionColor;
    private Color inactiveSelectionBackgroundColor;

    private Insets margin = new Insets(4);

    private boolean wrapText = true;

    private static final int PARAGRAPH_TERMINATOR_WIDTH = 4;
    private static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(null, true, true);
    private static final int SCROLL_RATE = 30;

    public TextAreaSkin() {
        Theme theme = Theme.getTheme();
        font = theme.getFont();
        color = Color.BLACK;
        inactiveColor = Color.GRAY;
        backgroundColor = null;
        selectionColor = Color.LIGHT_GRAY;
        selectionBackgroundColor = Color.BLACK;
        inactiveSelectionColor = Color.LIGHT_GRAY;
        inactiveSelectionBackgroundColor = Color.BLACK;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        TextArea textArea = (TextArea)component;
        textArea.getTextAreaListeners().add(this);
        textArea.getTextAreaSelectionListeners().add(this);

        textArea.setCursor(Cursor.TEXT);

        Document document = textArea.getDocument();
        if (document != null) {
            documentView = (DocumentView)createNodeView(document);
            documentView.attach();
            updateSelection();
        }
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth;

        if (documentView == null) {
           preferredWidth = 0;
        } else {
            documentView.setBreakWidth(Integer.MAX_VALUE);
            documentView.validate();

            preferredWidth = documentView.getWidth() + margin.left + margin.right;
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight;

        if (documentView == null
            || width == -1) {
            preferredHeight = 0;
        } else {
            int breakWidth;
            if (wrapText) {
                breakWidth = Math.max(width - (margin.left + margin.right), 0);
            } else {
                breakWidth = Integer.MAX_VALUE;
            }

            documentView.setBreakWidth(breakWidth);
            documentView.validate();

            preferredHeight = documentView.getHeight() + margin.top + margin.bottom;
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredHeight;
        int preferredWidth;

        if (documentView == null) {
           preferredWidth = 0;
           preferredHeight = 0;
        } else {
            documentView.setBreakWidth(Integer.MAX_VALUE);
            documentView.validate();

            preferredWidth = documentView.getWidth() + margin.left + margin.right;
            preferredHeight = documentView.getHeight() + margin.top + margin.bottom;
        }

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        LineMetrics lm = font.getLineMetrics("", FONT_RENDER_CONTEXT);
        float ascent = lm.getAscent();
        return margin.top + Math.round(ascent);
    }

    @Override
    public void layout() {
        if (documentView != null) {
            TextArea textArea = (TextArea)getComponent();
            int width = getWidth();

            documentView.setBreakWidth(Math.max(width - (margin.left + margin.right), 0));
            documentView.validate();

            updateSelection();
            caretX = caret.x;

            if (textArea.isFocused()) {
                scrollCharacterToVisible(textArea.getSelectionStart());
            }

            showCaret(textArea.isFocused()
                && textArea.getSelectionLength() == 0);
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        TextArea textArea = (TextArea)getComponent();
        int width = getWidth();
        int height = getHeight();

        if (backgroundColor != null) {
            graphics.setColor(backgroundColor);
            graphics.fillRect(0, 0, width, height);
        }

        if (documentView != null) {
            // Draw the selection highlight
            if (selection != null) {
                graphics.setColor(textArea.isFocused()
                    && textArea.isEditable() ?
                    selectionBackgroundColor : inactiveSelectionBackgroundColor);
                graphics.fill(selection);
            }

            // Draw the document content
            graphics.translate(margin.left, margin.top);
            documentView.paint(graphics);
            graphics.translate(-margin.left, -margin.top);

            // Draw the caret
            if (selection == null
                && caretOn
                && textArea.isFocused()) {
                graphics.setColor(textArea.isEditable() ? color : inactiveColor);
                graphics.fill(caret);
            }
        }
    }

    @Override
    public boolean isOpaque() {
        return (backgroundColor != null
            && backgroundColor.getTransparency() == Transparency.OPAQUE);
    }

    @Override
    public int getInsertionPoint(int x, int y) {
        int offset;

        if (documentView == null) {
            offset = -1;
        } else {
            x = Math.min(documentView.getWidth() - 1, Math.max(x - margin.left, 0));

            if (y < margin.top) {
                offset = documentView.getNextInsertionPoint(x, -1, FocusTraversalDirection.FORWARD);
            } else if (y > documentView.getHeight() + margin.top) {
                offset = documentView.getNextInsertionPoint(x, -1, FocusTraversalDirection.BACKWARD);
            } else {
                offset = documentView.getInsertionPoint(x, y - margin.top);
            }
        }

        return offset;
    }

    @Override
    public int getNextInsertionPoint(int x, int from, FocusTraversalDirection direction) {
        int offset;

        if (documentView == null) {
            offset = -1;
        } else {
            offset = documentView.getNextInsertionPoint(x - margin.left, from, direction);
        }

        return offset;
    }

    @Override
    public int getRowIndex(int offset) {
        int rowIndex;

        if (documentView == null) {
            rowIndex = -1;
        } else {
            rowIndex = documentView.getRowIndex(offset);
        }

        return rowIndex;
    }

    @Override
    public int getRowCount() {
        int rowCount;

        if (documentView == null) {
            rowCount = 0;
        } else {
            rowCount = documentView.getRowCount();
        }

        return rowCount;
    }

    @Override
    public Bounds getCharacterBounds(int offset) {
        Bounds characterBounds;

        if (documentView == null) {
            characterBounds = null;
        } else {
            characterBounds = documentView.getCharacterBounds(offset);

            if (characterBounds != null) {
                characterBounds = characterBounds.translate(margin.left, margin.top);
            }
        }

        return characterBounds;
    }

    private void scrollCharacterToVisible(int offset) {
        TextArea textArea = (TextArea)getComponent();
        Bounds characterBounds = getCharacterBounds(offset);

        if (characterBounds != null) {
            textArea.scrollAreaToVisible(characterBounds.x, characterBounds.y,
                characterBounds.width, characterBounds.height);
        }
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

    public final void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(GraphicsUtilities.decodeColor(color));
    }

    public Color getInactiveColor() {
        return inactiveColor;
    }

    public void setInactiveColor(Color inactiveColor) {
        if (inactiveColor == null) {
            throw new IllegalArgumentException("inactiveColor is null.");
        }

        this.inactiveColor = inactiveColor;
        repaintComponent();
    }

    public final void setInactiveColor(String inactiveColor) {
        if (inactiveColor == null) {
            throw new IllegalArgumentException("inactiveColor is null.");
        }

        setColor(GraphicsUtilities.decodeColor(inactiveColor));
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    public final void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null");
        }

        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor));
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

    public final void setFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(decodeFont(font));
    }

    public final void setFont(Dictionary<String, ?> font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Theme.deriveFont(font));
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

    public final void setSelectionColor(String selectionColor) {
        if (selectionColor == null) {
            throw new IllegalArgumentException("selectionColor is null.");
        }

        setSelectionColor(GraphicsUtilities.decodeColor(selectionColor));
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

    public final void setSelectionBackgroundColor(String selectionBackgroundColor) {
        if (selectionBackgroundColor == null) {
            throw new IllegalArgumentException("selectionBackgroundColor is null.");
        }

        setSelectionBackgroundColor(GraphicsUtilities.decodeColor(selectionBackgroundColor));
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

    public final void setInactiveSelectionColor(String inactiveSelectionColor) {
        if (inactiveSelectionColor == null) {
            throw new IllegalArgumentException("inactiveSelectionColor is null.");
        }

        setInactiveSelectionColor(GraphicsUtilities.decodeColor(inactiveSelectionColor));
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

    public final void setInactiveSelectionBackgroundColor(String inactiveSelectionBackgroundColor) {
        if (inactiveSelectionBackgroundColor == null) {
            throw new IllegalArgumentException("inactiveSelectionBackgroundColor is null.");
        }

        setInactiveSelectionBackgroundColor(GraphicsUtilities.decodeColor(inactiveSelectionBackgroundColor));
    }

    public Insets getMargin() {
        return margin;
    }

    public void setMargin(Insets margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        this.margin = margin;
        invalidateComponent();
    }

    public final void setMargin(Dictionary<String, ?> margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        setMargin(new Insets(margin));
    }

    public final void setMargin(int margin) {
        setMargin(new Insets(margin));
    }

    public final void setMargin(Number margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        setMargin(margin.intValue());
    }

    public final void setMargin(String margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        setMargin(Insets.decode(margin));
    }

    public boolean getWrapText() {
        return wrapText;
    }

    public void setWrapText(boolean wrapText) {
        if (this.wrapText != wrapText) {
            this.wrapText = wrapText;

            if (documentView != null) {
                documentView.invalidate();
            }
        }
    }
    @Override
    public boolean mouseMove(Component component, int x, int y) {
        boolean consumed = super.mouseMove(component, x, y);

        if (Mouse.getCapturer() == component) {
            TextArea textArea = (TextArea)getComponent();

            Bounds visibleArea = textArea.getVisibleArea();
            visibleArea = new Bounds(visibleArea.x, visibleArea.y,
                visibleArea.width, visibleArea.height);

            if (y >= visibleArea.y
                && y < visibleArea.y + visibleArea.height) {
                // Stop the scroll selection timer
                if (scheduledScrollSelectionCallback != null) {
                    scheduledScrollSelectionCallback.cancel();
                    scheduledScrollSelectionCallback = null;
                }

                scrollDirection = null;
                int offset = getInsertionPoint(x, y);

                if (offset != -1) {
                    // Select the range
                    if (offset > anchor) {
                        textArea.setSelection(anchor, offset - anchor);
                    } else {
                        textArea.setSelection(offset, anchor - offset);
                    }
                }
            } else {
                if (scheduledScrollSelectionCallback == null) {
                    scrollDirection = (y < visibleArea.y) ? FocusTraversalDirection.BACKWARD : FocusTraversalDirection.FORWARD;

                    scheduledScrollSelectionCallback =
                        ApplicationContext.scheduleRecurringCallback(scrollSelectionCallback,
                            SCROLL_RATE);

                    // Run the callback once now to scroll the selection immediately
                    scrollSelectionCallback.run();
                }
            }

            mouseX = x;
        } else {
            if (Mouse.isPressed(Mouse.Button.LEFT)
                && Mouse.getCapturer() == null
                && anchor != -1) {
                // Capture the mouse so we can select text
                Mouse.capture(component);
            }
        }

        return consumed;
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        if (button == Mouse.Button.LEFT) {
            TextArea textArea = (TextArea)component;

            anchor = getInsertionPoint(x, y);

            if (anchor != -1) {
                if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                    // Select the range
                    int selectionStart = textArea.getSelectionStart();

                    if (anchor > selectionStart) {
                        textArea.setSelection(selectionStart, anchor - selectionStart);
                    } else {
                        textArea.setSelection(anchor, selectionStart - anchor);
                    }
                } else {
                    // Move the caret to the insertion point
                    textArea.setSelection(anchor, 0);
                    consumed = true;
                }
            }

            caretX = caret.x;

            // Set focus to the text input
            textArea.requestFocus();
        }

        return consumed;
    }

    @Override
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseUp(component, button, x, y);

        if (Mouse.getCapturer() == component) {
            // Stop the scroll selection timer
            if (scheduledScrollSelectionCallback != null) {
                scheduledScrollSelectionCallback.cancel();
                scheduledScrollSelectionCallback = null;
            }

            Mouse.release();
        }

        anchor = -1;
        scrollDirection = null;
        mouseX = -1;

        return consumed;
    }


    @Override
    public boolean keyTyped(final Component component, char character) {
        boolean consumed = super.keyTyped(component, character);

        final TextArea textArea = (TextArea)getComponent();

        if (textArea.isEditable()) {
            Document document = textArea.getDocument();

            if (document != null) {
                // Ignore characters in the control range and the ASCII delete
                // character as well as meta key presses
                if (character > 0x1F
                    && character != 0x7F
                    && !Keyboard.isPressed(Keyboard.Modifier.META)) {
                    textArea.insertText(character);
                    showCaret(true);
                }
            }
        }

        return consumed;
    }

    @Override
    public boolean keyPressed(final Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        final TextArea textArea = (TextArea)getComponent();
        Document document = textArea.getDocument();

        Keyboard.Modifier commandModifier = Platform.getCommandModifier();
        if (document != null) {
            if (keyCode == Keyboard.KeyCode.ENTER
                && textArea.isEditable()) {
                textArea.insertParagraph();

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.DELETE
                && textArea.isEditable()) {
                textArea.delete(false);

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.BACKSPACE
                && textArea.isEditable()) {
                textArea.delete(true);

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.LEFT) {
                int selectionStart = textArea.getSelectionStart();
                int selectionLength = textArea.getSelectionLength();

                if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                    // Add the previous character to the selection
                    if (selectionStart > 0) {
                        selectionStart--;
                        selectionLength++;
                    }
                } else {
                    // Clear the selection and move the caret back by one
                    // character
                    if (selectionLength == 0
                        && selectionStart > 0) {
                        selectionStart--;
                    }

                    selectionLength = 0;
                }

                textArea.setSelection(selectionStart, selectionLength);
                scrollCharacterToVisible(selectionStart);

                caretX = caret.x;

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.RIGHT) {
                int selectionStart = textArea.getSelectionStart();
                int selectionLength = textArea.getSelectionLength();

                if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                    // Add the next character to the selection
                    if (selectionStart + selectionLength < document.getCharacterCount()) {
                        selectionLength++;
                    }

                    textArea.setSelection(selectionStart, selectionLength);
                    scrollCharacterToVisible(selectionStart + selectionLength);
                } else {
                    // Clear the selection and move the caret forward by one
                    // character
                    if (selectionLength > 0) {
                        selectionStart += selectionLength - 1;
                    }

                    if (selectionStart < document.getCharacterCount() - 1) {
                        selectionStart++;
                    }

                    textArea.setSelection(selectionStart, 0);
                    scrollCharacterToVisible(selectionStart);

                    caretX = caret.x;
                }

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.UP) {
                int selectionStart = textArea.getSelectionStart();

                int offset = getNextInsertionPoint(caretX, selectionStart, FocusTraversalDirection.BACKWARD);

                if (offset == -1) {
                    offset = 0;
                }

                int selectionLength;
                if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                    int selectionEnd = selectionStart + textArea.getSelectionLength() - 1;
                    selectionLength = selectionEnd - offset + 1;
                } else {
                    selectionLength = 0;
                }

                textArea.setSelection(offset, selectionLength);
                scrollCharacterToVisible(offset);

                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.DOWN) {
                int selectionStart = textArea.getSelectionStart();
                int selectionLength = textArea.getSelectionLength();

                if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                    int from;
                    int x;
                    if (selectionLength == 0) {
                        // Get next insertion point from leading selection character
                        from = selectionStart;
                        x = caretX;
                    } else {
                        // Get next insertion point from right edge of trailing selection
                        // character
                        from = selectionStart + selectionLength - 1;

                        Bounds trailingSelectionBounds = getCharacterBounds(from);
                        x = trailingSelectionBounds.x + trailingSelectionBounds.width;
                    }

                    int offset = getNextInsertionPoint(x, from, FocusTraversalDirection.FORWARD);

                    if (offset == -1) {
                        offset = documentView.getCharacterCount() - 1;
                    } else {
                        // If the next character is a paragraph terminator and is not the
                        // final terminator character, increment the selection
                        if (document.getCharacterAt(offset) == '\n'
                            && offset < documentView.getCharacterCount() - 1) {
                            offset++;
                        }
                    }

                    textArea.setSelection(selectionStart, offset - selectionStart);
                    scrollCharacterToVisible(offset);
                } else {
                    int from;
                    if (selectionLength == 0) {
                        // Get next insertion point from leading selection character
                        from = selectionStart;
                    } else {
                        // Get next insertion point from trailing selection character
                        from = selectionStart + selectionLength - 1;
                    }

                    int offset = getNextInsertionPoint(caretX, from, FocusTraversalDirection.FORWARD);

                    if (offset == -1) {
                        offset = documentView.getCharacterCount() - 1;
                    }

                    textArea.setSelection(offset, 0);
                    scrollCharacterToVisible(offset);
                }

                consumed = true;
            } else if (Keyboard.isPressed(commandModifier)) {
                if (keyCode == Keyboard.KeyCode.A) {
                    textArea.setSelection(0, document.getCharacterCount());
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.X
                    && textArea.isEditable()) {
                    textArea.cut();
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.C) {
                    textArea.copy();
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.V
                    && textArea.isEditable()) {
                    textArea.paste();
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.Z
                    && textArea.isEditable()) {
                    if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                        textArea.undo();
                    } else {
                        textArea.redo();
                    }

                    consumed = true;
                }
            } else {
                consumed = super.keyPressed(component, keyCode, keyLocation);
            }
        }

        return consumed;
    }

    // Component state events
    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        repaintComponent();
    }

    @Override
    public void focusedChanged(Component component, Component obverseComponent) {
        super.focusedChanged(component, obverseComponent);

        TextArea textArea = (TextArea)getComponent();
        if (textArea.isFocused()
            && textArea.getSelectionLength() == 0) {
            scrollCharacterToVisible(textArea.getSelectionStart());
            showCaret(true);
        } else {
            showCaret(false);
        }

        repaintComponent();
    }

    // Text area events
    @Override
    public void documentChanged(TextArea textArea, Document previousDocument) {
        if (documentView != null) {
            documentView.detach();
            documentView = null;
        }

        Document document = textArea.getDocument();
        if (document != null) {
            documentView = (DocumentView)createNodeView(document);
            documentView.attach();
        }

        invalidateComponent();
    }

    @Override
    public void editableChanged(TextArea textArea) {
        // No-op
    }

    @Override
    public void textKeyChanged(TextArea textArea, String previousTextKey) {
        // No-op
    }

    @Override
    public void textBindTypeChanged(TextArea textArea, BindType previousTextBindType) {
        // No-op
    }

    @Override
    public void textBindMappingChanged(TextArea textArea, TextArea.TextBindMapping previousTextBindMapping) {
        // No-op
    }

    // Text area selection events
    @Override
    public void selectionChanged(TextArea textArea, int previousSelectionStart,
        int previousSelectionLength) {
        // If the document view is valid, repaint the selection state; otherwise,
        // the selection will be updated in layout()
        if (documentView != null
            && documentView.isValid()) {
            if (selection == null) {
                // Repaint previous caret bounds
                textArea.repaint(caret.x, caret.y, caret.width, caret.height);
            } else {
                // Repaint previous selection bounds
                Rectangle bounds = selection.getBounds();
                textArea.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            updateSelection();

            if (selection == null) {
                showCaret(textArea.isFocused());
            } else {
                showCaret(false);

                // Repaint current selection bounds
                Rectangle bounds = selection.getBounds();
                textArea.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }
    }

    private NodeView createNodeView(Node node) {
        NodeView nodeView = null;

        if (node instanceof Document) {
            nodeView = new DocumentView((Document)node);
        } else if (node instanceof Paragraph) {
            nodeView = new ParagraphView((Paragraph)node);
        } else if (node instanceof TextNode) {
            nodeView = new TextNodeView((TextNode)node);
        } else if (node instanceof ImageNode) {
            nodeView = new ImageNodeView((ImageNode)node);
        } else {
            throw new IllegalArgumentException("Unsupported node type: "
                + node.getClass().getName());
        }

        return nodeView;
    }

    private void updateSelection() {
        if (documentView.getCharacterCount() > 0) {
            TextArea textArea = (TextArea)getComponent();

            // Update the caret
            int selectionStart = textArea.getSelectionStart();

            Bounds leadingSelectionBounds = getCharacterBounds(selectionStart);
            caret = leadingSelectionBounds.toRectangle();
            caret.width = 1;

            // Update the selection
            int selectionLength = textArea.getSelectionLength();

            if (selectionLength > 0) {
                int selectionEnd = selectionStart + selectionLength - 1;
                Bounds trailingSelectionBounds = getCharacterBounds(selectionEnd);
                selection = new Area();

                int firstRowIndex = getRowIndex(selectionStart);
                int lastRowIndex = getRowIndex(selectionEnd);

                if (firstRowIndex == lastRowIndex) {
                    selection.add(new Area(new Rectangle(leadingSelectionBounds.x, leadingSelectionBounds.y,
                        trailingSelectionBounds.x + trailingSelectionBounds.width - leadingSelectionBounds.x,
                        trailingSelectionBounds.y + trailingSelectionBounds.height - leadingSelectionBounds.y)));
                } else {
                    int width = getWidth();

                    selection.add(new Area(new Rectangle(leadingSelectionBounds.x,
                        leadingSelectionBounds.y,
                        width - margin.right - leadingSelectionBounds.x,
                        leadingSelectionBounds.height)));

                    if (lastRowIndex - firstRowIndex > 0) {
                        selection.add(new Area(new Rectangle(margin.left,
                            leadingSelectionBounds.y + leadingSelectionBounds.height,
                            width - (margin.left + margin.right),
                            trailingSelectionBounds.y - (leadingSelectionBounds.y
                                + leadingSelectionBounds.height))));
                    }

                    selection.add(new Area(new Rectangle(margin.left, trailingSelectionBounds.y,
                        trailingSelectionBounds.x + trailingSelectionBounds.width - margin.left,
                        trailingSelectionBounds.height)));
                }
            } else {
                selection = null;
            }
        } else {
            // Clear the caret and the selection
            caret = new Rectangle();
            selection = null;
        }
    }

    private void showCaret(boolean show) {
        if (scheduledBlinkCaretCallback != null) {
            scheduledBlinkCaretCallback.cancel();
        }

        if (show) {
            caretOn = true;
            scheduledBlinkCaretCallback =
                ApplicationContext.scheduleRecurringCallback(blinkCaretCallback,
                    Platform.getCursorBlinkRate());

            // Run the callback once now to show the cursor immediately
            blinkCaretCallback.run();
        } else {
            scheduledBlinkCaretCallback = null;
        }
    }
}
