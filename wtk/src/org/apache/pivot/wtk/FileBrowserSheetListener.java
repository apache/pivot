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
 */
public interface FileBrowserSheetListener {
    /**
     * File browser sheet listener adapter.
     *
     */
    public static class Adapter implements FileBrowserSheetListener {
        public void multiSelectChanged(FileBrowserSheet fileBrowserSheet) {
        }

        public void selectedFolderChanged(FileBrowserSheet fileBrowserSheet, Folder previousSelectedFolder) {
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
     * Called when a file browser sheet's selected folder has changed.
     *
     * @param fileBrowserSheet
     * @param previousSelectedFolder
     */
    public void selectedFolderChanged(FileBrowserSheet fileBrowserSheet, Folder previousSelectedFolder);

    /**
     * Called when a file browser sheet's selection state has been reset.
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
