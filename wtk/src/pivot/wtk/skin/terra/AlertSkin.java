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

import java.awt.Color;

import pivot.collections.ArrayList;
import pivot.wtk.Alert;
import pivot.wtk.AlertListener;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.FlowPane;
import pivot.wtk.ImageView;
import pivot.wtk.Label;
import pivot.wtk.PushButton;
import pivot.wtk.Window;
import pivot.wtk.media.Image;
import pivot.wtkx.WTKXSerializer;

/**
 * Alert skin.
 *
 * @author tvolkert
 * @author gbrown
 */
public class AlertSkin extends DialogSkin
    implements AlertListener {
    private static final Color OPTION_BUTTON_COLOR = Color.WHITE;
    private static final Color OPTION_BUTTON_BACKGROUND_COLOR = new Color(0x3c, 0x77, 0xb2);
    private static final Color OPTION_BUTTON_BORDER_COLOR = new Color(0x2c, 0x56, 0x80);
    private static final Color OPTION_BUTTON_BEVEL_COLOR = new Color(0x45, 0x89, 0xcc);
    private static final Color OPTION_BUTTON_PRESSED_BEVEL_COLOR = new Color(0x34, 0x66, 0x99);

    private ArrayList<Button> optionButtons = new ArrayList<Button>();

    private static Image informationImage = null;
    private static Image warningImage = null;
    private static Image errorImage = null;
    private static Image questionImage = null;

    public AlertSkin() {
        setResizable(false);
    }

    @Override
    public void install(Component component) {
        validateComponentType(component, Alert.class);

        super.install(component);

        Alert alert = (Alert)component;
        alert.getAlertListeners().add(this);

        // Load the alert content
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        Component content = null;

        try {
            content = (Component)wtkxSerializer.readObject(getClass().getResource("alert.wtkx"));
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }

        alert.setContent(content);

        // Set the type image
        ImageView typeImageView = (ImageView)wtkxSerializer.getObjectByName("typeImageView");
        Image typeImage = null;

        switch (alert.getType()) {
            case INFO: {
                if (informationImage == null) {
                    informationImage =
                        Image.load(getClass().getResource("AlertSkin-Information-32x32.png"));
                }

                typeImage = informationImage;
                break;
            }

            case WARNING: {
                if (warningImage == null) {
                    warningImage =
                        Image.load(getClass().getResource("AlertSkin-Warning-32x32.png"));
                }

                typeImage = warningImage;
                break;
            }

            case ERROR: {
                if (errorImage == null) {
                    errorImage =
                        Image.load(getClass().getResource("AlertSkin-Error-32x32.png"));
                }

                typeImage = errorImage;
                break;
            }

            case QUESTION: {
                if (questionImage == null) {
                    questionImage =
                        Image.load(getClass().getResource("AlertSkin-Question-32x32.png"));
                }

                typeImage = questionImage;
                break;
            }
        }

        typeImageView.setImage(typeImage);

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
            optionButton.getStyles().put("color", OPTION_BUTTON_COLOR);
            optionButton.getStyles().put("backgroundColor", OPTION_BUTTON_BACKGROUND_COLOR);
            optionButton.getStyles().put("borderColor", OPTION_BUTTON_BORDER_COLOR);
            optionButton.getStyles().put("bevelColor", OPTION_BUTTON_BEVEL_COLOR);
            optionButton.getStyles().put("pressedBevelColor", OPTION_BUTTON_PRESSED_BEVEL_COLOR);

            int preferredHeight = optionButton.getPreferredHeight(-1);
            int minWidth = 3 * preferredHeight;
            if (optionButton.getPreferredWidth(-1) < minWidth) {
                optionButton.setPreferredWidth(minWidth);
            }

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
            Component.setFocusedComponent(optionButtons.get(index));
        }
    }

    public void selectedOptionChanged(Alert alert, int previousSelectedOption) {
        int index = alert.getSelectedOption();

        if (alert.isOpen()
            && index >= 0) {
            Component.setFocusedComponent(optionButtons.get(index));
        }
    }
}
