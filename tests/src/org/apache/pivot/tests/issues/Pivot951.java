

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Mouse.Button;
import org.apache.pivot.wtk.Window;
 
public class Pivot951 implements Application {
    private Window window = null;

    @Override
    public void startup(Display display, Map<String, String> properties) {
        window = new Window();
 
        Border brd = new Border();
        brd.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter() {
			@Override
			public boolean mouseClick(Component component, Button button, int x, int y, int count) {
				if (count == 1)
					System.out.println("Click!");
				else
					System.out.println("Double Click!");
				return true;
			}
		});

        window.setContent(brd);
        window.setTitle("Pivot951: Cannot click twice");
        window.setMaximized(true);
 
        window.open(display);
    }
 
    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }
        return false;
    }
 
    @Override public void suspend() { }
    @Override public void resume() { }
    
    public static void main(String[] args) {
        DesktopApplicationContext.main(Pivot951.class, args);
    }
}

