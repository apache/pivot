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

/**
 * Decorator that rotates a component about its center.
 */
public class RotationDecorator implements Decorator {
    private double theta;

    /**
     * Creates a new rotation decorator with the default theta value (zero).
     */
    public RotationDecorator() {
        this(0d);
    }

    /**
     * Creates a new rotation decorator with the specified theta value.
     *
     * @param theta
     * The rotation angle, in radians.
     */
    public RotationDecorator(double theta) {
        setTheta(theta);
    }

    /**
     * Gets the rotation angle, in radians.
     */
    public double getTheta() {
        return theta;
    }

    /**
     * Sets the rotation angle, in radians.
     */
    public void setTheta(double theta) {
        this.theta = theta;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Graphics2D prepare(Component component, Graphics2D graphics) {
        graphics.rotate(theta, component.getWidth() * 0.5, component.getHeight() * 0.5);
        return graphics;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update() {
        // No-op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bounds getBounds(Component component) {
        int width = component.getWidth();
        int height = component.getHeight();

        double sinTheta = Math.abs(Math.sin(theta));
        double cosTheta = Math.abs(Math.cos(theta));

        int transformedWidth = (int)Math.ceil((height * sinTheta) + (width * cosTheta));
        int transformedHeight = (int)Math.ceil((height * cosTheta) + (width * sinTheta));
        int transformedX = (int)Math.floor((width - transformedWidth) * 0.5);
        int transformedY = (int)Math.floor((height - transformedHeight) * 0.5);

        return new Bounds(transformedX, transformedY, transformedWidth, transformedHeight);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AffineTransform getTransform(Component component) {
        return AffineTransform.getRotateInstance(theta, component.getWidth() * 0.5,
            component.getHeight() * 0.5);
    }
}
