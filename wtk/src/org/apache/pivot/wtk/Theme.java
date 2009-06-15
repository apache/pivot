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
import java.lang.reflect.Modifier;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.util.Service;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.BorderSkin;
import org.apache.pivot.wtk.skin.CardPaneSkin;
import org.apache.pivot.wtk.skin.FlowPaneSkin;
import org.apache.pivot.wtk.skin.ImageViewSkin;
import org.apache.pivot.wtk.skin.LabelSkin;
import org.apache.pivot.wtk.skin.MovieViewSkin;
import org.apache.pivot.wtk.skin.PanelSkin;
import org.apache.pivot.wtk.skin.ScrollPaneSkin;
import org.apache.pivot.wtk.skin.SeparatorSkin;
import org.apache.pivot.wtk.skin.StackPaneSkin;
import org.apache.pivot.wtk.skin.TablePaneSkin;
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

    public static final String PROVIDER_NAME = "org.apache.pivot.wtk.Theme";

    private static Theme theme = null;

    static {
        Theme theme = (Theme)Service.getProvider(PROVIDER_NAME);

        if (theme == null) {
            throw new ThemeNotFoundException();
        }

        setTheme(theme);
    }

    public Theme() {
        componentSkinMap.put(Border.class, BorderSkin.class);
        componentSkinMap.put(CardPane.class, CardPaneSkin.class);
        componentSkinMap.put(FlowPane.class, FlowPaneSkin.class);
        componentSkinMap.put(ImageView.class, ImageViewSkin.class);
        componentSkinMap.put(Label.class, LabelSkin.class);
        componentSkinMap.put(MovieView.class, MovieViewSkin.class);
        componentSkinMap.put(Panel.class, PanelSkin.class);
        componentSkinMap.put(ScrollPane.class, ScrollPaneSkin.class);
        componentSkinMap.put(Separator.class, SeparatorSkin.class);
        componentSkinMap.put(StackPane.class, StackPaneSkin.class);
        componentSkinMap.put(TablePane.class, TablePaneSkin.class);
        componentSkinMap.put(TextArea.class, TextAreaSkin.class);
        componentSkinMap.put(Window.class, WindowSkin.class);
    }

    public final Class<? extends Skin> getSkinClass(Class<? extends Component> componentClass) {
        return componentSkinMap.get(componentClass);
    }

    protected abstract void install();
    protected abstract void uninstall();

    public abstract Font getFont();
    public abstract Image getMessageIcon(MessageType messageType);
    public abstract Image getSmallMessageIcon(MessageType messageType);

    public static Theme getTheme() {
        if (theme == null) {
            throw new IllegalStateException("No installed theme.");
        }

        return theme;
    }

    public static void setTheme(Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("theme is null.");
        }

        Theme previousTheme = Theme.theme;
        if (previousTheme != null) {
            previousTheme.uninstall();
        }

        theme.install();
        Theme.theme = theme;

        if (previousTheme != null) {
            Component.ComponentDictionary components = Component.getComponents();
            ArrayList<Integer> componentHandles = new ArrayList<Integer>();

            for (Integer handle : components) {
                componentHandles.add(handle);
            }

            for (Integer handle : componentHandles) {
                Component component = components.get(handle);
                Class<? extends Component> componentClass = component.getClass();

                if (theme.componentSkinMap.containsKey(componentClass)
                    && (componentClass.getEnclosingClass() == null
                        || (componentClass.getModifiers() & Modifier.STATIC) == Modifier.STATIC)) {
                    component.installSkin(componentClass);
                }
            }
        }
    }
}
