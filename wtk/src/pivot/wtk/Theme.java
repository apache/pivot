/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk;

import java.awt.Font;

import pivot.collections.HashMap;
import pivot.wtk.skin.BorderSkin;
import pivot.wtk.skin.CardPaneSkin;
import pivot.wtk.skin.FlowPaneSkin;
import pivot.wtk.skin.ImageViewSkin;
import pivot.wtk.skin.LabelSkin;
import pivot.wtk.skin.PopupSkin;
import pivot.wtk.skin.ScrollPaneSkin;
import pivot.wtk.skin.SeparatorSkin;
import pivot.wtk.skin.StackPaneSkin;
import pivot.wtk.skin.TablePaneSkin;
import pivot.wtk.skin.WindowSkin;

/**
 * Base class for Pivot themes. A theme defines a complete "look and feel"
 * for a Pivot application.
 * <p>
 * Note that concrete Theme implementations should be declared as final. If
 * multiple third-party libraries attempted to extend a theme, it would cause a
 * conflict, as only one could be used in any given application.
 */
public abstract class Theme {
    protected HashMap<Class<? extends Component>, Class<? extends Skin>> componentSkinMap =
        new HashMap<Class<? extends Component>, Class<? extends Skin>>();

    private static Theme theme = null;

    public Theme() {
        componentSkinMap.put(Border.class, BorderSkin.class);
        componentSkinMap.put(CardPane.class, CardPaneSkin.class);
        componentSkinMap.put(FlowPane.class, FlowPaneSkin.class);
        componentSkinMap.put(ImageView.class, ImageViewSkin.class);
        componentSkinMap.put(Label.class, LabelSkin.class);
        componentSkinMap.put(Popup.class, PopupSkin.class);
        componentSkinMap.put(ScrollPane.class, ScrollPaneSkin.class);
        componentSkinMap.put(Separator.class, SeparatorSkin.class);
        componentSkinMap.put(StackPane.class, StackPaneSkin.class);
        componentSkinMap.put(TablePane.class, TablePaneSkin.class);
        componentSkinMap.put(Window.class, WindowSkin.class);
    }

    public final Class<? extends Skin> getSkinClass(Class<? extends Component> componentClass) {
        return componentSkinMap.get(componentClass);
    }

    public abstract void install();
    public abstract void uninstall();

    public abstract Font getFont();

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

        if (Theme.theme != null) {
            Theme.theme.uninstall();
        }

        theme.install();
        Theme.theme = theme;

        // TODO Walk existing component tree from display down and install new
        // skins by calling installSkin() on each component (except for display,
        // which is not skinnable)
    }
}
