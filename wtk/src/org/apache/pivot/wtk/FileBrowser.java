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
import org.apache.pivot.util.Utils;

/**
 * Component representing a file browser.
 */
public class FileBrowser extends Container {
    /**
     * File browser skin interface.
     */
    public interface Skin extends org.apache.pivot.wtk.Skin {
        public File getFileAt(int x, int y);
    }

    private static final String USER_HOME = System.getProperty("user.home");

    private File rootDirectory;
    private FileList selectedFiles = new FileList();
    private boolean multiSelect = false;
    private Filter<File> disabledFileFilter = null;

    private FileBrowserListener.Listeners fileBrowserListeners = new FileBrowserListener.Listeners();

    /**
     * Creates a new FileBrowser <p> Note that this version set by default mode
     * to open.
     */
    public FileBrowser() {
        this(USER_HOME);
    }

    /**
     * Creates a new FileBrowser <p> Note that this version of the constructor
     * must be used when a custom root folder has to be set.
     *
     * @param rootFolder The root folder full name.
     */
    public FileBrowser(String rootFolder) {
        Utils.checkNull(rootFolder, "rootFolder");

        rootDirectory = new File(rootFolder);
        if (!rootDirectory.isDirectory()) {
            throw new IllegalArgumentException(rootFolder + " is not a directory.");
        }

        installSkin(FileBrowser.class);
    }

    /**
     * Returns the current root directory.
     *
     * @return The current root directory.
     */
    public File getRootDirectory() {
        return rootDirectory;
    }

    /**
     * Sets the root directory. Clears any existing file selection.
     *
     * @param rootDirectory The new root directory to browse in.
     * @throws IllegalArgumentException if the argument is {@code null}
     * or is not a directory.
     */
    public void setRootDirectory(File rootDirectory) {
        Utils.checkNull(rootDirectory, "rootDirectory");

        if (!rootDirectory.isDirectory()) {
            throw new IllegalArgumentException(rootDirectory.getPath() + " is not a directory.");
        }

        if (rootDirectory.exists()) {
            File previousRootDirectory = this.rootDirectory;

            if (!rootDirectory.equals(previousRootDirectory)) {
                this.rootDirectory = rootDirectory;
                selectedFiles.clear();
                fileBrowserListeners.rootDirectoryChanged(this, previousRootDirectory);
            }
        } else {
            setRootDirectory(rootDirectory.getParentFile());
        }
    }

    /**
     * Adds a file to the file selection.
     *
     * @param file The new file to add to the selection.
     * @return <tt>true</tt> if the file was added; <tt>false</tt> if it was
     * already selected.
     * @throws IllegalArgumentException if the file argument is {@code null}
     * or if the file is not in the current root directory.
     */
    public boolean addSelectedFile(final File file) {
        Utils.checkNull(file, "file");

        File fileMutable = file;
        if (fileMutable.isAbsolute()) {
            if (!fileMutable.getParentFile().equals(rootDirectory)) {
                throw new IllegalArgumentException(file.getPath() + " is not a child of the root directory.");
            }
        } else {
            fileMutable = new File(rootDirectory, fileMutable.getPath());
        }

        int index = selectedFiles.add(fileMutable);
        if (index != -1) {
            fileBrowserListeners.selectedFileAdded(this, fileMutable);
        }

        return (index != -1);
    }

    /**
     * Removes a file from the file selection.
     *
     * @param file The previously selected file to be removed from the selection.
     * @return <tt>true</tt> if the file was removed; <tt>false</tt> if it was
     * not already selected.
     * @throws IllegalArgumentException if the file argument is {@code null}.
     */
    public boolean removeSelectedFile(File file) {
        Utils.checkNull(file, "file");

        int index = selectedFiles.remove(file);
        if (index != -1) {
            fileBrowserListeners.selectedFileRemoved(this, file);
        }

        return (index != -1);
    }

    /**
     * When in single-select mode, returns the currently selected file.
     *
     * @return The currently selected file.
     */
    public File getSelectedFile() {
        if (multiSelect) {
            throw new IllegalStateException("File browser is not in single-select mode.");
        }

        return (selectedFiles.getLength() == 0) ? null : selectedFiles.get(0);
    }

