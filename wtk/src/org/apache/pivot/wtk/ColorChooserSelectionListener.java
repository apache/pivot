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
 * Color chooser selection listener interface.
 */
public interface ColorChooserSelectionListener {
    /**
     * Color chooser selection listener listeners list.
     */
    public static class Listeners extends ListenerList<ColorChooserSelectionListener>
        implements ColorChooserSelectionListener {

        @Override
        public void selectedColorChanged(ColorChooser colorChooser, Color previousSelectedColor) {
            forEach(listener -> listener.selectedColorChanged(colorChooser, previousSelectedColor));
        }
    }

    /**
     * Called when a color chooser's selected color has changed.
     *
     * @param colorChooser          The color chooser that has changed.
     * @param previousSelectedColor The previously selected color in the chooser.
     */
    public void selectedColorChanged(ColorChooser colorChooser, Color previousSelectedColor);
}
