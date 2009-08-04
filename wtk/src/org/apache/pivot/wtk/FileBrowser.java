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
 * Component representing a file browser.
 *
 * @author gbrown
 */
public class FileBrowser extends Container {
    private static class FileBrowserListenerList extends ListenerList<FileBrowserListener>
        implements FileBrowserListener {
        public void multiSelectChanged(FileBrowser fileBrowser) {
            for (FileBrowserListener listener : this) {
                listener.multiSelectChanged(fileBrowser);
            }
        }

        public void selectedFolderChanged(FileBrowser fileBrowser, Folder previousSelectedFolder) {
            for (FileBrowserListener listener : this) {
                listener.selectedFolderChanged(fileBrowser, previousSelectedFolder);
            }
        }

        public void selectedFileAdded(FileBrowser fileBrowser, File file) {
            for (FileBrowserListener listener : this) {
                listener.selectedFileAdded(fileBrowser, file);
            }
        }

        public void selectedFileRemoved(FileBrowser fileBrowser, File file) {
            for (FileBrowserListener listener : this) {
                listener.selectedFileRemoved(fileBrowser, file);
            }
        }

        public void selectedFilesChanged(FileBrowser fileBrowser, Sequence<File> previousSelectedFiles) {
            for (FileBrowserListener listener : this) {
                listener.selectedFilesChanged(fileBrowser, previousSelectedFiles);
            }
        }

        public void fileFilterChanged(FileBrowser fileBrowser, Filter<File> previousFileFilter) {
            for (FileBrowserListener listener : this) {
                listener.fileFilterChanged(fileBrowser, previousFileFilter);
            }
        }
    }

    private Folder selectedFolder;
    private FileList fileSelection = new FileList();
    private boolean multiSelect = false;
    private Filter<File> fileFilter = null;

    private FileBrowserListenerList fileBrowserListeners = new FileBrowserListenerList();

    public FileBrowser() {
        String userHome = System.getProperty("user.home");
        selectedFolder = new Folder(userHome);

        installSkin(FileBrowser.class);
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
            fileBrowserListeners.selectedFolderChanged(this, previousSelectedFolder);
        }
    }

    public boolean addSelectedFile(File file) {
        int index = fileSelection.add(file);
        if (index != -1) {
            fileBrowserListeners.selectedFileAdded(this, file);
        }

        return (index != -1);
    }

    public boolean removeSelectedFile(File file) {
        int index = fileSelection.remove(file);
        if (index != -1) {
            fileBrowserListeners.selectedFileRemoved(this, file);
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
        fileBrowserListeners.selectedFilesChanged(this, previousSelectedFiles);

        return getSelectedFiles();
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(boolean multiSelect) {
        if (this.multiSelect != multiSelect) {
            // Clear any existing selection
            fileSelection.clear();

            this.multiSelect = multiSelect;

            fileBrowserListeners.multiSelectChanged(this);
        }
    }

    public Filter<File> getFileFilter() {
        return fileFilter;
    }

    public void setFileFilter(Filter<File> fileFilter) {
        Filter<File> previousFileFilter = this.fileFilter;

        if (previousFileFilter != fileFilter) {
            this.fileFilter = fileFilter;
            fileBrowserListeners.fileFilterChanged(this, previousFileFilter);
        }
    }

    public ListenerList<FileBrowserListener> getFileBrowserListeners() {
        return fileBrowserListeners;
    }
}
