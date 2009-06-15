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
package org.apache.pivot.tutorials.filebrowser;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;

import pivot.collections.Dictionary;
import pivot.collections.Sequence;
import pivot.collections.Sequence.Tree.Path;
import pivot.io.Folder;
import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Component;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.TreeView;
import pivot.wtk.TreeViewBranchListener;
import pivot.wtk.Window;
import pivot.wtkx.WTKX;
import pivot.wtkx.WTKXSerializer;

public class FileBrowser implements Application {
    private Window window = null;

    @WTKX private TreeView folderTreeView;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "file_browser.wtkx");
        wtkxSerializer.bind(this, FileBrowser.class);

        String pathname = System.getProperty("user.home");
        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {
                return true; // (file.isDirectory());
            }
        };

        final Folder rootFolder = new Folder(pathname, fileFilter);
        rootFolder.refresh();
        folderTreeView.setTreeData(rootFolder);

        folderTreeView.getTreeViewBranchListeners().add(new TreeViewBranchListener() {
            public void branchExpanded(TreeView treeView, Path path) {
                Folder folder = (Folder)Sequence.Tree.get(rootFolder, path);
                folder.refresh();
            }

            public void branchCollapsed(TreeView treeView, Path path) {
                // No-op
            }
        });

        folderTreeView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter() {
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
                if (count == 2) {
                    openSelectedFile();
                }

                return false;
            }
        });

        folderTreeView.getComponentKeyListeners().add(new ComponentKeyListener() {
            public boolean keyTyped(Component component, char character) {
                return false;
            }

            public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
                if (keyCode == Keyboard.KeyCode.ENTER) {
                    openSelectedFile();
                }

                return false;
            }

            public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
                return false;
            }
        });

        window.open(display);
    }

    public boolean shutdown(boolean optional) throws Exception {
        if (window != null) {
            window.close();
        }

        window = null;

        return false;
    }

    public void suspend() {
    }

    public void resume() {
    }

    private void openSelectedFile() {
        File file = (File)folderTreeView.getSelectedNode();
        if (file != null) {
            try {
                ApplicationContext.open(file.toURI().toURL());
            } catch(MalformedURLException exception) {
                // No-op
            }
        }
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(FileBrowser.class, args);
    }
}
