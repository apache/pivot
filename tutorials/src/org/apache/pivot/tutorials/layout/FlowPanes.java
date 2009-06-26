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
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FlowPane;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKXSerializer;

public class FlowPanes implements Application {
    private Window window = null;
    private FlowPane flowPane = null;
    private RadioButton horizontalOrientationButton = null;
    private RadioButton verticalOrientationButton = null;
    private RadioButton horizontalAlignmentRightButton = null;
    private RadioButton horizontalAlignmentLeftButton = null;
    private RadioButton horizontalAlignmentCenterButton = null;
    private RadioButton horizontalAlignmentJustifyButton = null;
    private RadioButton verticalAlignmentTopButton = null;
    private RadioButton verticalAlignmentBottomButton = null;
    private RadioButton verticalAlignmentCenterButton = null;
    private RadioButton verticalAlignmentJustifyButton = null;

    public void startup(Display display, Map<String, String> properties) throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "flow_panes.wtkx");
        flowPane = (FlowPane)wtkxSerializer.get("flowPane");
        horizontalOrientationButton = (RadioButton)wtkxSerializer.get("horizontalOrientationButton");
        verticalOrientationButton = (RadioButton)wtkxSerializer.get("verticalOrientationButton");
        horizontalAlignmentRightButton = (RadioButton)wtkxSerializer.get("horizontalAlignmentRightButton");
        horizontalAlignmentLeftButton = (RadioButton)wtkxSerializer.get("horizontalAlignmentLeftButton");
        horizontalAlignmentCenterButton = (RadioButton)wtkxSerializer.get("horizontalAlignmentCenterButton");
        horizontalAlignmentJustifyButton = (RadioButton)wtkxSerializer.get("horizontalAlignmentJustifyButton");
        verticalAlignmentTopButton = (RadioButton)wtkxSerializer.get("verticalAlignmentTopButton");
        verticalAlignmentBottomButton = (RadioButton)wtkxSerializer.get("verticalAlignmentBottomButton");
        verticalAlignmentCenterButton = (RadioButton)wtkxSerializer.get("verticalAlignmentCenterButton");
        verticalAlignmentJustifyButton = (RadioButton)wtkxSerializer.get("verticalAlignmentJustifyButton");

        ButtonStateListener radioButtonStateListener = new ButtonStateListener() {
            public void stateChanged(Button button, Button.State previousState) {
                if (button.isSelected()) {
                    updateFlowPaneState();
                }
            }
        };

        horizontalOrientationButton.getButtonStateListeners().add(radioButtonStateListener);
        verticalOrientationButton.getButtonStateListeners().add(radioButtonStateListener);
        horizontalAlignmentLeftButton.getButtonStateListeners().add(radioButtonStateListener);
        horizontalAlignmentRightButton.getButtonStateListeners().add(radioButtonStateListener);
        horizontalAlignmentCenterButton.getButtonStateListeners().add(radioButtonStateListener);
        horizontalAlignmentJustifyButton.getButtonStateListeners().add(radioButtonStateListener);
        verticalAlignmentTopButton.getButtonStateListeners().add(radioButtonStateListener);
        verticalAlignmentBottomButton.getButtonStateListeners().add(radioButtonStateListener);
        verticalAlignmentCenterButton.getButtonStateListeners().add(radioButtonStateListener);
        verticalAlignmentJustifyButton.getButtonStateListeners().add(radioButtonStateListener);

        updateFlowPaneState();

        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public void suspend() {
    }

    public void resume() {
    }

    private void updateFlowPaneState() {
        Orientation orientation = null;
        if (horizontalOrientationButton.isSelected()) {
            orientation = Orientation.HORIZONTAL;
        } else if (verticalOrientationButton.isSelected()) {
            orientation = Orientation.VERTICAL;
        }

        if (orientation != null) {
            flowPane.setOrientation(orientation);
        }

        HorizontalAlignment horizontalAlignment = null;
        if (horizontalAlignmentLeftButton.isSelected()) {
            horizontalAlignment = HorizontalAlignment.LEFT;
        } else if (horizontalAlignmentRightButton.isSelected()) {
            horizontalAlignment = HorizontalAlignment.RIGHT;
        } else if (horizontalAlignmentCenterButton.isSelected()) {
            horizontalAlignment = HorizontalAlignment.CENTER;
        } else if (horizontalAlignmentJustifyButton.isSelected()) {
            horizontalAlignment = HorizontalAlignment.JUSTIFY;
        }

        if (horizontalAlignment != null) {
            flowPane.getStyles().put("horizontalAlignment", horizontalAlignment);
        }

        VerticalAlignment verticalAlignment = null;
        if (verticalAlignmentTopButton.isSelected()) {
            verticalAlignment = VerticalAlignment.TOP;
        } else if (verticalAlignmentBottomButton.isSelected()) {
            verticalAlignment = VerticalAlignment.BOTTOM;
        } else if (verticalAlignmentCenterButton.isSelected()) {
            verticalAlignment = VerticalAlignment.CENTER;
        } else if (verticalAlignmentJustifyButton.isSelected()) {
            verticalAlignment = VerticalAlignment.JUSTIFY;
        }

        if (verticalAlignment != null) {
            flowPane.getStyles().put("verticalAlignment", verticalAlignment);
        }
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(FlowPanes.class, args);
    }
}
