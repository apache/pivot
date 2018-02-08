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
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.TreeViewSelectionListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.TreeBranch;

public class Pivot734 implements Application {

    private Window window = null;
    private TreeView tree;
    private PushButton treeButtonAdd;
    private PushButton treeButtonRemove;
    private TreeBranch newBranch = new TreeBranch("new branch");

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
        treeButtonAdd = (PushButton) bxmlSerializer.getNamespace().get("treeButtonAdd");
        treeButtonRemove = (PushButton) bxmlSerializer.getNamespace().get("treeButtonRemove");
        tree = (TreeView) bxmlSerializer.getNamespace().get("tree");
        boolean treeStyleForShowEmptyBranchControls = tree.getStyles().getBoolean(
            Style.showEmptyBranchControls);
        System.out.println("tree style for showEmptyBranchControls is "
            + treeStyleForShowEmptyBranchControls);

        tree.getTreeViewSelectionListeners().add(new TreeViewSelectionListener() {
            @Override
            public void selectedPathAdded(TreeView treeView, Path path) {
                System.out.println("selectedPathAdded");
            }

            @Override
            public void selectedPathRemoved(TreeView treeView, Path path) {
                System.out.println("selectedPathRemoved");
            }

            @Override
            public void selectedPathsChanged(TreeView treeView, Sequence<Path> previousSelectedPaths) {
                System.out.println("selectedPathsChanged");
            }

            @Override
            public void selectedNodeChanged(TreeView treeView, Object previousSelectedNode) {
                System.out.println("selectedNodeChanged");
            }
        });

        treeButtonAdd.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                Object x = tree.getSelectedNode();
                System.out.println("add a 'new branch' element to the selected element :: " + x);

                if (x != null && x instanceof TreeBranch) {
                    ((TreeBranch) x).add(newBranch);
                }

            }
        });

        treeButtonRemove.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                Object x = tree.getSelectedNode();
                System.out.println("remove a 'new branch' element under the selected element :: " + x);

                if (x != null && x instanceof TreeBranch) {
                    ((TreeBranch) x).remove(newBranch);
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

    public static void main(String[] args) {
        DesktopApplicationContext.main(Pivot734.class, args);
    }

}
