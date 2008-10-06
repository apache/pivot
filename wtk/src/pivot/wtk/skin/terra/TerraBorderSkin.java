package pivot.wtk.skin.terra;

import pivot.wtk.Theme;
import pivot.wtk.skin.BorderSkin;

public class TerraBorderSkin extends BorderSkin {
    public TerraBorderSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setColor(theme.getColor(2));
        setBackgroundColor(theme.getColor(1));
    }
}
