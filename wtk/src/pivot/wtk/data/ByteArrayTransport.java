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
package pivot.wtk.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import pivot.serialization.SerializationException;
import pivot.serialization.Serializer;

/**
 * Transport backed by a byte array.
 *
 * @author gbrown
 */
public class ByteArrayTransport extends Transport {
    private byte[] data = null;

    public ByteArrayTransport(Object object, Serializer serializer) {
        super(object, serializer);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (data == null) {
            Object object = getObject();
            Serializer serializer = getSerializer();

            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                serializer.writeObject(object, outputStream);
                outputStream.close();

                data = outputStream.toByteArray();
            } catch(SerializationException exception) {
                System.out.println(exception);
            }
        }

        return (data == null) ? null : new ByteArrayInputStream(data);
    }

    public void dispose() {
        data = null;
    }
}
