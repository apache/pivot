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

    private static final String USER_HOME = System.getProperty("user.home");

    private static class FileBrowserSheetListenerList
        extends WTKListenerList<FileBrowserSheetListener>
        implements FileBrowserSheetListener {
        @Override
        public void modeChanged(FileBrowserSheet fileBrowserSheet,
            FileBrowserSheet.Mode previousMode) {
            for (FileBrowserSheetListener listener : this) {
                listener.modeChanged(fileBrowserSheet, previousMode);
            }
        }

        @Override
        public void rootDirectoryChanged(FileBrowserSheet fileBrowserSheet,
            File previousRootDirectory) {
            for (FileBrowserSheetListener listener : this) {
                listener.rootDirectoryChanged(fileBrowserSheet, previousRootDirectory);
            }
        }

        @Override
        public void selectedFilesChanged(FileBrowserSheet fileBrowserSheet,
            Sequence<File> previousSelectedFiles) {
            for (FileBrowserSheetListener listener : this) {
                listener.selectedFilesChanged(fileBrowserSheet, previousSelectedFiles);
            }
        }

        @Override
        public void disabledFileFilterChanged(FileBrowserSheet fileBrowserSheet,
            Filter<File> previousDisabledFileFilter) {
            for (FileBrowserSheetListener listener : this) {
                listener.disabledFileFilterChanged(fileBrowserSheet,
                    previousDisabledFileFilter);
            }
        }
    }

    private Mode mode;
    private File rootDirectory;
    private FileList selectedFiles = new FileList();
    private Filter<File> disabledFileFilter = null;

    private FileBrowserSheetListenerList fileBrowserSheetListeners = new FileBrowserSheetListenerList();

    public FileBrowserSheet() {
        this(Mode.OPEN);
    }

    public FileBrowserSheet(Mode mode) {
        this(mode, USER_HOME);
    }

    public FileBrowserSheet(Mode mode, String rootFolder) {
        if (mode == null) {
            throw new IllegalArgumentException();
        }

        if (rootFolder == null) {
            throw new IllegalArgumentException();
        }

        this.mode = mode;

        rootDirectory = new File(rootFolder);
        if (!rootDirectory.isDirectory()) {
            throw new IllegalArgumentException();
        }

        installSkin(FileBrowserSheet.class);
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        if (mode == null) {
            throw new IllegalArgumentException();
        }

        Mode previousMode = this.mode;

        if (previousMode != mode) {
            this.mode = mode;
            fileBrowserSheetListeners.modeChanged(this, previousMode);
        }
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(File rootDirectory) {
        if (rootDirectory == null
            || !rootDirectory.isDirectory()) {
            throw new IllegalArgumentException();
        }

        if (rootDirectory.exists()) {
            File previousRootDirectory = this.rootDirectory;

            if (!rootDirectory.equals(previousRootDirectory)) {
                this.rootDirectory = rootDirectory;
                selectedFiles.clear();
                fileBrowserSheetListeners.rootDirectoryChanged(this, previousRootDirectory);
            }
        } else {
            setRootDirectory(rootDirectory.getParentFile());
        }
    }

    /**
     * When in single-select mode, returns the currently selected file.
     *
     * @return
     * The currently selected file.
     */
    public File getSelectedFile() {
        if (mode == Mode.OPEN_MULTIPLE) {
            throw new IllegalStateException("File browser sheet is not in single-select mode.");
        }

        return (selectedFiles.getLength() == 0) ? null : selectedFiles.get(0);
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
            if (file.isAbsolute()) {
                setRootDirectory(file.getParentFile());
            }

            setSelectedFiles(new ArrayList<File>(file));
        }
    }

    /**
     * Returns the currently selected files.
     *
     * @return
     * An immutable list containing the currently selected files. Note that the returned
     * list is a wrapper around the actual selection, not a copy. Any changes made to the
     * selection state will be reflected in the list, but events will not be fired.
     */
    public ImmutableList<File> getSelectedFiles() {
        return new ImmutableList<File>(selectedFiles);
    }

    /**
     * Sets the selected files.
     *
     * @param selectedFiles
     * The files to select.
     *
     * @return
     * The files that were selected, with duplicates eliminated.
     */
    public Sequence<File> setSelectedFiles(Sequence<File> selectedFiles) {
        if (selectedFiles == null) {
            throw new IllegalArgumentException("selectedFiles is null.");
        }

        if (mode != Mode.OPEN_MULTIPLE
            && selectedFiles.getLength() > 1) {
            throw new IllegalArgumentException("Multi-select is not enabled.");
        }

        // Update the selection
        Sequence<File> previousSelectedFiles = getSelectedFiles();

        FileList fileList = new FileList();
        for (int i = 0, n = selectedFiles.getLength(); i < n; i++) {
            File file = selectedFiles.get(i);

            if (file == null) {
                throw new IllegalArgumentException("file is null.");
            }

            if (!file.isAbsolute()) {
                file = new File(rootDirectory, file.getPath());
            }

            if (!file.getParentFile().equals(rootDirectory)) {
                throw new IllegalArgumentException();
            }

            fileList.add(file);
        }

        this.selectedFiles = fileList;

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
