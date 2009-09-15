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

import java.awt.geom.QuadCurve2D;

import org.apache.pivot.util.ListenerList;

/**
 * Shape representing a quad curve.
 */
public class QuadCurve extends Shape2D {
    private static class QuadCurveListenerList extends ListenerList<QuadCurveListener>
        implements QuadCurveListener {
        public void endpointsChanged(QuadCurve quadCurve, int previousX1, int previousY1,
            int previousX2, int previousY2) {
            for (QuadCurveListener listener : this) {
                listener.endpointsChanged(quadCurve, previousX1, previousY1,
                    previousX2, previousY2);
            }
        }

        public void controlPointChanged(QuadCurve quadCurve, int previousControlX,
            int previousControlY) {
            for (QuadCurveListener listener : this) {
                listener.controlPointChanged(quadCurve, previousControlX,
                    previousControlY);
            }
        }
    }

    private QuadCurve2D.Float quadCurve2D = new QuadCurve2D.Float();

    private QuadCurveListenerList quadCurveListeners = new QuadCurveListenerList();

    public int getX1() {
        return (int)quadCurve2D.x1;
    }

    public void setX1(int x1) {
        setEndpoints(x1, getY1(), getX2(), getY2());
    }

    public int getY1() {
        return (int)quadCurve2D.y1;
    }

    public void setY1(int y1) {
        setEndpoints(getX1(), y1, getX2(), getY2());
    }

    public int getX2() {
        return (int)quadCurve2D.x2;
    }

    public void setX2(int x2) {
        setEndpoints(getX1(), getY1(), x2, getY2());
    }

    public int getY2() {
        return (int)quadCurve2D.y2;
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
            quadCurve2D.x1 = x1;
            quadCurve2D.y1 = y1;
            quadCurve2D.x2 = x2;
            quadCurve2D.y2 = y2;
            invalidate();
            quadCurveListeners.endpointsChanged(this, previousX1, previousY1,
                previousX2, previousY2);
        }
    }

    public int getControlX() {
        return (int)quadCurve2D.ctrlx;
    }

    public void setControlX(int controlX) {
        setControlPoint(controlX, getControlY());
    }

    public int getControlY() {
        return (int)quadCurve2D.ctrly;
    }

    public void setControlY(int controlY) {
        setControlPoint(getControlX(), controlY);
    }

    public void setControlPoint(int controlX, int controlY) {
        int previousControlX = getControlX();
        int previousControlY = getControlY();

        if (previousControlX != controlX
            || previousControlY != controlY) {
            quadCurve2D.ctrlx = controlX;
            quadCurve2D.ctrly = controlY;
            invalidate();
            quadCurveListeners.controlPointChanged(this, previousControlX,
                previousControlY);
        }
    }

    @Override
    protected java.awt.Shape getShape2D() {
        return quadCurve2D;
    }

    public ListenerList<QuadCurveListener> getQuadCurveListeners() {
        return quadCurveListeners;
    }
}
