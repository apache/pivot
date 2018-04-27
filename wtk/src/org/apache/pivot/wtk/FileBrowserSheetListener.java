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
 * File browser sheet listener interface.
 */
public interface FileBrowserSheetListener {
    /**
     * File browser sheet listeners.
     */
    public static class Listeners extends ListenerList<FileBrowserSheetListener>
        implements FileBrowserSheetListener {
        @Override
        public void modeChanged(FileBrowserSheet fileBrowserSheet,
            FileBrowserSheet.Mode previousMode) {
            forEach(listener -> listener.modeChanged(fileBrowserSheet, previousMode));
        }

        @Override
        public void rootDirectoryChanged(FileBrowserSheet fileBrowserSheet,
            File previousRootDirectory) {
            forEach(listener -> listener.rootDirectoryChanged(fileBrowserSheet, previousRootDirectory));
        }

        @Override
        public void selectedFilesChanged(FileBrowserSheet fileBrowserSheet,
            Sequence<File> previousSelectedFiles) {
            forEach(listener -> listener.selectedFilesChanged(fileBrowserSheet, previousSelectedFiles));
        }

        @Override
        public void disabledFileFilterChanged(FileBrowserSheet fileBrowserSheet,
            Filter<File> previousDisabledFileFilter) {
            forEach(listener -> listener.disabledFileFilterChanged(fileBrowserSheet, previousDisabledFileFilter));
        }
    }

    /**
     * File browser sheet listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
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
     * @param fileBrowserSheet The browser sheet that has changed.
     * @param previousMode     The previous mode that was in effect.
     */
    default void modeChanged(FileBrowserSheet fileBrowserSheet, FileBrowserSheet.Mode previousMode) {
    }

    /**
     * Called when a file browser sheet's root directory has changed.
     *
     * @param fileBrowserSheet      The browser sheet that has changed.
     * @param previousRootDirectory The previous root directory that was being browsed.
     */
    default void rootDirectoryChanged(FileBrowserSheet fileBrowserSheet, File previousRootDirectory) {
    }

    /**
     * Called when a file browser sheet's selection state has been reset.
     *
     * @param fileBrowserSheet      The browser sheet that has changed.
     * @param previousSelectedFiles The complete sequence of files that used to be selected.
     */
    default void selectedFilesChanged(FileBrowserSheet fileBrowserSheet,
        Sequence<File> previousSelectedFiles) {
    }

    /**
     * Called when a file browser sheet's disabled file filter has changed.
     *
     * @param fileBrowserSheet           The browser sheet that has changed.
     * @param previousDisabledFileFilter The previous disabled file filter.
     */
    default void disabledFileFilterChanged(FileBrowserSheet fileBrowserSheet,
        Filter<File> previousDisabledFileFilter) {
    }
}
