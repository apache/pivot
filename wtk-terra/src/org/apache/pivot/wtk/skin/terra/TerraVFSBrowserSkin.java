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
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.text.FileSizeFormat;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.concurrent.AbortException;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.ComponentTooltipListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.GridPane;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListButtonSelectionListener;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.ScrollPane;
import org.apache.pivot.wtk.SortDirection;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.StackPane;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.TableViewSortListener;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.apache.pivot.wtk.Tooltip;
import org.apache.pivot.wtk.VFSBrowser;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.VFSBrowserSkin;

/**
 * Terra Commons VFS browser skin.
 */
public class TerraVFSBrowserSkin extends VFSBrowserSkin {

    /**
     * Abstract renderer for displaying file system contents.
     */
    public static abstract class FileRenderer extends BoxPane {
        protected ImageView imageView = new ImageView();
        protected Label label = new Label();
        protected VFSBrowser fileBrowser = null;

        public static final int ICON_WIDTH = 16;
        public static final int ICON_HEIGHT = 16;

        public static final Image FOLDER_IMAGE;
        public static final Image HOME_FOLDER_IMAGE;
        public static final Image FILE_IMAGE;

        static {
            try {
                FOLDER_IMAGE = Image.load(FileRenderer.class.getResource("folder.png"));
                HOME_FOLDER_IMAGE = Image.load(FileRenderer.class.getResource("folder_home.png"));
                FILE_IMAGE = Image.load(FileRenderer.class.getResource("page_white.png"));
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

        @Override
        public void setSize(int width, int height) {
            super.setSize(width, height);

            // Since this component doesn't have a parent, it won't be validated
            // via layout; ensure that it is valid here
            validate();
        }

        protected void setFileBrowser(VFSBrowser fileBrowser) {
            this.fileBrowser = fileBrowser;
        }

        /**
         * @return The icon to display for a given file.
         *
         * @param file The current file.
         */
        public Image getIcon(FileObject file) {
            Image icon;
            if (file.getName().getType() == FileType.FOLDER) {
                icon = file.equals(fileBrowser.getHomeDirectory()) ? HOME_FOLDER_IMAGE
                    : FOLDER_IMAGE;
            } else {
                icon = FILE_IMAGE;
            }

            return icon;
        }

    public boolean isFileHidden(FileObject file) {
        try {
            boolean hidden = false;
            if (file != null) {
                if (file.getName().getBaseName().length() != 0 && file.isHidden())
                    hidden = true;
            }
            return hidden;
        }
        catch (FileSystemException fse) {
            throw new RuntimeException(fse);
        }
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
                FileObject file = (FileObject) data;
                boolean hidden = isFileHidden(file);

                // Update the image view
                imageView.setImage(getIcon(file));
                imageView.getStyles().put("opacity", button.isEnabled() && !hidden ? 1.0f : 0.5f);

                // Update the label
                String text = file.getName().getBaseName();
                if (text.length() == 0) {
                    text = FileName.ROOT_PATH;
                }

                label.setText(text);

                Object color = null;
                if (button.isEnabled() && !hidden) {
                    color = button.getStyles().get("color");
                } else {
                    color = button.getStyles().get("disabledColor");
                }

                label.getStyles().put("color", color);
            }
        }

        @Override
        public String toString(Object item) {
            FileObject file = (FileObject) item;
            String text = file.getName().getBaseName();
            if (text.length() == 0) {
                text = FileName.ROOT_PATH;
            }

            return text;
        }
    }

    /**
     * List view file renderer.
     */
    public static class ListViewFileRenderer extends FileRenderer implements ListView.ItemRenderer {
        public ListViewFileRenderer() {
            getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
            getStyles().put("padding", new Insets(2, 3, 2, 3));
        }

        @Override
        public void render(Object item, int index, ListView listView, boolean selected,
            Button.State state, boolean highlighted, boolean disabled) {
            boolean hidden = false;

            label.getStyles().put("font", listView.getStyles().get("font"));

            if (item != null) {
                FileObject file = (FileObject) item;
                hidden = isFileHidden(file);

                // Update the image view
                imageView.setImage(getIcon(file));
                imageView.getStyles().put("opacity",
                    (listView.isEnabled() && !disabled && !hidden) ? 1.0f : 0.5f);

                // Update the label
                String text = file.getName().getBaseName();
                if (text.length() == 0) {
                    text = FileName.ROOT_PATH;
                }

                label.setText(text);
            }

            Object color = null;
            if (listView.isEnabled() && !disabled && !hidden) {
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
        }

        @Override
        public String toString(Object item) {
            FileObject file = (FileObject) item;
            // TODO: should this be the full path or the base name?
            String text = file.getName().getBaseName();
            if (text.length() == 0) {
                text = FileName.ROOT_PATH;
            }

            return text;
        }
    }

