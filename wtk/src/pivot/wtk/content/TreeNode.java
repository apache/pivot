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

import pivot.collections.ArrayList;
import pivot.wtk.media.Image;

/**
 * Default tree node implementation.
 *
 * @author gbrown
 */
public class TreeNode extends ArrayList<TreeNode> {
    private Image icon = null;
    private Image expandedIcon = null;
    private String label = null;

    public TreeNode() {
        this(null, null, null);
    }

    public TreeNode(Image icon) {
        this(icon, null, null);
    }

    public TreeNode(String label) {
        this(null, null, label);
    }

    public TreeNode(Image icon, String label) {
        this(icon, null, label);
    }

    public TreeNode(Image icon, Image expandedIcon, String label) {
        this.icon = icon;
        this.expandedIcon = expandedIcon;
        this.label = label;
    }

    public Image getIcon() {
        return icon;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    public Image getExpandedIcon() {
        return (expandedIcon == null) ? icon : expandedIcon;
    }

    public void setExpandedIcon(Image expandedIcon) {
        this.expandedIcon = expandedIcon;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
