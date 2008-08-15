/*
 * Copyright (c) 2008 VMware, Inc.
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
package pivot.wtk.media;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import pivot.wtk.Dimensions;

public class Picture extends Image {
    private BufferedImage bufferedImage = null;

    private int width = 0;
    private int height = 0;

    protected Picture(BufferedImage image) {
        this.bufferedImage = image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getPreferredWidth(int height) {
        BufferedImage image = this.bufferedImage;

        int preferredWidth = 0;

        if (image != null) {
            if (height == -1) {
                preferredWidth = image.getWidth();
            }
            else {
                int imageWidth = image.getWidth();
                int imageHeight = image.getHeight();

                if (imageWidth > 0
                    && imageHeight > 0) {
                    preferredWidth = (int)Math.round(((double)height / (double)imageHeight)
                        * (double)imageWidth);
                }
            }
        }

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        BufferedImage image = this.bufferedImage;

        int preferredHeight = 0;

        if (image != null) {
            if (width == -1) {
                preferredHeight = image.getHeight();
            }
            else {
                int imageWidth = image.getWidth();
                int imageHeight = image.getHeight();

                if (imageWidth > 0
                    && imageHeight > 0) {
                    preferredHeight = (int)Math.round(((double)width / (double)imageWidth)
                        * (double)imageHeight);
                }
            }
        }

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        BufferedImage image = this.bufferedImage;

        return (image == null) ?
            new Dimensions(0, 0) : new Dimensions(image.getWidth(),
                image.getHeight());
    }

    public void paint(Graphics2D graphics) {
        graphics.drawImage(bufferedImage, 0, 0, width, height, null);
    }

    public Graphics2D getGraphics() {
        return bufferedImage.createGraphics();
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }
}
