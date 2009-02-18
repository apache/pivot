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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.font.TextMeasurer;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.BreakIterator;
import java.util.Comparator;
import java.util.Iterator;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.Sequence;
import pivot.util.ImmutableIterator;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Bounds;
import pivot.wtk.Component;
import pivot.wtk.Container;
import pivot.wtk.Cursor;
import pivot.wtk.Dimensions;
import pivot.wtk.Insets;
import pivot.wtk.Mouse;
import pivot.wtk.Platform;
import pivot.wtk.Point;
import pivot.wtk.TextArea;
import pivot.wtk.TextAreaListener;
import pivot.wtk.TextAreaSelectionListener;
import pivot.wtk.Theme;
import pivot.wtk.Visual;
import pivot.wtk.media.Image;
import pivot.wtk.text.Document;
import pivot.wtk.text.Element;
import pivot.wtk.text.ElementListener;
import pivot.wtk.text.ImageNode;
import pivot.wtk.text.ImageNodeListener;
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
public class TextAreaSkin extends ComponentSkin
    implements TextAreaListener, TextAreaSelectionListener {
    /**
     * Abstract base class for node views.
     *
     * @author gbrown
     */
    public abstract class NodeView implements Visual, NodeListener {
        private Node node = null;
        private ElementView parent = null;

        private int width = 0;
        private int height = 0;
        private int x = 0;
        private int y = 0;

        private int breakWidth = 0;

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
            return getHeight(true);
        }

        public int getHeight(boolean validate) {
            if (validate) {
                validate();
            }

            return height;
        }

        public Dimensions getSize() {
            return getSize(true);
        }

        public Dimensions getSize(boolean validate) {
            if (validate) {
                validate();
            }

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
            Rectangle clipBounds = graphics.getClipBounds();
            Bounds paintBounds = (clipBounds == null) ?
                new Bounds(0, 0, getWidth(), getHeight()) : new Bounds(clipBounds);

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

        public int getIndexAt(int x, int y) {
            nullNodeView.setLocation(x, y);

            int index = Sequence.Search.binarySearch(nodeViews, nullNodeView,
                nodeViewLocationComparator);

            if (index < 0) {
                index = -(index + 1) - 1;
            }

            return index;
        }

        public NodeView getNodeViewAt(int x, int y) {
            return get(getIndexAt(x, y));
        }

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
        private class ValidateCallback implements Runnable {
            private int index;

            public ValidateCallback(int index) {
                this.index = index;
            }

            public void run() {
                if (index != -1) {
                    NodeView nodeView = get(index++);
                    nodeView.validate();

                    if (index < getLength()) {
                        ApplicationContext.setTimeout(this, 0);
                    } else {
                        validateCallback = null;
                        invalidateComponent();
                    }
                }
            }

            public void abort() {
                index = -1;
            }
        }

        private ValidateCallback validateCallback = null;

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
                TextArea textArea = (TextArea)getComponent();
                Container parent = textArea.getParent();

                int breakWidth = getBreakWidth();

                int width = 0;
                int y = 0;

                int top = -textArea.getY();
                int bottom = top + parent.getHeight();

                int i = 0;
                int j = 0;
                int n = getLength();
                int start = -1;

                while (i < n) {
                    NodeView nodeView = get(i++);
                    nodeView.setBreakWidth(breakWidth);

                    // TODO Make this configurable; e.g. validateVisibleContentOnly = false
                    int height = nodeView.getHeight(false);
                    if (y + height >= top) {
                        if (y < bottom) {
                            nodeView.validate();
                        } else {
                            if (start == -1) {
                                start = i - 1;
                            }
                        }
                    }

                    nodeView.setLocation(0, y);

                    if (nodeView.isValid()) {
                        width = Math.max(width, nodeView.getWidth());
                        height = nodeView.getHeight();
                        j++;
                    }

                    y += height;
                }

                setSize(width, y);

                if (i == j) {
                    super.validate();
                } else {
                    if (start != -1) {
                        if (validateCallback != null) {
                            validateCallback.abort();
                        }

                        validateCallback = new ValidateCallback(start);
                        ApplicationContext.setTimeout(validateCallback, 0);
                    }
                }
            }
        }

        @Override
        public NodeView getNext() {
            return null;
        }

        public void nodeInserted(Element element, int index) {
            super.nodeInserted(element, index);

            Document document = (Document)getNode();
            insert(createNodeView(document.get(index)), index);
        }

        public void nodesRemoved(Element element, int index, Sequence<Node> nodes) {
            remove(index, nodes.getLength());

            super.nodesRemoved(element, index, nodes);
        }
    }

    public class ParagraphView extends ElementView {
        public ParagraphView(Paragraph paragraph) {
            super(paragraph);
        }

        @Override
        public void validate() {
            if (!isValid()) {
                // Build the row list
                Paragraph paragraph = (Paragraph)getNode();
                ArrayList<ArrayList<NodeView>> rows = new ArrayList<ArrayList<NodeView>>();

                // Break the views into multiple rows
                int breakWidth = getBreakWidth();

                ArrayList<NodeView> row = new ArrayList<NodeView>();
                int rowWidth = 0;

                for (Node node : paragraph) {
                    NodeView nodeView = createNodeView(node);

                    nodeView.setBreakWidth(breakWidth - rowWidth);
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
                int y = 0;

                for (int i = 0, n = rows.getLength(); i < n; i++) {
                    row = rows.get(i);

                    int x = 0;
                    int rowHeight = 0;

                    for (NodeView nodeView : row) {
                        nodeView.setLocation(x, y);
                        x += nodeView.getWidth();
                        rowHeight = Math.max(rowHeight, nodeView.getHeight());

                        add(nodeView);
                    }

                    width = Math.max(width, x);
                    y += rowHeight;
                }

                // TODO Don't hard-code padding; use the value specified
                // by the Paragraph
                setSize(width, y + 2);
            }

            super.validate();
        }

        @Override
        public NodeView getNext() {
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
        private int length = 0;

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
            super.invalidate();
        }

        @Override
        public void validate() {
            if (!isValid()) {
                TextNode textNode = (TextNode)getNode();
                String text = textNode.getText();

                // Update the length value
                length = text.length() - start;

                // Calculate the size of the text
                int width;
                int height;

                if (length == 0) {
                    // Return a width of 0 and the font height
                    width = 0;

                    LineMetrics lm = font.getLineMetrics("", start, start + length,
                        fontRenderContext);
                    height = (int)Math.ceil(lm.getAscent() + lm.getDescent()
                        + lm.getLeading());
                } else {
                    // TODO Use a custom iterator so we don't have to copy the string
                    AttributedString attributedText = new AttributedString(text);
                    attributedText.addAttribute(TextAttribute.FONT, font);

                    AttributedCharacterIterator aci = attributedText.getIterator();

                    int breakWidth = getBreakWidth();

                    // Attempt to break the text
                    TextMeasurer textMeasurer = new TextMeasurer(aci, fontRenderContext);

                    // Get the break index
                    int lineBreakIndex = textMeasurer.getLineBreakIndex(start, breakWidth);

                    int end;
                    if (lineBreakIndex < text.length()) {
                        BreakIterator breakIterator = BreakIterator.getLineInstance();
                        breakIterator.setText(aci);

                        char c = text.charAt(lineBreakIndex);
                        if (Character.isWhitespace(c)) {
                            end = breakIterator.following(lineBreakIndex);
                        } else {
                            // Move back to the previous break
                            end = breakIterator.preceding(lineBreakIndex);

                            if (end <= start) {
                                // The whole word doesn't fit in the given space
                                if (breakOnWhitespaceOnly) {
                                    // Move forward to the next break
                                    end = breakIterator.following(start);
                                } else {
                                    // Force a break at this index
                                    end = lineBreakIndex;
                                }
                            }
                        }

                        if (end == BreakIterator.DONE) {
                            end = text.length();
                        }
                    } else {
                        end = text.length();
                    }

                    // Calculate the string bounds
                    Rectangle2D stringBounds = font.getStringBounds(aci,
                        start, end, fontRenderContext);
                    width = (int)Math.ceil(stringBounds.getWidth());
                    height = (int)Math.ceil(stringBounds.getHeight());

                    // Create a new node containing the remainder of the text
                    if (end < text.length()) {
                        length = end - start;
                        next = new TextNodeView(textNode, end);
                    }
                }

                setSize(width, height);
            }

            super.validate();
        }

        public void paint(Graphics2D graphics) {
            TextNode textNode = (TextNode)getNode();
            String text = textNode.getText();

            // TODO Use a custom iterator so we don't have to copy the string
            text = text.substring(start, start + length);

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

        public void imageChanged(ImageNode imageNode, Image previousImage) {
            invalidate();
        }
    }

    /**
     * Null node view, used for binary searches.
     *
     * @author gbrown
     */
    private class NullNodeView extends NodeView {

        public NullNodeView() {
            super(null);
        }

        @Override
        public NodeView getNext() {
            return null;
        }

        public void paint(Graphics2D graphics) {
            // No-op
        }
    }

    /**
     * Comparator used to perform binary searches on node views.
     *
     * @author gbrown
     */
    private class NodeViewLocationComparator implements Comparator<NodeView> {
        public int compare(NodeView nodeView1, NodeView nodeView2) {
            int width = getWidth();

            int x1 = nodeView1.getX();
            int y1 = nodeView1.getY();

            int x2 = nodeView2.getX();
            int y2 = nodeView2.getY();

            return (y1 * width + x1) - (y2 * width + x2);
        }
    }

    private class BlinkCursorCallback implements Runnable {
        public void run() {
            caretOn = !caretOn;

            TextArea textArea = (TextArea)getComponent();
            textArea.repaint(caret.x, caret.y, caret.width, caret.height, true);
        }
    }

    private DocumentView documentView = null;

    private FontRenderContext fontRenderContext = new FontRenderContext(null, true, true);

    private Rectangle caret = new Rectangle();
    private boolean caretOn = true;
    private BlinkCursorCallback blinkCursorCallback = new BlinkCursorCallback();
    private int blinkCursorIntervalID = -1;

    private Font font;
    private Insets margin = new Insets(4);
    private boolean breakOnWhitespaceOnly = false;

    private NullNodeView nullNodeView = new NullNodeView();
    private NodeViewLocationComparator nodeViewLocationComparator = new NodeViewLocationComparator();

    public TextAreaSkin() {
        Theme theme = Theme.getTheme();
        font = theme.getFont();
    }

    public void install(Component component) {
        super.install(component);

        TextArea textArea = (TextArea)component;
        textArea.getTextAreaListeners().add(this);
        textArea.getTextAreaSelectionListeners().add(this);

        textArea.setCursor(Cursor.TEXT);

        Document document = textArea.getDocument();
        if (document != null) {
            documentView = new DocumentView(document);
            documentView.attach();
        }

        selectionChanged(textArea, 0, 0);
    }

    public void uninstall() {
        TextArea textArea = (TextArea)getComponent();
        textArea.getTextAreaListeners().remove(this);
        textArea.getTextAreaSelectionListeners().remove(this);

        textArea.setCursor(Cursor.DEFAULT);

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
            documentView.setBreakWidth(Integer.MAX_VALUE);
            preferredWidth = documentView.getWidth() + margin.left + margin.right;
        }

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight;
        if (documentView == null) {
            preferredHeight = 0;
        } else {
            documentView.setBreakWidth((width == -1) ?
                Integer.MAX_VALUE : Math.max(width - (margin.left + margin.right), 0));
            preferredHeight = documentView.getHeight() + margin.top + margin.bottom;
        }

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        Dimensions preferredSize;

        if (documentView == null) {
            preferredSize = new Dimensions(0, 0);
        } else {
            documentView.setBreakWidth(Integer.MAX_VALUE);
            preferredSize = documentView.getSize();
            preferredSize.width += margin.left + margin.right;
            preferredSize.height += margin.top + margin.bottom;
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
            TextArea textArea = (TextArea)getComponent();

            // TODO Paint selection background

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

    private void showCaret(boolean show) {
        if (show) {
            if (blinkCursorIntervalID == -1) {
                blinkCursorIntervalID = ApplicationContext.setInterval(blinkCursorCallback,
                    Platform.getCursorBlinkRate());

                // Run the callback once now to show the cursor immediately
                blinkCursorCallback.run();
            }
        } else {
            if (blinkCursorIntervalID != -1) {
                ApplicationContext.clearInterval(blinkCursorIntervalID);
                blinkCursorIntervalID = -1;
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

        setFont(Font.decode(font));
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

    public boolean isBreakOnWhitespaceOnly() {
        return breakOnWhitespaceOnly;
    }

    public void setBreakOnWhitespaceOnly(boolean breakOnWhitespaceOnly) {
        this.breakOnWhitespaceOnly = breakOnWhitespaceOnly;
        invalidateComponent();
    }

    @Override
    public void locationChanged(Component component, int previousX, int previousY) {
        super.locationChanged(component, previousX, previousY);

        if (documentView != null
            && !documentView.isValid()
            && component.getY() > previousY) {
            invalidateComponent();
        }
    }

    @Override
    public void focusedChanged(Component component, boolean temporary) {
        super.focusedChanged(component, temporary);

        TextArea textArea = (TextArea)getComponent();
        showCaret(textArea.isFocused()
            && textArea.getSelectionLength() == 0);

        repaintComponent();
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        if (button == Mouse.Button.LEFT) {
            TextArea textArea = (TextArea)component;

            // TODO Move the caret to the insertion point

            // TODO Register mouse listener to begin selecting text

            // Set focus to the text input
            textArea.requestFocus();
        }

        return super.mouseDown(component, button, x, y);
    }

    public void documentChanged(TextArea textArea, Document previousDocument) {
        if (documentView != null) {
            documentView.detach();
            documentView = null;
        }

        Document document = textArea.getDocument();
        if (document != null) {
            documentView = new DocumentView(document);
            documentView.attach();
        }

        invalidateComponent();
    }

    public void selectionChanged(TextArea textArea, int previousSelectionStart,
        int previousSelectionLength) {
        // TODO Determine the caret shape/bounds
        caret.x = 0;
        caret.y = 0;
        caret.width = 1;
        caret.height = 14;

        showCaret(textArea.isFocused()
            && textArea.getSelectionLength() == 0);

        repaintComponent();
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
