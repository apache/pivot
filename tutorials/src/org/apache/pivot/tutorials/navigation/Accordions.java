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
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Accordion;
import org.apache.pivot.wtk.AccordionSelectionListener;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Window;

public class Accordions extends Window implements Bindable {
    private Accordion accordion = null;
    private PushButton shippingNextButton = null;
    private PushButton paymentNextButton = null;
    private PushButton confirmOrderButton = null;
    private ActivityIndicator activityIndicator = null;
    private Label processingOrderLabel = null;

    private AccordionSelectionListener accordionSelectionListener = new AccordionSelectionListener() {
        private int selectedIndex = -1;

        @Override
        public Vote previewSelectedIndexChange(Accordion accordion, int selectedIndex) {
            this.selectedIndex = selectedIndex;

            // Enable the next panel or disable the previous panel so the
            // transition looks smoother
            if (selectedIndex != -1) {
                int previousSelectedIndex = accordion.getSelectedIndex();
                if (selectedIndex > previousSelectedIndex) {
                    accordion.getPanels().get(selectedIndex).setEnabled(true);
                } else {
                    accordion.getPanels().get(previousSelectedIndex).setEnabled(false);
                }

            }

            return Vote.APPROVE;
        }

        @Override
        public void selectedIndexChangeVetoed(Accordion accordion, Vote reason) {
            if (reason == Vote.DENY
                && selectedIndex != -1) {
                Component panel = accordion.getPanels().get(selectedIndex);
                panel.setEnabled(!panel.isEnabled());
            }
        }

        @Override
        public void selectedIndexChanged(Accordion accordion, int previousSelection) {
            updateAccordion();
        }
    };

    @Override
    public void initialize(Dictionary<String, Object> context, Resources resources) {
        accordion = (Accordion)context.get("accordion");
        shippingNextButton = (PushButton)context.get("shippingPanel.nextButton");
        paymentNextButton = (PushButton)context.get("paymentPanel.nextButton");
        confirmOrderButton = (PushButton)context.get("summaryPanel.confirmOrderButton");
        activityIndicator = (ActivityIndicator)context.get("summaryPanel.activityIndicator");
        processingOrderLabel = (Label)context.get("summaryPanel.processingOrderLabel");

        accordion.getAccordionSelectionListeners().add(accordionSelectionListener);

        ButtonPressListener nextButtonPressListener = new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                accordion.setSelectedIndex(accordion.getSelectedIndex() + 1);
            }
        };

        shippingNextButton.getButtonPressListeners().add(nextButtonPressListener);

        paymentNextButton.getButtonPressListeners().add(nextButtonPressListener);

        confirmOrderButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                // Pretend to submit or cancel the order
                activityIndicator.setActive(!activityIndicator.isActive());
                processingOrderLabel.setVisible(activityIndicator.isActive());
                updateConfirmOrderButton();
            }
        });

        updateAccordion();
        updateConfirmOrderButton();
    }

    private void updateAccordion() {
        int selectedIndex = accordion.getSelectedIndex();

        Sequence<Component> panels = accordion.getPanels();
        for (int i = 0, n = panels.getLength(); i < n; i++) {
            panels.get(i).setEnabled(i <= selectedIndex);
        }
    }

    private void updateConfirmOrderButton() {
        if (activityIndicator.isActive()) {
            confirmOrderButton.setButtonData("Cancel");
        } else {
            confirmOrderButton.setButtonData("Confirm Order");
        }
    }
}
