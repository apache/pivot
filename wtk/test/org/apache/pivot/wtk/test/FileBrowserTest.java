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
package org.apache.pivot.wtk.test;

import java.io.File;
import java.util.Comparator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.io.Folder;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FileBrowser;
import org.apache.pivot.wtk.FileBrowserListener;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;

public class FileBrowserTest implements Application {
    private Window window = null;

    @WTKX private FileBrowser fileBrowser = null;
    @WTKX private Label selectedFolderLabel = null;
    @WTKX private ListView filesListView = null;

    private ArrayList<File> selectedFiles = new ArrayList<File>();

    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(getClass().getResource("file_browser_test.wtkx"));

        wtkxSerializer.bind(this, FileBrowserTest.class);

        final Comparator<File> filePathComparator = new Comparator<File>() {
            public int compare(File file1, File file2) {
                String path1 = file1.getPath();
                String path2 = file2.getPath();

                return path1.compareTo(path2);
            }
        };

        fileBrowser.getFileBrowserListeners().add(new FileBrowserListener.Adapter() {
            public void selectedFolderChanged(FileBrowser fileBrowser, Folder previousSelectedFolder) {
                updateSelectedFolder();
            }

            public void selectedFileAdded(FileBrowser fileBrowser, File file) {
                int index = ArrayList.binarySearch(selectedFiles, file, filePathComparator);
                if (index < 0) {
                    index = -(index + 1);
                }

                selectedFiles.insert(file, index);
            }

            public void selectedFileRemoved(FileBrowser fileBrowser, File file) {
                int index = ArrayList.binarySearch(selectedFiles, file, filePathComparator);
                if (index < 0) {
                    index = -(index + 1);
                }

                selectedFiles.remove(index, 1);
            }

            public void selectedFilesChanged(FileBrowser fileBrowser, Sequence<File> previousSelectedFiles) {
                updateSelectedFiles();
            }
        });

        updateSelectedFolder();

        window.open(display);

        fileBrowser.requestFocus();
    }

    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public void suspend() {
    }

    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(FileBrowserTest.class, args);
    }

    private void updateSelectedFolder() {
        selectedFolderLabel.setText(fileBrowser.getSelectedFolder().getPath());

        updateSelectedFiles();
    }

    private void updateSelectedFiles() {
        selectedFiles = new ArrayList<File>(fileBrowser.getSelectedFiles());
        filesListView.setListData(selectedFiles);
    }
}
