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
import java.awt.Font;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.util.Service;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.skin.BorderSkin;
import org.apache.pivot.wtk.skin.BoxPaneSkin;
import org.apache.pivot.wtk.skin.CardPaneSkin;
import org.apache.pivot.wtk.skin.ColorChooserButtonSkin;
import org.apache.pivot.wtk.skin.ComponentSkin;
import org.apache.pivot.wtk.skin.FillPaneSkin;
import org.apache.pivot.wtk.skin.FlowPaneSkin;
import org.apache.pivot.wtk.skin.GridPaneFillerSkin;
import org.apache.pivot.wtk.skin.GridPaneSkin;
import org.apache.pivot.wtk.skin.ImageViewSkin;
import org.apache.pivot.wtk.skin.LabelSkin;
import org.apache.pivot.wtk.skin.MovieViewSkin;
import org.apache.pivot.wtk.skin.NumberRulerSkin;
import org.apache.pivot.wtk.skin.PanelSkin;
import org.apache.pivot.wtk.skin.RulerSkin;
import org.apache.pivot.wtk.skin.ScrollPaneSkin;
import org.apache.pivot.wtk.skin.SeparatorSkin;
import org.apache.pivot.wtk.skin.StackPaneSkin;
import org.apache.pivot.wtk.skin.TablePaneFillerSkin;
import org.apache.pivot.wtk.skin.TablePaneSkin;
import org.apache.pivot.wtk.skin.TextAreaSkin;
import org.apache.pivot.wtk.skin.TextPaneSkin;
import org.apache.pivot.wtk.skin.WindowSkin;

/**
 * Base class for Pivot themes. A theme defines a complete "look and feel" for a
 * Pivot application. <p> Note that concrete Theme implementations should be
 * declared as final. If multiple third-party libraries attempted to extend a
 * theme, it would cause a conflict, as only one could be used in any given
 * application. <p> IMPORTANT All skin mappings must be added to the map, even
 * non-static inner classes. Otherwise, the component's base class will attempt
 * to install its own skin, which will result in the addition of duplicate
 * listeners.
 */
public abstract class Theme {
    /**
     * The map between the components and their associated skins.
     * <p>Note: there are some skins that are partially or completely implemented
     * in the "wtk" classes, and thus have entries made in here.  Most, however,
     * have skins implemented in a specific theme, and therefore are initialized
     * by the specific theme subclass.
     */
    private HashMap<Class<? extends Component>, Class<? extends Skin>> componentSkinMap = new HashMap<>();

    /**
     * The single installed theme for the application.
     */
    private static Theme theme = null;

    public static final String NAME_KEY = "name";
    public static final String SIZE_KEY = "size";
    public static final String BOLD_KEY = "bold";
    public static final String ITALIC_KEY = "italic";

    /**
     * The service provider name (see {@link Service#getProvider(String)}).
     */
    public static final String PROVIDER_NAME = Theme.class.getName();

    static {
        theme = (Theme) Service.getProvider(PROVIDER_NAME);

        if (theme == null) {
            throw new ThemeNotFoundException();
        }
    }

    /**
     * Construct the generic theme by assigning default skin classes
     * to some components.
     */
    public Theme() {
        set(Border.class, BorderSkin.class);
        set(BoxPane.class, BoxPaneSkin.class);
        set(CardPane.class, CardPaneSkin.class);
        set(ColorChooserButtonSkin.ColorChooserPopup.class, ColorChooserButtonSkin.ColorChooserPopupSkin.class);
        set(FillPane.class, FillPaneSkin.class);
        set(FlowPane.class, FlowPaneSkin.class);
        set(GridPane.class, GridPaneSkin.class);
        set(GridPane.Filler.class, GridPaneFillerSkin.class);
        set(ImageView.class, ImageViewSkin.class);
        set(Label.class, LabelSkin.class);
        set(MovieView.class, MovieViewSkin.class);
        set(NumberRuler.class, NumberRulerSkin.class);
        set(Panel.class, PanelSkin.class);
        set(Ruler.class, RulerSkin.class);
        set(ScrollPane.class, ScrollPaneSkin.class);
        set(Separator.class, SeparatorSkin.class);
        set(StackPane.class, StackPaneSkin.class);
        set(TablePane.class, TablePaneSkin.class);
        set(TablePane.Filler.class, TablePaneFillerSkin.class);
        set(TextArea.class, TextAreaSkin.class);
        set(TextPane.class, TextPaneSkin.class);
        set(Window.class, WindowSkin.class);
    }

    /**
     * Returns the skin class responsible for skinning the specified component
     * class.
     *
     * @param componentClass The component class.
     * @return The skin class, or <tt>null</tt> if no skin mapping exists for
     * the component class.
     * @throws IllegalArgumentException if the given component class is <tt>null</tt>.
     */
    public final Class<? extends Skin> getSkinClass(final Class<? extends Component> componentClass) {
        return componentSkinMap.get(componentClass);
    }

    /**
     * Sets the skin class responsible for skinning the specified component class.
     *
     * @param componentClass The component class.
     * @param skinClass The skin class.
     */
    public void set(final Class<? extends Component> componentClass, final Class<? extends Skin> skinClass) {
        Utils.checkNull(componentClass, "Component class");
        Utils.checkNull(skinClass, "Skin class");

        // Make sure if an override is made of a class that already has a skin that the new one
        // is a subclass of the initial one.
        Class<? extends Skin> existingSkinClass = componentSkinMap.get(componentClass);
        if (existingSkinClass != null) {
            if (existingSkinClass.isAssignableFrom(skinClass)) {
                componentSkinMap.put(componentClass, skinClass);
            } else {
                throw new RuntimeException("Incompatible skin class " + skinClass.getName()
                    + " being assigned to " + componentClass.getName() + " class.");
            }
        } else {
            componentSkinMap.put(componentClass, skinClass);
        }
    }

