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

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.immutable.ImmutableList;
import org.apache.pivot.io.FileObjectList;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.ListenerList;

/**
 * A file browser sheet that uses the Apache Commons VFS (Virtual File System)
 * to be able to browse local and remote file systems, and browse inside of
 * .zip, .tar, etc. archives as well.
 */
public class VFSBrowserSheet extends Sheet {
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

    private static class FileBrowserSheetListenerList extends
        WTKListenerList<VFSBrowserSheetListener> implements VFSBrowserSheetListener {
        @Override
        public void managerChanged(VFSBrowserSheet fileBrowserSheet,
            FileSystemManager previousManager) {
            for (VFSBrowserSheetListener listener : this) {
                listener.managerChanged(fileBrowserSheet, previousManager);
            }
        }

        @Override
        public void modeChanged(VFSBrowserSheet fileBrowserSheet, VFSBrowserSheet.Mode previousMode) {
            for (VFSBrowserSheetListener listener : this) {
                listener.modeChanged(fileBrowserSheet, previousMode);
            }
        }

        @Override
        public void rootDirectoryChanged(VFSBrowserSheet fileBrowserSheet,
            FileObject previousRootDirectory) {
            for (VFSBrowserSheetListener listener : this) {
                listener.rootDirectoryChanged(fileBrowserSheet, previousRootDirectory);
            }
        }

        @Override
        public void homeDirectoryChanged(VFSBrowserSheet fileBrowserSheet,
            FileObject previousHomeDirectory) {
            for (VFSBrowserSheetListener listener : this) {
                listener.homeDirectoryChanged(fileBrowserSheet, previousHomeDirectory);
            }
        }

        @Override
        public void selectedFilesChanged(VFSBrowserSheet fileBrowserSheet,
            Sequence<FileObject> previousSelectedFiles) {
            for (VFSBrowserSheetListener listener : this) {
                listener.selectedFilesChanged(fileBrowserSheet, previousSelectedFiles);
            }
        }

        @Override
        public void disabledFileFilterChanged(VFSBrowserSheet fileBrowserSheet,
            Filter<FileObject> previousDisabledFileFilter) {
            for (VFSBrowserSheetListener listener : this) {
                listener.disabledFileFilterChanged(fileBrowserSheet, previousDisabledFileFilter);
            }
        }
    }

    private Mode mode;
    private FileSystemManager manager;
    private FileName baseFileName;
    private FileObject rootDirectory;
    private FileObject homeDirectory;
    private FileObjectList selectedFiles = new FileObjectList();
    private Filter<FileObject> disabledFileFilter = null;

    private FileBrowserSheetListenerList fileBrowserSheetListeners = new FileBrowserSheetListenerList();

    /**
     * Creates a new VFSBrowserSheet <p> Note that this version set by default
     * mode to open and user home as root folder.
     *
     * @throws FileSystemException if there are any problems.
     */
    public VFSBrowserSheet() throws FileSystemException {
        this(Mode.OPEN);
    }

    /**
     * Creates a new VFSBrowserSheet <p> Note that this version sets by default
     * the user home as root folder, which is probably not that useful.
     *
     * @param mode The mode for opening the sheet.
     * @throws FileSystemException if there are any problems.
     * @see Mode
     */
    public VFSBrowserSheet(Mode mode) throws FileSystemException {
        this(mode, USER_HOME);
    }

    /**
     * Creates a new VFSBrowserSheet <p> Note that this version of the
     * constructor must be used when a custom root folder has to be set.
     *
     * @param mode The mode for opening the sheet.
     * @param rootFolder The root folder full name.
     * @throws FileSystemException if there are any problems.
     * @see Mode
     */
    public VFSBrowserSheet(Mode mode, String rootFolder) throws FileSystemException {
        this(null, mode, rootFolder);
    }

    /**
     * Creates a new VFSBrowserSheet <p> Note that this version of the
     * constructor must be used when a custom root folder has to be set.
     *
     * @param manager The VFS FileSystemManager that we will be browsing. If
     * <tt>null</tt> the default (local) will be used.
     * @param mode The mode for opening the sheet.
     * @param rootFolder The root folder full name.
     * @throws FileSystemException if there are any problems.
     * @see Mode
     */
    public VFSBrowserSheet(FileSystemManager manager, Mode mode, String rootFolder)
        throws FileSystemException {
        this(manager, mode, rootFolder, null);
    }

