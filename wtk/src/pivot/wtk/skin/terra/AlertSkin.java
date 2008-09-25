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
import pivot.collections.Map;
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
import pivot.wtk.Window;
import pivot.wtk.media.Image;
import pivot.wtkx.WTKXSerializer;

/**
 * <p>Alert skin.</p>
 *
 * @author tvolkert
 * @author gbrown
 */
public class AlertSkin extends DialogSkin
    implements AlertListener {
    private ArrayList<Button> optionButtons = new ArrayList<Button>();

    private static Image informationImage = null;
    private static Image warningImage = null;
    private static Image errorImage = null;
    private static Image questionImage = null;

    private static Resources resources = null;

    static {
        try {
            resources = new Resources(AlertSkin.class.getName());
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public AlertSkin() {
        setResizable(false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void install(Component component) {
        validateComponentType(component, Alert.class);

        super.install(component);

        Alert alert = (Alert)component;
        alert.getAlertListeners().add(this);

        // Load the alert content
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        Component content = null;

        try {
            content = (Component)wtkxSerializer.readObject(getClass().getResource("alert_skin.wtkx"));
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }

        alert.setContent(content);

        // Set the type image
        ImageView typeImageView = (ImageView)wtkxSerializer.getObjectByName("typeImageView");
        Image typeImage = null;

        switch (alert.getMessageType()) {
            case INFO: {
                if (informationImage == null) {
                    String informationImageName = (String)resources.get("informationImageName");
                    informationImage = Image.load(getClass().getResource(informationImageName));
                }

                typeImage = informationImage;
                break;
            }

            case WARNING: {
                if (warningImage == null) {
                    String warningImageName = (String)resources.get("warningImageName");
                    warningImage = Image.load(getClass().getResource(warningImageName));
                }

                typeImage = warningImage;
                break;
            }

            case ERROR: {
                if (errorImage == null) {
                    String errorImageName = (String)resources.get("errorImageName");
                    errorImage = Image.load(getClass().getResource(errorImageName));
                }

                typeImage = errorImage;
                break;
            }

            case QUESTION: {
                if (questionImage == null) {
                    String questionImageName = (String)resources.get("questionImageName");
                    questionImage = Image.load(getClass().getResource(questionImageName));
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
            optionButton.setStyles((Map<String, Object>)resources.get("optionButtonStyles"));
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
