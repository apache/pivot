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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.AlertListener;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKXSerializer;


/**
 * Alert skin.
 */
public class TerraAlertSkin extends TerraDialogSkin
    implements AlertListener {
    private ArrayList<Button> optionButtons = new ArrayList<Button>();

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
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        Component content = null;

        try {
            content = (Component)wtkxSerializer.readObject(this, "terra_alert_skin.wtkx");
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }

        alert.setContent(content);

        // Set the type image
        TerraTheme theme = (TerraTheme)Theme.getTheme();

        ImageView typeImageView = (ImageView)wtkxSerializer.get("typeImageView");
        typeImageView.setImage(theme.getMessageIcon(alert.getMessageType()));

        // Set the message
        Label messageLabel = (Label)wtkxSerializer.get("messageLabel");
        String message = alert.getMessage();
        messageLabel.setText(message);

        // Set the body
        BoxPane messageBoxPane = (BoxPane)wtkxSerializer.get("messageBoxPane");
        Component body = alert.getBody();
        if (body != null) {
            messageBoxPane.add(body);
        }

        // Add the option buttons
        BoxPane buttonBoxPane = (BoxPane)wtkxSerializer.get("buttonBoxPane");

        for (int i = 0, n = alert.getOptionCount(); i < n; i++) {
            Object option = alert.getOption(i);

            PushButton optionButton = new PushButton(option);
            HashMap<String, Object> optionButtonStyles = new HashMap<String, Object>();
            optionButtonStyles.put("color", theme.getColor(4));
            optionButtonStyles.put("backgroundColor", theme.getColor(16));
            optionButtonStyles.put("borderColor", theme.getColor(13));
            optionButtonStyles.put("padding", new Insets(3, 4, 3, 4));

            optionButton.setStyles(optionButtonStyles);
            optionButton.getStyles().put("minimumAspectRatio", 3);

            optionButton.getButtonPressListeners().add(new ButtonPressListener() {
                @Override
                public void buttonPressed(Button button) {
                    int optionIndex = optionButtons.indexOf(button);

                    if (optionIndex >= 0) {
                        Alert alert = (Alert)getComponent();
                        alert.setSelectedOption(optionIndex);
                        alert.close(true);
                    }
                }
            });

            buttonBoxPane.add(optionButton);
            optionButtons.add(optionButton);
        }
    }

    @Override
    public void uninstall() {
        Alert alert = (Alert)getComponent();
        alert.getAlertListeners().remove(this);

        alert.setContent(null);

        super.uninstall();
    }

    @Override
    public void windowOpened(Window window) {
        super.windowOpened(window);

        Alert alert = (Alert)window;
        int index = alert.getSelectedOption();

        if (index >= 0) {
            optionButtons.get(index).requestFocus();
        } else {
            window.requestFocus();
        }
    }

    @Override
    public void selectedOptionChanged(Alert alert, int previousSelectedOption) {
        int index = alert.getSelectedOption();

        if (alert.isOpen()
            && index >= 0) {
            optionButtons.get(index).requestFocus();
        }
    }
}
