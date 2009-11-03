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
import org.apache.pivot.wtk.Point;


/**
 * Decorator that translates the paint origin of its component.
 */
public class TranslationDecorator implements Decorator {
    private int x;
    private int y;
    private boolean clip;

    public TranslationDecorator() {
        this(0, 0, false);
    }

    public TranslationDecorator(boolean clip) {
        this(0, 0, clip);
    }

    public TranslationDecorator(int x, int y, boolean clip) {
        setOffset(x, y);
        setClip(clip);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Point getOffset() {
        return new Point(x, y);
    }

    public void setOffset(Point offset) {
        if (offset == null) {
            throw new IllegalArgumentException("offset is null.");
        }

        setOffset(offset.x, offset.y);
    }

    public void setOffset(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean getClip() {
        return clip;
    }

    public void setClip(boolean clip) {
        this.clip = clip;
    }

    @Override
    public Graphics2D prepare(Component component, Graphics2D graphics) {
        if (clip) {
            Bounds decoratedBounds = component.getDecoratedBounds();
            graphics.clipRect(decoratedBounds.x - component.getX(),
                decoratedBounds.y - component.getY(),
                decoratedBounds.width, decoratedBounds.height);
        }

        graphics.translate(x, y);
        return graphics;
    }

    @Override
    public void update() {
        // No-op
    }

    @Override
    public Bounds getBounds(Component component) {
        int width = component.getWidth();
        int height = component.getHeight();

        Bounds bounds = new Bounds(x, y, width, height);

        if (clip) {
            bounds = bounds.intersect(0, 0, width, height);
        }

        return bounds;
    }

    @Override
    public AffineTransform getTransform(Component component) {
        return AffineTransform.getTranslateInstance(x, y);
    }
}
