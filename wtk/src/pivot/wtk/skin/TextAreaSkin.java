/*
 * Copyright (c) 2009 VMware, Inc.
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
package pivot.wtk.skin;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Iterator;

import pivot.collections.ArrayList;
import pivot.collections.Sequence;
import pivot.util.ImmutableIterator;
import pivot.wtk.Bounds;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Platform;
import pivot.wtk.Point;
import pivot.wtk.TextArea;
import pivot.wtk.TextAreaListener;
import pivot.wtk.Theme;
import pivot.wtk.Visual;
import pivot.wtk.text.Document;
import pivot.wtk.text.Element;
import pivot.wtk.text.ElementListener;
import pivot.wtk.text.Node;
import pivot.wtk.text.NodeListener;
import pivot.wtk.text.Paragraph;
import pivot.wtk.text.TextNode;
import pivot.wtk.text.TextNodeListener;

/**
 * Text area skin.
 *
 * @author gbrown
 */
public class TextAreaSkin extends ComponentSkin implements TextAreaListener {
    /**
     * Abstract base class for node views.
     *
     * @author gbrown
     */
    public abstract class NodeView implements Visual, NodeListener {
        private Node node = null;
        private ElementView parent = null;

        private int width = -1;
        private int height = -1;
        private boolean valid = false;

        private int x = 0;
        private int y = 0;

        private int maximumWidth = -1;

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

        public int getWidth() {
            validate();
            assert(width >= 0);
            return width;
        }

        public int getHeight() {
            validate();
            assert(height >= 0);
            return height;
        }

        public Dimensions getSize() {
            validate();
            assert(width >= 0
                && height >= 0);
            return new Dimensions(width, height);
        }

        protected void setSize(int width, int height) {
            assert(width >= 0);
            assert(height >= 0);
            assert(maximumWidth == -1
                || width <= maximumWidth);

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
            return new Bounds(x, y, getWidth(), getHeight());
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
            width = -1;
            height = -1;
            valid = false;

            if (parent != null) {
                parent.invalidate();
            }
        }

        public void validate() {
            valid = true;
        }

        public int getMaximumWidth() {
            return maximumWidth;
        }

        public void setMaximumWidth(int maximumWidth) {
            if (maximumWidth < -1) {
                throw new IllegalArgumentException(maximumWidth
                    + " is not a valid value for maximumWidth.");
            }

            int previousMaximumWidth = this.maximumWidth;

            if (previousMaximumWidth != maximumWidth) {
                this.maximumWidth = maximumWidth;
                invalidate();
            }
        }

        public abstract NodeView getNext();

        public void parentChanged(Node node, Element previousParent) {
            // No-op
        }

        public void offsetChanged(Node node, int previousOffset) {
            // No-op
        }
    }

