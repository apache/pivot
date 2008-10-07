package pivot.wtk.skin.terra;

import pivot.wtk.Theme;
import pivot.wtk.skin.BorderSkin;

public class TerraBorderSkin extends BorderSkin {
    public TerraBorderSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(1));
        setColor(theme.getColor(2));
        setTitleColor(theme.getColor(7));
    }
}
