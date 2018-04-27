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
 * Color chooser button binding listener interface.
 */
public interface ColorChooserButtonBindingListener {
    /**
     * Color chooser button binding listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements ColorChooserButtonBindingListener {
        @Override
        public void selectedColorKeyChanged(ColorChooserButton colorChooserButton,
            String previousSelectedColorKey) {
            // empty block
        }

        @Override
        public void selectedColorBindTypeChanged(ColorChooserButton colorChooserButton,
            BindType previousSelectedColorBindType) {
            // empty block
        }

        @Override
        public void selectedColorBindMappingChanged(ColorChooserButton colorChooserButton,
            ColorChooser.SelectedColorBindMapping previousSelectedColorBindMapping) {
            // empty block
        }
    }

    /**
     * Color chooser button binding listener listeners list.
     */
    public static class Listeners extends ListenerList<ColorChooserButtonBindingListener>
        implements ColorChooserButtonBindingListener {
        @Override
        public void selectedColorKeyChanged(ColorChooserButton colorChooserButton,
            String previousSelectedColorKey) {
            forEach(listener -> listener.selectedColorKeyChanged(colorChooserButton, previousSelectedColorKey));
        }

        @Override
        public void selectedColorBindTypeChanged(ColorChooserButton colorChooserButton,
            BindType previousSelectedColorBindType) {
            forEach(listener -> listener.selectedColorBindTypeChanged(colorChooserButton,
                    previousSelectedColorBindType));
        }

        @Override
        public void selectedColorBindMappingChanged(ColorChooserButton colorChooserButton,
            ColorChooser.SelectedColorBindMapping previousSelectedColorBindMapping) {
            forEach(listener -> listener.selectedColorBindMappingChanged(colorChooserButton,
                    previousSelectedColorBindMapping));
        }
    }

    /**
     * Called when a color chooser button's selected color key has changed.
     *
     * @param colorChooserButton       The color chooser button that has changed.
     * @param previousSelectedColorKey The previous binding key for the selected color.
     */
    default void selectedColorKeyChanged(ColorChooserButton colorChooserButton,
        String previousSelectedColorKey) {
    }

    /**
     * Called when a color chooser button's selected color bind type has
     * changed.
     *
     * @param colorChooserButton            The color chooser button that has changed.
     * @param previousSelectedColorBindType The previous bind type for the selected color.
     */
    default void selectedColorBindTypeChanged(ColorChooserButton colorChooserButton,
        BindType previousSelectedColorBindType) {
    }

    /**
     * Called when a color chooser button's selected color bind mapping has
     * changed.
     *
     * @param colorChooserButton               The color chooser button that has changed.
     * @param previousSelectedColorBindMapping The previous bind mapping for the selected color.
     */
    default void selectedColorBindMappingChanged(ColorChooserButton colorChooserButton,
        ColorChooser.SelectedColorBindMapping previousSelectedColorBindMapping) {
    }
}
