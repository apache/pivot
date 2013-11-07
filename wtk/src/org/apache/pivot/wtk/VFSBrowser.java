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
import java.net.URI;

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
 * A file browser that uses the Apache Commons VFS (Virtual File System) to be
 * able to browse local and remote file systems, and browse inside of .zip,
 * .tar, etc. archives as well.
 */
public class VFSBrowser extends Container {
    /**
     * Commons VFS browser skin interface.
     */
    public interface Skin extends org.apache.pivot.wtk.Skin {
        public FileObject getFileAt(int x, int y);
    }

    private static final URI USER_HOME = new File(System.getProperty("user.home")).toURI();

    private static class FileBrowserListenerList extends WTKListenerList<VFSBrowserListener>
        implements VFSBrowserListener {
        @Override
        public void managerChanged(VFSBrowser fileBrowser, FileSystemManager previousManager) {
            for (VFSBrowserListener listener : this) {
                listener.managerChanged(fileBrowser, previousManager);
            }
        }

        @Override
        public void rootDirectoryChanged(VFSBrowser fileBrowser, FileObject previousRootDirectory) {
            for (VFSBrowserListener listener : this) {
                listener.rootDirectoryChanged(fileBrowser, previousRootDirectory);
            }
        }

        @Override
        public void homeDirectoryChanged(VFSBrowser fileBrowser, FileObject previousHomeDirectory) {
            for (VFSBrowserListener listener : this) {
                listener.homeDirectoryChanged(fileBrowser, previousHomeDirectory);
            }
        }

        @Override
        public void selectedFileAdded(VFSBrowser fileBrowser, FileObject file) {
            for (VFSBrowserListener listener : this) {
                listener.selectedFileAdded(fileBrowser, file);
            }
        }

        @Override
        public void selectedFileRemoved(VFSBrowser fileBrowser, FileObject file) {
            for (VFSBrowserListener listener : this) {
                listener.selectedFileRemoved(fileBrowser, file);
            }
        }

        @Override
        public void selectedFilesChanged(VFSBrowser fileBrowser,
            Sequence<FileObject> previousSelectedFiles) {
            for (VFSBrowserListener listener : this) {
                listener.selectedFilesChanged(fileBrowser, previousSelectedFiles);
            }
        }

        @Override
        public void multiSelectChanged(VFSBrowser fileBrowser) {
            for (VFSBrowserListener listener : this) {
                listener.multiSelectChanged(fileBrowser);
            }
        }

        @Override
        public void disabledFileFilterChanged(VFSBrowser fileBrowser,
            Filter<FileObject> previousDisabledFileFilter) {
            for (VFSBrowserListener listener : this) {
                listener.disabledFileFilterChanged(fileBrowser, previousDisabledFileFilter);
            }
        }
    }

    private FileSystemManager manager;
    private FileName baseFileName;
    private FileObject rootDirectory;
    private FileObject homeDirectory;
    private FileObjectList selectedFiles = new FileObjectList();
    private boolean multiSelect = false;
    private Filter<FileObject> disabledFileFilter = null;

    private FileBrowserListenerList fileBrowserListeners = new FileBrowserListenerList();

    /**
     * Creates a new VFSBrowser <p> Note that this version sets, by default,
     * the mode to open.
     */
    public VFSBrowser() throws FileSystemException {
        this(null, USER_HOME, null);
    }

    /**
     * Creates a new VFSBrowser <p> Note that this version of the constructor
     * must be used when a custom root folder (that may include a completely
     * different URI scheme) has to be set.
     *
     * @param manager The virtual file system we're going to manage.
     * @param rootFolder The root folder full name.
     * @param homeFolder The default home folder full name.
     */
    public VFSBrowser(FileSystemManager manager, URI rootFolder, URI homeFolder) throws FileSystemException {
        this(manager,
            rootFolder == null ? null : rootFolder.toString(),
            homeFolder == null ? null : homeFolder.toString());
    }

    /**
     * Creates a new VFSBrowser <p> Note that this version of the constructor
     * must be used when a custom root folder has to be set.
     *
     * @param manager The virtual file system we're going to manage.
     * @param rootFolder The root folder full name.
     * @param homeFolder The home folder full name.
     */
    public VFSBrowser(FileSystemManager manager, String rootFolder, String homeFolder) throws FileSystemException {
        if (rootFolder == null) {
            throw new IllegalArgumentException("Root folder is null.");
        }

        // Note: these methods all could trigger events, but since we're
        // in the constructor and the skin isn't set yet, there will not
        // be any listeners registered yet
        setManager(manager);
        setRootDirectory(rootFolder);
        setHomeDirectory(homeFolder == null ? USER_HOME.toString() : homeFolder);

        installSkin(VFSBrowser.class);
    }

