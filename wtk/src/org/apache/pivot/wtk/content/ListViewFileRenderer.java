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

import java.io.File;

import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.ListView;

/**
 * List view renderer for displaying file system contents.
 *
 * @author gbrown
 */
public class ListViewFileRenderer extends FileRenderer implements ListView.ItemRenderer {
    public ListViewFileRenderer() {
        getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
        getStyles().put("padding", new Insets(2, 3, 2, 3));
    }

    public void render(Object item, ListView listView, boolean selected,
        boolean checked, boolean highlighted, boolean disabled) {
        label.getStyles().put("font", listView.getStyles().get("font"));

        Object color = null;
        if (listView.isEnabled() && !disabled) {
            if (selected) {
                if (listView.isFocused()) {
                    color = listView.getStyles().get("selectionColor");
                } else {
                    color = listView.getStyles().get("inactiveSelectionColor");
                }
            } else {
                color = listView.getStyles().get("color");
            }
        } else {
            color = listView.getStyles().get("disabledColor");
        }

        label.getStyles().put("color", color);

        if (item != null) {
            File file = (File)item;

            // Update the image view
            imageView.setImage(getIcon(file));
            imageView.getStyles().put("opacity",
                (listView.isEnabled() && !disabled) ? 1.0f : 0.5f);

            // Update the label
            String text = file.getName();
            if (text.length() == 0) {
                text = System.getProperty("file.separator");
            }

            label.setText(text);
        }
    }
}
