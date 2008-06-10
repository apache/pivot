package pivot.tutorials.buttons;

import pivot.wtk.Alert;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.PushButton;
import pivot.wtk.RadioButton;
import pivot.wtk.Window;
import pivot.wtkx.ComponentLoader;

public class RadioButtons implements Application {
    private Window window = null;

    public void startup() throws Exception {
        ComponentLoader.initialize();
        ComponentLoader componentLoader = new ComponentLoader();

        Component content =
            componentLoader.load("pivot/tutorials/buttons/radio_buttons.wtkx");

        // Get a reference to the button group
        RadioButton oneButton =
            (RadioButton)componentLoader.getComponent("oneButton");
        final Button.Group numbersGroup = oneButton.getGroup();

        // Add a button press listener
        PushButton selectButton =
            (PushButton)componentLoader.getComponent("selectButton");

        selectButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                String message = "You selected \""
                    + numbersGroup.getSelection().getButtonData()
                    + "\".";
                Alert.alert(Alert.Type.INFO, message, window);
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
