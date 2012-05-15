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
         * @param value
         */
        public Color toColor(Object value);

        /**
         * Converts a color to a context value.
         *
         * @param color
         */
        public Object valueOf(Color color);
    }

    /**
     * Color chooser selection listener list.
     */
    private static class ColorChooserSelectionListenerList
        extends WTKListenerList<ColorChooserSelectionListener>
        implements ColorChooserSelectionListener {

        @Override
        public void selectedColorChanged(ColorChooser colorChooser,
            Color previousSelectedColor) {
            for (ColorChooserSelectionListener listener : this) {
                listener.selectedColorChanged(colorChooser, previousSelectedColor);
            }
        }
    }

    /**
     * Color chooser binding listener list.
     */
    private static class ColorChooserBindingListenerList
        extends WTKListenerList<ColorChooserBindingListener>
        implements ColorChooserBindingListener {
        @Override
        public void selectedColorKeyChanged(ColorChooser colorChooser,
            String previousSelectedColorKey) {
            for (ColorChooserBindingListener listener : this) {
                listener.selectedColorKeyChanged(colorChooser, previousSelectedColorKey);
            }
        }

        @Override
        public void selectedColorBindTypeChanged(ColorChooser colorChooser,
            BindType previousSelectedColorBindType) {
            for (ColorChooserBindingListener listener : this) {
                listener.selectedColorBindTypeChanged(colorChooser, previousSelectedColorBindType);
            }
        }

        @Override
        public void selectedColorBindMappingChanged(ColorChooser colorChooser,
            SelectedColorBindMapping previousSelectedColorBindMapping) {
            for (ColorChooserBindingListener listener : this) {
                listener.selectedColorBindMappingChanged(colorChooser,
                    previousSelectedColorBindMapping);
            }
        }
    }

    private Color selectedColor = null;

    private String selectedColorKey = null;
    private BindType selectedColorBindType = BindType.BOTH;
    private SelectedColorBindMapping selectedColorBindMapping = null;

    private ColorChooserSelectionListenerList colorChooserSelectionListeners =
        new ColorChooserSelectionListenerList();
    private ColorChooserBindingListenerList colorChooserBindingListeners =
        new ColorChooserBindingListenerList();

    public ColorChooser() {
        installSkin(ColorChooser.class);
    }

    /**
     * Gets the currently selected color, or <tt>null</tt> if no color is
     * selected.
     */
    public Color getSelectedColor() {
        return selectedColor;
    }

    /**
     * Sets the selected color.
     *
     * @param selectedColor
     * The color to select, or <tt>null</tt> to clear the selection.
     */
    public void setSelectedColor(Color selectedColor) {
        Color previousSelectedColor = this.selectedColor;

        if (previousSelectedColor != selectedColor
            && (previousSelectedColor == null
                || !previousSelectedColor.equals(selectedColor))) {
            this.selectedColor = selectedColor;
            colorChooserSelectionListeners.selectedColorChanged(this, previousSelectedColor);
        }
    }

    /**
     * Sets the selected color.
     *
     * @param selectedColor
     * The color to select, or <tt>null</tt> to clear the selection.
     */
    public void setSelectedColor(String selectedColor) {
        if (selectedColor == null) {
            throw new IllegalArgumentException("selectedColor is null.");
        }

        setSelectedColor(Color.decode(selectedColor));
    }

    /**
     * Gets the data binding key that is set on this color chooser.
     */
    public String getSelectedColorKey() {
        return selectedColorKey;
    }

    /**
     * Sets this color chooser's data binding key.
     */
    public void setSelectedColorKey(String selectedColorKey) {
        String previousSelectedColorKey = this.selectedColorKey;

        if (selectedColorKey != previousSelectedColorKey) {
            this.selectedColorKey = selectedColorKey;
            colorChooserBindingListeners.selectedColorKeyChanged(this, previousSelectedColorKey);
        }
    }

    public BindType getSelectedColorBindType() {
        return selectedColorBindType;
    }

    public void setSelectedColorBindType(BindType selectedColorBindType) {
        if (selectedColorBindType == null) {
            throw new IllegalArgumentException();
        }

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

    public void setSelectedColorBindMapping(SelectedColorBindMapping selectedColorBindMapping) {
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
    public void load(Object context) {
        if (selectedColorKey != null
            && JSON.containsKey(context, selectedColorKey)
            && selectedColorBindType != BindType.STORE) {
            Object value = JSON.get(context, selectedColorKey);

            Color selectedColorLocal = null;

            if (value instanceof Color) {
                selectedColorLocal = (Color)value;
            } else if (selectedColorBindMapping == null) {
                if (value != null) {
                    selectedColorLocal = Color.decode(value.toString());
                }
            } else {
                selectedColorLocal = selectedColorBindMapping.toColor(value);
            }

            setSelectedColor(selectedColorLocal);
        }
    }

    /**
     * Stores the selected color into the specified bind context using this color
     * chooser's bind key, if one is set.
     */
    @Override
    public void store(Object context) {
        if (selectedColorKey != null
            && selectedColorBindType != BindType.LOAD) {
            JSON.put(context, selectedColorKey, (selectedColorBindMapping == null) ?
                selectedColor : selectedColorBindMapping.valueOf(selectedColor));
        }
    }

    /**
     * If a bind key is set, clears the selected color.
     */
    @Override
    public void clear() {
        if (selectedColorKey != null) {
            setSelectedColor((Color)null);
        }
    }

    /**
     * Returns the color chooser selection listener list.
     */
    public ListenerList<ColorChooserSelectionListener> getColorChooserSelectionListeners() {
        return colorChooserSelectionListeners;
    }

    /**
     * Returns the color chooser binding listener list.
     */
    public ListenerList<ColorChooserBindingListener> getColorChooserBindingListeners() {
        return colorChooserBindingListeners;
    }
}
