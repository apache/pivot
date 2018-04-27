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

import org.apache.pivot.util.ListenerList;

/**
 * Button binding listener interface.
 */
public interface ButtonBindingListener {
    /**
     * Button binding listeners.
     */
    public static class Listeners extends ListenerList<ButtonBindingListener>
        implements ButtonBindingListener {
        @Override
        public void buttonDataKeyChanged(Button button, String previousButtonDataKey) {
            forEach(listener -> listener.buttonDataKeyChanged(button, previousButtonDataKey));
        }

        @Override
        public void buttonDataBindTypeChanged(Button button, BindType previousDataBindType) {
            forEach(listener -> listener.buttonDataBindTypeChanged(button, previousDataBindType));
        }

        @Override
        public void buttonDataBindMappingChanged(Button button,
            Button.ButtonDataBindMapping previousButtonDataBindMapping) {
            forEach(listener -> listener.buttonDataBindMappingChanged(button, previousButtonDataBindMapping));
        }

        @Override
        public void selectedKeyChanged(Button button, String previousSelectedKey) {
            forEach(listener -> listener.selectedKeyChanged(button, previousSelectedKey));
        }

        @Override
        public void selectedBindTypeChanged(Button button, BindType previousSelectedBindType) {
            forEach(listener -> listener.selectedBindTypeChanged(button, previousSelectedBindType));
        }

        @Override
        public void selectedBindMappingChanged(Button button,
            Button.SelectedBindMapping previousSelectedBindMapping) {
            forEach(listener -> listener.selectedBindMappingChanged(button, previousSelectedBindMapping));
        }

        @Override
        public void stateKeyChanged(Button button, String previousStateKey) {
            forEach(listener -> listener.stateKeyChanged(button, previousStateKey));
        }

        @Override
        public void stateBindTypeChanged(Button button, BindType previousStateBindType) {
            forEach(listener -> listener.stateBindTypeChanged(button, previousStateBindType));
        }

        @Override
        public void stateBindMappingChanged(Button button,
            Button.StateBindMapping previousStateBindMapping) {
            forEach(listener -> listener.stateBindMappingChanged(button, previousStateBindMapping));
        }
    }

    /**
     * Button binding listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
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
        public void buttonDataBindMappingChanged(Button button,
            Button.ButtonDataBindMapping previousButtonDataBindMapping) {
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
        public void selectedBindMappingChanged(Button button,
            Button.SelectedBindMapping previousSelectedBindMapping) {
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
        public void stateBindMappingChanged(Button button,
            Button.StateBindMapping previousStateBindMapping) {
            // empty block
        }
    }

    /**
     * Called when a button's buttonData key has changed.
     *
     * @param button                The button that has changed.
     * @param previousButtonDataKey The previous binding key for the button data.
     */
    default void buttonDataKeyChanged(Button button, String previousButtonDataKey) {
    }

    /**
     * Called when a button's buttonData bind type has changed.
     *
     * @param button                     The button that has changed.
     * @param previousButtonDataBindType The previous bind type for the button data.
     */
    default void buttonDataBindTypeChanged(Button button, BindType previousButtonDataBindType) {
    }

    /**
     * Called when a button's buttonData bind mapping has changed.
     *
     * @param button                        The button that has changed.
     * @param previousButtonDataBindMapping The previous bind mapping for the button data.
     */
    default void buttonDataBindMappingChanged(Button button,
        Button.ButtonDataBindMapping previousButtonDataBindMapping) {
    }

    /**
     * Called when a button's selected key has changed.
     *
     * @param button              The button that has changed.
     * @param previousSelectedKey The previous binding key for the selected state.
     */
    default void selectedKeyChanged(Button button, String previousSelectedKey) {
    }

    /**
     * Called when a button's selected bind type has changed.
     *
     * @param button                   The button that has changed.
     * @param previousSelectedBindType The previous bind type for the selected state.
     */
    default void selectedBindTypeChanged(Button button, BindType previousSelectedBindType) {
    }

    /**
     * Called when a button's selected bind mapping has changed.
     *
     * @param button                      The button that has changed.
     * @param previousSelectedBindMapping The previous bind mapping for the selected state.
     */
    default void selectedBindMappingChanged(Button button,
        Button.SelectedBindMapping previousSelectedBindMapping) {
    }

    /**
     * Called when a button's state key has changed.
     *
     * @param button           The button that has changed.
     * @param previousStateKey The previous binding key for the button state.
     */
    default void stateKeyChanged(Button button, String previousStateKey) {
    }

    /**
     * Called when a button's state bind type has changed.
     *
     * @param button                The button that has changed.
     * @param previousStateBindType The previous bind type for the button state.
     */
    default void stateBindTypeChanged(Button button, BindType previousStateBindType) {
    }

    /**
     * Called when a button's state bind mapping has changed.
     *
     * @param button                   The button that has changed.
     * @param previousStateBindMapping The previous bind mapping for the button state.
     */
    default void stateBindMappingChanged(Button button,
        Button.StateBindMapping previousStateBindMapping) {
    }
}
