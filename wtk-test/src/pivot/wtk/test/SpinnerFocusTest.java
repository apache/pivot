package pivot.wtk.test;

import pivot.collections.Dictionary;
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
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        frame = new Frame((Component)wtkxSerializer.readObject(getClass().getResource("spinner_focus_test.wtkx")));
        frame.setTitle("Spinner Focus Test");
        frame.open(display);

        Spinner spinner = (Spinner)wtkxSerializer.getObjectByName("spinner");
        spinner.requestFocus();
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
