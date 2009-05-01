package pivot.wtk.media.drawing.test;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.ImageView;
import pivot.wtk.Window;
import pivot.wtk.media.Image;
import pivot.wtk.media.drawing.Shape;
import pivot.wtkx.WTKXSerializer;

public class RotationTest implements Application {
    private Window window = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception{
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        Image image = (Image)wtkxSerializer.readObject(getClass().getResource("rotate.wtkd"));

        final Shape.Rotate rotation = (Shape.Rotate)wtkxSerializer.getObjectByName("rotation");
        ApplicationContext.scheduleRecurringCallback(new Runnable() {
            public void run() {
                int angle = (int)rotation.getAngle();
                angle = (angle + 6) % 360;
                rotation.setAngle(angle);
            }
        }, 1000);

        window = new Window(new ImageView(image));
        window.setMaximized(true);
        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(RotationTest.class, args);
    }
}
