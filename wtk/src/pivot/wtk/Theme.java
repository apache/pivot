package pivot.wtk;

import pivot.collections.HashMap;
import pivot.wtk.skin.WindowSkin;

public abstract class Theme {
    protected HashMap<Class<? extends Component>, Class<? extends Skin>> componentSkinMap =
        new HashMap<Class<? extends Component>, Class<? extends Skin>>();

    private static Theme theme = null;

    public Theme() {
        // TODO Add additional mappings

        componentSkinMap.put(Window.class, WindowSkin.class);
    }

    @SuppressWarnings("unchecked")
    public final Class<? extends Skin> getSkinClass(Class<? extends Component> componentClass) {
        Class<? extends Skin> skinClass = null;

        // Walk the class hierarchy of this component type to find a match
        while (componentClass != null
            && skinClass == null) {
            skinClass = componentSkinMap.get(componentClass);

            if (skinClass == null) {
                Class<?> superClass = componentClass.getSuperclass();
                componentClass = (superClass == Object.class) ?
                    null : (Class<? extends Component>)superClass;
            }
        }

        if (skinClass == null) {
            throw new IllegalArgumentException("Could not find skin for "
                + componentClass.getName());
        }

        return skinClass;
    }

    public static Theme getTheme() {
        if (theme == null) {
            throw new IllegalStateException("No installed theme.");
        }

        return theme;
    }

    public static void setTheme(Theme theme) {
        // TODO Walk existing component tree from display down and install new
        // skins

        Theme.theme = theme;
    }
}
