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

/**
 * Node mouse wheel listener interface. Node mouse events are "bubbling" events;
 * consuming a node mouse event prevents it from propagating up the node
 * hierarchy.
 */
public interface NodeMouseWheelListener {
    /**
     * Node mouse wheel listener adapter.
     */
    public static class Adapter implements NodeMouseWheelListener {
        @Override
        public boolean mouseWheelScrolled(Node node, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            return false;
        }
    }

    /**
     * Called when the mouse wheel is scrolled over a node.
     *
     * @param node
     * @param scrollType
     * @param scrollAmount
     * @param wheelRotation
     * @param x
     * @param y
     *
     * @return
     * <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate.
     */
    public boolean mouseWheelScrolled(Node node, Mouse.ScrollType scrollType,
        int scrollAmount, int wheelRotation, int x, int y);
}
