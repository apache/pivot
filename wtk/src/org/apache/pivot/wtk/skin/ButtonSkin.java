/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.wtk.skin;

import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonListener;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.ButtonStateListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Cursor;

/**
 * Abstract base class for button skins.
 *
 * @author gbrown
 */
public abstract class ButtonSkin extends ComponentSkin
    implements ButtonListener, ButtonStateListener, ButtonPressListener {
    protected boolean highlighted = false;

    @Override
    public void install(Component component) {
        super.install(component);

        Button button = (Button)component;
        button.getButtonListeners().add(this);
        button.getButtonStateListeners().add(this);
        button.getButtonPressListeners().add(this);

        button.setCursor(Cursor.HAND);
    }

    @Override
    public void uninstall() {
        Button button = (Button)getComponent();
        button.getButtonListeners().remove(this);
        button.getButtonStateListeners().remove(this);
        button.getButtonPressListeners().remove(this);

        button.setCursor(Cursor.DEFAULT);

        super.uninstall();
    }

    public void layout() {
        // No-op
    }

    // Component state events
    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        highlighted = false;
        repaintComponent();
    }

    @Override
    public void focusedChanged(Component component, Component obverseComponent) {
        super.focusedChanged(component, obverseComponent);

        repaintComponent(!component.isFocused());
    }

    // Component mouse events
    @Override
    public void mouseOver(Component component) {
        super.mouseOver(component);

        highlighted = true;
        repaintComponent();
    }

    @Override
    public void mouseOut(Component component) {
        super.mouseOut(component);

        highlighted = false;
        repaintComponent();
    }

    // Button events
    public void buttonDataChanged(Button button, Object previousButtonData) {
        invalidateComponent();
    }

    public void dataRendererChanged(Button button, Button.DataRenderer previousDataRenderer) {
        invalidateComponent();
    }

    public void actionChanged(Button button, Action previousAction) {
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

    // Button state events
    public void stateChanged(Button button, Button.State previousState) {
        repaintComponent();
    }

    // Button press events
    public void buttonPressed(Button button) {
        // No-op
    }
}
