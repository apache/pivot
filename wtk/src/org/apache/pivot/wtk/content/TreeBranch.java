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

import java.net.URL;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.media.Image;

/**
 * Default tree branch implementation.
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

    /**
     * Sets the tree branch's expanded icon by URL.
     * <p>
     * If the icon already exists in the application context resource cache,
     * the cached value will be used. Otherwise, the icon will be loaded
     * synchronously and added to the cache.
     *
     * @param expandedIconURL
     * The location of the expanded icon to set.
     */
    public void setExpandedIcon(URL expandedIconURL) {
        if (expandedIconURL == null) {
            throw new IllegalArgumentException("expandedIconURL is null.");
        }

        Image icon = (Image)ApplicationContext.getResourceCache().get(expandedIconURL);

        if (icon == null) {
            try {
                icon = Image.load(expandedIconURL);
            } catch (TaskExecutionException exception) {
                throw new IllegalArgumentException(exception);
            }

            ApplicationContext.getResourceCache().put(expandedIconURL, icon);
        }

        setExpandedIcon(icon);
    }

    /**
     * Sets the tree branch's expanded icon by {@linkplain ClassLoader#getResource(String)
     * resource name}.
     *
     * @param expandedIconName
     * The resource name of the expanded icon to set.
     *
     * @see #setExpandedIcon(URL)
     */
    public void setExpandedIcon(String expandedIconName) {
        if (expandedIconName == null) {
            throw new IllegalArgumentException("expandedIconName is null.");
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(expandedIconName.substring(1));
        if (url == null) {
            throw new IllegalArgumentException("cannot find expandedIcon resource " + expandedIconName);
        }
        setExpandedIcon(url);
    }

    @Override
    public int add(TreeNode treeNode) {
        if (treeNode == null) {
            throw new IllegalArgumentException("treeNode is null.");
        }

        if (treeNode.getParent() != null) {
            throw new IllegalArgumentException("treeNode already has a parent.");
        }

        int index = treeNodes.add(treeNode);
        treeNode.setParent(this);
        listListeners.itemInserted(this, index);

        return index;
    }

    @Override
    public void insert(TreeNode treeNode, int index) {
        if (treeNode == null) {
            throw new IllegalArgumentException("treeNode is null.");
        }

        if (treeNode.getParent() != null) {
            throw new IllegalArgumentException("treeNode already has a parent.");
        }

        treeNodes.insert(treeNode, index);
        treeNode.setParent(this);
        listListeners.itemInserted(this, index);
    }

    @Override
    public TreeNode update(int index, TreeNode treeNode) {
        if (treeNode == null) {
            throw new IllegalArgumentException("treeNode is null.");
        }

        if (treeNode.getParent() != null) {
            throw new IllegalArgumentException("treeNode already has a parent.");
        }

        TreeNode previousTreeNode = treeNodes.update(index, treeNode);
        previousTreeNode.setParent(null);
        treeNode.setParent(this);
        listListeners.itemUpdated(this, index, previousTreeNode);

        return previousTreeNode;
    }

    @Override
    public int remove(TreeNode treeNode) {
        int index = indexOf(treeNode);
        if (index != -1) {
            remove(index, 1);
        }

        return index;
    }

    @Override
    public Sequence<TreeNode> remove(int index, int count) {
        Sequence<TreeNode> removed = treeNodes.remove(index, count);
        if (count > 0) {
            for (int i = 0, n = removed.getLength(); i < n; i++ ) {
                TreeNode treeNode = removed.get(i);
                treeNode.setParent(null);
            }

            listListeners.itemsRemoved(this, index, removed);
        }

        return removed;
    }

    @Override
    public void clear() {
        if (getLength() > 0) {
            for (int i = 0, n = treeNodes.getLength(); i < n; i++) {
                TreeNode treeNode = treeNodes.get(i);
                treeNode.setParent(null);
            }

            treeNodes.clear();
            listListeners.listCleared(this);
        }
    }

    @Override
    public TreeNode get(int index) {
        return treeNodes.get(index);
    }

    @Override
    public int indexOf(TreeNode treeNode) {
        // We can't use the ArrayList indexOf method, because if we have a comparator, it
        // might return the wrong answer.
        int index = 0;
        int length = treeNodes.getLength();
        while (index < length) {
            TreeNode node = treeNodes.get(index);
            if (treeNode == null) {
                if (node == null) {
                    break;
                }
            } else {
                if (treeNode.equals(node)) {
                    break;
                }
            }

            index++;
        }

        if (index == length) {
            index = -1;
        }

        return index;
    }

    @Override
    public boolean isEmpty() {
        return treeNodes.isEmpty();
    }

    @Override
    public int getLength() {
        return treeNodes.getLength();
    }

    @Override
    public Comparator<TreeNode> getComparator() {
        return treeNodes.getComparator();
    }

    @Override
    public void setComparator(Comparator<TreeNode> comparator) {
        Comparator<TreeNode> previousComparator = treeNodes.getComparator();

        if (previousComparator != comparator) {
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
    }

    @Override
    public Iterator<TreeNode> iterator() {
        return new ImmutableIterator<TreeNode>(treeNodes.iterator());
    }

    @Override
    public ListenerList<ListListener<TreeNode>> getListListeners() {
        return listListeners;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getClass().getName());
        sb.append(" [");

        int i = 0;
        for (TreeNode item : treeNodes) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(item);
            i++;
        }

        sb.append("]");

        return sb.toString();
    }
}
