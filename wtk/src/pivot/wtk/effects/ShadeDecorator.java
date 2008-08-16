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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

public class ShadeDecorator extends AbstractDecorator {
    private float opacity;
    private Color color;

    /**
     * Creates a new <tt>ShadeDecorator</tt> with the default opacity and
     * shade color.
     */
    public ShadeDecorator() {
        this(0.33f, Color.BLACK);
    }

    /**
     * Creates a new <tt>ShadeDecorator</tt> with the specified opacity and
     * shade color.
     *
     * @param opacity
     * The opacity of the shade, between 0 and 1, exclusive.
     * @param color
     * The color of the shade.
     */
    public ShadeDecorator(float opacity, Color color) {
        if (opacity <= 0 || opacity >= 1) {
            throw new IllegalArgumentException("opacity must be between 0 and 1, exclusive.");
        }

        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.opacity = opacity;
        this.color = color;
    }

    public void paint(Graphics2D graphics) {
        visual.paint(graphics);

        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        graphics.setColor(color);
        graphics.fillRect(0, 0, visual.getWidth(), visual.getHeight());
    }
}
