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

/**
 * Image representing a bitmapped picture.
 *
 * @author gbrown
 */
public class Picture extends Image {
    private BufferedImage bufferedImage = null;

    public Picture(BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            throw new IllegalArgumentException("bufferedImage is null.");
        }

        this.bufferedImage = bufferedImage;
    }

    public int getWidth() {
        return bufferedImage.getWidth();
    }

    public int getHeight() {
        return bufferedImage.getHeight();
    }

    public void paint(Graphics2D graphics) {
        graphics.drawImage(bufferedImage, 0, 0, null);
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }
}
