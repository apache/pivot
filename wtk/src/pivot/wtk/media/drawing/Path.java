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

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.GeneralPath;

import pivot.collections.ArrayList;
import pivot.collections.Sequence;
import pivot.util.ListenerList;

/**
 * The <tt>Path</tt> shape represents a geometric path constructed from
 * straight lines, quadratic curves, and and cubic (B&eacute;zier) curves.
 *
 * @author tvolkert
 */
public class Path extends Shape implements Sequence<Path.Operation> {
    /**
     *
     *
     * @author tvolkert
     */
    public static abstract class Operation {
        public static enum Type {
            MOVE_TO,
            LINE_TO,
            CURVE_TO,
            QUAD_TO;
        }

        /**
         * Private constructor to prevent others from extending this class.
         */
        private Operation() {
        }

        public abstract Type getType();
    }

    /**
     *
     *
     * @author tvolkert
     */
    public static final class MoveTo extends Operation {
        private int x;
        private int y;

        public MoveTo() {
            this(0, 0);
        }

        public MoveTo(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Type getType() {
            return Type.MOVE_TO;
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
    }

    /**
     *
     *
     * @author tvolkert
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

        public Type getType() {
            return Type.LINE_TO;
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
    }

    /**
     *
     *
     * @author tvolkert
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

        public Type getType() {
            return Type.CURVE_TO;
        }

        public int getX1() {
            return x1;
        }

        public void setX1(int x1) {
            this.x1 = x1;
        }

        public int getY1() {
            return y1;
        }

        public void setY1(int y1) {
            this.y1 = y1;
        }

        public int getX2() {
            return x2;
        }

        public void setX2(int x2) {
            this.x2 = x2;
        }

        public int getY2() {
            return y2;
        }

        public void setY2(int y2) {
            this.y2 = y2;
        }

        public int getX3() {
            return x3;
        }

        public void setX3(int x3) {
            this.x3 = x3;
        }

        public int getY3() {
            return y3;
        }

        public void setY3(int y3) {
            this.y3 = y3;
        }
    }

    /**
     *
     *
     * @author tvolkert
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

        public Type getType() {
            return Type.QUAD_TO;
        }

        public int getX1() {
            return x1;
        }

        public void setX1(int x1) {
            this.x1 = x1;
        }

        public int getY1() {
            return y1;
        }

        public void setY1(int y1) {
            this.y1 = y1;
        }

        public int getX2() {
            return x2;
        }

        public void setX2(int x2) {
            this.x2 = x2;
        }

        public int getY2() {
            return y2;
        }

        public void setY2(int y2) {
            this.y2 = y2;
        }
    }

    /**
     * The winding rule specifies how the interior of a path is determined.
     *
     * @author tvolkert
     */
    public static enum WindingRule {
        /**
         * A <tt>NON_ZERO</tt> winding rule means that if a ray is drawn in any
         * direction from a given point to infinity and the places where the
         * path intersects the ray are examined, the point is inside of the
         * path if and only if the number of times that the path crosses the
         * ray from left to right does not equal the number of times that the
         * path crosses the ray from right to left.
         */
        NON_ZERO (GeneralPath.WIND_NON_ZERO),

        /**
         * An <tt>EVEN_ODD</tt> winding rule means that enclosed regions of the
         * path alternate between interior and exterior areas as traversed from
         * the outside of the path towards a point inside the region.
         */
        EVEN_ODD (GeneralPath.WIND_EVEN_ODD);

        private int constantValue;

        private WindingRule(int constantValue) {
            this.constantValue = constantValue;
        }

        private int getConstantValue() {
            return constantValue;
        }
    }

    /**
     * Path listener list.
     *
     * @author tvolkert
     */
    private static class PathListenerList extends ListenerList<PathListener>
        implements PathListener {
        public void windingRuleChanged(Path path, WindingRule previousWindingRule) {
            for (PathListener listener : this) {
                listener.windingRuleChanged(path, previousWindingRule);
            }
        }
    }

    private GeneralPath generalPath = null;
    private WindingRule windingRule = WindingRule.NON_ZERO;

    private ArrayList<Operation> operations = new ArrayList<Operation>();

    private PathListenerList pathListeners = new PathListenerList();

    @Override
    public boolean contains(int x, int y) {
        validateGeneralPath();
        return generalPath.contains(x, y);
    }

    public void draw(Graphics2D graphics) {
        validateGeneralPath();

        Paint fill = getFill();
        if (fill != null) {
            graphics.setPaint(fill);
            graphics.fill(generalPath);
        }

        Paint stroke = getStroke();
        if (stroke != null) {
            int strokeThickness = getStrokeThickness();
            graphics.setPaint(stroke);
            graphics.setStroke(new BasicStroke(strokeThickness));
            graphics.draw(generalPath);
        }
    }

    @Override
    protected void validate() {
        validateGeneralPath();
        // TODO

        super.validate();
    }

    private void validateGeneralPath() {
        if (generalPath == null) {
            int length = getLength();
            generalPath = new GeneralPath(windingRule.getConstantValue(), length);

            for (int i = 0; i < length; i++) {
                Operation operation = get(i);

                switch (operation.getType()) {
                case MOVE_TO:
                    MoveTo moveTo = (MoveTo)operation;
                    generalPath.moveTo(moveTo.getX(), moveTo.getY());
                    break;

                case LINE_TO:
                    LineTo lineTo = (LineTo)operation;
                    generalPath.lineTo(lineTo.getX(), lineTo.getY());
                    break;

                case CURVE_TO:
                    CurveTo curveTo = (CurveTo)operation;
                    generalPath.curveTo(curveTo.getX1(), curveTo.getY1(), curveTo.getX2(),
                        curveTo.getY2(), curveTo.getX3(), curveTo.getY3());
                    break;

                case QUAD_TO:
                    QuadTo quadTo = (QuadTo)operation;
                    generalPath.quadTo(quadTo.getX1(), quadTo.getY1(),
                        quadTo.getX2(), quadTo.getY2());
                    break;
                }
            }

            generalPath.closePath();
        }
    }

    public int add(Operation operation) {
        generalPath = null;
        return operations.add(operation);
    }

    public void insert(Operation operation, int index) {
        generalPath = null;
        operations.insert(operation, index);
    }

    public Operation update(int index, Operation operation) {
        generalPath = null;
        return operations.update(index, operation);
    }

    public int remove(Operation operation) {
        generalPath = null;
        return operations.remove(operation);
    }

    public Sequence<Operation> remove(int index, int count) {
        generalPath = null;
        return operations.remove(index, count);
    }

    public Operation get(int index) {
        return operations.get(index);
    }

    public int indexOf(Operation operation) {
        return operations.indexOf(operation);
    }

    public int getLength() {
        return operations.getLength();
    }

    public ListenerList<PathListener> getPathListeners() {
        return pathListeners;
    }
}
