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

import java.awt.Color;
import org.apache.pivot.util.ListenerList;


/**
 * Color chooser button selection listener interface.
 */
public interface ColorChooserButtonSelectionListener {
    /**
     * ColorChooser button selection listener listeners list.
     */
    public static class Listeners extends ListenerList<ColorChooserButtonSelectionListener>
        implements ColorChooserButtonSelectionListener {

        @Override
        public void selectedColorChanged(ColorChooserButton colorChooserButton,
            Color previousSelectedColor) {
            forEach(listener -> listener.selectedColorChanged(colorChooserButton, previousSelectedColor));
        }
    }

    /**
     * Called when a color chooser button's selected color has changed.
     *
     * @param colorChooserButton    The color chooser button that changed.
     * @param previousSelectedColor The previously selected color.
     */
    public void selectedColorChanged(ColorChooserButton colorChooserButton,
        Color previousSelectedColor);
}
