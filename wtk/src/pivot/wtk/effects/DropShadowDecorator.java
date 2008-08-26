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
import pivot.wtk.Rectangle;

/**
 * Adds drop shadows to components.
 *
 * @author gbrown
 * @author tvolkert
 * @author eryzhikov
 * @author Romain Guy
 * @author Sebastien Petrucci
 */
public class DropShadowDecorator implements Decorator {
    private Color shadowColor = Color.BLACK;
    private float shadowOpacity = 0.33f;
    private int blurRadius = 5;

    private Component component = null;
    private Graphics2D graphics = null;

    private BufferedImage componentImage = null;
    private Graphics2D componentGraphics = null;

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
    public float getBlurRadius() {
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

    public void update() {
        BufferedImage shadowImage = createShadow(componentImage);

        graphics.drawImage(shadowImage, 0, 0, null);
        graphics.drawImage(componentImage, 0, 0, null);
    }

    public Rectangle getBounds(Component component) {
        return new Rectangle(0, 0, component.getWidth() + blurRadius * 2,
            component.getHeight() + blurRadius * 2);
    }

    public void repaint(Component component, int x, int y, int width, int height) {
        // No-op
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
     * @param image
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

        BufferedImage dst = new BufferedImage(dstWidth, dstHeight,
            BufferedImage.TYPE_INT_ARGB);

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

                // Substract the oldest pixel from the sum
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

                // Substract the oldest pixel from the sum...and nothing new
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

            // Compute the blur avera`ge with pixels from the previous pass
            for (int y = 0; y < yStop; y++, bufferOffset += dstWidth) {
                // Store alpha value + shadow color
                int a = vSumLookup[aSum];
                dstBuffer[bufferOffset] = a << 24 | shadowRgb;

                // Substract the oldest pixel from the sum
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

                // Substract the oldest pixel from the sum
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