    /**
     * Abstract base class for element views.
     *
     * @author gbrown
     */
    public abstract class ElementView extends NodeView
        implements Sequence<NodeView>, Iterable<NodeView>, ElementListener {
        private ArrayList<NodeView> nodeViews = new ArrayList<NodeView>();

        public ElementView(Element element) {
            super(element);

        }

        @Override
        protected void attach() {
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
        }

        public int add(NodeView nodeView) {
            int index = getLength();
            insert(nodeView, index);

            return index;
        }

        public void insert(NodeView nodeView, int index) {
            nodeView.setParent(this);
            nodeView.attach();

            nodeViews.insert(nodeView, index);
        }

        public NodeView update(int index, NodeView nodeView) {
            throw new UnsupportedOperationException();
        }

        public int remove(NodeView nodeView) {
            int index = indexOf(nodeView);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        public Sequence<NodeView> remove(int index, int count) {
            Sequence<NodeView> removed = nodeViews.remove(index, count);

            for (int i = 0, n = removed.getLength(); i < n; i++) {
                NodeView nodeView = removed.get(i);
                nodeView.setParent(null);
                nodeView.detach();
            }

            return removed;
        }

        public NodeView get(int index) {
            return nodeViews.get(index);
        }

        public int indexOf(NodeView nodeView) {
            return nodeViews.indexOf(nodeView);
        }

        public int getLength() {
            return nodeViews.getLength();
        }

        public void paint(Graphics2D graphics) {
            java.awt.Rectangle clipBounds = graphics.getClipBounds();
            Bounds paintBounds = (clipBounds == null) ?
                new Bounds(0, 0, getWidth(), getHeight()) : new Bounds(clipBounds);

            for (NodeView nodeView : nodeViews) {
                Bounds nodeViewBounds = nodeView.getBounds();

                // Only paint node views that intersect the current clip rectangle
                if (nodeViewBounds.intersects(paintBounds)) {
                    // Create a copy of the current graphics context and
                    // translate to the node view's coordinate system
                    Graphics2D nodeViewGraphics = (Graphics2D)graphics.create();
                    nodeViewGraphics.translate(nodeViewBounds.x, nodeViewBounds.y);
                    nodeViewGraphics.clipRect(0, 0, nodeViewBounds.width, nodeViewBounds.height);

                    // Paint the node view
                    nodeView.paint(nodeViewGraphics);

                    // Dispose of the node views's graphics
                    nodeViewGraphics.dispose();
                }
            }
        }

        public abstract NodeView getNodeViewAt(int x, int y);

        public void nodeInserted(Element element, int index) {
            invalidate();
        }

        public void nodesRemoved(Element element, int index, Sequence<Node> nodes) {
            invalidate();
        }

        public Iterator<NodeView> iterator() {
            return new ImmutableIterator<NodeView>(nodeViews.iterator());
        }
    }

    /**
     * Document view.
     *
     * @author gbrown
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

            TextAreaSkin.this.repaintComponent(x, y, width, height);
        }

        @Override
        public void invalidate() {
            super.invalidate();

            TextAreaSkin.this.invalidateComponent();
        }

        @Override
        public void validate() {
            if (!isValid()) {
                int width = 0;

                int y = 0;
                for (NodeView nodeView : this) {
                    nodeView.setMaximumWidth(getMaximumWidth());
                    nodeView.validate();

                    nodeView.setLocation(0, y);

                    width = Math.max(width, nodeView.getWidth());
                    y += nodeView.getHeight();
                }

                setSize(width, y);
            }

            super.validate();
        }

        @Override
        public NodeView getNext() {
            return null;
        }

        @Override
        public NodeView getNodeViewAt(int x, int y) {
            // TODO Perform a binary search for the node view at the given
            // y-coordinate
            return null;
        }
    }

    public class ParagraphView extends ElementView {
        public ParagraphView(Paragraph paragraph) {
            super(paragraph);
        }

        @Override
        public void validate() {
            if (!isValid()) {
                Paragraph paragraph = (Paragraph)getNode();

                int maximumWidth = getMaximumWidth();

                // Determine index of first invalid node view
                int i = 0;
                int n = getLength();
                if (i < n) {
                    NodeView nodeView;
                    do {
                        nodeView = get(i++);
                        nodeView.setMaximumWidth(maximumWidth);
                    } while (nodeView.isValid());
                }

                i--;

                // Determine index of first node for which new views will be
                // created
                NodeView nodeView;
                int j;

                if (i < 0) {
                    nodeView = null;
                    j = -1;
                } else {
                    nodeView = get(i);
                    Node node = nodeView.getNode();
                    j = paragraph.indexOf(node);
                }

                j++;

                // Remove all subsequent views; they will be recreated
                i++;
                remove(i, n - i);

                // Initialize geometry values
                int width = 0;

                int x = 0;
                int y = 0;

                int lineWidth = 0;
                int lineHeight = 0;

                int c = paragraph.getLength();
                if (nodeView == null
                    && j < c) {
                    Node node = paragraph.get(j++);
                    nodeView = createNodeView(node);
                }

                while (nodeView != null) {
                    nodeView.setMaximumWidth(maximumWidth - x);
                    nodeView.validate();

                    // Set view position; update geometry values
                    nodeView.setLocation(x, y);
                    lineWidth += nodeView.getWidth();
                    lineHeight = Math.max(lineHeight, nodeView.getHeight());

                    x += nodeView.getWidth();
                    if (x > maximumWidth) {
                        width = Math.max(width, lineWidth);
                        lineWidth = 0;

                        x = 0;

                        y += lineHeight;
                        lineHeight = 0;
                    }

                    // Add node view and subsequent views to this view
                    while (nodeView != null) {
                        add(nodeView);
                        nodeView = nodeView.getNext();
                    }

                    // Create a new view for the next node
                    if (j < c) {
                        Node node = paragraph.get(j++);
                        nodeView = createNodeView(node);
                    } else {
                        nodeView = null;
                    }
                };

                // TODO Don't hard-code padding
                setSize(width, y + 6);
            }

            super.validate();
        }

        @Override
        public NodeView getNext() {
            return null;
        }

        @Override
        public NodeView getNodeViewAt(int x, int y) {
            // TODO Perform a binary search based on both x and y values
            return null;
        }
    }

    /**
     * Text node view.
     *
     * @author gbrown
     */
    public class TextNodeView extends NodeView implements TextNodeListener {
        private int start;
        private int length = -1;

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
            length = -1;
            super.invalidate();
        }

        @Override
        public void validate() {
            if (!isValid()) {
                TextNode textNode = (TextNode)getNode();
                String text = textNode.getText();

                // Reset the length and next node values
                length = text.length() - start;
                next = null;

                // Get the characters that this view represents
                // TODO Optimize using a character iterator
                text = text.substring(start, length);

                // Trim whitespace
                text = text.trim();

                // Calculate the size of the text
                int width;
                int height;

                if (text.length() == 0) {
                    // Return a width of 0 and the font height
                    width = 0;

                    LineMetrics lm = font.getLineMetrics(text, fontRenderContext);
                    height = (int)Math.ceil(lm.getAscent() + lm.getDescent()
                        + lm.getLeading());
                } else {
                    int maximumWidth = getMaximumWidth();

                    if (maximumWidth == -1) {
                        // Calculate the unconstrained text bounds
                        Rectangle2D stringBounds = font.getStringBounds(text,
                            fontRenderContext);
                        width = (int)Math.ceil(stringBounds.getWidth());
                        height = (int)Math.ceil(stringBounds.getHeight());
                    } else {
                        // Attempt to break the text at max. width
                        AttributedString attributedText = new AttributedString(text);
                        attributedText.addAttribute(TextAttribute.FONT, font);

                        AttributedCharacterIterator aci = attributedText.getIterator();
                        LineBreakMeasurer lbm = new LineBreakMeasurer(aci,
                            fontRenderContext);

                        assert(lbm.getPosition() < aci.getEndIndex());

                        // Get the break index
                        int end = lbm.nextOffset(maximumWidth);

                        text = text.substring(start, end);
                        Rectangle2D stringBounds = font.getStringBounds(text,
                            fontRenderContext);
                        width = (int)Math.ceil(stringBounds.getWidth());
                        height = (int)Math.ceil(stringBounds.getHeight());

                        // Create a new node containing the remainder of the text
                        if (end < length) {
                            next = new TextNodeView(textNode, end);
                            length = end - start;

                            next.setMaximumWidth(maximumWidth);
                            next.validate();
                        }
                    }
                }

                setSize(width, height);
            }

            super.validate();
        }

        public void paint(Graphics2D graphics) {
            TextNode textNode = (TextNode)getNode();
            String text = textNode.getText();

            // TODO Optimize using a character iterator
            text = text.substring(start, length - start);

            if (text.length() > 0) {
                LineMetrics lm = font.getLineMetrics(text, fontRenderContext);

                if (fontRenderContext.isAntiAliased()) {
                    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        Platform.getTextAntialiasingHint());
                }

                if (fontRenderContext.usesFractionalMetrics()) {
                    graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                        RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                }

                graphics.setFont(font);
                graphics.setPaint(Color.BLACK);
                graphics.drawString(text, 0, lm.getAscent());
            }
        }

