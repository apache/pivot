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
package org.apache.pivot.tutorials.layout;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonStateListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FlowPane;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKXSerializer;

public class FlowPanes implements Application {
    private Window window = null;
    private FlowPane flowPane = null;
    private RadioButton leftRadioButton = null;
    private RadioButton rightRadioButton = null;
    private RadioButton centerRadioButton = null;
    private Checkbox alignToBaselineCheckbox = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "flow_panes.wtkx");
        flowPane = (FlowPane)wtkxSerializer.get("flowPane");
        leftRadioButton = (RadioButton)wtkxSerializer.get("leftRadioButton");
        rightRadioButton = (RadioButton)wtkxSerializer.get("rightRadioButton");
        centerRadioButton = (RadioButton)wtkxSerializer.get("centerRadioButton");
        alignToBaselineCheckbox = (Checkbox)wtkxSerializer.get("alignToBaselineCheckbox");

        ButtonStateListener buttonStateListener = new ButtonStateListener() {
            @Override
            public void stateChanged(Button button, Button.State previousState) {
                updateFlowPaneState();
            }
        };

        leftRadioButton.getButtonStateListeners().add(buttonStateListener);
        rightRadioButton.getButtonStateListeners().add(buttonStateListener);
        centerRadioButton.getButtonStateListeners().add(buttonStateListener);
        alignToBaselineCheckbox.getButtonStateListeners().add(buttonStateListener);

        updateFlowPaneState();

        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    private void updateFlowPaneState() {
        HorizontalAlignment alignment = null;

        if (leftRadioButton.isSelected()) {
            alignment = HorizontalAlignment.LEFT;
        } else if (rightRadioButton.isSelected()) {
            alignment = HorizontalAlignment.RIGHT;
        } else if (centerRadioButton.isSelected()) {
            alignment = HorizontalAlignment.CENTER;
        }

        if (alignment != null) {
            flowPane.getStyles().put("alignment", alignment);
        }

        flowPane.getStyles().put("alignToBaseline", alignToBaselineCheckbox.isSelected());
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(FlowPanes.class, args);
    }
}
