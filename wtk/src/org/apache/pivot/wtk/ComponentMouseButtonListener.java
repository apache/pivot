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

import org.apache.pivot.util.BooleanResult;
import org.apache.pivot.util.ListenerList;

/**
 * Component mouse button listener interface.
 */
public interface ComponentMouseButtonListener {
    /**
     * Component mouse button listeners.
     */
    public static class Listeners extends ListenerList<ComponentMouseButtonListener>
        implements ComponentMouseButtonListener {
        @Override
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            BooleanResult consumed = new BooleanResult();

            forEach(listener -> consumed.or(listener.mouseDown(component, button, x, y)));

            return consumed.get();
        }

        @Override
        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            BooleanResult consumed = new BooleanResult();

            forEach(listener -> consumed.or(listener.mouseUp(component, button, x, y)));

            return consumed.get();
        }

        @Override
        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            BooleanResult consumed = new BooleanResult();

            forEach(listener -> consumed.or(listener.mouseClick(component, button, x, y, count)));

            return consumed.get();
        }
    }

    /**
     * Component mouse button listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
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
     * @param component Component that is under the mouse pointer.
     * @param button Which mouse button was pressed.
     * @param x X position of the mouse.
     * @param y Y position of the mouse.
     * @return <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate (default).
     */
    default boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        return false;
    }

    /**
     * Called when a mouse button is released over a component.
     *
     * @param component Component user the mouse pointer.
     * @param button Which mouse button that was released.
     * @param x X position of the mouse.
     * @param y Y position of the mouse.
     * @return <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate (default).
     */
    default boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
        return false;
    }

    /**
     * Called when a mouse button is clicked over a component.
     *
     * @param component Component user the mouse pointer.
     * @param button Which mouse button was clicked.
     * @param x X position of the mouse.
     * @param y Y position of the mouse.
     * @param count Number of clicks (1 = single click, 2 = double click, etc.).
     * @return <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate (default).
     */
    default boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        return false;
    }
}
