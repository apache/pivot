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
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;

import org.apache.pivot.util.ListenerList;


/**
 * Shape representing a rectangle.
 *
 * @author gbrown
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

    private RectangularShape rectangularShape = new Rectangle2D.Float();
    private int cornerRadius = 0;

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
        int previousWidth = getWidth();
        int previousHeight = getHeight();
        if (previousWidth != width
            || previousHeight != height) {
            if (cornerRadius == 0) {
                Rectangle2D.Float rectangle2D
                    = (Rectangle2D.Float)rectangularShape;
                rectangle2D.width = width;
                rectangle2D.height = height;
            } else {
                RoundRectangle2D.Float roundRectangle2D
                    = (RoundRectangle2D.Float)rectangularShape;
                roundRectangle2D.width = width;
                roundRectangle2D.height = height;
            }

            invalidate();
            rectangleListeners.sizeChanged(this, previousWidth, previousHeight);
        }
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(int cornerRadius) {
        int previousCornerRadius = this.cornerRadius;
        if (previousCornerRadius != cornerRadius) {
            this.cornerRadius = cornerRadius;

            if (cornerRadius == 0) {
                rectangularShape = new Rectangle2D.Float(0, 0, getWidth(), getHeight());
            } else {
                rectangularShape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                    cornerRadius, cornerRadius);
            }

            update();
            rectangleListeners.cornerRadiusChanged(this, previousCornerRadius);
        }
    }

    public void draw(Graphics2D graphics) {
        Paint fill = getFill();
        if (fill != null) {
            graphics.setPaint(fill);
            graphics.fill(rectangularShape);
        }

        Paint stroke = getStroke();
        if (stroke != null) {
            int strokeThickness = getStrokeThickness();
            graphics.setPaint(stroke);
            graphics.setStroke(new BasicStroke(strokeThickness));
            graphics.draw(rectangularShape);
        }
    }

    @Override
    protected void validate() {
        if (!isValid()) {
            int strokeThickness = getStrokeThickness();
            setBounds(-strokeThickness / 2, -strokeThickness / 2,
                (int)rectangularShape.getWidth() + strokeThickness,
                (int)rectangularShape.getHeight() + strokeThickness);
        }

        super.validate();
    }

    public ListenerList<RectangleListener> getRectangleListeners() {
        return rectangleListeners;
    }
}
