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
package org.apache.pivot.scene;

import org.apache.pivot.scene.Mouse.Button;

/**
 * Node mouse button listener interface. Node mouse events are "bubbling" events;
 * consuming a node mouse event prevents it from propagating up the node
 * hierarchy.
 */
public interface NodeMouseButtonListener {
    /**
     * Node mouse button listener adapter.
     */
    public static class Adapter implements NodeMouseButtonListener {
        @Override
        public boolean mousePressed(Node node, Button button, int x, int y) {
            return false;
        }

        @Override
        public boolean mouseReleased(Node node, Button button, int x, int y) {
            return false;
        }

        @Override
        public boolean mouseClicked(Node node, Button button, int x, int y, int count) {
            return false;
        }
    }

    /**
     * Called when a mouse button is pressed over a node.
     *
     * @param node
     * @param button
     * @param x
     * @param y
     *
     * @return
     * <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate.
     */
    public boolean mousePressed(Node node, Mouse.Button button, int x, int y);

    /**
     * Called when a mouse button is released over a node.
     *
     * @param node
     * @param button
     * @param x
     * @param y
     *
     * @return
     * <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate.
     */
    public boolean mouseReleased(Node node, Mouse.Button button, int x, int y);

    /**
     * Called when a mouse button is clicked over a node.
     *
     * @param node
     * @param button
     * @param x
     * @param y
     * @param count
     *
     * @return
     * <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate.
     */
    public boolean mouseClicked(Node node, Mouse.Button button, int x, int y, int count);
}
