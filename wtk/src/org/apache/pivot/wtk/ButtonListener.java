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
 * Button listener interface.
 */
public interface ButtonListener {
    /**
     * Button listener adapter.
     */
    public static class Adapter implements ButtonListener {
        @Override
        public void buttonDataChanged(Button button, Object previousButtonData) {
            // empty block
        }

        @Override
        public void dataRendererChanged(Button button, Button.DataRenderer previousDataRenderer) {
            // empty block
        }

        @Override
        public void actionChanged(Button button, Action previousAction) {
            // empty block
        }

        @Override
        public void toggleButtonChanged(Button button) {
            // empty block
        }

        @Override
        public void triStateChanged(Button button) {
            // empty block
        }

        @Override
        public void buttonGroupChanged(Button button, ButtonGroup previousButtonGroup) {
            // empty block
        }
    }

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
     * Called when a button's button group has changed.
     *
     * @param button
     * @param previousButtonGroup
     */
    public void buttonGroupChanged(Button button, ButtonGroup previousButtonGroup);
}
