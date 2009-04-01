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
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Orientation;
import pivot.wtk.RadioButton;
import pivot.wtk.Theme;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.Window;
import pivot.wtkx.WTKXSerializer;

public class FlowPanes implements Application, ButtonStateListener {
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

    private Window window = null;

    public void startup(Display display, Dictionary<String, String> properties) throws Exception {
        String themeClassName = properties.get("themeClassName");

        if (themeClassName != null) {
            Class<?> themeClass = Class.forName(themeClassName);
            Theme.setTheme((Theme)themeClass.newInstance());
        }

        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        Component content =
            (Component)wtkxSerializer.readObject("pivot/tutorials/layout/flowpanes.wtkx");

        flowPane = (FlowPane)wtkxSerializer.getObjectByName("flowPane");

        // Orientation
        horizontalOrientationButton =
            (RadioButton)wtkxSerializer.getObjectByName("horizontalOrientationButton");
        horizontalOrientationButton.getButtonStateListeners().add(this);

        verticalOrientationButton =
            (RadioButton)wtkxSerializer.getObjectByName("verticalOrientationButton");
        verticalOrientationButton.getButtonStateListeners().add(this);

        // Horizontal alignment
        horizontalAlignmentLeftButton =
            (RadioButton)wtkxSerializer.getObjectByName("horizontalAlignmentLeftButton");
        horizontalAlignmentLeftButton.getButtonStateListeners().add(this);

        horizontalAlignmentRightButton =
            (RadioButton)wtkxSerializer.getObjectByName("horizontalAlignmentRightButton");
        horizontalAlignmentRightButton.getButtonStateListeners().add(this);

        horizontalAlignmentCenterButton =
            (RadioButton)wtkxSerializer.getObjectByName("horizontalAlignmentCenterButton");
        horizontalAlignmentCenterButton.getButtonStateListeners().add(this);

        horizontalAlignmentJustifyButton =
            (RadioButton)wtkxSerializer.getObjectByName("horizontalAlignmentJustifyButton");
        horizontalAlignmentJustifyButton.getButtonStateListeners().add(this);

        // Vertical alignment
        verticalAlignmentTopButton =
            (RadioButton)wtkxSerializer.getObjectByName("verticalAlignmentTopButton");
        verticalAlignmentTopButton.getButtonStateListeners().add(this);

        verticalAlignmentBottomButton =
            (RadioButton)wtkxSerializer.getObjectByName("verticalAlignmentBottomButton");
        verticalAlignmentBottomButton.getButtonStateListeners().add(this);

        verticalAlignmentCenterButton =
            (RadioButton)wtkxSerializer.getObjectByName("verticalAlignmentCenterButton");
        verticalAlignmentCenterButton.getButtonStateListeners().add(this);

        verticalAlignmentJustifyButton =
            (RadioButton)wtkxSerializer.getObjectByName("verticalAlignmentJustifyButton");
        verticalAlignmentJustifyButton.getButtonStateListeners().add(this);

        stateChanged(null, null);

        window = new Window();
        window.setContent(content);
        window.setMaximized(true);
        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        window.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }

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
}
