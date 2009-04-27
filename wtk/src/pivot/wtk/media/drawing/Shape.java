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
package pivot.wtk.media.drawing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.util.Iterator;

import pivot.collections.ArrayList;
import pivot.collections.Sequence;
import pivot.util.ListenerList;
import pivot.wtk.Bounds;
import pivot.wtk.Point;

/**
 * Abstract base class for shapes.
 *
 * @author gbrown
 */
public abstract class Shape {
    /**
     * Represents a sequence of affine transformations applied to this shape.
     * <p>
     * TODO These operations invalidate the shape.
     * <p>
     * TODO Fire ShapeTransform events as appropriate.
     *
     * @author gbrown
     */
    public class TransformSequence
        implements Transform, Sequence<Transform>, Iterable<Transform> {
        public int add(Transform transform) {
            // TODO Auto-generated method stub
            return 0;
        }

        public Transform update(int index, Transform transform) {
            throw new UnsupportedOperationException();
        }

        public void insert(Transform transform, int index) {
            // TODO Auto-generated method stub

        }

        public int remove(Transform transform) {
            // TODO Auto-generated method stub
            return 0;
        }

        public Sequence<Transform> remove(int index, int count) {
            // TODO Auto-generated method stub
            return null;
        }

        public Transform get(int index) {
            return transforms.get(index);
        }

        public int indexOf(Transform transform) {
            return transforms.indexOf(transform);
        }

        public int getLength() {
            return transforms.getLength();
        }

        public AffineTransform getAffineTransform() {
            // TODO Invalidate this when the list is modified and lazily
            // recalculate here when requested

            return null;
        }

        public Iterator<Transform> iterator() {
            // TODO
            return null;
        }
    }

    private class ShapeListenerList extends ListenerList<ShapeListener>
        implements ShapeListener {
        public void originChanged(Shape shape, int previousX, int previousY) {
            for (ShapeListener listener : this) {
                listener.originChanged(shape, previousX, previousY);
            }
        }

        public void strokeChanged(Shape shape, Paint previousStroke) {
            for (ShapeListener listener : this) {
                listener.strokeChanged(shape, previousStroke);
            }
        }

        public void strokeThicknessChanged(Shape shape, int previousStrokeThickness) {
            for (ShapeListener listener : this) {
                listener.strokeThicknessChanged(shape, previousStrokeThickness);
            }
        }

        public void fillChanged(Shape shape, Paint previousFill) {
            for (ShapeListener listener : this) {
                listener.fillChanged(shape, previousFill);
            }
        }
    }

    private Group parent = null;

    private int x = 0;
    private int y = 0;
    private Bounds bounds = new Bounds(0, 0, 0, 0);

    private Paint fill = null;
    private Paint stroke = Color.BLACK;
    private int strokeThickness = 1;

    private ArrayList<Transform> transforms = new ArrayList<Transform>();
    private TransformSequence transformSequence = new TransformSequence();

    private ShapeListenerList shapeListeners = new ShapeListenerList();

    public Group getParent() {
        return parent;
    }

    protected void setParent(Group parent) {
        this.parent = parent;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        setOrigin(x, y);
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        setOrigin(x, y);
    }

    public Point getOrigin() {
        return new Point(x, y);
    }

    public void setOrigin(int x, int y) {
        // Repaint the region formerly occupied by this shape
        update();

        this.x = x;
        this.y = y;

        // Repaint the region currently occupied by this shape
        update();
    }

    public void setOrigin(Point origin) {
        if (origin == null) {
            throw new IllegalArgumentException("origin is null.");
        }

        setOrigin(origin.x, origin.y);
    }

    /**
     * Returns the bounds of the component, including the stroke thickness.
     *
     * @return
     * The component's bounding area. The x and y coordinates are relative to
     * the parent group's origin.
     */
    public abstract Bounds getBounds();

    public Bounds getTransformedBounds() {
        // TODO Apply transform to bounds and return
        return null;
    }

    /**
     * Determines if the shape contains the given point.
     *
     * @param x
     * The x-coordinate of the point to test, in untransformed shape
     * coordinates.
     *
     * @param y
     * The y-coordinate of the point to test, in untransformed shape
     * coordinates.
     *
     * @return
     * <tt>true</tt> if the shape contains the point; <tt>false</tt>, otherwise.
     */
    public abstract boolean contains(int x, int y);

    public Paint getFill() {
        Paint fill = this.fill;
        if (fill == null) {
            if (fill == null) {
                throw new IllegalStateException();
            }

            fill = parent.getFill();
        }

        return fill;
    }

    public void setFill(Paint fill) {
        this.fill = fill;
        update();
    }

    public void setFill(String fill) {
        if (fill == null) {
            throw new IllegalArgumentException("fill is null.");
        }

        setFill(Color.decode(fill));
    }

    public Paint getStroke() {
        Paint stroke = this.stroke;
        if (stroke == null) {
            if (parent == null) {
                throw new IllegalStateException();
            }

            stroke = parent.getStroke();
        }

        return stroke;
    }

    public void setStroke(Paint stroke) {
        this.stroke = stroke;
        update();
    }

    public void setStroke(String stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("stroke is null.");
        }

        setStroke(Color.decode(stroke));
    }

    public int getStrokeThickness() {
        int strokeThickness = this.strokeThickness;
        if (strokeThickness == -1) {
            if (parent == null) {
                throw new IllegalStateException();
            }

            strokeThickness = parent.getStrokeThickness();
        }

        return strokeThickness;
    }

    public void setStrokeThickness(int strokeThickness) {
        if (strokeThickness < -1) {
            throw new IllegalArgumentException();
        }

        this.strokeThickness = strokeThickness;

        invalidate();
    }

    public abstract void draw(Graphics2D graphics);

    public TransformSequence getTransforms() {
        return transformSequence;
    }

    protected void invalidate() {
        // TODO Clear transformedBounds; recalculate in getTransformedBounds()
        // TODO If parent is non-null, propagate upwards
    }

    protected void validate() {
        // TODO Recalculate transformed bounds
    }

    protected final void update() {
        update(bounds);
    }

    protected final void update(Bounds bounds) {
        update(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    protected void update(int x, int y, int width, int height) {
        // TODO Transform region bounds

        if (parent != null) {
            parent.update(this.x + x, this.y + y, width, height);
        }
    }

    public ListenerList<ShapeListener> getShapeListeners() {
        return shapeListeners;
    }
}
