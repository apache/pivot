/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk.skin.terra;

import pivot.collections.ArrayList;
import pivot.collections.HashMap;
import pivot.util.Resources;
import pivot.wtk.Alert;
import pivot.wtk.AlertListener;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.FlowPane;
import pivot.wtk.ImageView;
import pivot.wtk.Label;
import pivot.wtk.PushButton;
import pivot.wtk.Theme;
import pivot.wtk.Window;
import pivot.wtkx.WTKXSerializer;

/**
 * Alert skin.
 *
 * @author tvolkert
 * @author gbrown
 */
public class TerraAlertSkin extends TerraDialogSkin
    implements AlertListener {
    private ArrayList<Button> optionButtons = new ArrayList<Button>();

    public TerraAlertSkin() {
        setResizable(false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void install(Component component) {
        super.install(component);

        Alert alert = (Alert)component;
        alert.getAlertListeners().add(this);

        // Load the alert content
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        Resources resources = theme.getResources();

        WTKXSerializer wtkxSerializer = new WTKXSerializer(resources);
        Component content = null;

        try {
            content = (Component)wtkxSerializer.readObject(getClass().getResource("alert_skin.wtkx"));
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }

        alert.setContent(content);

        // Set the type image
        ImageView typeImageView = (ImageView)wtkxSerializer.getObjectByName("typeImageView");
        typeImageView.setImage(theme.getMessageIcon(alert.getMessageType()));

        // Set the message
        Label messageLabel = (Label)wtkxSerializer.getObjectByName("messageLabel");
        String message = alert.getMessage();
        messageLabel.setText(message);

        // Set the body
        FlowPane messageFlowPane = (FlowPane)wtkxSerializer.getObjectByName("messageFlowPane");
        Component body = alert.getBody();
        if (body != null) {
            messageFlowPane.add(body);
        }

        // Add the option buttons
        FlowPane buttonFlowPane = (FlowPane)wtkxSerializer.getObjectByName("buttonFlowPane");

        for (int i = 0, n = alert.getOptionCount(); i < n; i++) {
            Object option = alert.getOption(i);

            PushButton optionButton = new PushButton(option);
            HashMap<String, Object> optionButtonStyles = new HashMap<String, Object>();
            optionButtonStyles.put("color", theme.getColor(1));
            optionButtonStyles.put("backgroundColor", theme.getColor(7));
            optionButtonStyles.put("borderColor", theme.getColor(6));

            optionButton.setStyles(optionButtonStyles);
            optionButton.getStyles().put("preferredAspectRatio", 3);

            optionButton.getButtonPressListeners().add(new ButtonPressListener() {
                public void buttonPressed(Button button) {
                    int optionIndex = optionButtons.indexOf(button);

                    if (optionIndex >= 0) {
                        Alert alert = (Alert)getComponent();
                        alert.setSelectedOption(optionIndex);
                        alert.close(true);
                    }
                }
            });

            buttonFlowPane.add(optionButton);
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
        }
    }

    public void selectedOptionChanged(Alert alert, int previousSelectedOption) {
        int index = alert.getSelectedOption();

        if (alert.isOpen()
            && index >= 0) {
            optionButtons.get(index).requestFocus();
        }
    }
}
