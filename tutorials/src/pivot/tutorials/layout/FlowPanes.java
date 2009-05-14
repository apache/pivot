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
package pivot.tutorials.layout;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonStateListener;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Orientation;
import pivot.wtk.RadioButton;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.Window;
import pivot.wtkx.Bindable;

public class FlowPanes extends Bindable implements Application {
    @Load(resourceName="flow_panes.wtkx") private Window window;
    @Bind(fieldName="window") private FlowPane flowPane;
    @Bind(fieldName="window") private RadioButton horizontalOrientationButton;
    @Bind(fieldName="window") private RadioButton verticalOrientationButton;
    @Bind(fieldName="window") private RadioButton horizontalAlignmentRightButton;
    @Bind(fieldName="window") private RadioButton horizontalAlignmentLeftButton;
    @Bind(fieldName="window") private RadioButton horizontalAlignmentCenterButton;
    @Bind(fieldName="window") private RadioButton horizontalAlignmentJustifyButton;
    @Bind(fieldName="window") private RadioButton verticalAlignmentTopButton;
    @Bind(fieldName="window") private RadioButton verticalAlignmentBottomButton;
    @Bind(fieldName="window") private RadioButton verticalAlignmentCenterButton;
    @Bind(fieldName="window") private RadioButton verticalAlignmentJustifyButton;

    private ButtonStateListener buttonStateListener = new ButtonStateListener() {
        public void stateChanged(Button button, Button.State previousState) {
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
    };

    public void startup(Display display, Dictionary<String, String> properties) throws Exception {
        bind();

        horizontalOrientationButton.getButtonStateListeners().add(buttonStateListener);
        verticalOrientationButton.getButtonStateListeners().add(buttonStateListener);
        horizontalAlignmentLeftButton.getButtonStateListeners().add(buttonStateListener);
        horizontalAlignmentRightButton.getButtonStateListeners().add(buttonStateListener);
        horizontalAlignmentCenterButton.getButtonStateListeners().add(buttonStateListener);
        horizontalAlignmentJustifyButton.getButtonStateListeners().add(buttonStateListener);
        verticalAlignmentTopButton.getButtonStateListeners().add(buttonStateListener);
        verticalAlignmentBottomButton.getButtonStateListeners().add(buttonStateListener);
        verticalAlignmentCenterButton.getButtonStateListeners().add(buttonStateListener);
        verticalAlignmentJustifyButton.getButtonStateListeners().add(buttonStateListener);

        buttonStateListener.stateChanged(null, null);

        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(FlowPanes.class, args);
    }
}
