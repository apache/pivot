package pivot.wtk.skin.terra;

import pivot.wtk.Theme;
import pivot.wtk.skin.ScrollPaneSkin;

public class TerraScrollPaneSkin extends ScrollPaneSkin {
    public TerraScrollPaneSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(6));
    }
}
