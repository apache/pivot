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
 * File browser sheet listener interface.
 */
public interface FileBrowserSheetListener {
    /**
     * File browser sheet listener adapter.
     */
    public static class Adapter implements FileBrowserSheetListener {
        @Override
        public void modeChanged(FileBrowserSheet fileBrowserSheet,
            FileBrowserSheet.Mode previousMode) {
            // empty block
        }

        @Override
        public void rootDirectoryChanged(FileBrowserSheet fileBrowserSheet,
            File previousRootDirectory) {
            // empty block
        }

        @Override
        public void selectedFilesChanged(FileBrowserSheet fileBrowserSheet,
            Sequence<File> previousSelectedFiles) {
            // empty block
        }

        @Override
        public void disabledFileFilterChanged(FileBrowserSheet fileBrowserSheet,
            Filter<File> previousDisabledFileFilter) {
            // empty block
        }
    }

    /**
     * Called when a file browser sheet's mode has changed.
     *
     * @param fileBrowserSheet
     * @param previousMode
     */
    public void modeChanged(FileBrowserSheet fileBrowserSheet, FileBrowserSheet.Mode previousMode);

    /**
     * Called when a file browser sheet's root directory has changed.
     *
     * @param fileBrowserSheet
     * @param previousRootDirectory
     */
    public void rootDirectoryChanged(FileBrowserSheet fileBrowserSheet,
        File previousRootDirectory);

    /**
     * Called when a file browser sheet's selection state has been reset.
     *
     * @param fileBrowserSheet
     * @param previousSelectedFiles
     */
    public void selectedFilesChanged(FileBrowserSheet fileBrowserSheet,
        Sequence<File> previousSelectedFiles);

    /**
     * Called when a file browser sheet's disabled file filter has changed.
     *
     * @param fileBrowserSheet
     * @param previousDisabledFileFilter
     */
    public void disabledFileFilterChanged(FileBrowserSheet fileBrowserSheet,
        Filter<File> previousDisabledFileFilter);
}
