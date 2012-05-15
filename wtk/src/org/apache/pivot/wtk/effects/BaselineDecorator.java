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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Orientation;

/**
 * Displays a component's baseline.
 */
public class BaselineDecorator implements Decorator {
    private Component component = null;
    private Graphics2D graphics = null;

    @Override
    public Graphics2D prepare(Component componentArgument, Graphics2D graphicsArgument) {
        this.component = componentArgument;
        this.graphics = graphicsArgument;

        return graphicsArgument;
    }

    @Override
    public void update() {
        int width = component.getWidth();
        int height = component.getHeight();
        int baseline = component.getBaseline(width, height);

        int y;
        Color color;
        if (baseline == -1) {
            y = height / 2;
            color = Color.BLUE;
        } else {
            y = baseline;
            color = Color.RED;
        }

        graphics.setPaint(color);
        GraphicsUtilities.drawLine(graphics, 0, y, width, Orientation.HORIZONTAL);

        component = null;
        graphics = null;
    }

    @Override
    public Bounds getBounds(Component componentArgument) {
        return new Bounds(0, 0, componentArgument.getWidth(), componentArgument.getHeight());
    }

    @Override
    public AffineTransform getTransform(Component componentArgument) {
        return new AffineTransform();
    }

}
