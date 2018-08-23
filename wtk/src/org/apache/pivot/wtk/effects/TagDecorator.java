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

import org.apache.pivot.util.Utils;
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

    public TagDecorator(final Visual tag) {
        this(tag, HorizontalAlignment.RIGHT, VerticalAlignment.TOP, 0, 0);
    }

    public TagDecorator(final Visual tag, final HorizontalAlignment horizontalAlignment,
        final VerticalAlignment verticalAlignment, final int xOffset, final int yOffset) {
        Utils.checkNull(horizontalAlignment, "horizontalAlignment");
        Utils.checkNull(verticalAlignment, "verticalAlignment");

        this.tag = tag;
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public Visual getTag() {
        return tag;
    }

    public void setTag(final Visual tag) {
        this.tag = tag;
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(final HorizontalAlignment horizontalAlignment) {
        Utils.checkNull(horizontalAlignment, "horizontalAlignment");

        this.horizontalAlignment = horizontalAlignment;
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(final VerticalAlignment verticalAlignment) {
        Utils.checkNull(verticalAlignment, "verticalAlignment");

        this.verticalAlignment = verticalAlignment;
    }

    public int getXOffset() {
        return xOffset;
    }

    public void setXOffset(final int xOffset) {
        this.xOffset = xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public void setYOffset(final int yOffset) {
        this.yOffset = yOffset;
    }

    @Override
    public Graphics2D prepare(final Component component, final Graphics2D graphicsArgument) {
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
    public Bounds getBounds(final Component component) {
        Bounds localBounds = null;

        if (tag != null) {
            int x = 0, y = 0;
            int tagWidth = tag.getWidth();
            int tagHeight = tag.getHeight();

            switch (horizontalAlignment) {
                case LEFT:
                    break;

                case RIGHT:
                    x = component.getWidth() - tagWidth;
                    break;

                case CENTER:
                    x = (component.getWidth() - tagWidth) / 2;
                    break;

                default:
                    throw new UnsupportedOperationException();
            }
            x += xOffset;

            switch (verticalAlignment) {
                case TOP:
                    break;

                case BOTTOM:
                    y = component.getHeight() - tagHeight;
                    break;

                case CENTER:
                    y = (component.getHeight() - tagHeight) / 2;
                    break;

                default:
                    throw new UnsupportedOperationException();
            }
            y += yOffset;

            localBounds = new Bounds(x, y, tagWidth, tagHeight);
        }

        return localBounds;
    }

}
