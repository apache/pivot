package pivot.tutorials;

import java.awt.Color;
import java.awt.Font;

import pivot.wtk.Application;
import pivot.wtk.Display;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Label;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.Window;

public class HelloJava implements Application {
    private Window window = null;

    public void startup() throws Exception {
        Label label = new Label();
        label.setText("Hello World!");
        label.getStyles().put("font", new Font("Arial", Font.BOLD, 24));
        label.getStyles().put("color", Color.RED);
        label.getStyles().put("horizontalAlignment",
            HorizontalAlignment.CENTER);
        label.getStyles().put("verticalAlignment",
            VerticalAlignment.CENTER);

        window = new Window();
        window.setContent(label);
        window.getAttributes().put(Display.MAXIMIZED_ATTRIBUTE,
            Boolean.TRUE);
        window.open();
    }

    public void shutdown() throws Exception {
        window.close();
    }

    public void suspend() throws Exception {
    }

    public void resume() throws Exception {
    }
}
