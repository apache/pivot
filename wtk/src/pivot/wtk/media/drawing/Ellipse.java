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
import java.awt.geom.Ellipse2D;

import pivot.util.ListenerList;

/**
 * Shape representing an ellipse.
 *
 * @author tvolkert
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

    public void draw(Graphics2D graphics) {
        Paint fill = getFill();
        if (fill != null) {
            graphics.setPaint(fill);
            graphics.fill(ellipse2D);
        }

        Paint stroke = getStroke();
        if (stroke != null) {
            int strokeThickness = getStrokeThickness();
            graphics.setPaint(stroke);
            graphics.setStroke(new BasicStroke(strokeThickness));
            graphics.draw(ellipse2D);
        }
    }

    @Override
    protected void validate() {
        if (!isValid()) {
            int strokeThickness = getStrokeThickness();
            setBounds(-strokeThickness / 2, -strokeThickness / 2,
                (int)ellipse2D.width + strokeThickness,
                (int)ellipse2D.height + strokeThickness);
        }

        super.validate();
    }

    public ListenerList<EllipseListener> getEllipseListeners() {
        return ellipseListeners;
    }
}
