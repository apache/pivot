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

import org.apache.pivot.wtk.Point;

/**
 * Abstract base class for shapes that encapsulate a Java2D shape.
 */
public abstract class Shape2D extends Shape {
    @Override
    public boolean contains(int x, int y) {
        boolean contains = false;

        java.awt.Shape shape2D = getShape2D();

        if (getFill() != null) {
            contains = shape2D.contains(x, y);
        }

        if (getStroke() != null) {
            BasicStroke basicStroke = new BasicStroke(getStrokeThickness());
            java.awt.Shape strokeShape = basicStroke.createStrokedShape(shape2D);
            contains |= strokeShape.contains(x, y);
        }

        return contains;
    }

    @Override
    public void draw(Graphics2D graphics) {
        java.awt.Shape shape2D = getShape2D();

        Paint fill = getFill();
        if (fill != null) {
            graphics.setPaint(fill);
            graphics.fill(shape2D);
        }

        Paint stroke = getStroke();
        if (stroke != null) {
            graphics.setPaint(stroke);
            BasicStroke basicStroke = new BasicStroke(getStrokeThickness());
            java.awt.Shape strokeShape = basicStroke.createStrokedShape(shape2D);
            graphics.fill(strokeShape);
        }
    }

    @Override
    protected void validate() {
        if (!isValid()) {
            java.awt.Shape shape2D = getShape2D();

            java.awt.Shape boundingShape;
            if (getStroke() == null) {
                boundingShape = shape2D;
            } else {
                int strokeThickness = getStrokeThickness();
                BasicStroke basicStroke = new BasicStroke(strokeThickness);
                java.awt.Shape strokeShape = basicStroke.createStrokedShape(shape2D);
                boundingShape = strokeShape;
            }

            java.awt.Rectangle bounds = boundingShape.getBounds();

            Point origin = getOrigin();
            setBounds(origin.x + bounds.x, origin.y + bounds.y, bounds.width, bounds.height);
        }
    }

    protected abstract java.awt.Shape getShape2D();
}
