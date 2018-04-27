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
 * Component mouse listener interface. Component mouse events are "bubbling" and
 * are fired as the event propagates up the component hierarchy.
 */
public interface ComponentMouseListener {
    /**
     * Component mouse listeners.
     */
    public static class Listeners extends ListenerList<ComponentMouseListener>
        implements ComponentMouseListener {
        @Override
        public boolean mouseMove(Component component, int x, int y) {
            BooleanResult consumed = new BooleanResult();

            forEach(listener -> consumed.or(listener.mouseMove(component, x, y)));

            return consumed.get();
        }

        @Override
        public void mouseOver(Component component) {
            forEach(listener -> listener.mouseOver(component));
        }

        @Override
        public void mouseOut(Component component) {
            forEach(listener -> listener.mouseOut(component));
        }
    }

    /**
     * Component mouse button listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements ComponentMouseListener {
        @Override
        public boolean mouseMove(Component component, int x, int y) {
            return false;
        }

        @Override
        public void mouseOver(Component component) {
            // empty block
        }

        @Override
        public void mouseOut(Component component) {
            // empty block
        }
    }

    /**
     * Called when the mouse is moved over a component.
     *
     * @param component Component that is under the mouse.
     * @param x X position of the mouse.
     * @param y Y position of the mouse.
     * @return <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate (default return).
     */
    default boolean mouseMove(Component component, int x, int y) {
        return false;
    }

    /**
     * Called when the mouse enters a component.
     * <p> Default is to do nothing.
     *
     * @param component Component that is now under the mouse pointer.
     */
    default void mouseOver(Component component) {
    }

    /**
     * Called when the mouse exits a component.
     * <p> Default is to do nothing.
     *
     * @param component Component that has now lost the mouse pointer.
     */
    default void mouseOut(Component component) {
    }
}
