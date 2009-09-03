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
package org.apache.pivot.wtk;

/**
 * Container mouse listener interface. Container mouse events are "tunneling"
 * events that are fired as the event propagates down the component hierarchy.
 */
public interface ContainerMouseListener {
    /**
     * Container mouse listener adapter.
     */
    public static class Adapter implements ContainerMouseListener {
        @Override
        public boolean mouseMove(Container container, int x, int y) {
            return false;
        }

        @Override
        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            return false;
        }

        @Override
        public boolean mouseUp(Container container, Mouse.Button button, int x, int y) {
            return false;
        }

        @Override
        public boolean mouseWheel(Container container, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            return false;
        }
    }

    /**
     * Called when the mouse is moved over a container.
     *
     * @param container
     * @param x
     * @param y
     *
     * @return
     * <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate.
     */
    public boolean mouseMove(Container container, int x, int y);

    /**
     * Called when the mouse is pressed over a container.
     *
     * @param container
     * @param button
     * @param x
     * @param y
     *
     * @return
     * <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate.
     */
    public boolean mouseDown(Container container, Mouse.Button button, int x, int y);

    /**
     * Called when the mouse is released over a container.
     *
     * @param container
     * @param button
     * @param x
     * @param y
     *
     * @return
     * <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate.
     */
    public boolean mouseUp(Container container, Mouse.Button button, int x, int y);

    /**
     * Called when the mouse wheel is scrolled over a container.
     *
     * @param container
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
    public boolean mouseWheel(Container container, Mouse.ScrollType scrollType,
        int scrollAmount, int wheelRotation, int x, int y);
}
