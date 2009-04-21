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
import java.awt.Paint;
import java.awt.geom.AffineTransform;

import pivot.util.ListenerList;
import pivot.wtk.Bounds;
import pivot.wtk.Point;
import pivot.wtk.Visual;

/**
 * Abstract base class for shapes.
 *
 * @author gbrown
 */
public abstract class Shape implements Visual {
    private class ShapeListenerList extends ListenerList<ShapeListener>
        implements ShapeListener {
        public void originChanged(Shape shape, int previousX, int previousY) {
            for (ShapeListener listener : this) {
                listener.originChanged(shape, previousX, previousY);
            }
        }

        public void boundsChanged(Shape shape, int previousX, int previousY,
            int previousWidth, int previousHeight) {
            for (ShapeListener listener : this) {
                listener.boundsChanged(shape, previousX, previousY, previousWidth, previousHeight);
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

        public void regionInvalidated(Shape shape, int x, int y, int width, int height) {
            for (ShapeListener listener : this) {
                listener.regionInvalidated(shape, x, y, width, height);
            }
        }
    }

    private Group parent = null;

    private int x = 0;
    private int y = 0;
    private int width = 0;
    private int height = 0;

    private Paint fill = null;
    private Paint stroke = Color.BLACK;
    private int strokeThickness = 1;

    private double rotation = 0;
    private double scaleX = 0;
    private double scaleY = 0;
    private double translateX = 0;
    private double translateY = 0;

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
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Point getOrigin() {
        return new Point(x, y);
    }

    public void setOrigin(int x, int y) {
        setX(x);
        setY(y);
    }

    public void setOrigin(Point origin) {
        if (origin == null) {
            throw new IllegalArgumentException("origin is null.");
        }

        setOrigin(origin.x, origin.y);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    protected void setSize(int width, int height) {
        // TODO
        this.width = width;
        this.height = height;
    }

    public Bounds getBounds() {
        return getBounds(true);
    }

    protected Bounds getBounds(boolean validate) {
        // TODO Transform untransformed bounds
        return null;
    }

    /**
     * TODO Subclasses should override this method to perform an inverse
     * transformation and map to the untransformed coordinate space.
     *
     * @param x
     * @param y
     */
    public abstract boolean contains(int x, int y);

    public Paint getFill() {
        return fill;
    }

    public void setFill(Paint fill) {
        this.fill = fill;
    }

    public void setFill(String fill) {
        if (fill == null) {
            throw new IllegalArgumentException("fill is null.");
        }

        // TODO Support an encoding for gradient paints

        setFill(Color.decode(fill));
    }

    public Paint getStroke() {
        return stroke;
    }

    public void setStroke(Paint stroke) {
        this.stroke = stroke;
    }

    public void setStroke(String stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("stroke is null.");
        }

        // TODO Support an encoding for gradient paints

        setStroke(Color.decode(stroke));
    }

    public int getStrokeThickness() {
        return strokeThickness;
    }

    public void setStrokeThickness(int strokeThickness) {
        this.strokeThickness = strokeThickness;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public double getScaleX() {
        return scaleX;
    }

    public void setScaleX(double scaleX) {
        this.scaleX = scaleX;
    }

    public double getScaleY() {
        return scaleY;
    }

    public void setScaleY(double scaleY) {
        this.scaleY = scaleY;
    }

    public double getTranslateX() {
        return translateX;
    }

    public void setTranslateX(double translateX) {
        this.translateX = translateX;
    }

    public double getTranslateY() {
        return translateY;
    }

    public void setTranslateY(double translateY) {
        this.translateY = translateY;
    }

    public AffineTransform getTransform() {
        // TODO Calculate transform based on properties
        return null;
    }

    protected void invalidateRegion() {
        if (parent != null) {
            parent.invalidateRegion(this);
        }

        // TODO Fire event
    }

    protected void invalidateBounds() {
        if (parent != null) {
            parent.invalidateBounds(this);
        }

        // TODO Fire event when bounds are set
    }

    public ListenerList<ShapeListener> getShapeListeners() {
        return shapeListeners;
    }
}
