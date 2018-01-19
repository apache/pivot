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
 * Component mouse wheel listener interface.
 */
public interface ComponentMouseWheelListener {
    /**
     * Mouse wheel listeners.
     */
    public static class Listeners extends ListenerList<ComponentMouseWheelListener>
        implements ComponentMouseWheelListener {
        @Override
        public boolean mouseWheel(Component component, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            BooleanResult consumed = new BooleanResult();

            forEach(listener -> consumed.or(listener.mouseWheel(component, scrollType, scrollAmount, wheelRotation,
                    x, y)));

            return consumed.get();
        }
    }

    /**
     * Called when the mouse wheel is scrolled over a component.
     *
     * @param component Component under the mouse pointer.
     * @param scrollType What type of scroll was requested on the mouse.
     * @param scrollAmount Amount of scrolling.
     * @param wheelRotation Rotation value.
     * @param x X position of the mouse.
     * @param y Y position of the mouse.
     * @return <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate.
     */
    public boolean mouseWheel(Component component, Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y);
}
