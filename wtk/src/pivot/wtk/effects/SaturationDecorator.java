/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Contains code and concepts from ShadowRenderer.java v1.6,
 * copyright 2006 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
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
package pivot.wtk.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import pivot.wtk.Component;
import pivot.wtk.Decorator;
import pivot.wtk.Bounds;

/**
 * Decorator that modifies the color saturation of a component.
 *
 * @author tvolkert
 */
public class SaturationDecorator implements Decorator {
    private float multiplier;

    private Component component = null;
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

    public Graphics2D prepare(Component component, Graphics2D graphics) {
        int width = component.getWidth();
        int height = component.getHeight();

        if (this.component != component
            || componentImage == null
            || componentImage.getWidth() != width
            || componentImage.getHeight() != height) {
            componentImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        this.component = component;
        this.graphics = graphics;

        componentGraphics = componentImage.createGraphics();
        componentGraphics.setClip(graphics.getClip());

        return componentGraphics;
    }

    /**
     * Adjusts the saturation of the component image and draws the resulting
     * image using the component's graphics.
     */
    public void update() {
        java.awt.Rectangle clipBounds = componentGraphics.getClipBounds();
        componentGraphics.dispose();

        int x = 0;
        int y = 0;
        int width = componentImage.getWidth();
        int height = componentImage.getHeight();

        if (clipBounds != null) {
            // We only bother to adjust the saturation of pixels that
            // are within the clip bounds
            // TODO Debug this. It should work, but there are areas whose
            // saturation is unaffected
            /*
            x = Math.min(Math.max(clipBounds.x, 0), x);
            y = Math.min(Math.max(clipBounds.y, 0), y);
            width = Math.min(Math.max(clipBounds.width, 0), width - x);
            height = Math.min(Math.max(clipBounds.height, 0), height - y);
            */
        }

        int[] buffer = new int[width * height];

        WritableRaster raster = componentImage.getRaster();
        raster.getDataElements(x, y, width, height, buffer);

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

        raster.setDataElements(x, y, width, height, buffer);

        graphics.drawImage(componentImage, 0, 0, null);
    }

    public Bounds getAffectedArea(Component component, int x, int y, int width, int height) {
        return new Bounds(x, y, width, height);
    }
}
