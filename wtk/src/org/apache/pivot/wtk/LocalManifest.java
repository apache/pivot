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
package org.apache.pivot.wtk;

import org.apache.pivot.collections.HashMap;
import org.apache.pivot.io.FileList;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.media.Image;

/**
 * Manifest class that serves as data source for a clipboard or drag/drop
 * operation.
 */
public class LocalManifest implements Manifest {
    private String text = null;
    private Image image = null;
    private FileList fileList = null;
    private HashMap<String, Object> values = new HashMap<>();

    @Override
    public String getText() {
        return text;
    }

    public void putText(String textArgument) {
        Utils.checkNull(textArgument, "text");

        this.text = textArgument;
    }

    @Override
    public boolean containsText() {
        return (text != null);
    }

    @Override
    public Image getImage() {
        return image;
    }

    public void putImage(Image imageArgument) {
        Utils.checkNull(imageArgument, "image");

        this.image = imageArgument;
    }

    @Override
    public boolean containsImage() {
        return image != null;
    }

    @Override
    public FileList getFileList() {
        return fileList;
    }

    public void putFileList(FileList fileListArgument) {
        Utils.checkNull(fileListArgument, "fileList");

        this.fileList = fileListArgument;
    }

    @Override
    public boolean containsFileList() {
        return fileList != null;
    }

    @Override
    public Object getValue(String key) {
        return values.get(key);
    }

    public Object putValue(String key, Object value) {
        return values.put(key, value);
    }

    @Override
    public boolean containsValue(String key) {
        return values.containsKey(key);
    }
}

