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
import pivot.wtk.Button.Group;
import pivot.wtkx.Bindable;

public class TabPanes extends Bindable implements Application {
    @Load(resourceName="tab_panes.wtkx") private Window window;
    @Bind(fieldName="window") private TabPane tabPane;
    @Bind(fieldName="window") private Checkbox collapsibleCheckbox;
    @Bind(fieldName="window") private RadioButton horizontalRadioButton;
    @Bind(fieldName="window") private FlowPane cornerFlowPane;

    private Button.Group tabOrientationGroup = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        bind();

        collapsibleCheckbox.getButtonStateListeners().add(new ButtonStateListener() {
            public void stateChanged(Button button, Button.State previousState) {
                updateTabPane();
            }
        });

        tabOrientationGroup = Button.getGroup("tabOrientation");
        tabOrientationGroup.getGroupListeners().add(new Button.GroupListener() {
            public void selectionChanged(Group group, Button previousSelection) {
                updateTabPane();
            }
        });

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

        if (tabOrientationGroup.getSelection() == horizontalRadioButton) {
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
