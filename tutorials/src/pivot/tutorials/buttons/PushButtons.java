package pivot.tutorials.buttons;

import pivot.wtk.Alert;
import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.PushButton;
import pivot.wtk.Window;
import pivot.wtkx.ComponentLoader;

public class PushButtons implements Application {
    private Window window = null;

    public void startup() throws Exception {
        ComponentLoader.initialize();
        ComponentLoader componentLoader = new ComponentLoader();

        Component content =
            componentLoader.load("pivot/tutorials/buttons/push_buttons.wtkx");

        // Get a reference to the button and add a button press listener
        PushButton pushButton =
            (PushButton)componentLoader.getComponent("pushButton");
        pushButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Alert.alert(Alert.Type.INFO, "You clicked me!", window);
            }
        });

        window = new Window();
        window.setContent(content);
        window.getAttributes().put(Display.MAXIMIZED_ATTRIBUTE,
            Boolean.TRUE);
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
