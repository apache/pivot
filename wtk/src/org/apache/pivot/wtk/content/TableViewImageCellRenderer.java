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

import org.apache.pivot.json.JSON;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableView.CellRenderer;
import org.apache.pivot.wtk.media.Image;

/**
 * Default renderer for table view cells that contain image data.
 */
public class TableViewImageCellRenderer extends ImageView implements CellRenderer {
    public static int DEFAULT_HEIGHT = 16;

    public TableViewImageCellRenderer() {
        setPreferredHeight(DEFAULT_HEIGHT);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);

        // Since this component doesn't have a parent, it won't be validated
        // via layout; ensure that it is valid here
        validate();
    }

    @Override
    public void setPreferredHeight(int preferredHeight) {
        if (preferredHeight == -1) {
            throw new IllegalArgumentException("Preferred height must be a fixed value.");
        }

        super.setPreferredHeight(preferredHeight);
    }

    @Override
    public void setPreferredSize(int preferredWidth, int preferredHeight) {
        if (preferredHeight == -1) {
            throw new IllegalArgumentException("Preferred height must be a fixed value.");
        }

        super.setPreferredSize(preferredWidth, preferredHeight);
    }

    @Override
    public void render(Object row, int rowIndex, int columnIndex,
        TableView tableView, String columnName,
        boolean selected, boolean highlighted, boolean disabled) {
        if (row != null) {
            Image image = null;

            // Get the row and cell data
            if (columnName != null) {
                Object cellData = JSON.get(row, columnName);

                if (cellData == null
                    || cellData instanceof Image) {
                    image = (Image)cellData;
                } else {
                    System.err.println("Data for \"" + columnName + "\" is not an instance of "
                        + Image.class.getName());
                }
            }

            setImage(image);
        }
    }

    @Override
    public String toString(Object row, String columnName) {
        return null;
    }
}
