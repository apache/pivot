package pivot.wtk.skin.obsidian;

import pivot.wtk.PushButton;

/**
 * TODO Eliminate dependency on TerraTheme. This will be a stand-alone theme.
 */
public class ObsidianTheme extends pivot.wtk.skin.terra.TerraTheme {
    public ObsidianTheme() {
        // TODO Add additional mappings

        componentSkinMap.put(PushButton.class, PushButtonSkin.class);
    }
}
