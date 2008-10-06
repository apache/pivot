package pivot.wtk.skin.terra;

import pivot.wtk.Theme;
import pivot.wtk.skin.SeparatorSkin;

public class TerraSeparatorSkin extends SeparatorSkin {
    public TerraSeparatorSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setColor(theme.getColor(2));
    }
}
