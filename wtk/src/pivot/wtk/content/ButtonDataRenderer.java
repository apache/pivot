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
import pivot.collections.Dictionary;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Button;
import pivot.wtk.Component;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.ImageView;
import pivot.wtk.Label;
import pivot.wtk.media.Image;

public class ButtonDataRenderer extends FlowPane implements Button.DataRenderer {
    protected ImageView imageView = new ImageView();
    protected Label label = new Label();

    public static final String ICON_KEY = "icon";
    public static final String ICON_URL_KEY = "iconURL";
    public static final String LABEL_KEY = "label";

    public ButtonDataRenderer() {
        super();

        getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
        getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

        getComponents().add(imageView);
        getComponents().add(label);

        imageView.getStyles().put("backgroundColor", null);
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

        if (data instanceof Dictionary<?, ?>) {
            Dictionary<String, Object> dictionary = (Dictionary<String, Object>)data;

            icon = (Image)dictionary.get(ICON_KEY);

            if (icon == null) {
                URL iconURL = (URL)dictionary.get(ICON_URL_KEY);

                if (iconURL != null) {
                    ApplicationContext applicationContext = ApplicationContext.getInstance();
                    icon = (Image)applicationContext.getResources().get(iconURL);

                    if (icon == null) {
                        icon = Image.load(iconURL);
                        applicationContext.getResources().put(iconURL, icon);
                    }
                }
            }

            text = (String)dictionary.get(LABEL_KEY);
        } else if (data instanceof Image) {
            icon = (Image)data;
        } else {
            text = (data == null) ? null : data.toString();
        }

        // Show/hide the image view
        if (icon == null) {
            imageView.setDisplayable(false);
        } else {
            imageView.setDisplayable(true);
            imageView.setImage(icon);

            imageView.getStyles().put("opacity", button.isEnabled() ? 1.0f : 0.5f);
        }

        // Show/hide the label
        if (text == null) {
            label.setDisplayable(false);
        } else {
            label.setDisplayable(true);
            label.setText(text);

            // Update the label styles
            Component.StyleDictionary labelStyles = label.getStyles();

            Object labelFont = button.getStyles().get("font");
            if (labelFont instanceof Font) {
                labelStyles.put("font", labelFont);
            } else {
                labelStyles.remove("font");
            }

            Object color = button.isEnabled() ?
                button.getStyles().get("color") :
                    button.getStyles().get("disabledColor");

            if (color instanceof Color) {
                labelStyles.put("color", color);
            } else {
                labelStyles.remove("color");
            }
        }
    }
}
