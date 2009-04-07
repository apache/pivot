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
package pivot.io;

import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;
import java.util.Iterator;

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.util.ImmutableIterator;
import pivot.util.ListenerList;

/**
 * Class representing a folder in the file system.
 * <p>
 * NOTE Because Java does not provide any way to monitor the file system for
 * changes, instances of this class must be refreshed periodically to reflect
 * updates. Instances must also be refreshed to perform initial population
 * immediately after construction. See {@link #refresh()}.
 *
 * @author gbrown
 */
public class Folder extends File implements List<File> {
    public static class FileNameComparator implements Comparator<File> {
        public int compare(File file1, File file2) {
            String path1 = file1.getPath();
            String path2 = file2.getPath();

            return path1.compareToIgnoreCase(path2);
        }
    }

    private static final long serialVersionUID = 0;

    private ArrayList<File> files;
    private FileFilter fileFilter;

    private Comparator<File> comparator = null;

    private transient ListListenerList<File> listListeners = new ListListenerList<File>();

    public Folder() {
        this("", null);
    }

    public Folder(FileFilter fileFilter) {
        this("", fileFilter);
    }

    public Folder(String pathname) {
        this(pathname, null);
    }

    public Folder(String pathname, FileFilter fileFilter) {
        super(pathname);

        if (!isDirectory()) {
            throw new IllegalArgumentException(this + " is not a directory.");
        }

        files = new ArrayList<File>();
        this.fileFilter = fileFilter;
    }

    /**
     * Returns the file filter that is applied to this folder.
     */
    public FileFilter getFileFilter() {
        return fileFilter;
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
        return files.get(index);
    }

    public int indexOf(File file) {
        int index = -1;
        if (comparator == null) {
            index = files.indexOf(file);
        }
        else {
            // Perform a binary search to find the index
            index = Search.binarySearch(this, file, comparator);
            if (index < 0) {
                index = -1;
            }
        }

        return index;
    }

    public int getLength() {
        return files.getLength();
    }

    /**
     * Refreshes the file list by requerying the file system for the current
     * contents.
     */
    public void refresh() {
        // Clear the list contents
        files.clear();
        listListeners.itemsRemoved(this, 0, null);

        // Refresh list and fire an insert event for each file
        File[] fileList;
        if (getPath().length() == 0) {
            fileList = listRoots();
        } else {
            fileList = listFiles(fileFilter);
        }

        if (fileList != null) {
            for (int i = 0; i < fileList.length; i++) {
                File file = fileList[i];

                if (!file.isHidden()) {
                    if (file.isDirectory()) {
                        file = new Folder(file.getPath(), fileFilter);
                    }

                    int index = -1;
                    if (comparator == null) {
                        index = getLength();
                    }
                    else {
                        // Perform a binary search to find the insertion point
                        index = Search.binarySearch(this, file, comparator);
                        if (index < 0) {
                            index = -(index + 1);
                        }
                    }

                    files.insert(file, index);
                    listListeners.itemInserted(this, files.getLength() - 1);
                }
            }
        }
    }

    public Comparator<File> getComparator() {
        return comparator;
    }

    public void setComparator(Comparator<File> comparator) {
        Comparator<File> previousComparator = this.comparator;

        if (previousComparator != comparator) {
            if (comparator != null) {
                Sequence.Sort.quickSort(files, comparator);
            }

            this.comparator = comparator;

            listListeners.comparatorChanged(this, previousComparator);
        }
    }

    public Iterator<File> iterator() {
        return new ImmutableIterator<File>(files.iterator());
    }

    public ListenerList<ListListener<File>> getListListeners() {
        return listListeners;
    }
}
