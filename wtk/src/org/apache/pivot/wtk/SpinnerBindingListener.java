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
 * Spinner binding listener interface.
 */
public interface SpinnerBindingListener {
    /**
     * Spinner binding listener adapter.
     */
    public static class Adapter implements SpinnerBindingListener {
        @Override
        public void spinnerDataKeyChanged(Spinner spinner, String previousSpinnerDataKey) {
            // empty block
        }

        @Override
        public void spinnerDataBindTypeChanged(Spinner spinner, BindType previousSpinnerDataBindType) {
            // empty block
        }

        @Override
        public void spinnerDataBindMappingChanged(Spinner spinner,
            Spinner.SpinnerDataBindMapping previousSpinnerDataBindMapping) {
            // empty block
        }

        @Override
        public void selectedItemKeyChanged(Spinner spinner, String previousSelectedItemKey) {
            // empty block
        }

        @Override
        public void selectedItemBindTypeChanged(Spinner spinner, BindType previousSelectedItemBindType) {
            // empty block
        }

        @Override
        public void selectedItemBindMappingChanged(Spinner spinner, Spinner.ItemBindMapping previousSelectedItemBindMapping) {
            // empty block
        }
    }

    /**
     * Called when a spinner's spinner data key has changed.
     *
     * @param spinner
     * @param previousSpinnerDataKey
     */
    public void spinnerDataKeyChanged(Spinner spinner, String previousSpinnerDataKey);

    /**
     * Called when a spinner's spinner data bind type has changed.
     *
     * @param spinner
     * @param previousSpinnerDataBindType
     */
    public void spinnerDataBindTypeChanged(Spinner spinner, BindType previousSpinnerDataBindType);

    /**
     * Called when a spinner's spinner data bind mapping has changed.
     *
     * @param spinner
     * @param previousSpinnerDataBindMapping
     */
    public void spinnerDataBindMappingChanged(Spinner spinner,
        Spinner.SpinnerDataBindMapping previousSpinnerDataBindMapping);

    /**
     * Called when a spinner's selected item key has changed.
     *
     * @param spinner
     * @param previousSelectedItemKey
     */
    public void selectedItemKeyChanged(Spinner spinner, String previousSelectedItemKey);

    /**
     * Called when a spinner's selected item bind type has changed.
     *
     * @param spinner
     * @param previousSelectedItemBindType
     */
    public void selectedItemBindTypeChanged(Spinner spinner, BindType previousSelectedItemBindType);

    /**
     * Called when a spinner's selected item bind mapping has changed.
     *
     * @param spinner
     * @param previousSelectedItemBindMapping
     */
    public void selectedItemBindMappingChanged(Spinner spinner, Spinner.ItemBindMapping previousSelectedItemBindMapping);
}