    /**
     * @return The theme's font.
     */
    public abstract Font getFont();

    /**
     * Sets the theme's font.
     *
     * @param font The font.
     */
    public abstract void setFont(Font font);

    /**
     * @return A color from the theme's base color palette.
     *
     * @param index The index of the color, starting from 0.
     */
    public abstract Color getBaseColor(int index);

    /**
     * Sets a color in the theme's base color palette.
     *
     * @param index the index of the color, starting from 0
     * @param baseColor the color to set
     */
    public abstract void setBaseColor(int index, Color baseColor);

    /**
     * @return A value from the theme's complete color palette (including derived
     * colors, if any).
     *
     * @param index The index of the color, starting from 0.
     */
    public abstract Color getColor(int index);

    /**
     * Sets a value in the theme's complete color palette (including derived
     * colors, if any).
     *
     * @param index the index of the color, starting from 0
     * @param color the color to set
     */
    public abstract void setColor(int index, Color color);

    /**
     * Gets the number of Palette Colors.
     *
     * @return The number of colors in the theme's palette.
     */
    public abstract int getNumberOfPaletteColors();

    /**
     * Gets the total number of Colors (including derived colors, if any).
     *
     * @return The total number of colors.
     */
    public abstract int getNumberOfColors();

    /**
     * Tell if the theme is dark.<br> Usually this means that (if true) any
     * color will be transformed in the opposite way (brightening instead of
     * darkening, and darkening instead of brightening).
     * <p>Note: this value is set in the theme properties file.
     *
     * @return {@code true} if dark, {@code false} otherwise.
     */
    public abstract boolean isThemeDark();

    /**
     * Tell if the theme is flat.<br> Usually this means that (if true) any
     * border/shadow will not be drawn.
     *
     * @return {@code true} if flat, {@code false} otherwise.
     */
    public abstract boolean isThemeFlat();

    /**
     * Tell if the theme has transitions enabled.<br> Usually this means that (if false) any
     * effect/transition will not be drawn.
     *
     * @return {@code true} if enabled (default), {@code false} otherwise.
     */
    public abstract boolean isTransitionEnabled();

    /**
     * Set appropriate default styles for the given skin object, specified by the
     * current theme.
     *
     * @param <T>  The skin class whose type we are dealing with.
     * @param skin The skin object of that type whose styles are to be set.
     */
    public abstract <T extends ComponentSkin> void setDefaultStyles(T skin);

    /**
     * Returns a safe (and general) default background color.
     *
     * @return White if the theme is not dark (default), or Black.
     */
    public Color getDefaultBackgroundColor() {
        return isThemeDark() ? Color.BLACK : Color.WHITE;
    }

    /**
     * Returns a safe (and general) default foreground color.
     *
     * @return Black if the theme is not dark (default), or White otherwise.
     */
    public Color getDefaultForegroundColor() {
        return isThemeDark() ? Color.WHITE : Color.BLACK;
    }

    /**
     * @return The current theme, as determined by the {@linkplain #PROVIDER_NAME
     * theme provider}.
     *
     * @throws IllegalStateException If a theme has not been installed.
     */
    public static Theme getTheme() {
        if (theme == null) {
            throw new ThemeNotFoundException("No installed theme.");
        }

        return theme;
    }

    /**
     * Produce a font by describing it relative to the current theme's font.
     *
     * @param dictionary A dictionary with any of the following keys:
     * <ul>
     * <li>{@value #NAME_KEY} - the family name of the font</li>
     * <li>{@value #SIZE_KEY} - the font size as an integer, or a string "x%" for a
     * relative size</li>
     * <li>{@value #BOLD_KEY} - true/false</li>
     * <li>{@value #ITALIC_KEY} - true/false</li>
     * </ul>
     * Omitted values are taken from the theme's font.
     * @return The new font derived from the current font.
     * @throws IllegalArgumentException if the supplied dictionary is <tt>null</tt>.
     */
    public static Font deriveFont(final Dictionary<String, ?> dictionary) {
        Utils.checkNull(dictionary, "dictionary");

        Font font = theme.getFont();

        String name = font.getName();
        if (dictionary.containsKey(NAME_KEY)) {
            name = (String) dictionary.get(NAME_KEY);
        }

        int size = font.getSize();
        if (dictionary.containsKey(SIZE_KEY)) {
            Object value = dictionary.get(SIZE_KEY);

            if (value instanceof String) {
                String string = (String) value;

                if (string.endsWith("%")) {
                    float percentage = Float.parseFloat(string.substring(0, string.length() - 1)) / 100f;
                    size = Math.round(font.getSize() * percentage);
                } else {
                    throw new IllegalArgumentException(value + " is not a valid font size.");
                }
            } else {
                size = ((Integer) value).intValue();
            }
        }

        int style = font.getStyle();
        if (dictionary.containsKey(BOLD_KEY)) {
            boolean bold = dictionary.getBoolean(BOLD_KEY);

            if (bold) {
                style |= Font.BOLD;
            } else {
                style &= ~Font.BOLD;
            }
        }

        if (dictionary.containsKey(ITALIC_KEY)) {
            boolean italic = dictionary.getBoolean(ITALIC_KEY);

            if (italic) {
                style |= Font.ITALIC;
            } else {
                style &= ~Font.ITALIC;
            }
        }

        return new Font(name, style, size);
    }

}
