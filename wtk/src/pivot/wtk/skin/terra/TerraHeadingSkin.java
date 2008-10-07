package pivot.wtk.skin.terra;

import java.awt.Font;

import pivot.wtk.Component;
import pivot.wtk.Heading;
import pivot.wtk.HeadingListener;
import pivot.wtk.Theme;
import pivot.wtk.skin.LabelSkin;

public class TerraHeadingSkin extends LabelSkin implements HeadingListener {
    @Override
    public void install(Component component) {
        super.install(component);

        Heading heading = (Heading)component;
        heading.getHeadingListeners().add(this);

        levelChanged(heading, -1);
    }

    @Override
    public void uninstall() {
        Heading heading = (Heading)getComponent();
        heading.getHeadingListeners().remove(this);

        super.uninstall();
    }

    public void levelChanged(Heading heading, int previousLevel) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();

        switch(heading.getLevel()) {
            case 1: {
                setFont(theme.getLargeFont().deriveFont(Font.BOLD));
                setColor(theme.getColor(7));
                break;
            }

            case 2: {
                setFont(theme.getFont().deriveFont(Font.BOLD));
                setColor(theme.getColor(0));
                break;
            }

            case 3: {
                setFont(theme.getSmallFont().deriveFont(Font.ITALIC));
                setColor(theme.getColor(0));
                break;
            }
        }
    }
}
