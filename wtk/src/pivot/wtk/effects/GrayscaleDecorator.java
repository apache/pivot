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
package pivot.wtk.effects;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Decorator that applies a grayscale conversion to all paint operations.
 *
 * @author tvolkert
 */
public class GrayscaleDecorator extends AbstractDecorator {
    private BufferedImage bufferedImage = null;

    public void paint(Graphics2D graphics) {
        int width = visual.getWidth();
        int height = visual.getHeight();

        if (bufferedImage == null
            || bufferedImage.getWidth() != width
            || bufferedImage.getHeight() != height) {
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        }

        // Paint the visual to the buffer
        Graphics2D bufferedImageGraphics = (Graphics2D)bufferedImage.createGraphics();
        bufferedImageGraphics.setClip(graphics.getClip());
        visual.paint(bufferedImageGraphics);

        // Dispose of the buffered image graphics
        bufferedImageGraphics.dispose();

        // Draw the grayscale image to the real graphics
        bufferedImage.flush();
        graphics.drawImage(bufferedImage, 0, 0, null);
    }
}
