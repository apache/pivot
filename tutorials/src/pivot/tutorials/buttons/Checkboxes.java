package pivot.tutorials.buttons;

import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Checkbox;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.ImageView;
import pivot.wtk.Window;
import pivot.wtkx.ComponentLoader;

public class Checkboxes implements Application {
    private class ButtonPressHandler implements ButtonPressListener {
        public void buttonPressed(Button button) {
            ImageView imageView = (ImageView)button.getUserData();
            imageView.setDisplayable(!imageView.isDisplayable());
        }
    }

    private Window window = null;
    private ButtonPressHandler buttonPressHandler = new ButtonPressHandler();

    public void startup() throws Exception {
        ComponentLoader.initialize();
        ComponentLoader componentLoader = new ComponentLoader();

        Component content =
            componentLoader.load("pivot/tutorials/buttons/checkboxes.wtkx");

        // Wire up user data and event listeners
        Checkbox bellCheckbox =
            (Checkbox)componentLoader.getComponent("bellCheckbox");
        ImageView bellImageView =
            (ImageView)componentLoader.getComponent("bellImageView");
        bellCheckbox.setUserData(bellImageView);
        bellCheckbox.getButtonPressListeners().add(buttonPressHandler);

        Checkbox clockCheckbox =
            (Checkbox)componentLoader.getComponent("clockCheckbox");
        ImageView clockImageView =
            (ImageView)componentLoader.getComponent("clockImageView");
        clockCheckbox.setUserData(clockImageView);
        clockCheckbox.getButtonPressListeners().add(buttonPressHandler);

        Checkbox houseCheckbox =
            (Checkbox)componentLoader.getComponent("houseCheckbox");
        ImageView houseImageView =
            (ImageView)componentLoader.getComponent("houseImageView");
        houseCheckbox.setUserData(houseImageView);
        houseCheckbox.getButtonPressListeners().add(buttonPressHandler);

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
