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
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewHeader;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.media.Image;

/**
 * Default table view header data renderer.
 */
public class TableViewHeaderDataRenderer extends BoxPane
    implements TableView.HeaderDataRenderer {
    protected ImageView imageView = new ImageView();
    protected Label label = new Label();

    public TableViewHeaderDataRenderer() {
        getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
        getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        getStyles().put("padding", new Insets(1, 2, 1, 2));

        add(imageView);
        add(label);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);

        // Since this component doesn't have a parent, it won't be validated
        // via layout; ensure that it is valid here
        validate();
    }

    @Override
    public void render(Object data, int columnIndex, TableViewHeader tableViewHeader,
        String columnName, boolean highlighted) {
        Image icon = null;
        String text = null;

        if (data instanceof TableViewHeaderData) {
            TableViewHeaderData tableViewHeaderData = (TableViewHeaderData)data;
            icon = tableViewHeaderData.getIcon();
            text = tableViewHeaderData.getText();
        } else if (data instanceof Image) {
            icon = (Image)data;
        } else {
            if (data != null) {
                text = data.toString();
            }
        }

        // Update the icon image view
        imageView.setImage(icon);

        if (icon == null) {
            imageView.setVisible(false);
        } else {
            imageView.setVisible(true);
            imageView.getStyles().put("opacity", tableViewHeader.isEnabled() ? 1.0f : 0.5f);
        }

        // Show/hide the label
        label.setText(text);

        if (text == null) {
            label.setVisible(false);
        } else {
            label.setVisible(true);

            // Update the label styles
            Component.StyleDictionary labelStyles = label.getStyles();

            Object labelFont = tableViewHeader.getStyles().get("font");
            if (labelFont instanceof Font) {
                labelStyles.put("font", labelFont);
            }

            Object color = null;
            if (tableViewHeader.isEnabled()) {
                color = tableViewHeader.getStyles().get("color");
            } else {
                color = tableViewHeader.getStyles().get("disabledColor");
            }

            if (color instanceof Color) {
                labelStyles.put("color", color);
            }
        }
    }

    @Override
    public String toString(Object data) {
        if (data == null) {
            throw new IllegalArgumentException();
        }

        String string;
        if (data instanceof TableViewHeaderData) {
            TableViewHeaderData tableViewHeaderData = (TableViewHeaderData)data;
            string = tableViewHeaderData.getText();
        } else {
            string = data.toString();
        }

        return string;
    }
}
