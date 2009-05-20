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
import pivot.wtk.ButtonPressListener;
import pivot.wtk.ButtonStateListener;
import pivot.wtk.CardPane;
import pivot.wtk.CardPaneListener;
import pivot.wtk.Checkbox;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.LinkButton;
import pivot.wtk.RadioButton;
import pivot.wtk.Window;
import pivot.wtk.skin.CardPaneSkin;
import pivot.wtkx.Bindable;

public class CardPanes extends Bindable implements Application {
    @Load(resourceName="card_panes.wtkx") private Window window;
    @Bind(fieldName="window") private CardPane cardPane;
    @Bind(fieldName="window") private LinkButton previousButton;
    @Bind(fieldName="window") private LinkButton nextButton;
    @Bind(fieldName="window") private Checkbox sizeToSelectionCheckbox;
    @Bind(fieldName="window") private RadioButton crossfadeRadioButton;
    @Bind(fieldName="window") private RadioButton horizontalSlideRadioButton;
    @Bind(fieldName="window") private RadioButton verticalSlideRadioButton;
    @Bind(fieldName="window") private RadioButton noneRadioButton;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        bind();

        cardPane.getCardPaneListeners().add(new CardPaneListener.Adapter() {
            public void selectedIndexChanged(CardPane cardPane, int previousSelectedIndex) {
                updateLinkButtonState();
            }
        });

        previousButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                cardPane.setSelectedIndex(cardPane.getSelectedIndex() - 1);
            }
        });

        nextButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                cardPane.setSelectedIndex(cardPane.getSelectedIndex() + 1);
            }
        });

        ButtonStateListener checkboxStateListener = new ButtonStateListener() {
            public void stateChanged(Button button, Button.State previousState) {
                updateCardPane();
            }
        };

        sizeToSelectionCheckbox.getButtonStateListeners().add(checkboxStateListener);

        ButtonStateListener radioButtonStateListener = new ButtonStateListener() {
            public void stateChanged(Button button, Button.State previousState) {
                if (button.isSelected()) {
                    updateCardPane();
                }
            }
        };

        crossfadeRadioButton.getButtonStateListeners().add(radioButtonStateListener);
        horizontalSlideRadioButton.getButtonStateListeners().add(radioButtonStateListener);
        verticalSlideRadioButton.getButtonStateListeners().add(radioButtonStateListener);
        noneRadioButton.getButtonStateListeners().add(radioButtonStateListener);

        updateCardPane();
        updateLinkButtonState();

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

    private void updateCardPane() {
        cardPane.getStyles().put("sizeToSelection", sizeToSelectionCheckbox.isSelected());

        if (crossfadeRadioButton.isSelected()) {
            cardPane.getStyles().put("selectionChangeEffect",
                CardPaneSkin.SelectionChangeEffect.CROSSFADE);
        } else if (horizontalSlideRadioButton.isSelected()) {
            cardPane.getStyles().put("selectionChangeEffect",
                CardPaneSkin.SelectionChangeEffect.HORIZONTAL_SLIDE);
        } else if (verticalSlideRadioButton.isSelected()) {
            cardPane.getStyles().put("selectionChangeEffect",
                CardPaneSkin.SelectionChangeEffect.VERTICAL_SLIDE);
        } else {
            cardPane.getStyles().put("selectionChangeEffect", null);
        }
    }

    private void updateLinkButtonState() {
        int selectedIndex = cardPane.getSelectedIndex();
        previousButton.setEnabled(selectedIndex > 0);
        nextButton.setEnabled(selectedIndex < cardPane.getLength() - 1);
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(CardPanes.class, args);
    }
}
