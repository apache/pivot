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
package org.apache.pivot.wtk.skin.terra;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.AlertListener;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;

/**
 * Alert skin.
 */
public class TerraAlertSkin extends TerraDialogSkin
    implements AlertListener {
    private ImageView typeImageView = null;
    private Label messageLabel = null;
    private BoxPane messageBoxPane = null;
    private BoxPane optionButtonBoxPane = null;

    private ButtonPressListener optionButtonPressListener = new ButtonPressListener() {
        @Override
        public void buttonPressed(Button button) {
            int optionIndex = optionButtonBoxPane.indexOf(button);

            if (optionIndex >= 0) {
                Alert alert = (Alert)getComponent();
                alert.setSelectedOptionIndex(optionIndex);
                alert.close(true);
            }
        }
    };

    public TerraAlertSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(9));
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Alert alert = (Alert)component;
        alert.setPreferredWidth(320);
        alert.setMinimumWidth(160);

        alert.getAlertListeners().add(this);

        // Load the alert content
        BXMLSerializer bxmlSerializer = new BXMLSerializer();

        Component content;
        try {
            content = (Component)bxmlSerializer.readObject(TerraAlertSkin.class,
                "terra_alert_skin.bxml");
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }

        alert.setContent(content);

        typeImageView = (ImageView)bxmlSerializer.getNamespace().get("typeImageView");
        messageLabel = (Label)bxmlSerializer.getNamespace().get("messageLabel");
        messageBoxPane = (BoxPane)bxmlSerializer.getNamespace().get("messageBoxPane");
        optionButtonBoxPane = (BoxPane)bxmlSerializer.getNamespace().get("optionButtonBoxPane");

        for (Object option : alert.getOptions()) {
            PushButton optionButton = new PushButton(option);
            optionButton.setStyleName(TerraAlertSkin.class.getPackage().getName()
                + "." + TerraTheme.COMMAND_BUTTON_STYLE);
            optionButton.getButtonPressListeners().add(optionButtonPressListener);

            optionButtonBoxPane.add(optionButton);
        }

        messageTypeChanged(alert, null);
        messageChanged(alert, null);
        bodyChanged(alert, null);
    }

    @Override
    public void windowOpened(Window window) {
        super.windowOpened(window);

        Alert alert = (Alert)window;
        int index = alert.getSelectedOptionIndex();

        if (index >= 0) {
            optionButtonBoxPane.get(index).requestFocus();
        } else {
            window.requestFocus();
        }
    }

    @Override
    public void messageTypeChanged(Alert alert, MessageType previousMessageType) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        typeImageView.setImage(theme.getMessageIcon(alert.getMessageType()));
    }

    @Override
    public void messageChanged(Alert alert, String previousMessage) {
        messageLabel.setText(alert.getMessage());
    }

    @Override
    public void bodyChanged(Alert alert, Component previousBody) {
        if (previousBody != null) {
            messageBoxPane.remove(previousBody);
        }

        Component body = alert.getBody();
        if (body != null) {
            messageBoxPane.add(body);
        }
    }

    @Override
    public void optionInserted(Alert alert, int index) {
        Object option = alert.getOptions().get(index);

        PushButton optionButton = new PushButton(option);
        optionButton.setStyleName(TerraAlertSkin.class.getPackage().getName()
            + "." + TerraTheme.COMMAND_BUTTON_STYLE);
        optionButton.getButtonPressListeners().add(optionButtonPressListener);

        optionButtonBoxPane.insert(optionButton, index);
    }

    @Override
    public void optionsRemoved(Alert alert, int index, Sequence<?> removed) {
        optionButtonBoxPane.remove(index, removed.getLength());
    }

    @Override
    public void selectedOptionChanged(Alert alert, int previousSelectedOption) {
        int index = alert.getSelectedOptionIndex();

        if (alert.isOpen()
            && index >= 0) {
            optionButtonBoxPane.get(index).requestFocus();
        }
    }
}
