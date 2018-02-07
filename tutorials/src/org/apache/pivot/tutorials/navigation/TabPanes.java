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

import java.net.URL;

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonStateListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.TabPaneListener;
import org.apache.pivot.wtk.Window;

public class TabPanes extends Window implements Bindable {
    private Prompt confirmCloseTabPrompt = null;
    private TabPane tabPane = null;
    private Checkbox closeableCheckbox = null;
    private Checkbox collapsibleCheckbox = null;
    private RadioButton horizontalRadioButton = null;
    private RadioButton verticalRadioButton = null;
    private BoxPane cornerBoxPane = null;

    private boolean confirmCloseTab = true;

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        confirmCloseTabPrompt = (Prompt) namespace.get("confirmCloseTabPrompt");
        tabPane = (TabPane) namespace.get("tabPane");
        closeableCheckbox = (Checkbox) namespace.get("closeableCheckbox");
        collapsibleCheckbox = (Checkbox) namespace.get("collapsibleCheckbox");
        horizontalRadioButton = (RadioButton) namespace.get("horizontalRadioButton");
        verticalRadioButton = (RadioButton) namespace.get("verticalRadioButton");
        cornerBoxPane = (BoxPane) namespace.get("cornerBoxPane");

        tabPane.getTabPaneListeners().add(new TabPaneListener() {
            @Override
            public Vote previewRemoveTabs(final TabPane tabPaneArgument, final int index,
                final int count) {
                Vote vote;
                if (confirmCloseTab) {
                    confirmCloseTabPrompt.open(TabPanes.this, new SheetCloseListener() {
                        @Override
                        public void sheetClosed(Sheet sheet) {
                            if (confirmCloseTabPrompt.getResult()
                                && confirmCloseTabPrompt.getSelectedOptionIndex() == 1) {
                                confirmCloseTab = false;

                                int n = tabPaneArgument.getTabs().getLength();
                                if (index < n - 1) {
                                    tabPaneArgument.setSelectedIndex(index + 1);
                                } else {
                                    tabPaneArgument.setSelectedIndex(index - 1);
                                }

                                tabPaneArgument.getTabs().remove(index, count);

                                confirmCloseTab = true;
                            }
                        }
                    });

                    vote = Vote.DENY;
                } else {
                    vote = Vote.APPROVE;
                }

                return vote;
            }
        });

        ButtonStateListener checkboxStateListener = new ButtonStateListener() {
            @Override
            public void stateChanged(Button button, Button.State previousState) {
                updateTabPane();
            }
        };

        closeableCheckbox.getButtonStateListeners().add(checkboxStateListener);
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
        tabPane.setCloseable(closeableCheckbox.isSelected());
        tabPane.setCollapsible(collapsibleCheckbox.isSelected());

        if (horizontalRadioButton.isSelected()) {
            tabPane.getStyles().put(Style.tabOrientation, Orientation.HORIZONTAL);
            if (tabPane.getCorner() == null) {
                tabPane.setCorner(cornerBoxPane);
            }
        } else {
            tabPane.getStyles().put(Style.tabOrientation, Orientation.VERTICAL);
            if (tabPane.getCorner() == cornerBoxPane) {
                tabPane.setCorner(null);
            }
        }
    }
}
