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
import org.apache.pivot.serialization.JSON;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.content.ListButtonColorItemRenderer;

/**
 * A component that allows a user to select a color. The color chooser
 * is hidden until the user pushes the button.
 */
public class ColorChooserButton extends Button {
    /**
     * Color chooser button listener list.
     */
    private static class ColorChooserButtonListenerList
        extends ListenerList<ColorChooserButtonListener>
        implements ColorChooserButtonListener {
        @Override
        public void selectedColorKeyChanged(ColorChooserButton colorChooserButton,
            String previousSelectedColorKey) {
            for (ColorChooserButtonListener listener : this) {
                listener.selectedColorKeyChanged(colorChooserButton, previousSelectedColorKey);
            }
        }
    }

    /**
     * ColorChooser button selection listener list.
     */
    private static class ColorChooserButtonSelectionListenerList
        extends ListenerList<ColorChooserButtonSelectionListener>
        implements ColorChooserButtonSelectionListener {

        @Override
        public void selectedColorChanged(ColorChooserButton colorChooserButton,
            Color previousSelectedColor) {
            for (ColorChooserButtonSelectionListener listener : this) {
                listener.selectedColorChanged(colorChooserButton, previousSelectedColor);
            }
        }
    }

    private Color selectedColor = null;
    private String selectedColorKey = null;

    private ColorChooserButtonListenerList colorChooserButtonListeners =
        new ColorChooserButtonListenerList();
    private ColorChooserButtonSelectionListenerList colorChooserButtonSelectionListeners =
        new ColorChooserButtonSelectionListenerList();

    private static final Button.DataRenderer DEFAULT_DATA_RENDERER =
        new ListButtonColorItemRenderer();

    public ColorChooserButton() {
        this(null);
    }

    public ColorChooserButton(Object buttonData) {
        super(buttonData);

        setDataRenderer(DEFAULT_DATA_RENDERER);
        installThemeSkin(ColorChooserButton.class);
    }

    /**
     * @throws UnsupportedOperationException
     * This method is not supported by ColorChooserButton.
     */
    @Override
    public void setToggleButton(boolean toggleButton) {
        throw new UnsupportedOperationException("Color chooser buttons cannot be toggle buttons.");
    }

    /**
     * Returns the currently selected color.
     *
     * @return
     * The currently selected color, or <tt>null</tt> if nothing is selected.
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

        if (previousSelectedColor != selectedColor) {
            this.selectedColor = selectedColor;
            colorChooserButtonSelectionListeners.selectedColorChanged(this,
                previousSelectedColor);
        }
    }

    /**
     * Sets the selected color to the color represented by the specified color
     * string.
     *
     * @param selectedColor
     * A string representing a color
     */
    public final void setSelectedColor(String selectedColor) {
        if (selectedColor == null) {
            throw new IllegalArgumentException("selectedColor is null.");
        }

        setSelectedColor(Color.decode(selectedColor));
    }

    /**
     * Gets the data binding key that is set on this color chooser button.
     */
    public String getSelectedColorKey() {
        return selectedColorKey;
    }

    /**
     * Sets this color chooser button's data binding key.
     */
    public void setSelectedColorKey(String selectedColorKey) {
        String previousSelectedColorKey = this.selectedColorKey;

        if (previousSelectedColorKey != selectedColorKey) {
            this.selectedColorKey = selectedColorKey;
            colorChooserButtonListeners.selectedColorKeyChanged(this, previousSelectedColorKey);
        }
    }

    /**
     * Loads the selected color from the specified bind context using this color
     * picker button's bind key, if one is set.
     */
    @Override
    public void load(Dictionary<String, ?> context) {
        String selectedColorKey = getSelectedColorKey();

        if (selectedColorKey != null
            && JSON.containsKey(context, selectedColorKey)) {
            Object value = JSON.get(context, selectedColorKey);

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
     * picker button's bind key, if one is set.
     */
    @Override
    public void store(Dictionary<String, ?> context) {
        if (isEnabled()
            && selectedColorKey != null) {
            JSON.put(context, selectedColorKey, selectedColor);
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
     * Returns the color chooser button listener list.
     */
    public ListenerList<ColorChooserButtonListener> getColorChooserButtonListeners() {
        return colorChooserButtonListeners;
    }

    /**
     * Returns the color chooser button selection listener list.
     */
    public ListenerList<ColorChooserButtonSelectionListener> getColorChooserButtonSelectionListeners() {
        return colorChooserButtonSelectionListeners;
    }
}
