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
package org.apache.pivot.tutorials.navigation;

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonStateListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.Window;

public class TabPanes extends Window implements Bindable {
    private TabPane tabPane = null;
    private Checkbox collapsibleCheckbox = null;
    private RadioButton horizontalRadioButton = null;
    private RadioButton verticalRadioButton = null;
    private BoxPane cornerBoxPane = null;

    @Override
    public void initialize(Dictionary<String, Object> context, Resources resources) {
        tabPane = (TabPane)context.get("tabPane");
        collapsibleCheckbox = (Checkbox)context.get("collapsibleCheckbox");
        horizontalRadioButton = (RadioButton)context.get("horizontalRadioButton");
        verticalRadioButton = (RadioButton)context.get("verticalRadioButton");
        cornerBoxPane = (BoxPane)context.get("cornerBoxPane");

        ButtonStateListener checkboxStateListener = new ButtonStateListener() {
            @Override
            public void stateChanged(Button button, Button.State previousState) {
                updateTabPane();
            }
        };

        collapsibleCheckbox.getButtonStateListeners().add(checkboxStateListener);

        ButtonStateListener radioButtonStateListener = new ButtonStateListener() {
            @Override
            public void stateChanged(Button button, Button.State previousState) {
                if (button.isSelected()) {
                    updateTabPane();
                }
            }
        };

        horizontalRadioButton.getButtonStateListeners().add(radioButtonStateListener);
        verticalRadioButton.getButtonStateListeners().add(radioButtonStateListener);

        updateTabPane();
    }

    private void updateTabPane() {
        tabPane.getStyles().put("collapsible", collapsibleCheckbox.isSelected());

        if (horizontalRadioButton.isSelected()) {
            tabPane.getStyles().put("tabOrientation", Orientation.HORIZONTAL);
            if (tabPane.getCorner() == null) {
                tabPane.setCorner(cornerBoxPane);
            }
        } else {
            tabPane.getStyles().put("tabOrientation", Orientation.VERTICAL);
            if (tabPane.getCorner() == cornerBoxPane) {
                tabPane.setCorner(null);
            }
        }
    }
}
