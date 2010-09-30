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

import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.media.Image;


/**
 * Default button data renderer.
 */
public class ButtonDataRenderer extends BoxPane implements Button.DataRenderer {
    protected ImageView imageView = new ImageView();
    protected Label label = new Label();

    public ButtonDataRenderer() {
        getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
        getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

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

    @Override
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
        if (icon == null) {
            imageView.setVisible(false);
        } else {
            imageView.setVisible(true);
            imageView.setImage(icon);

            imageView.getStyles().put("opacity", button.isEnabled() ? 1.0f : 0.5f);
        }

        // Update the label
        label.setText(text);

        if (text == null) {
            label.setVisible(false);
        } else {
            label.setVisible(true);

            Font font = (Font)button.getStyles().get("font");
            label.getStyles().put("font", font);

            Color color;
            if (button.isEnabled()) {
                color = (Color)button.getStyles().get("color");
            } else {
                color = (Color)button.getStyles().get("disabledColor");
            }

            label.getStyles().put("color", color);
        }
    }

    public int getIconWidth() {
        return imageView.getPreferredWidth(-1);
    }

    public void setIconWidth(int iconWidth) {
        imageView.setPreferredWidth(iconWidth);
    }

    public int getIconHeight() {
        return imageView.getPreferredHeight(-1);
    }

    public void setIconHeight(int iconHeight) {
        imageView.setPreferredHeight(iconHeight);
    }

    public boolean getShowIcon() {
        return imageView.isVisible();
    }

    public void setShowIcon(boolean showIcon) {
        imageView.setVisible(showIcon);
    }

    public boolean getFillIcon() {
        return (Boolean)imageView.getStyles().get("fill");
    }

    public void setFillIcon(boolean fillIcon) {
        imageView.getStyles().put("fill", fillIcon);
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
