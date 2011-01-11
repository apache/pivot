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
package org.apache.pivot.scene;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.apache.pivot.scene.Color;
import org.apache.pivot.scene.Graphics;
import org.apache.pivot.scene.media.Raster;

/**
 * AWT raster implementation.
 */
public class AWTRaster extends Raster {
    private BufferedImage bufferedImage;

    public AWTRaster(BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            throw new IllegalArgumentException();
        }

        this.bufferedImage = bufferedImage;
    }

    @Override
    public int getWidth() {
        return bufferedImage.getWidth();
    }

    @Override
    public int getHeight() {
        return bufferedImage.getHeight();
    }

    @Override
    public Color getPixel(int x, int y) {
        return new Color(bufferedImage.getRGB(x, y));
    }

    @Override
    public void setPixel(int x, int y, Color color) {
        bufferedImage.setRGB(x, y, color.toInt());
    }

    @Override
    public Graphics getGraphics() {
        Graphics2D graphics2D = (Graphics2D)bufferedImage.getGraphics();
        graphics2D.clipRect(0, 0, getWidth(), getHeight());

        return new AWTGraphics(graphics2D);
    }

    @Override
    public BufferedImage getNativeRaster() {
        return bufferedImage;
    }
}
