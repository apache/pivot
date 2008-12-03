package pivot.wtk.test;

import java.awt.Font;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Display;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Label;
import pivot.wtk.Mouse;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.Window;

public class NativeDragDropTest implements Application {
    private Window window = null;

    public void startup(final Display display, Dictionary<String, String> properties)
        throws Exception {
        final Label label = new Label("Drag Text Here");
        label.getStyles().put("font", new Font("Arial", Font.PLAIN, 24));
        label.getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
        label.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

        window = new Window(label);
        window.setMaximized(true);
        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
