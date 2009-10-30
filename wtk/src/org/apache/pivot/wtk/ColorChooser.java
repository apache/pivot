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

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.util.ListenerList;

/**
 * Component that allows the user to select a color.
 */
public class ColorChooser extends Container {
    /**
     * Calendar listener list.
     */
    private static class ColorChooserListenerList extends ListenerList<ColorChooserListener>
        implements ColorChooserListener {
        @Override
        public void selectedColorKeyChanged(ColorChooser colorChooser,
            String previousSelectedColorKey) {
            for (ColorChooserListener listener : this) {
                listener.selectedColorKeyChanged(colorChooser, previousSelectedColorKey);
            }
        }
    }

    /**
     * Color chooser selection listener list.
     */
    private static class ColorChooserSelectionListenerList
        extends ListenerList<ColorChooserSelectionListener>
        implements ColorChooserSelectionListener {

        @Override
        public void selectedColorChanged(ColorChooser colorChooser,
            Color previousSelectedColor) {
            for (ColorChooserSelectionListener listener : this) {
                listener.selectedColorChanged(colorChooser, previousSelectedColor);
            }
        }
    }

    private Color selectedColor = null;
    private String selectedColorKey = null;

    private ColorChooserListenerList colorChooserListeners = new ColorChooserListenerList();
    private ColorChooserSelectionListenerList colorChooserSelectionListeners =
        new ColorChooserSelectionListenerList();

    public ColorChooser() {
        installThemeSkin(ColorChooser.class);
    }

    /**
     * Gets the currently selected color, or <tt>null</tt> if no color is
     * selected.
     */
    public Color getSelectedColor() {
        return selectedColor;
    }

    /**
     * Sets the currently selected color.
     *
     * @param selectedColor
     * The selected color, or <tt>null</tt> to specify no selection
     */
    public void setSelectedColor(Color selectedColor) {
        Color previousSelectedColor = this.selectedColor;

        if (selectedColor != previousSelectedColor) {
            this.selectedColor = selectedColor;
            colorChooserSelectionListeners.selectedColorChanged(this, previousSelectedColor);
        }
    }

    /**
     * Sets the currently selected color.
     *
     * @param selectedColor
     * The selected color
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
            colorChooserListeners.selectedColorKeyChanged(this, previousSelectedColorKey);
        }
    }

    /**
     * Loads the selected color from the specified bind context using this color
     * chooser's bind key, if one is set.
     */
    @Override
    public void load(Dictionary<String, ?> context) {
        if (selectedColorKey != null
            && JSONSerializer.containsKey(context, selectedColorKey)) {
            Object value = JSONSerializer.get(context, selectedColorKey);

            if (value instanceof Color) {
                setSelectedColor((Color)value);
            } else if (value instanceof String) {
                setSelectedColor((String)value);
            } else {
                throw new IllegalArgumentException("Invalid color type: " +
                    value.getClass().getName());
            }
        }
    }

    /**
     * Stores the selected color into the specified bind context using this color
     * picker's bind key, if one is set.
     */
    @Override
    public void store(Dictionary<String, ?> context) {
        if (isEnabled()
            && selectedColorKey != null) {
            JSONSerializer.put(context, selectedColorKey, selectedColor);
        }
    }

    /**
     * Returns the color chooser listener list.
     */
    public ListenerList<ColorChooserListener> getColorChooserListeners() {
        return colorChooserListeners;
    }

    /**
     * Returns the color chooser selection listener list.
     */
    public ListenerList<ColorChooserSelectionListener> getColorChooserSelectionListeners() {
        return colorChooserSelectionListeners;
    }
}
