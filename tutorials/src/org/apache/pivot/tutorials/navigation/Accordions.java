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
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Accordion;
import org.apache.pivot.wtk.AccordionSelectionListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Window;

public class Accordions extends Window implements Bindable {
    private Accordion accordion = null;

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        accordion = (Accordion) namespace.get("accordion");
        accordion.getAccordionSelectionListeners().add(new AccordionSelectionListener() {
            private int selectedIndex = -1;

            @Override
            public Vote previewSelectedIndexChange(Accordion accordionArgument,
                int selectedIndexArgument) {
                this.selectedIndex = selectedIndexArgument;

                // Enable the next panel or disable the previous panel so the
                // transition looks smoother
                if (selectedIndexArgument != -1) {
                    int previousSelectedIndex = accordionArgument.getSelectedIndex();
                    if (selectedIndexArgument > previousSelectedIndex) {
                        accordionArgument.getPanels().get(selectedIndexArgument).setEnabled(true);
                    } else {
                        accordionArgument.getPanels().get(previousSelectedIndex).setEnabled(false);
                    }

                }

                return Vote.APPROVE;
            }

            @Override
            public void selectedIndexChangeVetoed(Accordion accordionArgument, Vote reason) {
                if (reason == Vote.DENY && selectedIndex != -1) {
                    Component panel = accordionArgument.getPanels().get(selectedIndex);
                    panel.setEnabled(!panel.isEnabled());
                }
            }

            @Override
            public void selectedIndexChanged(Accordion accordionArgument, int previousSelection) {
                updateAccordion();
            }
        });

        updateAccordion();
    }

    private void updateAccordion() {
        int selectedIndex = accordion.getSelectedIndex();

        Sequence<Component> panels = accordion.getPanels();
        for (int i = 0, n = panels.getLength(); i < n; i++) {
            panels.get(i).setEnabled(i <= selectedIndex);
        }
    }
}
