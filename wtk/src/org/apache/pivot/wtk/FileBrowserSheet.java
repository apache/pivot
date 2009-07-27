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
import java.io.FileFilter;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Resources;

/**
 * File browser sheet.
 *
 * @author gbrown
 */
public class FileBrowserSheet extends Sheet {
    private static class FileBrowserSheetListenerList extends ListenerList<FileBrowserSheetListener>
        implements FileBrowserSheetListener {
        public void selectedFileChanged(FileBrowserSheet fileBrowserSheet, File previousSelectedFile) {
            for (FileBrowserSheetListener listener : this) {
                listener.selectedFileChanged(fileBrowserSheet, previousSelectedFile);
            }
        }
    }

    private String fileNameInputLabel;
    private ArrayList<FileBrowser.FilterSpecification> filterSpecifications;

    private File selectedFile = null;

    private FileBrowserSheetListenerList fileBrowserSheetListeners = new FileBrowserSheetListenerList();

    private static Resources resources = null;

    static {
        try {
            resources = new Resources(FileBrowserSheet.class.getName());
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public FileBrowserSheet() {
        this(null, null);
    }

    public FileBrowserSheet(String fileNameInputLabel) {
        this(fileNameInputLabel, null);
    }

    public FileBrowserSheet(Sequence<FileBrowser.FilterSpecification> filterSpecifications) {
        this(null, filterSpecifications);
    }

    public FileBrowserSheet(String fileNameInputLabel, Sequence<FileBrowser.FilterSpecification> filterSpecifications) {
        this.fileNameInputLabel = fileNameInputLabel;
        this.filterSpecifications = new ArrayList<FileBrowser.FilterSpecification>(filterSpecifications);

        installSkin(FileBrowserSheet.class);
    }

    public String getFileNameInputLabel() {
        return fileNameInputLabel;
    }

    public FileBrowser.FilterSpecification getFilterSpecification(int index) {
        return filterSpecifications.get(index);
    }

    public int getFilterSpecificationCount() {
        return filterSpecifications.getLength();
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public void setSelectedFile(File selectedFile) {
        File previousSelectedFile = this.selectedFile;

        if (selectedFile != previousSelectedFile) {
            this.selectedFile = selectedFile;
            fileBrowserSheetListeners.selectedFileChanged(this, previousSelectedFile);
        }
    }

    public ListenerList<FileBrowserSheetListener> getFileBrowserSheetListeners() {
        return fileBrowserSheetListeners;
    }

    public static void showFileOpen(Window owner) {
        showFileOpen(owner, null);
    }

    public static void showFileOpen(Window owner, Sequence<FileBrowser.FilterSpecification> filterSpecifications) {
        FileBrowserSheet fileBrowserSheet = new FileBrowserSheet(filterSpecifications);
        fileBrowserSheet.setTitle(resources.getString("openFile"));
        fileBrowserSheet.open(owner);
    }

    public static void showFileSave(Window owner) {
        FileBrowserSheet fileBrowserSheet = new FileBrowserSheet(resources.getString("saveAs"));
        fileBrowserSheet.setTitle(resources.getString("saveFile"));
        fileBrowserSheet.open(owner);
    }

    public static void showFolderSelect(Window owner) {
        FileFilter folderFilter = new FileFilter() {
            public boolean accept(File file) {
                return (file.isDirectory());
            }
        };

        ArrayList<FileBrowser.FilterSpecification> filterSpecifications =
            new ArrayList<FileBrowser.FilterSpecification>();
        filterSpecifications.add(new FileBrowser.FilterSpecification(resources.getString("allFolders"),
            folderFilter));

        FileBrowserSheet fileBrowserSheet = new FileBrowserSheet(filterSpecifications);
        fileBrowserSheet.setTitle(resources.getString("selectFolder"));
        fileBrowserSheet.open(owner);
    }
}
