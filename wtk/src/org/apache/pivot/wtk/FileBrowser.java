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

import org.apache.pivot.util.ListenerList;

/**
 * Component representing a file browser.
 *
 * @author gbrown
 */
public class FileBrowser extends Container {
    private static class FileBrowserListenerList extends ListenerList<FileBrowserListener>
        implements FileBrowserListener {
        public void selectedFileChanged(FileBrowser fileBrowser, File previousSelectedFile) {
            for (FileBrowserListener listener : this) {
                listener.selectedFileChanged(fileBrowser, previousSelectedFile);
            }
        }
    }

    private File selectedFile = null;

    private FileBrowserListenerList fileBrowserListeners = new FileBrowserListenerList();

    public FileBrowser() {
        installSkin(FileBrowser.class);
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public void setSelectedFile(File selectedFile) {
        File previousSelectedFile = this.selectedFile;

        if (selectedFile != previousSelectedFile) {
            this.selectedFile = selectedFile;
            fileBrowserListeners.selectedFileChanged(this, previousSelectedFile);
        }
    }

    public ListenerList<FileBrowserListener> getFileBrowserListeners() {
        return fileBrowserListeners;
    }
}
