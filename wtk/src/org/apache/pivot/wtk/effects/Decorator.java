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
 * Interface defining a component "decorator". Decorators allow a caller to
 * attach additional visual effects to a component.
 * <p>
 * Decorators use a chained prepare/update model to modify the graphics in
 * which a component is painted. The <tt>prepare()</tt> method of each decorator
 * in a component's decorator sequence is called in reverse order before the
 * component's <tt>paint()</tt> method is called. <tt>prepare()</tt> returns
 * an instance of <tt>Graphics2D</tt> that is passed to prior decorators,
 * and ultimately to the component itself. This allows decorators to modify the
 * graphics context before it reaches the component. After the component has
 * been painted, each decorator's <tt>update()</tt> method is then called in
 * order to allow the decorator to further modify the resulting output.
 * <p>
 * Decorators are not restricted to painting within the component's bounds.
 * However, they are clipped to the bounds of the component's parent. They are
 * not clipped to their own bounds because, due to the chained painting model,
 * it is not safe assume that a clip applied to a given decorator during the
 * prepare phase will be valid for subsequent updates, or even for painting the
 * component itself; though a component paints into a copy of the final prepared
 * graphics, it must still be clipped to the intersection of its own bounds and
 * the current clip (not the intersections of all preceding decorator prepare()
 * calls).
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
