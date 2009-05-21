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
import pivot.collections.Sequence;
import pivot.util.Vote;
import pivot.wtk.Accordion;
import pivot.wtk.AccordionSelectionListener;
import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.PushButton;
import pivot.wtk.Window;
import pivot.wtkx.Bindable;

public class Accordions extends Bindable implements Application {
    @Load(resourceName="accordions.wtkx") private Window window;
    @Bind(fieldName="window") private Accordion accordion;
    @Bind(fieldName="window", id="shippingPanel.nextButton") private PushButton shippingNextButton;
    @Bind(fieldName="window", id="paymentPanel.nextButton") private PushButton paymentNextButton;

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

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        bind();

        accordion.getAccordionSelectionListeners().add(accordionSelectionListener);

        ButtonPressListener nextButtonPressListener = new ButtonPressListener() {
            public void buttonPressed(Button button) {
                accordion.setSelectedIndex(accordion.getSelectedIndex() + 1);
            }
        };

        shippingNextButton.getButtonPressListeners().add(nextButtonPressListener);
        paymentNextButton.getButtonPressListeners().add(nextButtonPressListener);

        updateAccordion();

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

    private void updateAccordion() {
        int selectedIndex = accordion.getSelectedIndex();

        Sequence<Component> panels = accordion.getPanels();
        for (int i = 0, n = panels.getLength(); i < n; i++) {
            panels.get(i).setEnabled(i <= selectedIndex);
        }
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Accordions.class, args);
    }
}
