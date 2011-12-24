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

import java.awt.Font;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.util.Service;
import org.apache.pivot.wtk.skin.BorderSkin;
import org.apache.pivot.wtk.skin.BoxPaneSkin;
import org.apache.pivot.wtk.skin.CardPaneSkin;
import org.apache.pivot.wtk.skin.ColorChooserButtonSkin;
import org.apache.pivot.wtk.skin.FillPaneSkin;
import org.apache.pivot.wtk.skin.FlowPaneSkin;
import org.apache.pivot.wtk.skin.GridPaneFillerSkin;
import org.apache.pivot.wtk.skin.GridPaneSkin;
import org.apache.pivot.wtk.skin.ImageViewSkin;
import org.apache.pivot.wtk.skin.LabelSkin;
import org.apache.pivot.wtk.skin.MovieViewSkin;
import org.apache.pivot.wtk.skin.PanelSkin;
import org.apache.pivot.wtk.skin.ScrollPaneSkin;
import org.apache.pivot.wtk.skin.SeparatorSkin;
import org.apache.pivot.wtk.skin.StackPaneSkin;
import org.apache.pivot.wtk.skin.TablePaneFillerSkin;
import org.apache.pivot.wtk.skin.TablePaneSkin;
import org.apache.pivot.wtk.skin.TextPaneSkin;
import org.apache.pivot.wtk.skin.TextAreaSkin;
import org.apache.pivot.wtk.skin.WindowSkin;

/**
 * Base class for Pivot themes. A theme defines a complete "look and feel"
 * for a Pivot application.
 * <p>
 * Note that concrete Theme implementations should be declared as final. If
 * multiple third-party libraries attempted to extend a theme, it would cause a
 * conflict, as only one could be used in any given application.
 * <p>
 * IMPORTANT All skin mappings must be added to the map, even non-static inner
 * classes. Otherwise, the component's base class will attempt to install its
 * own skin, which will result in the addition of duplicate listeners.
 */
public abstract class Theme {
    protected HashMap<Class<? extends Component>, Class<? extends Skin>> componentSkinMap =
        new HashMap<Class<? extends Component>, Class<? extends Skin>>();

    private static Theme theme = null;

    public static final String NAME_KEY = "name";
    public static final String SIZE_KEY = "size";
    public static final String BOLD_KEY = "bold";
    public static final String ITALIC_KEY = "italic";

    /**
     * The service provider name (see {@link Service#getProvider(String)}).
     */
    public static final String PROVIDER_NAME = "org.apache.pivot.wtk.Theme";

    static {
        theme = (Theme)Service.getProvider(PROVIDER_NAME);

        if (theme == null) {
            throw new ThemeNotFoundException();
        }
    }

    public Theme() {
        componentSkinMap.put(Border.class, BorderSkin.class);
        componentSkinMap.put(BoxPane.class, BoxPaneSkin.class);
        componentSkinMap.put(CardPane.class, CardPaneSkin.class);
        componentSkinMap.put(ColorChooserButtonSkin.ColorChooserPopup.class,
            ColorChooserButtonSkin.ColorChooserPopupSkin.class);
        componentSkinMap.put(FillPane.class, FillPaneSkin.class);
        componentSkinMap.put(FlowPane.class, FlowPaneSkin.class);
        componentSkinMap.put(GridPane.class, GridPaneSkin.class);
        componentSkinMap.put(GridPane.Filler.class, GridPaneFillerSkin.class);
        componentSkinMap.put(ImageView.class, ImageViewSkin.class);
        componentSkinMap.put(Label.class, LabelSkin.class);
        componentSkinMap.put(MovieView.class, MovieViewSkin.class);
        componentSkinMap.put(Panel.class, PanelSkin.class);
        componentSkinMap.put(ScrollPane.class, ScrollPaneSkin.class);
        componentSkinMap.put(Separator.class, SeparatorSkin.class);
        componentSkinMap.put(StackPane.class, StackPaneSkin.class);
        componentSkinMap.put(TablePane.class, TablePaneSkin.class);
        componentSkinMap.put(TablePane.Filler.class, TablePaneFillerSkin.class);
        componentSkinMap.put(TextArea.class, TextAreaSkin.class);
        componentSkinMap.put(TextPane.class, TextPaneSkin.class);
        componentSkinMap.put(Window.class, WindowSkin.class);
    }

