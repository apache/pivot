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
import java.awt.geom.Rectangle2D;
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
 * TODO Add a lineStyle property (solid, dashed, dotted, etc.)? Or support a
 * strokeDashArray property?
 *
 * @author gbrown
 */
public abstract class Shape {
    /**
     * Interface encapsulating an affine transformation.
     *
     * @author gbrown
     */
    public static abstract class Transform {
        private Shape shape = null;

        private Transform() {
        }

        public Shape getShape() {
            return shape;
        }

        private void setShape(Shape shape) {
            this.shape = shape;
        }

        public abstract AffineTransform getAffineTransform();
    }

    /**
     * Represents a rotation transformation.
     *
     * @author gbrown
     */
    public static final class Rotate extends Transform {
        private double angle = 0;
        private AffineTransform affineTransform = null;

        public double getAngle() {
            return angle;
        }

        public void setAngle(double angle) {
            if (this.angle != angle) {
                this.angle = angle;
                affineTransform = null;

                Shape shape = getShape();
                if (shape != null) {
                    shape.invalidate();
                    shape.shapeTransformListeners.transformUpdated(this);
                }
            }
        }

        public AffineTransform getAffineTransform() {
            if (affineTransform == null) {
                double radians = (2 * Math.PI) / 360 * angle;
                affineTransform = AffineTransform.getRotateInstance(radians);
            }

            return affineTransform;
        }
    }

    /**
     * Reprensents a scale transformation.
     *
     * @author gbrown
     */
    public static final class Scale extends Transform {
        private double x = 0;
        private double y = 0;
        private AffineTransform affineTransform = null;

        public double getX() {
            return x;
        }

        public void setX(double x) {
            setScale(x, y);
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            setScale(x, y);
        }

        public void setScale(double x, double y) {
            if (this.x != x
                || this.y != y) {
                this.x = x;
                this.y = y;
                affineTransform = null;

                Shape shape = getShape();
                if (shape != null) {
                    shape.invalidate();
                    shape.shapeTransformListeners.transformUpdated(this);
                }
            }
        }

        public AffineTransform getAffineTransform() {
            if (affineTransform == null) {
                affineTransform = AffineTransform.getScaleInstance(x, y);
            }

            return affineTransform;
        }
    }

    /**
     * Represents a translation transformation.
     *
     * @author gbrown
     */
    public static final class Translate extends Transform {
        private double x = 0;
        private double y = 0;
        private AffineTransform affineTransform = null;

        public double getX() {
            return x;
        }

        public void setX(double x) {
            setTranslation(x, y);
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            setTranslation(x, y);
        }

        public void setTranslation(double x, double y) {
            if (this.x != x
                || this.y != y) {
                this.x = x;
                this.y = y;
                affineTransform = null;

                Shape shape = getShape();
                if (shape != null) {
                    shape.invalidate();
                    shape.shapeTransformListeners.transformUpdated(this);
                }
            }
        }

        public AffineTransform getAffineTransform() {
            if (affineTransform == null) {
                affineTransform = AffineTransform.getTranslateInstance(x, y);
            }

            return affineTransform;
        }
    }

    /**
     * Represents a sequence of affine transformations applied to this shape.
     *
     * @author gbrown
     */
    public class TransformSequence extends Transform
        implements Sequence<Transform>, Iterable<Transform> {
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
            if (transform.getShape() != null) {
                throw new IllegalArgumentException();
            }

            transform.setShape(Shape.this);
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
                for (int i = 0, n = removed.getLength(); i < n; i++) {
                    Transform transform = removed.get(i);
                    transform.setShape(null);
                }

                invalidate();
                affineTransform = null;

                shapeTransformListeners.transformsRemoved(Shape.this, index, removed);
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

        public void visibleChanged(Shape shape) {
            for (ShapeListener listener : this) {
                listener.visibleChanged(shape);
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

        public void transformsRemoved(Shape shape, int index, Sequence<Transform> transforms) {
            for (ShapeTransformListener listener : this) {
                listener.transformsRemoved(shape, index, transforms);
            }
        }

        public void transformUpdated(Shape.Transform transform) {
            for (ShapeTransformListener listener : this) {
                listener.transformUpdated(transform);
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

    private boolean visible = true;

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
     * @param x
     * @param y
     * @param width
     * @param height
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

    public Paint getFill() {
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

    public Paint getStroke() {
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

    public int getStrokeThickness() {
        return strokeThickness;
    }

    public void setStrokeThickness(int strokeThickness) {
        if (strokeThickness < 0) {
            throw new IllegalArgumentException();
        }

        this.strokeThickness = strokeThickness;

        invalidate();
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        if (this.visible != visible) {
            this.visible = visible;
            invalidate();
            shapeListeners.visibleChanged(this);
        }
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

            // Transform the current bounds
            transformedBounds = transform(bounds);

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
        Bounds bounds = transform(x, y, width, height);

        if (parent != null) {
            parent.update(this.x + bounds.x, this.y + bounds.y,
                bounds.width, bounds.height);
        }
    }

    private Bounds transform(Bounds bounds) {
        return transform(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    private Bounds transform(int x, int y, int width, int height) {
        AffineTransform affineTransform = transformSequence.getAffineTransform();

        Rectangle2D boundingRectangle = new Rectangle2D.Double(x, y, width, height);

        java.awt.Shape transformedShape = affineTransform.createTransformedShape(boundingRectangle);
        Rectangle2D transformedBoundingRectangle = transformedShape.getBounds2D();

        Bounds transformedBounds = new Bounds((int)transformedBoundingRectangle.getX(),
            (int)transformedBoundingRectangle.getY(),
            (int)transformedBoundingRectangle.getWidth(),
            (int)transformedBoundingRectangle.getHeight());

        return transformedBounds;
    }

    public ListenerList<ShapeListener> getShapeListeners() {
        return shapeListeners;
    }

    public ListenerList<ShapeTransformListener> getShapeTransformListeners() {
        return shapeTransformListeners;
    }
}
