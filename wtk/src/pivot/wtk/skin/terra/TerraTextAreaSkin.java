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
package pivot.wtk.skin.terra;

import java.awt.Graphics2D;

import pivot.collections.ArrayList;
import pivot.wtk.Bounds;
import pivot.wtk.Component;
import pivot.wtk.ConstrainedVisual;
import pivot.wtk.Dimensions;
import pivot.wtk.skin.ContainerSkin;
import pivot.wtk.text.Element;
import pivot.wtk.text.Node;
import pivot.wtk.text.NodeListener;

/**
 * Terra text area skin.
 *
 * @author gbrown
 */
public class TerraTextAreaSkin extends ContainerSkin {
    // TODO Should these inner classes be static? Should they live in
    // pivot.wtk.skin.text?

    public abstract class NodeView implements ConstrainedVisual, NodeListener {
        private ElementView parent = null;

        private int x = 0;
        private int y = 0;

        private int width = 0;
        private int height = 0;

        public ElementView getParent() {
            return parent;
        }

        protected void setParent(ElementView parent) {
            this.parent = parent;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public void setSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public Bounds getBounds() {
            return new Bounds(x, y, width, height);
        }

        public boolean isValid() {
            return true;
        }

        public void invalidate() {
            if (parent != null) {
                parent.invalidate();
            }
        }

        public void validate() {
            if (width > 0
                && height > 0) {
                layout();
            }
        }

        // TODO Move these to inner class
        public void parentChanged(Node node, Element previousParent) {
            // TODO?
        }

        public void offsetChanged(Node node, int previousOffset) {
            // TODO?
        }

        public void rangeInserted(Node node, Node range, int offset) {
            // TODO?
        }

        public void rangeRemoved(Node node, int offset, Node range) {
            // TODO?
        }
    }

    public abstract class ElementView extends NodeView {
        ArrayList<NodeView> nodeViews = new ArrayList<NodeView>();
        private boolean valid = true;

        public NodeView get(int index) {
            return nodeViews.get(index);
        }

        public int getLength() {
            return nodeViews.getLength();
        }

        @Override
        public boolean isValid() {
            return valid;
        }

        @Override
        public void invalidate() {
            if (valid) {
                valid = false;
                super.invalidate();
            }
        }

        @Override
        public void validate() {
            if (!valid) {
                try {
                    super.validate();

                    for (int i = 0, n = nodeViews.getLength(); i < n; i++) {
                        NodeView nodeView = nodeViews.get(i);
                        nodeView.validate();
                    }
                } finally {
                    valid = true;
                }
            }
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
    }

    public void install(Component component) {
        super.install(component);

        // TODO
    }

    public void uninstall() {
        // TODO

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
        // TODO
        return null;
    }

    public void layout() {
        // TODO
    }

    public void paint(Graphics2D graphics) {
        // TODO
    }

    @Override
    public boolean isFocusable() {
        // TODO Update Container#requestFocus() to only transfer focus to
        // first subcomponent if the container itself is not focusable
        return true;
    }
}