    /**
     * Table view file renderer.
     */
    public static class TableViewFileRenderer extends FileRenderer implements
        TableView.CellRenderer {
        public static final String NAME_KEY = "name";
        public static final String SIZE_KEY = "size";
        public static final String LAST_MODIFIED_KEY = "lastModified";

        public TableViewFileRenderer() {
            getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
            getStyles().put("padding", new Insets(2));
        }

        @Override
        public void render(Object row, int rowIndex, int columnIndex, TableView tableView,
            String columnName, boolean selected, boolean highlighted, boolean disabled) {
            boolean hidden = false;

            if (row != null) {
                FileObject file = (FileObject) row;

                String text = null;
                Image icon = null;

                try {
                    FileType type = file.getType();
                    hidden = isFileHidden(file);

                    if (columnName.equals(NAME_KEY)) {
                        text = file.getName().getBaseName();
                        icon = getIcon(file);
                        getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
                    } else if (columnName.equals(SIZE_KEY)) {
                        if (type == FileType.FOLDER || type == FileType.IMAGINARY) {
                            text = "";
                        } else {
                            long length = file.getContent().getSize();
                            text = FileSizeFormat.getInstance().format(length);
                        }
                        getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);
                    } else if (columnName.equals(LAST_MODIFIED_KEY)) {
                        if (type == FileType.FOLDER || type == FileType.IMAGINARY) {
                            text = "";
                        } else {
                            long lastModified = file.getContent().getLastModifiedTime();
                            Date lastModifiedDate = new Date(lastModified);
                            text = DATE_FORMAT.format(lastModifiedDate);
                            getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);
                        }
                    } else {
                        System.err.println("Unexpected column name in " + getClass().getName()
                            + ": " + columnName);
                    }
                } catch (FileSystemException fse) {
                    // TODO: should we display an exception error here?
                    throw new RuntimeException(fse);
                }

                label.setText(text);
                imageView.setImage(icon);
                imageView.getStyles().put("opacity",
                    (tableView.isEnabled() && !disabled && !hidden) ? 1.0f : 0.5f);
            }

            Font font = (Font) tableView.getStyles().get("font");
            label.getStyles().put("font", font);

            Color color;
            if (tableView.isEnabled() && !disabled && !hidden) {
                if (selected) {
                    if (tableView.isFocused()) {
                        color = (Color) tableView.getStyles().get("selectionColor");
                    } else {
                        color = (Color) tableView.getStyles().get("inactiveSelectionColor");
                    }
                } else {
                    color = (Color) tableView.getStyles().get("color");
                }
            } else {
                color = (Color) tableView.getStyles().get("disabledColor");
            }

            label.getStyles().put("color", color);
        }

        @Override
        public String toString(Object row, String columnName) {
            String string;

            FileObject file = (FileObject) row;
            try {
                FileType type = file.getType();
                if (columnName.equals(NAME_KEY)) {
                    string = file.getName().getBaseName();
                } else if (columnName.equals(SIZE_KEY)) {
                    if (type == FileType.FOLDER || type == FileType.IMAGINARY) {
                        string = "";
                    } else {
                        long length = file.getContent().getSize();
                        string = FileSizeFormat.getInstance().format(length);
                    }
                } else if (columnName.equals(LAST_MODIFIED_KEY)) {
                    if (type == FileType.FOLDER || type == FileType.IMAGINARY) {
                        string = "";
                    } else {
                        long lastModified = file.getContent().getLastModifiedTime();
                        Date lastModifiedDate = new Date(lastModified);
                        string = DATE_FORMAT.format(lastModifiedDate);
                    }
                } else {
                    System.err.println("Unexpected column name in " + getClass().getName() + ": "
                        + columnName);
                    string = null;
                }
            } catch (FileSystemException fse) {
                throw new RuntimeException(fse);
            }

            return string;
        }
    }

    /**
     * Abstract base class for drive renderers.
     */
    public static abstract class DriveRenderer extends BoxPane {
        protected ImageView imageView = new ImageView();
        protected Label label = new Label();

        public static final int ICON_WIDTH = 16;
        public static final int ICON_HEIGHT = 16;

        public static final Image DRIVE_IMAGE;

        static {
            try {
                DRIVE_IMAGE = Image.load(DriveRenderer.class.getResource("drive.png"));
            } catch (TaskExecutionException exception) {
                throw new RuntimeException(exception);
            }
        }

        public DriveRenderer() {
            getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

            add(imageView);
            add(label);

            imageView.setPreferredSize(ICON_WIDTH, ICON_HEIGHT);
            imageView.getStyles().put("backgroundColor", null);
        }

        @Override
        public void setSize(int width, int height) {
            super.setSize(width, height);

            // Since this component doesn't have a parent, it won't be validated
            // via layout; ensure that it is valid here
            validate();
        }
    }

    /**
     * List button drive renderer.
     */
    public static class ListButtonDriveRenderer extends DriveRenderer implements
        Button.DataRenderer {
        public ListButtonDriveRenderer() {
            getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
        }

        @Override
        public void render(Object data, Button button, boolean highlight) {
            if (data != null) {
                FileObject file = (FileObject) data;

                // Update the image view
                imageView.setImage(DRIVE_IMAGE);
                imageView.getStyles().put("opacity", button.isEnabled() ? 1.0f : 0.5f);

                // Update the label
                label.setText(file.toString());
            }
        }

        @Override
        public String toString(Object data) {
            return null;
        }
    }

