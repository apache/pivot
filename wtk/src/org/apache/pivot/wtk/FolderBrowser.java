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

import org.apache.pivot.io.Folder;
import org.apache.pivot.util.ListenerList;

/**
 * Component representing a folder browser.
 *
 * @author gbrown
 */
public class FolderBrowser extends Container {
    private static class FolderBrowserListenerList extends ListenerList<FolderBrowserListener>
        implements FolderBrowserListener {
        public void selectedFolderChanged(FolderBrowser folderBrowser, Folder previousSelectedFolder) {
            for (FolderBrowserListener listener : this) {
                listener.selectedFolderChanged(folderBrowser, previousSelectedFolder);
            }
        }
    }

    private Folder selectedFolder = null;

    private FolderBrowserListenerList folderBrowserListeners = new FolderBrowserListenerList();

    public FolderBrowser() {
        installSkin(FolderBrowser.class);
    }

    public Folder getSelectedFolder() {
        return selectedFolder;
    }

    public void setSelectedFolder(Folder selectedFolder) {
        Folder previousSelectedFolder = this.selectedFolder;

        if (selectedFolder != previousSelectedFolder) {
            this.selectedFolder = selectedFolder;
            folderBrowserListeners.selectedFolderChanged(this, previousSelectedFolder);
        }
    }

    public ListenerList<FolderBrowserListener> getFolderBrowserListeners() {
        return folderBrowserListeners;
    }
}
