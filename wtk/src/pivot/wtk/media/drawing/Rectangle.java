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

import pivot.util.ListenerList;

/**
 * Shape representing a rectangle.
 * <p>
 * TODO Add a corner radii property.
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
    }

    private Rectangle2D.Double rectangle2D = new Rectangle2D.Double();

    private RectangleListenerList rectangleListeners = new RectangleListenerList();

    @Override
    public boolean contains(int x, int y) {
        return rectangle2D.contains(x, y);
    }

    public void draw(Graphics2D graphics) {
        Paint fill = getFill();
        graphics.setPaint(fill);
        graphics.fill(rectangle2D);

        Paint stroke = getStroke();
        int strokeThickness = getStrokeThickness();
        graphics.setPaint(stroke);
        graphics.setStroke(new BasicStroke(strokeThickness));
        graphics.draw(rectangle2D);
    }

    public int getWidth() {
        return (int)rectangle2D.width;
    }

    public void setWidth(int width) {
        setSize(width, (int)rectangle2D.height);
    }

    public int getHeight() {
        return (int)rectangle2D.height;
    }

    public void setHeight(int height) {
        setSize((int)rectangle2D.width, height);
    }

    public void setSize(int width, int height) {
        int previousWidth = (int)rectangle2D.width;
        int previousHeight = (int)rectangle2D.height;
        if (previousWidth != width
            || previousHeight != height) {
            rectangle2D.width = width;
            rectangle2D.height = height;
            invalidate();
            rectangleListeners.sizeChanged(this, previousWidth, previousHeight);
        }
    }

    @Override
    protected void validate() {
        int strokeThickness = getStrokeThickness();
        setBounds(-strokeThickness / 2, -strokeThickness / 2,
            (int)rectangle2D.width + strokeThickness,
            (int)rectangle2D.height + strokeThickness);

        super.validate();
    }

    public ListenerList<RectangleListener> getRectangleListeners() {
        return rectangleListeners;
    }
}
