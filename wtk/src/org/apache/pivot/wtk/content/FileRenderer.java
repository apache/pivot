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

import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.media.Image;

/**
 * Abstract renderer for displaying file system contents.
 *
 * @author gbrown
 */
public abstract class FileRenderer extends BoxPane {
    protected ImageView imageView = new ImageView();
    protected Label label = new Label();

    public static final int ICON_WIDTH = 16;
    public static final int ICON_HEIGHT = 16;

    public static final Image FOLDER_IMAGE;
    public static final Image HOME_FOLDER_IMAGE;
    public static final Image FILE_IMAGE;

    public static final File HOME_DIRECTORY;

    static {
        try {
            FOLDER_IMAGE = Image.load(TreeViewFileRenderer.class.getResource("folder.png"));
            HOME_FOLDER_IMAGE = Image.load(TreeViewFileRenderer.class.getResource("folder_home.png"));
            FILE_IMAGE = Image.load(TreeViewFileRenderer.class.getResource("page_white.png"));

            HOME_DIRECTORY = new File(System.getProperty("user.home"));
        } catch (TaskExecutionException exception) {
            throw new RuntimeException(exception);
        }
    }

    public FileRenderer() {
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

    protected void render(File file, Component component, boolean disabled) {
        // Update the image view
        Image icon;
        if (file.isDirectory()) {
            icon = file.equals(HOME_DIRECTORY) ? HOME_FOLDER_IMAGE : FOLDER_IMAGE;
        } else {
            icon = FILE_IMAGE;
        }

        imageView.setImage(icon);
        imageView.getStyles().put("opacity",
            (component.isEnabled() && !disabled) ? 1.0f : 0.5f);

        // Update the label
        String text = file.getName();
        if (text.length() == 0) {
            text = System.getProperty("file.separator");
        }

        label.setText(text);
    }
}
