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
package org.apache.pivot.wtk.media.drawing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Point;

/**
 * Abstract base class for shapes.
 * <p>
 * TODO Add a strokeDashArray property.
 */
public abstract class Shape {
    /**
     * Interface encapsulating an affine transformation.
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
     */
    public static final class Rotate extends Transform {
        private double angle;
        private double anchorX;
        private double anchorY;
        private AffineTransform affineTransform = null;

        public Rotate() {
            this(0, 0, 0);
        }

        public Rotate(double angle, double anchorX, double anchorY) {
            this.angle = angle;
            this.anchorX = anchorX;
            this.anchorY = anchorY;
        }

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
                    shape.transformSequence.affineTransform = null;
                    shape.shapeTransformListeners.transformUpdated(this);
                }
            }
        }

        public double getAnchorX() {
            return anchorX;
        }

        public void setAnchorX(double anchorX) {
            setAnchor(anchorX, anchorY);
        }

        public double getAnchorY() {
            return anchorY;
        }

        public void setAnchorY(double anchorY) {
            setAnchor(anchorX, anchorY);
        }

        public void setAnchor(double anchorX, double anchorY) {
            if (this.anchorX != anchorX
                || this.anchorY != anchorY) {
                this.anchorX = anchorX;
                this.anchorY = anchorY;
                affineTransform = null;

                Shape shape = getShape();
                if (shape != null) {
                    shape.invalidate();
                    shape.transformSequence.affineTransform = null;
                    shape.shapeTransformListeners.transformUpdated(this);
                }
            }
        }

        @Override
        public AffineTransform getAffineTransform() {
            if (affineTransform == null) {
                double radians = (2 * Math.PI) / 360 * angle;
                affineTransform = AffineTransform.getRotateInstance(radians, anchorX, anchorY);
            }

            return affineTransform;
        }
    }

    /**
     * Represents a scale transformation.
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
                    shape.transformSequence.affineTransform = null;
                    shape.shapeTransformListeners.transformUpdated(this);
                }
            }
        }

        @Override
        public AffineTransform getAffineTransform() {
            if (affineTransform == null) {
                affineTransform = AffineTransform.getScaleInstance(x, y);
            }

            return affineTransform;
        }
    }

    /**
     * Represents a translation transformation.
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
                    shape.transformSequence.affineTransform = null;
                    shape.shapeTransformListeners.transformUpdated(this);
                }
            }
        }

        @Override
        public AffineTransform getAffineTransform() {
            if (affineTransform == null) {
                affineTransform = AffineTransform.getTranslateInstance(x, y);
            }

            return affineTransform;
        }
    }

    /**
     * Represents a sequence of affine transformations applied to this shape.
     */
    public class TransformSequence extends Transform
        implements Sequence<Transform>, Iterable<Transform> {
        private AffineTransform affineTransform = null;

        public int add(Transform transform) {
            int index = getLength();
            insert(transform, index);

            return index;
        }

        public Transform update(int index, Transform transform) {
            throw new UnsupportedOperationException();
        }

        public void insert(Transform transform, int index) {
            if (transform == null
                || transform.getShape() != null) {
                throw new IllegalArgumentException();
            }

            transform.setShape(Shape.this);
            transforms.insert(transform, index);

            invalidate();
            affineTransform = null;

            shapeTransformListeners.transformInserted(Shape.this, index);
        }

        public int remove(Transform transform) {
            int index = indexOf(transform);
            if (index != -1) {
                remove(index, 1);
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

        @Override
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
    private Bounds bounds = null;
    private Bounds transformedBounds = new Bounds(0, 0, 0, 0);

    private Paint fill = null;
    private Paint stroke = Color.BLACK;
    private int strokeThickness = 1;

    private boolean visible = true;

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
        int previousX = this.x;
        int previousY = this.y;
        if (previousX != x
            || previousY != y) {
            // Repaint the region formerly occupied by this shape
            update();

            this.x = x;
            this.y = y;

            // Repaint the region currently occupied by this shape
            update();

            shapeListeners.originChanged(this, previousX, previousY);
        }
    }

    public final void setOrigin(Point origin) {
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
     * y coordinates are relative to the shape's origin.
     */
    public Bounds getBounds() {
        validate();
        return bounds;
    }

    /**
     * Sets the bounds of the shape. The x and y coordinates are relative to the
     * shape's origin.
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    protected void setBounds(int x, int y, int width, int height) {
        // Repaint the region formerly occupied by this shape
        update();

        bounds = new Bounds(x, y, width, height);
        transformedBounds = transform(x, y, width, height);

        // Repaint the region currently occupied by this shape
        update();
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

    public abstract boolean contains(int x, int y);

    public Paint getFill() {
        return fill;
    }

    public void setFill(Paint fill) {
        Paint previousFill = this.fill;
        if (previousFill != fill) {
            this.fill = fill;
            update();
            shapeListeners.fillChanged(this, previousFill);
        }
    }

    public final void setFill(String fill) {
        if (fill == null) {
            throw new IllegalArgumentException("fill is null.");
        }

        setFill(fill.length() == 0 ? null : GraphicsUtilities.decodePaint(fill));
    }

    public Paint getStroke() {
        return stroke;
    }

    public void setStroke(Paint stroke) {
        Paint previousStroke = this.stroke;
        if (previousStroke != stroke) {
            this.stroke = stroke;
            update();
            shapeListeners.strokeChanged(this, previousStroke);
        }
    }

    public final void setStroke(String stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("stroke is null.");
        }

        setStroke(stroke.length() == 0 ? null : GraphicsUtilities.decodePaint(stroke));
    }

    public int getStrokeThickness() {
        return strokeThickness;
    }

    public void setStrokeThickness(int strokeThickness) {
        if (strokeThickness < 0) {
            throw new IllegalArgumentException();
        }

        int previousStrokeThickness = this.strokeThickness;
        if (previousStrokeThickness != strokeThickness) {
            this.strokeThickness = strokeThickness;
            invalidate();
            shapeListeners.strokeThicknessChanged(this, previousStrokeThickness);
        }
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
        if (bounds != null) {
            bounds = null;

            if (parent != null) {
                parent.invalidate();
            }
        }
    }

    protected abstract void validate();

    protected boolean isValid() {
        return (bounds != null);
    }

    protected final void update() {
        update(transformedBounds);
    }

    protected final void update(Bounds bounds) {
        update(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    protected void update(int x, int y, int width, int height) {
        Bounds transformedBounds = transform(x, y, width, height);

        if (parent != null) {
            parent.update(transformedBounds.x + x, transformedBounds.y + y,
                transformedBounds.width, transformedBounds.height);
        }
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
