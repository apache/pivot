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
import java.awt.Font;
import java.io.File;

import org.apache.pivot.io.Folder;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.FlowPane;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.media.Image;


/**
 * Tree view renderer for displaying file system contents.
 *
 * @author gbrown
 */
public class TreeViewFileRenderer extends FlowPane implements TreeView.NodeRenderer {
    private ImageView imageView = new ImageView();
    private Label label = new Label();

    public static final int ICON_WIDTH = 16;
    public static final int ICON_HEIGHT = 16;

    public static final Image FOLDER_IMAGE;
    public static final Image FILE_IMAGE;

    static {
        try {
            FOLDER_IMAGE = Image.load(TreeViewFileRenderer.class.getResource("folder.png"));
            FILE_IMAGE = Image.load(TreeViewFileRenderer.class.getResource("page_white.png"));
        } catch (TaskExecutionException exception) {
            throw new RuntimeException(exception);
        }
    }

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

    public void render(Object node, TreeView treeView, boolean expanded,
        boolean selected, TreeView.NodeCheckState checkState,
        boolean highlighted, boolean disabled) {
        // Update styles
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

        if (node != null) {
            File file = (File)node;

            // Update the image view
            Image icon = (file instanceof Folder) ? FOLDER_IMAGE : FILE_IMAGE;

            imageView.setImage(icon);
            imageView.getStyles().put("opacity",
                (treeView.isEnabled() && !disabled) ? 1.0f : 0.5f);

            // Update the label
            label.setText(file.getName());
        }
    }
}
