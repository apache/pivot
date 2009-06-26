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

import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Accordion;
import org.apache.pivot.wtk.AccordionSelectionListener;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKXSerializer;

public class Accordions implements Application {
    private Window window = null;
    private Accordion accordion = null;
    private PushButton shippingNextButton = null;
    private PushButton paymentNextButton = null;
    private PushButton confirmOrderButton = null;
    private ActivityIndicator activityIndicator = null;
    private Label processingOrderLabel = null;

    private AccordionSelectionListener accordionSelectionListener = new AccordionSelectionListener() {
        private int selectedIndex = -1;

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

        public void selectedIndexChangeVetoed(Accordion accordion, Vote reason) {
            if (reason == Vote.DENY
                && selectedIndex != -1) {
                Component panel = accordion.getPanels().get(selectedIndex);
                panel.setEnabled(!panel.isEnabled());
            }
        }

        public void selectedIndexChanged(Accordion accordion, int previousSelection) {
            updateAccordion();
        }
    };

    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "accordions.wtkx");
        accordion = (Accordion)wtkxSerializer.get("accordion");

        accordion.getAccordionSelectionListeners().add(accordionSelectionListener);

        ButtonPressListener nextButtonPressListener = new ButtonPressListener() {
            public void buttonPressed(Button button) {
                accordion.setSelectedIndex(accordion.getSelectedIndex() + 1);
            }
        };

        shippingNextButton = (PushButton)wtkxSerializer.get("shippingPanel.nextButton");
        shippingNextButton.getButtonPressListeners().add(nextButtonPressListener);

        paymentNextButton = (PushButton)wtkxSerializer.get("paymentPanel.nextButton");
        paymentNextButton.getButtonPressListeners().add(nextButtonPressListener);

        confirmOrderButton = (PushButton)wtkxSerializer.get("summaryPanel.confirmOrderButton");
        confirmOrderButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                // Pretend to submit or cancel the order
                activityIndicator.setActive(!activityIndicator.isActive());
                processingOrderLabel.setDisplayable(activityIndicator.isActive());
                updateConfirmOrderButton();
            }
        });

        activityIndicator = (ActivityIndicator)wtkxSerializer.get("summaryPanel.activityIndicator");
        processingOrderLabel = (Label)wtkxSerializer.get("summaryPanel.processingOrderLabel");

        updateAccordion();
        updateConfirmOrderButton();

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

    public static void main(String[] args) {
        DesktopApplicationContext.main(Accordions.class, args);
    }
}
