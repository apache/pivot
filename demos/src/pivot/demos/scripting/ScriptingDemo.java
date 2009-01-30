package pivot.demos.scripting;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.Window;
import pivot.wtkx.WTKXSerializer;

public class ScriptingDemo implements Application {
    private Window window = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = new Window((Component)wtkxSerializer.readObject(getClass().getResource("scripting_demo.wtkx")));

        String foo = (String)wtkxSerializer.getObjectByName("foo");
        System.out.println("foo = " + foo);

        window.setTitle("Scripting Demo");
        window.setMaximized(true);
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
}
