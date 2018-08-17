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

import org.apache.pivot.json.JSON;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;

/**
 * Component that allows the user to select a color.
 */
public class ColorChooser extends Container {
    /**
     * Translates between color and context data during data binding.
     */
    public interface SelectedColorBindMapping {
        /**
         * Converts a context value to a color.
         *
         * @param value The value returned from the bound object.
         * @return The value converted to a {@link Color}.
         */
        public Color toColor(Object value);

        /**
         * Converts a color to a context value.
         *
         * @param color The color value selected in this component.
         * @return The color value converted to a format suitable for
         * persistence in the bound object.
         */
        public Object valueOf(Color color);
    }

    private Color selectedColor = null;

    private String selectedColorKey = null;
    private BindType selectedColorBindType = BindType.BOTH;
    private SelectedColorBindMapping selectedColorBindMapping = null;

    private ColorChooserSelectionListener.Listeners colorChooserSelectionListeners =
        new ColorChooserSelectionListener.Listeners();
    private ColorChooserBindingListener.Listeners colorChooserBindingListeners =
        new ColorChooserBindingListener.Listeners();

    public ColorChooser() {
        installSkin(ColorChooser.class);
    }

    /**
     * @return The currently selected color, or <tt>null</tt> if no color is
     * selected.
     */
    public Color getSelectedColor() {
        return selectedColor;
    }

    /**
     * Sets the selected color.
     *
     * @param selectedColor The color to select, or <tt>null</tt> to clear the
     * selection.
     */
    public void setSelectedColor(final Color selectedColor) {
        Color previousSelectedColor = this.selectedColor;

        if (previousSelectedColor != selectedColor
            && (previousSelectedColor == null || !previousSelectedColor.equals(selectedColor))) {
            this.selectedColor = selectedColor;
            colorChooserSelectionListeners.selectedColorChanged(this, previousSelectedColor);
        }
    }

    /**
     * Sets the selected color.
     *
     * @param selectedColor The color to select, or <tt>"null"</tt> to clear the
     * selection.
     */
    public void setSelectedColor(final String selectedColor) {
        setSelectedColor(GraphicsUtilities.decodeColor(selectedColor, "selectedColor"));
    }

    /**
     * @return The data binding key that is set on this color chooser.
     */
    public String getSelectedColorKey() {
        return selectedColorKey;
    }

    /**
     * Sets this color chooser's data binding key.
     *
     * @param selectedColorKey The binding key for the selected color.
     */
    public void setSelectedColorKey(final String selectedColorKey) {
        String previousSelectedColorKey = this.selectedColorKey;

        if (selectedColorKey != previousSelectedColorKey) {
            this.selectedColorKey = selectedColorKey;
            colorChooserBindingListeners.selectedColorKeyChanged(this, previousSelectedColorKey);
        }
    }

    public BindType getSelectedColorBindType() {
        return selectedColorBindType;
    }

    public void setSelectedColorBindType(final BindType selectedColorBindType) {
        Utils.checkNull(selectedColorBindType, "selectedColorBindType");

        BindType previousSelectedColorBindType = this.selectedColorBindType;

        if (previousSelectedColorBindType != selectedColorBindType) {
            this.selectedColorBindType = selectedColorBindType;
            colorChooserBindingListeners.selectedColorBindTypeChanged(this,
                previousSelectedColorBindType);
        }
    }

    public SelectedColorBindMapping getSelectedColorBindMapping() {
        return selectedColorBindMapping;
    }

    public void setSelectedColorBindMapping(final SelectedColorBindMapping selectedColorBindMapping) {
        SelectedColorBindMapping previousSelectedColorBindMapping = this.selectedColorBindMapping;

        if (previousSelectedColorBindMapping != selectedColorBindMapping) {
            this.selectedColorBindMapping = selectedColorBindMapping;
            colorChooserBindingListeners.selectedColorBindMappingChanged(this,
                previousSelectedColorBindMapping);
        }
    }

    /**
     * Loads the selected color from the specified bind context using this color
     * chooser's bind key, if one is set.
     */
    @Override
    public void load(final Object context) {
        if (selectedColorKey != null && JSON.containsKey(context, selectedColorKey)
            && selectedColorBindType != BindType.STORE) {
            Object value = JSON.get(context, selectedColorKey);

            Color newSelectedColor = null;

            if (value instanceof Color) {
                newSelectedColor = (Color) value;
            } else if (selectedColorBindMapping == null) {
                if (value != null) {
                    newSelectedColor = GraphicsUtilities.decodeColor(value.toString(), "selectedColor");
                }
            } else {
                newSelectedColor = selectedColorBindMapping.toColor(value);
            }

            setSelectedColor(newSelectedColor);
        }
    }

    /**
     * Stores the selected color into the specified bind context using this
     * color chooser's bind key, if one is set.
     */
    @Override
    public void store(final Object context) {
        if (selectedColorKey != null && selectedColorBindType != BindType.LOAD) {
            JSON.put(context, selectedColorKey, (selectedColorBindMapping == null) ? selectedColor
                : selectedColorBindMapping.valueOf(selectedColor));
        }
    }

    /**
     * If a bind key is set, clears the selected color.
     */
    @Override
    public void clear() {
        if (selectedColorKey != null) {
            setSelectedColor((Color) null);
        }
    }

    /**
     * @return The color chooser selection listener list.
     */
    public ListenerList<ColorChooserSelectionListener> getColorChooserSelectionListeners() {
        return colorChooserSelectionListeners;
    }

    /**
     * @return The color chooser binding listener list.
     */
    public ListenerList<ColorChooserBindingListener> getColorChooserBindingListeners() {
        return colorChooserBindingListeners;
    }
}
