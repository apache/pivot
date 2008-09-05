package pivot.wtk.test;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.Frame;
import pivot.wtkx.WTKXSerializer;

public class FlowPaneTest implements Application {
    private Frame frame = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        frame = new Frame((Component)wtkxSerializer.readObject(getClass().getResource("flow_pane_test.wtkx")));
        frame.setTitle("Flow Pane Test");
        frame.setPreferredSize(480, 360);
        frame.open(display);
    }

    public boolean shutdown(boolean optional) {
        frame.close();
        return true;
    }

    public void resume() {
    }


    public void suspend() {
    }
}
