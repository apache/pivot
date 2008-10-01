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
 * Button listener interface.
 *
 * @author gbrown
 */
public interface ButtonListener {
    /**
     * Called when a button's data has changed.
     *
     * @param button
     * @param previousButtonData
     */
    public void buttonDataChanged(Button button, Object previousButtonData);

    /**
     * Called when a button's data renderer has changed.
     *
     * @param button
     * @param previousDataRenderer
     */
    public void dataRendererChanged(Button button, Button.DataRenderer previousDataRenderer);

    /**
     * Called when a button's action has changed.
     *
     * @param button
     * @param previousAction
     */
    public void actionChanged(Button button, Action previousAction);

    /**
     * Called when a button's toggle button flag has changed.
     *
     * @param button
     */
    public void toggleButtonChanged(Button button);

    /**
     * Called when a button's tri-state flag has changed.
     *
     * @param button
     */
    public void triStateChanged(Button button);

    /**
     * Called when a button's group has changed.
     *
     * @param button
     * @param previousGroup
     */
    public void groupChanged(Button button, Button.Group previousGroup);

    /**
     * Called when a button's selected key has changed.
     *
     * @param button
     * @param previousSelectedKey
     */
    public void selectedKeyChanged(Button button, String previousSelectedKey);

    /**
     * Called when a button's state key has changed.
     *
     * @param button
     * @param previousStateKey
     */
    public void stateKeyChanged(Button button, String previousStateKey);
}
