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
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.PromptListener;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;

/**
 * Prompt skin.
 */
public class TerraPromptSkin extends TerraSheetSkin
    implements PromptListener {
    private ImageView typeImageView = null;
    private Label messageLabel = null;
    private BoxPane messageBoxPane = null;
    private BoxPane optionButtonBoxPane = null;

    private ButtonPressListener optionButtonPressListener = new ButtonPressListener() {
        @Override
        public void buttonPressed(Button button) {
            int optionIndex = optionButtonBoxPane.indexOf(button);

            if (optionIndex >= 0) {
                Prompt prompt = (Prompt)getComponent();
                prompt.setSelectedOptionIndex(optionIndex);
                prompt.close(true);
            }
        }
    };

    public TerraPromptSkin() {
        setResizable(true);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Prompt prompt = (Prompt)component;
        prompt.setPreferredWidth(320);
        prompt.setMinimumWidth(160);

        prompt.getPromptListeners().add(this);

        // Load the prompt content
        BXMLSerializer bxmlSerializer = new BXMLSerializer();

        Component content;
        try {
            content = (Component)bxmlSerializer.readObject(TerraPromptSkin.class,
                "terra_prompt_skin.bxml");
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }

        prompt.setContent(content);

        typeImageView = (ImageView)bxmlSerializer.getNamespace().get("typeImageView");
        messageLabel = (Label)bxmlSerializer.getNamespace().get("messageLabel");
        messageBoxPane = (BoxPane)bxmlSerializer.getNamespace().get("messageBoxPane");
        optionButtonBoxPane = (BoxPane)bxmlSerializer.getNamespace().get("optionButtonBoxPane");

        for (Object option : prompt.getOptions()) {
            PushButton optionButton = new PushButton(option);
            optionButton.setStyleName(TerraPromptSkin.class.getPackage().getName()
                + "." + TerraTheme.COMMAND_BUTTON_STYLE);
            optionButton.getButtonPressListeners().add(optionButtonPressListener);

            optionButtonBoxPane.add(optionButton);
        }

        messageTypeChanged(prompt, null);
        messageChanged(prompt, null);
        bodyChanged(prompt, null);
    }

    @Override
    public void windowOpened(Window window) {
        super.windowOpened(window);

        Prompt prompt = (Prompt)window;
        int index = prompt.getSelectedOptionIndex();

        if (index >= 0) {
            optionButtonBoxPane.get(index).requestFocus();
        } else {
            window.requestFocus();
        }
    }

    @Override
    public void messageTypeChanged(Prompt prompt, MessageType previousMessageType) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        typeImageView.setImage(theme.getMessageIcon(prompt.getMessageType()));
    }

    @Override
    public void messageChanged(Prompt prompt, String previousMessage) {
        String message = prompt.getMessage();
        messageLabel.setText(message != null ? message : "");
    }

    @Override
    public void bodyChanged(Prompt prompt, Component previousBody) {
        if (previousBody != null) {
            messageBoxPane.remove(previousBody);
        }

        Component body = prompt.getBody();
        if (body != null) {
            messageBoxPane.add(body);
        }
    }

    @Override
    public void optionInserted(Prompt prompt, int index) {
        Object option = prompt.getOptions().get(index);

        PushButton optionButton = new PushButton(option);
        optionButton.setStyleName(TerraPromptSkin.class.getPackage().getName()
            + "." + TerraTheme.COMMAND_BUTTON_STYLE);
        optionButton.getButtonPressListeners().add(optionButtonPressListener);

        optionButtonBoxPane.insert(optionButton, index);
    }

    @Override
    public void optionsRemoved(Prompt prompt, int index, Sequence<?> removed) {
        optionButtonBoxPane.remove(index, removed.getLength());
    }

    @Override
    public void selectedOptionChanged(Prompt prompt, int previousSelectedOption) {
        int index = prompt.getSelectedOptionIndex();

        if (prompt.isOpen()
            && index >= 0) {
            optionButtonBoxPane.get(index).requestFocus();
        }
    }
}
