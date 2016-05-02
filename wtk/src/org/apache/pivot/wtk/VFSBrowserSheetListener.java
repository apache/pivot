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

/**
 * Commons VFS browser sheet listener interface.
 */
public interface VFSBrowserSheetListener {
    /**
     * Commons VFS browser sheet listener adapter.
     */
    public static class Adapter implements VFSBrowserSheetListener {
        @Override
        public void managerChanged(VFSBrowserSheet fileBrowserSheet,
            FileSystemManager previousManager) {
            // empty block
        }

        @Override
        public void modeChanged(VFSBrowserSheet fileBrowserSheet, VFSBrowserSheet.Mode previousMode) {
            // empty block
        }

        @Override
        public void rootDirectoryChanged(VFSBrowserSheet fileBrowserSheet,
            FileObject previousRootDirectory) {
            // empty block
        }

        @Override
        public void homeDirectoryChanged(VFSBrowserSheet fileBrowserSheet,
            FileObject previousHomeDirectory) {
            // empty block
        }

        @Override
        public void selectedFilesChanged(VFSBrowserSheet fileBrowserSheet,
            Sequence<FileObject> previousSelectedFiles) {
            // empty block
        }

        @Override
        public void disabledFileFilterChanged(VFSBrowserSheet fileBrowserSheet,
            Filter<FileObject> previousDisabledFileFilter) {
            // empty block
        }
    }

    /**
     * Called when a file browser's file system manager has changed (as when
     * browsing into a new virtual file system).
     *
     * @param fileBrowserSheet The source of this event.
     * @param previousManager  The previous file manager that was set.
     */
    public void managerChanged(VFSBrowserSheet fileBrowserSheet, FileSystemManager previousManager);

    /**
     * Called when a file browser sheet's mode has changed.
     *
     * @param fileBrowserSheet The source of this event.
     * @param previousMode     The previous mode for this browser.
     */
    public void modeChanged(VFSBrowserSheet fileBrowserSheet, VFSBrowserSheet.Mode previousMode);

    /**
     * Called when a file browser sheet's root directory has changed.
     *
     * @param fileBrowserSheet      The browser sheet that has changed.
     * @param previousRootDirectory The previously set root directory for this browser.
     */
    public void rootDirectoryChanged(VFSBrowserSheet fileBrowserSheet,
        FileObject previousRootDirectory);

    /**
     * Called when a file browser sheet's home directory has changed.
     * <p> Usually this would only be when you browse to another file system.
     *
     * @param fileBrowserSheet      The browser sheet that has changed.
     * @param previousHomeDirectory The previously set home directory for this browser.
     */
    public void homeDirectoryChanged(VFSBrowserSheet fileBrowserSheet,
        FileObject previousHomeDirectory);

    /**
     * Called when a file browser sheet's selection state has been reset.
     *
     * @param fileBrowserSheet      The source of this event.
     * @param previousSelectedFiles The previous sequence of selected files.
     */
    public void selectedFilesChanged(VFSBrowserSheet fileBrowserSheet,
        Sequence<FileObject> previousSelectedFiles);

    /**
     * Called when a file browser sheet's disabled file filter has changed.
     *
     * @param fileBrowserSheet           The source of this event.
     * @param previousDisabledFileFilter The previous filter for disabled files.
     */
    public void disabledFileFilterChanged(VFSBrowserSheet fileBrowserSheet,
        Filter<FileObject> previousDisabledFileFilter);
}
