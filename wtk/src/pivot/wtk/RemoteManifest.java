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
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import pivot.collections.ArrayList;
import pivot.serialization.PlainTextSerializer;
import pivot.serialization.SerializationException;
import pivot.util.MIMEType;
import pivot.wtk.data.Manifest;

class RemoteManifest extends Manifest {
    private Transferable transferable;
    private ArrayList<DataFlavor> content;

    public RemoteManifest(Transferable transferable) {
        this.transferable = transferable;

        // Extract applicable content
        content = new ArrayList<DataFlavor>();

        DataFlavor[] transferDataFlavors = transferable.getTransferDataFlavors();
        for (int i = 0, n = transferDataFlavors.length; i < n; i++) {
            DataFlavor dataFlavor = transferDataFlavors[i];

            if (dataFlavor.getRepresentationClass() == InputStream.class) {
                System.out.println(content.getLength() + " " + dataFlavor.getMimeType());
                content.add(dataFlavor);
            }
        }
    }

    @Override
    public String getMIMEType(int index) {
        return content.get(index).getMimeType();
    }

    @Override
    public InputStream getInputStream(int index) throws IOException {
        InputStream inputStream = null;

        try {
            DataFlavor dataFlavor = content.get(index);
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
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                    try {
                        serializer.writeObject(text, byteArrayOutputStream);
                    } catch(SerializationException exception) {
                        System.err.println(exception);
                    }

                    byteArrayOutputStream.close();

                    byte[] data = byteArrayOutputStream.toByteArray();
                    inputStream = new ByteArrayInputStream(data);
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
