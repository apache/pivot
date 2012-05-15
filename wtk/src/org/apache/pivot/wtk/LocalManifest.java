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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.io.FileList;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.media.Picture;

/**
 * Manifest class that serves as data source for a clipboard or drag/drop
 * operation.
 */
public class LocalManifest implements Manifest {
    private String text = null;
    private Image image = null;
    private FileList fileList = null;
    private HashMap<String, Object> values = new HashMap<String, Object>();

    @Override
    public String getText() {
        return text;
    }

    public void putText(String textArgument) {
        if (textArgument == null) {
            throw new IllegalArgumentException("text is null.");
        }

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
        if (imageArgument == null) {
            throw new IllegalArgumentException("image is null.");
        }

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
        if (fileListArgument == null) {
            throw new IllegalArgumentException("fileList is null.");
        }

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

class LocalManifestAdapter implements Transferable {
    private LocalManifest localManifest;
    private ArrayList<DataFlavor> transferDataFlavors = new ArrayList<DataFlavor>();

    private static final String URI_LIST_MIME_TYPE = "text/uri-list; class=java.lang.String";

    public LocalManifestAdapter(LocalManifest localManifest) {
        this.localManifest = localManifest;

        if (localManifest.containsText()) {
            transferDataFlavors.add(DataFlavor.stringFlavor);
        }

        if (localManifest.containsImage()) {
            transferDataFlavors.add(DataFlavor.imageFlavor);
        }

        if (localManifest.containsFileList()) {
            transferDataFlavors.add(DataFlavor.javaFileListFlavor);

            try {
                transferDataFlavors.add(new DataFlavor(URI_LIST_MIME_TYPE));
            } catch (ClassNotFoundException exception) {
                // No-op
            }
        }
    }

    @Override
    public Object getTransferData(DataFlavor dataFlavor)
        throws UnsupportedFlavorException {
        Object transferData = null;

        int index = transferDataFlavors.indexOf(dataFlavor);
        if (index == -1) {
            throw new UnsupportedFlavorException(dataFlavor);
        }

        if (dataFlavor.equals(DataFlavor.stringFlavor)) {
            transferData = localManifest.getText();
        } else if (dataFlavor.equals(DataFlavor.imageFlavor)) {
            Picture picture = (Picture)localManifest.getImage();
            transferData = picture.getBufferedImage();
        } else if (dataFlavor.equals(DataFlavor.javaFileListFlavor)) {
            FileList fileList = localManifest.getFileList();
            transferData = fileList.getList();
        } else if (dataFlavor.getMimeType().equals(URI_LIST_MIME_TYPE)) {
            FileList fileList = localManifest.getFileList();

            StringBuilder buf = new StringBuilder();
            for (File file : fileList) {
                buf.append(file.toURI().toString()).append("\r\n");
            }

            transferData = buf.toString();
        }

        return transferData;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return transferDataFlavors.toArray(DataFlavor[].class);
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
        return (transferDataFlavors.indexOf(dataFlavor) != -1);
    }
}
