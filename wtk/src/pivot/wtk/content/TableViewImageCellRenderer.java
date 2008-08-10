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

import java.net.URL;
import pivot.collections.Dictionary;
import pivot.wtk.ApplicationContext;
import pivot.wtk.ImageView;
import pivot.wtk.TableView;
import pivot.wtk.TableView.CellRenderer;
import pivot.wtk.media.Image;

public class TableViewImageCellRenderer extends ImageView implements CellRenderer {
    public static int DEFAULT_HEIGHT = 16;

    public TableViewImageCellRenderer() {
        super();

        setPreferredHeight(DEFAULT_HEIGHT);
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

    @SuppressWarnings("unchecked")
    public void render(Object value, TableView tableView, TableView.Column column,
        boolean rowSelected, boolean rowHighlighted, boolean rowDisabled) {
        Image image = null;

        // Get the row and cell data
        String columnName = column.getName();
        if (columnName != null) {
            Dictionary<String, Object> rowData = (Dictionary<String, Object>)value;
            Object cellData = rowData.get(columnName);

            if (cellData instanceof URL) {
                ApplicationContext applicationContext = ApplicationContext.getInstance();
                URL imageURL = (URL)cellData;
                image = (Image)applicationContext.getResources().get(imageURL);

                if (image == null) {
                    image = Image.load(imageURL);
                    applicationContext.getResources().put(imageURL, image);
                }
            }

            if (cellData instanceof Image) {
                image = (Image)cellData;
            }
        }

        setImage(image);
    }
}
