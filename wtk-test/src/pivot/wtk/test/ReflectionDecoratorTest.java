package pivot.wtk.test;

import pivot.collections.ArrayList;
import pivot.wtk.Alert;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.effects.ReflectionDecorator;

public class ReflectionDecoratorTest implements Application {
    private Alert alert = null;

    public void startup() throws Exception {
        alert = new Alert(Alert.Type.INFO, "Reflection Demo");

        ArrayList<String> options = new ArrayList<String>();
        options.add("OK");
        options.add("Cancel");
        alert.setOptionData(options);

        // Replace the drop shadow decorator with a reflection decorator
        Component.DecoratorSequence decorators = alert.getDecorators();
        alert.getDecorators().update(decorators.getLength() - 1,
            new ReflectionDecorator());
        alert.getStyles().put("resizable", true);
        alert.open();
    }

    public void shutdown() throws Exception {
        alert.close();
    }

    public void suspend() throws Exception {
    }

    public void resume() throws Exception {
    }
}
