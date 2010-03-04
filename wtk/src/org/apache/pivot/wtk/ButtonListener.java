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
        }

        @Override
        public void dataRendererChanged(Button button, Button.DataRenderer previousDataRenderer) {
        }

        @Override
        public void actionChanged(Button button, Action previousAction) {
        }

        @Override
        public void toggleButtonChanged(Button button) {
        }

        @Override
        public void triStateChanged(Button button) {
        }

        @Override
        public void buttonGroupChanged(Button button, ButtonGroup previousButtonGroup) {
        }

        @Override
        public void selectedKeyChanged(Button button, String previousSelectedKey) {
        }

        @Override
        public void selectedBindTypeChanged(Button button, BindType previousSelectedBindType) {
        }

        @Override
        public void selectedBindMappingChanged(Button button, Button.SelectedBindMapping previousSelectedBindMapping) {
        }

        @Override
        public void stateKeyChanged(Button button, String previousStateKey) {
        }

        @Override
        public void stateBindTypeChanged(Button button, BindType previousStateBindType) {
        }

        @Override
        public void stateBindMappingChanged(Button button, Button.StateBindMapping previousStateBindMapping) {
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

    /**
     * Called when a button's selected key has changed.
     *
     * @param button
     * @param previousSelectedKey
     */
    public void selectedKeyChanged(Button button, String previousSelectedKey);

    /**
     * Called when a button's selected bind type has changed.
     *
     * @param button
     * @param previousSelectedBindType
     */
    public void selectedBindTypeChanged(Button button, BindType previousSelectedBindType);

    /**
     * Called when a button's selected bind mapping has changed.
     *
     * @param button
     * @param previousSelectedBindMapping
     */
    public void selectedBindMappingChanged(Button button, Button.SelectedBindMapping previousSelectedBindMapping);

    /**
     * Called when a button's state key has changed.
     *
     * @param button
     * @param previousStateKey
     */
    public void stateKeyChanged(Button button, String previousStateKey);

    /**
     * Called when a button's state bind type has changed.
     *
     * @param button
     * @param previousStateBindType
     */
    public void stateBindTypeChanged(Button button, BindType previousStateBindType);

    /**
     * Called when a button's bind mapping has changed.
     *
     * @param button
     * @param previousStateBindMapping
     */
    public void stateBindMappingChanged(Button button, Button.StateBindMapping previousStateBindMapping);
}
