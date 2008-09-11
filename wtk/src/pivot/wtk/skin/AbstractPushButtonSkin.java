package pivot.wtk.skin;

import pivot.wtk.Button;
import pivot.wtk.ButtonStateListener;
import pivot.wtk.Component;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.PushButton;

public abstract class AbstractPushButtonSkin extends ButtonSkin
    implements ButtonStateListener {
    protected boolean highlighted = false;
    protected boolean pressed = false;

    @Override
    public void install(Component component) {
        validateComponentType(component, PushButton.class);

        super.install(component);

        PushButton pushButton = (PushButton)component;
        pushButton.getButtonStateListeners().add(this);
    }

    @Override
    public void uninstall() {
        PushButton pushButton = (PushButton)getComponent();
        pushButton.getButtonStateListeners().remove(this);

        super.uninstall();
    }

    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        highlighted = false;
        pressed = false;
        repaintComponent();
    }

    @Override
    public void focusedChanged(Component component, boolean temporary) {
        super.focusedChanged(component, temporary);

        pressed = false;
        repaintComponent();
    }

    @Override
    public void mouseOver() {
        super.mouseOver();

        highlighted = true;
        repaintComponent();
    }

    @Override
    public void mouseOut() {
        super.mouseOut();

        highlighted = false;
        pressed = false;
        repaintComponent();
    }

    @Override
    public boolean mouseDown(Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(button, x, y);

        pressed = true;
        repaintComponent();

        return consumed;
    }

    @Override
    public boolean mouseUp(Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseUp(button, x, y);

        pressed = false;
        repaintComponent();

        return consumed;
    }

    @Override
    public void mouseClick(Mouse.Button button, int x, int y, int count) {
        PushButton pushButton = (PushButton)getComponent();

        if (pushButton.isFocusable()) {
            Component.setFocusedComponent(pushButton);
        }

        pushButton.press();
    }

    @Override
    public boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (keyCode == Keyboard.KeyCode.SPACE) {
            pressed = true;
            repaintComponent();
        } else {
            consumed = super.keyPressed(keyCode, keyLocation);
        }

        return consumed;
    }

    @Override
    public boolean keyReleased(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        PushButton pushButton = (PushButton)getComponent();

        if (keyCode == Keyboard.KeyCode.SPACE) {
            pressed = false;
            repaintComponent();

            pushButton.press();
        } else {
            consumed = super.keyReleased(keyCode, keyLocation);
        }

        return consumed;
    }

    // ButtonStateListener methods

    public boolean previewStateChange(Button button, Button.State state) {
        return true;
    }

    public void stateChanged(Button toggleButton, Button.State previousState) {
        repaintComponent();
    }
}
