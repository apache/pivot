package pivot.wtk.skin.obsidian;

import pivot.wtk.Component;
import pivot.wtk.PushButton;
import pivot.wtk.Skin;

/**
 * TODO Eliminate dependency on TerraTheme. This will be a stand-alone theme.
 */
public class ObsidianTheme extends pivot.wtk.skin.terra.TerraTheme {
    @Override
    public Class<? extends Skin> getSkinClass(Class<? extends Component> componentClass) {
        Class<? extends Skin> skinClass = null;

        // TODO Add additional mappings

        if (componentClass == PushButton.class) {
            skinClass = PushButtonSkin.class;
        } else {
            skinClass = super.getSkinClass(componentClass);
        }

        return skinClass;
    }
}
