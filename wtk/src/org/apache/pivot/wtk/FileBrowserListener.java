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
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.ListenerList;

/**
 * File browser listener interface.
 */
public interface FileBrowserListener {
    /**
     * File browser listeners.
     */
    public static class Listeners extends ListenerList<FileBrowserListener>
        implements FileBrowserListener {
        @Override
        public void rootDirectoryChanged(FileBrowser fileBrowser, File previousRootDirectory) {
            forEach(listener -> listener.rootDirectoryChanged(fileBrowser, previousRootDirectory));
        }

        @Override
        public void selectedFileAdded(FileBrowser fileBrowser, File file) {
            forEach(listener -> listener.selectedFileAdded(fileBrowser, file));
        }

        @Override
        public void selectedFileRemoved(FileBrowser fileBrowser, File file) {
            forEach(listener -> listener.selectedFileRemoved(fileBrowser, file));
        }

        @Override
        public void selectedFilesChanged(FileBrowser fileBrowser,
            Sequence<File> previousSelectedFiles) {
            forEach(listener -> listener.selectedFilesChanged(fileBrowser, previousSelectedFiles));
        }

        @Override
        public void multiSelectChanged(FileBrowser fileBrowser) {
            forEach(listener -> listener.multiSelectChanged(fileBrowser));
        }

        @Override
        public void disabledFileFilterChanged(FileBrowser fileBrowser,
            Filter<File> previousDisabledFileFilter) {
            forEach(listener -> listener.disabledFileFilterChanged(fileBrowser, previousDisabledFileFilter));
        }
    }

    /**
     * File browser listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements FileBrowserListener {
        @Override
        public void rootDirectoryChanged(FileBrowser fileBrowser, File previousRootDirectory) {
            // empty block
        }

        @Override
        public void selectedFileAdded(FileBrowser fileBrowser, File file) {
            // empty block
        }

        @Override
        public void selectedFileRemoved(FileBrowser fileBrowser, File file) {
            // empty block
        }

        @Override
        public void selectedFilesChanged(FileBrowser fileBrowser,
            Sequence<File> previousSelectedFiles) {
            // empty block
        }

        @Override
        public void multiSelectChanged(FileBrowser fileBrowser) {
            // empty block
        }

        @Override
        public void disabledFileFilterChanged(FileBrowser fileBrowser,
            Filter<File> previousDisabledFileFilter) {
            // empty block
        }
    }

    /**
     * Called when a file browser's root directory has changed.
     *
     * @param fileBrowser           The file browser that has changed.
     * @param previousRootDirectory The previous root directory of the browser.
     */
    default void rootDirectoryChanged(FileBrowser fileBrowser, File previousRootDirectory) {
    }

    /**
     * Called when a file has been added to a file browser's selection.
     *
     * @param fileBrowser The file browser that has changed.
     * @param file        The newly selected file.
     */
    default void selectedFileAdded(FileBrowser fileBrowser, File file) {
    }

    /**
     * Called when a file has been removed from a file browser's selection.
     *
     * @param fileBrowser The file browser that has changed.
     * @param file        The file that was just unselected.
     */
    default void selectedFileRemoved(FileBrowser fileBrowser, File file) {
    }

    /**
     * Called when a file browser's selection state has been reset.
     *
     * @param fileBrowser           The file browser that has changed.
     * @param previousSelectedFiles The complete sequence of files that used to be selected.
     */
    default void selectedFilesChanged(FileBrowser fileBrowser, Sequence<File> previousSelectedFiles) {
    }

    /**
     * Called when a file browser's multi-select flag has changed.
     *
     * @param fileBrowser The file browser that has changed.
     */
    default void multiSelectChanged(FileBrowser fileBrowser) {
    }

    /**
     * Called when a file browser's file filter has changed.
     *
     * @param fileBrowser                The file browser that has changed.
     * @param previousDisabledFileFilter The previous disabled file filter.
     */
    default void disabledFileFilterChanged(FileBrowser fileBrowser,
        Filter<File> previousDisabledFileFilter) {
    }
}
