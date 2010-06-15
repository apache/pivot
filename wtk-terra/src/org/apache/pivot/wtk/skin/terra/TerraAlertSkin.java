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

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.pivot.beans.BeanSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
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
@SuppressWarnings("unchecked")
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
                alert.setSelectedOption(optionIndex);
                alert.close(true);
            }
        }
    };

    private static Map<String, ?> commandButtonStyles;

    static {
        URL location = TerraAlertSkin.class.getResource("command_button.json");

        try {
            InputStream inputStream = location.openStream();

            try {
                JSONSerializer serializer = new JSONSerializer();
                commandButtonStyles = (Map<String, ?>)serializer.readObject(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }
    }

    public TerraAlertSkin() {
        setResizable(false);

        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(9));
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Alert alert = (Alert)component;
        alert.getAlertListeners().add(this);

        // Load the alert content
        BeanSerializer wtkxSerializer = new BeanSerializer();

        Component content;
        try {
            content = (Component)wtkxSerializer.readObject(this, "terra_alert_skin.bxml");
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }

        alert.setContent(content);

        typeImageView = (ImageView)wtkxSerializer.get("typeImageView");
        messageLabel = (Label)wtkxSerializer.get("messageLabel");
        messageBoxPane = (BoxPane)wtkxSerializer.get("messageBoxPane");
        optionButtonBoxPane = (BoxPane)wtkxSerializer.get("optionButtonBoxPane");

        for (Object option : alert.getOptions()) {
            PushButton optionButton = new PushButton(option);
            optionButton.setStyles(commandButtonStyles);
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
        int index = alert.getSelectedOption();

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
        optionButton.setStyles(commandButtonStyles);
        optionButton.getButtonPressListeners().add(optionButtonPressListener);

        optionButtonBoxPane.insert(optionButton, index);
    }

    @Override
    public void optionsRemoved(Alert alert, int index, Sequence<?> removed) {
        optionButtonBoxPane.remove(index, removed.getLength());
    }

    @Override
    public void selectedOptionChanged(Alert alert, int previousSelectedOption) {
        int index = alert.getSelectedOption();

        if (alert.isOpen()
            && index >= 0) {
            optionButtonBoxPane.get(index).requestFocus();
        }
    }
}
