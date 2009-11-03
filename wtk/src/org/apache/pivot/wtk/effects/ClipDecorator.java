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
package org.apache.pivot.wtk.effects;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Point;


/**
 * Decorator that adds a rectangular region to the current clip.
 */
public class ClipDecorator implements Decorator {
    private int x = 0;
    private int y = 0;
    private int width = 0;
    private int height = 0;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        setOrigin(x, y);
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        setOrigin(x, y);
    }

    public void setOrigin(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point getOrigin() {
        return new Point(x, y);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        setSize(width, height);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        setSize(width, height);
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Dimensions getSize() {
        return new Dimensions(width, height);
    }

    @Override
    public Graphics2D prepare(Component component, Graphics2D graphics) {
        graphics.clipRect(x, y, width, height);
        return graphics;
    }

    @Override
    public void update() {
        // No-op
    }

    @Override
    public Bounds getBounds(Component component) {
        return new Bounds(x, y, width, height);
    }

    @Override
    public AffineTransform getTransform(Component component) {
        return new AffineTransform();
    }
}
