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
import java.text.NumberFormat;

import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.BoxPane;
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

    public static final int KILOBYTE = 1024;
    public static final String[] ABBREVIATIONS = {"K", "M", "G", "T", "P", "E", "Z", "Y"};

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
        getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

        add(imageView);
        add(label);

        imageView.setPreferredSize(ICON_WIDTH, ICON_HEIGHT);
        imageView.getStyles().put("backgroundColor", null);
    }

    public void setSize(int width, int height) {
        super.setSize(width, height);

        // Since this component doesn't have a parent, it won't be validated
        // via layout; ensure that it is valid here
        validate();
    }

    /**
     * Obtains the icon to display for a given file.
     *
     * @param file
     */
    public static Image getIcon(File file) {
        Image icon;
        if (file.isDirectory()) {
            icon = file.equals(HOME_DIRECTORY) ? HOME_FOLDER_IMAGE : FOLDER_IMAGE;
        } else {
            icon = FILE_IMAGE;
        }

        return icon;
    }

    /**
     * Converts a file size into a human-readable representation using binary
     * prefixes (1KB = 1024 bytes).
     *
     * @param length
     * The length of the file, in bytes. May be <tt>-1</tt> to indicate an
     * unknown file size.
     *
     * @return
     * The formatted file size, or null if <tt>length</tt> is <tt>-1</tt>.
     */
    public static String formatSize(File file) {
        String formattedSize;

        long length = file.length();
        if (length == -1) {
            formattedSize = null;
        } else {
            double size = length;

            int i = -1;
            do {
                size /= KILOBYTE;
                i++;
            } while (size > KILOBYTE);

            NumberFormat numberFormat = NumberFormat.getNumberInstance();
            if (i == 0
                && size > 1) {
                numberFormat.setMaximumFractionDigits(0);
            } else {
                numberFormat.setMaximumFractionDigits(1);
            }

            formattedSize = numberFormat.format(size) + " " + ABBREVIATIONS[i] + "B";
        }

        return formattedSize;
    }
}
