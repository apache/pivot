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
package org.apache.pivot.wtk.content;

import java.awt.Color;
import java.awt.Font;

import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.media.Image;

/**
 * Default menu item data renderer.
 */
public class MenuItemDataRenderer extends TablePane implements Button.DataRenderer {
    protected ImageView imageView = new ImageView();
    protected Label textLabel = new Label();
    protected Label keyboardShortcutLabel = new Label();

    public MenuItemDataRenderer() {
        getStyles().put("padding", new Insets(2));

        getColumns().add(new TablePane.Column(1, true));
        getColumns().add(new TablePane.Column());

        BoxPane boxPane = new BoxPane();
        boxPane.add(imageView);
        boxPane.add(textLabel);
        boxPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        boxPane.getStyles().put("padding", new Insets(0, 0, 0, 6));

        TablePane.Row row = new TablePane.Row();
        row.add(boxPane);
        row.add(keyboardShortcutLabel);

        getRows().add(row);

        imageView.getStyles().put("backgroundColor", null);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);

        // Since this component doesn't have a parent, it won't be validated
        // via layout; ensure that it is valid here
        validate();
    }

    @Override
    public void render(Object data, Button button, boolean highlighted) {
        Image icon = null;
        String text = null;
        Keyboard.KeyStroke keyboardShortcut = null;

        if (data instanceof ButtonData) {
            ButtonData buttonData = (ButtonData)data;
            icon = buttonData.getIcon();
            text = buttonData.getText();

            if (buttonData instanceof MenuItemData) {
                MenuItemData menuItemData = (MenuItemData)buttonData;
                keyboardShortcut = menuItemData.getKeyboardShortcut();
            }
        } else if (data instanceof Image) {
            icon = (Image)data;
        } else {
            if (data != null) {
                text = data.toString();
            }
        }

        // If the button is selected, icon is a checkmark; otherwise,
        // attempt to retrieve icon from button data
        if (button.isSelected()) {
            icon = (Image)button.getStyles().get("checkmarkImage");
        }

        // Update the image view
        Menu.Item menuItem = (Menu.Item)button;
        Menu menu = (Menu)menuItem.getParent();

        int margin = (Integer)menu.getStyles().get("margin");
        Insets padding = (Insets)getStyles().get("padding");

        imageView.setImage(icon);
        imageView.setPreferredWidth(margin - padding.left * 2);
        imageView.getStyles().put("opacity", button.isEnabled() ? 1.0f : 0.5f);

        // Update the labels
        textLabel.setText(text);

        Font font = (Font)menu.getStyles().get("font");
        textLabel.getStyles().put("font", font);
        keyboardShortcutLabel.getStyles().put("font", font.deriveFont(Font.ITALIC));

        Color color;
        if (button.isEnabled()) {
            if (highlighted) {
                color = (Color)menu.getStyles().get("activeColor");
            } else {
                color = (Color)menu.getStyles().get("color");
            }
        } else {
            color = (Color)menu.getStyles().get("disabledColor");
        }

        textLabel.getStyles().put("color", color);
        keyboardShortcutLabel.getStyles().put("color", color);

        boolean showKeyboardShortcuts = false;
        if (menu.getStyles().containsKey("showKeyboardShortcuts")) {
            showKeyboardShortcuts = (Boolean)menu.getStyles().get("showKeyboardShortcuts");
        }

        if (showKeyboardShortcuts) {
            keyboardShortcutLabel.setVisible(true);
            keyboardShortcutLabel.setText(keyboardShortcut == null ?
                null : keyboardShortcut.toString());
        } else {
            keyboardShortcutLabel.setVisible(false);
        }
    }

    @Override
    public String toString(Object data) {
        String string = null;

        if (data instanceof ButtonData) {
            ButtonData buttonData = (ButtonData)data;
            string = buttonData.getText();
        } else {
            if (data != null) {
                string = data.toString();
            }
        }

        return string;
    }
}
