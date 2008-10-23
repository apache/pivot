package pivot.wtk.skin.obsidian;

import java.awt.Font;

import pivot.wtk.MessageType;
import pivot.wtk.PushButton;
import pivot.wtk.RadioButton;
import pivot.wtk.Theme;
import pivot.wtk.media.Image;

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

    public Image getMessageIcon(MessageType messageType) {
    	return null;
    }

    public Image getSmallMessageIcon(MessageType messageType) {
    	return null;
    }
}
