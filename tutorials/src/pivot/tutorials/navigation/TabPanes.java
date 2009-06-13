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
package pivot.tutorials.navigation;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonStateListener;
import pivot.wtk.Checkbox;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.FlowPane;
import pivot.wtk.Orientation;
import pivot.wtk.RadioButton;
import pivot.wtk.TabPane;
import pivot.wtk.Window;
import pivot.wtkx.WTKX;
import pivot.wtkx.WTKXSerializer;

public class TabPanes implements Application {
    private Window window = null;

    @WTKX private TabPane tabPane;
    @WTKX private Checkbox collapsibleCheckbox;
    @WTKX private RadioButton horizontalRadioButton;
    @WTKX private RadioButton verticalRadioButton;
    @WTKX private FlowPane cornerFlowPane;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "tab_panes.wtkx");
        wtkxSerializer.bind(this, TabPanes.class);

        ButtonStateListener checkboxStateListener = new ButtonStateListener() {
            public void stateChanged(Button button, Button.State previousState) {
                updateTabPane();
            }
        };

        collapsibleCheckbox.getButtonStateListeners().add(checkboxStateListener);

        ButtonStateListener radioButtonStateListener = new ButtonStateListener() {
            public void stateChanged(Button button, Button.State previousState) {
                if (button.isSelected()) {
                    updateTabPane();
                }
            }
        };

        horizontalRadioButton.getButtonStateListeners().add(radioButtonStateListener);
        verticalRadioButton.getButtonStateListeners().add(radioButtonStateListener);

        updateTabPane();

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

    private void updateTabPane() {
        tabPane.getStyles().put("collapsible", collapsibleCheckbox.isSelected());

        if (horizontalRadioButton.isSelected()) {
            tabPane.getStyles().put("tabOrientation", Orientation.HORIZONTAL);
            if (tabPane.getCorner() == null) {
                tabPane.setCorner(cornerFlowPane);
            }
        } else {
            tabPane.getStyles().put("tabOrientation", Orientation.VERTICAL);
            if (tabPane.getCorner() == cornerFlowPane) {
                tabPane.setCorner(null);
            }
        }
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(TabPanes.class, args);
    }
}
