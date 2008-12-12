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

import pivot.collections.ArrayList;
import pivot.collections.adapter.ListAdapter;
import pivot.serialization.PlainTextSerializer;
import pivot.serialization.URIListSerializer;
import pivot.util.MIMEType;
import pivot.wtk.data.ByteArrayTransport;
import pivot.wtk.data.Manifest;
import pivot.wtk.media.BufferedImageSerializer;

class RemoteManifest extends Manifest {
    private Transferable transferable;
    private ArrayList<DataFlavor> content;

    private static BufferedImageSerializer bufferedImageSerializer = new BufferedImageSerializer();
    private static URIListSerializer uriListSerializer = new URIListSerializer();

    static {
        bufferedImageSerializer.setOutputFormat(BufferedImageSerializer.Format.PNG);
    }

    public RemoteManifest(Transferable transferable) {
        this.transferable = transferable;

        // Extract applicable content
        content = new ArrayList<DataFlavor>();

        DataFlavor[] transferDataFlavors = transferable.getTransferDataFlavors();
        for (int i = 0, n = transferDataFlavors.length; i < n; i++) {
            DataFlavor dataFlavor = transferDataFlavors[i];
            System.out.println(dataFlavor.getMimeType());

            if (dataFlavor.equals(DataFlavor.imageFlavor)
                || dataFlavor.equals(DataFlavor.javaFileListFlavor)
                || dataFlavor.isRepresentationClassInputStream()) {
                System.out.println(content.getLength() + " " + dataFlavor.getMimeType());
                content.add(dataFlavor);
            }
        }
    }

    @Override
    public String getMIMEType(int index) {
        String mimeType;

        DataFlavor dataFlavor = content.get(index);

        if (dataFlavor.equals(DataFlavor.imageFlavor)) {
            mimeType = bufferedImageSerializer.getMIMEType(null);
        } else if (dataFlavor.equals(DataFlavor.javaFileListFlavor)) {
            mimeType = uriListSerializer.getMIMEType(null);
        } else {
            mimeType = dataFlavor.getMimeType();
        }

        return mimeType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public InputStream getInputStream(int index) throws IOException {
        InputStream inputStream = null;

        try {
            DataFlavor dataFlavor = content.get(index);

            if (dataFlavor.equals(DataFlavor.imageFlavor)) {
                // Write the image to a memory buffer as a PNG
                BufferedImage bufferedImage = (BufferedImage)transferable.getTransferData(dataFlavor);
                ByteArrayTransport transport = new ByteArrayTransport(bufferedImage, bufferedImageSerializer);
                inputStream = transport.getInputStream();
            } else if (dataFlavor.equals(DataFlavor.javaFileListFlavor)) {
                // Wrap the java.util.List<File> in a ListAdapter and
                // write it to a memory buffer
                java.util.List<File> fileList = (java.util.List<File>)transferable.getTransferData(dataFlavor);
                ListAdapter<File> fileListAdapter = new ListAdapter<File>(fileList);
                ByteArrayTransport transport = new ByteArrayTransport(fileListAdapter, uriListSerializer);
                inputStream = transport.getInputStream();
            } else {
                Object transferData = transferable.getTransferData(dataFlavor);

                if (transferData instanceof InputStream) {
                    inputStream = (InputStream)transferData;
                } else {
                    // NOTE This is a workaround for Sun bug #4147507
                    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4147507

                    if (transferData instanceof Reader) {
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
                        ByteArrayTransport transport = new ByteArrayTransport(text, serializer);
                        inputStream = transport.getInputStream();
                    }
                }
            }
        } catch(UnsupportedFlavorException exception) {
            // No-op; shouldn't get here
        }

        return inputStream;
    }

    @Override
    public int getLength() {
        return content.getLength();
    }
}
