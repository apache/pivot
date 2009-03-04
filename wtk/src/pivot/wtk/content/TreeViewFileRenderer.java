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
package pivot.wtk.content;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;

import pivot.io.Folder;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.ImageView;
import pivot.wtk.Label;
import pivot.wtk.TreeView;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.media.Image;
import pivot.wtk.media.Picture;
import sun.awt.shell.ShellFolder;

/**
 * Tree view renderer for displaying file system contents.
 *
 * @author gbrown
 */
public class TreeViewFileRenderer extends FlowPane implements TreeView.NodeRenderer {
    protected ImageView imageView = new ImageView();
    protected Label label = new Label();

    public static final int ICON_WIDTH = 16;
    public static final int ICON_HEIGHT = 16;

    private static final Image folderImage =
        Image.load(TreeViewFileRenderer.class.getResource("folder.png"));
    private static final Image defaultFileImage =
        Image.load(TreeViewFileRenderer.class.getResource("page_white.png"));

    public TreeViewFileRenderer() {
        super();

        getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
        getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

        add(imageView);
        add(label);

        imageView.setPreferredSize(ICON_WIDTH, ICON_HEIGHT);
    }

    public void setSize(int width, int height) {
        super.setSize(width, height);

        // Since this component doesn't have a parent, it won't be validated
        // via layout; ensure that it is valid here
        validate();
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = super.getPreferredHeight(width);
        return preferredHeight;
    }

    @SuppressWarnings("unchecked")
    public void render(Object node, TreeView treeView, boolean expanded,
        boolean selected, boolean highlighted, boolean disabled) {
        File file = (File)node;

        // Update the image view
        Image icon = null;
        if (file instanceof Folder) {
            icon = folderImage;
        } else {
            try {
                ShellFolder shellFolder = ShellFolder.getShellFolder(file);
                java.awt.Image image = shellFolder.getIcon(false);

                if (image instanceof BufferedImage) {
                    icon = new Picture((BufferedImage)image);
                } else {
                    icon = defaultFileImage;
                }
            } catch(FileNotFoundException exception) {
                icon = defaultFileImage;
            }
        }

        imageView.setImage(icon);
        imageView.getStyles().put("opacity",
            (treeView.isEnabled() && !disabled) ? 1.0f : 0.5f);

        // Update the label
        label.setText(file.getName());

        Object labelFont = treeView.getStyles().get("font");
        if (labelFont instanceof Font) {
            label.getStyles().put("font", labelFont);
        }

        Object color = null;
        if (treeView.isEnabled() && !disabled) {
            if (selected) {
                if (treeView.isFocused()) {
                    color = treeView.getStyles().get("selectionColor");
                } else {
                    color = treeView.getStyles().get("inactiveSelectionColor");
                }
            } else {
                color = treeView.getStyles().get("color");
            }
        } else {
            color = treeView.getStyles().get("disabledColor");
        }

        if (color instanceof Color) {
            label.getStyles().put("color", color);
        }
    }
}
