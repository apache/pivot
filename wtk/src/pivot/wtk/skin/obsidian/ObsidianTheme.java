package pivot.wtk.skin.obsidian;

import java.awt.Font;

import pivot.wtk.PushButton;
import pivot.wtk.RadioButton;
import pivot.wtk.Theme;

/**
 * Obsidian theme.
 *
 * @author gbrown
 */
public final class ObsidianTheme extends Theme {
    private Font font = new Font("Verdana", Font.PLAIN, 11);

    public ObsidianTheme() {
        componentSkinMap.put(PushButton.class, ObsidianPushButtonSkin.class);
        componentSkinMap.put(RadioButton.class, ObsidianRadioButtonSkin.class);
    }

    public void install() {
    }

    public void uninstall() {
    }

    public Font getFont() {
        return font;
    }
}
