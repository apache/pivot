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
package pivot.wtk.skin;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import pivot.wtk.Orientation;

/**
 * Contains utility methods dealing with the Java2D API.
 *
 * @author tvolkert
 */
public final class GraphicsUtilities {
    private GraphicsUtilities() {
    }

    public static final void drawLine(final Graphics2D graphics, final int x, final int y,
        final int length, final Orientation orientation) {
        drawLine(graphics, x, y, length, orientation, 1);
    }

    public static final void drawLine(final Graphics2D graphics, final int x, final int y,
        final int length, final Orientation orientation, final int thickness) {
        if (length > 0 && thickness > 0) {
            switch (orientation) {
            case HORIZONTAL:
                graphics.fillRect(x, y, length, thickness);
                break;

            case VERTICAL:
                graphics.fillRect(x, y, thickness, length);
                break;
            }
        }
    }

    /**
     * Draws a rectangle with a thickness of 1 pixel at the specified
     * coordinates whose <u>outer border</u> is the specified width and height.
     * In other words, the distance from the left edge of the leftmost pixel to
     * the left edge of the rightmost pixel is <tt>width - 1</tt>.
     * <p>
     * This method provides more reliable pixel rounding behavior than
     * <tt>java.awt.Graphics#drawRect</tt> when scaling is applied because this
     * method does not stroke the shape but instead explicitly fills the
     * desired pixels with the graphics context's paint. For this reason, and
     * because Pivot supports scaling the display host, it is recommended that
     * skins use this method over <tt>java.awt.Graphics#drawRect</tt>.
     *
     * @param graphics
     * The graphics context that will be used to perform the operation.
     *
     * @param x
     * The x-coordinate of the upper-left corner of the rectangle.
     *
     * @param y
     * The y-coordinate of the upper-left corner of the rectangle.
     *
     * @param width
     * The <i>outer width</i> of the rectangle.
     *
     * @param height
     * The <i>outer height</i> of the rectangle.
     */
    public static final void drawRect(final Graphics2D graphics, final int x, final int y,
        final int width, final int height) {
        drawRect(graphics, x, y, width, height, 1);
    }

    /**
     * Draws a rectangle with the specified thickness at the specified
     * coordinates whose <u>outer border</u> is the specified width and height.
     * In other words, the distance from the left edge of the leftmost pixel to
     * the left edge of the rightmost pixel is <tt>width - thickness</tt>.
     * <p>
     * This method provides more reliable pixel rounding behavior than
     * <tt>java.awt.Graphics#drawRect</tt> when scaling is applied because this
     * method does not stroke the shape but instead explicitly fills the
     * desired pixels with the graphics context's paint. For this reason, and
     * because Pivot supports scaling the display host, it is recommended that
     * skins use this method over <tt>java.awt.Graphics#drawRect</tt>.
     *
     * @param graphics
     * The graphics context that will be used to perform the operation.
     *
     * @param x
     * The x-coordinate of the upper-left corner of the rectangle.
     *
     * @param y
     * The y-coordinate of the upper-left corner of the rectangle.
     *
     * @param width
     * The <i>outer width</i> of the rectangle.
     *
     * @param height
     * The <i>outer height</i> of the rectangle.
     *
     * @param thickness
     * The thickness of each edge.
     */
    public static final void drawRect(final Graphics2D graphics, final int x, final int y,
        final int width, final int height, final int thickness) {
        Graphics2D rectGraphics = graphics;

        if ((graphics.getTransform().getType() & AffineTransform.TYPE_MASK_SCALE) != 0) {
            rectGraphics = (Graphics2D)graphics.create();
            rectGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        }

        if (width > 0 && height > 0 && thickness > 0) {
            drawLine(rectGraphics, x, y, width, Orientation.HORIZONTAL, thickness);
            drawLine(rectGraphics, x + width - thickness, y, height, Orientation.VERTICAL, thickness);
            drawLine(rectGraphics, x, y + height - thickness, width, Orientation.HORIZONTAL, thickness);
            drawLine(rectGraphics, x, y, height, Orientation.VERTICAL, thickness);
        }

        if (rectGraphics != graphics) {
            rectGraphics.dispose();
        }
    }
}
