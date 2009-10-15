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
import java.io.Serializable;
import java.util.Comparator;

import org.apache.pivot.collections.adapter.ListAdapter;


/**
 * Collection representing a list of files. Each entry in the list is unique;
 * i.e. a single file can't be added to the list more than once.
 */
public class FileList extends ListAdapter<File> {
    private static final long serialVersionUID = -6741822480264805279L;

    private static class FilePathComparator implements Comparator<File>, Serializable {
        private static final long serialVersionUID = 6341769187574031281L;

        @Override
        public int compare(File file1, File file2) {
            String path1 = file1.getPath();
            String path2 = file2.getPath();

            return path1.compareTo(path2);
        }
    }

    private static final FilePathComparator filePathComparator = new FilePathComparator();

    public FileList() {
        this(new java.util.ArrayList<File>());
    }

    public FileList(java.util.List<File> files) {
        super(files);

        super.setComparator(filePathComparator);
    }

    @Override
    public int add(File file) {
        int index = indexOf(file);

        if (index == -1) {
            index = super.add(file);
        }

        return index;
    }

    @Override
    public void insert(File file, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public File update(int index, File file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setComparator(Comparator<File> comparator) {
        throw new UnsupportedOperationException();
    }
}
