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
 * Button binding listener interface.
 */
public interface ButtonBindingListener {
    /**
     * Button binding listener adapter.
     */
    public static class Adapter implements ButtonBindingListener {
        @Override
        public void buttonDataKeyChanged(Button button, String previousButtonDataKey) {
            // empty block
        }

        @Override
        public void buttonDataBindTypeChanged(Button button, BindType previousButtonDataBindType) {
            // empty block
        }

        @Override
        public void buttonDataBindMappingChanged(Button button, Button.ButtonDataBindMapping previousButtonDataBindMapping) {
            // empty block
        }

        @Override
        public void selectedKeyChanged(Button button, String previousSelectedKey) {
            // empty block
        }

        @Override
        public void selectedBindTypeChanged(Button button, BindType previousSelectedBindType) {
            // empty block
        }

        @Override
        public void selectedBindMappingChanged(Button button, Button.SelectedBindMapping previousSelectedBindMapping) {
            // empty block
        }

        @Override
        public void stateKeyChanged(Button button, String previousStateKey) {
            // empty block
        }

        @Override
        public void stateBindTypeChanged(Button button, BindType previousStateBindType) {
            // empty block
        }

        @Override
        public void stateBindMappingChanged(Button button, Button.StateBindMapping previousStateBindMapping) {
            // empty block
        }
    }

    /**
     * Called when a button's buttonData key has changed.
     *
     * @param button
     * @param previousButtonDataKey
     */
    public void buttonDataKeyChanged(Button button, String previousButtonDataKey);

    /**
     * Called when a button's buttonData bind type has changed.
     *
     * @param button
     * @param previousButtonDataBindType
     */
    public void buttonDataBindTypeChanged(Button button, BindType previousButtonDataBindType);

    /**
     * Called when a button's buttonData bind mapping has changed.
     *
     * @param button
     * @param previousButtonDataBindMapping
     */
    public void buttonDataBindMappingChanged(Button button, Button.ButtonDataBindMapping previousButtonDataBindMapping);

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
     * Called when a button's state bind mapping has changed.
     *
     * @param button
     * @param previousStateBindMapping
     */
    public void stateBindMappingChanged(Button button, Button.StateBindMapping previousStateBindMapping);
}
