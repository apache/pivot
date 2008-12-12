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
import java.io.IOException;
import java.io.InputStream;

import pivot.collections.Sequence;
import pivot.serialization.Serializer;
import pivot.wtk.data.Manifest;
import pivot.wtk.data.Transport;

class LocalManifest extends Manifest {
    private Sequence<Transport> content;

    private Transferable transferable = new Transferable() {
        public Object getTransferData(DataFlavor flavor) throws IOException {
            int index = getIndex(flavor.getMimeType());
            return (index == -1) ? null : getInputStream(index);
        }

        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] transferDataFlavors = new DataFlavor[content.getLength()];

            for (int i = 0, n = content.getLength(); i < n; i++) {
                Transport transport = content.get(i);
                Serializer serializer = transport.getSerializer();
                Object object = transport.getObject();
                String mimeType = serializer.getMIMEType(object);

                try {
                    DataFlavor dataFlavor = new DataFlavor(mimeType);
                    transferDataFlavors[i] = dataFlavor;
                } catch(ClassNotFoundException exception) {
                    // No-op; since we don't append a class name, InputStream
                    // will be used by default and we shouldn't get here
                }
            }

            return transferDataFlavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return (getIndex(flavor.getMimeType()) != -1);
        }
    };

    public LocalManifest(Sequence<Transport> content) {
        assert (content != null);
        this.content = content;
    }

    @Override
    public String getMIMEType(int index) {
        Transport transport = content.get(index);
        Object object = transport.getObject();
        Serializer serializer = transport.getSerializer();

        return serializer.getMIMEType(object);
    }

    @Override
    public InputStream getInputStream(int index) throws IOException {
        return content.get(index).getInputStream();
    }

    @Override
    public int getLength() {
        return content.getLength();
    }

    public Transferable getTransferable() {
        return transferable;
    }

    public void dispose() {
        for (int i = 0, n = content.getLength(); i < n; i++) {
            Transport transport = content.get(i);
            transport.dispose();
        }
    }
}
