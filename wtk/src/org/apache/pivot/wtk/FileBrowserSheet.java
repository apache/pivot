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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.immutable.ImmutableList;
import org.apache.pivot.io.FileList;
import org.apache.pivot.io.Folder;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.ListenerList;

/**
 * File browser sheet.
 */
public class FileBrowserSheet extends Sheet {
    /**
     * Enumeration defining supported modes.
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

        public void selectedFilesChanged(FileBrowserSheet fileBrowserSheet, Sequence<File> previousSelectedFiles) {
            for (FileBrowserSheetListener listener : this) {
                listener.selectedFilesChanged(fileBrowserSheet, previousSelectedFiles);
            }
        }

        public void disabledFileFilterChanged(FileBrowserSheet fileBrowserSheet,
            Filter<File> previousDisabledFileFilter) {
            for (FileBrowserSheetListener listener : this) {
                listener.disabledFileFilterChanged(fileBrowserSheet, previousDisabledFileFilter);
            }
        }
    }

    private Mode mode;
    private Folder selectedFolder;
    private FileList fileSelection = new FileList();
    private boolean multiSelect = false;
    private Filter<File> disabledFileFilter = null;

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

    /**
     * When in single-select mode, returns the currently selected file.
     *
     * @return
     * The currently selected file.
     */
    public File getSelectedFile() {
        if (multiSelect) {
            throw new IllegalStateException("File browser is not in single-select mode.");
        }

        return (fileSelection.getLength() == 0) ? null : fileSelection.get(0);
    }

    /**
     * Sets the selection to a single file.
     *
     * @param file
     */
    public void setSelectedFile(File file) {
        if (file == null) {
            clearSelection();
        } else {
            setSelectedFiles(new ArrayList<File>(file));
        }
    }

    /**
     * Returns the currently selected files.
     *
     * @return
     * An immutable list of selected files. The file paths are relative to
     * the currently selected folder.
     */
    public Sequence<File> getSelectedFiles() {
        return new ImmutableList<File>(fileSelection);
    }

    /**
     * Sets the selected files.
     *
     * @param selectedFiles
     * The files to select. The file paths are relative to the currently
     * selected folder.
     *
     * @return
     * The files that were selected, with duplicates eliminated.
     */
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
        for (int i = 0, n = selectedFiles.getLength(); i < n; i++) {
            File file = selectedFiles.get(i);

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

    /**
     * Clears the selection.
     */
    public void clearSelection() {
        setSelectedFiles(new ArrayList<File>());
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(boolean multiSelect) {
        if (this.multiSelect != multiSelect) {
            // Clear any existing selection
            fileSelection.clear();

            this.multiSelect = multiSelect;

            fileBrowserSheetListeners.multiSelectChanged(this);
        }
    }

    public Filter<File> getDisabledFileFilter() {
        return disabledFileFilter;
    }

    public void setDisabledFileFilter(Filter<File> disabledFileFilter) {
        Filter<File> previousDisabledFileFilter = this.disabledFileFilter;

        if (previousDisabledFileFilter != disabledFileFilter) {
            this.disabledFileFilter = disabledFileFilter;
            fileBrowserSheetListeners.disabledFileFilterChanged(this, previousDisabledFileFilter);
        }
    }

    public ListenerList<FileBrowserSheetListener> getFileBrowserSheetListeners() {
        return fileBrowserSheetListeners;
    }
}
