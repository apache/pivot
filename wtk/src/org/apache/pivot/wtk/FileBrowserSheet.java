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

    private Mode mode;
    private File rootDirectory;
    private FileList selectedFiles = new FileList();
    private Filter<File> disabledFileFilter = null;

    private FileBrowserSheetListener.Listeners fileBrowserSheetListeners = new FileBrowserSheetListener.Listeners();

    /**
     * Creates a new FileBrowserSheet <p> Note that this version set by default
     * mode to open and user home as root folder.
     */
    public FileBrowserSheet() {
        this(Mode.OPEN);
    }

    /**
     * Creates a new FileBrowserSheet <p> Note that this version set by default
     * the user home as root folder.
     *
     * @param mode The mode for opening the sheet.
     * @see Mode
     */
    public FileBrowserSheet(Mode mode) {
        this(mode, USER_HOME);
    }

    /**
     * Creates a new FileBrowserSheet <p> Note that this version of the
     * constructor can be used when a custom root folder has to be set, and uses
     * the default mode.
     *
     * @param rootFolder The root folder full name.
     */
    public FileBrowserSheet(String rootFolder) {
        this(Mode.OPEN, rootFolder);
    }

    /**
     * Creates a new FileBrowserSheet <p> Note that this version of the
     * constructor must be used when a custom root folder has to be set.
     *
     * @param mode The mode for opening the sheet.
     * @see Mode
     * @param rootFolder The root folder full name.
     */
    public FileBrowserSheet(Mode mode, String rootFolder) {
        Utils.checkNull(mode, "mode");
        Utils.checkNullOrEmpty(rootFolder, "rootFolder");

        this.mode = mode;

        setRootFolder(rootFolder);

        installSkin(FileBrowserSheet.class);
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        Utils.checkNull(mode, "mode");

        Mode previousMode = this.mode;

        if (previousMode != mode) {
            this.mode = mode;
            fileBrowserSheetListeners.modeChanged(this, previousMode);
        }
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    /**
     * Set the root folder but without firing events.
     *
     * @param rootFolder The new root directory to browse.
     * @throws IllegalArgumentException if the folder argument is {@code null}.
     */
    public void setRootFolder(String rootFolder) {
        Utils.checkNullOrEmpty(rootFolder, "rootFolder");

        File rootFile = new File(rootFolder);
        if (!rootFile.isDirectory()) {
            // Give some grace here to allow setting the root directory
            // to a regular file and have it work (by using its parent)
            rootFile = rootFile.getParentFile();
            if (rootFile == null || !rootFile.isDirectory()) {
                throw new IllegalArgumentException(rootFolder + " is not a directory.");
            }
        }
        this.rootDirectory = rootFile;

    }

    public void setRootDirectory(File rootDirectory) {
        Utils.checkNull(rootDirectory, "rootDirectory");

        if (!rootDirectory.isDirectory()) {
            // Give some grace here to allow setting the root directory
            // to a regular file and have it work (by using its parent)
            rootDirectory = rootDirectory.getParentFile();
            if (rootDirectory == null || !rootDirectory.isDirectory()) {
                throw new IllegalArgumentException("Root directory is not a directory.");
            }
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
     * @return The currently selected file or {@code null} if nothing is selected.
     * @throws IllegalStateException if not in single-select mode.
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
     * @param file The single file to be selected or {@code null} to select nothing.
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

        if (mode != Mode.OPEN_MULTIPLE && selectedFiles.getLength() > 1) {
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
