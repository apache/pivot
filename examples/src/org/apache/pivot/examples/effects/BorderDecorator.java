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
package org.apache.pivot.examples.effects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.effects.Decorator;

public class BorderDecorator implements Decorator {
    @Override
    public Graphics2D prepare(Component component, Graphics2D graphics) {
        graphics.setColor(Color.RED);
        graphics.setStroke(new BasicStroke(1));
        graphics.draw(new Rectangle2D.Double(-1, -1, component.getWidth() + 1,
            component.getHeight() + 1));
        return graphics;
    }

    @Override
    public void update() {
        // No-op
    }

    @Override
    public Bounds getBounds(Component component) {
        return new Bounds(-1, -1, component.getWidth() + 2, component.getHeight() + 2);
    }

    @Override
    public AffineTransform getTransform(Component component) {
        return new AffineTransform();
    }
}