    /**
     * Creates a new VFSBrowserSheet.
     * <p> Note that this version of the constructor must be used when a
     * custom home folder has to be set.
     *
     * @param manager The VFS FileSystemManager that we will be browsing.
     * If <tt>null</tt> the default (local) will be used.
     * @param mode The mode for opening the sheet.
     * @param rootFolder The root folder full name.
     * @param homeFolder The default for the "home" folder (full name).
     * @throws FileSystemException if there are any problems.
     * @see Mode
     */
    public VFSBrowserSheet(FileSystemManager manager, Mode mode, String rootFolder, String homeFolder)
            throws FileSystemException
    {
        if (mode == null) {
            throw new IllegalArgumentException("Mode is null.");
        }

        if (rootFolder == null) {
            throw new IllegalArgumentException("Root folder is null.");
        }

        this.mode = mode;

        // Note: these three methods all could trigger events, but since we're
        // in the constructor and the skin isn't set yet, there will not be any
        // listeners registered yet
        setManager(manager);
        setRootDirectory(rootFolder);
        setHomeDirectory(homeFolder == null ? USER_HOME : homeFolder);

        installSkin(VFSBrowserSheet.class);
    }

    /**
     * Creates a new VFSBrowserSheet.
     * <p> Note that this version of the constructor must be used when a
     * custom home folder has to be set.
     *
     * @param manager The VFS FileSystemManager that we will be browsing.
     * If <tt>null</tt> the default (local) will be used.
     * @param mode The mode for opening the sheet.
     * @param rootFolder The root folder object.
     * @param homeFolder The default for the "home" folder.
     * @throws FileSystemException if there are any problems.
     * @see Mode
     */
    public VFSBrowserSheet(FileSystemManager manager, Mode mode, FileObject rootFolder, FileObject homeFolder)
            throws FileSystemException
    {
        if (mode == null) {
            throw new IllegalArgumentException("Mode is null.");
        }

        if (rootFolder == null) {
            throw new IllegalArgumentException("Root folder is null.");
        }

        this.mode = mode;

        // Note: these three methods all could trigger events, but since we're
        // in the constructor and the skin isn't set yet, there will not be any
        // listeners registered yet
        setManager(manager);
        setRootDirectory(rootFolder);
        setHomeDirectory(homeFolder == null ? rootFolder : homeFolder);

        installSkin(VFSBrowserSheet.class);
    }

    public FileSystemManager getManager() {
        return manager;
    }

    public void setManager(FileSystemManager manager) throws FileSystemException {
        FileSystemManager previousManager = this.manager;

        if (manager == null) {
            this.manager = VFS.getManager();
        } else {
            this.manager = manager;
        }
        FileObject baseFile = this.manager.getBaseFile();
        if (baseFile != null) {
            baseFileName = baseFile.getName();
        }

        if (previousManager != null && previousManager != this.manager) {
            fileBrowserSheetListeners.managerChanged(this, previousManager);
        }
    }

    public FileName getBaseFileName() {
        return baseFileName;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Mode is null.");
        }

        Mode previousMode = this.mode;

