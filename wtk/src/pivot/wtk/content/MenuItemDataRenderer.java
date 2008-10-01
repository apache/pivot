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
package pivot.wtk.content;

import java.awt.Color;
import java.awt.Font;
import java.net.URL;

import pivot.beans.BeanDictionary;
import pivot.collections.Dictionary;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Button;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.ImageView;
import pivot.wtk.Insets;
import pivot.wtk.Keyboard;
import pivot.wtk.Label;
import pivot.wtk.Menu;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.media.Image;

/**
 * Default menu item data renderer.
 *
 * @author gbrown
 */
public class MenuItemDataRenderer extends FlowPane implements Button.DataRenderer {
    protected ImageView imageView = new ImageView();
    protected Label textLabel = new Label();
    protected Label keyboardShortcutLabel = new Label();

    public static final String ICON_KEY = "icon";
    public static final String TEXT_KEY = "text";
    public static final String KEYBOARD_SHORTCUT_KEY = "keyboardShortcut";

    public MenuItemDataRenderer() {
        getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        getStyles().put("padding", new Insets(2));

        add(imageView);
        add(textLabel);
        add(keyboardShortcutLabel);

        imageView.getStyles().put("backgroundColor", null);

        keyboardShortcutLabel.getStyles().put("horizontalAlignment",
            HorizontalAlignment.RIGHT);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);

        // Since this component doesn't have a parent, it won't be validated
        // via layout; ensure that it is valid here
        validate();
    }

    @SuppressWarnings("unchecked")
    public void render(Object data, Button button, boolean highlighted) {
        Image icon = null;
        String text = null;
        Keyboard.KeyStroke keyboardShortcut = null;

        if (data != null) {
            if (data instanceof Image) {
                icon = (Image)data;
            } else if (data instanceof String) {
                text = (String)data;
            } else {
                Dictionary<String, Object> dictionary = (data instanceof Dictionary<?, ?>) ?
                    (Dictionary<String, Object>)data : new BeanDictionary(data);

                Object iconValue = dictionary.get(ICON_KEY);
                if (iconValue instanceof Image) {
                    icon = (Image)iconValue;
                } else {
                    if (iconValue instanceof String) {
                        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                        iconValue = classLoader.getResource((String)iconValue);
                    }

                    if (iconValue instanceof URL) {
                        URL iconURL = (URL)iconValue;
                        icon = (Image)ApplicationContext.getResourceCache().get(iconURL);

                        if (icon == null) {
                            icon = Image.load(iconURL);
                            ApplicationContext.getResourceCache().put(iconURL, icon);
                        }
                    }
                }

                text = (String)dictionary.get(TEXT_KEY);

                Object keyboardShortcutValue = dictionary.get(KEYBOARD_SHORTCUT_KEY);
                if (keyboardShortcutValue instanceof Keyboard.KeyStroke) {
                    keyboardShortcut = (Keyboard.KeyStroke)keyboardShortcutValue;
                } else {
                    if (keyboardShortcutValue instanceof String) {
                        keyboardShortcut = Keyboard.KeyStroke.decode((String)keyboardShortcutValue);
                    }
                }
            }
        }

        // If the button is selected, icon is a checkmark; otherwise,
        // attempt to retrieve icon from button data
        if (button.isSelected()) {
            icon = (Image)button.getStyles().get("checkmarkImage");
        }

        // Update the image view
        Menu.Item menuItem = (Menu.Item)button;
        Menu menu = menuItem.getSection().getMenu();

        int margin = (Integer)menu.getStyles().get("margin");
        Insets padding = (Insets)getStyles().get("padding");

        imageView.setImage(icon);
        imageView.setPreferredWidth(margin - padding.left * 2);
        imageView.getStyles().put("opacity", button.isEnabled() ? 1.0f : 0.5f);

        // Update the labels
        Object font = menu.getStyles().get("font");
        if (font instanceof Font) {
            textLabel.getStyles().put("font", font);
            keyboardShortcutLabel.getStyles().put("font",
                ((Font)font).deriveFont(Font.ITALIC));
        }

        Object color;
        if (button.isEnabled()) {
            if (highlighted) {
                color = menu.getStyles().get("highlightColor");
            } else {
                color = menu.getStyles().get("color");
            }
        } else {
            color = menu.getStyles().get("disabledColor");
        }

        if (color instanceof Color) {
            textLabel.getStyles().put("color", color);
            keyboardShortcutLabel.getStyles().put("color", color);
        }

        textLabel.setText(text);

        boolean showKeyboardShortcuts = false;
        if (menu.getStyles().containsKey("showKeyboardShortcuts")) {
            showKeyboardShortcuts = (Boolean)menu.getStyles().get("showKeyboardShortcuts");
        }

        if (showKeyboardShortcuts) {
            keyboardShortcutLabel.setDisplayable(true);
            keyboardShortcutLabel.setText(keyboardShortcut == null ?
                null : keyboardShortcut.toString());
        } else {
            keyboardShortcutLabel.setDisplayable(false);
        }
    }
}
