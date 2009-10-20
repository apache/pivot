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
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.text.CharacterIterator;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Cursor;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Direction;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TextAreaListener;
import org.apache.pivot.wtk.TextAreaSelectionListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Visual;
import org.apache.pivot.wtk.media.Image;
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
            return getBounds(true);
        }

        public Bounds getBounds(boolean validate) {
            if (validate) {
                validate();
            }

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
            if (breakWidth < 0) {
                throw new IllegalArgumentException(breakWidth
                    + " is not a valid value for breakWidth.");
            }

            int previousMaximumWidth = this.breakWidth;

            if (previousMaximumWidth != breakWidth) {
                this.breakWidth = breakWidth;
                invalidate();
            }
        }

        public abstract int getOffset();
        public abstract NodeView getNext();
        public abstract int getCharacterAt(int x, int y);
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
        /**
         * Null node view, used for binary searches.
         */
        private class NullNodeView extends NodeView {
            private int offset;

            public NullNodeView() {
                super(null);
            }

            @Override
            public int getOffset() {
                return offset;
            }

            protected void setOffset(int offset) {
                this.offset = offset;
            }

            @Override
            public NodeView getNext() {
                return null;
            }

            @Override
            public int getCharacterAt(int x, int y) {
                return -1;
            }

            @Override
            public Bounds getCharacterBounds(int offset) {
                return null;
            }

            @Override
            public void paint(Graphics2D graphics) {
                // No-op
            }
        }

        /**
         * Comparator used to perform binary searches on node views by location.
         */
        private class NodeViewLocationComparator implements Comparator<NodeView> {
            @Override
            public int compare(NodeView nodeView1, NodeView nodeView2) {
                int x1 = nodeView1.getX();
                int y1 = nodeView1.getY();

                int x2 = nodeView2.getX();
                int y2 = nodeView2.getY();

                int width = (x2 - x1);

                return (y1 * width + x1) - (y2 * width + x2);
            }
        }

        /**
         * Comparator used to perform binary searches on node views by offset.
         */
        private class NodeViewOffsetComparator implements Comparator<NodeView> {
            @Override
            public int compare(NodeView nodeView1, NodeView nodeView2) {
                int offset1 = nodeView1.getOffset();
                int offset2 = nodeView2.getOffset();

                return (offset1 - offset2);
            }
        }

        private ArrayList<NodeView> nodeViews = new ArrayList<NodeView>();

        private NullNodeView nullNodeView = new NullNodeView();
        private NodeViewLocationComparator nodeViewLocationComparator = new NodeViewLocationComparator();
        private NodeViewOffsetComparator nodeViewOffsetComparator = new NodeViewOffsetComparator();

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
            remove(0, getLength());

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
                Bounds nodeViewBounds = nodeView.getBounds(false);

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
        public int getOffset() {
            return getNode().getOffset();
        }

        @Override
        public int getCharacterAt(int x, int y) {
            // Get the index of the node view at x, y
            nullNodeView.setLocation(x, y);

            int i = ArrayList.binarySearch(nodeViews, nullNodeView,
                nodeViewLocationComparator);

            if (i < 0) {
                i = -(i + 1) - 1;
            }

            // TODO i should never be less than 0 here? What about in getCharacterBounds()?
            int offset;
            if (i < 0) {
                offset = -1;
            } else {
                if (i < nodeViews.getLength()) {
                    NodeView nodeView = nodeViews.get(i);

                    // Adjust the x and y values
                    x -= nodeView.getX();
                    y -= nodeView.getY();

                    offset = nodeView.getCharacterAt(x, y);

                    // Adjust the offset
                    Node node = nodeView.getNode();
                    offset += node.getOffset();
                } else {
                    // Return the character count of this node
                    Node node = getNode();
                    offset = node.getCharacterCount();
                }
            }

            return offset;
        }

        @Override
        public Bounds getCharacterBounds(int offset) {
            // Get the index of the node view at offset
            nullNodeView.setOffset(offset);

            int i = ArrayList.binarySearch(nodeViews, nullNodeView,
                nodeViewOffsetComparator);

            if (i < 0) {
                i = -(i + 1) - 1;
            }

            Bounds bounds = null;
            if (i < nodeViews.getLength()) {
                NodeView nodeView = nodeViews.get(i);

                offset -= nodeView.getOffset();
                bounds = nodeView.getCharacterBounds(offset);
                bounds = bounds.translate(nodeView.getX(), nodeView.getY());
            }

            return bounds;
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
                invalidateComponent();
            }
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
        private Bounds terminatorBounds = null;

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
                // Build the row list
                Paragraph paragraph = (Paragraph)getNode();
                ArrayList<ArrayList<NodeView>> rows = new ArrayList<ArrayList<NodeView>>();

                // Break the views into multiple rows
                int breakWidth = Math.max(getBreakWidth() - PARAGRAPH_TERMINATOR_WIDTH, 0);

                ArrayList<NodeView> row = new ArrayList<NodeView>();
                int rowWidth = 0;

                for (Node node : paragraph) {
                    NodeView nodeView = createNodeView(node);

                    nodeView.setBreakWidth(Math.max(breakWidth - rowWidth, 0));
                    nodeView.validate();

                    int nodeViewWidth = nodeView.getWidth();

                    if (rowWidth + nodeViewWidth > breakWidth
                        && rowWidth > 0) {
                        // The view is too big to fit in the remaining space,
                        // and it is not the only view in this row
                        rows.add(row);
                        row = new ArrayList<NodeView>();
                        rowWidth = 0;
                    }

                    // Add the view to the row
                    row.add(nodeView);
                    rowWidth += nodeViewWidth;

                    // If the view was split into multiple views, add them to
                    // their own rows
                    nodeView = nodeView.getNext();
                    while (nodeView != null) {
                        rows.add(row);
                        row = new ArrayList<NodeView>();

                        nodeView.setBreakWidth(breakWidth);
                        nodeView.validate();

                        row.add(nodeView);
                        rowWidth = nodeView.getWidth();

                        nodeView = nodeView.getNext();
                    }
                }

                // Add the last row
                if (row.getLength() > 0) {
                    rows.add(row);
                }

                // Clear all existing views
                remove(0, getLength());

                // Add the row views to this view, lay out, and calculate size
                int width = 0;
                int height = 0;

                for (int i = 0, n = rows.getLength(); i < n; i++) {
                    row = rows.get(i);

                    // Determine the row height
                    int rowHeight = 0;
                    for (NodeView nodeView : row) {
                        rowHeight = Math.max(rowHeight, nodeView.getHeight());
                    }

                    int x = 0;
                    for (NodeView nodeView : row) {
                        // TODO Align to baseline
                        int y = rowHeight - nodeView.getHeight();

                        nodeView.setLocation(x, y + height);
                        x += nodeView.getWidth();

                        add(nodeView);
                    }

                    width = Math.max(width, x);
                    height += rowHeight;
                }

                // Recalculate terminator bounds
                LineMetrics lm = font.getLineMetrics("", 0, 0, FONT_RENDER_CONTEXT);
                terminatorBounds = new Bounds(0, 0, PARAGRAPH_TERMINATOR_WIDTH,
                    (int)Math.ceil(lm.getHeight()));

                int n = getLength();
                if (n > 0) {
                    NodeView lastNodeView = get(n - 1);
                    terminatorBounds = new Bounds(lastNodeView.getX() + lastNodeView.getWidth(),
                        lastNodeView.getY(), terminatorBounds.width, terminatorBounds.height);
                }

                // Ensure that the paragraph is at least as large as the
                // terminator, so it still has space even when empty
                width = Math.max(width, terminatorBounds.width);
                height = Math.max(height, terminatorBounds.height);

                // TODO Don't hard-code padding; use the value specified
                // by the Paragraph
                setSize(width, height + 2);
            }

            super.validate();
        }

        @Override
        public void paint(Graphics2D graphics) {
            super.paint(graphics);

            // TODO Make this styleable
            /*
            Paragraph paragraph = (Paragraph)getNode();
            Bounds terminatorBounds = getCharacterBounds(paragraph.getCharacterCount() - 1);
            graphics.setColor(Color.RED);
            graphics.fillRect(terminatorBounds.x, terminatorBounds.y,
                terminatorBounds.width, terminatorBounds.height);
            */
        }

        @Override
        public NodeView getNext() {
            return null;
        }

        @Override
        public int getCharacterAt(int x, int y) {
            Paragraph paragraph = (Paragraph)getNode();
            int terminatorOffset = paragraph.getCharacterCount() - 1;
            Bounds terminatorBounds = getCharacterBounds(terminatorOffset);

            int offset;
            if (terminatorBounds.contains(x, y)) {
                offset = terminatorOffset;
            } else {
                offset = super.getCharacterAt(x, y);
            }

            return offset;
        }

        @Override
        public Bounds getCharacterBounds(int offset) {
            Bounds bounds;

            Paragraph paragraph = (Paragraph)getNode();
            if (offset == paragraph.getCharacterCount() - 1) {
                bounds = new Bounds(terminatorBounds);
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
                if (lineWidth < breakWidth) {
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

                Rectangle2D logicalBounds = glyphVector.getLogicalBounds();
                setSize((int)Math.ceil(logicalBounds.getWidth()),
                    (int)Math.ceil(logicalBounds.getHeight()));
            }

            super.validate();
        }

        @Override
        public void paint(Graphics2D graphics) {
            if (glyphVector != null) {
                if (FONT_RENDER_CONTEXT.isAntiAliased()) {
                    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        Platform.getTextAntialiasingHint());
                }

                if (FONT_RENDER_CONTEXT.usesFractionalMetrics()) {
                    graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                        RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                }

                graphics.setFont(font);
                graphics.setPaint(color);

                LineMetrics lm = font.getLineMetrics("", FONT_RENDER_CONTEXT);
                graphics.drawGlyphVector(glyphVector, 0, lm.getAscent());
            }
        }

        @Override
        public int getOffset() {
            return getNode().getOffset() + start;
        }

        @Override
        public NodeView getNext() {
            return next;
        }

        @Override
        public int getCharacterAt(int x, int y) {
            validate();

            int n = glyphVector.getNumGlyphs();
            int i = 0;

            while (i < n) {
                GlyphMetrics glyphMetrics = glyphVector.getGlyphMetrics(i++);
                Rectangle2D bounds2D = glyphMetrics.getBounds2D();
                if (bounds2D.contains(x, bounds2D.getY())) {
                    break;
                }
            }

            int offset;
            if (i < n) {
                offset = i + start;
            } else {
                offset = -1;
            }

            return offset;
        }

        @Override
        public Bounds getCharacterBounds(int offset) {
            validate();

            GlyphMetrics glyphMetrics = glyphVector.getGlyphMetrics(offset);
            Rectangle2D bounds2D = glyphMetrics.getBounds2D();

            return new Bounds(0, 0, (int)Math.ceil(bounds2D.getWidth()), getHeight());
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
            return text.substring(start, start + length);
        }
    }

    public class ImageNodeView extends NodeView implements ImageNodeListener {
        public ImageNodeView(ImageNode imageNode) {
            super(imageNode);
        }

        @Override
        protected void attach() {
            super.attach();

            ImageNode imageNode = (ImageNode)getNode();
            imageNode.getImageNodeListeners().add(this);

            // TODO Add image listener so we can invalidate as needed
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
        public int getOffset() {
            return getNode().getOffset();
        }

        @Override
        public NodeView getNext() {
            return null;
        }

        @Override
        public int getCharacterAt(int x, int y) {
            return 0;
        }

        @Override
        public Bounds getCharacterBounds(int offset) {
            return new Bounds(0, 0, getWidth(), getHeight());
        }

        @Override
        public void imageChanged(ImageNode imageNode, Image previousImage) {
            invalidate();

            // TODO Attach/detach image listener
        }
    }

    private class BlinkCursorCallback implements Runnable {
        @Override
        public void run() {
            caretOn = !caretOn;

            TextArea textArea = (TextArea)getComponent();
            textArea.repaint(caret.x + margin.left, caret.y + margin.top,
                caret.width, caret.height, true);
        }
    }


    private DocumentView documentView = null;

    private static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(null, true, true);

    private int caretX = 0;
    private Rectangle caret = new Rectangle();
    private boolean caretOn = false;
    private BlinkCursorCallback blinkCursorCallback = new BlinkCursorCallback();
    private ApplicationContext.ScheduledCallback scheduledBlinkCursorCallback = null;

    private Font font;
    private Color color;
    private Insets margin = new Insets(4);

    public static final int PARAGRAPH_TERMINATOR_WIDTH = 2;

    public TextAreaSkin() {
        Theme theme = Theme.getTheme();
        font = theme.getFont();
        color = Color.BLACK;
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
        }

        selectionChanged(textArea, 0, 0);
    }

    @Override
    public boolean isFocusable() {
        TextArea textArea = (TextArea)getComponent();
        return textArea.isEditable();
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth;
        if (documentView == null) {
            preferredWidth = 0;
        } else {
            preferredWidth = documentView.getWidth() + margin.left + margin.right;
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight;
        if (documentView == null) {
            preferredHeight = 0;
        } else {
            preferredHeight = documentView.getHeight() + margin.top + margin.bottom;
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredWidth;
        int preferredHeight;

        if (documentView == null) {
            preferredWidth = 0;
            preferredHeight = 0;
        } else {
            Dimensions preferredSize = documentView.getSize();
            preferredWidth = preferredSize.width + (margin.left + margin.right);
            preferredHeight = preferredSize.height + (margin.top + margin.bottom);
        }

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
    public void layout() {
        if (documentView != null) {
            int width = getWidth();
            documentView.setBreakWidth(Math.max(width - (margin.left + margin.right), 0));
            documentView.validate();

            // TODO Why does this cause an exception?
            // updateSelectionBounds();
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        if (documentView != null) {
            TextArea textArea = (TextArea)getComponent();

            // TODO Paint selection state here, or in view classes?

            graphics.translate(margin.left, margin.top);
            documentView.paint(graphics);

            if (textArea.getSelectionLength() == 0
                && textArea.isFocused()
                && caretOn) {
                graphics.setPaint(Color.BLACK);
                graphics.fill(caret);
            }
        }
    }

    @Override
    public int getCharacterAt(int x, int y) {
        return (documentView == null) ?
            -1 : documentView.getCharacterAt(x - margin.left, y - margin.top);
    }

    @Override
    public Bounds getCharacterBounds(int offset) {
        return (documentView == null) ?
            null : documentView.getCharacterBounds(offset);
    }

    private void showCaret(boolean show) {
        if (show) {
            if (scheduledBlinkCursorCallback == null) {
                scheduledBlinkCursorCallback =
                    ApplicationContext.scheduleRecurringCallback(blinkCursorCallback,
                        Platform.getCursorBlinkRate());

                // Run the callback once now to show the cursor immediately
                blinkCursorCallback.run();
            }
        } else {
            if (scheduledBlinkCursorCallback != null) {
                scheduledBlinkCursorCallback.cancel();
                scheduledBlinkCursorCallback = null;
            }
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

    @Override
    public void locationChanged(Component component, int previousX, int previousY) {
        super.locationChanged(component, previousX, previousY);

        // TODO Is there a better way to do this? We are trying to ensure that the newly
        // visible area of the document is now valid.
        if (documentView != null
            && !documentView.isValid()
            && component.getY() > previousY) {
            invalidateComponent();
        }
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        TextArea textArea = (TextArea)component;

        if (button == Mouse.Button.LEFT) {
            // Move the caret to the insertion point
            int offset = getCharacterAt(x, y);
            if (offset != -1) {
                textArea.setSelection(offset, 0);
            }

            caretX = x;

            // TODO Register mouse listener to begin selecting text; also handle
            // auto-scrolling when the mouse moves outside the component

            // Set focus to the text input
            if (textArea.isEditable()) {
                textArea.requestFocus();
            }
        }

        return super.mouseDown(component, button, x, y);
    }

    @Override
    public boolean keyTyped(Component component, char character) {
        boolean consumed = super.keyTyped(component, character);

        TextArea textArea = (TextArea)getComponent();

        if (textArea.isEditable()) {
            Document document = textArea.getDocument();

            if (document != null) {
                // Ignore characters in the control range and the ASCII delete
                // character
                if (character > 0x1F
                    && character != 0x7F) {
                    textArea.insertText(character);
                }
            }
        }

        return consumed;
    }

    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        TextArea textArea = (TextArea)getComponent();

        if (textArea.isEditable()) {
            Document document = textArea.getDocument();

            Keyboard.Modifier commandModifier = Platform.getCommandModifier();
            if (document != null) {
                if (keyCode == Keyboard.KeyCode.ENTER) {
                    textArea.insertParagraph();
                    caretX = 0;
                } else if (keyCode == Keyboard.KeyCode.DELETE) {
                    textArea.delete(Direction.FORWARD);
                } else if (keyCode == Keyboard.KeyCode.BACKSPACE) {
                    textArea.delete(Direction.BACKWARD);
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
                    } else {
                        // Clear the selection and move the caret forward by one
                        // character
                        selectionStart += selectionLength;

                        if (selectionLength == 0
                            && selectionStart < document.getCharacterCount()) {
                            selectionStart++;
                        }

                        selectionLength = 0;
                    }

                    textArea.setSelection(selectionStart, selectionLength);

                    caretX = caret.x;

                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.UP) {
                    // TODO We shouldn't need a "magic" number like 2 here
                    int offset = documentView.getCharacterAt(caretX, caret.y - 2);

                    // TODO Modify selection based on SHIFT key
                    textArea.setSelection(offset, 0);

                    // TODO Make sure we scroll the next view to visible

                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.DOWN) {
                    int offset = documentView.getCharacterAt(caretX, caret.y + caret.height + 1);

                    // TODO Modify selection based on SHIFT key
                    textArea.setSelection(offset, 0);

                    // TODO Make sure we scroll the next view to visible

                    consumed = true;
                } else if (Keyboard.isPressed(commandModifier)) {
                    if (keyCode == Keyboard.KeyCode.A) {
                        textArea.setSelection(0, document.getCharacterCount());
                    } else if (keyCode == Keyboard.KeyCode.X) {
                        textArea.cut();
                    } else if (keyCode == Keyboard.KeyCode.C) {
                        textArea.copy();
                    } else if (keyCode == Keyboard.KeyCode.V) {
                        textArea.paste();
                    } else if (keyCode == Keyboard.KeyCode.Z) {
                        if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                            textArea.undo();
                        } else {
                            textArea.redo();
                        }
                    }
                } else {
                    consumed = super.keyPressed(component, keyCode, keyLocation);
                }
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
        showCaret(textArea.isFocused()
            && textArea.getSelectionLength() == 0);

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

    // Text area selection events
    @Override
    public void selectionChanged(TextArea textArea, int previousSelectionStart,
        int previousSelectionLength) {
        if (textArea.getDocument() != null) {
            updateSelectionBounds();

            showCaret(textArea.isFocused()
                && textArea.getSelectionLength() == 0);

            repaintComponent();
        }
    }

    private void updateSelectionBounds() {
        if (documentView.isValid()) {
            TextArea textArea = (TextArea)getComponent();

            int selectionStart = textArea.getSelectionStart();

            Bounds startCharacterBounds = getCharacterBounds(selectionStart);

            caret.x = startCharacterBounds.x;
            caret.y = startCharacterBounds.y;
            caret.width = 1;
            caret.height = startCharacterBounds.height;
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
}
