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

    private static class FileBrowserListenerList extends WTKListenerList<FileBrowserListener>
        implements FileBrowserListener {
        @Override
        public void rootDirectoryChanged(FileBrowser fileBrowser, File previousRootDirectory) {
            for (FileBrowserListener listener : this) {
                listener.rootDirectoryChanged(fileBrowser, previousRootDirectory);
            }
        }

        @Override
        public void selectedFileAdded(FileBrowser fileBrowser, File file) {
            for (FileBrowserListener listener : this) {
                listener.selectedFileAdded(fileBrowser, file);
            }
        }

        @Override
        public void selectedFileRemoved(FileBrowser fileBrowser, File file) {
            for (FileBrowserListener listener : this) {
                listener.selectedFileRemoved(fileBrowser, file);
            }
        }

        @Override
        public void selectedFilesChanged(FileBrowser fileBrowser,
            Sequence<File> previousSelectedFiles) {
            for (FileBrowserListener listener : this) {
                listener.selectedFilesChanged(fileBrowser, previousSelectedFiles);
            }
        }

        @Override
        public void multiSelectChanged(FileBrowser fileBrowser) {
            for (FileBrowserListener listener : this) {
                listener.multiSelectChanged(fileBrowser);
            }
        }

        @Override
        public void disabledFileFilterChanged(FileBrowser fileBrowser,
            Filter<File> previousDisabledFileFilter) {
            for (FileBrowserListener listener : this) {
                listener.disabledFileFilterChanged(fileBrowser, previousDisabledFileFilter);
            }
        }
    }

    private File rootDirectory;
    private FileList selectedFiles = new FileList();
    private boolean multiSelect = false;
    private Filter<File> disabledFileFilter = null;

    private FileBrowserListenerList fileBrowserListeners = new FileBrowserListenerList();

    public FileBrowser() {
        this(USER_HOME);
    }

    public FileBrowser(String rootFolder) {
        if (rootFolder == null) {
            throw new IllegalArgumentException();
        }

        rootDirectory = new File(rootFolder);
        if (!rootDirectory.isDirectory()) {
            throw new IllegalArgumentException();
        }

        installSkin(FileBrowser.class);
    }

    /**
     * Returns the current root directory.
     *
     * @return
     * The current root directory.
     */
    public File getRootDirectory() {
        return rootDirectory;
    }

    /**
     * Sets the root directory. Clears any existing file selection.
     *
     * @param rootDirectory
     */
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
                fileBrowserListeners.rootDirectoryChanged(this, previousRootDirectory);
            }
        } else {
            setRootDirectory(rootDirectory.getParentFile());
        }
    }

    /**
     * Adds a file to the file selection.
     *
     * @param file
     *
     * @return
     * <tt>true</tt> if the file was added; <tt>false</tt> if it was already
     * selected.
     */
    public boolean addSelectedFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException();
        }

        if (file.isAbsolute()) {
            if (!file.getParentFile().equals(rootDirectory)) {
                throw new IllegalArgumentException();
            }
        } else {
            file = new File(rootDirectory, file.getPath());
        }

        int index = selectedFiles.add(file);
        if (index != -1) {
            fileBrowserListeners.selectedFileAdded(this, file);
        }

        return (index != -1);
    }

    /**
     * Removes a file from the file selection.
     *
     * @param file
     *
     * @return
     * <tt>true</tt> if the file was removed; <tt>false</tt> if it was not
     * already selected.
     */
    public boolean removeSelectedFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException();
        }

        int index = selectedFiles.remove(file);
        if (index != -1) {
            fileBrowserListeners.selectedFileRemoved(this, file);
        }

        return (index != -1);
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

        if (!multiSelect
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
     * Returns the file browser's multi-select state.
     */
    public boolean isMultiSelect() {
        return multiSelect;
    }

    /**
     * Sets the file browser's multi-select state.
     *
     * @param multiSelect
     * <tt>true</tt> if multi-select is enabled; <tt>false</tt>, otherwise.
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
     * @return
     * The current file filter, or <tt>null</tt> if no filter is set.
     */
    public Filter<File> getDisabledFileFilter() {
        return disabledFileFilter;
    }

    /**
     * Sets the file filter.
     *
     * @param disabledFileFilter
     * The file filter to use, or <tt>null</tt> for no filter.
     */
    public void setDisabledFileFilter(Filter<File> disabledFileFilter) {
        Filter<File> previousDisabledFileFilter = this.disabledFileFilter;

        if (previousDisabledFileFilter != disabledFileFilter) {
            this.disabledFileFilter = disabledFileFilter;
            fileBrowserListeners.disabledFileFilterChanged(this, previousDisabledFileFilter);
        }
    }

    public File getFileAt(int x, int y) {
        FileBrowser.Skin fileBrowserSkin = (FileBrowser.Skin)getSkin();
        return fileBrowserSkin.getFileAt(x, y);
    }

    public ListenerList<FileBrowserListener> getFileBrowserListeners() {
        return fileBrowserListeners;
    }
}