    /**
     * List view drive renderer.
     */
    public static class ListViewDriveRenderer extends DriveRenderer implements
        ListView.ItemRenderer {
        public ListViewDriveRenderer() {
            getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
            getStyles().put("padding", new Insets(2, 3, 2, 3));
        }

        @Override
        public void render(Object item, int index, ListView listView, boolean selected,
            Button.State state, boolean highlighted, boolean disabled) {
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
                FileObject file = (FileObject) item;

                // Update the image view
                imageView.setImage(DRIVE_IMAGE);
                imageView.getStyles().put("opacity",
                    (listView.isEnabled() && !disabled) ? 1.0f : 0.5f);

                // Update the label
                label.setText(file.toString());
            }
        }

        @Override
        public String toString(Object item) {
            return null;
        }
    }

    public static abstract class FileComparator implements Comparator<FileObject>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public abstract int compare(FileObject f1, FileObject f2);
    }

    public static class FileNameAscendingComparator extends FileComparator {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(FileObject f1, FileObject f2) {
            FileType f1Type = f1.getName().getType();
            FileType f2Type = f2.getName().getType();
            boolean file1IsFile = f1Type == FileType.FILE || f1Type == FileType.FILE_OR_FOLDER;
            boolean file2IsFile = f2Type == FileType.FILE || f2Type == FileType.FILE_OR_FOLDER;

            int result;
            if (!file1IsFile && file2IsFile) {
                result = -1;
            } else if (file1IsFile && !file2IsFile) {
                result = 1;
            } else {
                // Do the compare according to the rules of the file system
                result = f1.compareTo(f2);
            }
            return result;
        }
    }

    public static class FileNameDescendingComparator extends FileComparator {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(FileObject f1, FileObject f2) {
            FileType f1Type = f1.getName().getType();
            FileType f2Type = f2.getName().getType();
            boolean file1IsFile = f1Type == FileType.FILE || f1Type == FileType.FILE_OR_FOLDER;
            boolean file2IsFile = f2Type == FileType.FILE || f2Type == FileType.FILE_OR_FOLDER;

            int result;
            if (!file1IsFile && file2IsFile) {
                result = -1;
            } else if (file1IsFile && !file2IsFile) {
                result = 1;
            } else {
                // Do the compare according to the rules of the file system
                result = f2.compareTo(f1);
            }
            return result;
        }
    }

    public static class FileSizeAscendingComparator extends FileComparator {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(FileObject f1, FileObject f2) {
            try {
                FileType f1Type = f1.getName().getType();
                FileType f2Type = f2.getName().getType();
                boolean file1IsFile = f1Type == FileType.FILE || f1Type == FileType.FILE_OR_FOLDER;
                boolean file2IsFile = f2Type == FileType.FILE || f2Type == FileType.FILE_OR_FOLDER;
                long size1 = file1IsFile ? f1.getContent().getSize() : 0L;
                long size2 = file2IsFile ? f2.getContent().getSize() : 0L;
                return Long.signum(size1 - size2);
            } catch (FileSystemException fse) {
                throw new RuntimeException(fse);
            }
        }
    }

    public static class FileSizeDescendingComparator extends FileComparator {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(FileObject f1, FileObject f2) {
            try {
                FileType f1Type = f1.getName().getType();
                FileType f2Type = f2.getName().getType();
                boolean file1IsFile = f1Type == FileType.FILE || f1Type == FileType.FILE_OR_FOLDER;
                boolean file2IsFile = f2Type == FileType.FILE || f2Type == FileType.FILE_OR_FOLDER;
                long size1 = file1IsFile ? f1.getContent().getSize() : 0L;
                long size2 = file2IsFile ? f2.getContent().getSize() : 0L;
                return Long.signum(size2 - size1);
            } catch (FileSystemException fse) {
                throw new RuntimeException(fse);
            }
        }
    }

    public static class FileDateAscendingComparator extends FileComparator {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(FileObject f1, FileObject f2) {
            try {
                FileType f1Type = f1.getName().getType();
                FileType f2Type = f2.getName().getType();
                boolean file1IsFile = f1Type == FileType.FILE || f1Type == FileType.FILE_OR_FOLDER;
                boolean file2IsFile = f2Type == FileType.FILE || f2Type == FileType.FILE_OR_FOLDER;
                long time1 = file1IsFile ? f1.getContent().getLastModifiedTime() : 0L;
                long time2 = file2IsFile ? f2.getContent().getLastModifiedTime() : 0L;
                return Long.signum(time1 - time2);
            } catch (FileSystemException fse) {
                throw new RuntimeException(fse);
            }
        }
    }

    public static class FileDateDescendingComparator extends FileComparator {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(FileObject f1, FileObject f2) {
            try {
                FileType f1Type = f1.getName().getType();
                FileType f2Type = f2.getName().getType();
                boolean file1IsFile = f1Type == FileType.FILE || f1Type == FileType.FILE_OR_FOLDER;
                boolean file2IsFile = f2Type == FileType.FILE || f2Type == FileType.FILE_OR_FOLDER;
                long time1 = file1IsFile ? f1.getContent().getLastModifiedTime() : 0L;
                long time2 = file2IsFile ? f2.getContent().getLastModifiedTime() : 0L;
                return Long.signum(time2 - time1);
            } catch (FileSystemException fse) {
                throw new RuntimeException(fse);
            }
        }
    }

    /**
     * @return A new {@link FileObject} comparator for the given column and sort order.
     * @param columnName Name of the column to sort on.
     * @param sortDirection The sort order.
     */
    public static FileComparator getFileComparator(String columnName, SortDirection sortDirection) {
        if (columnName.equals("name")) {
            return sortDirection == SortDirection.ASCENDING ? new FileNameAscendingComparator()
                : new FileNameDescendingComparator();
        } else if (columnName.equals("size")) {
            return sortDirection == SortDirection.ASCENDING ? new FileSizeAscendingComparator()
                : new FileSizeDescendingComparator();
        } else if (columnName.equals("lastModified")) {
            return sortDirection == SortDirection.ASCENDING ? new FileDateAscendingComparator()
                : new FileDateDescendingComparator();
        } else {
            throw new IllegalArgumentException("Invalid column name for file comparator.");
        }
    }

    /**
     * Include file filter.
     */
    public static class IncludeFileFilter implements Filter<FileObject> {
        private String match;

        public IncludeFileFilter() {
            this(null);
        }

        public IncludeFileFilter(String match) {
            this.match = (match == null ? null : match.toLowerCase());
        }

        @Override
        public boolean include(FileObject file) {
            boolean include = true;

            if (match != null) {
                String name = file.getName().getBaseName();
                name = name.toLowerCase();

                if (match.startsWith("*")) {
                    if (match.length() == 1) {
                        include = true;
                    } else {
                        include = name.contains(match.substring(1));
                    }
                } else {
                    include = name.startsWith(match);
                }
            }

            return include;
        }
    }

    public static class FullFileSelector implements FileSelector {
        private FileFilter hiddenFileFilter;
        private Filter<FileObject> includeFileFilter;
        private Filter<FileObject> excludeFileFilter;

        public FullFileSelector(boolean showHiddenFiles, Filter<FileObject> includeFileFilter,
            Filter<FileObject> excludeFileFilter) {
            this.hiddenFileFilter = showHiddenFiles ? null : HIDDEN_FILE_FILTER;
            this.includeFileFilter = includeFileFilter;
            this.excludeFileFilter = excludeFileFilter;
        }

        @Override
        public boolean includeFile(FileSelectInfo fileInfo) {
            boolean include = hiddenFileFilter == null ? true : hiddenFileFilter.accept(fileInfo);
            if (include && includeFileFilter != null) {
                include = includeFileFilter.include(fileInfo.getFile());
            }
            if (include && excludeFileFilter != null) {
                include = !excludeFileFilter.include(fileInfo.getFile());
            }
            // Don't include the base folder itself
            if (include && fileInfo.getFile() == fileInfo.getBaseFolder()) {
                include = false;
            }
            return include;
        }

        @Override
        public boolean traverseDescendents(FileSelectInfo fileInfo) {
            // Only traverse the first-level descendents
            return fileInfo.getDepth() == 0;
        }
    }

    private class RefreshFileListTask extends Task<ArrayList<FileObject>> {
        private Filter<FileObject> includeFileFilter;
        private Filter<FileObject> excludeFileFilter;
        private FileComparator fileComparator;

        public RefreshFileListTask(Filter<FileObject> includeFileFilter,
            Filter<FileObject> excludeFileFilter, FileComparator fileComparator) {
            this.includeFileFilter = includeFileFilter;
            this.excludeFileFilter = excludeFileFilter;
            this.fileComparator = fileComparator;
        }

        @Override
        public ArrayList<FileObject> execute() {
            VFSBrowser fileBrowser = (VFSBrowser) getComponent();

            FileObject rootDirectory = fileBrowser.getRootDirectory();
            if (abort) {
                throw new AbortException();
            }

            try {
                FileObject[] files = rootDirectory.findFiles(new FullFileSelector(
                    showHiddenFiles, includeFileFilter, excludeFileFilter));
                if (abort) {
                    throw new AbortException();
                }

                Arrays.sort(files, fileComparator);

                return new ArrayList<>(files, 0, files.length);

            } catch (FileSystemException fse) {
                throw new RuntimeException(fse);
            }
        }
    }

    private Component content = null;

    @BXML
    private BoxPane pushButtonPane = null;
    @BXML
    private ListButton driveListButton = null;
    @BXML
    private ListButton pathListButton = null;
    @BXML
    private PushButton goUpButton = null;
    @BXML
    private PushButton newFolderButton = null;
    @BXML
    private PushButton goHomeButton = null;
    @BXML
    private TextInput searchTextInput = null;

    @BXML
    private StackPane fileStackPane = null;
    @BXML
    private ScrollPane fileScrollPane = null;
    @BXML
    private TableView fileTableView = null;

    private ActivityIndicator indicator = null;
    private GridPane activityGrid = null;

    private boolean keyboardFolderTraversalEnabled = true;
    private boolean hideDisabledFiles = false;
    private boolean showHiddenFiles = false;

    private boolean updatingSelection = false;
    private boolean refreshRoots = true;

    private RefreshFileListTask refreshFileListTask = null;

    private FileObject homeDirectory = null;

    private static final FileFilter HIDDEN_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(FileSelectInfo fileInfo) {
            try {
                return !fileInfo.getFile().isHidden();
            } catch (FileSystemException fse) {
                throw new RuntimeException(fse);
            }
        }
    };

    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT,
        DateFormat.SHORT);

    @Override
    public void install(Component component) {
        super.install(component);
        final VFSBrowser fileBrowser = (VFSBrowser) component;
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        try {
            content = (Component) bxmlSerializer.readObject(TerraVFSBrowserSkin.class,
                "terra_vfs_browser_skin.bxml", true);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }
        fileBrowser.add(content);

        bxmlSerializer.bind(this, TerraVFSBrowserSkin.class);

        // Notify all the renderers of which component they are dealing with
        ((FileRenderer)pathListButton.getDataRenderer()).setFileBrowser(fileBrowser);
        ((FileRenderer)pathListButton.getItemRenderer()).setFileBrowser(fileBrowser);
        for (TableView.Column col : fileTableView.getColumns()) {
            ((FileRenderer)col.getCellRenderer()).setFileBrowser(fileBrowser);
        }

        homeDirectory = fileBrowser.getHomeDirectory();

        driveListButton.getListButtonSelectionListeners().add(
            new ListButtonSelectionListener.Adapter() {
                @Override
                public void selectedItemChanged(ListButton listButton, Object previousSelectedItem) {
                    if (previousSelectedItem != null) {
                        FileObject drive = (FileObject) listButton.getSelectedItem();
                        try {
                            if (drive.isReadable()) {
                                fileBrowser.setRootDirectory(drive);
                            } else {
                                refreshRoots = true;
                                listButton.setSelectedItem(previousSelectedItem);
                            }
                        } catch (FileSystemException fse) {
                            throw new RuntimeException(fse);
                        }
                    }
                }
            });

        pathListButton.getListButtonSelectionListeners().add(
            new ListButtonSelectionListener.Adapter() {
                @Override
                public void selectedItemChanged(ListButton listButton, Object previousSelectedItem) {
                    FileObject ancestorDirectory = (FileObject) listButton.getSelectedItem();

                    if (ancestorDirectory != null) {
                        try {
                            fileBrowser.setRootDirectory(ancestorDirectory);
                        } catch (FileSystemException fse) {
                            throw new RuntimeException(fse);
                        }
                    }
                }
            });

        goUpButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                try {
                    FileObject rootDirectory = fileBrowser.getRootDirectory();
                    FileObject parentDirectory = rootDirectory.getParent();
                    fileBrowser.setRootDirectory(parentDirectory);
                } catch (FileSystemException fse) {
                    throw new RuntimeException(fse);
                }
            }
        });

        newFolderButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                // TODO
            }
        });

        goHomeButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                try {
                    fileBrowser.setRootDirectory(fileBrowser.getHomeDirectory());
                } catch (FileSystemException fse) {
                    throw new RuntimeException(fse);
                }
            }
        });

        /**
         * {@link KeyCode#DOWN DOWN} Transfer focus to the file list and select
         * the first item.<br> {@link KeyCode#ESCAPE ESCAPE} Clear the search
         * field.
         */
        searchTextInput.getComponentKeyListeners().add(new ComponentKeyListener.Adapter() {
            @Override
            public boolean keyPressed(Component componentArgument, int keyCode,
                Keyboard.KeyLocation keyLocation) {
                boolean consumed = super.keyPressed(componentArgument, keyCode, keyLocation);

                if (keyCode == Keyboard.KeyCode.ESCAPE) {
                    searchTextInput.setText("");
                    consumed = true;
                } else if (keyCode == Keyboard.KeyCode.DOWN) {
                    if (fileTableView.getTableData().getLength() > 0) {
                        fileTableView.setSelectedIndex(0);
                        fileTableView.requestFocus();
                    }
                }

                return consumed;
            }
        });

        searchTextInput.getTextInputContentListeners().add(new TextInputContentListener.Adapter() {
            @Override
            public void textChanged(TextInput textInput) {
                refreshFileList();
            }
        });

        fileTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener() {
            @Override
            public void selectedRangeAdded(TableView tableView, int rangeStart, int rangeEnd) {
                if (!updatingSelection) {
                    updatingSelection = true;

                    try {
                        for (int i = rangeStart; i <= rangeEnd; i++) {
                            @SuppressWarnings("unchecked")
                            List<FileObject> files = (List<FileObject>) fileTableView.getTableData();
                            FileObject file = files.get(i);
                            fileBrowser.addSelectedFile(file);
                        }
                    } catch (FileSystemException fse) {
                        throw new RuntimeException(fse);
                    }

                    updatingSelection = false;
                }
            }

            @Override
            public void selectedRangeRemoved(TableView tableView, int rangeStart, int rangeEnd) {
                if (!updatingSelection) {
                    updatingSelection = true;

                    for (int i = rangeStart; i <= rangeEnd; i++) {
                        @SuppressWarnings("unchecked")
                        List<FileObject> files = (List<FileObject>) fileTableView.getTableData();
                        FileObject file = files.get(i);
                        fileBrowser.removeSelectedFile(file);
                    }

                    updatingSelection = false;
                }
            }

            @Override
            public void selectedRangesChanged(TableView tableView,
                Sequence<Span> previousSelectedRanges) {
                if (!updatingSelection && previousSelectedRanges != null) {
                    updatingSelection = true;

                    @SuppressWarnings("unchecked")
                    Sequence<FileObject> files = (Sequence<FileObject>) tableView.getSelectedRows();
                    for (int i = 0, n = files.getLength(); i < n; i++) {
                        FileObject file = files.get(i);
                        files.update(i, file);
                    }

                    try {
                        fileBrowser.setSelectedFiles(files);
                    } catch (FileSystemException fse) {
                        throw new RuntimeException(fse);
                    }

                    updatingSelection = false;
                }
            }

            @Override
            public void selectedRowChanged(TableView tableView, Object previousSelectedRow) {
                // No-op
            }
        });

        fileTableView.getTableViewSortListeners().add(new TableViewSortListener.Adapter() {
            @Override
            public void sortChanged(TableView tableView) {
                TableView.SortDictionary sort = fileTableView.getSort();

                if (!sort.isEmpty()) {
                    Dictionary.Pair<String, SortDirection> pair = fileTableView.getSort().get(0);
                    @SuppressWarnings("unchecked")
                    List<FileObject> files = (List<FileObject>) fileTableView.getTableData();
                    files.setComparator(getFileComparator(pair.key, pair.value));
                }
            }
        });

        fileTableView.getComponentMouseButtonListeners().add(
            new ComponentMouseButtonListener.Adapter() {
                private int index = -1;

                @Override
                public boolean mouseClick(Component componentArgument, Mouse.Button button, int x,
                    int y, int count) {
                    boolean consumed = super.mouseClick(componentArgument, button, x, y, count);

                    if (count == 1) {
                        index = fileTableView.getRowAt(y);
                    } else if (count == 2) {
                        int indexLocal = fileTableView.getRowAt(y);
                        if (indexLocal != -1 && indexLocal == this.index
                            && fileTableView.isRowSelected(indexLocal)) {
                            FileObject file = (FileObject) fileTableView.getTableData().get(
                                indexLocal);

                            try {
                                if (file.getName().getType() == FileType.FOLDER) {
                                    fileBrowser.setRootDirectory(file);
                                    consumed = true;
                                }
                            } catch (FileSystemException fse) {
                                throw new RuntimeException(fse);
                            }
                        }
                    }

                    return consumed;
                }
            });

        fileBrowser.setFocusTraversalPolicy(new IndexFocusTraversalPolicy() {
            @Override
            public Component getNextComponent(Container container, Component componentArgument,
                FocusTraversalDirection direction) {
                Component nextComponent;
                if (componentArgument == null) {
                    nextComponent = fileTableView;
                } else {
                    nextComponent = super.getNextComponent(container, componentArgument, direction);
                }

                return nextComponent;
            }
        });

        fileTableView.setSort(TableViewFileRenderer.NAME_KEY, SortDirection.ASCENDING);
        fileTableView.getComponentTooltipListeners().add(new ComponentTooltipListener() {

            @Override
            public void tooltipTriggered(Component comp, int x, int y) {

                // Check that we are on the first column.
                if (fileTableView.getColumnAt(x) != 0) {
                    return;
                }

                // Gets the underlying file
                int row = fileTableView.getRowAt(y);
                if (row < 0) {
                    return;
                }
                FileObject file = (FileObject) fileTableView.getTableData().get(row);

                // Construct and show the tooltip.
                final Tooltip tooltip = new Tooltip();

                String text = null;

                if (file != null){
                    text = file.getName().getBaseName();
                }

                if (text == null || text.isEmpty()) {
                    return;
                }

                TextArea toolTipTextArea = new TextArea();

                toolTipTextArea.setText(text);
                toolTipTextArea.getStyles().put("wrapText", true);

                tooltip.setContent(toolTipTextArea);

                Point location = comp.getDisplay().getMouseLocation();
                x = location.x;
                y = location.y;

                // Ensure that the tooltip stays on screen
                Display display = comp.getDisplay();
                int tooltipHeight = tooltip.getPreferredHeight();
                if (y + tooltipHeight > display.getHeight()) {
                    y -= tooltipHeight;
                }

                int tooltipXOffset = 16;
                int padding = 15;

                toolTipTextArea.setMaximumWidth(display.getWidth() - ( x + tooltipXOffset + padding) );
                tooltip.setLocation(x + tooltipXOffset, y);
                tooltip.open(comp.getWindow());
            }
        });

        rootDirectoryChanged(fileBrowser, null);
        selectedFilesChanged(fileBrowser, null);
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

    @Override
    public FileObject getFileAt(int x, int y) {
        FileObject file = null;

        VFSBrowser fileBrowser = (VFSBrowser) getComponent();
        Component component = fileBrowser.getDescendantAt(x, y);
        if (component == fileTableView) {
            Point location = fileTableView.mapPointFromAncestor(fileBrowser, x, y);

            int index = fileTableView.getRowAt(location.y);
            if (index != -1) {
                file = (FileObject) fileTableView.getTableData().get(index);
            }
        }

        return file;
    }

    @Override
    public void addActionComponent(Component component) {
        pushButtonPane.add(component);
    }

    public boolean isKeyboardFolderTraversalEnabled() {
        return keyboardFolderTraversalEnabled;
    }

    public void setKeyboardFolderTraversalEnabled(boolean keyboardFolderTraversalEnabled) {
        this.keyboardFolderTraversalEnabled = keyboardFolderTraversalEnabled;
    }

    public boolean isHideDisabledFiles() {
        return hideDisabledFiles;
    }

    public void setHideDisabledFiles(boolean hideDisabledFiles) {
        this.hideDisabledFiles = hideDisabledFiles;
        refreshFileList();
    }

    /**
     * @return Whether hidden files will be shown in the browser.
     */
    public boolean isShowHiddenFiles() {
        return showHiddenFiles;
    }

    /**
     * Set to determine if hidden files should be shown.
     * @param showHiddenFiles Whether to show hidden files.
     */
    public void setShowHiddenFiles(boolean showHiddenFiles) {
        this.showHiddenFiles = showHiddenFiles;
        refreshFileList();
    }

    /**
     * {@link KeyCode#ENTER ENTER} Change into the selected directory if
     * {@link #keyboardFolderTraversalEnabled} is true.<br>
     * {@link KeyCode#DELETE DELETE} or {@link KeyCode#BACKSPACE BACKSPACE}
     * Change into the parent of the current directory.<br> {@link KeyCode#F5
     * F5} Refresh the file list.
     */
    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(component, keyCode, keyLocation);

        VFSBrowser fileBrowser = (VFSBrowser) getComponent();

        if (keyCode == Keyboard.KeyCode.ENTER && keyboardFolderTraversalEnabled) {
            Sequence<FileObject> selectedFiles = fileBrowser.getSelectedFiles();

            if (selectedFiles.getLength() == 1) {
                FileObject selectedFile = selectedFiles.get(0);
                try {
                    if (selectedFile.getName().getType() == FileType.FOLDER) {
                        fileBrowser.setRootDirectory(selectedFile);
                        consumed = true;
                    }
                } catch (FileSystemException fse) {
                    throw new RuntimeException(fse);
                }
            }
        } else if (keyCode == Keyboard.KeyCode.DELETE || keyCode == Keyboard.KeyCode.BACKSPACE) {
            FileObject rootDirectory = fileBrowser.getRootDirectory();
            try {
                FileObject parentDirectory = rootDirectory.getParent();
                if (parentDirectory != null) {
                    fileBrowser.setRootDirectory(parentDirectory);
                    consumed = true;
                }
            } catch (FileSystemException fse) {
                throw new RuntimeException(fse);
            }
        } else if (keyCode == Keyboard.KeyCode.F5) {
            refreshFileList();
            consumed = true;
        }

        return consumed;
    }

    /**
     * CommandModifier + {@link KeyCode#F F} Transfers focus to the search
     * TextInput.
     *
     * @see Platform#getCommandModifier()
     */
    @Override
    public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyReleased(component, keyCode, keyLocation);

        Keyboard.Modifier commandModifier = Platform.getCommandModifier();
        if (keyCode == Keyboard.KeyCode.F && Keyboard.isPressed(commandModifier)) {
            searchTextInput.requestFocus();
            consumed = true;
        }

        return consumed;
    }

    @Override
    public void managerChanged(VFSBrowser fileBrowser, FileSystemManager previousManager) {
        // TODO: Is there anything to do here? Surely, but what?
    }

    @Override
    public void rootDirectoryChanged(VFSBrowser fileBrowser, FileObject previousRootDirectory) {
        ArrayList<FileObject> path = new ArrayList<>();

        // FileSystemManager manager = fileBrowser.getManager();
        FileObject rootDirectory = fileBrowser.getRootDirectory();

        try {
            FileObject ancestorDirectory = rootDirectory.getParent();
            while (ancestorDirectory != null) {
                path.add(ancestorDirectory);
                ancestorDirectory = ancestorDirectory.getParent();
            }
        } catch (FileSystemException fse) {
            throw new RuntimeException(fse);
        }

        @SuppressWarnings("unchecked")
        ArrayList<FileObject> drives = (ArrayList<FileObject>) driveListButton.getListData();
        if (refreshRoots) {
            // TODO: this is ugly -- need to do much better at managing drive
            // list with VFS
            // There is an open question on the Dev list about adding
            // "getFileRoots()" to the VFS API.
            /*
             * try { FileObject[] roots = new FileObject[1]; roots[0] =
             * manager.resolveFile
             * (manager.getBaseFile().getName().getRoot().getURI()); drives =
             * new ArrayList<>(); for (int i = 0; i < roots.length; i++) {
             * FileObject root = roots[i]; if (root.exists()) {
             * drives.add(root); } } driveListButton.setListData(drives); }
             * catch (FileSystemException fse) { throw new
             * RuntimeException(fse); }
             */
            refreshRoots = false;
        }

        driveListButton.setVisible(drives.getLength() > 1);

        FileObject drive;
        if (path.getLength() == 0) {
            drive = rootDirectory;
        } else {
            drive = path.get(path.getLength() - 1);
        }

        driveListButton.setSelectedItem(drive);

        pathListButton.setListData(path);
        pathListButton.setButtonData(rootDirectory);
        pathListButton.setEnabled(rootDirectory.getName().getDepth() > 0);

        goUpButton.setEnabled(pathListButton.isEnabled());

        goHomeButton.setEnabled(!rootDirectory.equals(homeDirectory));

        fileScrollPane.setScrollTop(0);
        fileScrollPane.setScrollLeft(0);

        searchTextInput.setText("");

        fileTableView.requestFocus();
    }

    @Override
    public void homeDirectoryChanged(VFSBrowser fileBrowser, FileObject previousHomeDirectory) {
        this.homeDirectory = fileBrowser.getHomeDirectory();
        goHomeButton.setEnabled(!fileBrowser.getRootDirectory().equals(homeDirectory));
        // Refresh the list in order to redo the icons correctly
        refreshFileList();
    }

    @Override
    public void selectedFileAdded(VFSBrowser fileBrowser, FileObject file) {
        if (!updatingSelection) {
            @SuppressWarnings("unchecked")
            List<FileObject> files = (List<FileObject>) fileTableView.getTableData();
            int index = files.indexOf(file);
            if (index != -1) {
                updatingSelection = true;
                fileTableView.addSelectedIndex(index);
                updatingSelection = false;
            }
        }
    }

    @Override
    public void selectedFileRemoved(VFSBrowser fileBrowser, FileObject file) {
        if (!updatingSelection) {
            @SuppressWarnings("unchecked")
            List<FileObject> files = (List<FileObject>) fileTableView.getTableData();
            int index = files.indexOf(file);
            if (index != -1) {
                updatingSelection = true;
                fileTableView.removeSelectedIndex(index);
                updatingSelection = false;
            }
        }
    }

    @Override
    public void selectedFilesChanged(VFSBrowser fileBrowser,
        Sequence<FileObject> previousSelectedFiles) {
        updateSelectedFiles(fileBrowser);
    }

    private void updateSelectedFiles(VFSBrowser fileBrowser) {
        if (!updatingSelection) {
            Sequence<FileObject> selectedFiles = fileBrowser.getSelectedFiles();

            ArrayList<Span> selectedRanges = new ArrayList<>();
            for (int i = 0, n = selectedFiles.getLength(); i < n; i++) {
                FileObject selectedFile = selectedFiles.get(i);

                @SuppressWarnings("unchecked")
                List<FileObject> files = (List<FileObject>) fileTableView.getTableData();
                int index = files.indexOf(selectedFile);
                if (index != -1) {
                    selectedRanges.add(new Span(index, index));
                }
            }

            updatingSelection = true;
            fileTableView.setSelectedRanges(selectedRanges);
            updatingSelection = false;
        }
    }

    @Override
    public void multiSelectChanged(VFSBrowser fileBrowser) {
        fileTableView.setSelectMode(fileBrowser.isMultiSelect() ? TableView.SelectMode.MULTI
            : TableView.SelectMode.SINGLE);
    }

    @Override
    public void disabledFileFilterChanged(VFSBrowser fileBrowser,
        Filter<FileObject> previousDisabledFileFilter) {
        fileTableView.setDisabledRowFilter(fileBrowser.getDisabledFileFilter());
        refreshFileList();
    }

    private void refreshFileList() {
        // Cancel any outstanding task
        if (refreshFileListTask != null) {
            refreshFileListTask.abort();

            if (indicator != null) {
                indicator.setActive(false);
                fileStackPane.remove(fileStackPane.getLength() - 1, 1);
            }
        }

        if (indicator == null) {
            indicator = new ActivityIndicator();
            activityGrid = new GridPane(5);
            GridPane.Row row1 = new GridPane.Row(activityGrid);
            GridPane.Row row2 = new GridPane.Row(activityGrid);
            GridPane.Row row3 = new GridPane.Row(activityGrid);
            for (int i = 0; i < 5; i++) {
                row1.add(new GridPane.Filler());
                if (i == 2) {
                    row2.add(indicator);
                } else {
                    row2.add(new GridPane.Filler());
                }
                row3.add(new GridPane.Filler());
            }
        }
        fileStackPane.add(activityGrid);
        indicator.setActive(true);

        fileTableView.setTableData(new ArrayList<FileObject>());

        String text = searchTextInput.getText().trim();
        Filter<FileObject> disabledFileFilter = hideDisabledFiles ? ((VFSBrowser) getComponent()).getDisabledFileFilter()
            : null;
        Filter<FileObject> includeFileFilter = text.length() != 0 ? new IncludeFileFilter(text)
            : null;

        TableView.SortDictionary sort = fileTableView.getSort();

        final FileComparator fileComparator;
        if (sort.isEmpty()) {
            fileComparator = null;
        } else {
            Dictionary.Pair<String, SortDirection> pair = fileTableView.getSort().get(0);
            fileComparator = getFileComparator(pair.key, pair.value);
        }

        refreshFileListTask = new RefreshFileListTask(includeFileFilter, disabledFileFilter,
            fileComparator);
        refreshFileListTask.execute(new TaskAdapter<>(new TaskListener<ArrayList<FileObject>>() {
            @Override
            public void taskExecuted(Task<ArrayList<FileObject>> task) {
                if (task == refreshFileListTask) {
                    indicator.setActive(false);
                    fileStackPane.remove(fileStackPane.getLength() - 1, 1);

                    ArrayList<FileObject> fileList = task.getResult();
                    fileTableView.setTableData(fileList);

                    updateSelectedFiles((VFSBrowser) getComponent());

                    refreshFileListTask = null;
                }
            }

            @Override
            public void executeFailed(Task<ArrayList<FileObject>> task) {
                if (task == refreshFileListTask) {
                    indicator.setActive(false);
                    fileStackPane.remove(fileStackPane.getLength() - 1, 1);

                    refreshFileListTask = null;
                }
            }
        }));
    }

}
