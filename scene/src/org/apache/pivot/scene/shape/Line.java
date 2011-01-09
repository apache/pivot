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
package org.apache.pivot.scene.shape;

import org.apache.pivot.scene.Extents;
import org.apache.pivot.scene.Graphics;
import org.apache.pivot.scene.Point;

/**
 * Shape representing a line.
*/
public class Line extends Shape {
    private int x1 = 0;
    private int y1 = 0;

    private int x2 = 0;
    private int y2 = 0;

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        setEndpoint1(x1, y1);
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        setEndpoint1(x1, y1);
    }

    public int getX2() {
        return x2;
    }

    public void setX2(int x2) {
        setEndpoint2(x1, y1);
    }

    public int getY2() {
        return y2;
    }

    public void setY2(int y2) {
        setEndpoint2(x1, y1);
    }

    public Point getEndpoint1() {
        return new Point(getX1(), getY1());
    }

    public void setEndpoint1(Point endpoint1) {
        setEndpoint1(endpoint1.x, endpoint1.y);
    }

    public void setEndpoint1(int x1, int y1) {
        int previousX1 = this.x1;
        int previousY1 = this.y1;

        if (previousX1 != x1
            || previousY1 != y1) {
            this.x1 = x1;
            this.y1 = y1;

            invalidate();
        }
    }

    public Point getEndpoint2() {
        return new Point(getX2(), getY2());
    }

    public void setEndpoint2(int x2, int y2) {
        int previousX2 = this.x2;
        int previousY2 = this.y2;

        if (previousX2 != x2
            || previousY2 != y2) {
            this.x2 = x2;
            this.y2 = y2;

            invalidate();
        }
    }

    @Override
    public boolean contains(int x, int y) {
        // TODO
        return true;
    }

    @Override
    public Extents getExtents() {
        return new Extents(Math.min(x1, x2),
            Math.max(x1, x2),
            Math.min(y1, y2),
            Math.min(y1, y2));
    }

    @Override
    protected void drawShape(Graphics graphics) {
        graphics.drawLine(x1, y1, x2, y2);
    }

    @Override
    protected void fillShape(Graphics graphics) {
        // No-op
    }
}
