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
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.ButtonStateListener;
import org.apache.pivot.wtk.CardPane;
import org.apache.pivot.wtk.CardPaneListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.skin.CardPaneSkin;
import org.apache.pivot.wtkx.WTKXSerializer;

public class CardPanes implements Application {
    private Window window = null;
    private CardPane cardPane = null;
    private LinkButton previousButton = null;
    private LinkButton nextButton = null;
    private Checkbox sizeToSelectionCheckbox = null;
    private RadioButton crossfadeRadioButton = null;
    private RadioButton horizontalSlideRadioButton = null;
    private RadioButton verticalSlideRadioButton = null;
    private RadioButton horizontalFlipRadioButton = null;
    private RadioButton verticalFlipRadioButton = null;
    private RadioButton zoomRadioButton = null;
    private RadioButton noneRadioButton = null;

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "card_panes.wtkx");
        cardPane = (CardPane)wtkxSerializer.get("cardPane");
        previousButton = (LinkButton)wtkxSerializer.get("previousButton");
        nextButton = (LinkButton)wtkxSerializer.get("nextButton");
        sizeToSelectionCheckbox = (Checkbox)wtkxSerializer.get("sizeToSelectionCheckbox");


        crossfadeRadioButton = (RadioButton)wtkxSerializer.get("crossfadeRadioButton");
        horizontalSlideRadioButton = (RadioButton)wtkxSerializer.get("horizontalSlideRadioButton");
        verticalSlideRadioButton = (RadioButton)wtkxSerializer.get("verticalSlideRadioButton");
        horizontalFlipRadioButton = (RadioButton)wtkxSerializer.get("horizontalFlipRadioButton");
        verticalFlipRadioButton = (RadioButton)wtkxSerializer.get("verticalFlipRadioButton");
        zoomRadioButton = (RadioButton)wtkxSerializer.get("zoomRadioButton");
        noneRadioButton = (RadioButton)wtkxSerializer.get("noneRadioButton");

        cardPane.getCardPaneListeners().add(new CardPaneListener.Adapter() {
            @Override
            public void selectedIndexChanged(CardPane cardPane, int previousSelectedIndex) {
                updateLinkButtonState();
            }
        });

        previousButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                cardPane.setSelectedIndex(cardPane.getSelectedIndex() - 1);
            }
        });

        nextButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                cardPane.setSelectedIndex(cardPane.getSelectedIndex() + 1);
            }
        });

        ButtonStateListener checkboxStateListener = new ButtonStateListener() {
            @Override
            public void stateChanged(Button button, Button.State previousState) {
                updateCardPane();
            }
        };

        sizeToSelectionCheckbox.getButtonStateListeners().add(checkboxStateListener);

        ButtonStateListener radioButtonStateListener = new ButtonStateListener() {
            @Override
            public void stateChanged(Button button, Button.State previousState) {
                if (button.isSelected()) {
                    updateCardPane();
                }
            }
        };

        crossfadeRadioButton.getButtonStateListeners().add(radioButtonStateListener);
        horizontalSlideRadioButton.getButtonStateListeners().add(radioButtonStateListener);
        verticalSlideRadioButton.getButtonStateListeners().add(radioButtonStateListener);
        horizontalFlipRadioButton.getButtonStateListeners().add(radioButtonStateListener);
        verticalFlipRadioButton.getButtonStateListeners().add(radioButtonStateListener);
        zoomRadioButton.getButtonStateListeners().add(radioButtonStateListener);
        noneRadioButton.getButtonStateListeners().add(radioButtonStateListener);

        updateCardPane();
        updateLinkButtonState();

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
        } else if (horizontalFlipRadioButton.isSelected()) {
            cardPane.getStyles().put("selectionChangeEffect",
                CardPaneSkin.SelectionChangeEffect.HORIZONTAL_FLIP);
        } else if (verticalFlipRadioButton.isSelected()) {
            cardPane.getStyles().put("selectionChangeEffect",
                CardPaneSkin.SelectionChangeEffect.VERTICAL_FLIP);
        } else if (zoomRadioButton.isSelected()) {
            cardPane.getStyles().put("selectionChangeEffect",
                CardPaneSkin.SelectionChangeEffect.ZOOM);
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
