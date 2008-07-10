package pivot.wtk.skin.terra;

import pivot.wtk.Component;
import pivot.wtk.PushButton;
import pivot.wtk.Skin;
import pivot.wtk.Theme;

public class TerraTheme extends Theme {
    @Override
    public Class<? extends Skin> getSkinClass(Class<? extends Component> componentClass) {
        Class<? extends Skin> skinClass = null;

        // TODO Add additional mappings

        if (componentClass == PushButton.class) {
            skinClass = PushButtonSkin.class;
        } else {
            throw new IllegalArgumentException("Unrecognized component class: "
                + componentClass.getName());
        }

        return skinClass;
    }
}
