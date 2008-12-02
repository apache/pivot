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
package pivot.io;

import java.io.File;

import pivot.collections.Sequence;
import pivot.collections.adapter.ListAdapter;

/**
 * A list of files, typically used during drag/drop operations.
 *
 * @author gbrown
 */
public class FileSequence implements Sequence<File> {
    private ListAdapter<File> files;

    public FileSequence() {
        this(new java.util.ArrayList<File>());
    }

    public FileSequence(java.util.List<File> files) {
        this.files = new ListAdapter<File>(files);
    }

    public java.util.List<File> getList() {
        return files.getList();
    }

    public int add(File file) {
        return files.add(file);
    }

    public void insert(File file, int index) {
        files.insert(file, index);
    }

    public File update(int index, File file) {
        return files.update(index, file);
    }

    public int remove(File file) {
        return files.remove(file);
    }

    public Sequence<File> remove(int index, int count) {
        return files.remove(index, count);
    }

    public File get(int index) {
        return files.get(index);
    }

    public int indexOf(File file) {
        return files.indexOf(file);
    }

    public int getLength() {
        return files.getLength();
    }
}
