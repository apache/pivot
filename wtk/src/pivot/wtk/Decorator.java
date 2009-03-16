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
package pivot.wtk;

import java.awt.Graphics2D;

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
     * Returns the bounds of the area affected by a change to a given region
     * within a component.
     *
     * @param component
     * @param x
     * @param y
     * @param width
     * @param height
     *
     * @return
     * The bounds of the affected area, relative to the component's
     * origin. The bounds may exceed the actual bounds of the component.
     */
    public Bounds getAffectedArea(Component component, int x, int y, int width, int height);
}
