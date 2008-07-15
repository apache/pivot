package pivot.wtk;

import pivot.collections.HashMap;
import pivot.wtk.skin.BorderSkin;
import pivot.wtk.skin.CardPaneSkin;
import pivot.wtk.skin.FlowPaneSkin;
import pivot.wtk.skin.ImageViewSkin;
import pivot.wtk.skin.LabelSkin;
import pivot.wtk.skin.PopupSkin;
import pivot.wtk.skin.ScrollPaneSkin;
import pivot.wtk.skin.StackPaneSkin;
import pivot.wtk.skin.TablePaneSkin;
import pivot.wtk.skin.WindowSkin;

/**
 * Base class for Pivot themes. Note that concrete Theme implementations should
 * be declared as final. If multiple third-party libraries attempted to extend
 * a theme, it would cause a conflict, as only one could be used in any given
 * application.
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
        componentSkinMap.put(StackPane.class, StackPaneSkin.class);
        componentSkinMap.put(TablePane.class, TablePaneSkin.class);
        componentSkinMap.put(Window.class, WindowSkin.class);
    }

    public final Class<? extends Skin> getSkinClass(Class<? extends Component> componentClass) {
        return componentSkinMap.get(componentClass);
    }

    public abstract void install();
    public abstract void uninstall();

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
            theme.uninstall();
        }

        // TODO Walk existing component tree from display down and install new
        // skins; re-install skin by walking up class hierarchy until a skin
        // match is found (do this here in this method)

        Theme.theme = theme;
        theme.install();
    }
}
