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

import java.awt.geom.Path2D;
import java.util.Locale;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;

/**
 * A shape representing a geometric path constructed from straight lines,
 * quadratic curves, and and cubic (B&eacute;zier) curves.
 */
public class Path extends Shape2D implements Sequence<Path.Operation> {
    /**
     * Abstract base class for path operations. See the specific subclasses for
     * details.
     */
    public static abstract class Operation {
        private Path path = null;

        /**
         * Private constructor to prevent others from extending this class.
         */
        private Operation() {
        }

        /**
         * Gets the path with which this operation is associated.
         *
         * @return
         * This operation's path, or <tt>null</tt> if it is not currently
         * associated with any path.
         */
        public Path getPath() {
            return path;
        }

        /**
         * Sets the path with which this operation is associated.
         *
         * @param path
         * The path to associate with this operation, or <tt>null</tt> to break
         * an existing association.
         */
        private void setPath(Path path) {
            this.path = path;
        }

        /**
         * Performs this operation on the specified general path.
         *
         * @param path2D
         * The object upon which this operation is performed.
         */
        abstract void operate(Path2D path2D);
    }

    /**
     * Adds a point to the path by moving to the specified coordinates.
     */
    public static final class MoveTo extends Operation {
        private int x;
        private int y;

        /**
         * Creates a <tt>MoveTo</tt> operation that moves to <tt>(0,0)</tt>.
         */
        public MoveTo() {
            this(0, 0);
        }

        /**
         * Creates a <tt>MoveTo</tt> operation that moves to the specified
         * coordinates.
         *
         * @param x
         * The x-coordinate
         *
         * @param y
         * The y-coordinate
         */
        public MoveTo(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        void operate(Path2D path2D) {
            path2D.moveTo(x, y);
        }

        /**
         * Gets the x-coordinate to which this will operation will move.
         */
        public int getX() {
            return x;
        }

        /**
         * Sets the x-coordinate to which this will operation will move.
         */
        public void setX(int x) {
            setLocation(x, y);
        }

        /**
         * Gets the y-coordinate to which this will operation will move.
         */
        public int getY() {
            return y;
        }

        /**
         * Sets the y-coordinate to which this will operation will move.
         */
        public void setY(int y) {
            setLocation(x, y);
        }

        /**
         * Sets the location to which this will operation will move.
         */
        public void setLocation(int x, int y) {
            if (x != this.x
                || y != this.y) {
                this.x = x;
                this.y = y;

                Path path = getPath();
                if (path != null) {
                    path.invalidate();
                    path.pathListeners.operationUpdated(this);
                }
            }
        }
    }

    /**
     * Adds a point to the path by drawing a straight line from the current
     * coordinates to the new specified coordinates.
     */
    public static final class LineTo extends Operation {
        private int x;
        private int y;

        public LineTo() {
            this(0, 0);
        }

        public LineTo(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        void operate(Path2D path2D) {
            path2D.lineTo(x, y);
        }

        /**
         * Gets the x-coordinate to which this will operation will draw a line.
         */
        public int getX() {
            return x;
        }

        /**
         * Sets the x-coordinate to which this will operation will draw a line.
         */
        public void setX(int x) {
            setLocation(x, y);
        }

        /**
         * Gets the y-coordinate to which this will operation will draw a line.
         */
        public int getY() {
            return y;
        }

        /**
         * Sets the y-coordinate to which this will operation will draw a line.
         */
        public void setY(int y) {
            setLocation(x, y);
        }

        /**
         * Sets the location to which this will operation will draw a line.
         */
        public void setLocation(int x, int y) {
            if (x != this.x
                || y != this.y) {
                this.x = x;
                this.y = y;

                Path path = getPath();
                if (path != null) {
                    path.invalidate();
                    path.pathListeners.operationUpdated(this);
                }
            }
        }
    }

    /**
     * Adds a curved segment, defined by three new points, to the path by
     * drawing a B&eacute;zier curve that intersects both the current
     * coordinates and the specified coordinates {@code (x3,y3)},
     * using the specified points {@code (x1,y1)} and {@code (x2,y2)} as
     * B&eacute;zier control points.
     */
    public static final class CurveTo extends Operation {
        private int x1;
        private int y1;
        private int x2;
        private int y2;
        private int x3;
        private int y3;

        public CurveTo() {
            this(0, 0, 0, 0, 0, 0);
        }