    /**
     * Returns the current file system manager.
     *
     * @return The current file system manager.
     */
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
            fileBrowserListeners.managerChanged(this, previousManager);
        }
    }

    /**
     * Returns the current root directory.
     *
     * @return The current root directory.
     */
    public FileObject getRootDirectory() {
        return rootDirectory;
    }

    /**
     * Sets the root directory from a string. Clears any existing file
     * selection.
     *
     * @param rootDirectory
     */
    public void setRootDirectory(String rootDirectory) throws FileSystemException {
        setRootDirectory(manager.resolveFile(rootDirectory));
    }

    /**
     * Sets the root directory. Clears any existing file selection.
     *
     * @param rootDirectory
     */
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
                fileBrowserListeners.rootDirectoryChanged(this, previousRootDirectory);
            }
        } else {
            setRootDirectory(rootDirectory.getParent());
        }
    }

    /**
     * Returns the current home directory.
     */
    public FileObject getHomeDirectory() {
        return homeDirectory;
    }

    /**
     * Sets the home directory from a string.
     *
     * @param homeDirectory
     */
    public void setHomeDirectory(String homeDirectory) throws FileSystemException {
        setHomeDirectory(manager.resolveFile(homeDirectory));
    }

    /**
     * Sets the home directory.
     *
     * @param homeDirectory
     */
    public void setHomeDirectory(FileObject homeDirectory) throws FileSystemException {
        if (homeDirectory == null) {
            throw new IllegalArgumentException("Home directory is null.");
        }

        // Give some grace to set the home folder to an actual file and
        // have it work (by using the parent folder instead)
        if (homeDirectory.getType() != FileType.FOLDER) {
            homeDirectory = homeDirectory.getParent();
            if (homeDirectory == null || homeDirectory.getType() != FileType.FOLDER) {
                throw new IllegalArgumentException("Home file is not a directory.");
            }
        }

        if (homeDirectory.exists()) {
            FileObject previousHomeDirectory = this.homeDirectory;

            if (!homeDirectory.equals(previousHomeDirectory)) {
                this.homeDirectory = homeDirectory;
                fileBrowserListeners.homeDirectoryChanged(this, previousHomeDirectory);
            }
        } else {
            setHomeDirectory(homeDirectory.getParent());
        }
    }

    /**
     * Adds a file to the file selection.
     *
     * @param file
     * @return <tt>true</tt> if the file was added; <tt>false</tt> if it was
     * already selected.
     */
    public boolean addSelectedFile(FileObject file) throws FileSystemException {
        if (file == null) {
            throw new IllegalArgumentException();
        }

        // TODO: is this a good way to do this?
        // if (file.isAbsolute()) {
        if (baseFileName != null && baseFileName.isAncestor(file.getName())) {
            if (!file.getParent().equals(rootDirectory)) {
                throw new IllegalArgumentException();
            }
        } else {
            file = manager.resolveFile(rootDirectory, file.getName().getBaseName());
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
     * @return <tt>true</tt> if the file was removed; <tt>false</tt> if it was
     * not already selected.
     */
    public boolean removeSelectedFile(FileObject file) {
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
     * @return The currently selected file.
     */
    public FileObject getSelectedFile() {
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
    public void setSelectedFile(FileObject file) throws FileSystemException {
        if (file == null) {
            clearSelection();
        } else {
            // TODO: adequate replacement for "isAbsolute"?
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
     */
    public Sequence<FileObject> setSelectedFiles(Sequence<FileObject> selectedFiles)
        throws FileSystemException {
        if (selectedFiles == null) {
            throw new IllegalArgumentException("selectedFiles is null.");
        }

        if (!multiSelect && selectedFiles.getLength() > 1) {
            throw new IllegalArgumentException("Multi-select is not enabled.");
        }

        // Update the selection
        Sequence<FileObject> previousSelectedFiles = getSelectedFiles();

        FileObjectList fileList = new FileObjectList();
        for (int i = 0, n = selectedFiles.getLength(); i < n; i++) {
            FileObject file = selectedFiles.get(i);

            if (file == null) {
                throw new IllegalArgumentException("file is null.");
            }

            // TODO: is this correct?
            // if (!file.isAbsolute()) {
            if (baseFileName == null || !baseFileName.isAncestor(file.getName())) {
                file = manager.resolveFile(rootDirectory, file.getName().getBaseName());
            }

            // TODO: don't do this for now -- revisit later
            // if (!file.getParent().equals(rootDirectory)) {
            // throw new IllegalArgumentException();
            // }

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
    public void clearSelection() throws FileSystemException {
        setSelectedFiles(new ArrayList<FileObject>());
    }

    public boolean isFileSelected(FileObject file) {
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
    public Filter<FileObject> getDisabledFileFilter() {
        return disabledFileFilter;
    }

    /**
     * Sets the file filter.
     *
     * @param disabledFileFilter The file filter to use, or <tt>null</tt> for no
     * filter.
     */
    public void setDisabledFileFilter(Filter<FileObject> disabledFileFilter) {
        Filter<FileObject> previousDisabledFileFilter = this.disabledFileFilter;

        if (previousDisabledFileFilter != disabledFileFilter) {
            this.disabledFileFilter = disabledFileFilter;
            fileBrowserListeners.disabledFileFilterChanged(this, previousDisabledFileFilter);
        }
    }

    public FileObject getFileAt(int x, int y) {
        VFSBrowser.Skin fileBrowserSkin = (VFSBrowser.Skin) getSkin();
        return fileBrowserSkin.getFileAt(x, y);
    }

    public ListenerList<VFSBrowserListener> getFileBrowserListeners() {
        return fileBrowserListeners;
    }
}
