/*
 * Copyright (c) 2009 VMware, Inc.
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
package pivot.io;

import java.io.File;
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
 *
 * @author gbrown
 */
public class Folder extends File implements List<File> {
    private static final long serialVersionUID = 0;

    private ArrayList<File> files = null;

    private Comparator<File> comparator = null;
    private transient ListListenerList<File> listListeners = new ListListenerList<File>();

    public Folder() {
        super("");

        refresh();
    }

    public Folder(String pathname) {
        super(pathname);

        if (!isDirectory()) {
            throw new IllegalArgumentException(pathname + " is not a directory.");
        }
    }

    public int add(File item) {
        throw new UnsupportedOperationException();
    }

    public void insert(File item, int index) {
        throw new UnsupportedOperationException();
    }

    public File update(int index, File item) {
        throw new UnsupportedOperationException();
    }

    public int remove(File item) {
        throw new UnsupportedOperationException();
    }

    public Sequence<File> remove(int index, int count) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public File get(int index) {
        if (files == null) {
            refresh();
        }

        return files.get(index);
    }

    public int indexOf(File item) {
        if (files == null) {
            refresh();
        }

        int index = -1;
        if (comparator == null) {
            index = files.indexOf(item);
        }
        else {
            // Perform a binary search to find the index
            index = Search.binarySearch(this, item, comparator);
            if (index < 0) {
                index = -1;
            }
        }

        return index;
    }

    public int getLength() {
        if (files == null) {
            refresh();
        }

        return files.getLength();
    }

    public void refresh() {
        files = new ArrayList<File>();

        File[] fileList;
        if (getPath().length() == 0) {
            fileList = listRoots();
        } else {
            fileList = listFiles();
        }

        if (fileList != null) {
            for (int i = 0; i < fileList.length; i++) {
                File file = fileList[i];

                if (!file.isHidden()) {
                    if (file.isDirectory()) {
                        files.add(new Folder(file.getPath()));
                    } else {
                        files.add(file);
                    }
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
            if (comparator != null
                && files != null) {
                Sequence.Sort.quickSort(files, comparator);
            }

            this.comparator = comparator;

            listListeners.comparatorChanged(this, previousComparator);
        }
    }

    public Iterator<File> iterator() {
        if (files == null) {
            refresh();
        }

        return new ImmutableIterator<File>(files.iterator());
    }

    public ListenerList<ListListener<File>> getListListeners() {
        return listListeners;
    }

    public void setListListener(ListListener<File> listener) {
        listListeners.add(listener);
    }
}
