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
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.FlowPane;
import pivot.wtk.ImageView;
import pivot.wtk.Label;
import pivot.wtk.Prompt;
import pivot.wtk.PromptListener;
import pivot.wtk.PushButton;
import pivot.wtk.Theme;
import pivot.wtk.Window;
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
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        Resources resources = theme.getResources();

        WTKXSerializer wtkxSerializer = new WTKXSerializer(resources);
        Component content = null;

        try {
            content = (Component)wtkxSerializer.readObject(getClass().getResource("prompt_skin.wtkx"));
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }

        prompt.setContent(content);

        // Set the type image
        ImageView typeImageView = (ImageView)wtkxSerializer.getObjectByName("typeImageView");
        typeImageView.setImage(theme.getMessageIcon(prompt.getMessageType()));

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
