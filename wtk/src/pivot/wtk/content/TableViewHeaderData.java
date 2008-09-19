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

import pivot.wtk.media.Image;

/**
 * <p>Default table header data implementation.</p>
 *
 * @author gbrown
 */
public class TableViewHeaderData {
    private Image icon = null;
    private String text = null;

    public TableViewHeaderData() {
        this(null, null);
    }

    public TableViewHeaderData(Image icon) {
        this(icon, null);
    }

    public TableViewHeaderData(String text) {
        this(null, text);
    }

    public TableViewHeaderData(Image icon, String text) {
        this.icon = icon;
        this.text = text;
    }

    public Image getIcon() {
        return icon;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
