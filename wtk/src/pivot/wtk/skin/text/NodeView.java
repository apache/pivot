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
package pivot.wtk.skin.text;

import pivot.wtk.Bounds;
import pivot.wtk.ConstrainedVisual;
import pivot.wtk.text.Element;
import pivot.wtk.text.Node;
import pivot.wtk.text.NodeListener;

/**
 * Abstract base class for node views.
 *
 * @author gbrown
 */
public abstract class NodeView implements ConstrainedVisual, NodeListener {
    private ElementView parent = null;
    private Node node = null;

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

    public void attach(Node node) {
        node.getNodeListeners().add(this);

        this.node = node;
    }

    public void detach() {
        node.getNodeListeners().remove(this);

        node = null;
    }

    public Node getNode() {
        return node;
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
        assert(width >= 0);
        assert(height >= 0);

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

    public void layout() {
        // No-op
    }

    public abstract NodeView breakAt(int x);

    public void parentChanged(Node node, Element previousParent) {
        // No-op
    }

    public void offsetChanged(Node node, int previousOffset) {
        // No-op
    }
}
