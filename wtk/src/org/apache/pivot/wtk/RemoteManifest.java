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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.pivot.io.FileList;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.media.Picture;


/**
 * Manifest class that acts as a proxy to remote clipboard or drag/drop data.
 */
public class RemoteManifest implements Manifest {
    private Transferable transferable;

    private DataFlavor textDataFlavor = null;
    private DataFlavor imageDataFlavor = null;
    private DataFlavor fileListDataFlavor = null;
    private DataFlavor uriListDataFlavor = null;

    private static final String URI_LIST_MIME_TYPE = "text/uri-list";
    private static final String FILE_URI_SCHEME = "file";

    RemoteManifest(Transferable transferable) {
        assert(transferable != null);
        this.transferable = transferable;

        DataFlavor[] transferDataFlavors = transferable.getTransferDataFlavors();
        if (transferDataFlavors != null) {
            for (int i = 0, n = transferDataFlavors.length; i < n; i++) {
                DataFlavor dataFlavor = transferDataFlavors[i];

                if (dataFlavor.equals(DataFlavor.stringFlavor)) {
                    textDataFlavor = dataFlavor;
                } else if (dataFlavor.equals(DataFlavor.imageFlavor)) {
                    imageDataFlavor = dataFlavor;
                } else if (dataFlavor.equals(DataFlavor.javaFileListFlavor)) {
                    fileListDataFlavor = dataFlavor;
                } else if (dataFlavor.getMimeType().startsWith(URI_LIST_MIME_TYPE)
                    && dataFlavor.getRepresentationClass() == String.class) {
                    uriListDataFlavor = dataFlavor;
                }
            }
        }
    }

    @Override
    public String getText() throws IOException {
        String text = null;
        try {
            text = (String)transferable.getTransferData(textDataFlavor);
        } catch (UnsupportedFlavorException exception) {
            // No-op
        }

        return text;
    }

    @Override
    public boolean containsText() {
        return (textDataFlavor != null);
    }

    @Override
    public Image getImage() throws IOException {
        Image image = null;
        try {
            image = new Picture((BufferedImage)transferable.getTransferData(imageDataFlavor));
        } catch (UnsupportedFlavorException exception) {
            // No-op
        }

        return image;
    }

    @Override
    public boolean containsImage() {
        return (imageDataFlavor != null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FileList getFileList() throws IOException {
        FileList fileList = null;

        try {
            if (fileListDataFlavor != null) {
                fileList = new FileList((java.util.List<File>)
                    transferable.getTransferData(fileListDataFlavor));
            } else if (uriListDataFlavor != null) {
                fileList = new FileList();

                String uriList = (String)transferable.getTransferData(uriListDataFlavor);
                LineNumberReader reader = new LineNumberReader(new StringReader(uriList));

                try {
                    String line = reader.readLine();
                    while (line != null) {
                        URI uri = new URI(line);
                        String scheme = uri.getScheme();

                        if (scheme != null
                            && scheme.equalsIgnoreCase(FILE_URI_SCHEME)) {
                            File file = new File(uri);
                            fileList.add(file);
                        }

                        line = reader.readLine();
                    }
                } catch (URISyntaxException exception) {
                    // No-op
                }
            }
        } catch (UnsupportedFlavorException exception) {
            // No-op
        }

        return fileList;
    }

    @Override
    public boolean containsFileList() {
        return (fileListDataFlavor != null
            || uriListDataFlavor != null);
    }

    @Override
    public Object getValue(String key) {
        return null;
    }

    @Override
    public boolean containsValue(String key) {
        return false;
    }
}
