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
package org.apache.pivot.wtk.media;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

/**
 * Image representing a bitmapped picture.
 */
public class Picture extends Image {
    /**
     * Enum defing the algorithms to apply when resizing a picture.
     */
    public enum Interpolation {
        NEAREST_NEIGHBOR,
        BILINEAR,
        BICUBIC
    }

    private BufferedImage bufferedImage = null;

    private int baseline = -1;

    public Picture(BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            throw new IllegalArgumentException("bufferedImage is null.");
        }

        this.bufferedImage = bufferedImage;
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    @Override
    public int getWidth() {
        return bufferedImage.getWidth();
    }

    @Override
    public int getHeight() {
        return bufferedImage.getHeight();
    }

    public void resample(int size) {
        resample(size, Interpolation.NEAREST_NEIGHBOR);
    }

    public void resample(int size, Interpolation interpolation) {
        int width = getWidth();
        int height = getHeight();

        float aspectRatio = (float)width / (float)height;
        if (aspectRatio > 1) {
            width = size;
            height = (int)(size / aspectRatio);
        } else {
            width = (int)(size * aspectRatio);
            height = size;
        }

        resample(width, height, interpolation);
    }

    public void resample(int width, int height) {
        resample(width, height, Interpolation.NEAREST_NEIGHBOR);
    }

    public void resample(int width, int height, Interpolation interpolation) {
        if (interpolation == null) {
            throw new IllegalArgumentException("interpolation is null.");
        }

        int previousWidth = getWidth();
        int previousHeight = getHeight();

        if (previousWidth != width
            || previousHeight != height) {
            int type = bufferedImage.getType();

            float scaleX = ((float)width / (float)previousWidth);
            float scaleY = ((float)height / (float)previousHeight);

            java.awt.image.BufferedImage bufferedImageLocal = new BufferedImage(width, height, type);
            Graphics2D bufferedImageGraphics = (Graphics2D)bufferedImageLocal.getGraphics();

            // Clear the background
            if (this.bufferedImage.getTransparency() != Transparency.OPAQUE) {
                bufferedImageGraphics.setComposite(AlphaComposite.Clear);
                bufferedImageGraphics.fillRect(0, 0, width, height);

                bufferedImageGraphics.setComposite(AlphaComposite.SrcOver);
            }

            // Set the interpolation
            Object interpolationHint = 0;
            switch (interpolation) {
                case NEAREST_NEIGHBOR: {
                    interpolationHint = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                    break;
                }

                case BILINEAR: {
                    interpolationHint = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
                    break;
                }

                case BICUBIC: {
                    interpolationHint = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
                    break;
                }
            }

            bufferedImageGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationHint);

            // Draw the image
            bufferedImageGraphics.scale(scaleX, scaleY);
            paint(bufferedImageGraphics);

            bufferedImageGraphics.dispose();

            // Set the scaled image as the new instance
            this.bufferedImage = bufferedImageLocal;

            imageListeners.sizeChanged(this, previousWidth, previousHeight);
        }
    }

    @Override
    public int getBaseline() {
        return baseline;
    }

    public void setBaseline(int baseline) {
        int previousBaseline = this.baseline;

        if (baseline != previousBaseline) {
            this.baseline = baseline;
            imageListeners.baselineChanged(this, previousBaseline);
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        graphics.drawImage(bufferedImage, 0, 0, null);
    }
}
