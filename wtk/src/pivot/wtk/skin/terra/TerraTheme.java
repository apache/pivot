package pivot.wtk.skin.terra;

import pivot.wtk.PushButton;
import pivot.wtk.Theme;

public class TerraTheme extends Theme {
    public TerraTheme() {
        // TODO Add additional mappings

        componentSkinMap.put(PushButton.class, PushButtonSkin.class);
    }
}
