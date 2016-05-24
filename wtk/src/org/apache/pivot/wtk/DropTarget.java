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
 * Interface representing a drop target.
 */
public interface DropTarget {
    /**
     * Called when the mouse first enters a drop target during a drag operation.
     *
     * @param component            The drop target component.
     * @param dragContent          What is being dragged.
     * @param supportedDropActions What drop actions should be considered.
     * @param userDropAction       What drop action the user is requesting.
     * @return The drop action that would result if the user dropped the item at
     * this location, or <tt>null</tt> if the target cannot accept the drop.
     */
    public DropAction dragEnter(Component component, Manifest dragContent,
        int supportedDropActions, DropAction userDropAction);

    /**
     * Called when the mouse leaves a drop target during a drag operation.
     *
     * @param component The drop target component.
     */
    public void dragExit(Component component);

    /**
     * Called when the mouse is moved while positioned over a drop target during
     * a drag operation.
     *
     * @param component            The drop target component.
     * @param dragContent          What is being dragged.
     * @param supportedDropActions The drop actions that are supported by the contents.
     * @param x                    The current mouse X-location.
     * @param y                    The current mouse Y-location.
     * @param userDropAction       What drop action the user is requesting.
     * @return The drop action that would result if the user dropped the item at
     * this location, or <tt>null</tt> if the target cannot accept the drop.
     */
    public DropAction dragMove(Component component, Manifest dragContent, int supportedDropActions,
        int x, int y, DropAction userDropAction);

    /**
     * Called when the user drop action changes while the mouse is positioned
     * over a drop target during a drag operation.
     *
     * @param component            The drop target component.
     * @param dragContent          What is being dragged.
     * @param supportedDropActions The drop actions that are supported by the contents.
     * @param x                    The current mouse X-location.
     * @param y                    The current mouse Y-location.
     * @param userDropAction       What drop action the user is requesting.
     * @return The drop action that would result if the user dropped the item at
     * this location, or <tt>null</tt> if the target cannot accept the drop.
     */
    public DropAction userDropActionChange(Component component, Manifest dragContent,
        int supportedDropActions, int x, int y, DropAction userDropAction);

    /**
     * Called to drop the drag content.
     *
     * @param component            The drop target component.
     * @param dragContent          What is being dragged.
     * @param supportedDropActions The drop actions that are supported by the contents.
     * @param x                    The current mouse X-location.
     * @param y                    The current mouse Y-location.
     * @param userDropAction       What drop action the user is requesting.
     * @return The drop action used to perform the drop, or <tt>null</tt> if the
     * target rejected the drop.
     */
    public DropAction drop(Component component, Manifest dragContent, int supportedDropActions,
        int x, int y, DropAction userDropAction);
}
