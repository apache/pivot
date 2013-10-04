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
import org.apache.pivot.wtk.FillPane;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.Window;

public class FillPanes extends Window implements Bindable {
    private FillPane fillPane = null;
    private RadioButton horizontalOrientationButton = null;
    private RadioButton verticalOrientationButton = null;

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        fillPane = (FillPane) namespace.get("fillPane");
        horizontalOrientationButton = (RadioButton) namespace.get("horizontalOrientationButton");
        verticalOrientationButton = (RadioButton) namespace.get("verticalOrientationButton");

        ButtonStateListener buttonStateListener = new ButtonStateListener() {
            @Override
            public void stateChanged(Button button, Button.State previousState) {
                updateFillPaneState();
            }
        };

        horizontalOrientationButton.getButtonStateListeners().add(buttonStateListener);
        verticalOrientationButton.getButtonStateListeners().add(buttonStateListener);

        updateFillPaneState();
    }

    private void updateFillPaneState() {
        Orientation orientation = null;
        if (horizontalOrientationButton.isSelected()) {
            orientation = Orientation.HORIZONTAL;
        } else if (verticalOrientationButton.isSelected()) {
            orientation = Orientation.VERTICAL;
        }

        if (orientation != null) {
            fillPane.setOrientation(orientation);
        }
    }
}
