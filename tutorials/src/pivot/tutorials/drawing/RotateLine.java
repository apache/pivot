package pivot.tutorials.drawing;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.ImageView;
import pivot.wtk.Window;
import pivot.wtk.media.Drawing;
import pivot.wtk.media.drawing.Shape;
import pivot.wtkx.Bind;
import pivot.wtkx.Load;
import pivot.wtkx.WTKXSerializer;

public class RotateLine implements Application {
    @Load(name="rotate_line.wtkd")
    private Drawing drawing = null;

    @Bind(resource="drawing", id="rotation")
    private Shape.Rotate rotation = null;

    private Window window = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception{
        WTKXSerializer.bind(this);

        ApplicationContext.scheduleRecurringCallback(new Runnable() {
            public void run() {
                int angle = (int)rotation.getAngle();
                angle = (angle + 6) % 360;
                rotation.setAngle(angle);
            }
        }, 1000);

        window = new Window(new ImageView(drawing));
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
        DesktopApplicationContext.main(RotateLine.class, args);
    }
}
