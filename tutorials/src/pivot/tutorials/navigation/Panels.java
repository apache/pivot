package pivot.tutorials.navigation;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.Window;
import pivot.wtkx.Bindable;

public class Panels extends Bindable implements Application {
    @Load(resourceName="panels.wtkx") private Window window;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        bind();
        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Panels.class, args);
    }
}
