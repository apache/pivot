package pivot.wtk.skin.obsidian;

import pivot.wtk.PushButton;
import pivot.wtk.RadioButton;
import pivot.wtk.Theme;

/**
 * <p>Obsidian theme.</p>
 *
 * @author gbrown
 */
public final class ObsidianTheme extends Theme {
    public ObsidianTheme() {
        componentSkinMap.put(PushButton.class, PushButtonSkin.class);
        componentSkinMap.put(RadioButton.class, pivot.wtk.skin.terra.RadioButtonSkin.class);
    }

    public void install() {
    }

    public void uninstall() {
    }
}
