package pivot.wtk;

import java.awt.Graphics2D;

public interface Decorator {
    public void install(Component component);
    public void uninstall();

    public Graphics2D prepare(Component component, Graphics2D graphics);
    public void update();
}
