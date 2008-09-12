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
import java.awt.RenderingHints;

import pivot.wtk.Bounds;
import pivot.wtk.Component;
import pivot.wtk.Container;
import pivot.wtk.Decorator;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.VerticalAlignment;

/**
 * <p>Decorator that scales the painting of a component along the X and/or Y
 * axes.</p>
 *
 * @author tvolkert
 */
public class ScaleDecorator implements Decorator {
    private float scaleX;
    private float scaleY;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
    private VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;

    /**
     * Creates a new <tt>ScaleDecorator</tt> with the default <tt>scaleX</tt>
     * <tt>scaleY</tt> values of <tt>1</tt>.
     */
    public ScaleDecorator() {
        this(1f, 1f);
    }

    /**
     * Creates a new <tt>ScaleDecorator</tt> with the specified <tt>scaleX</tt>
     * and <tt>scaleY</tt> values.
     *
     * @param scaleX
     * The amount to scale the component's x-axis
     *
     * @param scaleY
     * The amount to scale the component's y-axis
     */
    public ScaleDecorator(float scaleX, float scaleY) {
        if (scaleX <= 0
            || scaleY <= 0) {
            throw new IllegalArgumentException("Scale values must be positive.");
        }

        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    /**
     * Gets the amount by which drawing operations will be scaled along the
     * x-axis.
     *
     * @return
     * The amount to scale the component's x-axis
     */
    public float getScaleX() {
        return scaleX;
    }

    /**
     * Sets the amount by which drawing operations will be scaled along the
     * x-axis.
     *
     * @param scaleX
     * The amount to scale the component's x-axis
     */
    public void setScaleX(float scaleX) {
        if (scaleX <= 0) {
            throw new IllegalArgumentException("scaleX must be positive.");
        }

        this.scaleX = scaleX;
    }

    /**
     * Sets the amount by which drawing operations will be scaled along the
     * x-axis.
     *
     * @param scaleX
     * The amount to scale the component's x-axis
     */
    public void setScaleX(Number scaleX) {
        if (scaleX == null) {
            throw new IllegalArgumentException("scaleX is null.");
        }

        setScaleX(scaleX.floatValue());
    }

    /**
     * Gets the amount by which drawing operations will be scaled along the
     * y-axis.
     *
     * @return
     * The amount to scale the component's y-axis
     */
    public float getScaleY() {
        return scaleY;
    }

    /**
     * Sets the amount by which drawing operations will be scaled along the
     * y-axis.
     *
     * @param scaleY
     * The amount to scale the component's y-axis
     */
    public void setScaleY(float scaleY) {
        if (scaleY <= 0) {
            throw new IllegalArgumentException("scaleY must be positive.");
        }

        this.scaleY = scaleY;
    }

    /**
     * Sets the amount by which drawing operations will be scaled along the
     * y-axis.
     *
     * @param scaleY
     * The amount to scale the component's y-axis
     */
    public void setScaleY(Number scaleY) {
        if (scaleY == null) {
            throw new IllegalArgumentException("scaleY is null.");
        }

        setScaleY(scaleY.floatValue());
    }

    /**
     * Gets the horizontal alignment of the decorator. A left alignment will
     * paint the component's left edge at the component's x-coordinate. A right
     * alignment will paint the component's right edge along the right side
     * of the component's bounding box. A center or justified alignment will
     * paint the scaled component centered with respect to the component's
     * bounding box.
     *
     * @return
     * The horizontal alignment
     */
    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * Sets the horizontal alignment of the decorator. A left alignment will
     * paint the component's left edge at the component's x-coordinate. A right
     * alignment will paint the component's right edge along the right side
     * of the component's bounding box. A center or justified alignment will
     * paint the scaled component centered with respect to the component's
     * bounding box.
     *
     * @param horizontalAlignment
     * The horizontal alignment
     */
    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        if (horizontalAlignment == null) {
            throw new IllegalArgumentException("horizontalAlignment is null.");
        }

        this.horizontalAlignment = horizontalAlignment;
    }

    /**
     * Sets the horizontal alignment of the decorator. A left alignment will
     * paint the component's left edge at the component's x-coordinate. A right
     * alignment will paint the component's right edge along the right side
     * of the component's bounding box. A center or justified alignment will
     * paint the scaled component centered with respect to the component's
     * bounding box.
     *
     * @param horizontalAlignment
     * The horizontal alignment
     */
    public final void setHorizontalAlignment(String horizontalAlignment) {
        if (horizontalAlignment == null) {
            throw new IllegalArgumentException("horizontalAlignment is null.");
        }

        setHorizontalAlignment(HorizontalAlignment.decode(horizontalAlignment));
    }

