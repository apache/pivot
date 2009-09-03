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
 * Component key listener interface.
 */
public interface ComponentKeyListener {
    /**
     * Component key listener adapter.
     */
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
        public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            return false;
        }
    }

    /**
     * Called when a key has been typed.
     *
     * @param component
     * @param character
     *
     * @return
     * <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate.
     */
    public boolean keyTyped(Component component, char character);

    /**
     * Called when a key has been pressed.
     *
     * @param component
     * @param keyCode
     * @param keyLocation
     *
     * @return
     * <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate.
     */
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation);

    /**
     * Called when a key has been released.
     *
     * @param component
     * @param keyCode
     * @param keyLocation
     *
     * @return
     * <tt>true</tt> to consume the event; <tt>false</tt> to allow it to
     * propagate.
     */
    public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation);
}
