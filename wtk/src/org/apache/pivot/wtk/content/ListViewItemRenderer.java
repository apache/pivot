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

import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.media.Image;

/**
 * Default list view item renderer.
 */
public class ListViewItemRenderer extends BoxPane implements ListView.ItemRenderer {
    protected ImageView imageView = new ImageView();
    protected Label label = new Label();

    public static final int DEFAULT_ICON_WIDTH = 16;
    public static final int DEFAULT_ICON_HEIGHT = 16;
    public static final boolean DEFAULT_SHOW_ICON = false;

    public ListViewItemRenderer() {
        getStyles().put(Style.horizontalAlignment, HorizontalAlignment.LEFT);
        getStyles().put(Style.verticalAlignment, VerticalAlignment.CENTER);
        getStyles().put(Style.padding, new Insets(2, 3, 2, 3));

        add(imageView);
        add(label);

        imageView.setPreferredSize(DEFAULT_ICON_WIDTH, DEFAULT_ICON_HEIGHT);
        imageView.setVisible(DEFAULT_SHOW_ICON);
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
        Button.State state, boolean highlighted, boolean disabled) {
        renderStyles(listView, selected, highlighted, disabled);

        Image icon = null;
        String text = null;

        if (item instanceof BaseContent) {
            BaseContent baseItem = (BaseContent) item;
            icon = baseItem.getIcon();
        } else if (item instanceof Image) {
            icon = (Image) item;
        }
        text = toString(item);

        imageView.setImage(icon);
        label.setText(text != null ? text : "");
    }

    protected void renderStyles(ListView listView, boolean selected, @SuppressWarnings("unused")
    boolean highlighted, boolean disabled) {
        imageView.getStyles().put(Style.opacity, listView.isEnabled() ? 1.0f : 0.5f);

        label.getStyles().copy(Style.font, listView.getStyles());

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

        label.getStyles().put(Style.color, color);
    }

    @Override
    public String toString(Object item) {
        String string = null;

        if (item instanceof BaseContent) {
            BaseContent baseItem = (BaseContent) item;
            string = baseItem.getText();
        } else if (!(item instanceof Image)) {
            if (item != null) {
                string = item.toString();
            }
        }

        return string;
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
        return imageView.getStyles().getBoolean(Style.fill);
    }

    public void setFillIcon(boolean fillIcon) {
        imageView.getStyles().put(Style.fill, fillIcon);
    }

    /**
     * Gets the bounds of the text that is rendered by this renderer.
     *
     * @return The bounds of the rendered text, or <tt>null</tt> if this
     * renderer did not render any text.
     */
    public Bounds getTextBounds() {
        return (label.isVisible() ? label.getBounds() : null);
    }
}
