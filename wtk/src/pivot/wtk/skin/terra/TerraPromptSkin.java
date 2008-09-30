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
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.FlowPane;
import pivot.wtk.ImageView;
import pivot.wtk.Label;
import pivot.wtk.Prompt;
import pivot.wtk.PromptListener;
import pivot.wtk.PushButton;
import pivot.wtk.Window;
import pivot.wtk.media.Image;
import pivot.wtkx.WTKXSerializer;

/**
 * Prompt skin.
 *
 * @author tvolkert
 * @author gbrown
 */
public class TerraPromptSkin extends TerraSheetSkin
    implements PromptListener {
    private ArrayList<Button> optionButtons = new ArrayList<Button>();

    private static Image informationImage = null;
    private static Image warningImage = null;
    private static Image errorImage = null;
    private static Image questionImage = null;

    private static Resources resources = null;

    static {
        try {
            resources = new Resources(TerraPromptSkin.class.getName());
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public TerraPromptSkin() {
        setResizable(false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void install(Component component) {
        super.install(component);

        Prompt prompt = (Prompt)component;
        prompt.getPromptListeners().add(this);

        // Load the prompt content
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        Component content = null;

        try {
            content = (Component)wtkxSerializer.readObject(getClass().getResource("prompt_skin.wtkx"));
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }

        prompt.setContent(content);

        // Set the type image
        ImageView typeImageView = (ImageView)wtkxSerializer.getObjectByName("typeImageView");
        Image typeImage = null;

        switch (prompt.getMessageType()) {
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
        String message = prompt.getMessage();
        messageLabel.setText(message);

        // Set the body
        FlowPane messageFlowPane = (FlowPane)wtkxSerializer.getObjectByName("messageFlowPane");
        Component body = prompt.getBody();
        if (body != null) {
            messageFlowPane.add(body);
        }

        // Add the option buttons
        FlowPane buttonFlowPane = (FlowPane)wtkxSerializer.getObjectByName("buttonFlowPane");

        for (int i = 0, n = prompt.getOptionCount(); i < n; i++) {
            Object option = prompt.getOption(i);

            PushButton optionButton = new PushButton(option);
            optionButton.setStyles((Map<String, Object>)resources.get("optionButtonStyles"));
            optionButton.getStyles().put("preferredAspectRatio", 3);

            optionButton.getButtonPressListeners().add(new ButtonPressListener() {
                public void buttonPressed(Button button) {
                    int optionIndex = optionButtons.indexOf(button);

                    if (optionIndex >= 0) {
                        Prompt prompt = (Prompt)getComponent();
                        prompt.setSelectedOption(optionIndex);
                        prompt.close(true);
                    }
                }
            });

            buttonFlowPane.add(optionButton);
            optionButtons.add(optionButton);
        }
    }

    @Override
    public void uninstall() {
        Prompt prompt = (Prompt)getComponent();
        prompt.getPromptListeners().remove(this);

        prompt.setContent(null);

        super.uninstall();
    }

    @Override
    public void windowOpened(Window window) {
        super.windowOpened(window);

        Prompt prompt = (Prompt)window;
        int index = prompt.getSelectedOption();

        if (index >= 0) {
            optionButtons.get(index).requestFocus();
        }
    }

    public void selectedOptionChanged(Prompt prompt, int previousSelectedOption) {
        int index = prompt.getSelectedOption();

        if (prompt.isOpen()
            && index >= 0) {
            optionButtons.get(index).requestFocus();
        }
    }
}
