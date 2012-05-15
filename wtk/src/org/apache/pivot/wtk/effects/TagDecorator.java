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
package org.apache.pivot.wtk.effects;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.Visual;


/**
 * Decorator that allows a caller to attach a "tag" visual to a component.
 */
public class TagDecorator implements Decorator {
    private Visual tag;
    private HorizontalAlignment horizontalAlignment;
    private VerticalAlignment verticalAlignment;
    private int xOffset;
    private int yOffset;

    private Graphics2D graphics = null;
    private Bounds bounds = null;

    public TagDecorator() {
        this(null);
    }

    public TagDecorator(Visual tag) {
        this(tag, HorizontalAlignment.RIGHT, VerticalAlignment.TOP, 0, 0);
    }

    public TagDecorator(Visual tag,
        HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment,
        int xOffset, int yOffset) {
        if (horizontalAlignment == null) {
            throw new IllegalArgumentException("horizontalAlignment is null.");
        }

        if (verticalAlignment == null) {
            throw new IllegalArgumentException("verticalAlignment is null.");
        }

        this.tag = tag;
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public Visual getTag() {
        return tag;
    }

    public void setTag(Visual tag) {
        this.tag = tag;
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        if (horizontalAlignment == null) {
            throw new IllegalArgumentException("horizontalAlignment is null.");
        }

        this.horizontalAlignment = horizontalAlignment;
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        if (verticalAlignment == null) {
            throw new IllegalArgumentException("verticalAlignment is null.");
        }

        this.verticalAlignment = verticalAlignment;
    }

    public int getXOffset() {
        return xOffset;
    }

    public void setXOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public void setYOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    @Override
    public Graphics2D prepare(Component component, Graphics2D graphicsArgument) {
        if (tag != null) {
            bounds = getBounds(component);
            this.graphics = graphicsArgument;
        }

        return graphicsArgument;
    }

    @Override
    public void update() {
        if (tag != null) {
            graphics.translate(bounds.x, bounds.y);
            tag.paint(graphics);
        }

        graphics = null;
    }

    @Override
    public Bounds getBounds(Component component) {
        Bounds boundsLocal;

        if (tag == null) {
            boundsLocal = null;
        } else {
            int x, y;

            switch (horizontalAlignment) {
                case LEFT: {
                    x = xOffset;
                    break;
                }

                case RIGHT: {
                    x = component.getWidth() - tag.getWidth() + xOffset;
                    break;
                }

                case CENTER: {
                    x = (component.getWidth() - tag.getWidth()) / 2 + xOffset;
                    break;
                }

                default: {
                    throw new UnsupportedOperationException();
                }
            }

            switch (verticalAlignment) {
                case TOP: {
                    y = yOffset;
                    break;
                }

                case BOTTOM: {
                    y = component.getHeight() - tag.getHeight() + yOffset;
                    break;
                }

                case CENTER: {
                    y = (component.getHeight() - tag.getHeight()) / 2 + yOffset;
                    break;
                }

                default: {
                    throw new UnsupportedOperationException();
                }
            }

            boundsLocal = new Bounds(x, y, tag.getWidth(), tag.getHeight());
        }

        return boundsLocal;
    }

    @Override
    public AffineTransform getTransform(Component component) {
        return new AffineTransform();
    }
}
