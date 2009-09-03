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
 * Component mouse button listener interface.
 */
public interface ComponentMouseButtonListener {
    /**
     * Component mouse button listener adapter.
     */
    public static class Adapter implements ComponentMouseButtonListener {
        @Override
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        @Override
        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        @Override
        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            return false;
        }
    }

    /**
     * Called when a mouse button is pressed over a component.
     *
     * @param component
     * @param button
     * @param x
     * @param y
     *
     * @return
     * <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate.
     */
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y);

    /**
     * Called when a mouse button is released over a component.
     *
     * @param component
     * @param button
     * @param x
     * @param y
     *
     * @return
     * <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate.
     */
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y);

    /**
     * Called when a mouse button is clicked over a component.
     *
     * @param component
     * @param button
     * @param x
     * @param y
     * @param count
     *
     * @return
     * <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate.
     */
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count);
}
