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
package org.apache.pivot.wtk.content;

import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.media.Image;


/**
 * Default tree branch implementation.
 *
 */
public class TreeBranch extends TreeNode implements List<TreeNode> {
    private Image expandedIcon = null;

    private ArrayList<TreeNode> treeNodes = new ArrayList<TreeNode>();
    private ListListenerList<TreeNode> listListeners = new ListListenerList<TreeNode>();

    public TreeBranch() {
        this(null, null, null);
    }

    public TreeBranch(Image icon) {
        this(icon, null, null);
    }

    public TreeBranch(String text) {
        this(null, null, text);
    }

    public TreeBranch(Image icon, String text) {
        this(icon, null, text);
    }

    public TreeBranch(Image icon, Image expandedIcon, String text) {
        super(icon, text);

        this.expandedIcon = expandedIcon;
    }

    public Image getExpandedIcon() {
        return expandedIcon;
    }

    public void setExpandedIcon(Image expandedIcon) {
        this.expandedIcon = expandedIcon;
    }

    public int add(TreeNode treeNode) {
        int index = treeNodes.add(treeNode);
        listListeners.itemInserted(this, index);

        return index;
    }

    public void insert(TreeNode treeNode, int index) {
        treeNodes.insert(treeNode, index);
        listListeners.itemInserted(this, index);
    }

    public TreeNode update(int index, TreeNode treeNode) {
        TreeNode previousTreeNode = treeNodes.update(index, treeNode);
        listListeners.itemUpdated(this, index, previousTreeNode);

        return previousTreeNode;
    }

    public int remove(TreeNode treeNode) {
        int index = treeNodes.indexOf(treeNode);
        if (index != -1) {
            remove(index, 1);
        }

        return index;
    }

    public Sequence<TreeNode> remove(int index, int count) {
        Sequence<TreeNode> removed = treeNodes.remove(index, count);
        listListeners.itemsRemoved(this, index, removed);

        return removed;
    }

    public void clear() {
        treeNodes.clear();
        listListeners.listCleared(this);
    }

    public TreeNode get(int index) {
        return treeNodes.get(index);
    }

    public int indexOf(TreeNode treeNode) {
        return treeNodes.indexOf(treeNode);
    }

    public int getLength() {
        return treeNodes.getLength();
    }

    public Comparator<TreeNode> getComparator() {
        return treeNodes.getComparator();
    }

    public void setComparator(Comparator<TreeNode> comparator) {
        Comparator<TreeNode> previousComparator = treeNodes.getComparator();
        treeNodes.setComparator(comparator);

        // Recursively apply comparator change
        for (int i = 0, n = treeNodes.getLength(); i < n; i++) {
            TreeNode treeNode = treeNodes.get(i);

            if (treeNode instanceof TreeBranch) {
                TreeBranch treeBranch = (TreeBranch)treeNode;
                treeBranch.setComparator(comparator);
            }
        }

        listListeners.comparatorChanged(this, previousComparator);
    }

    public Iterator<TreeNode> iterator() {
        return new ImmutableIterator<TreeNode>(treeNodes.iterator());
    }

    public ListenerList<ListListener<TreeNode>> getListListeners() {
        return listListeners;
    }
}
