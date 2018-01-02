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
package org.apache.pivot.tutorials.webqueries;

import java.awt.Color;
import java.awt.Font;

import org.apache.pivot.collections.Map;
import org.apache.pivot.json.JSON;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Style;

/**
 * List item renderer for query results.
 */
public class ResultItemRenderer extends BoxPane implements ListView.ItemRenderer {
    private Label titleLabel = new Label();
    private Label addressLabel = new Label();
    private Label phoneLabel = new Label();

    public ResultItemRenderer() {
        super(Orientation.VERTICAL);

        add(titleLabel);
        add(addressLabel);
        add(phoneLabel);

        getStyles().put(Style.padding, new Insets(3, 2, 3, 2));
        getStyles().put(Style.spacing, 2);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        validate();
    }

    @Override
    public String toString(Object item) {
        return JSON.get(item, "title");
    }

    @Override
    public void render(Object item, int index, ListView listView, boolean selected,
        Button.State checkedState, boolean highlighted, boolean disabled) {
        if (item != null) {
            titleLabel.setText((String) JSON.get(item, "title"));
            phoneLabel.setText((String) JSON.get(item, "Phone"));

            Map<String, ?> location = JSON.get(item, "['y:location']");
            if (location == null) {
                addressLabel.setText("");
            } else {
                String street = JSON.get(location, "street");
                String city = JSON.get(location, "city");
                String state = JSON.get(location, "state");
                addressLabel.setText(street + ", " + city + " " + state);
            }
        }

        Font font = listView.getStyles().getFont(Style.font);
        titleLabel.getStyles().put(Style.font, font.deriveFont(font.getStyle() | Font.BOLD));
        phoneLabel.getStyles().put(Style.font, font);
        addressLabel.getStyles().put(Style.font, font);

        Color color;
        if (listView.isEnabled() && !disabled) {
            if (selected) {
                if (listView.isFocused()) {
                    color = listView.getStyles().getColor(Style.selectionColor);
                } else {
                    color = listView.getStyles().getColor(Style.inactiveSelectionColor);
                }
            } else {
                color = listView.getStyles().getColor(Style.color);
            }
        } else {
            color = listView.getStyles().getColor(Style.disabledColor);
        }

        titleLabel.getStyles().put(Style.color, color);
        phoneLabel.getStyles().put(Style.color, color);
        addressLabel.getStyles().put(Style.color, color);
    }
}
