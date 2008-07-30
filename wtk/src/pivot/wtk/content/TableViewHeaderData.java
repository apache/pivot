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

public class TableViewHeaderData {
    private Image icon = null;
    private String label = null;

    public TableViewHeaderData() {
        this(null, null);
    }

    public TableViewHeaderData(Image icon) {
        this(icon, null);
    }

    public TableViewHeaderData(String label) {
        this(null, label);
    }

    public TableViewHeaderData(Image icon, String label) {
        this.icon = icon;
        this.label = label;
    }

    public Image getIcon() {
        return icon;
    }

    public String getLabel() {
        return label;
    }
}
