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

import pivot.wtk.Component;
import pivot.wtk.Decorator;

/**
 * Decorator that applies a grayscale conversion to all paint operations.
 *
 * @author tvolkert
 */
public class GrayscaleDecorator implements Decorator {
    private BufferedImage bufferedImage = null;
    private Graphics2D graphics = null;

    public Graphics2D prepare(Component component, Graphics2D graphics) {
        this.graphics = graphics;

        bufferedImage = new BufferedImage(component.getWidth(), component.getHeight(),
            BufferedImage.TYPE_BYTE_GRAY);
        graphics = bufferedImage.createGraphics();
        graphics.setClip(this.graphics.getClip());

        return graphics;
    }

    public void update() {
        // Draw the blurred image to the real graphics
        graphics.drawImage(bufferedImage, 0, 0, null);

        // We redirected the component's graphics to the buffered image
        // graphics, so we dispose of the original graphics ourselves
        graphics.dispose();
    }
}
