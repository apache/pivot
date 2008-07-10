package pivot.wtk;

public abstract class Theme {
    private static Theme theme = null;

    public abstract Class<? extends Skin> getSkinClass(Class<? extends Component> componentClass);

    public static Theme getTheme() {
        return theme;
    }

    public static void setTheme(Theme theme) {
        // TODO Walk existing component tree from display down and install new
        // skins

        Theme.theme = theme;
    }
}
