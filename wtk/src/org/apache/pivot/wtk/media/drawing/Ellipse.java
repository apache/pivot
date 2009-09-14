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
import java.awt.geom.Ellipse2D;

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Point;


/**
 * Shape representing an ellipse.
 */
public class Ellipse extends Shape {
    private static class EllipseListenerList extends ListenerList<EllipseListener>
        implements EllipseListener {
        public void sizeChanged(Ellipse ellipse, int previousWidth, int previousHeight) {
            for (EllipseListener listener : this) {
                listener.sizeChanged(ellipse, previousWidth, previousHeight);
            }
        }
    }

    private Ellipse2D.Float ellipse2D = new Ellipse2D.Float();
    private java.awt.Shape strokeShape = null;

    private EllipseListenerList ellipseListeners = new EllipseListenerList();

    public int getWidth() {
        return (int)ellipse2D.width;
    }

    public void setWidth(int width) {
        setSize(width, (int)ellipse2D.height);
    }

    public int getHeight() {
        return (int)ellipse2D.height;
    }

    public void setHeight(int height) {
        setSize((int)ellipse2D.width, height);
    }

    public void setSize(int width, int height) {
        int previousWidth = (int)ellipse2D.width;
        int previousHeight = (int)ellipse2D.height;
        if (previousWidth != width
            || previousHeight != height) {
            ellipse2D.width = width;
            ellipse2D.height = height;
            invalidate();
            ellipseListeners.sizeChanged(this, previousWidth, previousHeight);
        }
    }

    @Override
    public boolean contains(int x, int y) {
        return (ellipse2D.contains(x, y)
            || (strokeShape != null
                && strokeShape.contains(x, y)));
    }

    @Override
    public void draw(Graphics2D graphics) {
        Paint fill = getFill();
        if (fill != null) {
            graphics.setPaint(fill);
            graphics.fill(ellipse2D);
        }

        Paint stroke = getStroke();
        if (stroke != null) {
            graphics.setPaint(stroke);
            graphics.fill(strokeShape);
        }
    }

    @Override
    protected void validate() {
        if (!isValid()) {
            java.awt.Shape boundingShape;

            if (getStroke() == null) {
                strokeShape = null;
                boundingShape = ellipse2D;
            } else {
                int strokeThickness = getStrokeThickness();
                BasicStroke basicStroke = new BasicStroke(strokeThickness);
                strokeShape = basicStroke.createStrokedShape(ellipse2D);
                boundingShape = strokeShape;
            }

            java.awt.Rectangle bounds = boundingShape.getBounds();

            Point origin = getOrigin();
            setBounds(origin.x + bounds.x, origin.y + bounds.y, bounds.width, bounds.height);
        }
    }

    public ListenerList<EllipseListener> getEllipseListeners() {
        return ellipseListeners;
    }
}
