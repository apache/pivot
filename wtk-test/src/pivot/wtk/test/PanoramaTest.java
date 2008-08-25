package pivot.wtk.test;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.Frame;
import pivot.wtk.ImageView;
import pivot.wtk.Panorama;
import pivot.wtk.Window;
import pivot.wtk.effects.ReflectionDecorator;
import pivot.wtkx.WTKXSerializer;

public class PanoramaTest implements Application {
    private Frame frame1 = null;
    private Frame frame2 = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        frame1 = new Frame();
        frame1.setTitle("Panorama Test 1");

        Panorama panorama = new Panorama();
        frame1.setContent(panorama);
        frame1.setPreferredSize(240, 320);

        ImageView imageView = new ImageView();
        imageView.setImage(getClass().getResource("IMG_0767_2.jpg"));
        panorama.setView(imageView);
        frame1.open(display);

        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        frame2 = new Frame((Component)wtkxSerializer.readObject(getClass().getResource("panorama_test.wtkx")));
        frame2.setTitle("Panorama Test 2");
        frame2.setPreferredSize(480, 360);
        frame2.open(display);
    }

    public boolean shutdown(boolean optional) {
        frame1.close();
        frame2.close();
        return false;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
