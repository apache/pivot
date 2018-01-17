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

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;

/**
 * Window action mapping listener interface.
 */
public interface WindowActionMappingListener {
    /**
     * Window action mapping listeners.
     */
    public static class Listeners extends ListenerList<WindowActionMappingListener>
        implements WindowActionMappingListener {
        @Override
        public void actionMappingAdded(Window window) {
            forEach(listener -> listener.actionMappingAdded(window));
        }

        @Override
        public void actionMappingsRemoved(Window window, int index,
            Sequence<Window.ActionMapping> removed) {
            forEach(listener -> listener.actionMappingsRemoved(window, index, removed));
        }

        @Override
        public void keyStrokeChanged(Window.ActionMapping actionMapping,
            Keyboard.KeyStroke previousKeyStroke) {
            forEach(listener -> listener.keyStrokeChanged(actionMapping, previousKeyStroke));
        }

        @Override
        public void actionChanged(Window.ActionMapping actionMapping, Action previousAction) {
            forEach(listener -> listener.actionChanged(actionMapping, previousAction));
        }
    }

    /**
     * Called when an action mapping has been added to a window.
     *
     * @param window The source of this event.
     */
    public void actionMappingAdded(Window window);

    /**
     * Called when action mappings have been removed from a window.
     *
     * @param window  The window that is affected.
     * @param index   Starting index of the action mappings that were removed.
     * @param removed The sequence of action mappings that were removed.
     */
    public void actionMappingsRemoved(Window window, int index,
        Sequence<Window.ActionMapping> removed);

    /**
     * Called when an action mapping's keystroke has changed.
     *
     * @param actionMapping     The action mapping that has changed.
     * @param previousKeyStroke The previous keystroke (if any) associated with this mapping.
     */
    public void keyStrokeChanged(Window.ActionMapping actionMapping,
        Keyboard.KeyStroke previousKeyStroke);

    /**
     * Called when an action mapping's action has changed.
     *
     * @param actionMapping  The action mapping that has changed.
     * @param previousAction The action previously associated with this mapping.
     */
    public void actionChanged(Window.ActionMapping actionMapping, Action previousAction);
}
