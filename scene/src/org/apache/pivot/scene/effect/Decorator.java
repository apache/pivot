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
package org.apache.pivot.scene.effect;

import org.apache.pivot.scene.Extents;
import org.apache.pivot.scene.Graphics;
import org.apache.pivot.scene.Node;
import org.apache.pivot.scene.Transform;

/**
 * Interface defining a "decorator". Decorators allow a caller to attach
 * additional rendering behaviors to a node.
 * <p>
 * Decorators use a chained prepare/update model to modify the graphics in
 * which a node is painted. The <tt>prepare()</tt> method of each decorator
 * in a node's decorator sequence is called in reverse order before the
 * node's <tt>paint()</tt> method is called. <tt>prepare()</tt> returns
 * an instance of {@link Graphics} that is passed to prior decorators,
 * and ultimately to the node itself. This allows decorators to modify the
 * graphics context before it reaches the node. After the node has
 * been painted, each decorator's <tt>update()</tt> method is then called in
 * order to allow the decorator to further modify the resulting output.
 */
public interface Decorator {
    /**
     * Prepares the graphics context into which the node or prior
     * decorator will paint. This method is called immediately prior to
     * {@link Node#paint(Graphics)}; decorators are called in
     * descending order.
     *
     * @param node
     * @param graphics
     *
     * @return
     * The graphics context that should be used by the node or prior
     * decorators.
     */
    public Graphics prepare(Node node, Graphics graphics);

    /**
     * Updates the graphics context into which the node or prior
     * decorator was painted. This method is called immediately after
     * {@link Node#paint(Graphics)}; decorators are called in
     * ascending order.
     */
    public void update();

    /**
     * Returns the extents of the decorator.
     *
     * @param node
     *
     * @return
     * The decorator's extents, relative to the nodes's origin.
     */
    public Extents getExtents(Node node);

    /**
     * Returns the transformation the decorator applies to the node's
     * coordinate space.
     *
     * @return
     * The decorator's transform.
     */
    public Transform getTransform(Node node);
}
