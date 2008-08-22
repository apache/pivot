package pivot.wtk.test;

import pivot.collections.Dictionary;
import pivot.wtk.Alert;
import pivot.wtk.Application;
import pivot.wtk.Display;

public class ReflectionDecoratorTest implements Application {
    private Alert alert = null;

    public void startup(Display display, Dictionary<String, String> properties) {
        Alert.alert("Foo", display);
        Alert.alert("Bar", display);
    }

    public boolean shutdown(boolean optional) {
        alert.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
