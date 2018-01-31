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

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.ListenerList;

/**
 * Commons VFS browser listener interface.
 */
public interface VFSBrowserListener {
    /**
     * VFS Browser listeners.
     */
    public static class Listeners extends ListenerList<VFSBrowserListener>
        implements VFSBrowserListener {
        @Override
        public void managerChanged(VFSBrowser fileBrowser, FileSystemManager previousManager) {
            forEach(listener -> listener.managerChanged(fileBrowser, previousManager));
        }

        @Override
        public void rootDirectoryChanged(VFSBrowser fileBrowser, FileObject previousRootDirectory) {
            forEach(listener -> listener.rootDirectoryChanged(fileBrowser, previousRootDirectory));
        }

        @Override
        public void homeDirectoryChanged(VFSBrowser fileBrowser, FileObject previousHomeDirectory) {
            forEach(listener -> listener.homeDirectoryChanged(fileBrowser, previousHomeDirectory));
        }

        @Override
        public void selectedFileAdded(VFSBrowser fileBrowser, FileObject file) {
            forEach(listener -> listener.selectedFileAdded(fileBrowser, file));
        }

        @Override
        public void selectedFileRemoved(VFSBrowser fileBrowser, FileObject file) {
            forEach(listener -> listener.selectedFileRemoved(fileBrowser, file));
        }

        @Override
        public void selectedFilesChanged(VFSBrowser fileBrowser,
            Sequence<FileObject> previousSelectedFiles) {
            forEach(listener -> listener.selectedFilesChanged(fileBrowser, previousSelectedFiles));
        }

        @Override
        public void multiSelectChanged(VFSBrowser fileBrowser) {
            forEach(listener -> listener.multiSelectChanged(fileBrowser));
        }

        @Override
        public void disabledFileFilterChanged(VFSBrowser fileBrowser,
            Filter<FileObject> previousDisabledFileFilter) {
            forEach(listener -> listener.disabledFileFilterChanged(fileBrowser, previousDisabledFileFilter));
        }
    }

    /**
     * Commons VFS browser listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements VFSBrowserListener {
        @Override
        public void managerChanged(VFSBrowser fileBrowser, FileSystemManager previousManager) {
            // empty block
        }

        @Override
        public void rootDirectoryChanged(VFSBrowser fileBrowser, FileObject previousRootDirectory) {
            // empty block
        }

        @Override
        public void homeDirectoryChanged(VFSBrowser fileBrowser, FileObject previousHomeDirectory) {
            // empty block
        }

        @Override
        public void selectedFileAdded(VFSBrowser fileBrowser, FileObject file) {
            // empty block
        }

        @Override
        public void selectedFileRemoved(VFSBrowser fileBrowser, FileObject file) {
            // empty block
        }

        @Override
        public void selectedFilesChanged(VFSBrowser fileBrowser,
            Sequence<FileObject> previousSelectedFiles) {
            // empty block
        }

        @Override
        public void multiSelectChanged(VFSBrowser fileBrowser) {
            // empty block
        }

        @Override
        public void disabledFileFilterChanged(VFSBrowser fileBrowser,
            Filter<FileObject> previousDisabledFileFilter) {
            // empty block
        }
    }

    /**
     * Called when a file browser's FileSystemManager has changed, (such as when
     * a nested VirtualFileSystem is opened).
     *
     * @param fileBrowser     The browser that has changed.
     * @param previousManager The previous file manager for this browser.
     */
    default public void managerChanged(VFSBrowser fileBrowser, FileSystemManager previousManager) {
    }

    /**
     * Called when a file browser's root directory has changed.
     *
     * @param fileBrowser           The browser that has changed.
     * @param previousRootDirectory The previous root directory that we were browsing.
     */
    default public void rootDirectoryChanged(VFSBrowser fileBrowser, FileObject previousRootDirectory) {
    }

    /**
     * Called when a file browser's home directory has changed.
     *
     * @param fileBrowser           The browser that has changed.
     * @param previousHomeDirectory The previous home directory.
     */
    default public void homeDirectoryChanged(VFSBrowser fileBrowser, FileObject previousHomeDirectory) {
    }

    /**
     * Called when a file has been added to a file browser's selection.
     *
     * @param fileBrowser The source of this event.
     * @param file        The file newly added to the selection.
     */
    default public void selectedFileAdded(VFSBrowser fileBrowser, FileObject file) {
    }

    /**
     * Called when a file has been removed from a file browser's selection.
     *
     * @param fileBrowser The source of this event.
     * @param file        The file just removed from the selection.
     */
    default public void selectedFileRemoved(VFSBrowser fileBrowser, FileObject file) {
    }

    /**
     * Called when a file browser's selection state has been reset.
     *
     * @param fileBrowser           The source of this event.
     * @param previousSelectedFiles The sequence of files that were previously selected.
     */
    default public void selectedFilesChanged(VFSBrowser fileBrowser,
        Sequence<FileObject> previousSelectedFiles) {
    }

    /**
     * Called when a file browser's multi-select flag has changed.
     *
     * @param fileBrowser The browser that has changed selection modes.
     */
    default public void multiSelectChanged(VFSBrowser fileBrowser) {
    }

    /**
     * Called when a file browser's file filter has changed.
     *
     * @param fileBrowser                The source of this event.
     * @param previousDisabledFileFilter The previous filter for disabled files (if any).
     */
    default public void disabledFileFilterChanged(VFSBrowser fileBrowser,
        Filter<FileObject> previousDisabledFileFilter) {
    }
}