        @Override
        public NodeView getNext() {
            return next;
        }

        public void charactersInserted(TextNode textNode, int index, int count) {
            invalidate();
        }

        public void charactersRemoved(TextNode textNode, int index, String characters) {
            invalidate();
        }
    }

    private DocumentView documentView = null;

    private FontRenderContext fontRenderContext = new FontRenderContext(null, true, true);

    private Font font;

    public TextAreaSkin() {
        Theme theme = Theme.getTheme();
        font = theme.getFont();
    }

    public void install(Component component) {
        super.install(component);

        TextArea textArea = (TextArea)component;
        textArea.getTextAreaListeners().add(this);

        Document text = textArea.getText();
        if (text != null) {
            documentView = new DocumentView(text);
            documentView.attach();
        }
    }

    public void uninstall() {
        TextArea textArea = (TextArea)getComponent();
        textArea.getTextAreaListeners().remove(this);

        if (documentView != null) {
            documentView.detach();
            documentView = null;
        }

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth;
        if (documentView == null) {
            preferredWidth = 0;
        } else {
            documentView.setMaximumWidth(-1);
            preferredWidth = documentView.getWidth();
        }

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight;
        if (documentView == null) {
            preferredHeight = 0;
        } else {
            documentView.setMaximumWidth(width);
            preferredHeight = documentView.getHeight();
        }

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        Dimensions preferredSize;

        if (documentView == null) {
            preferredSize = new Dimensions(0, 0);
        } else {
            documentView.setMaximumWidth(-1);
            preferredSize = documentView.getSize();
        }

        return preferredSize;
    }

    public void layout() {
        if (documentView != null) {
            // TODO Here is where we would resize/reposition form components
            // (i.e. components attached to ComponentNodeViews); we'd probably
            // want a top-level list of ComponentNodeViews so we wouldn't have
            // to search the tree for them.
        }
    }

    public void paint(Graphics2D graphics) {
        if (documentView != null) {
            documentView.paint(graphics);
        }
    }

    public void textChanged(TextArea textArea, Document previousText) {
        if (documentView != null) {
            documentView.detach();
            documentView = null;
        }

        Document text = textArea.getText();
        if (text != null) {
            documentView = new DocumentView(text);
            documentView.attach();
        }

        invalidateComponent();
    }

    public NodeView createNodeView(Node node) {
        NodeView nodeView = null;

        if (node instanceof Document) {
            nodeView = new DocumentView((Document)node);
        } else if (node instanceof Paragraph) {
            nodeView = new ParagraphView((Paragraph)node);
        } else if (node instanceof TextNode) {
            nodeView = new TextNodeView((TextNode)node);
        } else {
            throw new IllegalArgumentException("Unsupported node type: "
                + node.getClass().getName());
        }

        return nodeView;
    }
}
