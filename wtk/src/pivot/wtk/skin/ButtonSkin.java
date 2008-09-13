/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk.skin;

import pivot.wtk.Button;
import pivot.wtk.ButtonListener;
import pivot.wtk.Component;
import pivot.wtk.Cursor;
import pivot.wtk.Keyboard;

/**
 * <p>Abstract base class for button skins.</p>
 *
 * @author gbrown
 */
public abstract class ButtonSkin extends ComponentSkin implements ButtonListener {
    public ButtonSkin() {
    }

    @Override
    public void install(Component component) {
        validateComponentType(component, Button.class);

        super.install(component);

        Button button = (Button)component;
        button.getButtonListeners().add(this);

        button.setCursor(Cursor.HAND);
    }

    @Override
    public void uninstall() {
        Button button = (Button)getComponent();
        button.getButtonListeners().remove(this);

        button.setCursor(Cursor.DEFAULT);

        super.uninstall();
    }

    public void layout() {
        // No-op
    }

    public void buttonDataChanged(Button button, Object previousButtonData) {
        invalidateComponent();
    }

    public void dataRendererChanged(Button button, Button.DataRenderer previousDataRenderer) {
        invalidateComponent();
    }

    public void actionTriggerChanged(Button button, Keyboard.KeyStroke previousActionTrigger) {
        // No-op
    }

    public void toggleButtonChanged(Button button) {
        // No-op
    }

    public void triStateChanged(Button button) {
        // No-op
    }

    public void groupChanged(Button button, Button.Group previousGroup) {
        // No-op
    }

    public void selectedKeyChanged(Button button, String previousSelectedKey) {
        // No-op
    }

    public void stateKeyChanged(Button button, String previousStateKey) {
        // No-op
    }
}
