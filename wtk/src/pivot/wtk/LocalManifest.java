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

    public LocalManifest(Sequence<Transport> content) {
        assert (content != null);
        this.content = content;
    }

    @Override
    public String getMIMEType(int index) {
        if (index < 0 || index >= content.getLength()) {
            throw new IndexOutOfBoundsException();
        }

        Transport transport = content.get(index);
        Object object = transport.getObject();
        Serializer serializer = transport.getSerializer();

        return serializer.getMIMEType(object);
    }

    @Override
    public InputStream getInputStream(int index) throws IOException {
        if (index < 0 || index >= content.getLength()) {
            throw new IndexOutOfBoundsException();
        }

        return content.get(index).getInputStream();
    }

    @Override
    public int getLength() {
        return content.getLength();
    }

    @Override
    public void dispose() {
        for (int i = 0, n = content.getLength(); i < n; i++) {
            Transport transport = content.get(i);
            transport.dispose();
        }
    }
}

class Export implements Transferable {
    private LocalManifest localManifest;

    public Export(LocalManifest localManifest) {
        this.localManifest = localManifest;
    }

    public Object getTransferData(DataFlavor flavor) throws IOException {
        int index = localManifest.getIndex(flavor.getMimeType());
        return (index == -1) ? null : localManifest.getInputStream(index);
    }

    public DataFlavor[] getTransferDataFlavors() {
        int n = localManifest.getLength();
        DataFlavor[] transferDataFlavors = new DataFlavor[n];

        for (int i = 0; i < n; i++) {
            String mimeType = localManifest.getMIMEType(i);

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
        return (localManifest.getIndex(flavor.getMimeType()) != -1);
    }
}

