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
 * Group mouse listener interface. Group mouse events are "tunneling" events;
 * consuming a group mouse event prevents it from propagating down the node
 * hierarchy.
 */
public interface GroupMouseListener {
    /**
     * Group mouse listener adapter.
     */
    public static class Adapter implements GroupMouseListener {
        @Override
        public boolean mouseMoved(Group group, int x, int y, boolean captured) {
            return false;
        }

        @Override
        public boolean mousePressed(Group group, Mouse.Button button, int x, int y) {
            return false;
        }

        @Override
        public boolean mouseReleased(Group group, Mouse.Button button, int x, int y) {
            return false;
        }

        @Override
        public boolean mouseWheelScrolled(Group group, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            return false;
        }
    }

    /**
     * Called when the mouse is moved while over a group.
     *
     * @param group
     * @param x
     * @param y
     * @param captured
     *
     * @return
     * <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate.
     */
    public boolean mouseMoved(Group group, int x, int y, boolean captured);

    /**
     * Called when the mouse is pressed over a group.
     *
     * @param group
     * @param button
     * @param x
     * @param y
     *
     * @return
     * <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate.
     */
    public boolean mousePressed(Group group, Mouse.Button button, int x, int y);

    /**
     * Called when the mouse is released over a group.
     *
     * @param group
     * @param button
     * @param x
     * @param y
     *
     * @return
     * <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate.
     */
    public boolean mouseReleased(Group group, Mouse.Button button, int x, int y);

    /**
     * Called when the mouse wheel is scrolled over a group.
     *
     * @param group
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
    public boolean mouseWheelScrolled(Group group, Mouse.ScrollType scrollType,
        int scrollAmount, int wheelRotation, int x, int y);
}
