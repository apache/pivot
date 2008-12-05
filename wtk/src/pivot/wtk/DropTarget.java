/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk;

/**
 * Interface representing a drop target.
 *
 * @author gbrown
 */
public interface DropTarget {
    /**
     * Tests whether the current drag state is a valid drop target. This method
     * is called repeatedly as the user drags the mouse or presses and releases
     * drag modifier keys.
     *
     * @param component
     * @param dragContentType
     * @param supportedDropActions
     * @param userDropAction
     * @param x
     * @param y
     *
     * @return
     * The drop action that would result if the user dropped the item at this
     * location, or <tt>null</tt> if the target cannot accept the drop.
     */
    public DropAction getDropAction(Component component, Class<?> dragContentType,
        int supportedDropActions, DropAction userDropAction, int x, int y);

    /**
     * Called to notify a drop target that it should show a drop state.
     *
     * @param component
     * @param dragContentType
     * @param dropAction
     */
    public void showDropState(Component component, Class<?> dragContentType,
        DropAction dropAction);

    /**
     * Called to notify a drop target that it should hide its drop state.
     *
     * @param component
     */
    public void hideDropState(Component component);

    /**
     * Called to notify a drop target that it should update its drop state.
     *
     * @param dropAction
     * @param x
     * @param y
     */
    public void updateDropState(Component component, DropAction dropAction, int x, int y);

    /**
     * Called when the user drops the drag content.
     *
     * @param component
     * @param dragContent
     * @param dropAction
     * @param x
     * @param y
     */
    public void drop(Component component, Object dragContent, DropAction dropAction,
        int x, int y);
}