    public final Class<? extends Skin> getSkinClass(Class<? extends Component> componentClass) {
        return componentSkinMap.get(componentClass);
    }

    public abstract Font getFont();
    public abstract void setFont(Font font);

    /**
     * Returns the skin class responsible for skinning the specified component
     * class.
     *
     * @param componentClass
     * The component class.
     *
     * @return
     * The skin class, or <tt>null</tt> if no skin mapping exists for the
     * component class.
     */
    public Class<? extends Skin> get(Class<? extends Component> componentClass) {
        if (componentClass == null) {
            throw new IllegalArgumentException("Component class is null.");
        }

        return componentSkinMap.get(componentClass);
    }

    /**
     * Sets the skin class responsible for skinning the specified component
     * class.
     *
     * @param componentClass
     * The component class.
     *
     * @param skinClass
     * The skin class.
     */
    public void set(Class<? extends Component> componentClass, Class<? extends Skin> skinClass) {
        if (componentClass == null) {
            throw new IllegalArgumentException("Component class is null.");
        }

        if (skinClass == null) {
            throw new IllegalArgumentException("Skin class is null.");
        }

        componentSkinMap.put(componentClass, skinClass);
    }

    /**
     * Gets the current theme, as determined by the {@linkplain #PROVIDER_NAME
     * theme provider}.
     *
     * @throws IllegalStateException
     * If a theme has not been installed.
     */
    public static Theme getTheme() {
        if (theme == null) {
            throw new IllegalStateException("No installed theme.");
        }

        return theme;
    }

    /**
     * Produce a font by describing it relative to the current theme's font
     * @param dictionary A dictionary with any of the following keys:
     * <ul>
     * <li>{@value #NAME_KEY} - the family name of the font</li>
     * <li>{@value #SIZE_KEY} - the font size as an integer, or a string "x%" for a relative size</li>
     * <li>{@value #BOLD_KEY} - true/false</li>
     * <li>{@value #ITALIC_KEY} - true/false</li>
     * </ul>
     * Omitted values are taken from the theme's font.
     */
    public static Font deriveFont(Dictionary<String, ?> dictionary) {
        Font font = theme.getFont();

        String name = font.getName();
        if (dictionary.containsKey(NAME_KEY)) {
            name = (String)dictionary.get(NAME_KEY);
        }

        int size = font.getSize();
        if (dictionary.containsKey(SIZE_KEY)) {
            Object value = dictionary.get(SIZE_KEY);

            if (value instanceof String) {
                String string = (String)value;

                if (string.endsWith("%")) {
                    float percentage = Float.parseFloat(string.substring(0, string.length() - 1)) / 100f;
                    size = Math.round(font.getSize() * percentage);
                } else {
                    throw new IllegalArgumentException(value + " is not a valid font size.");
                }
            } else {
                size = (Integer)value;
            }
        }

        int style = font.getStyle();
        if (dictionary.containsKey(BOLD_KEY)) {
            boolean bold = (Boolean)dictionary.get(BOLD_KEY);

            if (bold) {
                style |= Font.BOLD;
            } else {
                style &= ~Font.BOLD;
            }
        }

        if (dictionary.containsKey(ITALIC_KEY)) {
            boolean italic = (Boolean)dictionary.get(ITALIC_KEY);

            if (italic) {
                style |= Font.ITALIC;
            } else {
                style &= ~Font.ITALIC;
            }
        }

        return new Font(name, style, size);
    }
}
