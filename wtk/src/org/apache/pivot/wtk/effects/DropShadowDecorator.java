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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.GraphicsUtilities;

/**
 * Decorator that adds a drop shadows to a component.
 */
public class DropShadowDecorator implements Decorator {
    private int blurRadius;
    private int xOffset;
    private int yOffset;

    private Color shadowColor = Color.BLACK;
    private float shadowOpacity = DEFAULT_SHADOW_OPACITY;

    private BufferedImage shadowImage = null;

    public static final float DEFAULT_SHADOW_OPACITY = 0.25f;

    public DropShadowDecorator() {
        this(5, 5, 5);
    }

    public DropShadowDecorator(int blurRadius, int xOffset, int yOffset) {
        this.blurRadius = blurRadius;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    /**
     * Returns the color used to draw the shadow.
     *
     * @return
     * The color used to draw the shadow.
     */
    public Color getShadowColor() {
        return shadowColor;
    }

    /**
     * Sets the color used to draw the shadow.
     *
     * @param shadowColor
     * The color used to draw the shadow.
     */
    public void setShadowColor(Color shadowColor) {
        this.shadowColor = shadowColor;
    }

    /**
     * Sets the color used to draw the shadow.
     *
     * @param shadowColor
     * The color used to draw the shadow, which can be any of the
     * {@linkplain GraphicsUtilities#decodeColor color values recognized by Pivot}.
     */
    public final void setShadowColor(String shadowColor) {
        if (shadowColor == null) {
            throw new IllegalArgumentException("shadowColor is null.");
        }

        setShadowColor(GraphicsUtilities.decodeColor(shadowColor));
    }

    /**
     * Returns the opacity used to draw the shadow.
     *
     * @return
     * The color used to draw the shadow.
     */
    public float getShadowOpacity() {
        return shadowOpacity;
    }

    /**
     * Sets the opacity used to draw the shadow.
     *
     * @param shadowOpacity
     * The opacity used to draw the shadow.
     */
    public void setShadowOpacity(float shadowOpacity) {
        this.shadowOpacity = shadowOpacity;
    }

    /**
     * Returns the blur radius used to draw the shadow.
     *
     * @return
     * The blur radius used to draw the shadow.
     */
    public int getBlurRadius() {
        return blurRadius;
    }

    /**
     * Sets the blur radius used to draw the shadow.
     *
     * @param blurRadius
     * The blur radius used to draw the shadow.
     */
    public void setBlurRadius(int blurRadius) {
        this.blurRadius = blurRadius;
    }

    /**
     * Returns the amount that the drop shadow will be offset along the x axis.
     *
     * @return
     * The x offset used to draw the shadow
     */
    public int getXOffset() {
        return xOffset;
    }

    /**
     * Sets the amount that the drop shadow will be offset along the x axis.
     *
     * @param xOffset
     * The x offset used to draw the shadow
     */
    public void setXOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    /**
     * Returns the amount that the drop shadow will be offset along the y axis.
     *
     * @return
     * The y offset used to draw the shadow
     */
    public int getYOffset() {
        return yOffset;
    }

    /**
     * Sets the amount that the drop shadow will be offset along the y axis.
     *
     * @param yOffset
     * The y offset used to draw the shadow
     */
    public void setYOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    @Override
    public Graphics2D prepare(Component component, Graphics2D graphics) {
        int width = component.getWidth();
        int height = component.getHeight();

        if (width > 0
            && height > 0) {
            if (shadowImage == null
                || shadowImage.getWidth() != width + 2 * blurRadius
                || shadowImage.getHeight() != height + 2 * blurRadius) {
                // Recreate the shadow
                BufferedImage rectangleImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D rectangleImageGraphics = rectangleImage.createGraphics();
                rectangleImageGraphics.setColor(Color.BLACK);
                rectangleImageGraphics.fillRect(0, 0, width, height);
                rectangleImageGraphics.dispose();
                shadowImage = createShadow(rectangleImage);
            }

            // Avoid drawing shadow if it will be covered by the component itself:
            Bounds paintBounds = new Bounds(0, 0, width, height);
            if (!component.isOpaque() || !paintBounds.contains(new Bounds(graphics.getClipBounds())))
                graphics.drawImage(shadowImage, xOffset - blurRadius, yOffset - blurRadius, null);
        } else {
            shadowImage = null;
        }

        return graphics;
    }

    @Override
    public void update() {
        // No-op
    }

    @Override
    public Bounds getBounds(Component component) {
        return new Bounds(xOffset - blurRadius, yOffset - blurRadius,
            component.getWidth() + blurRadius * 2,
            component.getHeight() + blurRadius * 2);
    }

    @Override
    public AffineTransform getTransform(Component component) {
        return new AffineTransform();
    }

    /**
     * Generates the shadow for a given picture and the current properties of
     * the decorator. The generated image dimensions are computed as follows:
     *
     * <pre>
     * width = imageWidth + 2 * blurRadius
     * height = imageHeight + 2 * blurRadius
     * </pre>
     *
     * @param src
     * The image from which the shadow will be cast.
     *
     * @return
     * An image containing the generated shadow.
     */
    private BufferedImage createShadow(BufferedImage src) {
        int shadowSize = blurRadius * 2;

        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();

        int dstWidth = srcWidth + shadowSize;
        int dstHeight = srcHeight + shadowSize;

        int left = blurRadius;
        int right = shadowSize - left;

        int yStop = dstHeight - right;

        int shadowRgb = shadowColor.getRGB() & 0x00FFFFFF;
        int[] aHistory = new int[shadowSize];
        int historyIdx;

        int aSum;

        Graphics2D srcGraphics = src.createGraphics();
        BufferedImage dst = srcGraphics.getDeviceConfiguration()
                .createCompatibleImage(dstWidth, dstHeight, Transparency.TRANSLUCENT);
        srcGraphics.dispose();

        int[] dstBuffer = new int[dstWidth * dstHeight];
        int[] srcBuffer = new int[srcWidth * srcHeight];

        Raster srcRaster = src.getRaster();
        srcRaster.getDataElements(0, 0, srcWidth, srcHeight, srcBuffer);

        int lastPixelOffset = right * dstWidth;
        float hSumDivider = 1.0f / shadowSize;
        float vSumDivider = shadowOpacity / shadowSize;

        int[] hSumLookup = new int[256 * shadowSize];
        for (int i = 0; i < hSumLookup.length; i++) {
            hSumLookup[i] = (int) (i * hSumDivider);
        }

        int[] vSumLookup = new int[256 * shadowSize];
        for (int i = 0; i < vSumLookup.length; i++) {
            vSumLookup[i] = (int) (i * vSumDivider);
        }

        int srcOffset;

        // Horizontal pass: extract the alpha mask from the source picture and
        // blur it into the destination picture
        for (int srcY = 0, dstOffset = left * dstWidth; srcY < srcHeight; srcY++) {
            // First pixels are empty
            for (historyIdx = 0; historyIdx < shadowSize;) {
                aHistory[historyIdx++] = 0;
            }

            aSum = 0;
            historyIdx = 0;
            srcOffset = srcY * srcWidth;

            // Compute the blur average with pixels from the source image
            for (int srcX = 0; srcX < srcWidth; srcX++) {
                int a = hSumLookup[aSum];
                // Store the alpha value only; the shadow color will be added
                // in the next pass
                dstBuffer[dstOffset++] = a << 24;

                // Subtract the oldest pixel from the sum
                aSum -= aHistory[historyIdx];

                // Extract the new pixel and store its value into history...
                a = srcBuffer[srcOffset + srcX] >>> 24;
                aHistory[historyIdx] = a;

                // ...and add its value to the sum
                aSum += a;

                if (++historyIdx >= shadowSize) {
                    historyIdx -= shadowSize;
                }
            }

            // Blur the end of the row - no new pixels to grab
            for (int i = 0; i < shadowSize; i++) {
                int a = hSumLookup[aSum];
                dstBuffer[dstOffset++] = a << 24;

                // Subtract the oldest pixel from the sum...and nothing new
                // to add!
                aSum -= aHistory[historyIdx];

                if (++historyIdx >= shadowSize) {
                    historyIdx -= shadowSize;
                }
            }
        }

        // Vertical pass
        for (int x = 0, bufferOffset = 0; x < dstWidth; x++, bufferOffset = x) {
            aSum = 0;

            // First pixels are empty...
            for (historyIdx = 0; historyIdx < left;) {
                aHistory[historyIdx++] = 0;
            }

            // ...and then they come from the dstBuffer
            for (int y = 0; y < right; y++, bufferOffset += dstWidth) {
                // Extract alpha and store into history...
                int a = dstBuffer[bufferOffset] >>> 24;
                aHistory[historyIdx++] = a;

                // ...and add to sum
                aSum += a;
            }

            bufferOffset = x;
            historyIdx = 0;

            // Compute the blur average with pixels from the previous pass
            for (int y = 0; y < yStop; y++, bufferOffset += dstWidth) {
                // Store alpha value + shadow color
                int a = vSumLookup[aSum];
                dstBuffer[bufferOffset] = a << 24 | shadowRgb;

                // Subtract the oldest pixel from the sum
                aSum -= aHistory[historyIdx];

                // Extract the new pixel and store its value into history...
                a = dstBuffer[bufferOffset + lastPixelOffset] >>> 24;
                aHistory[historyIdx] = a;

                // ... and add its value to the sum
                aSum += a;

                if (++historyIdx >= shadowSize) {
                    historyIdx -= shadowSize;
                }
            }

            // Blur the end of the column - no pixels to grab anymore
            for (int y = yStop; y < dstHeight; y++, bufferOffset += dstWidth) {
                int a = vSumLookup[aSum];
                dstBuffer[bufferOffset] = a << 24 | shadowRgb;

                // Subtract the oldest pixel from the sum
                aSum -= aHistory[historyIdx];

                if (++historyIdx >= shadowSize) {
                    historyIdx -= shadowSize;
                }
            }
        }

        WritableRaster dstRaster = dst.getRaster();
        dstRaster.setDataElements(0, 0, dstWidth, dstHeight, dstBuffer);

        return dst;
    }
}
