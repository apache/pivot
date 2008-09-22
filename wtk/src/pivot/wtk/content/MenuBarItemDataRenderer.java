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
import pivot.wtk.MenuBar;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.ImageView;
import pivot.wtk.Label;
import pivot.wtk.media.Image;

/**
 * <p>Default menu bar item data renderer.</p>
 *
 * @author gbrown
 */
public class MenuBarItemDataRenderer extends FlowPane implements Button.DataRenderer {
    protected ImageView imageView = new ImageView();
    protected Label label = new Label();

    public static final String ICON_KEY = "icon";
    public static final String TEXT_KEY = "text";

    public MenuBarItemDataRenderer() {
        getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
        getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        getStyles().put("padding", 3);

        add(imageView);
        add(label);

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
            }
        }

        // Update the image view
        MenuBar.Item menuBarItem = (MenuBar.Item)button;
        MenuBar menuBar = menuBarItem.getMenuBar();

        imageView.setImage(icon);
        imageView.getStyles().put("opacity", button.isEnabled() ? 1.0f : 0.5f);

        // Update the label
        Object font = menuBar.getStyles().get("font");
        if (font instanceof Font) {
            label.getStyles().put("font", font);
        }

        Object color;
        if (button.isEnabled()) {
            if (highlighted) {
                color = menuBar.getStyles().get("highlightColor");
            } else {
                color = menuBar.getStyles().get("color");
            }
        } else {
            color = menuBar.getStyles().get("disabledColor");
        }

        if (color instanceof Color) {
            label.getStyles().put("color", color);
        }

        label.setText(text);
    }
}
