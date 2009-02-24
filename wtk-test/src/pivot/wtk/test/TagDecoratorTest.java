package pivot.wtk.test;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Display;
import pivot.wtk.Frame;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.effects.TagDecorator;
import pivot.wtk.media.Image;

public class TagDecoratorTest implements Application {
    private Frame frame = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        frame = new Frame();
        frame.setTitle("Tag Decorator Test");
        frame.setPreferredSize(480, 360);

        Image tag = Image.load(getClass().getResource("go-home.png"));

        // frame.getDecorators().removeAll();
        frame.getDecorators().add(new TagDecorator(tag,
            HorizontalAlignment.RIGHT, VerticalAlignment.TOP,
            10, -10));

        frame.open(display);
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
