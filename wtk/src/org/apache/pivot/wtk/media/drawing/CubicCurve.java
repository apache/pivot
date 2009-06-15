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

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Rectangle2D;

import org.apache.pivot.util.ListenerList;


/**
 * Shape representing a cubic curve.
 *
 * @author gbrown
 */
public class CubicCurve extends Shape {
    private static class CubicCurveListenerList extends ListenerList<CubicCurveListener>
        implements CubicCurveListener {
        public void endpointsChanged(CubicCurve cubicCurve, int previousX1, int previousY1,
            int previousX2, int previousY2) {
            for (CubicCurveListener listener : this) {
                listener.endpointsChanged(cubicCurve, previousX1, previousY1,
                    previousX2, previousY2);
            }
        }

        public void controlPointsChanged(CubicCurve cubicCurve, int previousControlX1,
            int previousControlY1, int previousControlX2, int previousControlY2) {
            for (CubicCurveListener listener : this) {
                listener.controlPointsChanged(cubicCurve, previousControlX1, previousControlY1,
                    previousControlX2, previousControlY2);
            }
        }
    }

    private CubicCurve2D.Float cubicCurve2D = new CubicCurve2D.Float();

    private CubicCurveListenerList cubicCurveListeners = new CubicCurveListenerList();

    public int getX1() {
        return (int)cubicCurve2D.x1;
    }

    public void setX1(int x1) {
        setEndpoints(x1, getY1(), getX2(), getY2());
    }

    public int getY1() {
        return (int)cubicCurve2D.y1;
    }

    public void setY1(int y1) {
        setEndpoints(getX1(), y1, getX2(), getY2());
    }

    public int getX2() {
        return (int)cubicCurve2D.x2;
    }

    public void setX2(int x2) {
        setEndpoints(getX1(), getY1(), x2, getY2());
    }

    public int getY2() {
        return (int)cubicCurve2D.y2;
    }

    public void setY2(int y2) {
        setEndpoints(getX1(), getY1(), getX2(), y2);
    }

    public void setEndpoints(int x1, int y1, int x2, int y2) {
        int previousX1 = getX1();
        int previousY1 = getY1();
        int previousX2 = getX2();
        int previousY2 = getY2();

        if (previousX1 != x1
            || previousY1 != y1
            || previousX2 != x2
            || previousY2 != y2) {
            cubicCurve2D.x1 = x1;
            cubicCurve2D.y1 = y1;
            cubicCurve2D.x2 = x2;
            cubicCurve2D.y2 = y2;
            invalidate();
            cubicCurveListeners.endpointsChanged(this, previousX1, previousY1,
                previousX2, previousY2);
        }
    }

    public int getControlX1() {
        return (int)cubicCurve2D.ctrlx1;
    }

    public void setControlX1(int controlX1) {
        setControlPoints(controlX1, getControlY1(), getControlX2(), getControlY2());
    }

    public int getControlY1() {
        return (int)cubicCurve2D.ctrly1;
    }

    public void setControlY1(int controlY1) {
        setControlPoints(getControlX1(), controlY1, getControlX2(), getControlY2());
    }

    public int getControlX2() {
        return (int)cubicCurve2D.ctrlx2;
    }

    public void setControlX2(int controlX2) {
        setControlPoints(getControlX1(), getControlY1(), controlX2, getControlY2());
    }

    public int getControlY2() {
        return (int)cubicCurve2D.ctrly2;
    }

    public void setControlY2(int controlY2) {
        setControlPoints(getControlX1(), getControlY1(), getControlX2(), controlY2);
    }

    public void setControlPoints(int controlX1, int controlY1, int controlX2, int controlY2) {
        int previousControlX1 = getControlX1();
        int previousControlY1 = getControlY1();
        int previousControlX2 = getControlX2();
        int previousControlY2 = getControlY2();

        if (previousControlX1 != controlX1
            || previousControlY1 != controlY1
            || previousControlX2 != controlX2
            || previousControlY2 != controlY2) {
            cubicCurve2D.ctrlx1 = controlX1;
            cubicCurve2D.ctrly1 = controlY1;
            cubicCurve2D.ctrlx2 = controlX2;
            cubicCurve2D.ctrly2 = controlY2;
            invalidate();
            cubicCurveListeners.controlPointsChanged(this, previousControlX1, previousControlY1,
                previousControlX2, previousControlY2);
        }
    }

    @Override
    public void draw(Graphics2D graphics) {
        Paint fill = getFill();
        if (fill != null) {
            graphics.setPaint(fill);
            graphics.fill(cubicCurve2D);
        }

        Paint stroke = getStroke();
        if (stroke != null) {
            int strokeThickness = getStrokeThickness();
            graphics.setPaint(stroke);
            graphics.setStroke(new BasicStroke(strokeThickness));
            graphics.draw(cubicCurve2D);
        }
    }

    @Override
    protected void validate() {
        if (!isValid()) {
            // Over-estimate the bounds to keep the logic simple
            int strokeThickness = getStrokeThickness();
            double radius = (strokeThickness / Math.cos(Math.PI / 4)) / 2;

            Rectangle2D boundingRectangle = cubicCurve2D.getBounds2D();
            setBounds((int)Math.floor(boundingRectangle.getX() - radius),
                (int)Math.floor(boundingRectangle.getY() - radius),
                (int)Math.ceil(boundingRectangle.getWidth() + radius * 2),
                (int)Math.ceil(boundingRectangle.getHeight() + radius * 2));
        }

        super.validate();
    }

    public ListenerList<CubicCurveListener> getCubicCurveListeners() {
        return cubicCurveListeners;
    }
}
