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
 * Component key listener interface.
 */
public interface ComponentKeyListener {
    /**
     * Component key listeners.
     */
    public static class Listeners extends ListenerList<ComponentKeyListener>
        implements ComponentKeyListener {
        @Override
        public boolean keyTyped(Component component, char character) {
            BooleanResult consumed = new BooleanResult();

            forEach(listener -> consumed.or(listener.keyTyped(component, character)));

            return consumed.get();
        }

        @Override
        public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            BooleanResult consumed = new BooleanResult();

            forEach(listener -> consumed.or(listener.keyPressed(component, keyCode, keyLocation)));

            return consumed.get();
        }

        @Override
        public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            BooleanResult consumed = new BooleanResult();

            forEach(listener -> consumed.or(listener.keyReleased(component, keyCode, keyLocation)));

            return consumed.get();
        }
    }

    /**
     * Component key listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements ComponentKeyListener {
        @Override
        public boolean keyTyped(Component component, char character) {
            return false;
        }

        @Override
        public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            return false;
        }

        @Override
        public boolean keyReleased(Component component, int keyCode,
            Keyboard.KeyLocation keyLocation) {
            return false;
        }
    }

    /**
     * Called when a key has been typed.
     *
     * @param component Component that has the focus, that is receiving this key.
     * @param character The decoded character that was typed.
     * @return <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate.
     */
    default boolean keyTyped(Component component, char character) {
        return false;
    }

    /**
     * Called when a key has been pressed.
     *
     * @param component Component that has the focus.
     * @param keyCode The key code for the key that was pressed.
     * @param keyLocation Location value for the key (left or right for shift keys, etc.).
     * @return <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate.
     */
    default boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        return false;
    }

    /**
     * Called when a key has been released.
     *
     * @param component Component that has the focus, that is receiving this key event.
     * @param keyCode Code for the key that was released.
     * @param keyLocation Location of the key.
     * @return <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate.
     */
    default boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        return false;
    }
}