        if (previousMode != mode) {
            this.mode = mode;
            fileBrowserSheetListeners.modeChanged(this, previousMode);
        }
    }

    public FileObject getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(String rootDirectory) throws FileSystemException {
        setRootDirectory(manager.resolveFile(rootDirectory));
    }

    public void setRootDirectory(FileObject rootDirectory) throws FileSystemException {
        if (rootDirectory == null) {
            throw new IllegalArgumentException("Root directory is null.");
        }

        // Give some grace to set the root folder to an actual file and
        // have it work (by using the parent folder instead)
        if (rootDirectory.getType() != FileType.FOLDER) {
            rootDirectory = rootDirectory.getParent();
            if (rootDirectory == null || rootDirectory.getType() != FileType.FOLDER) {
                throw new IllegalArgumentException("Root file is not a directory.");
            }
        }

        if (rootDirectory.exists()) {
            FileObject previousRootDirectory = this.rootDirectory;

            if (!rootDirectory.equals(previousRootDirectory)) {
                this.rootDirectory = rootDirectory;
                selectedFiles.clear();
                fileBrowserSheetListeners.rootDirectoryChanged(this, previousRootDirectory);
            }
        } else {
            setRootDirectory(rootDirectory.getParent());
        }
    }

    public FileObject getHomeDirectory() {
        return homeDirectory;
    }

    public void setHomeDirectory(String homeDirectory)
            throws FileSystemException
    {
        setHomeDirectory(manager.resolveFile(homeDirectory));
    }

    public void setHomeDirectory(FileObject homeDirectory)
            throws FileSystemException
    {
        if (homeDirectory == null) {
            throw new IllegalArgumentException("Home file is null.");
        }

        // Give some grace to set the home folder to an actual file and
        // have it work (by using the parent folder instead)
        if (homeDirectory.getType() != FileType.FOLDER) {
            homeDirectory = homeDirectory.getParent();
            if (homeDirectory == null || homeDirectory.getType() != FileType.FOLDER) {
                throw new IllegalArgumentException("Root file is not a directory.");
            }
        }

        if (homeDirectory.exists()) {
            FileObject previousHomeDirectory = this.homeDirectory;

            if (!homeDirectory.equals(previousHomeDirectory)) {
                this.homeDirectory = homeDirectory;
                fileBrowserSheetListeners.homeDirectoryChanged(this, previousHomeDirectory);
            }
        } else {
            setHomeDirectory(homeDirectory.getParent());
        }
    }

    /**
     * When in single-select mode, returns the currently selected file.
     *
     * @return The currently selected file.
     */
    public FileObject getSelectedFile() {
        if (mode == Mode.OPEN_MULTIPLE) {
            throw new IllegalStateException("File browser sheet is not in single-select mode.");
        }

        return (selectedFiles.getLength() == 0) ? null : selectedFiles.get(0);
    }

    /**
     * Sets the selection to a single file.
     *
     * @param file The new file to be selected (or {@code null} to clear the selection).
     * @throws FileSystemException if there are any problems.
     */
    public void setSelectedFile(FileObject file) throws FileSystemException {
        if (file == null) {
            clearSelection();
        } else {
            // TODO: will this work right?
            // if (file.isAbsolute()) {
            if (baseFileName != null && baseFileName.isAncestor(file.getName())) {
                setRootDirectory(file.getParent());
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
    public ImmutableList<FileObject> getSelectedFiles() {
        return new ImmutableList<>(selectedFiles);
    }

    /**
     * Sets the selected files.
     *
     * @param selectedFiles The files to select.
     * @return The files that were selected, with duplicates eliminated.
     * @throws FileSystemException if there are any problems.
     */
    public Sequence<FileObject> setSelectedFiles(Sequence<FileObject> selectedFiles)
        throws FileSystemException {
        if (selectedFiles == null) {
            throw new IllegalArgumentException("selectedFiles is null.");
        }

        if (mode != Mode.OPEN_MULTIPLE && selectedFiles.getLength() > 1) {
            throw new IllegalArgumentException("Multi-select is not enabled.");
        }

        // Update the selection
        Sequence<FileObject> previousSelectedFiles = getSelectedFiles();

        FileObjectList fileList = new FileObjectList();
        for (int i = 0, n = selectedFiles.getLength(); i < n; i++) {
            FileObject file = selectedFiles.get(i);

            if (file == null) {
                throw new IllegalArgumentException("Selected file is null.");
            }

            // TODO: is this correct?
            // if (!file.isAbsolute()) {
            if (baseFileName == null || !baseFileName.isAncestor(file.getName())) {
                file = manager.resolveFile(rootDirectory, file.getName().getBaseName());
            }

            if (!file.getParent().equals(rootDirectory)) {
                throw new IllegalArgumentException(
                    "Selected file doesn't appear to belong to the current directory.");
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
     *
     * @throws FileSystemException if there are any problems.
     */
    public void clearSelection() throws FileSystemException {
        setSelectedFiles(new ArrayList<FileObject>());
    }

    public Filter<FileObject> getDisabledFileFilter() {
        return disabledFileFilter;
    }

    public void setDisabledFileFilter(Filter<FileObject> disabledFileFilter) {
        Filter<FileObject> previousDisabledFileFilter = this.disabledFileFilter;

        if (previousDisabledFileFilter != disabledFileFilter) {
            this.disabledFileFilter = disabledFileFilter;
            fileBrowserSheetListeners.disabledFileFilterChanged(this, previousDisabledFileFilter);
        }
    }

    public ListenerList<VFSBrowserSheetListener> getFileBrowserSheetListeners() {
        return fileBrowserSheetListeners;
    }
}
