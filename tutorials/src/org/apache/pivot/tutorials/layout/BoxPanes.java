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

import org.apache.pivot.beans.BeanSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonStateListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.Window;

public class BoxPanes implements Application {
    private Window window = null;
    private BoxPane boxPane = null;
    private RadioButton horizontalOrientationButton = null;
    private RadioButton verticalOrientationButton = null;
    private RadioButton horizontalAlignmentRightButton = null;
    private RadioButton horizontalAlignmentLeftButton = null;
    private RadioButton horizontalAlignmentCenterButton = null;
    private RadioButton verticalAlignmentTopButton = null;
    private RadioButton verticalAlignmentBottomButton = null;
    private RadioButton verticalAlignmentCenterButton = null;
    private Checkbox fillCheckbox = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BeanSerializer beanSerializer = new BeanSerializer();
        window = (Window)beanSerializer.readObject(this, "box_panes.bxml");
        boxPane = (BoxPane)beanSerializer.get("boxPane");
        horizontalOrientationButton = (RadioButton)beanSerializer.get("horizontalOrientationButton");
        verticalOrientationButton = (RadioButton)beanSerializer.get("verticalOrientationButton");
        horizontalAlignmentRightButton = (RadioButton)beanSerializer.get("horizontalAlignmentRightButton");
        horizontalAlignmentLeftButton = (RadioButton)beanSerializer.get("horizontalAlignmentLeftButton");
        horizontalAlignmentCenterButton = (RadioButton)beanSerializer.get("horizontalAlignmentCenterButton");
        verticalAlignmentTopButton = (RadioButton)beanSerializer.get("verticalAlignmentTopButton");
        verticalAlignmentBottomButton = (RadioButton)beanSerializer.get("verticalAlignmentBottomButton");
        verticalAlignmentCenterButton = (RadioButton)beanSerializer.get("verticalAlignmentCenterButton");
        fillCheckbox = (Checkbox)beanSerializer.get("fillCheckbox");

        ButtonStateListener buttonStateListener = new ButtonStateListener() {
            @Override
            public void stateChanged(Button button, Button.State previousState) {
                updateBoxPaneState();
            }
        };

        horizontalOrientationButton.getButtonStateListeners().add(buttonStateListener);
        verticalOrientationButton.getButtonStateListeners().add(buttonStateListener);
        horizontalAlignmentLeftButton.getButtonStateListeners().add(buttonStateListener);
        horizontalAlignmentRightButton.getButtonStateListeners().add(buttonStateListener);
        horizontalAlignmentCenterButton.getButtonStateListeners().add(buttonStateListener);
        verticalAlignmentTopButton.getButtonStateListeners().add(buttonStateListener);
        verticalAlignmentBottomButton.getButtonStateListeners().add(buttonStateListener);
        verticalAlignmentCenterButton.getButtonStateListeners().add(buttonStateListener);
        fillCheckbox.getButtonStateListeners().add(buttonStateListener);

        updateBoxPaneState();

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

    private void updateBoxPaneState() {
        Orientation orientation = null;
        if (horizontalOrientationButton.isSelected()) {
            orientation = Orientation.HORIZONTAL;
        } else if (verticalOrientationButton.isSelected()) {
            orientation = Orientation.VERTICAL;
        }

        if (orientation != null) {
            boxPane.setOrientation(orientation);
        }

        HorizontalAlignment horizontalAlignment = null;
        if (horizontalAlignmentLeftButton.isSelected()) {
            horizontalAlignment = HorizontalAlignment.LEFT;
        } else if (horizontalAlignmentRightButton.isSelected()) {
            horizontalAlignment = HorizontalAlignment.RIGHT;
        } else if (horizontalAlignmentCenterButton.isSelected()) {
            horizontalAlignment = HorizontalAlignment.CENTER;
        }

        if (horizontalAlignment != null) {
            boxPane.getStyles().put("horizontalAlignment", horizontalAlignment);
        }

        VerticalAlignment verticalAlignment = null;
        if (verticalAlignmentTopButton.isSelected()) {
            verticalAlignment = VerticalAlignment.TOP;
        } else if (verticalAlignmentBottomButton.isSelected()) {
            verticalAlignment = VerticalAlignment.BOTTOM;
        } else if (verticalAlignmentCenterButton.isSelected()) {
            verticalAlignment = VerticalAlignment.CENTER;
        }

        if (verticalAlignment != null) {
            boxPane.getStyles().put("verticalAlignment", verticalAlignment);
        }

        boxPane.getStyles().put("fill", fillCheckbox.isSelected());
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(BoxPanes.class, args);
    }
}
