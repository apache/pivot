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
package org.apache.pivot.wtk.skin.terra;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.io.Folder;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.text.FileSizeFormat;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.FileBrowser;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListButtonSelectionListener;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.ScrollPane;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.FileBrowserSkin;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;

/**
 * Terra file browser skin.
 */
public class TerraFileBrowserSkin extends FileBrowserSkin {
    /**
     * Abstract renderer for displaying file system contents.
     */
    public static abstract class FileRenderer extends BoxPane {
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
                FOLDER_IMAGE = Image.load(FileRenderer.class.getResource("folder.png"));
                HOME_FOLDER_IMAGE = Image.load(FileRenderer.class.getResource("folder_home.png"));
                FILE_IMAGE = Image.load(FileRenderer.class.getResource("page_white.png"));

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
    }

    /**
     * List button file renderer.
     */
    public static class ListButtonFileRenderer extends FileRenderer implements Button.DataRenderer {
        public ListButtonFileRenderer() {
            getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
        }

        @Override
        public void render(Object data, Button button, boolean highlight) {
            if (data != null) {
                File file = (File)data;

                // Update the image view
                imageView.setImage(getIcon(file));
                imageView.getStyles().put("opacity", button.isEnabled() ? 1.0f : 0.5f);

                // Update the label
                String text = file.getName();
                if (text.length() == 0) {
                    text = System.getProperty("file.separator");
                }

                label.setText(text);
            }
        }
    }

    /**
     * List view renderer for displaying file system contents.
     */
    public static class ListViewFileRenderer extends FileRenderer implements ListView.ItemRenderer {
        public ListViewFileRenderer() {
            getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
            getStyles().put("padding", new Insets(2, 3, 2, 3));
        }

        public void render(Object item, ListView listView, boolean selected,
            boolean checked, boolean highlighted, boolean disabled) {
            label.getStyles().put("font", listView.getStyles().get("font"));

            Object color = null;
            if (listView.isEnabled() && !disabled) {
                if (selected) {
                    if (listView.isFocused()) {
                        color = listView.getStyles().get("selectionColor");
                    } else {
                        color = listView.getStyles().get("inactiveSelectionColor");
                    }
                } else {
                    color = listView.getStyles().get("color");
                }
            } else {
                color = listView.getStyles().get("disabledColor");
            }

            label.getStyles().put("color", color);

            if (item != null) {
                File file = (File)item;

                // Update the image view
                imageView.setImage(getIcon(file));
                imageView.getStyles().put("opacity",
                    (listView.isEnabled() && !disabled) ? 1.0f : 0.5f);

                // Update the label
                String text = file.getName();
                if (text.length() == 0) {
                    text = System.getProperty("file.separator");
                }

                label.setText(text);
            }
        }
    }

    public static class TableViewFileRenderer extends FileRenderer implements TableView.CellRenderer {
        public static final String NAME_KEY = "name";
        public static final String SIZE_KEY = "size";
        public static final String LAST_MODIFIED_KEY = "lastModified";

        public TableViewFileRenderer() {
            getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
            getStyles().put("padding", new Insets(2));
        }

        public void render(Object value, TableView tableView, TableView.Column column,
            boolean rowSelected, boolean rowHighlighted, boolean rowDisabled) {
            String columnName = column.getName();

            if (value != null) {
                File file = (File)value;

                String text = null;
                Image icon = null;

                if (columnName.equals(NAME_KEY)) {
                    text = file.getName();
                    icon = getIcon(file);
                    getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
                } else if (columnName.equals(SIZE_KEY)) {
                    long length = file.length();
                    text = FileSizeFormat.getInstance().format(length);
                    getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);
                } else if (columnName.equals(LAST_MODIFIED_KEY)) {
                    long lastModified = file.lastModified();
                    Date lastModifiedDate = new Date(lastModified);

                    DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                    text = dateFormat.format(lastModifiedDate);
                    getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);
                } else {
                    System.err.println("Unexpected column name in " + getClass().getName()
                        + ": " + columnName);
                }

                label.setText(text);
                imageView.setImage(icon);
            }

            Font font = (Font)tableView.getStyles().get("font");
            label.getStyles().put("font", font);

            Color color;
            if (tableView.isEnabled() && !rowDisabled) {
                if (rowSelected) {
                    if (tableView.isFocused()) {
                        color = (Color)tableView.getStyles().get("selectionColor");
                    } else {
                        color = (Color)tableView.getStyles().get("inactiveSelectionColor");
                    }
                } else {
                    color = (Color)tableView.getStyles().get("color");
                }
            } else {
                color = (Color)tableView.getStyles().get("disabledColor");
            }

