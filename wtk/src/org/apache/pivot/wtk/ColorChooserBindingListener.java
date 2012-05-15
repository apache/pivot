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
 * Color chooser binding listener interface.
 */
public interface ColorChooserBindingListener {
    /**
     * Color chooser binding listener adapter.
     */
    public static class Adapter implements ColorChooserBindingListener {
        @Override
        public void selectedColorKeyChanged(ColorChooser colorChooser,
            String previousSelectedColorKey) {
            // empty block
        }

        @Override
        public void selectedColorBindTypeChanged(ColorChooser colorChooser,
            BindType previousSelectedColorBindType) {
            // empty block
        }

        @Override
        public void selectedColorBindMappingChanged(ColorChooser colorChooser,
            ColorChooser.SelectedColorBindMapping previousSelectedColorBindMapping) {
            // empty block
        }
    }

    /**
     * Called when a color chooser's selected color key has changed.
     *
     * @param colorChooser
     * @param previousSelectedColorKey
     */
    public void selectedColorKeyChanged(ColorChooser colorChooser,
        String previousSelectedColorKey);

    /**
     * Called when a color chooser's selected color bind type has changed.
     *
     * @param colorChooser
     * @param previousSelectedColorBindType
     */
    public void selectedColorBindTypeChanged(ColorChooser colorChooser,
        BindType previousSelectedColorBindType);

    /**
     * Called when a color chooser's selected color bind mapping has changed.
     *
     * @param colorChooser
     * @param previousSelectedColorBindMapping
     */
    public void selectedColorBindMappingChanged(ColorChooser colorChooser,
        ColorChooser.SelectedColorBindMapping previousSelectedColorBindMapping);
}
