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

import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.GraphicsUtilities;

/**
 * Decorator that applies a "shade" to a component. The shade is a rectangle of
 * the same size as the component that is painted over the component using a
 * given color and opacity value.
 */
public class ShadeDecorator implements Decorator {
    private float opacity;
    private Color color;

    private Component component;
    private Graphics2D graphics;

    /**
     * Creates a new <tt>ShadeDecorator</tt> with the default opacity and shade
     * color.
     */
    public ShadeDecorator() {
        this(0.33f, Color.BLACK);
    }

    private void checkOpacity(float opacityValue) {
        if (opacityValue <= 0.0f || opacityValue >= 1.0f) {
            throw new IllegalArgumentException("opacity must be between 0.0 and 1.0, exclusive.");
        }
    }

    /**
     * Creates a new <tt>ShadeDecorator</tt> with the specified opacity and
     * shade color.
     *
     * @param opacity The opacity of the shade, between 0.0 and 1.0, exclusive.
     * @param color The color of the shade.
     */
    public ShadeDecorator(float opacity, Color color) {
        checkOpacity(opacity);
        Utils.checkNull(color, "color");

        this.opacity = opacity;
        this.color = color;
    }

    /**
     * @return The opacity of the decorator, in [0.0,1.0].
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     * Sets the opacity of the decorator.
     *
     * @param opacity A number between 0.0 (transparent) and 1.0 (opaque), exclusive.
     */
    public void setOpacity(float opacity) {
        checkOpacity(opacity);

        this.opacity = opacity;
    }

    /**
     * Sets the opacity of the decorator.
     *
     * @param opacity A number between 0.0 (transparent) and 1.0 (opaque), exclusive.
     */
    public void setOpacity(Number opacity) {
        Utils.checkNull(opacity, "opacity");

        setOpacity(opacity.floatValue());
    }

    /**
     * @return The color of the decorator.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the color of the decorator.
     *
     * @param color The new color for the decorator.
     */
    public void setColor(Color color) {
        Utils.checkNull(color, "color");

        this.color = color;
    }

    /**
     * Sets the color of the decorator.
     *
     * @param color Any of the {@linkplain GraphicsUtilities#decodeColor color
     * values recognized by Pivot}.
     */
    public final void setColor(String color) {
        setColor(GraphicsUtilities.decodeColor(color, "color"));
    }

    @Override
    public Graphics2D prepare(Component componentValue, Graphics2D graphicsValue) {
        this.component = componentValue;
        this.graphics = graphicsValue;

        return graphicsValue;
    }

    @Override
    public void update() {
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        graphics.setColor(color);
        graphics.fillRect(0, 0, component.getWidth(), component.getHeight());

        component = null;
        graphics = null;
    }

}

