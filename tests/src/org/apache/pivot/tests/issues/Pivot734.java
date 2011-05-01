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
package org.apache.pivot.tests.issues;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.TreeViewSelectionListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.TreeBranch;

public class Pivot734 implements Application {

    private Window window = null;
    private TreeView tree;
    private PushButton treeAddButton;

    public Pivot734() {

    }

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window) bxmlSerializer.readObject(Pivot734.class, "pivot_734.bxml");
        controlTree(bxmlSerializer);
        window.open(display);
    }

    private void controlTree(BXMLSerializer bxmlSerializer) {
        treeAddButton = (PushButton) bxmlSerializer.getNamespace().get("treeAddButton");
        tree = (TreeView) bxmlSerializer.getNamespace().get("tree");
        tree.getTreeViewSelectionListeners().add(new TreeViewSelectionListener() {

            public void selectedPathAdded(TreeView treeView, Path path) {
                System.out.println("selectedPathAdded");
            }

            public void selectedPathRemoved(TreeView treeView, Path path) {
                System.out.println("selectedPathRemoved");
            }

            public void selectedPathsChanged(TreeView treeView, Sequence<Path> previousSelectedPaths) {
                System.out.println("selectedPathsChanged");
            }

            public void selectedNodeChanged(TreeView treeView, Object previousSelectedNode) {
                System.out.println("selectedNodeChanged");
            }
        });
        treeAddButton.getButtonPressListeners().add(new ButtonPressListener() {

            public void buttonPressed(Button button) {
                Object x = tree.getSelectedNode();
                System.out.println("add a new element to :: " + x);

                if (x != null && x instanceof TreeBranch)
                {
                    TreeBranch treeBranch = new TreeBranch("new branch");
                    ((TreeBranch)x).add(treeBranch);
                }

            }
        });
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Pivot734.class, args);
    }

}
