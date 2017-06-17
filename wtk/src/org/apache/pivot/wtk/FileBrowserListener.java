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

/**
 * File browser listener interface.
 */
public interface FileBrowserListener {
    /**
     * File browser listener adapter.
     */
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
    public void rootDirectoryChanged(FileBrowser fileBrowser, File previousRootDirectory);

    /**
     * Called when a file has been added to a file browser's selection.
     *
     * @param fileBrowser The file browser that has changed.
     * @param file        The newly selected file.
     */
    public void selectedFileAdded(FileBrowser fileBrowser, File file);

    /**
     * Called when a file has been removed from a file browser's selection.
     *
     * @param fileBrowser The file browser that has changed.
     * @param file        The file that was just unselected.
     */
    public void selectedFileRemoved(FileBrowser fileBrowser, File file);

    /**
     * Called when a file browser's selection state has been reset.
     *
     * @param fileBrowser           The file browser that has changed.
     * @param previousSelectedFiles The complete sequence of files that used to be selected.
     */
    public void selectedFilesChanged(FileBrowser fileBrowser, Sequence<File> previousSelectedFiles);

    /**
     * Called when a file browser's multi-select flag has changed.
     *
     * @param fileBrowser The file browser that has changed.
     */
    public void multiSelectChanged(FileBrowser fileBrowser);

    /**
     * Called when a file browser's file filter has changed.
     *
     * @param fileBrowser                The file browser that has changed.
     * @param previousDisabledFileFilter The previous disabled file filter.
     */
    public void disabledFileFilterChanged(FileBrowser fileBrowser,
        Filter<File> previousDisabledFileFilter);
}
