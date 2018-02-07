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

import java.net.URL;

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonStateListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.FlowPane;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.Window;

public class FlowPanes extends Window implements Bindable {
    private FlowPane flowPane = null;
    private RadioButton leftRadioButton = null;
    private RadioButton rightRadioButton = null;
    private RadioButton centerRadioButton = null;
    private Checkbox alignToBaselineCheckbox = null;

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        flowPane = (FlowPane) namespace.get("flowPane");
        leftRadioButton = (RadioButton) namespace.get("leftRadioButton");
        rightRadioButton = (RadioButton) namespace.get("rightRadioButton");
        centerRadioButton = (RadioButton) namespace.get("centerRadioButton");
        alignToBaselineCheckbox = (Checkbox) namespace.get("alignToBaselineCheckbox");

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
            flowPane.getStyles().put(Style.alignment, alignment);
        }

        flowPane.getStyles().put(Style.alignToBaseline, alignToBaselineCheckbox.isSelected());
    }
}
