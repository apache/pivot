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
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewSelectionListener;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.TreeViewSelectionListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.TreeBranch;
import org.apache.pivot.wtk.content.TreeNode;

public class Pivot718 implements Application {

    private Window window = null;
    private TreeView tree;
    private PushButton treeDelButton;
    private ListView list;
    private PushButton listDelButton;

    public Pivot718() {

    }

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window) bxmlSerializer.readObject(Pivot718.class, "pivot_718.bxml");

        controlTree(bxmlSerializer);
        controlList(bxmlSerializer);

        window.open(display);
    }

    private void controlList(BXMLSerializer bxmlSerializer) {
        listDelButton = (PushButton) bxmlSerializer.getNamespace().get("listDelButton");
        list = (ListView) bxmlSerializer.getNamespace().get("list");
        list.getListViewSelectionListeners().add(new ListViewSelectionListener() {

            @Override
            public void selectedRangeAdded(ListView listView, int rangeStart, int rangeEnd) {
                System.out.println("selectedRangeAdded");
            }

            @Override
            public void selectedRangeRemoved(ListView listView, int rangeStart, int rangeEnd) {
                System.out.println("selectedRangeRemoved");
            }

            @Override
            public void selectedRangesChanged(ListView listView,
                Sequence<Span> previousSelectedRanges) {
                System.out.println("selectedRangesChanged");
            }

            @Override
            public void selectedItemChanged(ListView listView, Object previousSelectedItem) {
                System.out.println("selectedItemChanged :::" + listView.getSelectedItem());
            }
        });
        listDelButton.getButtonPressListeners().add(new ButtonPressListener() {

            @Override
            public void buttonPressed(Button button) {
                Object x = list.getSelectedItem();
                System.out.println("delete :: " + x);
                // List data = list.getListData();
                @SuppressWarnings("unchecked")
                List<Object> data = (List<Object>) list.getListData();
                data.remove(x);
            }
        });
    }

    private void controlTree(BXMLSerializer bxmlSerializer) {
        treeDelButton = (PushButton) bxmlSerializer.getNamespace().get("treeDelButton");
        tree = (TreeView) bxmlSerializer.getNamespace().get("tree");
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
        treeDelButton.getButtonPressListeners().add(new ButtonPressListener() {

            @Override
            public void buttonPressed(Button button) {
                TreeNode selectedNode = (TreeNode) tree.getSelectedNode();
                System.out.println("delete :: " + selectedNode);
                if (selectedNode != null) {
                    TreeBranch parent = selectedNode.getParent();
                    if (parent != null) {
                        parent.remove(selectedNode);
                    }
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
        DesktopApplicationContext.main(Pivot718.class, args);
    }

}
