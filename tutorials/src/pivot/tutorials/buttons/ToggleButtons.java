package pivot.tutorials.buttons;

import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.Window;
import pivot.wtkx.ComponentLoader;

public class ToggleButtons implements Application {
    private Window window = null;

    public void startup() throws Exception {
        ComponentLoader.initialize();
        ComponentLoader componentLoader = new ComponentLoader();

        Component content =
            componentLoader.load("pivot/tutorials/buttons/toggle_buttons.wtkx");

        window = new Window();
        window.setContent(content);
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
