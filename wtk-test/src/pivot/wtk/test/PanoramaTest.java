package pivot.wtk.test;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Display;
import pivot.wtk.Frame;
import pivot.wtk.ImageView;
import pivot.wtk.Panorama;

public class PanoramaTest implements Application {
    private Frame frame = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        frame = new Frame();
        frame.setTitle("Panorama Test");

        Panorama panorama = new Panorama();
        frame.setContent(panorama);
        frame.setPreferredSize(240, 320);

        ImageView imageView = new ImageView();
        imageView.setImage(getClass().getResource("IMG_1147.jpg"));

        panorama.setView(imageView);

        frame.open(display);
    }

    public boolean shutdown(boolean optional) {
        frame.close();
        return false;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
