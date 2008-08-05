package pivot.wtkx.test;

import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Window;
import pivot.wtkx.WTKXSerializer;

public class WTKXSerializerTest implements Application {
    private Window window = null;

    public void startup() throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        Component content = (Component)wtkxSerializer.readObject("pivot/wtkx/test/wtkx_serializer.wtkx");

        window = new Window();
        window.setTitle("WTKX Serializer Test");
        window.setContent(content);
        window.setMaximized(true);
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
