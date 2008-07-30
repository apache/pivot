/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk.content;

import java.util.Comparator;
import java.util.Iterator;

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.util.ListenerList;
import pivot.wtk.media.Image;

/**
 * Default tree node implementation.
 *
 * @author gbrown
 */
public class TreeNode implements List<TreeNode> {
    private Image icon = null;
    private Image expandedIcon = null;
    private String label = null;

    private ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
    private ListListenerList<TreeNode> listListeners = new ListListenerList<TreeNode>();

    public TreeNode() {
        this(null, null, null);
    }

    public TreeNode(Image icon) {
        this(icon, null, null);
    }

    public TreeNode(String label) {
        this(null, null, label);
    }

    public TreeNode(Image icon, String label) {
        this(icon, null, label);
    }

    public TreeNode(Image icon, Image expandedIcon, String label) {
        this.icon = icon;
        this.expandedIcon = expandedIcon;
        this.label = label;
    }

    public Image getIcon() {
        return icon;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    public Image getExpandedIcon() {
        return (expandedIcon == null) ? icon : expandedIcon;
    }

    public void setExpandedIcon(Image expandedIcon) {
        this.expandedIcon = expandedIcon;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int add(TreeNode treeNode) {
        int index = nodes.getLength();
        insert(treeNode, index);

        return index;
    }

    public void insert(TreeNode treeNode, int index) {
        nodes.insert(treeNode, index);
        listListeners.itemInserted(this, index);
    }

    public TreeNode update(int index, TreeNode treeNode) {
        TreeNode previousTreeNode = nodes.update(index, treeNode);
        listListeners.itemUpdated(this, index, previousTreeNode);

        return previousTreeNode;
    }

    public int remove(TreeNode treeNode) {
        return remove(treeNode);
    }

    public Sequence<TreeNode> remove(int index, int count) {
        Sequence<TreeNode> removed = nodes.remove(index, count);
        listListeners.itemsRemoved(this, index, removed);

        return removed;
    }

    public void clear() {
        nodes.clear();
        listListeners.itemsRemoved(this, 0, null);
    }

    public TreeNode get(int index) {
        return nodes.get(index);
    }

    public int indexOf(TreeNode treeNode) {
        return nodes.indexOf(treeNode);
    }

    public int getLength() {
        return nodes.getLength();
    }

    public Comparator<TreeNode> getComparator() {
        return nodes.getComparator();
    }

    public void setComparator(Comparator<TreeNode> comparator) {
        Comparator<TreeNode> previousComparator = nodes.getComparator();
        nodes.setComparator(comparator);
        listListeners.comparatorChanged(this, previousComparator);
    }

    public Iterator<TreeNode> iterator() {
        // TODO Wrap in a custom iterator to capture or prevent removals
        return nodes.iterator();
    }

    public ListenerList<ListListener<TreeNode>> getListListeners() {
        return listListeners;
    }
}
