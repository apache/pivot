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
import org.apache.pivot.io.Folder;
import org.apache.pivot.util.Filter;

/**
 * File browser sheet listener interface.
 *
 * @author gbrown
 */
public interface FileBrowserSheetListener {
    /**
     * File browser sheet listener adapter.
     *
     * @author gbrown
     */
    public static class Adapter implements FileBrowserSheetListener {
        public void multiSelectChanged(FileBrowserSheet fileBrowserSheet) {
        }

        public void selectedFolderChanged(FileBrowserSheet fileBrowserSheet, Folder previousSelectedFolder) {
        }

        public void selectedFileAdded(FileBrowserSheet fileBrowserSheet, File file) {
        }

        public void selectedFileRemoved(FileBrowserSheet fileBrowserSheet, File file) {
        }

        public void selectedFilesChanged(FileBrowserSheet fileBrowserSheet, Sequence<File> previousSelectedFiles) {
        }

        public void fileFilterChanged(FileBrowserSheet fileBrowserSheet, Filter<File> previousFileFilter) {
        }
    }

    /**
     * Called when a file browser sheet's multi-select flag has changed.
     *
     * @param fileBrowserSheet
     */
    public void multiSelectChanged(FileBrowserSheet fileBrowserSheet);

    /**
     * Called when a file browser's selected folder has changed.
     *
     * @param fileBrowserSheet
     * @param previousSelectedFolder
     */
    public void selectedFolderChanged(FileBrowserSheet fileBrowserSheet, Folder previousSelectedFolder);

    /**
     * Called when a file has been added to a file browser's selection.
     *
     * @param fileBrowserSheet
     * @param file
     */
    public void selectedFileAdded(FileBrowserSheet fileBrowserSheet, File file);

    /**
     * Called when a file has been removed from a file browser's selection.
     *
     * @param fileBrowserSheet
     * @param file
     */
    public void selectedFileRemoved(FileBrowserSheet fileBrowserSheet, File file);

    /**
     * Called when a file browser's selection state has been reset.
     *
     * @param fileBrowserSheet
     * @param previousSelectedFiles
     */
    public void selectedFilesChanged(FileBrowserSheet fileBrowserSheet, Sequence<File> previousSelectedFiles);

    /**
     * Called when a file browser sheet's file filter has changed.
     *
     * @param fileBrowserSheet
     * @param previousFileFilter
     */
    public void fileFilterChanged(FileBrowserSheet fileBrowserSheet, Filter<File> previousFileFilter);
}