    /**
     * Sets the selection to a single file.
     *
     * @param file The only file to select, or {@code null} to select nothing.
     */
    public void setSelectedFile(File file) {
        if (file == null) {
            clearSelection();
        } else {
            if (file.isAbsolute()) {
                setRootDirectory(file.getParentFile());
            }

            setSelectedFiles(new ArrayList<>(file));
        }
    }

    /**
     * Returns the currently selected files.
     *
     * @return An immutable list containing the currently selected files. Note
     * that the returned list is a wrapper around the actual selection, not a
     * copy. Any changes made to the selection state will be reflected in the
     * list, but events will not be fired.
     */
    public ImmutableList<File> getSelectedFiles() {
        return new ImmutableList<>(selectedFiles);
    }

    /**
     * Sets the selected files.
     *
     * @param selectedFiles The files to select.
     * @return The files that were selected, with duplicates eliminated.
     * @throws IllegalArgumentException if the selected files sequence is {@code null}
     * or if the sequence is longer than one file and multi-select is not enabled, or
     * if any entry is the sequence is {@code null} or whose parent is not the
     * current root directory.
     */
    public Sequence<File> setSelectedFiles(Sequence<File> selectedFiles) {
        Utils.checkNull(selectedFiles, "selectedFiles");

        if (!multiSelect && selectedFiles.getLength() > 1) {
            throw new IllegalArgumentException("Multi-select is not enabled.");
        }

        // Update the selection
        Sequence<File> previousSelectedFiles = getSelectedFiles();

        FileList fileList = new FileList();
        for (int i = 0, n = selectedFiles.getLength(); i < n; i++) {
            File file = selectedFiles.get(i);

            Utils.checkNull(file, "file");

            if (!file.isAbsolute()) {
                file = new File(rootDirectory, file.getPath());
            }

            if (!file.getParentFile().equals(rootDirectory)) {
                throw new IllegalArgumentException(file.getPath() + " is not a child of the root directory.");
            }

            fileList.add(file);
        }

        this.selectedFiles = fileList;

        // Notify listeners
        fileBrowserListeners.selectedFilesChanged(this, previousSelectedFiles);

        return getSelectedFiles();
    }

    /**
     * Clears the selection.
     */
    public void clearSelection() {
        setSelectedFiles(new ArrayList<File>());
    }

    public boolean isFileSelected(File file) {
        return (selectedFiles.indexOf(file) != -1);
    }

    /**
     * @return The file browser's multi-select state.
     */
    public boolean isMultiSelect() {
        return multiSelect;
    }

    /**
     * Sets the file browser's multi-select state.
     *
     * @param multiSelect <tt>true</tt> if multi-select is enabled;
     * <tt>false</tt>, otherwise.
     */
    public void setMultiSelect(boolean multiSelect) {
        if (this.multiSelect != multiSelect) {
            // Clear any existing selection
            selectedFiles.clear();

            this.multiSelect = multiSelect;

            fileBrowserListeners.multiSelectChanged(this);
        }
    }

    /**
     * Returns the current file filter.
     *
     * @return The current file filter, or <tt>null</tt> if no filter is set.
     */
    public Filter<File> getDisabledFileFilter() {
        return disabledFileFilter;
    }

    /**
     * Sets the file filter.
     *
     * @param disabledFileFilter The file filter to use, or <tt>null</tt> for no
     * filter.
     */
    public void setDisabledFileFilter(Filter<File> disabledFileFilter) {
        Filter<File> previousDisabledFileFilter = this.disabledFileFilter;

        if (previousDisabledFileFilter != disabledFileFilter) {
            this.disabledFileFilter = disabledFileFilter;
            fileBrowserListeners.disabledFileFilterChanged(this, previousDisabledFileFilter);
        }
    }

    public File getFileAt(int x, int y) {
        FileBrowser.Skin fileBrowserSkin = (FileBrowser.Skin) getSkin();
        return fileBrowserSkin.getFileAt(x, y);
    }

    public ListenerList<FileBrowserListener> getFileBrowserListeners() {
        return fileBrowserListeners;
    }
}
