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
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Point;

/**
 * Shape representing a rectangle.
 */
public class Rectangle extends Shape {
    private static class RectangleListenerList extends ListenerList<RectangleListener>
        implements RectangleListener {
        public void sizeChanged(Rectangle rectangle, int previousWidth, int previousHeight) {
            for (RectangleListener listener : this) {
                listener.sizeChanged(rectangle, previousWidth, previousHeight);
            }
        }

        public void cornerRadiusChanged(Rectangle rectangle, int previousCornerRadius) {
            for (RectangleListener listener : this) {
                listener.cornerRadiusChanged(rectangle, previousCornerRadius);
            }
        }
    }

    // TODO Use only RoundRectangle2D.Float when Sun fixes rendering issues with zero-value
    // arc width and height
    private RectangularShape rectangularShape = new Rectangle2D.Float();
    private java.awt.Shape strokeShape = null;

    private RectangleListenerList rectangleListeners = new RectangleListenerList();

    public int getWidth() {
        return (int)rectangularShape.getWidth();
    }

    public void setWidth(int width) {
        setSize(width, getHeight());
    }

    public int getHeight() {
        return (int)rectangularShape.getHeight();
    }

    public void setHeight(int height) {
        setSize(getWidth(), height);
    }

    public void setSize(int width, int height) {
        int previousWidth = (int)rectangularShape.getWidth();
        int previousHeight = (int)rectangularShape.getHeight();
        if (previousWidth != width
            || previousHeight != height) {
            if (rectangularShape instanceof Rectangle2D) {
                Rectangle2D.Float rectangle2D = (Rectangle2D.Float)rectangularShape;
                rectangle2D.width = width;
                rectangle2D.height = height;
            } else {
                RoundRectangle2D.Float roundRectangle2D = (RoundRectangle2D.Float)rectangularShape;
                roundRectangle2D.width = width;
                roundRectangle2D.height = height;
            }

            invalidate();
            rectangleListeners.sizeChanged(this, previousWidth, previousHeight);
        }
    }

    public int getCornerRadius() {
        int cornerRadius;
        if (rectangularShape instanceof Rectangle2D) {
            cornerRadius = 0;
        } else {
            RoundRectangle2D.Float roundRectangle2D = (RoundRectangle2D.Float)rectangularShape;
            cornerRadius = (int)roundRectangle2D.archeight;
        }

        return cornerRadius;
    }

    public void setCornerRadius(int cornerRadius) {
        int previousCornerRadius = getCornerRadius();

        int width = getWidth();
        int height = getHeight();

        if (cornerRadius == 0) {
            Rectangle2D.Float rectangle2D = new Rectangle2D.Float();
            rectangle2D.width = width;
            rectangle2D.height = height;

            rectangularShape = new Rectangle2D.Float();
        } else {
            RoundRectangle2D.Float roundRectangle2D = new RoundRectangle2D.Float();
            roundRectangle2D.width = width;
            roundRectangle2D.height = height;
            roundRectangle2D.arcwidth = cornerRadius;
            roundRectangle2D.archeight = cornerRadius;

            rectangularShape = roundRectangle2D;
        }

        invalidate();

        rectangleListeners.cornerRadiusChanged(this, previousCornerRadius);
    }

    @Override
    public boolean contains(int x, int y) {
        return (rectangularShape.contains(x, y)
            || (strokeShape != null
                && strokeShape.contains(x, y)));
    }

    @Override
    public void draw(Graphics2D graphics) {
        Paint fill = getFill();
        if (fill != null) {
            graphics.setPaint(fill);
            graphics.fill(rectangularShape);
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
                boundingShape = rectangularShape;
            } else {
                int strokeThickness = getStrokeThickness();
                BasicStroke basicStroke = new BasicStroke(strokeThickness);
                strokeShape = basicStroke.createStrokedShape(rectangularShape);
                boundingShape = strokeShape;
            }

            java.awt.Rectangle bounds = boundingShape.getBounds();

            Point origin = getOrigin();
            setBounds(origin.x + bounds.x, origin.y + bounds.y, bounds.width, bounds.height);
        }
    }

    public ListenerList<RectangleListener> getRectangleListeners() {
        return rectangleListeners;
    }
}
