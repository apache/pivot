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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;

/**
 * File browser sheet.
 *
 * @author gbrown
 */
public class FileBrowserSheet extends Sheet {
    private static class FileBrowserSheetListenerList extends ListenerList<FileBrowserSheetListener>
        implements FileBrowserSheetListener {
        public void fileNameInputLabelChanged(FileBrowserSheet fileBrowserSheet, String previousFileNameInputLabel) {
            for (FileBrowserSheetListener listener : this) {
                listener.fileNameInputLabelChanged(fileBrowserSheet, previousFileNameInputLabel);
            }
        }

        public void showFileNameInputChanged(FileBrowserSheet fileBrowserSheet) {
            for (FileBrowserSheetListener listener : this) {
                listener.showFileNameInputChanged(fileBrowserSheet);
            }
        }

        public void selectedFileChanged(FileBrowserSheet fileBrowserSheet, File previousSelectedFile) {
            for (FileBrowserSheetListener listener : this) {
                listener.selectedFileChanged(fileBrowserSheet, previousSelectedFile);
            }
        }
    }


    private ArrayList<FileBrowser.FilterSpecification> filterSpecifications;
    private String fileNameInputLabel;

    private boolean showFileNameInput = false;
    private File selectedFile = null;

    private FileBrowserSheetListenerList fileBrowserSheetListeners = new FileBrowserSheetListenerList();

    public FileBrowserSheet() {
        this(null);
    }
    public FileBrowserSheet(Sequence<FileBrowser.FilterSpecification> filterSpecifications) {
        this.filterSpecifications = new ArrayList<FileBrowser.FilterSpecification>(filterSpecifications);

        installSkin(FileBrowserSheet.class);
    }

    public int getFilterSpecificationCount() {
        return filterSpecifications.getLength();
    }

    public FileBrowser.FilterSpecification getFilterSpecification(int index) {
        return filterSpecifications.get(index);
    }

    public String getFileNameInputLabel() {
        return fileNameInputLabel;
    }

    public void setFileNameInputLabel(String fileNameInputLabel) {
        String previousFileNameInputLabel = this.fileNameInputLabel;

        if (fileNameInputLabel != previousFileNameInputLabel) {
            this.fileNameInputLabel = fileNameInputLabel;
            fileBrowserSheetListeners.fileNameInputLabelChanged(this, previousFileNameInputLabel);
        }
    }

    public boolean getShowFileNameInput() {
        return showFileNameInput;
    }

    public void setShowFileNameInput(boolean showFileNameInput) {
        if (this.showFileNameInput != showFileNameInput) {
            this.showFileNameInput = showFileNameInput;
            fileBrowserSheetListeners.showFileNameInputChanged(this);
        }
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
}
