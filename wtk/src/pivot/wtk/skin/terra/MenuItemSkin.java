package pivot.wtk.skin.terra;

import java.awt.Graphics2D;

import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Menu;
import pivot.wtk.skin.ButtonSkin;

public class MenuItemSkin extends ButtonSkin {
    @Override
    public void install(Component component) {
        validateComponentType(component, Menu.Item.class);

        super.install(component);

        // TODO
    }

    @Override
    public void uninstall() {
        // TODO

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getPreferredHeight(int width) {
        // TODO Auto-generated method stub
        return 0;
    }

    public Dimensions getPreferredSize() {
        // TODO Auto-generated method stub
        return null;
    }

    public void layout() {
        // No-op
    }

    public void paint(Graphics2D graphics) {
        // TODO Auto-generated method stub

    }
}
