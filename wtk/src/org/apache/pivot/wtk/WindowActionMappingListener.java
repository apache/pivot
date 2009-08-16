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

/**
 * Window action mapping listener interface.
 */
public interface WindowActionMappingListener {
    /**
     * Called when an action mapping has been added to a window.
     *
     * @param window
     */
    public void actionMappingAdded(Window window);

    /**
     * Called when action mappings have been removed from a window.
     *
     * @param window
     * @param index
     * @param removed
     */
    public void actionMappingsRemoved(Window window, int index, Sequence<Window.ActionMapping> removed);

    /**
     * Called when an action mapping's keystroke has changed.
     *
     * @param actionMapping
     * @param previousKeyStroke
     */
    public void keyStrokeChanged(Window.ActionMapping actionMapping, Keyboard.KeyStroke previousKeyStroke);

    /**
     * Called when an action mapping's action has changed.
     *
     * @param actionMapping
     * @param previousAction
     */
    public void actionChanged(Window.ActionMapping actionMapping, Action previousAction);
}
