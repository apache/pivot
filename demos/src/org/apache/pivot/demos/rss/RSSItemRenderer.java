/*
 * Copyright (c) 2009 VMware, Inc.
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
package org.apache.pivot.demos.rss;

import java.awt.Color;
import java.awt.Font;

import org.apache.pivot.collections.List;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.xml.Element;
import org.apache.pivot.xml.TextNode;
import org.apache.pivot.xml.XMLSerializer;

public class RSSItemRenderer extends BoxPane implements ListView.ItemRenderer {
    private Label titleLabel = new Label();
    private Label categoriesLabel = new Label();
    private Label submitterLabel = new Label();

    public RSSItemRenderer() {
        super(Orientation.VERTICAL);

        getStyles().put("padding", new Insets(2, 2, 8, 2));
        getStyles().put("fill", true);

        titleLabel.getStyles().put("wrapText", true);
        add(titleLabel);

        categoriesLabel.getStyles().put("wrapText", true);
        add(categoriesLabel);

        submitterLabel.getStyles().put("wrapText", true);
        add(submitterLabel);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);

        // Since this component doesn't have a parent, it won't be validated
        // via layout; ensure that it is valid here
        validate();
    }

    @Override
    public void render(Object item, int index, ListView listView, boolean selected,
        boolean checked, boolean highlighted, boolean disabled) {
        if (item != null) {
            Element itemElement = (Element)item;

            String title = XMLSerializer.getText(itemElement, "title");
            titleLabel.setText(title);

            String categories = "Categories:";
            List<Element> categoryElements = itemElement.getElements("category");
            for (int i = 0, n = categoryElements.getLength(); i < n; i++) {
                Element categoryElement = categoryElements.get(i);
                TextNode categoryTextNode = (TextNode)categoryElement.get(0);
                String category = categoryTextNode.getText();

                if (i > 0) {
                    categories += ", ";
                }

                categories += category;
            }

            categoriesLabel.setText(categories);

            String submitter = XMLSerializer.getText(itemElement, "dz:submitter/dz:username");
            submitterLabel.setText("Submitter: " + submitter);
        }

        Font font = (Font)listView.getStyles().get("font");
        Font largeFont = font.deriveFont(Font.BOLD, 14);
        titleLabel.getStyles().put("font", largeFont);
        categoriesLabel.getStyles().put("font", font);
        submitterLabel.getStyles().put("font", font);

        Color color;
        if (listView.isEnabled() && !disabled) {
            if (selected) {
                if (listView.isFocused()) {
                    color = (Color)listView.getStyles().get("selectionColor");
                } else {
                    color = (Color)listView.getStyles().get("inactiveSelectionColor");
                }
            } else {
                color = (Color)listView.getStyles().get("color");
            }
        } else {
            color = (Color)listView.getStyles().get("disabledColor");
        }

        titleLabel.getStyles().put("color", color);
        categoriesLabel.getStyles().put("color", color);
        submitterLabel.getStyles().put("color", color);
    }
}