        public CurveTo(int x1, int y1, int x2, int y2, int x3, int y3) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.x3 = x3;
            this.y3 = y3;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        void operate(Path2D path2D) {
            path2D.curveTo(x1, y1, x2, y2, x3, y3);
        }

        public int getX1() {
            return x1;
        }

        public void setX1(int x1) {
            setPoints(x1, y1, x2, y2, x3, y3);
        }

        public int getY1() {
            return y1;
        }

        public void setY1(int y1) {
            setPoints(x1, y1, x2, y2, x3, y3);
        }

        public int getX2() {
            return x2;
        }

        public void setX2(int x2) {
            setPoints(x1, y1, x2, y2, x3, y3);
        }

        public int getY2() {
            return y2;
        }

        public void setY2(int y2) {
            setPoints(x1, y1, x2, y2, x3, y3);
        }

        public int getX3() {
            return x3;
        }

        public void setX3(int x3) {
            setPoints(x1, y1, x2, y2, x3, y3);
        }

        public int getY3() {
            return y3;
        }

        public void setY3(int y3) {
            setPoints(x1, y1, x2, y2, x3, y3);
        }

        /**
         * Sets the points by which the curve will be drawn. The curve will be
         * drawn to {@code (x3,y3)}, using {@code (x1,y1)} and {@code (x2,y2)}
         * as B&eacute;zier control points.
         */
        public void setPoints(int x1, int y1, int x2, int y2, int x3, int y3) {
            if (x1 != this.x1
                || y1 != this.y1
                || x2 != this.x2
                || y2 != this.y2
                || x3 != this.x3
                || y3 != this.y3) {
                this.x1 = x1;
                this.y1 = y1;
                this.x2 = x2;
                this.y2 = y2;
                this.x3 = x3;
                this.y3 = y3;

                Path path = getPath();
                if (path != null) {
                    path.invalidate();
                    path.pathListeners.operationUpdated(this);
                }
            }
        }
    }

    /**
     * Adds a curved segment, defined by two new points, to the path by
     * drawing a Quadratic curve that intersects both the current
     * coordinates and the specified coordinates {@code (x2,y2)},
     * using the specified point {@code (x1,y1)} as a quadratic
     * parametric control point.
     */
    public static final class QuadTo extends Operation {
        private int x1;
        private int y1;
        private int x2;
        private int y2;

        public QuadTo() {
            this(0, 0, 0, 0);
        }

        public QuadTo(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        void operate(Path2D path2D) {
            path2D.quadTo(x1, y1, x2, y2);
        }

        public int getX1() {
            return x1;
        }

        public void setX1(int x1) {
            setPoints(x1, y1, x2, y2);
        }

        public int getY1() {
            return y1;
        }

        public void setY1(int y1) {
            setPoints(x1, y1, x2, y2);
        }

        public int getX2() {
            return x2;
        }

        public void setX2(int x2) {
            setPoints(x1, y1, x2, y2);
        }

        public int getY2() {
            return y2;
        }

        public void setY2(int y2) {
            setPoints(x1, y1, x2, y2);
        }

        /**
         * Sets the points by which the curve will be drawn. The curve will be
         * drawn to {@code (x2,y2)}, using {@code (x1,y1)} as a quadratic
         * parametric control point.
         */
        public void setPoints(int x1, int y1, int x2, int y2) {
            if (x1 != this.x1
                || y1 != this.y1
                || x2 != this.x2
                || y2 != this.y2) {
                this.x1 = x1;
                this.y1 = y1;
                this.x2 = x2;
                this.y2 = y2;

                Path path = getPath();
                if (path != null) {
                    path.invalidate();
                    path.pathListeners.operationUpdated(this);
                }
            }
        }
    }

    /**
     * The winding rule specifies how the interior of a path is determined.
     */
    public enum WindingRule {
        /**
         * A <tt>NON_ZERO</tt> winding rule means that if a ray is drawn in any
         * direction from a given point to infinity and the places where the
         * path intersects the ray are examined, the point is inside of the
         * path if and only if the number of times that the path crosses the
         * ray from left to right does not equal the number of times that the
         * path crosses the ray from right to left.
         */
        NON_ZERO(Path2D.WIND_NON_ZERO),

        /**
         * An <tt>EVEN_ODD</tt> winding rule means that enclosed regions of the
         * path alternate between interior and exterior areas as traversed from
         * the outside of the path towards a point inside the region.
         */
        EVEN_ODD(Path2D.WIND_EVEN_ODD);

        private int constantValue;

