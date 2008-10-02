package pivot.wtk.skin.obsidian;

import pivot.wtk.PushButton;
import pivot.wtk.RadioButton;
import pivot.wtk.Theme;

/**
 * Obsidian theme.
 *
 * @author gbrown
 */
public final class ObsidianTheme extends Theme {
    public ObsidianTheme() {
        componentSkinMap.put(PushButton.class, ObsidianPushButtonSkin.class);
        componentSkinMap.put(RadioButton.class, ObsidianRadioButtonSkin.class);
    }

    public void install() {
    }

    public void uninstall() {
    }
}
