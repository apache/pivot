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

import pivot.beans.Bean;
import pivot.wtk.media.Image;

/**
 * Default list item implementation. Represents items that can be displayed in
 * a list.
 *
 * @author gbrown
 */
public class ListItem extends Bean {
    private Image icon = null;
    private String label = null;

    public ListItem() {
        this(null, null);
    }

    public ListItem(Image icon) {
        this(icon, null);
    }

    public ListItem(String label) {
        this(null, label);
    }

    public ListItem(Image icon, String label) {
        this.icon = icon;
        this.label = label;
    }

    public Image getIcon() {
        return icon;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
