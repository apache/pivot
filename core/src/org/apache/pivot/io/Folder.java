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
package org.apache.pivot.io;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;

/**
 * Class representing a folder in the file system.
 *
 * @author gbrown
 */
public class Folder extends File implements List<File> {
    /**
     * Default file name comparator.
     *
     * @author gbrown
     */
    public static class FileNameComparator implements Comparator<File>, Serializable {
        private static final long serialVersionUID = 0;

        public int compare(File file1, File file2) {
            String path1 = file1.getPath();
            String path2 = file2.getPath();

            return path1.compareToIgnoreCase(path2);
        }
    }

    private static class FolderListenerList extends ListenerList<FolderListener>
        implements FolderListener {
        public void fileFilterChanged(Folder folder, Filter<File> previousFileFilter) {
            for (FolderListener listener : this) {
                listener.fileFilterChanged(folder, previousFileFilter);
            }
        }
    }

    private static final long serialVersionUID = 0;

    private ArrayList<File> files = null;

    private transient Filter<File> fileFilter = null;
    private transient Comparator<File> comparator = null;

    private transient ListListenerList<File> listListeners = new ListListenerList<File>();
    private transient FolderListenerList folderListeners = new FolderListenerList();

    public Folder() {
        this("");
    }

    public Folder(String pathname) {
        this(pathname, null);
    }

    public Folder(String pathname, Filter<File> fileFilter) {
        this(pathname, fileFilter, new FileNameComparator());
    }

    public Folder(String pathname, Filter<File> fileFilter, Comparator<File> comparator) {
        super(pathname);

        if (!isDirectory()) {
            throw new IllegalArgumentException(this + " is not a directory.");
        }

        // We don't need to call the setters here since there is no data to
        // sort or filter yet
        this.fileFilter = fileFilter;
        this.comparator = comparator;
    }

    /**
     * Returns the file filter that is applied to this folder.
     */
    public Filter<File> getFileFilter() {
        return fileFilter;
    }

    /**
     * Sets the file filter that is applied to this folder.
     *
     * @param fileFilter
     */
    public void setFileFilter(Filter<File> fileFilter) {
        Filter<File> previousFileFilter = this.fileFilter;

        if (fileFilter != previousFileFilter) {
            this.fileFilter = fileFilter;

            if (files != null) {
                refresh();
            }

            folderListeners.fileFilterChanged(this, previousFileFilter);
        }
    }

    /**
     * This method is not supported.
     */
    public int add(File file) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported.
     */
    public void insert(File file, int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported.
     */
    public File update(int index, File file) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported.
     */
    public int remove(File file) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported.
     */
    public Sequence<File> remove(int index, int count) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported.
     */
    public void clear() {
        throw new UnsupportedOperationException();
    }

    public File get(int index) {
        if (files == null) {
            refresh();
        }

        return files.get(index);
    }

    public int indexOf(File file) {
        if (files == null) {
            refresh();
        }

        return files.indexOf(file);
    }

    public int getLength() {
        if (files == null) {
            refresh();
        }

        return files.getLength();
    }

    public Comparator<File> getComparator() {
        return comparator;
    }

    public void setComparator(Comparator<File> comparator) {
        Comparator<File> previousComparator = this.comparator;

        if (comparator != previousComparator) {
            this.comparator = comparator;

            if (files != null) {
                refresh();
            }

            listListeners.comparatorChanged(this, previousComparator);
        }
    }

    /**
     * Refreshes the file list by requerying the file system for the current
     * contents.
     */
    public void refresh() {
        // Get the current folder contents
        File[] fileList;
        if (getPath().length() == 0) {
            fileList = listRoots();
        } else {
            fileList = listFiles(new FileFilter() {
                public boolean accept(File file) {
                    return fileFilter.include(file);
                }
            });
        }

        // Clear the file list
        files = new ArrayList<File>(fileList.length);
        listListeners.listCleared(this);

        // Add the new files, firing an insert event for each file
        if (fileList != null) {
            for (int i = 0; i < fileList.length; i++) {
                File file = fileList[i];

                if (!file.isHidden()) {
                    if (file.isDirectory()) {
                        Folder folder = new Folder(file.getPath(), fileFilter, comparator);
                        folder.setComparator(files.getComparator());
                        file = folder;
                    }

                    int index = files.add(file);
                    listListeners.itemInserted(this, index);
                }
            }
        }

        if (comparator != null) {
            ArrayList.sort(files, comparator);
        }
    }

    public Iterator<File> iterator() {
        return new ImmutableIterator<File>(files.iterator());
    }

    public ListenerList<ListListener<File>> getListListeners() {
        return listListeners;
    }

    public ListenerList<FolderListener> getFolderListeners() {
        return folderListeners;
    }
}
