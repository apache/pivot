package pivot.wtk.skin.terra;

import pivot.wtk.Theme;
import pivot.wtk.skin.FlowPaneSkin;

public class TerraFlowPaneSkin extends FlowPaneSkin {
    public final void setBackgroundColor(int backgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(backgroundColor));
    }
}
