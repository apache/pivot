package pivot.wtk;

import pivot.wtk.skin.terra.SpacerSkin;

public class Spacer extends Component {
    public Spacer() {
        if (getClass() == Spacer.class) {
            setSkinClass(SpacerSkin.class);
        }
    }
}
