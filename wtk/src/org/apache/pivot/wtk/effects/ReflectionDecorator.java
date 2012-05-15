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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;


/**
 * Decorator that paints a reflection of a component.
 * <p>
 * TODO Make gradient properties configurable.
 */
public class ReflectionDecorator implements Decorator {
    private Component component = null;
    private Graphics2D graphics = null;

    private BufferedImage componentImage = null;
    private Graphics2D componentImageGraphics = null;

    @Override
    public Graphics2D prepare(Component componentArgument, Graphics2D graphicsArgument) {
        this.component = componentArgument;
        this.graphics = graphicsArgument;

        int width = componentArgument.getWidth();
        int height = componentArgument.getHeight();

        componentImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        componentImageGraphics = componentImage.createGraphics();

        // Clear the image background
        componentImageGraphics.setComposite(AlphaComposite.Clear);
        componentImageGraphics.fillRect(0, 0, componentImage.getWidth(), componentImage.getHeight());

        componentImageGraphics.setComposite(AlphaComposite.SrcOver);

        return componentImageGraphics;
    }

    @Override
    public void update() {
        // Draw the component
        graphics.drawImage(componentImage, 0, 0, null);

        // Draw the reflection
        int width = componentImage.getWidth();
        int height = componentImage.getHeight();

        GradientPaint mask = new GradientPaint(0, height / 4f, new Color(1.0f, 1.0f, 1.0f, 0.0f),
            0, height, new Color(1.0f, 1.0f, 1.0f, 0.5f));
        componentImageGraphics.setPaint(mask);

        componentImageGraphics.setComposite(AlphaComposite.DstIn);
        componentImageGraphics.fillRect(0, 0, width, height);

        componentImageGraphics.dispose();
        componentImageGraphics = null;

        componentImage.flush();

        graphics.transform(getTransform(component));

        graphics.drawImage(componentImage, 0, 0, null);

        componentImage = null;
        component = null;
        graphics = null;
    }

    @Override
    public Bounds getBounds(Component componentArgument) {
        return new Bounds(0, 0, componentArgument.getWidth(), componentArgument.getHeight() * 2);
    }

    @Override
    public AffineTransform getTransform(Component componentArgument) {
        AffineTransform transform = AffineTransform.getScaleInstance(1.0, -1.0);
        transform.translate(0, -(componentArgument.getHeight() * 2));

        return transform;
    }

}
