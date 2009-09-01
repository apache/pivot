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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;

/**
 * Decorator that applies a "shade" to a component. The shade is a rectangle
 * of the same size as the component that is painted over the component using a
 * given color and opacity value.
 */
public class ShadeDecorator implements Decorator {
    private float opacity;
    private Color color;

    private Graphics2D graphics;
    private Component component;

    /**
     * Creates a new <tt>ShadeDecorator</tt> with the default opacity and
     * shade color.
     */
    public ShadeDecorator() {
        this(0.33f, Color.BLACK);
    }

    /**
     * Creates a new <tt>ShadeDecorator</tt> with the specified opacity and
     * shade color.
     *
     * @param opacity
     * The opacity of the shade, between 0 and 1, exclusive.
     *
     * @param color
     * The color of the shade.
     */
    public ShadeDecorator(float opacity, Color color) {
        if (opacity <= 0 || opacity >= 1) {
            throw new IllegalArgumentException("opacity must be between 0 and 1, exclusive.");
        }

        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.opacity = opacity;
        this.color = color;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public void setOpacity(Number opacity) {
        if (opacity == null) {
            throw new IllegalArgumentException("opacity is null.");
        }

        setOpacity(opacity.floatValue());
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.color = color;
    }

    /**
     */
    public final void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(Color.decode(color));
    }

    @Override
    public Graphics2D prepare(Component component, Graphics2D graphics) {
        this.graphics = graphics;
        this.component = component;

        return graphics;
    }

    @Override
    public void update() {
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        graphics.setColor(color);
        graphics.fillRect(0, 0, component.getWidth(), component.getHeight());
    }

    @Override
    public Bounds getBounds(Component component) {
        return new Bounds(0, 0, component.getWidth(), component.getHeight());
    }

    @Override
    public AffineTransform getTransform(Component component) {
        return new AffineTransform();
    }
}
