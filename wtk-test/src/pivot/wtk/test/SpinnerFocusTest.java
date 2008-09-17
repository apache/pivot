package pivot.wtk.test;

import pivot.collections.Dictionary;
import pivot.wtk.Action;
import pivot.wtk.Alert;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.Frame;
import pivot.wtk.Spinner;
import pivot.wtkx.WTKXSerializer;

public class SpinnerFocusTest implements Application {
    private Frame frame = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        Action action = new Action("buttonAction") {
            public String getDescription() {
                return null;
            }

            public void perform() {
                Alert.alert("Foo", frame);
            }
        };

        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        frame = new Frame((Component)wtkxSerializer.readObject(getClass().getResource("spinner_focus_test.wtkx")));
        frame.setTitle("Spinner Focus Test");
        frame.open(display);

        Spinner spinner = (Spinner)wtkxSerializer.getObjectByName("spinner");
        spinner.requestFocus();

        action.setEnabled(false);
    }

    public boolean shutdown(boolean optional) {
        frame.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