    /**
     * Gets the vertical alignment of the decorator. A top alignment will
     * paint the component's top edge at the component's y-coordinate. A bottom
     * alignment will paint the component's bottom edge along the bottom side
     * of the component's bounding box. A center or justified alignment will
     * paint the scaled component centered with respect to the component's
     * bounding box.
     *
     * @return
     * The vertical alignment
     */
    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     * Sets the vertical alignment of the decorator. A top alignment will
     * paint the component's top edge at the component's y-coordinate. A bottom
     * alignment will paint the component's bottom edge along the bottom side
     * of the component's bounding box. A center or justified alignment will
     * paint the scaled component centered with respect to the component's
     * bounding box.
     *
     * @param verticalAlignment
     * The vertical alignment
     */
    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        if (verticalAlignment == null) {
            throw new IllegalArgumentException("verticalAlignment is null.");
        }

        this.verticalAlignment = verticalAlignment;
    }

    /**
     * Sets the vertical alignment of the decorator. A top alignment will
     * paint the component's top edge at the component's y-coordinate. A bottom
     * alignment will paint the component's bottom edge along the bottom side
     * of the component's bounding box. A center or justified alignment will
     * paint the scaled component centered with respect to the component's
     * bounding box.
     *
     * @param verticalAlignment
     * The vertical alignment
     */
    public final void setVerticalAlignment(String verticalAlignment) {
        if (verticalAlignment == null) {
            throw new IllegalArgumentException("verticalAlignment is null.");
        }

        setVerticalAlignment(VerticalAlignment.decode(verticalAlignment));
    }

    /**
     * Gets the x translation that will be applied with respect to the
     * specified component, given this decorator's <tt>scaleX</tt> and
     * <tt>horizontalAlignment</tt> properties.
     *
     * @param component
     * The component being decorated
     *
     * @return
     * The amount to translate x-coordinate actions when decorating this
     * component
     */
    private int getTranslatedX(Component component) {
        int width = component.getWidth();
        int translatedWidth = (int)Math.ceil(width * scaleX);

        int tx;

        switch (horizontalAlignment) {
        case LEFT:
            tx = 0;
            break;
        case RIGHT:
            tx = width - translatedWidth;
            break;
        default:
            tx = (width - translatedWidth) / 2;
            break;
        }

        return tx;
    }

    /**
     * Gets the y translation that will be applied with respect to the
     * specified component, given this decorator's <tt>scaleY</tt> and
     * <tt>verticalAlignment</tt> properties.
     *
     * @param component
     * The component being decorated
     *
     * @return
     * The amount to translate y-coordinate actions when decorating this
     * component
     */
    private int getTranslatedY(Component component) {
        int height = component.getHeight();
        int translatedHeight = (int)Math.ceil(height * scaleY);

        int ty;

        switch (verticalAlignment) {
        case TOP:
            ty = 0;
            break;
        case BOTTOM:
            ty = height - translatedHeight;
            break;
        default:
            ty = (height - translatedHeight) / 2;
            break;
        }

        return ty;
    }

    public Graphics2D prepare(Component component, Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int tx = getTranslatedX(component);
        int ty = getTranslatedY(component);

        if (tx != 0 || ty != 0) {
            graphics.translate(tx, ty);
        }

        graphics.scale(scaleX, scaleY);

        return graphics;
    }

    public void update() {
        // No-op
    }

    public Bounds getBounds(Component component) {
        int width = (int)Math.ceil(component.getWidth() * scaleX);
        int height = (int)Math.ceil(component.getHeight() * scaleY);

        int tx = getTranslatedX(component);
        int ty = getTranslatedY(component);

        return new Bounds(tx, ty, width, height);
    }

    public void repaint(Component component, int x, int y, int width, int height) {
        Container parent = component.getParent();

        if (parent != null) {
            int tx = getTranslatedX(component);
            int ty = getTranslatedY(component);

            x = (int)((x * scaleX) + component.getX() + tx);
            y = (int)((y * scaleY) + component.getY() + ty);
            width = (int)Math.ceil(width * scaleX);
            height = (int)Math.ceil(height * scaleY);

            parent.repaint(x, y, width, height);
        }
    }

    public Bounds getAffectedArea(Component component, int x, int y, int width, int height) {
        // TODO
        return new Bounds(x, y, width, height);
    }
}
