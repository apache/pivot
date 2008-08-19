package pivot.wtk.test;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.wtk.Alert;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.effects.ReflectionDecorator;

public class ReflectionDecoratorTest implements Application {
    private Alert alert = null;

    public void startup(Display display, Dictionary<String, String> properties) {
        ArrayList<String> options = new ArrayList<String>();
        options.add("OK");
        options.add("Cancel");

        alert = new Alert(Alert.Type.INFO, "Reflection Demo", options, null);

        // Replace the drop shadow decorator with a reflection decorator
        Component.DecoratorSequence decorators = alert.getDecorators();
        alert.getDecorators().update(decorators.getLength() - 1,
            new ReflectionDecorator());
        alert.getStyles().put("resizable", true);
        alert.open(display);
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
