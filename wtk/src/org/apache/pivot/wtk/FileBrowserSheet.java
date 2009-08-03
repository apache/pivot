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
package org.apache.pivot.wtk;

import java.io.File;

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.immutable.ImmutableList;
import org.apache.pivot.io.FileList;
import org.apache.pivot.io.Folder;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.ListenerList;

/**
 * File browser sheet.
 *
 * @author gbrown
 */
public class FileBrowserSheet extends Sheet {
    /**
     * Enumeration defining supported modes.
     *
     * @author gbrown
     */
    public enum Mode {
        OPEN,
        OPEN_MULTIPLE,
        SAVE_AS,
        SAVE_TO
    }

    private static class FileBrowserSheetListenerList extends ListenerList<FileBrowserSheetListener>
        implements FileBrowserSheetListener {
        public void multiSelectChanged(FileBrowserSheet fileBrowserSheet) {
            for (FileBrowserSheetListener listener : this) {
                listener.multiSelectChanged(fileBrowserSheet);
            }
        }

        public void selectedFolderChanged(FileBrowserSheet fileBrowserSheet, Folder previousSelectedFolder) {
            for (FileBrowserSheetListener listener : this) {
                listener.selectedFolderChanged(fileBrowserSheet, previousSelectedFolder);
            }
        }

        public void selectedFileAdded(FileBrowserSheet fileBrowserSheet, File file) {
            for (FileBrowserSheetListener listener : this) {
                listener.selectedFileAdded(fileBrowserSheet, file);
            }
        }

        public void selectedFileRemoved(FileBrowserSheet fileBrowserSheet, File file) {
            for (FileBrowserSheetListener listener : this) {
                listener.selectedFileRemoved(fileBrowserSheet, file);
            }
        }

        public void selectedFilesChanged(FileBrowserSheet fileBrowserSheet, Sequence<File> previousSelectedFiles) {
            for (FileBrowserSheetListener listener : this) {
                listener.selectedFilesChanged(fileBrowserSheet, previousSelectedFiles);
            }
        }

        public void fileFilterChanged(FileBrowserSheet fileBrowserSheet, Filter<File> previousFileFilter) {
            for (FileBrowserSheetListener listener : this) {
                listener.fileFilterChanged(fileBrowserSheet, previousFileFilter);
            }
        }
    }

    private Mode mode;
    private Folder selectedFolder;
    private FileList fileSelection = new FileList();
    private boolean multiSelect = false;
    private Filter<File> fileFilter = null;

    private FileBrowserSheetListenerList fileBrowserSheetListeners = new FileBrowserSheetListenerList();

    public FileBrowserSheet(Mode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("mode is null.");
        }

        this.mode = mode;

        String userHome = System.getProperty("user.home");
        selectedFolder = new Folder(userHome);

        installSkin(FileBrowserSheet.class);
    }

    public Mode getMode() {
        return mode;
    }

    public Folder getSelectedFolder() {
        return selectedFolder;
    }

    public void setSelectedFolder(Folder selectedFolder) {
        if (selectedFolder == null) {
            throw new IllegalArgumentException("selectedFolder is null.");
        }

        Folder previousSelectedFolder = this.selectedFolder;
        if (previousSelectedFolder != selectedFolder) {
            this.selectedFolder = selectedFolder;
            fileBrowserSheetListeners.selectedFolderChanged(this, previousSelectedFolder);
        }
    }

    public boolean addSelectedFile(File file) {
        int index = fileSelection.add(file);
        if (index != -1) {
            fileBrowserSheetListeners.selectedFileAdded(this, file);
        }

        return (index != -1);
    }

    public boolean removeSelectedFile(File file) {
        int index = fileSelection.remove(file);
        if (index != -1) {
            fileBrowserSheetListeners.selectedFileRemoved(this, file);
        }

        return (index != -1);
    }

    public Sequence<File> getSelectedFiles() {
        return new ImmutableList<File>(fileSelection);
    }

    public Sequence<File> setSelectedFiles(Sequence<File> selectedFiles) {
        if (selectedFiles == null) {
            throw new IllegalArgumentException("selectedFiles is null.");
        }

        if (!multiSelect
            && selectedFiles.getLength() > 1) {
            throw new IllegalArgumentException("Multi-select is not enabled.");
        }

        // Update the selection
        Sequence<File> previousSelectedFiles = getSelectedFiles();

        FileList fileSelection = new FileList();
        for (int i = 0, n = fileSelection.getLength(); i < n; i++) {
            File file = fileSelection.get(i);

            if (file == null) {
                throw new IllegalArgumentException("file is null.");
            }

            fileSelection.add(file);
        }

        this.fileSelection = fileSelection;

        // Notify listeners
        fileBrowserSheetListeners.selectedFilesChanged(this, previousSelectedFiles);

        return getSelectedFiles();
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(boolean multiSelect) {
        if (this.multiSelect != multiSelect) {
            this.multiSelect = multiSelect;
            fileBrowserSheetListeners.multiSelectChanged(this);
        }
    }

    public Filter<File> getFileFilter() {
        return fileFilter;
    }

    public void setFileFilter(Filter<File> fileFilter) {
        Filter<File> previousFileFilter = this.fileFilter;

        if (previousFileFilter != fileFilter) {
            this.fileFilter = fileFilter;
            fileBrowserSheetListeners.fileFilterChanged(this, previousFileFilter);
        }
    }

    public ListenerList<FileBrowserSheetListener> getFileBrowserSheetListeners() {
        return fileBrowserSheetListeners;
    }
}
