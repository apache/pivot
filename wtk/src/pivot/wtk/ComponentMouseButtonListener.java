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
 * <p>Component mouse button listener interface.</p>
 *
 * @author gbrown
 */
public interface ComponentMouseButtonListener {
    /**
     * Called when a mouse button is pressed over a component.
     *
     * @param component
     * @param button
     * @param x
     * @param y
     */
    public void mouseDown(Component component, Mouse.Button button, int x, int y);

    /**
     * Called when a mouse button is released over a component.
     *
     * @param component
     * @param button
     * @param x
     * @param y
     */
    public void mouseUp(Component component, Mouse.Button button, int x, int y);

    /**
     * Called when a mouse button is clicked over a component.
     *
     * @param component
     * @param button
     * @param x
     * @param y
     * @param count
     */
    public void mouseClick(Component component, Mouse.Button button, int x, int y, int count);
}
