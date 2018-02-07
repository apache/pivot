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
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.ButtonStateListener;
import org.apache.pivot.wtk.CardPane;
import org.apache.pivot.wtk.CardPaneListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.skin.CardPaneSkin;

public class CardPanes extends Window implements Bindable {
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
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        cardPane = (CardPane) namespace.get("cardPane");
        previousButton = (LinkButton) namespace.get("previousButton");
        nextButton = (LinkButton) namespace.get("nextButton");
        sizeToSelectionCheckbox = (Checkbox) namespace.get("sizeToSelectionCheckbox");

        crossfadeRadioButton = (RadioButton) namespace.get("crossfadeRadioButton");
        horizontalSlideRadioButton = (RadioButton) namespace.get("horizontalSlideRadioButton");
        verticalSlideRadioButton = (RadioButton) namespace.get("verticalSlideRadioButton");
        horizontalFlipRadioButton = (RadioButton) namespace.get("horizontalFlipRadioButton");
        verticalFlipRadioButton = (RadioButton) namespace.get("verticalFlipRadioButton");
        zoomRadioButton = (RadioButton) namespace.get("zoomRadioButton");
        noneRadioButton = (RadioButton) namespace.get("noneRadioButton");

        cardPane.getCardPaneListeners().add(new CardPaneListener() {
            @Override
            public void selectedIndexChanged(CardPane cardPaneArgument, int previousSelectedIndex) {
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
    }

    private void updateCardPane() {
        cardPane.getStyles().put(Style.sizeToSelection, sizeToSelectionCheckbox.isSelected());

        if (crossfadeRadioButton.isSelected()) {
            cardPane.getStyles().put(Style.selectionChangeEffect,
                CardPaneSkin.SelectionChangeEffect.CROSSFADE);
        } else if (horizontalSlideRadioButton.isSelected()) {
            cardPane.getStyles().put(Style.selectionChangeEffect,
                CardPaneSkin.SelectionChangeEffect.HORIZONTAL_SLIDE);
        } else if (verticalSlideRadioButton.isSelected()) {
            cardPane.getStyles().put(Style.selectionChangeEffect,
                CardPaneSkin.SelectionChangeEffect.VERTICAL_SLIDE);
        } else if (horizontalFlipRadioButton.isSelected()) {
            cardPane.getStyles().put(Style.selectionChangeEffect,
                CardPaneSkin.SelectionChangeEffect.HORIZONTAL_FLIP);
        } else if (verticalFlipRadioButton.isSelected()) {
            cardPane.getStyles().put(Style.selectionChangeEffect,
                CardPaneSkin.SelectionChangeEffect.VERTICAL_FLIP);
        } else if (zoomRadioButton.isSelected()) {
            cardPane.getStyles().put(Style.selectionChangeEffect,
                CardPaneSkin.SelectionChangeEffect.ZOOM);
        } else {
            cardPane.getStyles().put(Style.selectionChangeEffect, null);
        }
    }

    private void updateLinkButtonState() {
        int selectedIndex = cardPane.getSelectedIndex();
        previousButton.setEnabled(selectedIndex > 0);
        nextButton.setEnabled(selectedIndex < cardPane.getLength() - 1);
    }
}