        private WindingRule(int constantValue) {
            this.constantValue = constantValue;
        }

        /**
         * Gets the Java2D constant value that indicates this winding rule.
         *
         * @see Path2D#WIND_EVEN_ODD
         * @see Path2D#WIND_NON_ZERO
         */
        private int getConstantValue() {
            return constantValue;
        }
    }

    /**
     * Path listener list.
     */
    private static class PathListenerList extends ListenerList<PathListener>
        implements PathListener {
        public void windingRuleChanged(Path path, WindingRule previousWindingRule) {
            for (PathListener listener : this) {
                listener.windingRuleChanged(path, previousWindingRule);
            }
        }

        public void operationInserted(Path path, int index) {
            for (PathListener listener : this) {
                listener.operationInserted(path, index);
            }
        }

        public void operationsRemoved(Path path, int index, Sequence<Operation> removed) {
            for (PathListener listener : this) {
                listener.operationsRemoved(path, index, removed);
            }
        }

        public void operationUpdated(Operation operation) {
            for (PathListener listener : this) {
                listener.operationUpdated(operation);
            }
        }
    }

    private WindingRule windingRule = WindingRule.NON_ZERO;
    private ArrayList<Operation> operations = new ArrayList<Operation>();

    private Path2D path2D = new Path2D.Double();

    private PathListenerList pathListeners = new PathListenerList();

    /**
     * Gets the winding rule that specifies how the interior of this path is
     * determined.
     */
    public WindingRule getWindingRule() {
        return windingRule;
    }

    /**
     * Sets the winding rule that specifies how the interior of this path is
     * determined.
     */
    public void setWindingRule(WindingRule windingRule) {
        if (windingRule == null) {
            throw new IllegalArgumentException("windingRule is null");
        }

        WindingRule previousWindingRule = this.windingRule;

        if (previousWindingRule != windingRule) {
            this.windingRule = windingRule;

            path2D.setWindingRule(windingRule.getConstantValue());
            update();

            pathListeners.windingRuleChanged(this, previousWindingRule);
        }
    }

    public final void setWindingRule(String windingRule) {
        setWindingRule(WindingRule.valueOf(windingRule.toUpperCase(Locale.ENGLISH)));
    }

    /**
     * {@inheritDoc}
     */
    public final int add(Operation operation) {
        int index = getLength();
        insert(operation, index);

        return index;
    }

    /**
     * {@inheritDoc}
     */
    public void insert(Operation operation, int index) {
        if (operation == null) {
            throw new IllegalArgumentException("operation is null.");
        }

        if (index < 0 || index > getLength()) {
            throw new IndexOutOfBoundsException();
        }

        if (operation.getPath() != null) {
            throw new IllegalArgumentException("operation is already in use by another path.");
        }

        operations.insert(operation, index);
        operation.setPath(this);

        invalidate();

        pathListeners.operationInserted(this, index);
    }

    /**
     * {@inheritDoc}
     */
    public Operation update(int index, Operation operation) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public final int remove(Operation operation) {
        int index = indexOf(operation);
        if (index != -1) {
            remove(index, 1);
        }

        return index;
    }

    /**
     * {@inheritDoc}
     */
    public Sequence<Operation> remove(int index, int count) {
        Sequence<Operation> removed = operations.remove(index, count);

        if (count > 0) {
            for (int i = 0, n = removed.getLength(); i < n; i++) {
                removed.get(i).setPath(null);
            }

            invalidate();

            pathListeners.operationsRemoved(this, index, removed);
        }

        return removed;
    }

    /**
     * {@inheritDoc}
     */
    public Operation get(int index) {
        return operations.get(index);
    }

    /**
     * {@inheritDoc}
     */
    public int indexOf(Operation operation) {
        return operations.indexOf(operation);
    }

    /**
     * {@inheritDoc}
     */
    public int getLength() {
        return operations.getLength();
    }

    @Override
    protected java.awt.Shape getShape2D() {
        return path2D;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validate() {
        if (!isValid()) {
            // Re-create our Path2D
            int n = operations.getLength();
            path2D = new Path2D.Double(windingRule.getConstantValue(), n);

            for (int i = 0; i < n; i++) {
                Operation operation = operations.get(i);
                operation.operate(path2D);
            }

            path2D.closePath();

            super.validate();
        }
    }

    /**
     * Returns the path listener list.
     *
     * @return
     * The path listeners.
     */
    public ListenerList<PathListener> getPathListeners() {
        return pathListeners;
    }
}
