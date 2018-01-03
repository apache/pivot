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
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.media.Image;

/**
 * Default menu bar item data renderer.
 */
public class MenuBarItemDataRenderer extends BoxPane implements Button.DataRenderer {
    protected ImageView imageView = new ImageView();
    protected Label label = new Label();

    public MenuBarItemDataRenderer() {
        getStyles().put(Style.horizontalAlignment, HorizontalAlignment.LEFT);
        getStyles().put(Style.verticalAlignment, VerticalAlignment.CENTER);
        getStyles().put(Style.padding, new Insets(4, 6, 4, 6));

        add(imageView);
        add(label);

        imageView.getStyles().put(Style.backgroundColor, null);
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

        if (data instanceof BaseContent) {
            BaseContent baseContent = (BaseContent) data;
            icon = baseContent.getIcon();
        } else if (data instanceof Image) {
            icon = (Image) data;
        }
        text = toString(data);

        // Update the image view
        MenuBar.Item menuBarItem = (MenuBar.Item) button;
        MenuBar menuBar = (MenuBar) menuBarItem.getParent();

        if (icon == null) {
            imageView.setVisible(false);
        } else {
            imageView.setVisible(true);
            imageView.setImage(icon);
            imageView.getStyles().put(Style.opacity, button.isEnabled() ? 1.0f : 0.5f);
        }

        // Update the label
        label.setText(text != null ? text : "");

        if (text == null) {
            label.setVisible(false);
        } else {
            label.setVisible(true);

            Font font = menuBar.getStyles().getFont(Style.font);
            label.getStyles().put(Style.font, font);

            Color color;
            if (button.isEnabled()) {
                if (highlighted) {
                    color = menuBar.getStyles().getColor(Style.activeColor);
                } else {
                    color = menuBar.getStyles().getColor(Style.color);
                }
            } else {
                color = menuBar.getStyles().getColor(Style.disabledColor);
            }

            label.getStyles().put(Style.color, color);
        }
    }

    @Override
    public String toString(Object data) {
        String string = null;

        if (data instanceof BaseContent) {
            BaseContent baseContent = (BaseContent) data;
            string = baseContent.getText();
        } else if (!(data instanceof Image)) {
            if (data != null) {
                string = data.toString();
            }
        }

        return string;
    }
}
