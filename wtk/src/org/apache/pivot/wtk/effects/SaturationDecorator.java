/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;

/**
 * Decorator that modifies the color saturation of a component.
 */
public class SaturationDecorator implements Decorator {
    private float multiplier;

    private Graphics2D graphics = null;

    private BufferedImage componentImage = null;
    private Graphics2D componentGraphics = null;

    public SaturationDecorator() {
        this(1.0f);
    }

    public SaturationDecorator(float multiplier) {
        this.multiplier = multiplier;
    }

    public float getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(float multiplier) {
        this.multiplier = multiplier;
    }

    public void setMultiplier(Number multiplier) {
        if (multiplier == null) {
            throw new IllegalArgumentException("Multiplier is null.");
        }

        setMultiplier(multiplier.floatValue());
    }

    @Override
    public Graphics2D prepare(Component component, Graphics2D graphicsArgument) {
        int x = 0;
        int y = 0;
        int width = component.getWidth();
        int height = component.getHeight();

        java.awt.Rectangle clipBounds = graphicsArgument.getClipBounds();
        if (clipBounds != null) {
            x = clipBounds.x;
            y = clipBounds.y;
            width = clipBounds.width;
            height = clipBounds.height;
        }

        componentImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        this.graphics = graphicsArgument;

        componentGraphics = componentImage.createGraphics();
        componentGraphics.translate(-x, -y);
        componentGraphics.setClip(graphicsArgument.getClip());

        return componentGraphics;
    }

    /**
     * Adjusts the saturation of the component image and draws the resulting
     * image using the component's graphics.
     */
    @Override
    public void update() {
        int width = componentImage.getWidth();
        int height = componentImage.getHeight();

        int[] buffer = new int[width * height];

        WritableRaster raster = componentImage.getRaster();
        raster.getDataElements(0, 0, width, height, buffer);

        float[] hsb = new float[3];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int srcRGB = buffer[i * width + j];

                // Adjust color saturation
                Color.RGBtoHSB((srcRGB >> 16) & 0xff,
                    (srcRGB >> 8) & 0xff, srcRGB & 0xff, hsb);
                int dstRGB = Color.HSBtoRGB(hsb[0],
                    Math.min(Math.max(hsb[1] * multiplier, 0f), 1f), hsb[2]);

                // Preserve the source alpha channel
                dstRGB = (srcRGB & 0xff000000) | (dstRGB & 0xffffff);

                buffer[i * width + j] = dstRGB;
            }
        }

        raster.setDataElements(0, 0, width, height, buffer);

        int x = 0;
        int y = 0;

        java.awt.Rectangle clipBounds = componentGraphics.getClipBounds();
        if (clipBounds != null) {
            x = clipBounds.x;
            y = clipBounds.y;
        }

        componentGraphics.dispose();
        componentGraphics = null;

        graphics.drawImage(componentImage, x, y, null);

        componentImage = null;
        graphics = null;
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
