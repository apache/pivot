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
package org.apache.pivot.scene.data;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.pivot.scene.media.Image;

/**
 * Manifest class that serves as data source for a clipboard or drag/drop
 * operation.
 */
public class LocalManifest implements Manifest {
    private String text = null;
    private Image image = null;
    private List<File> fileList = null;

    @Override
    public String getText() {
        return text;
    }

    public void putText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text is null.");
        }

        this.text = text;
    }

    @Override
    public boolean containsText() {
        return (text != null);
    }

    @Override
    public Image getImage() {
        return image;
    }

    public void putImage(Image image) {
        if (image == null) {
            throw new IllegalArgumentException("image is null.");
        }

        this.image = image;
    }

    @Override
    public boolean containsImage() {
        return image != null;
    }

    @Override
    public List<File> getFileList() {
        return fileList;
    }

    public void putFileList(List<File> fileList) {
        if (fileList == null) {
            throw new IllegalArgumentException("fileList is null.");
        }

        this.fileList = fileList;
    }

    @Override
    public boolean containsFileList() {
        return fileList != null;
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
            transferData = localManifest.getImage();
        } else if (dataFlavor.equals(DataFlavor.javaFileListFlavor)) {
            transferData = localManifest.getFileList();
        } else if (dataFlavor.getMimeType().equals(URI_LIST_MIME_TYPE)) {
            List<File> fileList = localManifest.getFileList();

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
        DataFlavor[] transferDataFlavors = new DataFlavor[this.transferDataFlavors.size()];
        this.transferDataFlavors.toArray(transferDataFlavors);

        return transferDataFlavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
        return (transferDataFlavors.indexOf(dataFlavor) != -1);
    }
}
