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
 * Color chooser binding listener interface.
 */
public interface ColorChooserBindingListener {
    /**
     * Color chooser binding listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
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
     * Color chooser binding listener listeners list.
     */
    public static class Listeners extends ListenerList<ColorChooserBindingListener>
        implements ColorChooserBindingListener {
        @Override
        public void selectedColorKeyChanged(ColorChooser colorChooser,
            String previousSelectedColorKey) {
            forEach(listener -> listener.selectedColorKeyChanged(colorChooser, previousSelectedColorKey));
        }

        @Override
        public void selectedColorBindTypeChanged(ColorChooser colorChooser,
            BindType previousSelectedColorBindType) {
            forEach(listener -> listener.selectedColorBindTypeChanged(colorChooser, previousSelectedColorBindType));
        }

        @Override
        public void selectedColorBindMappingChanged(ColorChooser colorChooser,
            ColorChooser.SelectedColorBindMapping previousSelectedColorBindMapping) {
            forEach(listener -> listener.selectedColorBindMappingChanged(colorChooser,
                    previousSelectedColorBindMapping));
        }
    }

    /**
     * Called when a color chooser's selected color key has changed.
     *
     * @param colorChooser             The color chooser that has changed.
     * @param previousSelectedColorKey The previous value of the selected color binding key.
     */
    default void selectedColorKeyChanged(ColorChooser colorChooser, String previousSelectedColorKey) {
    }

    /**
     * Called when a color chooser's selected color bind type has changed.
     *
     * @param colorChooser                  The color chooser that has changed.
     * @param previousSelectedColorBindType The previous value of the selected color bind type.
     */
    default void selectedColorBindTypeChanged(ColorChooser colorChooser,
        BindType previousSelectedColorBindType) {
    }

    /**
     * Called when a color chooser's selected color bind mapping has changed.
     *
     * @param colorChooser                     The color chooser that has changed.
     * @param previousSelectedColorBindMapping The previous bind mapping for the selected color.
     */
    default void selectedColorBindMappingChanged(ColorChooser colorChooser,
        ColorChooser.SelectedColorBindMapping previousSelectedColorBindMapping) {
    }
}
