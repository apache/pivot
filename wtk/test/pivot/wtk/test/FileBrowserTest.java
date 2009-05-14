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
package pivot.wtk.test;

import pivot.collections.Dictionary;
import pivot.collections.Sequence;
import pivot.collections.Sequence.Tree.Path;
import pivot.io.Folder;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.Frame;
import pivot.wtk.TreeView;
import pivot.wtk.TreeViewBranchListener;
import pivot.wtkx.WTKXSerializer;

public class FileBrowserTest implements Application {
    private Frame frame = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        frame = new Frame((Component)wtkxSerializer.readObject(getClass().getResource("file_browser_test.wtkx")));

        TreeView folderTreeView = (TreeView)wtkxSerializer.getObjectByID("folderTreeView");

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
        frame.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
