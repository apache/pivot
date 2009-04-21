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
import pivot.wtk.Bounds;
import pivot.wtk.media.drawing.Group;
import pivot.wtk.media.drawing.Shape;
import pivot.wtk.media.drawing.ShapeListener;

/**
 * Image representing a vector drawing.
 * <p>
 * TODO If developer retains a reference to the root group but not the drawing,
 * the drawing will never be garbage collected. Do we need a way to decouple
 * it?
 *
 * @author gbrown
 */
public class Drawing extends Image {
    private Group root;
    private ShapeListener rootListener = new ShapeListener.Adapter() {
        @Override
        public void boundsChanged(Shape shape, int previousX, int previousY,
            int previousWidth, int previousHeight) {
            // TODO Need to clip this to 0, 0
            imageListeners.sizeChanged(Drawing.this, previousWidth, previousHeight);
        }

        public void regionInvalidated(Shape shape, int x, int y, int width, int height) {
            // TODO Need to clip this to 0, 0
            imageListeners.regionInvalidated(Drawing.this, x, y, width, height);
        }
    };

    public Drawing() {
        this(new Group());
    }

    public Drawing(Group root) {
        this.root = root;

        root.getShapeListeners().add(rootListener);
    }

    public Group getRoot() {
        return root;
    }

    public int getWidth() {
        // TODO Need to clip this to 0, 0
        Bounds bounds = root.getBounds();
        return bounds.width + bounds.x;
    }

    public int getHeight() {
        // TODO Need to clip this to 0, 0
        Bounds bounds = root.getBounds();
        return bounds.height + bounds.y;
    }

    public void paint(Graphics2D graphics) {
        graphics.clipRect(0, 0, getWidth(), getHeight());
        root.paint(graphics);
    }
}
