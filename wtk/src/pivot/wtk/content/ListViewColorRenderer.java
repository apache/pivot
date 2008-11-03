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
import java.awt.Graphics2D;

import pivot.wtk.ListView;
import pivot.wtk.media.Image;

public class ListViewColorRenderer extends ListViewItemRenderer {
    public static class ColorBadge extends Image {
        private Color color = Color.BLACK;
        public static final int SIZE = 14;

        public int getWidth() {
            return SIZE;
        }

        public int getHeight() {
            return SIZE;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public void paint(Graphics2D graphics) {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, SIZE, SIZE);
            graphics.setColor(color);
            graphics.fillRect(2, 2, SIZE - 4, SIZE - 4);
            graphics.setColor(Color.GRAY);
            graphics.drawRect(0, 0, SIZE - 1, SIZE - 1);
        }
    }

    private ColorBadge colorBadge = new ColorBadge();
    private ListItem listItem = new ListItem(colorBadge);

    public ListViewColorRenderer() {
    	setShowIcon(true);
    }

    public void render(Object item, ListView listView, boolean selected,
        boolean highlighted, boolean disabled) {
    	ColorItem colorItem;
    	if (item instanceof ColorItem) {
    		colorItem = (ColorItem)item;
    	} else {
    		Color color;
        	if (item instanceof Color) {
        		color = (Color)item;
        	} else {
        		color = Color.decode(item.toString());
        	}

        	colorItem = new ColorItem(color);
    	}

    	colorBadge.setColor(colorItem.getColor());
        listItem.setText(colorItem.getName());

        super.render(listItem, listView, selected, highlighted, disabled);
    }
}
