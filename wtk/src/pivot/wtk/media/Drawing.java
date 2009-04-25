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
package pivot.wtk.media;

import java.awt.Graphics2D;

import pivot.util.ListenerList;
import pivot.wtk.Bounds;
import pivot.wtk.media.drawing.Group;
import pivot.wtk.media.drawing.Shape;
import pivot.wtk.media.drawing.ShapeListener;

/**
 * Image representing a vector drawing.
 *
 * @author gbrown
 */
public class Drawing extends Image {
    private static class DrawingListenerList extends ListenerList<DrawingListener>
        implements DrawingListener {
        public void rootChanged(Drawing drawing, Group previousRoot) {
            for (DrawingListener listener : this) {
                listener.rootChanged(drawing, previousRoot);
            }
        }
    }

    private Group root;
    private int width = 0;
    private int height = 0;

    private ShapeListener rootListener = new ShapeListener.Adapter() {
        @Override
        public void regionInvalidated(Shape shape, int x, int y, int width, int height) {
            Bounds bounds = new Bounds(0, 0, Drawing.this.width, Drawing.this.height);
            bounds = bounds.intersect(new Bounds(x, y, width, height));
            imageListeners.regionInvalidated(Drawing.this, x, y, width, height);
        }
    };

    private DrawingListenerList drawingListeners = new DrawingListenerList();

    public Drawing() {
        this(new Group());
    }

    public Drawing(Group root) {
        setRoot(root);
    }

    public Group getRoot() {
        return root;
    }

    public void setRoot(Group root) {
        Group previousRoot = this.root;

        if (previousRoot != root) {
            this.root = root;

            if (previousRoot != null) {
                previousRoot.getShapeListeners().remove(rootListener);
            }

            if (root != null) {
                root.getShapeListeners().add(rootListener);
            }

            drawingListeners.rootChanged(this, previousRoot);

            imageListeners.regionInvalidated(this, 0, 0, width, height);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setSize(int width, int height) {
        int previousWidth = this.width;
        int previousHeight = this.height;

        this.width = width;
        this.height = height;

        imageListeners.sizeChanged(this, previousWidth, previousHeight);
    }

    public void paint(Graphics2D graphics) {
        graphics.clipRect(0, 0, width, height);

        if (root != null) {
            // TODO Apply root transforms
            root.draw(graphics);
        }
    }

    public ListenerList<DrawingListener> getDrawingListeners() {
        return drawingListeners;
    }
}
