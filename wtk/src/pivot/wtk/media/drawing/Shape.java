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
import pivot.util.ImmutableIterator;
import pivot.util.ListenerList;
import pivot.wtk.Bounds;
import pivot.wtk.Point;

/**
 * Abstract base class for shapes.
 * <p>
 * TODO Add a visible property.
 * <p>
 * TODO Add a lineStyle property (solid, dashed, dotted, etc.)? Or support a
 * strokeDashArray property?
 *
 * @author gbrown
 */
public abstract class Shape {
    /**
     * Represents a sequence of affine transformations applied to this shape.
     *
     * @author gbrown
     */
    public class TransformSequence
        implements Transform, Sequence<Transform>, Iterable<Transform> {
        private AffineTransform affineTransform = null;

        public int add(Transform transform) {
            int index = transforms.getLength();
            transforms.insert(transform, index);

            return index;
        }

        public Transform update(int index, Transform transform) {
            throw new UnsupportedOperationException();
        }

        public void insert(Transform transform, int index) {
            transforms.insert(transform, index);
            invalidate();
            affineTransform = null;
            shapeTransformListeners.transformInserted(Shape.this, index);
        }

        public int remove(Transform transform) {
            int index = transforms.indexOf(transform);
            if (index != -1) {
                transforms.remove(index, 1);
            }

            return index;
        }

        public Sequence<Transform> remove(int index, int count) {
            Sequence<Transform> removed = transforms.remove(index, count);
            if (removed.getLength() > 0) {
                invalidate();
                affineTransform = null;
                shapeTransformListeners.transformsRemoved(Shape.this, index, count);
            }

            return removed;
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
            if (affineTransform == null) {
                affineTransform = new AffineTransform();
                for (Transform transform : this) {
                    affineTransform.concatenate(transform.getAffineTransform());
                }
            }

            return affineTransform;
        }

        public Iterator<Transform> iterator() {
            return new ImmutableIterator<Transform>(transforms.iterator());
        }
    }

    private static class ShapeListenerList extends ListenerList<ShapeListener>
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

    private static class ShapeTransformListenerList extends ListenerList<ShapeTransformListener>
        implements ShapeTransformListener {
        public void transformInserted(Shape shape, int index) {
            for (ShapeTransformListener listener : this) {
                listener.transformInserted(shape, index);
            }
        }

        public void transformsRemoved(Shape shape, int index, int count) {
            for (ShapeTransformListener listener : this) {
                listener.transformsRemoved(shape, index, count);
            }
        }
    }

    private Group parent = null;

    private int x = 0;
    private int y = 0;
    private Bounds bounds = new Bounds(0, 0, 0, 0);
    private Bounds transformedBounds = new Bounds(0, 0, 0, 0);

    private Paint fill = null;
    private Paint stroke = Color.BLACK;
    private int strokeThickness = 1;

    private boolean valid = true;

    private ArrayList<Transform> transforms = new ArrayList<Transform>();
    private TransformSequence transformSequence = new TransformSequence();

    private ShapeListenerList shapeListeners = new ShapeListenerList();
    private ShapeTransformListenerList shapeTransformListeners = new ShapeTransformListenerList();

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
     * Returns the bounds of the shape.
     *
     * @return
     * The component's bounding area, including the stroke thickness. The x and
     * y coordinates are relative to the parent group's origin.
     */
    public Bounds getBounds() {
        return bounds;
    }

    /**
     * Sets the bounds of the shape.
     *
     * @param bounds
     * The component's bounding area, including the stroke thickness. The x and
     * y coordinates are relative to the parent group's origin.
     */
    protected void setBounds(int x, int y, int width, int height) {
        bounds = new Bounds(x, y, width, height);
    }

    /**
     * Returns the transformed bounds of the shape.
     *
     * @return
     * The resulting bounds after all transforms have been applied.
     */
    public Bounds getTransformedBounds() {
        validate();
        return transformedBounds;
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
            if (parent == null) {
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

    public final void setFill(String fill) {
        if (fill == null) {
            throw new IllegalArgumentException("fill is null.");
        }

        setFill(Color.decode(fill));
    }

    public boolean isFillSet() {
        return (fill != null);
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

    public final void setStroke(String stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("stroke is null.");
        }

        setStroke(Color.decode(stroke));
    }

    public boolean isStrokeSet() {
        return (stroke != null);
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

    public boolean isStrokeThicknessSet() {
        return (strokeThickness != -1);
    }

    public abstract void draw(Graphics2D graphics);

    public TransformSequence getTransforms() {
        return transformSequence;
    }

    protected void invalidate() {
        if (valid) {
            valid = false;

            if (parent != null) {
                parent.invalidate();
            }
        }
    }

    protected void validate() {
        if (!valid) {
            // Repaint the region formerly occupied by this shape
            update(transformedBounds);

            // TODO Apply transforms to transformedBounds
            transformedBounds = new Bounds(bounds);

            // Repaint the region currently occupied by this shape
            update(transformedBounds);

            valid = true;
        }
    }

    protected boolean isValid() {
        return valid;
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

    public ListenerList<ShapeTransformListener> getShapeTransformListeners() {
        return shapeTransformListeners;
    }
}
