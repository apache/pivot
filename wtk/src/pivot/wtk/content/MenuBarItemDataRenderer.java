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
package pivot.wtk.content;

import java.awt.Color;
import java.awt.Font;

import pivot.wtk.Button;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.MenuBar;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.ImageView;
import pivot.wtk.Label;
import pivot.wtk.media.Image;

/**
 * Default menu bar item data renderer.
 *
 * @author gbrown
 */
public class MenuBarItemDataRenderer extends FlowPane implements Button.DataRenderer {
    protected ImageView imageView = new ImageView();
    protected Label label = new Label();

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

    public void render(Object data, Button button, boolean highlighted) {
        Image icon = null;
        String text = null;

        if (data instanceof ButtonData) {
            ButtonData buttonData = (ButtonData)data;
            icon = buttonData.getIcon();
            text = buttonData.getText();
        } else if (data instanceof Image) {
            icon = (Image)data;
        } else {
            if (data != null) {
                text = data.toString();
            }
        }

        // Update the image view
        MenuBar.Item menuBarItem = (MenuBar.Item)button;
        MenuBar menuBar = menuBarItem.getMenuBar();

        if (icon == null) {
            imageView.setDisplayable(false);
        } else {
            imageView.setDisplayable(true);
            imageView.setImage(icon);
            imageView.getStyles().put("opacity", button.isEnabled() ? 1.0f : 0.5f);
        }

        // Update the label
        if (text == null) {
            label.setDisplayable(false);
        } else {
            label.setDisplayable(true);
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
}
