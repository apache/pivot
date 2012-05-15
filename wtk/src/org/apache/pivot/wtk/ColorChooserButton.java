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
import org.apache.pivot.wtk.content.ListButtonColorItemRenderer;

/**
 * A component that allows a user to select a color. The color chooser
 * is hidden until the user pushes the button.
 */
public class ColorChooserButton extends Button {
    /**
     * ColorChooser button selection listener list.
     */
    private static class ColorChooserButtonSelectionListenerList
        extends WTKListenerList<ColorChooserButtonSelectionListener>
        implements ColorChooserButtonSelectionListener {

        @Override
        public void selectedColorChanged(ColorChooserButton colorChooserButton,
            Color previousSelectedColor) {
            for (ColorChooserButtonSelectionListener listener : this) {
                listener.selectedColorChanged(colorChooserButton, previousSelectedColor);
            }
        }
    }

    /**
     * Color chooser button binding listener list.
     */
    private static class ColorChooserButtonBindingListenerList
        extends WTKListenerList<ColorChooserButtonBindingListener>
        implements ColorChooserButtonBindingListener {
        @Override
        public void selectedColorKeyChanged(ColorChooserButton colorChooserButton,
            String previousSelectedColorKey) {
            for (ColorChooserButtonBindingListener listener : this) {
                listener.selectedColorKeyChanged(colorChooserButton, previousSelectedColorKey);
            }
        }

        @Override
        public void selectedColorBindTypeChanged(ColorChooserButton colorChooserButton,
            BindType previousSelectedColorBindType) {
            for (ColorChooserButtonBindingListener listener : this) {
                listener.selectedColorBindTypeChanged(colorChooserButton,
                    previousSelectedColorBindType);
            }
        }

        @Override
        public void selectedColorBindMappingChanged(ColorChooserButton colorChooserButton,
            ColorChooser.SelectedColorBindMapping previousSelectedColorBindMapping) {
            for (ColorChooserButtonBindingListener listener : this) {
                listener.selectedColorBindMappingChanged(colorChooserButton,
                    previousSelectedColorBindMapping);
            }
        }
    }

    /**
     * ColorChooserButton skin interface. ColorChooserButton skins must implement
     * this interface to facilitate additional communication between the
     * component and the skin.
     */
    public interface Skin {
        public Window getColorChooserPopup();
    }

    private Color selectedColor = null;

    private String selectedColorKey = null;
    private BindType selectedColorBindType = BindType.BOTH;
    private ColorChooser.SelectedColorBindMapping selectedColorBindMapping = null;

    private ColorChooserButtonSelectionListenerList colorChooserButtonSelectionListeners =
        new ColorChooserButtonSelectionListenerList();
    private ColorChooserButtonBindingListenerList colorChooserButtonBindingListeners =
        new ColorChooserButtonBindingListenerList();

    private static final Button.DataRenderer DEFAULT_DATA_RENDERER =
        new ListButtonColorItemRenderer();

    public ColorChooserButton() {
        this(null);
    }

    public ColorChooserButton(Object buttonData) {
        super(buttonData);

        setDataRenderer(DEFAULT_DATA_RENDERER);
        installSkin(ColorChooserButton.class);
    }

    @Override
    protected void setSkin(org.apache.pivot.wtk.Skin skin) {
        if (!(skin instanceof ColorChooserButton.Skin)) {
            throw new IllegalArgumentException("Skin class must implement "
                + ColorChooserButton.Skin.class.getName());
        }

        super.setSkin(skin);
    }

    /**
     * @return the popup window associated with this components skin
     */
    public Window getListPopup() {
        return ((ColorChooserButton.Skin) getSkin()).getColorChooserPopup();
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

        if (previousSelectedColor != selectedColor
            && (previousSelectedColor == null
                || !previousSelectedColor.equals(selectedColor))) {
            this.selectedColor = selectedColor;
            colorChooserButtonSelectionListeners.selectedColorChanged(this,
                previousSelectedColor);
        }
    }

    /**
     * Sets the selected color.
     *
     * @param selectedColor
     * A string representing a color.
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
            colorChooserButtonBindingListeners.selectedColorKeyChanged(this,
                previousSelectedColorKey);
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
            colorChooserButtonBindingListeners.selectedColorBindTypeChanged(this,
                previousSelectedColorBindType);
        }
    }

    public ColorChooser.SelectedColorBindMapping getSelectedColorBindMapping() {
        return selectedColorBindMapping;
    }

    public void setSelectedColorBindMapping(ColorChooser.SelectedColorBindMapping bindMapping) {
        ColorChooser.SelectedColorBindMapping previousSelectedColorBindMapping =
            this.selectedColorBindMapping;

        if (previousSelectedColorBindMapping != bindMapping) {
            this.selectedColorBindMapping = bindMapping;
            colorChooserButtonBindingListeners.selectedColorBindMappingChanged(this,
                previousSelectedColorBindMapping);
        }
    }

    /**
     * Loads the selected color from the specified bind context using this color
     * picker button's bind key, if one is set.
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
     * picker button's bind key, if one is set.
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
     * Returns the color chooser button selection listener list.
     */
    public ListenerList<ColorChooserButtonSelectionListener> getColorChooserButtonSelectionListeners() {
        return colorChooserButtonSelectionListeners;
    }

    /**
     * Returns the color chooser button binding listener list.
     */
    public ListenerList<ColorChooserButtonBindingListener> getColorChooserButtonBindingListeners() {
        return colorChooserButtonBindingListeners;
    }
}
