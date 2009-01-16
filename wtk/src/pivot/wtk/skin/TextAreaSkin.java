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

import java.awt.Graphics2D;

import pivot.collections.ArrayList;
import pivot.collections.Sequence;
import pivot.wtk.Bounds;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.TextArea;
import pivot.wtk.TextAreaListener;
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

        private int x = 0;
        private int y = 0;

        private int width = 0;
        private int height = 0;

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

        public int getWidth() {
            validate();
            return width;
        }

        public int getHeight() {
            validate();
            return height;
        }

        public Dimensions getSize() {
            validate();
            return new Dimensions(width, height);
        }

        protected void setSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        protected void setLocation(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Bounds getBounds() {
            return new Bounds(x, y, getWidth(), getHeight());
        }

        public void repaint() {
            repaint(0, 0, width, height);
        }

        public void repaint(int x, int y, int width, int height) {
            if (parent != null) {
                parent.repaint(x + this.x, y + this.y, width, height);
            }
        }

        public boolean isValid() {
            return valid;
        }

        public void invalidate(int x, int y) {
            // TODO Repaint the rectangle containing x, y, (width - x), (height - y)
            valid = false;
        }

        public void validate() {
            valid = true;
        }

        public abstract NodeView breakAt(int x);

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
        implements Sequence<NodeView>, ElementListener {
        private ArrayList<NodeView> nodeViews = new ArrayList<NodeView>();

        public ElementView(Element element) {
            super(element);

        }

        @Override
        protected void attach() {
            Element element = (Element)getNode();
            element.getElementListeners().add(this);

            // Attach child node views
            // TODO We may want to do this in subclasses, since not all
            // components may benefit?
            for (Node node : element) {
                add(createNodeView(node));
            }
        }

        @Override
        protected void detach() {
            Element element = (Element)getNode();
            element.getElementListeners().remove(this);

            // Detach child node views
            remove(0, nodeViews.getLength());
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

        public NodeView getNodeViewAt(int x, int y) {
            NodeView nodeView = null;

            int i = nodeViews.getLength() - 1;
            while (i >= 0) {
                nodeView = nodeViews.get(i);
                Bounds bounds = nodeView.getBounds();
                if (bounds.contains(x, y)) {
                    break;
                }

                i--;
            }

            if (i < 0) {
                nodeView = null;
            }

            return nodeView;
        }

        public void nodeInserted(Element element, int index) {
            // TODO
            invalidate(0, 0);
        }

        public void nodesRemoved(Element element, int index, Sequence<Node> nodes) {
         // TODO
            invalidate(0, 0);
        }

        protected NodeView createNodeView(Node node) {
            // TODO Should this be final?
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

    /**
     * Document view.
     *
     * @author gbrown
     */
    public class DocumentView extends ElementView {
        public DocumentView(Document document) {
            super(document);
        }

        public void layout() {
            // TODO
        }

        @Override
        public void repaint(int x, int y, int width, int height) {
            super.repaint(x, y, width, height);

            // TODO Call repaintComponent()
        }

        @Override
        public void invalidate(int x, int y) {
            super.invalidate(x, y);

            TextAreaSkin.this.invalidateComponent();
        }

        @Override
        public NodeView breakAt(int x) {
            return this;
        }
    }

    public class ParagraphView extends ElementView {
        public ParagraphView(Paragraph paragraph) {
            super(paragraph);
        }

        public void layout() {
            // TODO
        }

        @Override
        public NodeView breakAt(int x) {
            return this;
        }
    }

    /**
     * Text node view.
     *
     * @author gbrown
     */
    public class TextNodeView extends NodeView implements TextNodeListener {
        public TextNodeView(TextNode textNode) {
            super(textNode);

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

        public void paint(Graphics2D graphics) {
            // TODO Auto-generated method stub

        }

        public NodeView breakAt(int x) {
            // TODO
            return null;
        }

        public void charactersInserted(TextNode textNode, int index, int count) {
            // TODO
            invalidate(0, 0);
        }

        public void charactersRemoved(TextNode textNode, int index, String characters) {
            // TODO
            invalidate(0, 0);
        }
    }

    private DocumentView documentView = null;

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
        // TODO
        return 0;
    }

    public int getPreferredHeight(int width) {
        // TODO
        return 0;
    }

    public Dimensions getPreferredSize() {
        Dimensions preferredSize;

        if (documentView == null) {
            preferredSize = new Dimensions(0, 0);
        } else {
            preferredSize = documentView.getSize();
        }

        return preferredSize;
    }

    public void layout() {
        if (documentView != null) {
            documentView.validate();

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
}
