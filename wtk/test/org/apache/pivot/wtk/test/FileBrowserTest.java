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

import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.io.Folder;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.TreeViewBranchListener;
import org.apache.pivot.wtkx.WTKXSerializer;

public class FileBrowserTest implements Application {
    private Frame frame = null;

    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        frame = new Frame((Component)wtkxSerializer.readObject(getClass().getResource("file_browser_test.wtkx")));

        TreeView folderTreeView = (TreeView)wtkxSerializer.get("folderTreeView");

        String pathname = "/";
        final Folder rootFolder = new Folder(pathname);
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

        frame.setTitle("File Browser Test");
        frame.setPreferredSize(480, 640);
        frame.open(display);
    }

    public boolean shutdown(boolean optional) {
        if (frame != null) {
            frame.close();
        }

        return false;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
