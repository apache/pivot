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
package pivot.wtk;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;

import pivot.collections.ArrayList;
import pivot.net.URIListSerializer;
import pivot.serialization.PlainTextSerializer;
import pivot.util.MIMEType;
import pivot.wtk.data.ByteArrayTransport;
import pivot.wtk.data.Manifest;
import pivot.wtk.data.Transport;
import pivot.wtk.media.BufferedImageSerializer;

class RemoteManifest extends Manifest {
    private class ContentProxy {
        private DataFlavor dataFlavor;
        private Transport transport = null;

        public ContentProxy(DataFlavor dataFlavor) {
            this.dataFlavor = dataFlavor;
        }

        public String getMIMEType() {
            String mimeType;

            if (dataFlavor.equals(DataFlavor.imageFlavor)) {
                mimeType = bufferedImageSerializer.getMIMEType(null);
            } else if (dataFlavor.equals(DataFlavor.javaFileListFlavor)) {
                mimeType = uriListSerializer.getMIMEType(null);
            } else {
                mimeType = dataFlavor.getMimeType();
            }

            return mimeType;
        }

        @SuppressWarnings("unchecked")
        public InputStream getInputStream() throws IOException {
            InputStream inputStream = null;

            if (dataFlavor.equals(DataFlavor.imageFlavor)) {
                if (transport == null) {
                    // Write the image to a memory buffer as a PNG
                    BufferedImage bufferedImage = null;
                    try {
                        bufferedImage = (BufferedImage)transferable.getTransferData(dataFlavor);
                    } catch(UnsupportedFlavorException exception) {
                        // No-op; shouldn't get here
                    }

                    transport = new ByteArrayTransport(bufferedImage, bufferedImageSerializer);
                }

                inputStream = transport.getInputStream();
            } else if (dataFlavor.equals(DataFlavor.javaFileListFlavor)) {
                if (transport == null) {
                    // Wrap the java.util.List<File> in a ListAdapter and write it
                    // to a memory buffer
                    java.util.List<File> fileList = null;
                    try {
                        fileList = (java.util.List<File>)transferable.getTransferData(dataFlavor);
                    } catch(UnsupportedFlavorException exception) {
                        // No-op; shouldn't get here
                    }

                    ArrayList<URL> urlList = new ArrayList<URL>();
                    for (File file : fileList) {
                        URI uri = file.toURI();
                        urlList.add(uri.toURL());
                    }

                    transport = new ByteArrayTransport(urlList, uriListSerializer);
                }

                inputStream = transport.getInputStream();
            } else {
                Object transferData = null;
                try {
                    transferData = transferable.getTransferData(dataFlavor);
                } catch(UnsupportedFlavorException exception) {
                    // No-op; shouldn't get here
                }

                if (transferData instanceof InputStream) {
                    inputStream = (InputStream)transferData;
                } else {
                    // NOTE This is a workaround for Sun bug #4147507
                    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4147507
                    assert (transferData instanceof Reader);

                    if (transport == null) {
                        // Read the data into a string buffer
                        Reader reader = (Reader)transferData;
                        BufferedReader bufferedReader = new BufferedReader(reader);

                        StringBuilder stringBuilder = new StringBuilder();

                        String line = bufferedReader.readLine();
                        while (line != null) {
                            stringBuilder.append(line);
                            line = bufferedReader.readLine();
                        }

                        bufferedReader.close();

                        // Write the data to a memory buffer
                        String text = stringBuilder.toString();

                        MIMEType mimeType = MIMEType.decode(dataFlavor.getMimeType());
                        PlainTextSerializer serializer = new PlainTextSerializer(mimeType.get("charset"));
                        transport = new ByteArrayTransport(text, serializer);
                    }

                    inputStream = transport.getInputStream();
                }
            }

            return inputStream;
        }

        public void dispose() {
            if (transport != null) {
                transport.dispose();
            }
        }
    }

    private Transferable transferable;
    private ArrayList<ContentProxy> content;

    private static BufferedImageSerializer bufferedImageSerializer = new BufferedImageSerializer();
    private static URIListSerializer uriListSerializer = new URIListSerializer();

    static {
        bufferedImageSerializer.setOutputFormat(BufferedImageSerializer.Format.PNG);
    }

    public RemoteManifest(Transferable transferable) {
        this.transferable = transferable;

        // Extract applicable content
        content = new ArrayList<ContentProxy>();

        DataFlavor[] transferDataFlavors = transferable.getTransferDataFlavors();
        for (int i = 0, n = transferDataFlavors.length; i < n; i++) {
            DataFlavor dataFlavor = transferDataFlavors[i];

            if (dataFlavor.equals(DataFlavor.imageFlavor)
                || dataFlavor.equals(DataFlavor.javaFileListFlavor)
                || dataFlavor.isRepresentationClassInputStream()) {
                content.add(new ContentProxy(dataFlavor));
            }
        }
    }

    @Override
    public String getMIMEType(int index) {
        if (index < 0 || index >= content.getLength()) {
            throw new IndexOutOfBoundsException();
        }

        ContentProxy proxy = content.get(index);
        return proxy.getMIMEType();
    }

    @Override
    public InputStream getInputStream(int index) throws IOException {
        if (index < 0 || index >= content.getLength()) {
            throw new IndexOutOfBoundsException();
        }

        ContentProxy proxy = content.get(index);
        return proxy.getInputStream();
    }

    @Override
    public int getLength() {
        return content.getLength();
    }

    @Override
    public void dispose() {
        for (ContentProxy proxy : content) {
            proxy.dispose();
        }
    }
}