            label.getStyles().put("color", color);
        }
    }

    private Component content = null;

    @WTKX private ListButton pathListButton = null;
    @WTKX private PushButton goUpButton = null;
    @WTKX private PushButton newFolderButton = null;
    @WTKX private PushButton goHomeButton = null;

    @WTKX private ScrollPane fileScrollPane = null;
    @WTKX private TableView fileTableView = null;

    private boolean updatingSelection = false;

    @Override
    public void install(Component component) {
        super.install(component);

        final FileBrowser fileBrowser = (FileBrowser)component;

        Resources resources;
        try {
            resources = new Resources(this);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        WTKXSerializer wtkxSerializer = new WTKXSerializer(resources);
        try {
            content = (Component)wtkxSerializer.readObject(this, "terra_file_browser_skin.wtkx");
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        fileBrowser.add(content);

        wtkxSerializer.bind(this, TerraFileBrowserSkin.class);

        pathListButton.getListButtonSelectionListeners().add(new ListButtonSelectionListener() {
            public void selectedIndexChanged(ListButton listButton, int previousSelectedIndex) {
                File directory = (File)listButton.getSelectedItem();

                if (directory != null) {
                    fileBrowser.setSelectedFolder(new Folder(directory.getPath()));
                }
            }
        });

        goUpButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Folder selectedFolder = fileBrowser.getSelectedFolder();
                File parentDirectory = selectedFolder.getParentFile();
                fileBrowser.setSelectedFolder(new Folder(parentDirectory.getPath()));
            }
        });

        newFolderButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                // TODO
            }
        });

        goHomeButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                fileBrowser.setSelectedFolder(new Folder(System.getProperty("user.home")));
            }
        });

        fileTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener() {
            public void selectedRangeAdded(TableView tableView, int rangeStart, int rangeEnd) {
                updatingSelection = true;

                Folder selectedFolder = (Folder)tableView.getTableData();
                for (int i = rangeStart; i <= rangeEnd; i++) {
                    File file = selectedFolder.get(i);
                    file = getRelativeFile(selectedFolder, file);

                    fileBrowser.addSelectedFile(file);
                }

                updatingSelection = false;
            }

            public void selectedRangeRemoved(TableView tableView, int rangeStart, int rangeEnd) {
                updatingSelection = true;

                Folder selectedFolder = (Folder)tableView.getTableData();
                for (int i = rangeStart; i <= rangeEnd; i++) {
                    File file = selectedFolder.get(i);
                    file = getRelativeFile(selectedFolder, file);

                    fileBrowser.removeSelectedFile(file);
                }

                updatingSelection = false;
            }

            @SuppressWarnings("unchecked")
            public void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelectedRanges) {
                updatingSelection = true;

                Folder selectedFolder = (Folder)tableView.getTableData();

                Sequence<File> files = (Sequence<File>)tableView.getSelectedRows();
                for (int i = 0, n = files.getLength(); i < n; i++) {
                    File file = files.get(i);
                    files.update(i, getRelativeFile(selectedFolder, file));
                }

                fileBrowser.setSelectedFiles(files);

                updatingSelection = false;
            }
        });

        fileTableView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter() {
            private int index = -1;

            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
                if (count == 1) {
                    index = fileTableView.getRowAt(y);
                } else if (count == 2) {
                    int index = fileTableView.getRowAt(y);
                    if (index != -1
                        && index == this.index
                        && fileTableView.isRowSelected(index)) {
                        File selectedDirectory = (File)fileTableView.getTableData().get(index);
                        fileBrowser.setSelectedFolder(new Folder(selectedDirectory.getPath()));
                    }
                }

                return false;
            }
        });

        selectedFolderChanged(fileBrowser, null);
        selectedFilesChanged(fileBrowser, null);
    }

    @Override
    public void uninstall() {
        FileBrowser fileBrowser = (FileBrowser)getComponent();
        fileBrowser.remove(content);

        content = null;

        super.uninstall();
    }

    @Override
    public int getPreferredWidth(int height) {
        return content.getPreferredWidth(height);
    }

    @Override
    public int getPreferredHeight(int width) {
        return content.getPreferredHeight(width);
    }

    @Override
    public Dimensions getPreferredSize() {
        return content.getPreferredSize();
    }

    @Override
    public void layout() {
        int width = getWidth();
        int height = getHeight();

        content.setLocation(0, 0);
        content.setSize(width, height);
    }

    public void multiSelectChanged(FileBrowser fileBrowser) {
        fileTableView.setSelectMode(fileBrowser.isMultiSelect() ?
            TableView.SelectMode.MULTI : TableView.SelectMode.SINGLE);
    }

    public void selectedFolderChanged(FileBrowser fileBrowser, Folder previousSelectedFolder) {
        ArrayList<File> path = new ArrayList<File>();

        Folder selectedFolder = fileBrowser.getSelectedFolder();

        File directory = selectedFolder.getParentFile();
        while (directory != null) {
            path.add(directory);
            directory = directory.getParentFile();
        }

        pathListButton.setListData(path);
        pathListButton.setButtonData(selectedFolder);

        goUpButton.setEnabled(selectedFolder.getParentFile() != null);

        File homeDirectory = new File(System.getProperty("user.home"));
        goHomeButton.setEnabled(!selectedFolder.equals(homeDirectory));

        fileScrollPane.setScrollTop(0);
        fileScrollPane.setScrollLeft(0);
        fileTableView.setTableData(selectedFolder);

        fileTableView.requestFocus();
    }

    @SuppressWarnings("unchecked")    
    public void selectedFileAdded(FileBrowser fileBrowser, File file) {
        if (!updatingSelection) {
            // TODO This won't work because the table contains absolute files, while the selection
            // does not
            List<File> fileTableData = (List<File>)fileTableView.getTableData();
            int index = fileTableData.indexOf(file);
            if (index != -1) {
                fileTableView.addSelectedIndex(index);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void selectedFileRemoved(FileBrowser fileBrowser, File file) {
        if (!updatingSelection) {
            // TODO This won't work because the table contains absolute files, while the selection
            // does not
            List<File> fileTableData = (List<File>)fileTableView.getTableData();
            int index = fileTableData.indexOf(file);
            if (index != -1) {
                fileTableView.removeSelectedIndex(index);
            }
        }
    }

    public void selectedFilesChanged(FileBrowser fileBrowser, Sequence<File> previousSelectedFiles) {
        if (!updatingSelection) {
            // TODO Reset the selection
        }
    }

    public void disabledFileFilterChanged(FileBrowser fileBrowser, Filter<File> previousDisabledFileFilter) {
        fileTableView.setDisabledRowFilter(fileBrowser.getDisabledFileFilter());
    }

    public static File getRelativeFile(Folder folder, File file) {
        String path = file.getPath();
        path = path.substring(folder.getPath().length() + 1);

        return new File(path);
    }
}
