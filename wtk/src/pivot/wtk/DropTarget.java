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
     * @param dropAction
     * @param x
     * @param y
     *
     * @return
     * <tt>true</tt> if this drag state would result in a drop; <tt>false</tt>,
     * otherwise.
     */
    public boolean isDrop(Component component, Class<?> dragContentType,
        DropAction dropAction, int x, int y);

    /**
     * Called to notify a drop target that it should show or hide a drop
     * highlight state.
     *
     * @param component
     * @param highlight
     */
    public void highlightDrop(Component component, boolean highlight);

    /**
     * Called to notify a drop target that it should update its highlight
     * state.
     *
     * @param component
     * @param dragContentType
     * @param dropAction
     * @param x
     * @param y
     */
    public void updateDropHighlight(Component component, Class<?> dragContentType,
        DropAction dropAction, int x, int y);

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
