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

import pivot.wtk.Component;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.ImageView;
import pivot.wtk.Insets;
import pivot.wtk.Label;
import pivot.wtk.ListView;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.media.Image;

/**
 * Default list view item renderer.
 *
 * @author gbrown
 */
public class ListViewItemRenderer extends FlowPane implements ListView.ItemRenderer {
    protected ImageView imageView = new ImageView();
    protected Label label = new Label();

    public static final int DEFAULT_ICON_WIDTH = 16;
    public static final int DEFAULT_ICON_HEIGHT = 16;
    public static boolean DEFAULT_SHOW_ICON = false;

    public ListViewItemRenderer() {
        getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
        getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        getStyles().put("padding", new Insets(2, 3, 2, 3));

        add(imageView);
        add(label);

        imageView.setPreferredSize(DEFAULT_ICON_WIDTH, DEFAULT_ICON_HEIGHT);
        imageView.setDisplayable(DEFAULT_SHOW_ICON);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);

        // Since this component doesn't have a parent, it won't be validated
        // via layout; ensure that it is valid here
        validate();
    }

    public void render(Object item, ListView listView, boolean selected,
        boolean highlighted, boolean disabled) {
        Image icon = null;
        String text = null;

        if (item instanceof ListItem) {
            ListItem listItem = (ListItem)item;
            icon = listItem.getIcon();
            text = listItem.getText();
        } else if (item instanceof Image) {
            icon = (Image)item;
        } else {
            if (item != null) {
                text = (String)item.toString();
            }
        }

        // Update the image view
        imageView.setImage(icon);

        // Show/hide the label
        label.setText(text);

        renderStyles(listView, selected, highlighted, disabled);
    }

    protected void renderStyles(ListView listView, boolean selected,
        boolean highlighted, boolean disabled) {
        Component.StyleDictionary listViewStyles = listView.getStyles();

        Component.StyleDictionary imageViewStyles = imageView.getStyles();
        Component.StyleDictionary labelStyles = label.getStyles();

        imageViewStyles.put("opacity", listView.isEnabled() ? 1.0f : 0.5f);

        if (label.getText() != null) {
            Object labelFont = listViewStyles.get("font");
            if (labelFont instanceof Font) {
                labelStyles.put("font", labelFont);
            }

            Object color = null;
            if (listView.isEnabled() && !disabled) {
                if (selected) {
                    if (listView.isFocused()) {
                        color = listViewStyles.get("selectionColor");
                    } else {
                        color = listViewStyles.get("inactiveSelectionColor");
                    }
                } else {
                    color = listViewStyles.get("color");
                }
            } else {
                color = listViewStyles.get("disabledColor");
            }

            if (color instanceof Color) {
                labelStyles.put("color", color);
            }
        }
    }

    public int getIconWidth() {
        return imageView.getPreferredWidth(-1);
    }

    public void setIconWidth(int iconWidth) {
        if (iconWidth == -1) {
            throw new IllegalArgumentException();
        }

        imageView.setPreferredWidth(iconWidth);
    }

    public int getIconHeight() {
        return imageView.getPreferredHeight(-1);
    }

    public void setIconHeight(int iconHeight) {
        if (iconHeight == -1) {
            throw new IllegalArgumentException();
        }

        imageView.setPreferredHeight(iconHeight);
    }

    public boolean getShowIcon() {
        return imageView.isDisplayable();
    }

    public void setShowIcon(boolean showIcon) {
        imageView.setDisplayable(showIcon);
    }
}
