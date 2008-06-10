package pivot.tutorials.buttons;

import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.CardPane;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.LinkButton;
import pivot.wtk.Window;
import pivot.wtkx.ComponentLoader;

public class LinkButtons implements Application {
    private Window window = null;

    public void startup() throws Exception {
        ComponentLoader.initialize();
        ComponentLoader componentLoader = new ComponentLoader();

        Component content =
            componentLoader.load("pivot/tutorials/buttons/link_buttons.wtkx");

        final CardPane cardPane = (CardPane)componentLoader.getComponent("cardPane");

        LinkButton nextButton = (LinkButton)componentLoader.getComponent("nextButton");
        nextButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                cardPane.setSelectedIndex(1);
            }
        });

        LinkButton previousButton = (LinkButton)componentLoader.getComponent("previousButton");
        previousButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                cardPane.setSelectedIndex(0);
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
