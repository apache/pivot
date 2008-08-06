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
import java.awt.Font;

import pivot.collections.List;
import pivot.collections.Sequence;

import pivot.wtk.Alert;
import pivot.wtk.Alert.Type;
import pivot.wtk.AlertListener;
import pivot.wtk.AlertOptionListener;
import pivot.wtk.AlertSelectionListener;
import pivot.wtk.Border;
import pivot.wtk.Button;
import pivot.wtk.Component;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.ImageView;
import pivot.wtk.Insets;
import pivot.wtk.Label;
import pivot.wtk.Orientation;
import pivot.wtk.PushButton;
import pivot.wtk.TablePane;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.Window;
import pivot.wtk.media.Image;

/**
 *
 * @author tvolkert
 */
public class AlertSkin extends DialogSkin
    implements AlertListener, AlertOptionListener, AlertSelectionListener {

    public static final int DEFAULT_ALERT_WIDTH = 300;

    private static final Font SUBJECT_LABEL_FONT =
        new Font("Verdana", Font.BOLD, 11);
    private static final Color OPTION_BUTTON_COLOR = Color.WHITE;
    private static final Color OPTION_BUTTON_BACKGROUND_COLOR =
        new Color(0x3c, 0x77, 0xb2);
    private static final Color OPTION_BUTTON_BORDER_COLOR =
        new Color(0x2c, 0x56, 0x80);
    private static final Color OPTION_BUTTON_BEVEL_COLOR =
        new Color(0x45, 0x89, 0xcc);
    private static final Color OPTION_BUTTON_PRESSED_BEVEL_COLOR =
        new Color(0x34, 0x66, 0x99);

    private FlowPane alertContent = new FlowPane(Orientation.VERTICAL);
    private FlowPane messageFlowPane = new FlowPane(Orientation.VERTICAL);
    private FlowPane buttonFlowPane = new FlowPane(Orientation.HORIZONTAL);
    private ImageView typeImageView = new ImageView();
    private Label subjectLabel = null;

    public AlertSkin() {
        setResizable(false);

        alertContent.getStyles().put("horizontalAlignment",
            HorizontalAlignment.JUSTIFY);
        alertContent.getStyles().put("spacing", 8);
        messageFlowPane.getStyles().put("spacing", 10);
        messageFlowPane.getStyles().put("horizontalAlignment",
            HorizontalAlignment.JUSTIFY);
        buttonFlowPane.getStyles().put("horizontalAlignment",
            HorizontalAlignment.RIGHT);
        buttonFlowPane.getStyles().put("verticalAlignment",
            VerticalAlignment.JUSTIFY);

        Border border = new Border();
        border.getStyles().put("backgroundColor", new Color(0xe6, 0xe3, 0xda));
        border.getStyles().put("borderColor", new Color(0x99, 0x99, 0x99));
        border.getStyles().put("padding", new Insets(12));
        alertContent.add(border);
        alertContent.add(buttonFlowPane);

        TablePane tablePane = new TablePane();

        tablePane.getRows().add(new TablePane.Row(-1));
        tablePane.getColumns().add(new TablePane.Column(-1));
        tablePane.getColumns().add(new TablePane.Column(1, true));

        tablePane.getStyles().put("spacing", 12);

        border.setContent(tablePane);

        FlowPane imageFlow = new FlowPane(Orientation.VERTICAL);
        imageFlow.getStyles().put("verticalAlignment", VerticalAlignment.TOP);
        imageFlow.add(typeImageView);

        tablePane.setCellComponent(0, 0, imageFlow);
        tablePane.setCellComponent(0, 1, messageFlowPane);
    }

    @Override
    public void install(Component component) {
        validateComponentType(component, Alert.class);

        super.install(component);

        Alert alert = (Alert)component;

        alert.getAlertListeners().add(this);
        alert.getAlertOptionListeners().add(this);
        alert.getAlertSelectionListeners().add(this);

        alert.setContent(alertContent);
        alert.setPreferredWidth(DEFAULT_ALERT_WIDTH);

        setTypeImage(alert.getType());
        setSubjectLabel(alert.getSubject());
        setBodyComponent(alert.getBody(), null);
        setOptionButtons(alert.getOptionData());

        focusSelectedOptionButton();
    }

    @Override
    public void uninstall() {
        Alert alert = (Alert)getComponent();

        setTypeImage(null);
        setSubjectLabel(null);
        setBodyComponent(null, alert.getBody());
        setOptionButtons(null);

        alert.setContent(null);

        alert.getAlertListeners().remove(this);
        alert.getAlertOptionListeners().remove(this);
        alert.getAlertSelectionListeners().remove(this);

        super.uninstall();
    }

    private void setTypeImage(Type type) {
        String resourceName = null;

        if (type != null) {
            switch (type) {
            case ERROR:
                resourceName = "/pivot/wtk/skin/terra/AlertSkin-Error-32x32.png";
                break;
            case WARNING:
                resourceName = "/pivot/wtk/skin/terra/AlertSkin-Warning-32x32.png";
                break;
            case QUESTION:
                resourceName = "/pivot/wtk/skin/terra/AlertSkin-Question-32x32.png";
                break;
            case INFO:
                resourceName = "/pivot/wtk/skin/terra/AlertSkin-Information-32x32.png";
                break;
            case APPLICATION:
                // TODO
                break;
            }
        }

        Image typeIcon = null;
        if (resourceName != null) {
            typeIcon = Image.load(getClass().getResource(resourceName));
        }
        typeImageView.setImage(typeIcon);
    }

    private void setSubjectLabel(String subject) {
        if (subject != null && subjectLabel == null) {
            subjectLabel = new Label(subject);
            subjectLabel.getStyles().put("font", SUBJECT_LABEL_FONT);
            subjectLabel.getStyles().put("wrapText", true);
            messageFlowPane.insert(subjectLabel, 0);
        } else if (subject == null && subjectLabel != null) {
            messageFlowPane.remove(subjectLabel);
            subjectLabel = null;
        } else if (subject != null) {
            subjectLabel.setText(subject);
        }
    }

    private void setBodyComponent(Component body, Component previousBody) {
        if (previousBody != null) {
            messageFlowPane.remove(previousBody);
        }

        if (body != null) {
            messageFlowPane.add(body);
        }
    }

    private void setOptionButtons(List<String> optionData) {
        buttonFlowPane.removeAll();

        if (optionData != null) {
            int i = 0;
            for (String option : optionData) {
                insertOptionButton(option, i++);
            }
        }
    }

    private void insertOptionButton(String option, int index) {
        PushButton button = new PushButton(option);
        button.getButtonPressListeners().add(this);
        buttonFlowPane.insert(button, index);

        styleOptionButton(button);
    }

    private void styleOptionButton(Button button) {
        button.setPreferredWidth(-1);
        int heightConstraint = buttonFlowPane.getPreferredHeight(-1);
        int preferredWidth = button.getPreferredWidth(heightConstraint);

        int minWidth = 3 * heightConstraint;
        if (preferredWidth < minWidth) {
            button.setPreferredWidth(minWidth);
        }

        button.getStyles().put("color", OPTION_BUTTON_COLOR);
        button.getStyles().put("backgroundColor", OPTION_BUTTON_BACKGROUND_COLOR);
        button.getStyles().put("borderColor", OPTION_BUTTON_BORDER_COLOR);
        button.getStyles().put("bevelColor", OPTION_BUTTON_BEVEL_COLOR);
        button.getStyles().put("pressedBevelColor", OPTION_BUTTON_PRESSED_BEVEL_COLOR);
    }

    private void focusSelectedOptionButton() {
        Alert alert = (Alert)getComponent();

        if (alert.isOpen()) {
            int index = alert.getSelectedOption();

            if (index >= 0) {
                Component.setFocusedComponent(buttonFlowPane.get(index));
            }
        }
    }

    @Override
    public void windowOpened(Window window) {
        focusSelectedOptionButton();
    }

    @Override
    public void buttonPressed(Button button) {
        int optionIndex = buttonFlowPane.indexOf(button);

        if (optionIndex >= 0) {
            Alert alert = (Alert)getComponent();
            alert.setSelectedOption(optionIndex);
            alert.close(true);
        } else {
            super.buttonPressed(button);
        }
    }

    public void typeChanged(Alert alert, Type previousType) {
        setTypeImage(alert.getType());
    }

    public void subjectChanged(Alert alert, String previousSubject) {
        setSubjectLabel(alert.getSubject());
    }

    public void bodyChanged(Alert alert, Component previousBody) {
        setBodyComponent(alert.getBody(), previousBody);
    }

    public void optionDataChanged(Alert alert, List<String> previousOptionData) {
        setOptionButtons(alert.getOptionData());
    }

    public void optionInserted(Alert alert, int index) {
        String option = alert.getOptionData().get(index);
        insertOptionButton(option, index);
    }

    public void optionsRemoved(Alert alert, int index, int count) {
        Sequence<Component> buttons = buttonFlowPane.remove(index, count);

        for (int i = 0, n = buttons.getLength(); i < n; i++) {
            Button button = (Button)buttons.get(i);
            button.getButtonPressListeners().remove(this);
        }
    }

    public void optionUpdated(Alert alert, int index) {
        Button button = (Button)buttonFlowPane.get(index);
        styleOptionButton(button);
    }

    public void optionsSorted(Alert alert) {
        setOptionButtons(alert.getOptionData());
    }

    public void selectedOptionChanged(Alert alert, int previousSelectedOption) {
        focusSelectedOptionButton();
    }
}
