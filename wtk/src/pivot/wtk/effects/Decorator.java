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
import java.awt.geom.AffineTransform;

import pivot.wtk.Bounds;
import pivot.wtk.Component;

/**
 * Interface defining a component "decorator". Decorators allow a caller to
 * attach additional visual effects to a component.
 *
 * @author gbrown
 */
public interface Decorator {
    /**
     * Prepares the graphics context into which the component or prior
     * decorator will paint. This method is called immediately prior to
     * {@link Component#paint(Graphics2D)}; decorators are called in
     * descending order.
     *
     * @param component
     * @param graphics
     *
     * @return
     * The graphics context that should be used by the component or prior
     * decorators.
     */
    public Graphics2D prepare(Component component, Graphics2D graphics);

    /**
     * Updates the graphics context into which the component or prior
     * decorator was painted. This method is called immediately after
     * {@link Component#paint(Graphics2D)}; decorators are called in
     * ascending order.
     */
    public void update();

    /**
     * Returns the bounding area of the decorator.
     *
     * @param component
     *
     * @return
     * The decorator's bounds, relative to the component's origin.
     */
    public Bounds getBounds(Component component);

    /**
     * Returns the transformation the decorator applies to the component's
     * coordinate space.
     *
     * @return
     * The decorator's transform.
     */
    public AffineTransform getTransform(Component component);
}
